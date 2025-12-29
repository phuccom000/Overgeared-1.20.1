package net.stirdrem.overgeared.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.HashMap;
import java.util.Map;

public class KnappingResourceReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    /* ---------- TEXTURES ---------- */
    private static final Map<Item, ResourceLocation> ITEM_TEXTURES = new HashMap<>();
    private static final Map<TagKey<Item>, ResourceLocation> TAG_TEXTURES = new HashMap<>();

    /* ---------- SOUNDS ---------- */
    private static final Map<Item, SoundEvent> ITEM_SOUNDS = new HashMap<>();
    private static final Map<TagKey<Item>, SoundEvent> TAG_SOUNDS = new HashMap<>();

    /* ---------- FALLBACKS ---------- */
    public static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.tryBuild("minecraft", "textures/block/stone.png");

    public static final SoundEvent FALLBACK_SOUND =
            SoundEvent.createVariableRangeEvent(
                    ResourceLocation.tryBuild("minecraft", "block.stone.break")
            );

    public KnappingResourceReloadListener() {
        super(GSON, "knapping_resources");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsons,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {

        ITEM_TEXTURES.clear();
        TAG_TEXTURES.clear();
        ITEM_SOUNDS.clear();
        TAG_SOUNDS.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            JsonObject root = GsonHelper.convertToJsonObject(entry.getValue(), "root");

            if (!root.has("knapping")) continue;

            JsonArray array = GsonHelper.getAsJsonArray(root, "knapping");

            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();

                /* ---------- TEXTURE ---------- */
                ResourceLocation texture = obj.has("texture")
                        ? ResourceLocation.tryParse(GsonHelper.getAsString(obj, "texture"))
                        : null;

                /* ---------- SOUND ---------- */
                SoundEvent sound = null;
                if (obj.has("sound")) {
                    ResourceLocation soundId =
                            ResourceLocation.tryParse(GsonHelper.getAsString(obj, "sound"));

                    sound = BuiltInRegistries.SOUND_EVENT.get(soundId);

                    if (sound == null) {
                        OvergearedMod.LOGGER.warn(
                                "Unknown sound '{}' in {}",
                                soundId, entry.getKey()
                        );
                        continue;
                    }
                }

                /* ---------- ITEM ---------- */
                if (obj.has("item")) {
                    ResourceLocation itemId =
                            ResourceLocation.tryParse(GsonHelper.getAsString(obj, "item"));

                    Item item = BuiltInRegistries.ITEM.get(itemId);

                    if (item == null) {
                        OvergearedMod.LOGGER.warn(
                                "Unknown item '{}' in {}",
                                itemId, entry.getKey()
                        );
                        continue;
                    }

                    if (texture != null) ITEM_TEXTURES.put(item, texture);
                    if (sound != null) ITEM_SOUNDS.put(item, sound);
                }

                /* ---------- TAG ---------- */
                if (obj.has("tag")) {
                    ResourceLocation tagId =
                            ResourceLocation.tryParse(GsonHelper.getAsString(obj, "tag"));

                    TagKey<Item> tag = ItemTags.create(tagId);

                    if (texture != null) TAG_TEXTURES.put(tag, texture);
                    if (sound != null) TAG_SOUNDS.put(tag, sound);
                }
            }
        }

        OvergearedMod.LOGGER.info(
                "Loaded {} item textures, {} tag textures, {} item sounds, {} tag sounds",
                ITEM_TEXTURES.size(),
                TAG_TEXTURES.size(),
                ITEM_SOUNDS.size(),
                TAG_SOUNDS.size()
        );
    }

    /* ============================================================ */
    /* ====================== RESOLUTION API ====================== */
    /* ============================================================ */

    public static ResourceLocation getTexture(ItemStack stack) {
        Item item = stack.getItem();

        ResourceLocation tex = ITEM_TEXTURES.get(item);
        if (tex != null) return tex;

        for (var entry : TAG_TEXTURES.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return FALLBACK_TEXTURE;
    }

    public static SoundEvent getSound(ItemStack stack) {
        Item item = stack.getItem();

        SoundEvent snd = ITEM_SOUNDS.get(item);
        if (snd != null) return snd;

        for (var entry : TAG_SOUNDS.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue();
            }
        }

        return FALLBACK_SOUND;
    }
}
