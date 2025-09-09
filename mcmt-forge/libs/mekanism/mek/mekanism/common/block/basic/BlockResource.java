package mekanism.common.block.basic;

import mekanism.common.block.BlockMekanism;
import mekanism.common.resource.BlockResourceInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.NotNull;

public class BlockResource extends BlockMekanism {
   @NotNull
   private final BlockResourceInfo resource;

   public BlockResource(@NotNull BlockResourceInfo resource) {
      super(resource.modifyProperties(Properties.m_284310_().m_60999_()));
      this.resource = resource;
   }

   @NotNull
   public BlockResourceInfo getResourceInfo() {
      return this.resource;
   }

   public boolean isPortalFrame(BlockState state, BlockGetter world, BlockPos pos) {
      return this.resource.isPortalFrame();
   }
}
