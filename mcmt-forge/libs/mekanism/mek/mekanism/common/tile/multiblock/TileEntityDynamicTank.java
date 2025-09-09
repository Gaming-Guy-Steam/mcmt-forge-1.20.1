package mekanism.common.tile.multiblock;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class TileEntityDynamicTank extends TileEntityMultiblock<TankMultiblockData> implements IFluidContainerManager {
   public TileEntityDynamicTank(BlockPos pos, BlockState state) {
      this(MekanismBlocks.DYNAMIC_TANK, pos, state);
      this.addDisabledCapabilities(new Capability[]{ForgeCapabilities.ITEM_HANDLER});
   }

   public TileEntityDynamicTank(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   @Override
   public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
      if (!player.m_6144_()) {
         TankMultiblockData multiblock = this.getMultiblock();
         if (multiblock.isFormed()) {
            if (this.manageInventory(multiblock, player, hand, stack)) {
               player.m_150109_().m_6596_();
               return InteractionResult.SUCCESS;
            }

            return this.openGui(player);
         }
      }

      return InteractionResult.PASS;
   }

   @NotNull
   public TankMultiblockData createMultiblock() {
      return new TankMultiblockData(this);
   }

   @Override
   public MultiblockManager<TankMultiblockData> getManager() {
      return Mekanism.tankManager;
   }

   @Override
   public IFluidContainerManager.ContainerEditMode getContainerEditMode() {
      return this.getMultiblock().editMode;
   }

   @Override
   public void nextMode() {
      TankMultiblockData multiblock = this.getMultiblock();
      multiblock.setContainerEditMode(multiblock.editMode.getNext());
   }

   @Override
   public void previousMode() {
      TankMultiblockData multiblock = this.getMultiblock();
      multiblock.setContainerEditMode(multiblock.editMode.getPrevious());
   }

   private boolean manageInventory(TankMultiblockData multiblock, Player player, InteractionHand hand, ItemStack itemStack) {
      return multiblock.isFormed() ? FluidUtils.handleTankInteraction(player, hand, itemStack, multiblock.getFluidTank()) : false;
   }
}
