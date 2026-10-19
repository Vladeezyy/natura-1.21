package com.progwml6.natura.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * 1.12 EntityNitroCreeper: a fire-proof creeper with a short fuse (12 + the difficulty, +12 when charged) and a big blast
 * (3, charged 20), that also explodes when it falls more than 5 blocks. Iron golems kill it in one hit (1000 damage).
 * It saves its fuse as "Fuse", which the vanilla creeper reads too (1.12 did the same).
 */
public class NitroCreeperEntity extends Creeper {
    private int lastActiveTime;
    private int timeSinceIgnited;
    private int fuseTime = 12;
    private int explosionRadius = 1;

    public NitroCreeperEntity(EntityType<? extends NitroCreeperEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // 1.12 added a second melee task to the creeper's own one
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        if (!this.level().isClientSide) {
            if (distance > 5.0F) {
                this.explode();
                return false;
            }
            return super.causeFallDamage(distance, multiplier, source);
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Fuse", (short) this.fuseTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fuse", 99)) {
            this.fuseTime = tag.getShort("Fuse");
        }
    }

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.lastActiveTime = this.timeSinceIgnited;
            if (this.isIgnited()) {
                this.setSwellDir(1);
            }
            int i = this.getSwellDir();
            if (i > 0 && this.timeSinceIgnited == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            }
            this.timeSinceIgnited += i;
            if (this.timeSinceIgnited < 0) {
                this.timeSinceIgnited = 0;
            }
            int difficulty = this.level().getDifficulty().getId();
            int powered = this.isPowered() ? 12 : 0;
            if (this.timeSinceIgnited >= this.fuseTime + difficulty + powered) {
                this.timeSinceIgnited = this.fuseTime;
                this.explode();
            }
        }
        super.tick();
    }

    private void explode() {
        if (!this.level().isClientSide && this.isAlive()) {
            float f = this.isPowered() ? 20.0F : 3.0F;
            this.dead = true;
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionRadius * f, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    public float getSwelling(float partialTicks) {
        return (this.lastActiveTime + (this.timeSinceIgnited - this.lastActiveTime) * partialTicks) / (this.fuseTime - 2);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof IronGolem) {
            amount = 1000;
        }
        return super.hurt(source, amount);
    }
}
