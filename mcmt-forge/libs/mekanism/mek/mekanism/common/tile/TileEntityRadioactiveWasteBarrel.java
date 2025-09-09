package mekanism.common.tile;

import java.util.Collections;
import mekanism.api.Action;
import mekanism.api.IConfigurable;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.chemical.StackedWasteBarrel;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityRadioactiveWasteBarrel extends TileEntityMekanism implements IConfigurable {
   private long lastProcessTick;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getStored", "getCapacity", "getNeeded", "getFilledPercentage"},
      docPlaceholder = "barrel"
   )
   StackedWasteBarrel gasTank;
   private float prevScale;
   private int processTicks;

   public TileEntityRadioactiveWasteBarrel(BlockPos pos, BlockState state) {
      super(MekanismBlocks.RADIOACTIVE_WASTE_BARREL, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSide(this::getDirection);
      builder.addTank(this.gasTank = StackedWasteBarrel.create(this, listener), RelativeSide.TOP, RelativeSide.BOTTOM);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.f_58857_.m_46467_() > this.lastProcessTick) {
         this.lastProcessTick = this.f_58857_.m_46467_();
         if (MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get() > 0L
            && !this.gasTank.isEmpty()
            && !MekanismTags.Gases.WASTE_BARREL_DECAY_LOOKUP.contains(this.gasTank.getType())
            && ++this.processTicks >= MekanismConfig.general.radioactiveWasteBarrelProcessTicks.get()) {
            this.processTicks = 0;
            this.gasTank.shrinkStack(MekanismConfig.general.radioactiveWasteBarrelDecayAmount.get(), Action.EXECUTE);
         }

         if (this.getActive()) {
            ChemicalUtil.emit(Collections.singleton(Direction.DOWN), this.gasTank, this);
         }
      }
   }

   public StackedWasteBarrel getGasTank() {
      return this.gasTank;
   }

   public double getGasScale() {
      return (double)this.gasTank.getStored() / this.gasTank.getCapacity();
   }

   public GasStack getGas() {
      return this.gasTank.getStack();
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

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128365_("gas", this.gasTank.serializeNBT());
      updateTag.m_128405_("progress", this.processTicks);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setCompoundIfPresent(tag, "gas", nbt -> this.gasTank.deserializeNBT(nbt));
      NBTUtils.setIntIfPresent(tag, "progress", val -> this.processTicks = val);
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.gasTank.getStored(), this.gasTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.GAS;
   }
}
