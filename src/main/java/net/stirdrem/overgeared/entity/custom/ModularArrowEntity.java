/*
package net.stirdrem.overgeared.entity.custom;

import com.morearrows.lists.ArrowItems;
import com.morearrows.lists.backend.configurations.ArrowServerConfig;
import com.morearrows.specialarrowentities.slime.FlintSlimeArrowEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Endermite;


import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.stirdrem.overgeared.config.ServerConfig;
import net.stirdrem.overgeared.entity.ModEntities;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModularArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> TIMES_BOUNCED;
    private final ItemStack referenceStack;
    private int ignoreGravityTicks = 0;
    private int bounceCount = 0;
    private static boolean isSlimeArrow = false;

    static {
        TIMES_BOUNCED = SynchedEntityData.defineId(ModularArrowEntity.class, EntityDataSerializers.INT);
    }

    public ModularArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level);
        this.referenceStack = ItemStack.EMPTY;
    }

    public ModularArrowEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.MODULAR_ARROW.get(), shooter, level);
        this.referenceStack = stack.copy();
        this.setOwner(shooter);
        //this.setEffectsFromItem(stack);
        CompoundTag tag = this.referenceStack.getTag();
        if (tag != null && "slime_ball".equals(tag.getString("Feather"))) {
            isSlimeArrow = true;
        }
    }

    @Override

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            applyModularEffects(result);
            applyPotionEffectsCloudIfNeeded(result);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    private void applyModularEffects(HitResult res) {
        CompoundTag tag = referenceStack.getTag();
        if (tag == null) return;

        String tip = tag.getString("Tip");
        switch (tip) {
            case "ender_pearl" -> teleportShooter();
            case "chorus_fruit" -> teleportTarget(res);
            case "amethyst_shard" -> pierceEntities();
            case "fire_charge" -> smallExplosion(res);
            case "glow_ink" -> glowTarget(res);
            case "iron_nugget" -> multiplyDamage(1.2);
            case "steel_nugget" -> multiplyDamage(1.5);
            case "diamond_shard" -> multiplyDamage(1.7);
        }
        String shaft = tag.getString("Shaft");
        switch (shaft) {
            case "blaze_rod" -> setFireToTarget(res);
            case "breeze_rod" -> strongKnockback(res);
            case "end_rod" -> ignoreGravity();
            case "bamboo" -> highRangeLowDamageBreak();
        }
        String feather = tag.getString("Feather");
        switch (feather) {
            case "prismarine" -> waterUnaffected();
            //case "slime_ball" -> bounce(res);
            // Removed phantom_membrane case
        }
    }

    private void applyPotionEffectsCloudIfNeeded(HitResult result) {
        ItemStack stack = referenceStack;
        Potion potion = PotionUtils.getPotion(stack);
        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (!effects.isEmpty()) {
            makeAreaOfEffectCloud(stack, potion, result);
        }
    }

    private void makeAreaOfEffectCloud(ItemStack stack, Potion potion, HitResult result) {
        Vec3 hit = result.getLocation();
        Vec3 motion = this.getDeltaMovement();
        double verticalRatio = motion.y / motion.length();
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
            cloud.addEffect(new MobEffectInstance(inst));
        }

        CompoundTag compoundtag = stack.getTag();
        if (compoundtag != null && compoundtag.contains("CustomPotionColor", 99)) {
            cloud.setFixedColor(compoundtag.getInt("CustomPotionColor"));
        }

        level().addFreshEntity(cloud);
    }

    private void teleportShooter() {
        if (this.getOwner() instanceof ServerPlayer shooter && !this.level().isClientSide) {
            shooter.teleportTo(this.getX(), this.getY(), this.getZ());
            if (this.random.nextFloat() < 0.05f) {
                Endermite mite = EntityType.ENDERMITE.create(this.level());
                if (mite != null) {
                    mite.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
                    this.level().addFreshEntity(mite);
                }
            }
            this.level().playSound(null, shooter.blockPosition(), SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
            shooter.hurt(shooter.damageSources().magic(), 5.0f);
            this.discard();
        }
    }

    private void teleportTarget(HitResult res) {
        if (res instanceof EntityHitResult er && er.getEntity() instanceof LivingEntity target) {
            double dx = target.getX() + (this.random.nextDouble() - 0.5) * 16;
            double dy = target.getY() + (this.random.nextDouble() - 0.5) * 8;
            double dz = target.getZ() + (this.random.nextDouble() - 0.5) * 16;

            if (target instanceof ServerPlayer player) {
                player.teleportTo(dx, dy, dz);
            } else {
                target.teleportTo(dx, dy, dz);
            }

            this.level().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void smallExplosion(HitResult res) {
        Vec3 loc = res.getLocation();
        this.level().explode(this, loc.x, loc.y, loc.z, 1f, Level.ExplosionInteraction.NONE);
        this.discard();
    }

    private void glowTarget(HitResult res) {
        if (res instanceof EntityHitResult er && er.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
        }
    }

    private void multiplyDamage(double factor) {
        setBaseDamage(getBaseDamage() * factor);
    }

    private void pierceEntities() {
        this.setPierceLevel((byte) (getPierceLevel() + 2));
    }

    private void setFireToTarget(HitResult res) {
        if (res instanceof EntityHitResult er && er.getEntity() instanceof LivingEntity target) {
            target.setSecondsOnFire(5);
        }
    }

    private void strongKnockback(HitResult res) {
        if (res instanceof EntityHitResult er && er.getEntity() instanceof LivingEntity target) {
            Vec3 knockback = this.getDeltaMovement().normalize().scale(2.0);
            target.push(knockback.x, knockback.y, knockback.z);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TIMES_BOUNCED, 0);
    }

    @Override

    public void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("xtraarrows_bounces", (Integer) this.entityData.get(TIMES_BOUNCED));
    }

    @Override

    public void readAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(TIMES_BOUNCED, compoundTag.getInt("xtraarrows_bounces"));
    }

    private void ignoreGravity() {
        ignoreGravityTicks = 200; // ~10 seconds
    }

    private void highRangeLowDamageBreak() {
        multiplyDamage(0.7);
        this.setKnockback(this.getKnockback() + 1);
        this.discard();
    }

    private void waterUnaffected() {
        this.getPersistentData().putBoolean("WaterUnaffected", true);
    }


    private void bouncingArrow() {
        this.setKnockback(this.getKnockback() + 1);
        this.pickup = Pickup.ALLOWED;
        // Reset counter when shot
        this.bounceCount = 0;
    }

    public void bounce(HitResult raytraceResultIn) {
        if (raytraceResultIn.getType() != HitResult.Type.MISS) {
            this.setSoundEvent(SoundEvents.SLIME_BLOCK_HIT);
            if (raytraceResultIn.getType() == HitResult.Type.ENTITY) {
                this.onHitEntity((EntityHitResult) raytraceResultIn);
            } else if (raytraceResultIn.getType() == HitResult.Type.BLOCK) {
                Vec3 motion = this.getDeltaMovement();
                if (this.isInWater() || this.getPersistentData().getDouble("bounces") <= (double) 0.0F) {
                    super.onHit(raytraceResultIn);
                    return;
                }

                BlockHitResult blockraytraceresult = (BlockHitResult) raytraceResultIn;
                switch (blockraytraceresult.getDirection()) {
                    case DOWN:
                    case UP:
                        this.setDeltaMovement(motion.x, motion.y * (double) -1.0F, motion.z);
                        break;
                    case NORTH:
                    case SOUTH:
                        this.setDeltaMovement(motion.x, motion.y, motion.z * (double) -1.0F);
                        break;
                    case WEST:
                    case EAST:
                        this.setDeltaMovement(motion.x * (double) -1.0F, motion.y, motion.z);
                }

                float f = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
                this.setYRot((float) (Mth.atan2(motion.x, motion.z) * (double) (180F / (float) Math.PI)));
                this.setXRot((float) (Mth.atan2(motion.y, (double) f) * (double) (180F / (float) Math.PI)));
                this.yRotO = this.getYRot();
                this.xRotO = this.getYRot();
                this.getPersistentData().putDouble("bounces", this.getPersistentData().getDouble("bounces") - (double) 1.0F);
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return referenceStack.copy();
    }

    @Override
    public void tick() {
        super.tick();

        if (ignoreGravityTicks > 0) {
            setNoGravity(true);
            ignoreGravityTicks--;
        } else {
            setNoGravity(false);
        }

        if (this.getPersistentData().getBoolean("WaterUnaffected") && this.isInWaterOrRain()) {
            this.setNoGravity(true);
        }

        CompoundTag tag = this.referenceStack.getTag();
        if (tag != null && "slime_ball".equals(tag.getString("Feather"))) {
            */
