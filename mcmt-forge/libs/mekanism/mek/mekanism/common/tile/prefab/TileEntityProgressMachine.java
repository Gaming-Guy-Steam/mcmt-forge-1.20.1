package mekanism.common.tile.prefab;

import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityProgressMachine<RECIPE extends MekanismRecipe> extends TileEntityRecipeMachine<RECIPE> {
   private int operatingTicks;
   protected int baseTicksRequired;
   public int ticksRequired;

   protected TileEntityProgressMachine(
      IBlockProvider blockProvider, BlockPos pos, BlockState state, List<CachedRecipe.OperationTracker.RecipeError> errorTypes, int baseTicksRequired
   ) {
      super(blockProvider, pos, state, errorTypes);
      this.baseTicksRequired = baseTicksRequired;
      this.ticksRequired = this.baseTicksRequired;
   }

   public double getScaledProgress() {
      return (double)this.getOperatingTicks() / this.ticksRequired;
   }

   protected void setOperatingTicks(int ticks) {
      this.operatingTicks = ticks;
   }

   @ComputerMethod(
      nameOverride = "getRecipeProgress"
   )
   public int getOperatingTicks() {
      return this.operatingTicks;
   }

   @ComputerMethod
   public int getTicksRequired() {
      return this.ticksRequired;
   }

   @Override
   public int getSavedOperatingTicks(int cacheIndex) {
      return this.getOperatingTicks();
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.operatingTicks = nbt.m_128451_("progress");
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128405_("progress", this.getOperatingTicks());
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.ticksRequired = MekanismUtils.getTicks(this, this.baseTicksRequired);
      }
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableInt.create(this::getOperatingTicks, this::setOperatingTicks));
      container.track(SyncableInt.create(this::getTicksRequired, value -> this.ticksRequired = value));
   }
}
