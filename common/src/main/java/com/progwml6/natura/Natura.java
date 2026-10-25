package com.progwml6.natura;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.progwml6.natura.common.NaturaBlocks;
import com.progwml6.natura.common.NaturaItems;
import com.progwml6.natura.common.NaturaTabs;
import com.progwml6.natura.entities.NaturaEntities;
import com.progwml6.natura.tools.NaturaTools;
import com.progwml6.natura.world.gen.NaturaFeatures;

/** Natura: the common part, started by the Forge and the Fabric entry points. */
public final class Natura {
    public static final String MODID = "natura";
    public static final Logger LOG = LogUtils.getLogger();

    private Natura() {}

    /** Registers everything, in the 1.12 order (blocks, items, tools, entities, world generation, tabs). */
    public static void init() {
        NaturaBlocks.init();
        NaturaItems.init();
        NaturaTools.init();
        NaturaEntities.init();
        NaturaFeatures.init();
        NaturaTabs.init();
    }
}
