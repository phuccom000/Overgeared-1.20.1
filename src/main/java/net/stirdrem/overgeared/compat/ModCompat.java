package net.stirdrem.overgeared.compat;

import net.stirdrem.overgeared.compat.polymorph.Polymorph;

/**
 * Central mod compatibility handler.
 * Delegates to safe wrapper classes for optional mod integrations.
 */
public class ModCompat {
    
    public static void init() {
        Polymorph.init();
    }
    
    public static void initClient() {
        Polymorph.initClient();
    }
}
