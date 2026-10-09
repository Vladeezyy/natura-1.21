package com.progwml6.natura.entities;

import com.progwml6.natura.common.NaturaItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 EntityImp: a fire-proof nether animal (40 health, pig sounds) that follows players holding a Natura stew, runs
 * from players, and breeds on glowshroom stews and berry medley (1.12 soups metas 4-9), giving the bowl back. The 1.12
 * AI speeds multiplied the default 0.7 movement speed.
 */
public class ImpEntity extends Animal {
    public ImpEntity(EntityType<? extends ImpEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.7D);
    }

    private static Ingredient soups() {
        return Ingredient.of(NaturaItems.SOUPS.values().stream().map(s -> new ItemStack(s.get())));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.38D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 0.3D, soups(), false));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.25D));
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.25D, 0.3D));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 0.28D));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.25D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** 1.12: soups metas 4-9 (the glowshroom stews and berry medley). */
    @Override
    public boolean isFood(ItemStack stack) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        return id.endsWith("glowshroom_stew") || id.equals("berry_medley");
    }

    @Override
    protected void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        if (stack.getItem() instanceof com.progwml6.natura.common.item.NaturaSoupItem soup) {
            ItemStack bowl = soup.bowl();
            if (!player.getInventory().add(bowl)) {
                player.drop(bowl, false, false);
            }
        }
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return NaturaEntities.IMP.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.PIG_STEP, 0.15F, 1.0F);
    }

    /** 1.12 getCanSpawnHere: in a dimension where water vaporizes (the nether), on any block and in any light. */
    public static boolean checkImpSpawnRules(EntityType<ImpEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.dimensionType().ultraWarm();
    }
}
