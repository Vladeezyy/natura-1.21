package com.progwml6.natura.library;

import java.util.Locale;

/**
 * The 13 Natura woods (1.12 WoodTypes). 1.12 kept them as metadata of shared blocks (overworld_logs, overworld_planks,
 * ...); 1.21 gives every wood its own blocks, named &lt;wood&gt;_planks, &lt;wood&gt;_slab and so on.
 */
public enum NaturaWood {
    MAPLE(false),
    SILVERBELL(false),
    AMARANTH(false),
    TIGER(false),
    WILLOW(false),
    EUCALYPTUS(false),
    HOPSEED(false),
    SAKURA(false),
    REDWOOD(false),
    GHOSTWOOD(true),
    BLOODWOOD(true),
    FUSEWOOD(true),
    DARKWOOD(true);

    private final boolean nether;

    NaturaWood(boolean nether) {
        this.nether = nether;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.US);
    }

    public boolean isNether() {
        return this.nether;
    }
}
