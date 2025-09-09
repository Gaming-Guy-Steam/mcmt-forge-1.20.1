package mekanism.common.tile.laser;

import mekanism.api.IContentsListener;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableEnum;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntityLaserAmplifier extends TileEntityLaserReceptor implements IHasMode {
   private FloatingLong minThreshold = FloatingLong.ZERO;
   private FloatingLong maxThreshold = MekanismConfig.storage.laserAmplifier.get();
   private int ticks = 0;
   private int delay = 0;
   private boolean emittingRedstone;
   private TileEntityLaserAmplifier.RedstoneOutput outputMode = TileEntityLaserAmplifier.RedstoneOutput.OFF;

   public TileEntityLaserAmplifier(BlockPos pos, BlockState state) {
      super(MekanismBlocks.LASER_AMPLIFIER, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
   }

   @Override
   protected void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener) {
      builder.addContainer(
         this.energyContainer = LaserEnergyContainer.create(BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.internalOnly, this, listener)
      );
   }

   @Override
   protected void onUpdateServer() {
      this.setEmittingRedstone(false);
      if (this.ticks < this.delay) {
         this.ticks++;
      } else {
         this.ticks = 0;
      }

      super.onUpdateServer();
      if (this.outputMode != TileEntityLaserAmplifier.RedstoneOutput.ENTITY_DETECTION) {
         this.setEmittingRedstone(false);
      }
   }

   @Override
   protected void setEmittingRedstone(boolean foundEntity) {
      this.emittingRedstone = foundEntity;
   }

   private boolean shouldFire() {
      return this.ticks >= this.delay && this.energyContainer.getEnergy().compareTo(this.minThreshold) >= 0 && MekanismUtils.canFunction(this);
   }

   @Override
   protected FloatingLong toFire() {
      return this.shouldFire() ? super.toFire().min(this.maxThreshold) : FloatingLong.ZERO;
   }

   @Override
   public int getRedstoneLevel() {
      if (this.outputMode == TileEntityLaserAmplifier.RedstoneOutput.ENERGY_CONTENTS) {
         return MekanismUtils.redstoneLevelFromContents(this.energyContainer.getEnergy(), this.energyContainer.getMaxEnergy());
      } else {
         return this.emittingRedstone ? 15 : 0;
      }
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.ENERGY;
   }

   @Override
   protected void notifyComparatorChange() {
      this.f_58857_.m_46672_(this.m_58899_(), this.getBlockType());
   }

   public void setDelay(int delay) {
      delay = Math.max(0, delay);
      if (this.delay != delay) {
         this.delay = delay;
         this.markForSave();
      }
   }

   @Override
   public void nextMode() {
      this.outputMode = this.outputMode.getNext();
      this.m_6596_();
   }

   @Override
   public void previousMode() {
      this.outputMode = this.outputMode.getPrevious();
      this.m_6596_();
   }

   public void setMinThresholdFromPacket(FloatingLong target) {
      if (this.updateMinThreshold(target)) {
         this.markForSave();
      }
   }

   public void setMaxThresholdFromPacket(FloatingLong target) {
      if (this.updateMaxThreshold(target)) {
         this.markForSave();
      }
   }

   private boolean updateMinThreshold(FloatingLong target) {
      FloatingLong threshold = this.getThreshold(target);
      if (!this.minThreshold.equals(threshold)) {
         this.minThreshold = threshold;
         if (this.minThreshold.greaterThan(this.maxThreshold)) {
            this.maxThreshold = this.minThreshold;
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean updateMaxThreshold(FloatingLong target) {
      FloatingLong threshold = this.getThreshold(target);
      if (!this.maxThreshold.equals(threshold)) {
         this.maxThreshold = threshold;
         if (this.maxThreshold.smallerThan(this.minThreshold)) {
            this.minThreshold = this.maxThreshold;
         }

         return true;
      } else {
         return false;
      }
   }

   private FloatingLong getThreshold(FloatingLong target) {
      FloatingLong maxEnergy = this.energyContainer.getMaxEnergy();
      return target.smallerOrEqual(maxEnergy) ? target : maxEnergy.copyAsConst();
   }

   @Override
   protected void loadGeneralPersistentData(CompoundTag data) {
      super.loadGeneralPersistentData(data);
      NBTUtils.setFloatingLongIfPresent(data, "min", this::updateMinThreshold);
      NBTUtils.setFloatingLongIfPresent(data, "max", this::updateMaxThreshold);
      NBTUtils.setIntIfPresent(data, "time", value -> this.delay = value);
      NBTUtils.setEnumIfPresent(data, "outputMode", TileEntityLaserAmplifier.RedstoneOutput::byIndexStatic, mode -> this.outputMode = mode);
   }

   @Override
   protected void addGeneralPersistentData(CompoundTag data) {
      super.addGeneralPersistentData(data);
      data.m_128359_("min", this.minThreshold.toString());
      data.m_128359_("max", this.maxThreshold.toString());
      data.m_128405_("time", this.delay);
      NBTUtils.writeEnum(data, "outputMode", this.outputMode);
   }

   @Override
   public boolean canPulse() {
      return true;
   }

   @ComputerMethod(
      nameOverride = "getRedstoneOutputMode"
   )
   public TileEntityLaserAmplifier.RedstoneOutput getOutputMode() {
      return this.outputMode;
   }

   @ComputerMethod
   public int getDelay() {
      return this.delay;
   }

   @ComputerMethod
   public FloatingLong getMinThreshold() {
      return this.minThreshold;
   }

   @ComputerMethod
   public FloatingLong getMaxThreshold() {
      return this.maxThreshold;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getMinThreshold, value -> this.minThreshold = value));
      container.track(SyncableFloatingLong.create(this::getMaxThreshold, value -> this.maxThreshold = value));
      container.track(SyncableInt.create(this::getDelay, value -> this.delay = value));
      container.track(
         SyncableEnum.create(
            TileEntityLaserAmplifier.RedstoneOutput::byIndexStatic,
            TileEntityLaserAmplifier.RedstoneOutput.OFF,
            this::getOutputMode,
            value -> this.outputMode = value
         )
      );
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setRedstoneOutputMode(TileEntityLaserAmplifier.RedstoneOutput mode) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.outputMode != mode) {
         this.outputMode = mode;
         this.m_6596_();
      }
   }

   @ComputerMethod(
      nameOverride = "setDelay",
      requiresPublicSecurity = true
   )
   void computerSetDelay(int delay) throws ComputerException {
      this.validateSecurityIsPublic();
      if (delay < 0) {
         throw new ComputerException("Delay cannot be negative. Received: %d", delay);
      } else {
         this.setDelay(delay);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setMinThreshold(FloatingLong threshold) throws ComputerException {
      this.validateSecurityIsPublic();
      this.setMinThresholdFromPacket(threshold);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setMaxThreshold(FloatingLong threshold) throws ComputerException {
      this.validateSecurityIsPublic();
      this.setMaxThresholdFromPacket(threshold);
   }

   @NothingNullByDefault
   public static enum RedstoneOutput implements IIncrementalEnum<TileEntityLaserAmplifier.RedstoneOutput>, IHasTranslationKey {
      OFF(MekanismLang.OFF),
      ENTITY_DETECTION(MekanismLang.ENTITY_DETECTION),
      ENERGY_CONTENTS(MekanismLang.ENERGY_CONTENTS);

      private static final TileEntityLaserAmplifier.RedstoneOutput[] MODES = values();
      private final ILangEntry langEntry;

      private RedstoneOutput(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public String getTranslationKey() {
         return this.langEntry.getTranslationKey();
      }

      public TileEntityLaserAmplifier.RedstoneOutput byIndex(int index) {
         return byIndexStatic(index);
      }

      public static TileEntityLaserAmplifier.RedstoneOutput byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
