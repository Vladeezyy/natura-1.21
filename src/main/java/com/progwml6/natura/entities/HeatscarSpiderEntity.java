package com.progwml6.natura.entities;

import com.progwml6.natura.common.NaturaConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 EntityHeatscarSpider / EntityBabyHeatscarSpider: fire-proof nether spiders that jump higher (0.62), take extra fall
 * damage past 5 blocks (on top of the normal fall damage, as in 1.12), and set the mobs they hit on fire (5 s on Normal,
 * 10 s on Hard; players were not EntityLiving in 1.12, so they are not set on fire). The big one (50 health, speed 0.45,
 * damage 4, 25 xp) drops 2-4 babies (config) when it dies or despawns (1.12 setDead). Only with "Enable Heatscar
 * Spiders" (off by default) do they spawn.
 */
public class HeatscarSpiderEntity extends Spider {
    private final boolean baby;

    public HeatscarSpiderEntity(EntityType<? extends HeatscarSpiderEntity> type, Level level, boolean baby) {
        super(type, level);
        this.baby = baby;
        if (!baby) {
            this.xpReward = 25;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes().add(Attributes.MAX_HEALTH, 50.0D).add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.45000001788139344D).add(Attributes.ATTACK_DAMAGE, 4.0D);
    }

    @Override
    protected float getJumpPower() {
        return 0.62F;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        boolean hurt = super.causeFallDamage(distance, multiplier, source);
        int i = Mth.ceil(distance - 5.0F);
        if (i > 0) {
            this.playSound(i > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
            this.hurt(this.damageSources().fall(), i);
            BlockState state = this.level().getBlockState(BlockPos.containing(this.getX(), this.getY() - 0.2D, this.getZ()));
            if (!state.isAir()) {
                SoundType sound = state.getSoundType(this.level(), this.blockPosition(), this);
                this.playSound(sound.getStepSound(), sound.getVolume() * 0.5F, sound.getPitch() * 0.75F);
            }
            return true;
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof Mob) {
                int time = switch (this.level().getDifficulty()) {
                    case NORMAL -> 5;
                    case HARD -> 10;
                    default -> 0;
                };
                if (time > 0) {
                    target.igniteForSeconds(time);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.baby && !this.level().isClientSide && (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED)) {
            int min = NaturaConfig.BABY_HEATSCAR_MINIMUM.get();
            int num = NaturaConfig.BABY_HEATSCAR_MAXIMUM.get() - min + 1;
            int amount = this.random.nextInt(Math.max(1, num)) + min;
            for (int i = 0; i < amount; i++) {
                double f = this.random.nextDouble() * 2;
                double f1 = this.random.nextDouble() * 2;
                HeatscarSpiderEntity baby = NaturaEntities.BABY_HEATSCAR_SPIDER.get().create(this.level());
                if (baby != null) {
                    baby.moveTo(this.getX() + f, this.getY() + 0.5D, this.getZ() + f1, this.random.nextFloat() * 360.0F, 0.0F);
                    this.level().addFreshEntity(baby);
                }
            }
        }
        super.remove(reason);
    }

    /** 1.12 getCanSpawnHere: not on Peaceful, no light check (the collision and liquid checks are the vanilla ones). */
    public static boolean checkHeatscarSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && NaturaConfig.ENABLE_HEATSCAR_SPIDER.get();
    }
}
