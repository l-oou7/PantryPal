package com.pantrypal.engine;

/** Availability of one recipe ingredient against the pantry (named after the design document's enum). */
public enum IngredientStatus {
    /** Enough in the pantry for the requested servings. */
    HAVE_ENOUGH,
    /** In the pantry, but less than needed. */
    NEED_MORE,
    /** Not in the pantry. */
    MISSING,
    /** Not in the pantry, but a suitable substitute is. */
    CAN_SUBSTITUTE,
    /** Optional ingredient that is not in the pantry. Does not affect the match. */
    OPTIONAL
}
