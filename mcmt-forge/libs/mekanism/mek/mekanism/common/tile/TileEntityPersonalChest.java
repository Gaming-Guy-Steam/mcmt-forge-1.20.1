package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityPersonalChest extends TileEntityPersonalStorage implements LidBlockEntity {
   private final ChestLidController chestLidController = new ChestLidController();

   public TileEntityPersonalChest(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PERSONAL_CHEST, pos, state);
   }

   @Override
   protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
      level.m_6263_(
         null,
         pos.m_123341_() + 0.5,
         pos.m_123342_() + 0.5,
         pos.m_123343_() + 0.5,
         SoundEvents.f_11749_,
         SoundSource.BLOCKS,
         0.5F,
         level.f_46441_.m_188501_() * 0.1F + 0.9F
      );
   }

   @Override
   protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
      level.m_6263_(
         null,
         pos.m_123341_() + 0.5,
         pos.m_123342_() + 0.5,
         pos.m_123343_() + 0.5,
         SoundEvents.f_11747_,
         SoundSource.BLOCKS,
         0.5F,
         level.f_46441_.m_188501_() * 0.1F + 0.9F
      );
   }

   @Override
   protected ResourceLocation getStat() {
      return Stats.f_12968_;
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      this.chestLidController.m_155374_();
   }

   public boolean m_7531_(int id, int type) {
      if (id == 1) {
         this.chestLidController.m_155377_(type > 0);
         return true;
      } else {
         return super.m_7531_(id, type);
      }
   }

   public float m_6683_(float partialTicks) {
      return this.chestLidController.m_155375_(partialTicks);
   }

   @Override
   public InteractionResult openGui(Player player) {
      BlockPos above = this.m_58899_().m_7494_();
      return this.f_58857_.m_8055_(above).m_60796_(this.f_58857_, above) ? InteractionResult.CONSUME : super.openGui(player);
   }
}
