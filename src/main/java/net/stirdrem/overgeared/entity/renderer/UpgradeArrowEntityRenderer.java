package net.stirdrem.overgeared.entity.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.custom.UpgradeArrowEntity;

public class UpgradeArrowEntityRenderer extends ArrowRenderer<UpgradeArrowEntity> {
    public UpgradeArrowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation getTextureLocation(UpgradeArrowEntity entity) {
        ArrowTier tier = entity.getArrowTier();
        String tierName = tier.getSerializedName(); // e.g., "golden", "iron", etc.
        return ResourceLocation.tryBuild(OvergearedMod.MOD_ID, "textures/entity/projectiles/arrows/" + tierName + ".png");
    }
}
