package com.herocraftonline.dev.heroes.util;

/**
 * Stores node identifiers for settings
 * 
 * 
 */
public enum Setting {

    AMOUNT("amount"),
    APPLY_TEXT("apply-text"),
    CHANCE_LEVEL("chance-per-level"),
    COOLDOWN("cooldown"),
    DAMAGE("damage"),
    DELAY("delay"),
    DURATION("duration"),
    EXP("exp"),
    EXPIRE_TEXT("expire-text"),
    HEALTH("health"),
    HEALTH_COST("health-cost"),
    HEALTH_COST_REDUCE("health-cost-reduce"),
    LEVEL("level"),
    MANA("mana"),
    MANA_REDUCE("mana-reduce"),
    MAX_DISTANCE("max-distance"),
    PERIOD("period"),
    RADIUS("radius"),
    REAGENT("reagent"),
    REAGENT_COST("reagent-cost"), 
    STAMINA("stamina"),
    STAMINA_REDUCE("stamina-reduce"),
    UNAPPLY_TEXT("unapply-text"),
    USE_TEXT("use-text"),
    DEATH_TEXT("death-text");

    private final String node;

    Setting(String node) {
        this.node = node;
    }

    public String node() {
        return this.node;
    }
    
    @Override
    public String toString() {
        return this.node;
    }
}
