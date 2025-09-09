package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityPersonalBarrel extends TileEntityPersonalStorage {
   public TileEntityPersonalBarrel(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PERSONAL_BARREL, pos, state);
   }

   @Override
   protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
      this.playSound(level, state, SoundEvents.f_11725_);
      level.m_46597_(this.m_58899_(), (BlockState)state.m_61124_(BarrelBlock.f_49043_, true));
   }

   @Override
   protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
      this.playSound(level, state, SoundEvents.f_11724_);
      level.m_46597_(this.m_58899_(), (BlockState)state.m_61124_(BarrelBlock.f_49043_, false));
   }

   private void playSound(@NotNull Level level, BlockState state, SoundEvent sound) {
      Vec3i vec3i = ((Direction)state.m_61143_(BarrelBlock.f_49042_)).m_122436_();
      double d0 = this.f_58858_.m_123341_() + 0.5 + vec3i.m_123341_() / 2.0;
      double d1 = this.f_58858_.m_123342_() + 0.5 + vec3i.m_123342_() / 2.0;
      double d2 = this.f_58858_.m_123343_() + 0.5 + vec3i.m_123343_() / 2.0;
      level.m_6263_(null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5F, level.f_46441_.m_188501_() * 0.1F + 0.9F);
   }

   @Override
   protected ResourceLocation getStat() {
      return Stats.f_12971_;
   }
}
