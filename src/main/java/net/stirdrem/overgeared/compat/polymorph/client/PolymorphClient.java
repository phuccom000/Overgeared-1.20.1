package net.stirdrem.overgeared.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import net.stirdrem.overgeared.OvergearedMod;

/**
 * Client-side Polymorph integration for registering the widget factory.
 */
public class PolymorphClient {
    
    public static void init() {
        OvergearedMod.LOGGER.info("Registering Polymorph client widgets for smithing anvils.");
        
        // Register widget factory for smithing anvil screens
        PolymorphWidgets.getInstance().registerWidget(SmithingAnvilRecipesWidget::create);
    }
}
