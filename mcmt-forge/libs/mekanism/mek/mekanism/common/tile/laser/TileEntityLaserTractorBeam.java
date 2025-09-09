package mekanism.common.tile.laser;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public class TileEntityLaserTractorBeam extends TileEntityLaserReceptor {
   public TileEntityLaserTractorBeam(BlockPos pos, BlockState state) {
      super(MekanismBlocks.LASER_TRACTOR_BEAM, pos, state);
   }

   @Override
   protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
      builder.addContainer(
         this.energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.notExternal, BasicEnergyContainer.internalOnly, this, listener)
      );
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);

      for (int slotX = 0; slotX < 9; slotX++) {
         for (int slotY = 0; slotY < 3; slotY++) {
            OutputInventorySlot slot = OutputInventorySlot.at(listener, 8 + slotX * 18, 16 + slotY * 18);
            builder.addSlot(slot);
            slot.setSlotType(ContainerSlotType.NORMAL);
         }
      }

      return builder.build();
   }

   @Override
   protected void handleBreakBlock(BlockState state, BlockPos hitPos, Player player, ItemStack tool) {
      List<ItemStack> drops = new ArrayList<>(
         Block.m_49874_(state, (ServerLevel)this.f_58857_, hitPos, WorldUtils.getTileEntity(this.f_58857_, hitPos), player, tool)
      );
      CommonWorldTickHandler.fallbackItemCollector = drops::add;
      this.breakBlock(state, hitPos);
      CommonWorldTickHandler.fallbackItemCollector = null;
      if (!drops.isEmpty()) {
         Lazy<Direction> direction = Lazy.of(this::getDirection);
         Lazy<BlockPos> dropPos = Lazy.of(() -> this.f_58858_.m_5484_((Direction)direction.get(), 2));
         Lazy<Direction> opposite = Lazy.of(() -> ((Direction)direction.get()).m_122424_());
         List<IInventorySlot> inventorySlots = this.getInventorySlots(null);

         for (ItemStack drop : drops) {
            drop = InventoryUtils.insertItem(inventorySlots, drop, Action.EXECUTE, AutomationType.INTERNAL);
            if (!drop.m_41619_()) {
               Block.m_152435_(this.f_58857_, (BlockPos)dropPos.get(), (Direction)opposite.get(), drop);
            }
         }
      }
   }

   @Override
   protected boolean handleHitItem(ItemEntity entity) {
      ItemStack stack = entity.m_32055_();
      stack = InventoryUtils.insertItem(this.getInventorySlots(null), stack, Action.EXECUTE, AutomationType.INTERNAL);
      if (stack.m_41619_()) {
         entity.m_146870_();
      }

      return true;
   }

   @ComputerMethod
   int getSlotCount() {
      return this.getSlots();
   }

   @ComputerMethod
   ItemStack getItemInSlot(int slot) throws ComputerException {
      int slots = this.getSlotCount();
      if (slot >= 0 && slot < slots) {
         return this.getStackInSlot(slot);
      } else {
         throw new ComputerException("Slot: '%d' is out of bounds, as this laser amplifier only has '%d' slots (zero indexed).", slot, slots);
      }
   }
}
