package net.stirdrem.overgeared.compat.polymorph.client;

import com.illusivesoulworks.polymorph.api.client.widgets.children.OutputWidget;
import com.illusivesoulworks.polymorph.client.RecipesWidget;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Helper class to get Polymorph's selected recipe output on the client side.
 * Used to update the ghost result preview when switching recipes.
 */
public class PolymorphClientHelper {
    
    /** Cached reflection field for OutputWidget.highlighted - avoids repeated lookups */
    private static final Field HIGHLIGHTED_FIELD;
    
    static {
        Field field = null;
        try {
            field = OutputWidget.class.getDeclaredField("highlighted");
            field.setAccessible(true);
        } catch (Exception e) {
            OvergearedMod.LOGGER.debug("Failed to cache Polymorph highlighted field: {}", e.getMessage());
        }
        HIGHLIGHTED_FIELD = field;
    }
    
    /**
     * Gets the currently selected recipe output from Polymorph's client widget.
     * Returns empty if Polymorph widget is not active or no recipe is selected.
     */
    public static Optional<ItemStack> getSelectedOutput() {
        if (HIGHLIGHTED_FIELD == null) return Optional.empty();
        
        return RecipesWidget.get().flatMap(widget -> {
            try {
                var outputWidgets = widget.getSelectionWidget().getOutputWidgets();
                if (outputWidgets == null || outputWidgets.isEmpty()) {
                    return Optional.empty();
                }
                
                for (var outputWidget : outputWidgets) {
                    if (HIGHLIGHTED_FIELD.getBoolean(outputWidget)) {
                        return Optional.of(outputWidget.getOutput());
                    }
                }
            } catch (Exception e) {
                OvergearedMod.LOGGER.debug("Failed to get Polymorph selected output: {}", e.getMessage());
            }
            return Optional.empty();
        });
    }
}

