package com.pantrypal.service;

import com.pantrypal.domain.AlertLevel;
import com.pantrypal.engine.WasteRiskCalculator;
import com.pantrypal.model.Ingredient;
import com.pantrypal.repository.IngredientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Automated expiry monitoring. A scheduled backend task scans every user's pantry, classifies each item
 * (expired / expires today / expiring soon) using that user's own threshold, and notifies the observers.
 */
@Service
public class ExpiryMonitorService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryMonitorService.class);
    /** Largest threshold a user can choose; items further out are never "expiring soon". */
    private static final int MAX_THRESHOLD_DAYS = 14;

    private final IngredientRepository ingredients;
    private final List<ExpiryObserver> observers;
    private final CurrentTime clock;

    public ExpiryMonitorService(IngredientRepository ingredients, List<ExpiryObserver> observers, CurrentTime clock) {
        this.ingredients = ingredients;
        this.observers = observers;
        this.clock = clock;
    }

    /** Runs on the cron in application.properties (hourly by default). */
    @Scheduled(cron = "${pantrypal.expiry.cron:0 0 * * * *}")
    @Transactional
    public void scheduledScan() {
        int created = scan(ingredients.findExpiringOnOrBefore(clock.today().plusDays(MAX_THRESHOLD_DAYS)));
        log.info("Expiry scan finished: {} new alert(s)", created);
    }

    /** Also scan once when the app starts, so alerts are fresh after a restart. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void scanOnStartup() {
        scheduledScan();
    }

    /** Scans one user's pantry immediately (e.g. after adding an item or pressing "Scan now"). */
    @Transactional
    public int scanUser(Long userId) {
        return scan(ingredients.findByUserIdOrderByExpirationDateAsc(userId));
    }

    private int scan(List<Ingredient> items) {
        LocalDate today = clock.today();
        int created = 0;
        for (Ingredient i : items) {
            int threshold = i.getUser().getExpiryThresholdDays();
            AlertLevel level = WasteRiskCalculator.level(i.getExpirationDate(), today, threshold);
            if (level == AlertLevel.FRESH) continue;
            ExpiryEvent e = new ExpiryEvent(i, level, WasteRiskCalculator.daysLeft(i.getExpirationDate(), today),
                    WasteRiskCalculator.score(EngineMapper.toStock(i), today), today);
            for (ExpiryObserver o : observers) {
                if (o.onExpiryDetected(e)) created++;
            }
        }
        return created;
    }
}
