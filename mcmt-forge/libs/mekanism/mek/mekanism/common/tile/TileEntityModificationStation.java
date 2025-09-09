package mekanism.common.tile;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.IModuleItem;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityModificationStation extends TileEntityMekanism implements IBoundingBlock {
   private static final int BASE_TICKS_REQUIRED = 40;
   public int ticksRequired = 40;
   public int operatingTicks;
   private boolean usedEnergy = false;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getModuleItem"},
      docPlaceholder = "module slot"
   )
   InputInventorySlot moduleSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getContainerItem"},
      docPlaceholder = "module holder slot (suit, tool, etc)"
   )
   public InputInventorySlot containerSlot;
   private MachineEnergyContainer<TileEntityModificationStation> energyContainer;

   public TileEntityModificationStation(BlockPos pos, BlockState state) {
      super(MekanismBlocks.MODIFICATION_STATION, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
      return builder.build();
   }

   public MachineEnergyContainer<TileEntityModificationStation> getEnergyContainer() {
      return this.energyContainer;
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.moduleSlot = InputInventorySlot.at(stack -> stack.m_41720_() instanceof IModuleItem, listener, 35, 118));
      builder.addSlot(this.containerSlot = InputInventorySlot.at(stack -> stack.m_41720_() instanceof IModuleContainerItem, listener, 125, 118));
      this.moduleSlot.setSlotType(ContainerSlotType.NORMAL);
      this.moduleSlot.setSlotOverlay(SlotOverlay.MODULE);
      this.containerSlot.setSlotType(ContainerSlotType.NORMAL);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 149, 21));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      FloatingLong clientEnergyUsed = FloatingLong.ZERO;
      if (MekanismUtils.canFunction(this)) {
         boolean operated = false;
         if (this.energyContainer.getEnergy().greaterOrEqual(this.energyContainer.getEnergyPerTick())
            && !this.moduleSlot.isEmpty()
            && !this.containerSlot.isEmpty()) {
            ModuleData<?> data = ((IModuleItem)this.moduleSlot.getStack().m_41720_()).getModuleData();
            ItemStack stack = this.containerSlot.getStack();
            if (IModuleHelper.INSTANCE.getSupported(stack).contains(data)) {
               IModule<?> module = IModuleHelper.INSTANCE.load(stack, data);
               if (module == null || module.getInstalledCount() < data.getMaxStackSize()) {
                  operated = true;
                  this.operatingTicks++;
                  clientEnergyUsed = this.energyContainer.extract(this.energyContainer.getEnergyPerTick(), Action.EXECUTE, AutomationType.INTERNAL);
                  if (this.operatingTicks == this.ticksRequired) {
                     this.operatingTicks = 0;
                     ((IModuleContainerItem)stack.m_41720_()).addModule(stack, data);
                     this.containerSlot.setStack(stack);
                     MekanismUtils.logMismatchedStackSize(this.moduleSlot.shrinkStack(1, Action.EXECUTE), 1L);
                  }
               }
            }
         }

         if (!operated) {
            this.operatingTicks = 0;
         }
      }

      this.usedEnergy = !clientEnergyUsed.isZero();
   }

   public boolean usedEnergy() {
      return this.usedEnergy;
   }

   public void removeModule(Player player, ModuleData<?> type) {
      ItemStack stack = this.containerSlot.getStack();
      if (!stack.m_41619_()) {
         IModuleContainerItem container = (IModuleContainerItem)stack.m_41720_();
         if (container.hasModule(stack, type) && player.m_150109_().m_36054_(type.getItemProvider().getItemStack())) {
            container.removeModule(stack, type);
            this.containerSlot.setStack(stack);
         }
      }
   }

   public double getScaledProgress() {
      return (double)this.operatingTicks / this.ticksRequired;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.operatingTicks = nbt.m_128451_("progress");
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128405_("progress", this.operatingTicks);
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableInt.create(() -> this.operatingTicks, value -> this.operatingTicks = value));
      container.track(SyncableBoolean.create(this::usedEnergy, value -> this.usedEnergy = value));
   }
}
