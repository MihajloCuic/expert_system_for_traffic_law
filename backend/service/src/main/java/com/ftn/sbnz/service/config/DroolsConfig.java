package com.ftn.sbnz.service.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        KieServices ks = KieServices.Factory.get();
        return ks.newKieContainer(
            ks.newReleaseId("com.ftn.sbnz", "kjar", "0.0.1-SNAPSHOT"));
    }
}
