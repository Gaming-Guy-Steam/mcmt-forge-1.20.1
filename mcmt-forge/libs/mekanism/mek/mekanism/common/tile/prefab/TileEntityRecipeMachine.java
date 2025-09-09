package mekanism.common.tile.prefab;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BooleanSupplier;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.RecipeCacheLookupMonitor;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityRecipeMachine<RECIPE extends MekanismRecipe> extends TileEntityConfigurableMachine implements IRecipeLookupHandler<RECIPE> {
   public static final int RECIPE_CHECK_FREQUENCY = 100;
   protected final BooleanSupplier recheckAllRecipeErrors;
   private final List<CachedRecipe.OperationTracker.RecipeError> errorTypes;
   private final boolean[] trackedErrors;
   protected RecipeCacheLookupMonitor<RECIPE> recipeCacheLookupMonitor;
   @Nullable
   private IContentsListener recipeCacheSaveOnlyListener;

   protected TileEntityRecipeMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state, List<CachedRecipe.OperationTracker.RecipeError> errorTypes) {
      super(blockProvider, pos, state);
      this.errorTypes = List.copyOf(errorTypes);
      this.recheckAllRecipeErrors = shouldRecheckAllErrors(this);
      this.trackedErrors = new boolean[this.errorTypes.size()];
      this.recipeCacheSaveOnlyListener = null;
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.recipeCacheLookupMonitor = this.createNewCacheMonitor();
   }

   protected RecipeCacheLookupMonitor<RECIPE> createNewCacheMonitor() {
      return new RecipeCacheLookupMonitor<>(this);
   }

   protected IContentsListener getRecipeCacheSaveOnlyListener() {
      if (this.supportsComparator()) {
         if (this.recipeCacheSaveOnlyListener == null) {
            this.recipeCacheSaveOnlyListener = () -> {
               this.markForSave();
               this.recipeCacheLookupMonitor.onChange();
            };
         }

         return this.recipeCacheSaveOnlyListener;
      } else {
         return this.recipeCacheLookupMonitor;
      }
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.trackArray(this.trackedErrors);
   }

   @Override
   public void clearRecipeErrors(int cacheIndex) {
      Arrays.fill(this.trackedErrors, false);
   }

   protected void onErrorsChanged(Set<CachedRecipe.OperationTracker.RecipeError> errors) {
      for (int i = 0; i < this.trackedErrors.length; i++) {
         this.trackedErrors[i] = errors.contains(this.errorTypes.get(i));
      }
   }

   public BooleanSupplier getWarningCheck(CachedRecipe.OperationTracker.RecipeError error) {
      int errorIndex = this.errorTypes.indexOf(error);
      return errorIndex == -1 ? () -> false : () -> this.trackedErrors[errorIndex];
   }

   public static BooleanSupplier shouldRecheckAllErrors(TileEntityMekanism tile) {
      int checkOffset = ThreadLocalRandom.current().nextInt(100);
      return () -> !tile.playersUsing.isEmpty() && tile.m_58898_() && tile.m_58904_().m_46467_() % 100L == checkOffset;
   }

   @Nullable
   @Override
   public final IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      return this.getInitialGasTanks(listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener()));
   }

   @Nullable
   protected IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   public final IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      return this.getInitialInfusionTanks(
         listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener())
      );
   }

   @Nullable
   protected IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(
      IContentsListener listener, IContentsListener recipeCacheListener
   ) {
      return null;
   }

   @Nullable
   @Override
   public final IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener) {
      return this.getInitialPigmentTanks(
         listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener())
      );
   }

   @Nullable
   protected IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   public final IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener) {
      return this.getInitialSlurryTanks(listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener()));
   }

   @Nullable
   protected IChemicalTankHolder<Slurry, SlurryStack, ISlurryTank> getInitialSlurryTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   protected final IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
      return this.getInitialFluidTanks(listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener()));
   }

   @Nullable
   protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   protected final IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      return this.getInitialEnergyContainers(
         listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener())
      );
   }

   @Nullable
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   protected final IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      return this.getInitialInventory(listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener()));
   }

   @Nullable
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      return null;
   }

   @Nullable
   @Override
   protected final IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
      return this.getInitialHeatCapacitors(
         listener, (IContentsListener)(listener == this ? this.recipeCacheLookupMonitor : this.getRecipeCacheSaveOnlyListener()), ambientTemperature
      );
   }

   @Nullable
   protected IHeatCapacitorHolder getInitialHeatCapacitors(
      IContentsListener listener, IContentsListener recipeCacheListener, CachedAmbientTemperature ambientTemperature
   ) {
      return null;
   }
}
