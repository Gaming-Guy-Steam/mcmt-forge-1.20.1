package mekanism.common.tile;

import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class TileEntityIndustrialAlarm extends TileEntityMekanism {
   public TileEntityIndustrialAlarm(BlockPos pos, BlockState state) {
      super(MekanismBlocks.INDUSTRIAL_ALARM, pos, state);
      this.delaySupplier = () -> 3;
      this.onPowerChange();
   }

   @Override
   public void onPowerChange() {
      super.onPowerChange();
      if (this.m_58904_() != null && !this.m_58904_().m_5776_()) {
         this.setActive(this.isPowered());
      }
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return this.getActive() ? new AABB(this.f_58858_, this.f_58858_.m_7918_(1, 1, 1)) : super.getRenderBoundingBox();
   }
}
