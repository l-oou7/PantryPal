package com.pantrypal.engine;

import com.pantrypal.domain.Dimension;

import java.time.LocalDate;
import java.util.*;

/**
 * Information Expert for "what do I have?". Groups a user's stock by normalised name and answers
 * availability questions in base units. Expired items are never counted as available.
 */
public class PantrySnapshot {

    private final Map<String, List<StockItem>> byName = new HashMap<>();
    private final LocalDate today;

    public PantrySnapshot(Collection<StockItem> items, LocalDate today) {
        this.today = today;
        for (StockItem item : items) {
            if (item.quantity() <= 0 || item.isExpired(today)) continue;
            byName.computeIfAbsent(item.key(), k -> new ArrayList<>()).add(item);
        }
        // earliest expiry first: FIFO consumption and waste-risk lookups rely on this order
        byName.values().forEach(list -> list.sort(Comparator.comparing(StockItem::expirationDate,
                Comparator.nullsLast(Comparator.naturalOrder()))));
    }

    public LocalDate today() { return today; }

    public boolean has(String name) {
        return byName.containsKey(IngredientNames.normalize(name));
    }

    public List<StockItem> items(String name) {
        return byName.getOrDefault(IngredientNames.normalize(name), List.of());
    }

    /** Sum of usable stock of this ingredient in the given dimension, in base units. */
    public double availableBase(String name, Dimension dimension) {
        double total = 0;
        for (StockItem s : items(name)) {
            if (s.unit().getDimension() == dimension) total += s.baseQuantity();
        }
        return total;
    }

    /** The ingredient is stocked, but only in units that can't be compared (e.g. garlic in pieces vs grams). */
    public boolean hasOnlyIncompatibleUnits(String name, Dimension dimension) {
        List<StockItem> list = items(name);
        return !list.isEmpty() && list.stream().noneMatch(s -> s.unit().getDimension() == dimension);
    }

    /** Highest waste-risk score among stock items of this ingredient (0 if not stocked). */
    public int maxWasteRisk(String name) {
        return items(name).stream().mapToInt(s -> WasteRiskCalculator.score(s, today)).max().orElse(0);
    }

    public long daysUntilEarliestExpiry(String name) {
        return items(name).stream()
                .mapToLong(s -> WasteRiskCalculator.daysLeft(s.expirationDate(), today))
                .min().orElse(Long.MAX_VALUE);
    }

    public Set<String> names() { return Collections.unmodifiableSet(byName.keySet()); }

    public boolean isEmpty() { return byName.isEmpty(); }
}
