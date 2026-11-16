package com.pantrypal.service;

/**
 * Observer pattern: anything that wants to react to expiring food implements this.
 * The scanner does not need to know how alerts are delivered (Low Coupling).
 */
public interface ExpiryObserver {

    /** @return true if the observer created something new (e.g. a notification) for this event */
    boolean onExpiryDetected(ExpiryEvent event);
}
