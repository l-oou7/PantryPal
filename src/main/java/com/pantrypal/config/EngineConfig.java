package com.pantrypal.config;

import com.pantrypal.engine.MatchCalculator;
import com.pantrypal.engine.RecommendationEngine;
import com.pantrypal.engine.SubstitutionEngine;
import com.pantrypal.engine.strategy.StrategyRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the framework-free recommendation engine into Spring. */
@Configuration
public class EngineConfig {

    @Bean
    public SubstitutionEngine substitutionEngine() { return new SubstitutionEngine(); }

    @Bean
    public StrategyRegistry strategyRegistry() { return new StrategyRegistry(); }

    @Bean
    public RecommendationEngine recommendationEngine(SubstitutionEngine subs, StrategyRegistry registry) {
        return new RecommendationEngine(new MatchCalculator(subs), registry);
    }
}
