package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GrindingBlacklistReloadListener extends SimpleJsonResourceReloadListener {

    private static final Map<ResourceLocation, Ingredient> DATA = new ConcurrentHashMap<>();
    public static final GrindingBlacklistReloadListener INSTANCE = new GrindingBlacklistReloadListener();
    private static final Gson GSON = new Gson();

    public GrindingBlacklistReloadListener() {
        super(GSON, "grinding_blacklist");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        DATA.clear();
        System.out.println("Found " + resources.size() + " grinding blacklist resources:");
        for (ResourceLocation id : resources.keySet()) {
            System.out.println(" - " + id);
        }
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement jsonElement = entry.getValue();

            try {
                if (jsonElement.isJsonObject()) {
                    JsonObject json = jsonElement.getAsJsonObject();
                    Ingredient ingredient = parseIngredient(json);
                    DATA.put(id, ingredient);
                } else {
                    throw new JsonSyntaxException("Expected JSON object for grinding blacklist entry: " + id);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse grinding blacklist entry: " + id + ", error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Loaded " + DATA.size() + " grinding blacklist entries");

        // Debug: print all loaded ingredients
        for (Map.Entry<ResourceLocation, Ingredient> entry : DATA.entrySet()) {
            System.out.println("Blacklist entry: " + entry.getKey() + " -> " + Arrays.toString(entry.getValue().getItems()));
        }
    }

    private Ingredient parseIngredient(JsonObject json) {
        if (!json.has("item")) {
            throw new JsonSyntaxException("Missing 'item' for grinding blacklist entry");
        }

        JsonElement element = json.get("item");

        return Ingredient.CODEC
                .parse(JsonOps.INSTANCE, element)
                .getOrThrow(JsonSyntaxException::new);
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
                Collections.addAll(allItems, stacks);
            }
        }
        return allItems;
    }

    public static boolean isBlacklisted(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

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