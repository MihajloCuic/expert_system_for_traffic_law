package com.ftn.sbnz.service.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.drools.template.DataProvider;
import org.drools.template.DataProviderCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Builds the {@link KieContainer} programmatically by combining:
 *   1. {@code META-INF/kmodule.xml}                         (from kjar classpath)
 *   2. All static {@code rules/**.drl} files                (from kjar classpath)
 *   3. Runtime-compiled DRL produced by Drools Templates    ({@code .drt} + {@code .csv})
 *
 * The fourth required Drools technique for Module 1 is **Templates (.drt)**.
 * Three templates are compiled here:
 *   - {@code speeding_by_location.drt} - URBAN/OPEN_ROAD/SCHOOL_ZONE speeding tariff
 *   - {@code speeding_by_vehicle.drt}  - BUS-specific speeding tariff
 *   - {@code alcohol_by_bac.drt}       - ALCOHOL tariff by blood alcohol level (per mille)
 */
@Configuration
public class DroolsConfig {

    private static final String KMODULE_LOCATION = "classpath:META-INF/kmodule.xml";
    private static final String STATIC_RULES_PATTERN = "classpath*:rules/**/*.drl";

    @Bean
    public KieContainer kieContainer() throws IOException {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        // --- 1. kmodule.xml ---------------------------------------------------
        Resource kmodule = resolver.getResource(KMODULE_LOCATION);
        kfs.write("src/main/resources/META-INF/kmodule.xml",
                  readAll(kmodule.getInputStream()));

        // --- 2. all static DRL files from kjar --------------------------------
        Resource[] drlResources = resolver.getResources(STATIC_RULES_PATTERN);
        int staticRules = 0;
        for (Resource r : drlResources) {
            String virtualPath = toVirtualPath(r);
            kfs.write(virtualPath, readAll(r.getInputStream()));
            staticRules++;
        }
        System.out.println("[KIE] Loaded " + staticRules + " static DRL files");

        // --- 3. template-generated DRL ----------------------------------------
        int templateRules = 0;
        templateRules += compileTemplate(resolver, kfs,
                "rules/templates/speeding_by_location.drt",
                "rules/templates/speeding_by_location.csv",
                "src/main/resources/rules/templates/generated/speeding_by_location_gen.drl");
        templateRules += compileTemplate(resolver, kfs,
                "rules/templates/speeding_by_vehicle.drt",
                "rules/templates/speeding_by_vehicle.csv",
                "src/main/resources/rules/templates/generated/speeding_by_vehicle_gen.drl");
        templateRules += compileTemplate(resolver, kfs,
                "rules/templates/alcohol_by_bac.drt",
                "rules/templates/alcohol_by_bac.csv",
                "src/main/resources/rules/templates/generated/alcohol_by_bac_gen.drl");
        System.out.println("[KIE] Generated " + templateRules
                + " rules from .drt templates (Drools Templates technique)");

        // --- 4. build the KieContainer ----------------------------------------
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Drools build failed:\n"
                    + kb.getResults().getMessages(Message.Level.ERROR));
        }
        System.out.println("[KIE] KieContainer built successfully");
        return ks.newKieContainer(kb.getKieModule().getReleaseId());
    }

    /**
     * Reads a {@code .drt} template + its {@code .csv} data file from the
     * classpath, runs them through {@link DataProviderCompiler}, and writes
     * the produced DRL into the supplied {@link KieFileSystem}.
     *
     * @return the number of rules produced (= number of CSV data rows)
     */
    private int compileTemplate(PathMatchingResourcePatternResolver resolver,
                                KieFileSystem kfs,
                                String templateClasspath,
                                String csvClasspath,
                                String outputKfsPath) throws IOException {
        Resource templateRes = resolver.getResource("classpath:" + templateClasspath);
        Resource csvRes = resolver.getResource("classpath:" + csvClasspath);

        CsvDataProvider provider = new CsvDataProvider(csvRes.getInputStream());
        DataProviderCompiler compiler = new DataProviderCompiler();
        String drl;
        try (InputStream tplStream = templateRes.getInputStream()) {
            drl = compiler.compile(provider, tplStream);
        }
        kfs.write(outputKfsPath, drl);
        System.out.println("[KIE] Template " + templateClasspath
                + " -> " + provider.rowCount() + " rules");
        return provider.rowCount();
    }

    /**
     * Maps a classpath resource URL like
     *   {@code jar:file:/.../kjar.jar!/rules/sanctions/qualification_misc.drl}
     * or
     *   {@code file:/.../target/classes/rules/sanctions/qualification_misc.drl}
     * into a {@link KieFileSystem} virtual path like
     *   {@code src/main/resources/rules/sanctions/qualification_misc.drl}.
     */
    private String toVirtualPath(Resource r) throws IOException {
        String url = r.getURL().toString();
        int idx = url.lastIndexOf("/rules/");
        if (idx < 0) {
            throw new IllegalStateException("Unexpected DRL location: " + url);
        }
        return "src/main/resources" + url.substring(idx);
    }

    private byte[] readAll(InputStream is) throws IOException {
        try (InputStream s = is) {
            return s.readAllBytes();
        }
    }

    // ============================================================
    // Tiny CSV reader (no extra dependency).
    //
    // Separator is ';' so that field values may contain commas - e.g.
    // lawArticle = "ZOBS art. 43, 45 par. 1 pts. 1-3 (penal art. 333)".
    //
    // First line is treated as the header row and discarded; the column
    // order in the CSV must match the order of placeholders declared in
    // the .drt "template header" block.
    // ============================================================
    private static class CsvDataProvider implements DataProvider {

        private final BufferedReader reader;
        private String nextLine;
        private int rowCount = 0;

        CsvDataProvider(InputStream is) throws IOException {
            this.reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8));
            this.reader.readLine();          // discard header row
            this.nextLine = this.reader.readLine();
            // skip blank lines, if any
            while (nextLine != null && nextLine.trim().isEmpty()) {
                nextLine = reader.readLine();
            }
        }

        @Override
        public boolean hasNext() {
            return nextLine != null;
        }

        @Override
        public String[] next() {
            String[] tokens = nextLine.split(";", -1);
            rowCount++;
            try {
                nextLine = reader.readLine();
                while (nextLine != null && nextLine.trim().isEmpty()) {
                    nextLine = reader.readLine();
                }
            } catch (IOException e) {
                nextLine = null;
            }
            return tokens;
        }

        int rowCount() {
            return rowCount;
        }
    }
}
