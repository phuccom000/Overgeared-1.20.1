package net.stirdrem.overgeared.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potion;

import java.util.List;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;

public class LingeringArrowEntity extends Arrow {
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(LingeringArrowEntity.class, EntityDataSerializers.INT);
    private final ItemStack referenceStack;

    public LingeringArrowEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(level, shooter);
        this.referenceStack = stack;
        int color = PotionUtils.getColor(stack);
        this.entityData.set(DATA_POTION_COLOR, color);
    }

    public LingeringArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PotionColor", 99)) {
            this.entityData.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            ItemStack stack = this.referenceStack;
            Potion potion = PotionUtils.getPotion(stack);
            List<MobEffectInstance> effects = PotionUtils.getAllEffects(stack.getTag());

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, potion, result);
            }

            int eventId = potion.hasInstantEffects() ? 2007 : 2002;
            //level().levelEvent(eventId, this.blockPosition(), PotionUtils.getColor(stack));
            //this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if (!level().isClientSide) {
            ItemStack stack = this.referenceStack;
            Potion potion = PotionUtils.getPotion(stack);
            List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);

            if (!effects.isEmpty()) {
                makeAreaOfEffectCloud(stack, potion, pResult);
            }

            int eventId = potion.hasInstantEffects() ? 2007 : 2002;
            //level().levelEvent(eventId, this.blockPosition(), PotionUtils.getColor(stack));
            //this.discard();
        }
    }

    private void makeAreaOfEffectCloud(ItemStack stack, Potion potion, HitResult result) {
        Vec3 hit = result.getLocation();

        // Compute vertical motion ratio
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length(); // -1 to 1

        // Map verticalRatio to offset: more vertical ➜ larger downward offset
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        double cloudY = hit.y + offset + 0.25;
        double cloudX = hit.x;
        double cloudZ = hit.z;

        AreaEffectCloud cloud = new AreaEffectCloud(level(), cloudX, cloudY, cloudZ);
        Entity owner = getOwner();
        if (owner instanceof LivingEntity le) {
            cloud.setOwner(le);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.setPotion(potion);

        for (MobEffectInstance inst : PotionUtils.getCustomEffects(stack)) {
            MobEffectInstance reducedEffect = new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1), // 1/4 duration
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            );
            cloud.addEffect(reducedEffect);
        }

        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
            cloud.setFixedColor(compoundtag.getInt("CustomPotionColor"));
        }

        level().addFreshEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = this.entityData.get(DATA_POTION_COLOR);
        if (color != -1 && amount > 0) {
            double r = (double) (color >> 16 & 255) / 255.0D;
            double g = (double) (color >> 8 & 255) / 255.0D;
            double b = (double) (color & 255) / 255.0D;

            for (int j = 0; j < amount; ++j) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), r, g, b);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        //if (this.level().isClientSide()) {
        if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
                this.makeParticle(1);
            }
        } else {
            this.makeParticle(2);
        }

        //}
    }

}
