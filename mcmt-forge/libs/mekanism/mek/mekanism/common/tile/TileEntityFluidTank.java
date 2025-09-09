package mekanism.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.FluidTankFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IFluidContainerManager;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.upgrade.FluidTankUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFluidTank extends TileEntityMekanism implements IConfigurable, IFluidContainerManager, ISustainedData {
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getStored", "getCapacity", "getNeeded", "getFilledPercentage"},
      docPlaceholder = "tank"
   )
   public FluidTankFluidTank fluidTank;
   private IFluidContainerManager.ContainerEditMode editMode = IFluidContainerManager.ContainerEditMode.BOTH;
   public FluidTankTier tier;
   public int valve;
   @NotNull
   public FluidStack valveFluid = FluidStack.EMPTY;
   public float prevScale;
   private boolean needsPacket;
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
   private boolean updateClientLight = false;

   public TileEntityFluidTank(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.tier = Attribute.getTier(this.getBlockType(), FluidTankTier.class);
   }

   @NotNull
   @Override
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
      builder.addTank(this.fluidTank = FluidTankFluidTank.create(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.inputSlot = FluidInventorySlot.input(this.fluidTank, listener, 146, 19));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 146, 51));
      this.inputSlot.setSlotOverlay(SlotOverlay.INPUT);
      this.outputSlot.setSlotOverlay(SlotOverlay.OUTPUT);
      return builder.build();
   }

   @Override
   protected void onUpdateClient() {
      super.onUpdateClient();
      if (this.updateClientLight) {
         WorldUtils.recheckLighting(this.f_58857_, this.f_58858_);
         this.updateClientLight = false;
      }
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.valve > 0) {
         this.valve--;
         if (this.valve == 0) {
            this.valveFluid = FluidStack.EMPTY;
            this.needsPacket = true;
         }
      }

      float scale = MekanismUtils.getScale(this.prevScale, this.fluidTank);
      if (scale != this.prevScale) {
         if (this.prevScale == 0.0F || scale == 0.0F) {
            WorldUtils.recheckLighting(this.f_58857_, this.f_58858_);
         }

         this.prevScale = scale;
         this.needsPacket = true;
      }

      this.inputSlot.handleTank(this.outputSlot, this.editMode);
      if (this.getActive()) {
         FluidUtils.emit(Collections.singleton(Direction.DOWN), this.fluidTank, this, this.tier.getOutput());
      }

      if (this.needsPacket) {
         this.sendUpdatePacket();
         this.needsPacket = false;
      }
   }

   @Override
   public void writeSustainedData(CompoundTag data) {
      NBTUtils.writeEnum(data, "editMode", this.editMode);
   }

   @Override
   public void readSustainedData(CompoundTag data) {
      NBTUtils.setEnumIfPresent(data, "editMode", IFluidContainerManager.ContainerEditMode::byIndexStatic, mode -> this.editMode = mode);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("editMode", "editMode");
      return remap;
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
   public FluidStack insertFluid(int tank, @NotNull FluidStack stack, @Nullable Direction side, @NotNull Action action) {
      FluidStack remainder = super.insertFluid(tank, stack, side, action);
      if (side == Direction.UP && action.execute() && remainder.getAmount() < stack.getAmount() && !this.isRemote()) {
         if (this.valve == 0) {
            this.needsPacket = true;
         }

         this.valve = 20;
         this.valveFluid = new FluidStack(stack, 1);
      }

      return remainder;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      if (!this.isRemote()) {
         this.setActive(!this.getActive());
         Level world = this.m_58904_();
         if (world != null) {
            world.m_6263_(
               null,
               this.m_58899_().m_123341_(),
               this.m_58899_().m_123342_(),
               this.m_58899_().m_123343_(),
               (SoundEvent)SoundEvents.f_12490_.get(),
               SoundSource.BLOCKS,
               0.3F,
               1.0F
            );
         }
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      return InteractionResult.PASS;
   }

   @ComputerMethod
   @Override
   public IFluidContainerManager.ContainerEditMode getContainerEditMode() {
      return this.editMode;
   }

   @Override
   public void nextMode() {
      this.editMode = this.editMode.getNext();
      this.markForSave();
   }

   @Override
   public void previousMode() {
      this.editMode = this.editMode.getPrevious();
      this.m_6596_();
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof FluidTankUpgradeData data) {
         this.redstone = data.redstone;
         this.inputSlot.setStack(data.inputSlot.getStack());
         this.outputSlot.setStack(data.outputSlot.getStack());
         this.editMode = data.editMode;
         this.fluidTank.setStack(data.stored);

         for (ITileComponent component : this.getComponents()) {
            component.read(data.components);
         }
      } else {
         super.parseUpgradeData(upgradeData);
      }
   }

   @NotNull
   public FluidTankUpgradeData getUpgradeData() {
      return new FluidTankUpgradeData(this.redstone, this.inputSlot, this.outputSlot, this.editMode, this.fluidTank.getFluid(), this.getComponents());
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(
         SyncableEnum.create(
            IFluidContainerManager.ContainerEditMode::byIndexStatic,
            IFluidContainerManager.ContainerEditMode.BOTH,
            () -> this.editMode,
            value -> this.editMode = value
         )
      );
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128365_("fluid", this.fluidTank.getFluid().writeToNBT(new CompoundTag()));
      updateTag.m_128365_("valve", this.valveFluid.writeToNBT(new CompoundTag()));
      updateTag.m_128350_("scale", this.prevScale);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setFluidStackIfPresent(tag, "fluid", fluid -> this.fluidTank.setStack(fluid));
      NBTUtils.setFluidStackIfPresent(tag, "valve", fluid -> this.valveFluid = fluid);
      NBTUtils.setFloatIfPresent(tag, "scale", scale -> {
         if (this.prevScale != scale) {
            if (this.prevScale == 0.0F || scale == 0.0F) {
               this.updateClientLight = true;
            }

            this.prevScale = scale;
         }
      });
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setContainerEditMode(IFluidContainerManager.ContainerEditMode mode) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.editMode != mode) {
         this.editMode = mode;
         this.markForSave();
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void incrementContainerEditMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.nextMode();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void decrementContainerEditMode() throws ComputerException {
      this.validateSecurityIsPublic();
      this.previousMode();
   }
}
