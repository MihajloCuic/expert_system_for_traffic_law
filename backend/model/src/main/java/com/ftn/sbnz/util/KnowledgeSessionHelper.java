package com.ftn.sbnz.util;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

public class KnowledgeSessionHelper {

    public static KieContainer createRuleBase() {
        KieServices ks = KieServices.Factory.get();
        return ks.getKieClasspathContainer();
    }

    public static StatelessKieSession getStatelessKnowledgeSession(KieContainer kc, String sessionName) {
        return kc.newStatelessKieSession(sessionName);
    }

    public static KieSession getStatefulKnowledgeSession(KieContainer kc, String sessionName) {
        return kc.newKieSession(sessionName);
    }
}
