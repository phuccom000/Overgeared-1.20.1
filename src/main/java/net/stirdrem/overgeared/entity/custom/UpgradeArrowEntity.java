package net.stirdrem.overgeared.entity.custom;

import com.google.common.collect.Sets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UpgradeArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> DATA_TIER =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.INT);

    private final ItemStack referenceStack;
    private final Set<MobEffectInstance> effects = Sets.newHashSet();
    private final PotionContents potionContents;

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, ArrowTier tier, Level level, LivingEntity shooter, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, shooter, level, stack, firedFromWeapon);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        this.potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        int color = this.potionContents.equals(PotionContents.EMPTY) ? -1 : this.potionContents.getColor();
        this.entityData.set(DATA_POTION_COLOR, color);
    }

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
        this.potionContents = PotionContents.EMPTY;
    }

    public UpgradeArrowEntity(EntityType<? extends UpgradeArrowEntity> type, ArrowTier tier, Level level, double x, double y, double z, ItemStack stack, @Nullable ItemStack firedFromWeapon) {
        super(type, x, y, z, level, stack, firedFromWeapon);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        this.potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        int color = this.potionContents.equals(PotionContents.EMPTY) ? -1 : this.potionContents.getColor();
        this.entityData.set(DATA_POTION_COLOR, color);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
        builder.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        setBaseDamage(getBaseDamage() * getArrowTier().getDamageBonus());
        super.onHitEntity(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        Entity owner = this.getOwner();
        if (owner == null) {
            owner = this;
        }

        // Apply potion effects from potionContents
        for (MobEffectInstance effect : this.potionContents.getAllEffects()) {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(
                        owner,
                        owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target,
                        effect.getAmplifier(),
                        1.0D
                );
            } else {
                MobEffectInstance reduced = new MobEffectInstance(
                        effect.getEffect(),
                        Math.max(effect.getDuration() / 8, 1),
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                );
                target.addEffect(reduced, owner);
            }
        }

        // Apply custom effects
        for (MobEffectInstance effect : this.effects) {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(
                        owner,
                        owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target,
                        effect.getAmplifier(),
                        1.0D
                );
            } else {
                target.addEffect(effect, owner);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            PotionContents potionContents = this.referenceStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (!potionContents.getAllEffects().iterator().hasNext()) {
                return;
            }

            // Only create lingering cloud if this is actually a lingering arrow
            boolean isLingering = this.referenceStack.getOrDefault(ModComponents.LINGERING_STATUS, false);
            if (isLingering) {
                makeAreaOfEffectCloud(this.referenceStack, potionContents, result);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            PotionContents potionContents = this.referenceStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

            if (!potionContents.getAllEffects().iterator().hasNext()) {
                return;
            }

            // Only create lingering cloud if this is actually a lingering arrow
            boolean isLingering = this.referenceStack.getOrDefault(ModComponents.LINGERING_STATUS, false);
            if (isLingering) {
                makeAreaOfEffectCloud(this.referenceStack, potionContents, result);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return switch (getArrowTier()) {
            case FLINT -> new ItemStack(Items.ARROW);
            case IRON -> new ItemStack(ModItems.IRON_UPGRADE_ARROW.get());
            case STEEL -> new ItemStack(ModItems.STEEL_UPGRADE_ARROW.get());
            case DIAMOND -> new ItemStack(ModItems.DIAMOND_UPGRADE_ARROW.get());
        };
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return referenceStack;
    }

    private void multiplyDamage(double factor) {
        setBaseDamage(getBaseDamage() * factor);
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.entityData.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Tier", this.entityData.get(DATA_TIER));
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));

        if (!this.effects.isEmpty()) {
            ListTag listtag = new ListTag();
            for (MobEffectInstance mobeffectinstance : this.effects) {
                listtag.add(mobeffectinstance.save());
            }
            tag.put("CustomPotionEffects", listtag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        if (tag.contains("Tier", 99)) {
            this.entityData.set(DATA_TIER, tag.getByte("Tier"));
        }
        if (tag.contains("PotionColor", 99)) {
            this.entityData.set(DATA_POTION_COLOR, tag.getInt("PotionColor"));
        }

        this.effects.clear();
        if (tag.contains("CustomPotionEffects", 9)) {
            ListTag listtag = tag.getList("CustomPotionEffects", 10);
            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    this.addEffect(mobeffectinstance);
                }
            }
        }
    }

    public void addEffect(MobEffectInstance pEffectInstance) {
        this.effects.add(pEffectInstance);

        // Combine potion effects with custom effects for color calculation
        java.util.ArrayList<MobEffectInstance> allEffects = new java.util.ArrayList<>();
        for (MobEffectInstance effect : this.potionContents.getAllEffects()) {
            allEffects.add(effect);
        }
        allEffects.addAll(this.effects);

        // Create new PotionContents with all effects for color calculation
        PotionContents combined = new PotionContents(
                this.potionContents.potion(),
                this.potionContents.customColor(),
                allEffects
        );

        this.getEntityData().set(DATA_POTION_COLOR, combined.getColor());
    }

    private void makeAreaOfEffectCloud(ItemStack stack, PotionContents potionContents, HitResult result) {
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

        // Create reduced-duration potion contents for the cloud
        List<MobEffectInstance> reducedEffects = new java.util.ArrayList<>();
        for (MobEffectInstance inst : potionContents.getAllEffects()) {
            MobEffectInstance reducedEffect = new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1), // 1/8 duration
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            );
            reducedEffects.add(reducedEffect);
        }

        // Create new PotionContents with reduced effects
        PotionContents cloudContents = new PotionContents(
                potionContents.potion(),
                potionContents.customColor(),
                reducedEffects
        );

        cloud.setPotionContents(cloudContents);

        level().addFreshEntity(cloud);
    }

    private void makeParticle(int amount) {
        int color = this.entityData.get(DATA_POTION_COLOR);
        if (color != -1 && amount > 0) {
            for (int j = 0; j < amount; ++j) {
                this.level().addParticle(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color),
                        this.getRandomX(0.5D),
                        this.getRandomY(),
                        this.getRandomZ(0.5D),
                        0.0D, 0.0D, 0.0D
                );
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        }
    }
}