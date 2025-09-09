package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
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
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
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
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFluidicPlenisher extends TileEntityMekanism implements IConfigurable {
   private static final EnumSet<Direction> dirs = EnumSet.complementOf(EnumSet.of(Direction.UP));
   public static final int BASE_TICKS_REQUIRED = 20;
   private final Set<BlockPos> activeNodes = new ObjectLinkedOpenHashSet();
   private final Set<BlockPos> usedNodes = new ObjectOpenHashSet();
   public boolean finishedCalc;
   public int ticksRequired = 20;
   public int operatingTicks;
   private boolean usedEnergy = false;
   private MachineEnergyContainer<TileEntityFluidicPlenisher> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getFluid", "getFluidCapacity", "getFluidNeeded", "getFluidFilledPercentage"},
      docPlaceholder = "buffer tank"
   )
   public BasicFluidTank fluidTank;
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

   public TileEntityFluidicPlenisher(BlockPos pos, BlockState state) {
      super(MekanismBlocks.FLUIDIC_PLENISHER, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
      builder.addTank(this.fluidTank = BasicFluidTank.input(10000, this::isValidFluid, listener), RelativeSide.TOP);
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
      builder.addSlot(this.inputSlot = FluidInventorySlot.fill(this.fluidTank, listener, 28, 20), RelativeSide.TOP);
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 28, 51), RelativeSide.BOTTOM);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35), RelativeSide.BACK);
      return builder.build();
   }

   private boolean isValidFluid(@NotNull FluidStack stack) {
      return stack.getFluid().getFluidType().canBePlacedInLevel(this.m_58904_(), this.f_58858_.m_7495_(), stack);
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.inputSlot.fillTank(this.outputSlot);
      FloatingLong clientEnergyUsed = FloatingLong.ZERO;
      if (MekanismUtils.canFunction(this) && !this.fluidTank.isEmpty()) {
         FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
         if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
            if (!this.finishedCalc) {
               clientEnergyUsed = this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            }

            this.operatingTicks++;
            if (this.operatingTicks >= this.ticksRequired) {
               this.operatingTicks = 0;
               if (this.finishedCalc) {
                  BlockPos below = this.m_58899_().m_7495_();
                  if (this.canReplace(below, false, false)
                     && this.canExtractBucket()
                     && WorldUtils.tryPlaceContainedLiquid(null, this.f_58857_, below, this.fluidTank.getFluid(), null)) {
                     this.f_58857_.m_142346_(null, GameEvent.f_157769_, below);
                     clientEnergyUsed = this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                     this.fluidTank.extract(1000, Action.EXECUTE, AutomationType.INTERNAL);
                  }
               } else {
                  this.doPlenish();
               }
            }
         }
      }

      this.usedEnergy = !clientEnergyUsed.isZero();
   }

   private boolean canExtractBucket() {
      return this.fluidTank.extract(1000, Action.SIMULATE, AutomationType.INTERNAL).getAmount() == 1000;
   }

   private void doPlenish() {
      if (this.usedNodes.size() >= MekanismConfig.general.maxPlenisherNodes.get()) {
         this.finishedCalc = true;
      } else {
         if (this.activeNodes.isEmpty()) {
            if (!this.usedNodes.isEmpty()) {
               this.finishedCalc = true;
               return;
            }

            BlockPos below = this.m_58899_().m_7495_();
            if (!this.canReplace(below, true, true)) {
               this.finishedCalc = true;
               return;
            }

            this.activeNodes.add(below);
         }

         Set<BlockPos> toRemove = new ObjectOpenHashSet();

         for (BlockPos nodePos : this.activeNodes) {
            if (WorldUtils.isBlockLoaded(this.f_58857_, nodePos)) {
               if (this.canReplace(nodePos, true, false)
                  && this.canExtractBucket()
                  && WorldUtils.tryPlaceContainedLiquid(null, this.f_58857_, nodePos, this.fluidTank.getFluid(), null)) {
                  this.f_58857_.m_142346_(null, GameEvent.f_157769_, nodePos);
                  this.fluidTank.extract(1000, Action.EXECUTE, AutomationType.INTERNAL);
               }

               for (Direction dir : dirs) {
                  BlockPos sidePos = nodePos.m_121945_(dir);
                  if (WorldUtils.isBlockLoaded(this.f_58857_, sidePos) && this.canReplace(sidePos, true, true)) {
                     this.activeNodes.add(sidePos);
                  }
               }

               toRemove.add(nodePos);
               break;
            }

            toRemove.add(nodePos);
         }

         this.usedNodes.addAll(toRemove);
         this.activeNodes.removeAll(toRemove);
      }
   }

   private boolean canReplace(BlockPos pos, boolean checkNodes, boolean isPathfinding) {
      if (checkNodes && this.usedNodes.contains(pos)) {
         return false;
      } else {
         BlockState state = this.f_58857_.m_8055_(pos);
         if (state.m_60795_()) {
            return true;
         } else {
            FluidState currentFluidState = state.m_60819_();
            if (!currentFluidState.m_76178_()) {
               return currentFluidState.m_76170_() ? isPathfinding : true;
            } else {
               FluidStack stack = this.fluidTank.getFluid();
               if (stack.isEmpty()) {
                  return state.m_247087_() || state.m_60734_() instanceof LiquidBlockContainer;
               } else {
                  Fluid fluid = stack.getFluid();
                  return state.m_60722_(fluid)
                     ? true
                     : state.m_60734_() instanceof LiquidBlockContainer liquidBlockContainer && liquidBlockContainer.m_6044_(this.f_58857_, pos, state, fluid);
               }
            }
         }
      }
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128405_("progress", this.operatingTicks);
      nbtTags.m_128379_("finished", this.finishedCalc);
      if (!this.activeNodes.isEmpty()) {
         ListTag activeList = new ListTag();

         for (BlockPos wrapper : this.activeNodes) {
            activeList.add(NbtUtils.m_129224_(wrapper));
         }

         nbtTags.m_128365_("activeNodes", activeList);
      }

      if (!this.usedNodes.isEmpty()) {
         ListTag usedList = new ListTag();

         for (BlockPos obj : this.usedNodes) {
            usedList.add(NbtUtils.m_129224_(obj));
         }

         nbtTags.m_128365_("usedNodes", usedList);
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.operatingTicks = nbt.m_128451_("progress");
      this.finishedCalc = nbt.m_128471_("finished");
      if (nbt.m_128425_("activeNodes", 9)) {
         ListTag tagList = nbt.m_128437_("activeNodes", 10);

         for (int i = 0; i < tagList.size(); i++) {
            this.activeNodes.add(NbtUtils.m_129239_(tagList.m_128728_(i)));
         }
      }

      if (nbt.m_128425_("usedNodes", 9)) {
         ListTag tagList = nbt.m_128437_("usedNodes", 10);

         for (int i = 0; i < tagList.size(); i++) {
            this.usedNodes.add(NbtUtils.m_129239_(tagList.m_128728_(i)));
         }
      }
   }

   public void reset() {
      this.activeNodes.clear();
      this.usedNodes.clear();
      this.finishedCalc = false;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      this.reset();
      player.m_5661_(MekanismLang.PLENISHER_RESET.translate(new Object[0]), true);
      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      return InteractionResult.PASS;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.ticksRequired = MekanismUtils.getTicks(this, 20);
      }
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.fluidTank.getFluidAmount(), this.fluidTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.FLUID;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(() -> this.finishedCalc, value -> this.finishedCalc = value));
      container.track(SyncableBoolean.create(this::usedEnergy, value -> this.usedEnergy = value));
   }

   public boolean usedEnergy() {
      return this.usedEnergy;
   }

   public MachineEnergyContainer<TileEntityFluidicPlenisher> getEnergyContainer() {
      return this.energyContainer;
   }

   @ComputerMethod(
      nameOverride = "reset",
      requiresPublicSecurity = true
   )
   void resetPlenisher() throws ComputerException {
      this.validateSecurityIsPublic();
      this.reset();
   }
}
