package net.stirdrem.overgeared.compat.polymorph;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.fml.ModList;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.recipe.ForgingRecipe;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * SAFE: Entry point for Polymorph compatibility.
 * Uses reflection to call the unsafe {@link PolymorphIntegration} class.
 * This class is safe to load even without Polymorph installed.
 */
public class Polymorph {
    /** Polymorph mod ID constant */
    public static final String MOD_ID = "polymorph";
    
    /** Whether Polymorph is loaded */
    public static final boolean LOADED = ModList.get().isLoaded(MOD_ID);
    
    // All methods are in PolymorphIntegration - we only need to reflect into one class
    private static final Method INIT;
    private static final Method INIT_CLIENT;
    private static final Method GET_SELECTED_RECIPE;
    private static final Method GET_SELECTED_OUTPUT;
    
    static {
        Method initMethod = null;
        Method initClientMethod = null;
        Method getSelectedRecipeMethod = null;
        Method getSelectedOutputMethod = null;
        
        if (LOADED) {
            try {
                Class<?> integration = Class.forName("net.stirdrem.overgeared.compat.polymorph.PolymorphIntegration");
                initMethod = integration.getMethod("init");
                initClientMethod = integration.getMethod("initClient");
                getSelectedRecipeMethod = integration.getMethod("getSelectedRecipe", 
                    AbstractSmithingAnvilBlockEntity.class, RecipeInput.class);
                getSelectedOutputMethod = integration.getMethod("getSelectedOutput");
            } catch (Exception e) {
                OvergearedMod.LOGGER.debug("Polymorph integration unavailable: {}", e.getMessage());
            }
        }
        
        INIT = initMethod;
        INIT_CLIENT = initClientMethod;
        GET_SELECTED_RECIPE = getSelectedRecipeMethod;
        GET_SELECTED_OUTPUT = getSelectedOutputMethod;
    }
    
    /**
     * Initialize Polymorph integration (server-side).
     */
    public static void init() {
        if (INIT == null) return;
        try {
            INIT.invoke(null);
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Failed to initialize Polymorph integration", e);
        }
    }
    
    /**
     * Initialize Polymorph client integration.
     */
    public static void initClient() {
        if (INIT_CLIENT == null) return;
        try {
            INIT_CLIENT.invoke(null);
        } catch (Exception e) {
            OvergearedMod.LOGGER.error("Failed to initialize Polymorph client integration", e);
        }
    }
    
    /**
     * Gets the selected recipe from Polymorph if available.
     */
    @SuppressWarnings("unchecked")
    public static Optional<RecipeHolder<ForgingRecipe>> getSelectedRecipe(
        AbstractSmithingAnvilBlockEntity blockEntity, RecipeInput input
    ) {
        if (GET_SELECTED_RECIPE == null) return Optional.empty();
        try {
            return (Optional<RecipeHolder<ForgingRecipe>>) GET_SELECTED_RECIPE.invoke(null, blockEntity, input);
        } catch (Exception e) {
            OvergearedMod.LOGGER.debug("Failed to get Polymorph recipe", e);
            return Optional.empty();
        }
    }
    
    /**
     * Gets the selected output from Polymorph's client widget.
     */
    @SuppressWarnings("unchecked")
    public static Optional<ItemStack> getSelectedOutput() {
        if (GET_SELECTED_OUTPUT == null) return Optional.empty();
        try {
            return (Optional<ItemStack>) GET_SELECTED_OUTPUT.invoke(null);
        } catch (Exception e) {
            OvergearedMod.LOGGER.debug("Failed to get Polymorph client output", e);
            return Optional.empty();
        }
    }
}
