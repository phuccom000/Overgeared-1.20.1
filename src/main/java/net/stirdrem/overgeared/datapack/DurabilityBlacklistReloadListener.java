package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DurabilityBlacklistReloadListener extends SimpleJsonResourceReloadListener {

    private static final Map<ResourceLocation, Ingredient> DATA = new ConcurrentHashMap<>();
    public static final DurabilityBlacklistReloadListener INSTANCE = new DurabilityBlacklistReloadListener();
    private static final Gson GSON = new Gson();

    public DurabilityBlacklistReloadListener() {
        super(GSON, "durability_blacklist");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    Ingredient ingredient = parseIngredient(json);
                    DATA.put(id, ingredient);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for durability multiplier blacklist entry: " + id);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse durability multiplier blacklist entry: " + id + ", error: " + e.getMessage());
            }
        }

        System.out.println("Loaded " + DATA.size() + " durability blacklist entries");

        // Debug: print all loaded ingredients
        for (Map.Entry<ResourceLocation, Ingredient> entry : DATA.entrySet()) {
            System.out.println("Blacklist entry: " + entry.getKey() + " -> " + Arrays.toString(entry.getValue().getItems()));
        }
    }

    private Ingredient parseIngredient(JsonObject json) {
        if (!json.has("item")) {
            throw new JsonSyntaxException("Missing 'item' for durability multiplier blacklist entry");
        }

        JsonElement itemElement = json.get("item");
        return Ingredient.fromJson(itemElement);
    }

    public static Map<ResourceLocation, Ingredient> getData() {
        return Collections.unmodifiableMap(DATA);
    }

    public static List<Ingredient> getAllIngredients() {
        return List.copyOf(DATA.values());
    }

    public static List<ItemStack> getAllBlacklistedItems() {
        List<ItemStack> allItems = new ArrayList<>();
        for (Ingredient ingredient : DATA.values()) {
            ItemStack[] stacks = ingredient.getItems();
            if (stacks.length > 0) {
                allItems.addAll(Arrays.asList(stacks));
            }
        }
        return allItems;
    }

    public static boolean isBlacklisted(ItemStack stack) {
        for (Ingredient ingredient : DATA.values()) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        DATA.clear();
    }
}