/*if (this.inGround && bounceCount < MAX_BOUNCES) {
                this.inGround = false;
            }*//*

            //performSlimeBounce(this.level(), this);
            if (Math.random() < 0.3 && this.level() instanceof ServerLevel _level) {
                _level.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), Mth.nextInt(RandomSource.create(), 0, 1), 0.1, 0.1, 0.1, 0.1);
            }
        }
    }

    @Override
    protected float getWaterInertia() {
        CompoundTag tag = referenceStack.getTag();
        if (tag == null) return super.getWaterInertia();
        boolean isWaterUnaffected = "prismarine".equals(tag.getString("Feather"));
        if (isWaterUnaffected) return 0.99f;
        return super.getWaterInertia();
    }

    @Override
    public void setSoundEvent(SoundEvent pSoundEvent) {
        super.setSoundEvent(SoundEvents.SLIME_ATTACK);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.SLIME_BLOCK_HIT;
    }


    @Override
    public boolean shouldFall() {
        if (isSlimeArrow) {
            int maxBounces = ServerConfig.SLIME_ARROW_BOUNCE.get();
            double multiplier = ServerConfig.SLIME_ARROW_BOUNCE_MULTIPLIER.get();

            int bounces = this.getEntityData().get(TIMES_BOUNCED);
            if (bounces < maxBounces && this.inGround) {
                this.getEntityData().set(TIMES_BOUNCED, bounces + 1);

                // Bounce damping decreases each bounce
                double damping = 1 - (bounces + 1) * 0.1;
                damping = Mth.clamp(damping, 0.05, 1.0); // Prevent too much bounce

                Vec3 motion = this.getDeltaMovement();
                double speed = motion.length();
                double speedFactor = Mth.clamp(speed / 1.5, 1.0, 2.0); // 1.5 is roughly normal arrow speed
                double effectiveMultiplier = multiplier * speedFactor;

                Vec3 forward = this.getForward();

                this.push((double) -1.0F * forward.x * -effectiveMultiplier, (double) -1.0F * forward.y * -effectiveMultiplier, (double) -1.0F * forward.z * effectiveMultiplier);

                // Rotate to simulate bounce
                this.setYRot(this.getYRot() * -1.0F);
                this.yRotO += this.getYRot() * -1.0F;
                this.yOld *= (double) -1.0F;

                return true;
            } else super.shouldFall();
        }

        return super.shouldFall();
    }


    public static void performSlimeBounce(LevelAccessor world, Entity immediatesourceentity) {
        if (immediatesourceentity == null)
            return;
        double x_reflect = 0;
        double y_reflect = 0;
        double z_reflect = 0;
        double x_delta = 0;
        double y_delta = 0;
        double z_delta = 0;
        if (immediatesourceentity.getPersistentData().getDouble("bounces") > 0
                && (world.getBlockState(BlockPos.containing(immediatesourceentity.getX() + immediatesourceentity.getDeltaMovement().x() * 1, immediatesourceentity.getY(), immediatesourceentity.getZ())).canOcclude()
                || world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY() + immediatesourceentity.getDeltaMovement().y() * 1, immediatesourceentity.getZ())).canOcclude()
                || world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ() + immediatesourceentity.getDeltaMovement().z() * 1)).canOcclude())) {
            x_reflect = 1;
            y_reflect = 1;
            z_reflect = 1;
            if (world.getBlockState(BlockPos.containing(immediatesourceentity.getX() + immediatesourceentity.getDeltaMovement().x() * 1, immediatesourceentity.getY(), immediatesourceentity.getZ())).canOcclude()
                    && !world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY() + immediatesourceentity.getDeltaMovement().y() * 1.2, immediatesourceentity.getZ())).canOcclude()) {
                {
                    Entity _ent = immediatesourceentity;
                    _ent.setYRot((float) (immediatesourceentity.getYRot() + 180));
                    _ent.setXRot(immediatesourceentity.getXRot());
                    _ent.setYBodyRot(_ent.getYRot());
                    _ent.setYHeadRot(_ent.getYRot());
                    _ent.yRotO = _ent.getYRot();
                    _ent.xRotO = _ent.getXRot();
                    if (_ent instanceof LivingEntity _entity) {
                        _entity.yBodyRotO = _entity.getYRot();
                        _entity.yHeadRotO = _entity.getYRot();
                    }
                }
                x_reflect = -1;
            }
            if (world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ() + immediatesourceentity.getDeltaMovement().z() * 1)).canOcclude()
                    && !world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY() + immediatesourceentity.getDeltaMovement().y() * 1.2, immediatesourceentity.getZ())).canOcclude()) {
                {
                    Entity _ent = immediatesourceentity;
                    _ent.setYRot((float) (immediatesourceentity.getYRot() + 180));
                    _ent.setXRot(immediatesourceentity.getXRot());
                    _ent.setYBodyRot(_ent.getYRot());
                    _ent.setYHeadRot(_ent.getYRot());
                    _ent.yRotO = _ent.getYRot();
                    _ent.xRotO = _ent.getXRot();
                    if (_ent instanceof LivingEntity _entity) {
                        _entity.yBodyRotO = _entity.getYRot();
                        _entity.yHeadRotO = _entity.getYRot();
                    }
                }
                z_reflect = -1;
            }
            if (world.getBlockState(BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY() + immediatesourceentity.getDeltaMovement().y() * 1, immediatesourceentity.getZ())).canOcclude() && x_reflect > 0 && z_reflect > 0) {
                {
                    Entity _ent = immediatesourceentity;
                    _ent.setYRot(immediatesourceentity.getYRot());
                    _ent.setXRot((float) (immediatesourceentity.getXRot() + 180));
                    _ent.setYBodyRot(_ent.getYRot());
                    _ent.setYHeadRot(_ent.getYRot());
                    _ent.yRotO = _ent.getYRot();
                    _ent.xRotO = _ent.getXRot();
                    if (_ent instanceof LivingEntity _entity) {
                        _entity.yBodyRotO = _entity.getYRot();
                        _entity.yHeadRotO = _entity.getYRot();
                    }
                }
                y_reflect = -1;
            }
            immediatesourceentity.getPersistentData().putDouble("bounces", (immediatesourceentity.getPersistentData().getDouble("bounces") - 1));
            x_delta = immediatesourceentity.getDeltaMovement().x() * 0.8 * x_reflect;
            y_delta = immediatesourceentity.getDeltaMovement().y() * 0.8 * y_reflect;
            z_delta = immediatesourceentity.getDeltaMovement().z() * 0.8 * z_reflect;
            if (world instanceof Level _level) {
                if (!_level.isClientSide()) {
                    _level.playSound(null, BlockPos.containing(immediatesourceentity.getX(), immediatesourceentity.getY(), immediatesourceentity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.slime.jump")),
                            SoundSource.NEUTRAL, 1, 1);
                } else {
                    _level.playLocalSound((immediatesourceentity.getX()), (immediatesourceentity.getY()), (immediatesourceentity.getZ()), ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse("entity.slime.jump")), SoundSource.NEUTRAL, 1, 1,
                            false);
                }
            }
            immediatesourceentity.setDeltaMovement(new Vec3(x_delta, y_delta, z_delta));
            {
                Entity _ent = immediatesourceentity;
                _ent.teleportTo((immediatesourceentity.getX() + x_delta * 1.15), (immediatesourceentity.getY() + y_delta * 1.15), (immediatesourceentity.getZ() + z_delta * 1.15));
                if (_ent instanceof ServerPlayer _serverPlayer)
                    _serverPlayer.connection.teleport((immediatesourceentity.getX() + x_delta * 1.15), (immediatesourceentity.getY() + y_delta * 1.15), (immediatesourceentity.getZ() + z_delta * 1.15), _ent.getYRot(), _ent.getXRot());
            }
        }
    }
}
*/
