package net.stirdrem.overgeared.entity.custom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.entity.ArrowTier;
import net.stirdrem.overgeared.entity.ModEntities;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.item.custom.UpgradeArrowItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UpgradeArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> DATA_TIER =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_POTION_COLOR =
            SynchedEntityData.defineId(UpgradeArrowEntity.class, EntityDataSerializers.INT);

    private final ItemStack referenceStack;
    private final Set<MobEffectInstance> effects = Sets.newHashSet();
    private Potion potion = Potions.EMPTY;

    public UpgradeArrowEntity(ArrowTier tier, Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW.get(), shooter, level);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        // Server-side only: extract potion color
        CompoundTag tag = stack.getTag();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion")))
            color = OvergearedMod.ClientModEvents.getColor(stack);
        this.entityData.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    public UpgradeArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
    }

    public UpgradeArrowEntity(ArrowTier tier, Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.UPGRADE_ARROW.get(), x, y, z, level);
        this.referenceStack = stack;
        this.entityData.set(DATA_TIER, (byte) tier.ordinal());

        CompoundTag tag = stack.getTag();
        int color = -1;
        if (tag != null && (tag.contains("Potion") || tag.contains("CustomPotionEffects") || tag.contains("LingeringPotion"))) {
            color = OvergearedMod.ClientModEvents.getColor(stack);
        }
        this.entityData.set(DATA_POTION_COLOR, color);
        this.potion = getPotion(tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TIER, (byte) ArrowTier.FLINT.ordinal());
        this.entityData.define(DATA_POTION_COLOR, -1); // Default no color
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        setBaseDamage(getBaseDamage() * getArrowTier().getDamageBonus());
        super.onHitEntity(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        Entity owner = this.getOwner(); // More reliable than getEffectSource()
        if (owner == null) {
            owner = this; // Fallback to the arrow itself
        }
        for (MobEffectInstance effect : this.potion.getEffects()) {
            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
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

        for (MobEffectInstance effect : this.effects) {
            if (effect.getEffect().isInstantenous()) {
                effect.getEffect().applyInstantenousEffect(owner, owner instanceof LivingEntity livingOwner ? livingOwner : null,
                        target, effect.getAmplifier(), 1.0D);
            } else {
                target.addEffect(effect, owner);
            }
        }
    }


    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide) {
            CompoundTag tag = this.referenceStack.getTag();

            // Only lingering type creates cloud
            if (tag != null && (tag.contains("LingeringPotion") || getArrowTier() == ArrowTier.FLINT && tag.contains("Potion", 8))) {
                Potion potion = getPotion(tag);
                List<MobEffectInstance> effects = getAllEffects(tag);
                if (!effects.isEmpty()) {
                    makeAreaOfEffectCloud(this.referenceStack, potion, result);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
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

    private void multiplyDamage(double factor) {
        setBaseDamage(getBaseDamage() * factor);
    }

    public ArrowTier getArrowTier() {
        int ordinal = this.entityData.get(DATA_TIER);
        return ArrowTier.values()[ordinal % ArrowTier.values().length]; // safety check
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Tier", this.entityData.get(DATA_TIER));
        tag.putInt("PotionColor", this.entityData.get(DATA_POTION_COLOR));
        if (!this.effects.isEmpty()) {
            ListTag listtag = new ListTag();

            for (MobEffectInstance mobeffectinstance : this.effects) {
                listtag.add(mobeffectinstance.save(new CompoundTag()));
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
        for (MobEffectInstance mobeffectinstance : PotionUtils.getCustomEffects(tag)) {
            this.addEffect(mobeffectinstance);
        }

    }

    public void addEffect(MobEffectInstance pEffectInstance) {
        this.effects.add(pEffectInstance);
        CompoundTag tag = this.referenceStack.getTag();
        Potion potion = getPotion(tag);
        this.getEntityData().set(DATA_POTION_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(potion, this.effects)));
    }

    private void makeAreaOfEffectCloud(ItemStack stack, Potion potion, HitResult result) {
        Vec3 hit = result.getLocation();
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length();
        double offset = verticalRatio > 0 ? -verticalRatio * 0.5 : -0.2;

        double cloudY = hit.y + offset + 0.25;
        AreaEffectCloud cloud = new AreaEffectCloud(level(), hit.x, cloudY, hit.z);

        Entity owner = getOwner();
        if (owner instanceof LivingEntity le) {
            cloud.setOwner(le);
        }

        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        //cloud.setPotion(potion);

        for (MobEffectInstance inst : potion.getEffects()) {
            MobEffectInstance reducedEffect = new MobEffectInstance(
                    inst.getEffect(),
                    Math.max(inst.getDuration() / 8, 1),
                    inst.getAmplifier(),
                    inst.isAmbient(),
                    inst.isVisible(),
                    inst.showIcon()
            );
            cloud.addEffect(reducedEffect);
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("CustomPotionColor", 99)) {
            cloud.setFixedColor(tag.getInt("CustomPotionColor"));
        }

        level().addFreshEntity(cloud);
    }

    public static List<MobEffectInstance> getMobEffects(ItemStack pStack) {
        return getAllEffects(pStack.getTag());
    }

    public static Potion getPotion(@Nullable CompoundTag tag) {
        if (tag == null) return Potions.EMPTY;

        // Prioritize "LingeringPotion" if present
        if (tag.contains("LingeringPotion", 8)) { // 8 = string type
            return Potion.byName(tag.getString("LingeringPotion"));
        }
        if (tag.contains("LingeringPotion") && tag.getBoolean("LingeringPotion")) { // 8 = string type
            return Potion.byName(tag.getString("Potion"));
        }
        if (tag.contains("Potion", 8)) {
            return Potion.byName(tag.getString("Potion"));
        }

        return Potions.EMPTY;
    }

    public static List<MobEffectInstance> getAllEffects(@Nullable CompoundTag pCompoundTag) {
        List<MobEffectInstance> list = Lists.newArrayList();
        list.addAll(getPotion(pCompoundTag).getEffects());
        getCustomEffects(pCompoundTag, list);
        return list;
    }

    public static void getCustomEffects(@Nullable CompoundTag pCompoundTag, List<MobEffectInstance> pEffectList) {
        if (pCompoundTag != null && pCompoundTag.contains("CustomPotionEffects", 9)) {
            ListTag listtag = pCompoundTag.getList("CustomPotionEffects", 10);

            for (int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                MobEffectInstance mobeffectinstance = MobEffectInstance.load(compoundtag);
                if (mobeffectinstance != null) {
                    pEffectList.add(mobeffectinstance);
                }
            }
        }

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
