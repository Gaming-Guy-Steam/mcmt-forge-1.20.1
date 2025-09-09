package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityElectricPump extends TileEntityMekanism implements IConfigurable {
   private static final int BASE_TICKS_REQUIRED = 19;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"},
      docPlaceholder = "buffer tank"
   )
   public BasicFluidTank fluidTank;
   @NotNull
   private FluidStack activeType = FluidStack.EMPTY;
   public int ticksRequired = 19;
   public int operatingTicks;
   private boolean usedEnergy = false;
   private final Set<BlockPos> recurringNodes = new ObjectOpenHashSet();
   private MachineEnergyContainer<TileEntityElectricPump> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   FluidInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityElectricPump(BlockPos pos, BlockState state) {
      super(MekanismBlocks.ELECTRIC_PUMP, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
      builder.addTank(this.fluidTank = BasicFluidTank.output(10000, listener), RelativeSide.TOP);
      return builder.build();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener), RelativeSide.BACK);
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.inputSlot = FluidInventorySlot.drain(this.fluidTank, listener, 28, 20), RelativeSide.TOP);
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35), RelativeSide.BACK);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.inputSlot.drainTank(this.outputSlot);
      FloatingLong clientEnergyUsed = FloatingLong.ZERO;
      if (MekanismUtils.canFunction(this) && (this.fluidTank.isEmpty() || this.estimateIncrementAmount() <= this.fluidTank.getNeeded())) {
         FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
         if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
            if (!this.activeType.isEmpty()) {
               clientEnergyUsed = this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            }

            this.operatingTicks++;
            if (this.operatingTicks >= this.ticksRequired) {
               this.operatingTicks = 0;
               if (this.suck()) {
                  if (clientEnergyUsed.isZero()) {
                     clientEnergyUsed = this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                  }
               } else {
                  this.reset();
               }
            }
         }
      }

      this.usedEnergy = !clientEnergyUsed.isZero();
      if (!this.fluidTank.isEmpty()) {
         FluidUtils.emit(Collections.singleton(Direction.UP), this.fluidTank, this, 256 * (1 + this.upgradeComponent.getUpgrades(Upgrade.SPEED)));
      }
   }

   public int estimateIncrementAmount() {
      return this.fluidTank.getFluid().getFluid() == MekanismFluids.HEAVY_WATER.getFluid() ? MekanismConfig.general.pumpHeavyWaterAmount.get() : 1000;
   }

   private boolean suck() {
      boolean hasFilter = this.upgradeComponent.isUpgradeInstalled(Upgrade.FILTER);
      if (this.suck(this.f_58858_.m_121945_(Direction.DOWN), hasFilter, true)) {
         return true;
      } else {
         List<BlockPos> tempPumpList = new ArrayList<>(this.recurringNodes);
         Collections.shuffle(tempPumpList);

         for (BlockPos tempPumpPos : tempPumpList) {
            if (this.suck(tempPumpPos, hasFilter, false)) {
               return true;
            }

            for (Direction orientation : EnumUtils.DIRECTIONS) {
               BlockPos side = tempPumpPos.m_121945_(orientation);
               if (WorldUtils.distanceBetween(this.f_58858_, side) <= MekanismConfig.general.maxPumpRange.get() && this.suck(side, hasFilter, true)) {
                  return true;
               }
            }

            this.recurringNodes.remove(tempPumpPos);
         }

         return false;
      }
   }

   private boolean suck(BlockPos pos, boolean hasFilter, boolean addRecurring) {
      Optional<BlockState> state = WorldUtils.getBlockState(this.f_58857_, pos);
      if (state.isPresent()) {
         BlockState blockState = state.get();
         FluidState fluidState = blockState.m_60819_();
         if (!fluidState.m_76178_() && fluidState.m_76170_()) {
            Block block = blockState.m_60734_();
            if (block instanceof IFluidBlock fluidBlock) {
               if (this.validFluid(fluidBlock.drain(this.f_58857_, pos, FluidAction.SIMULATE))) {
                  this.suck(fluidBlock.drain(this.f_58857_, pos, FluidAction.EXECUTE), pos, addRecurring);
                  return true;
               }
            } else if (block instanceof BucketPickup bucketPickup) {
               Fluid sourceFluid = fluidState.m_76152_();
               FluidStack fluidStack = this.getOutput(sourceFluid, hasFilter);
               if (this.validFluid(fluidStack)) {
                  if (sourceFluid != Fluids.f_76193_ || MekanismConfig.general.pumpWaterSources.get()) {
                     ItemStack pickedUpStack = bucketPickup.m_142598_(this.f_58857_, pos, blockState);
                     if (pickedUpStack.m_41619_()) {
                        return false;
                     }

                     if (pickedUpStack.m_41720_() instanceof BucketItem bucket) {
                        sourceFluid = bucket.getFluid();
                        fluidStack = this.getOutput(sourceFluid, hasFilter);
                        if (!this.validFluid(fluidStack)) {
                           Mekanism.logger
                              .warn(
                                 "Fluid removed without successfully picking up. Fluid {} at {} in {} was valid, but after picking up was {}.",
                                 new Object[]{fluidState.m_76152_(), pos, this.f_58857_, sourceFluid}
                              );
                           return false;
                        }
                     }
                  }

                  this.suck(fluidStack, pos, addRecurring);
                  return true;
               }
            }
         }
      }

      return false;
   }

   private FluidStack getOutput(Fluid sourceFluid, boolean hasFilter) {
      return hasFilter && sourceFluid == Fluids.f_76193_
         ? MekanismFluids.HEAVY_WATER.getFluidStack(MekanismConfig.general.pumpHeavyWaterAmount.get())
         : new FluidStack(sourceFluid, 1000);
   }

   private void suck(@NotNull FluidStack fluidStack, BlockPos pos, boolean addRecurring) {
      this.activeType = new FluidStack(fluidStack, 1);
      if (addRecurring) {
         this.recurringNodes.add(pos);
      }

      this.fluidTank.insert(fluidStack, Action.EXECUTE, AutomationType.INTERNAL);
      this.f_58857_.m_142346_(null, GameEvent.f_157816_, pos);
   }

   private boolean validFluid(@NotNull FluidStack fluidStack) {
      if (!fluidStack.isEmpty() && (this.activeType.isEmpty() || this.activeType.isFluidEqual(fluidStack))) {
         if (this.fluidTank.isEmpty()) {
            return true;
         }

         if (this.fluidTank.isFluidEqual(fluidStack)) {
            return fluidStack.getAmount() <= this.fluidTank.getNeeded();
         }
      }

      return false;
   }

   public void reset() {
      this.activeType = FluidStack.EMPTY;
      this.recurringNodes.clear();
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128405_("progress", this.operatingTicks);
      if (!this.activeType.isEmpty()) {
         nbtTags.m_128365_("fluid", this.activeType.writeToNBT(new CompoundTag()));
      }

      if (!this.recurringNodes.isEmpty()) {
         ListTag recurringList = new ListTag();

         for (BlockPos nodePos : this.recurringNodes) {
            recurringList.add(NbtUtils.m_129224_(nodePos));
         }

         nbtTags.m_128365_("recurringNodes", recurringList);
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.operatingTicks = nbt.m_128451_("progress");
      NBTUtils.setFluidStackIfPresent(nbt, "fluid", fluid -> this.activeType = fluid);
      if (nbt.m_128425_("recurringNodes", 9)) {
         ListTag tagList = nbt.m_128437_("recurringNodes", 10);

         for (int i = 0; i < tagList.size(); i++) {
            this.recurringNodes.add(NbtUtils.m_129239_(tagList.m_128728_(i)));
         }
      }
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      this.reset();
      player.m_5661_(MekanismLang.PUMP_RESET.translate(new Object[0]), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      return InteractionResult.PASS;
   }

   @Override
   public boolean canPulse() {
      return true;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.ticksRequired = MekanismUtils.getTicks(this, 19);
      }
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.fluidTank.getFluidAmount(), this.fluidTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.FLUID;
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   public MachineEnergyContainer<TileEntityElectricPump> getEnergyContainer() {
      return this.energyContainer;
   }

   public boolean usedEnergy() {
      return this.usedEnergy;
   }

   @NotNull
   public FluidStack getActiveType() {
      return this.activeType;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(this::usedEnergy, value -> this.usedEnergy = value));
      container.track(SyncableFluidStack.create(this::getActiveType, value -> this.activeType = value));
   }

   @ComputerMethod(
      nameOverride = "reset",
      requiresPublicSecurity = true
   )
   void resetPump() throws ComputerException {
      this.validateSecurityIsPublic();
      this.reset();
   }
}
