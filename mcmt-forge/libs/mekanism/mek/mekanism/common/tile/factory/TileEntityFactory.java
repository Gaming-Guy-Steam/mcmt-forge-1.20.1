package mekanism.common.tile.factory;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FactoryInputInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.monitor.FactoryRecipeCacheLookupMonitor;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MachineUpgradeData;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityFactory<RECIPE extends MekanismRecipe>
   extends TileEntityConfigurableMachine
   implements IRecipeLookupHandler<RECIPE>,
   ISustainedData {
   protected static final int BASE_TICKS_REQUIRED = 200;
   protected FactoryRecipeCacheLookupMonitor<RECIPE>[] recipeCacheLookupMonitors;
   protected BooleanSupplier[] recheckAllRecipeErrors;
   protected final TileEntityFactory.ErrorTracker errorTracker;
   private final boolean[] activeStates;
   protected TileEntityFactory.ProcessInfo[] processInfoSlots;
   public FactoryTier tier;
   public final int[] progress;
   private int ticksRequired = 200;
   private boolean sorting;
   private boolean sortingNeeded = true;
   private FloatingLong lastUsage = FloatingLong.ZERO;
   @NotNull
   protected final FactoryType type;
   protected MachineEnergyContainer<TileEntityFactory<?>> energyContainer;
   protected final List<IInventorySlot> inputSlots;
   protected final List<IInventorySlot> outputSlots;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   protected TileEntityFactory(
      IBlockProvider blockProvider,
      BlockPos pos,
      BlockState state,
      List<CachedRecipe.OperationTracker.RecipeError> errorTypes,
      Set<CachedRecipe.OperationTracker.RecipeError> globalErrorTypes
   ) {
      super(blockProvider, pos, state);
      this.type = Attribute.get(blockProvider, AttributeFactoryType.class).getFactoryType();
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
      this.inputSlots = new ArrayList<>();
      this.outputSlots = new ArrayList<>();

      for (TileEntityFactory.ProcessInfo info : this.processInfoSlots) {
         this.inputSlots.add(info.inputSlot());
         this.outputSlots.add(info.outputSlot());
         if (info.secondaryOutputSlot() != null) {
            this.outputSlots.add(info.secondaryOutputSlot());
         }
      }

      this.configComponent.setupItemIOConfig(this.inputSlots, this.outputSlots, this.energySlot, false);
      IInventorySlot extraSlot = this.getExtraSlot();
      if (extraSlot != null) {
         ConfigInfo itemConfig = this.configComponent.getConfig(TransmissionType.ITEM);
         if (itemConfig != null) {
            itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, extraSlot));
            itemConfig.setDataType(DataType.EXTRA, RelativeSide.BOTTOM);
         }
      }

      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.progress = new int[this.tier.processes];
      this.activeStates = new boolean[this.tier.processes];
      this.recheckAllRecipeErrors = new BooleanSupplier[this.tier.processes];

      for (int i = 0; i < this.recheckAllRecipeErrors.length; i++) {
         this.recheckAllRecipeErrors[i] = TileEntityRecipeMachine.shouldRecheckAllErrors(this);
      }

      this.errorTracker = new TileEntityFactory.ErrorTracker(errorTypes, globalErrorTypes, this.tier.processes);
   }

   protected IContentsListener markAllMonitorsChanged(IContentsListener listener) {
      return () -> {
         listener.onContentsChanged();

         for (FactoryRecipeCacheLookupMonitor<RECIPE> cacheLookupMonitor : this.recipeCacheLookupMonitors) {
            cacheLookupMonitor.onChange();
         }
      };
   }

   @Override
   protected void presetVariables() {
      super.presetVariables();
      this.tier = Attribute.getTier(this.getBlockType(), FactoryTier.class);
      Runnable setSortingNeeded = () -> this.sortingNeeded = true;
      this.recipeCacheLookupMonitors = new FactoryRecipeCacheLookupMonitor[this.tier.processes];

      for (int i = 0; i < this.recipeCacheLookupMonitors.length; i++) {
         this.recipeCacheLookupMonitors[i] = new FactoryRecipeCacheLookupMonitor<>(this, i, setSortingNeeded);
      }
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      this.addSlots(builder, listener, () -> {
         listener.onContentsChanged();
         this.sortingNeeded = true;
      });
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 7, 13));
      return builder.build();
   }

   protected abstract void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener);

   @Nullable
   protected IInventorySlot getExtraSlot() {
      return null;
   }

   public FactoryType getFactoryType() {
      return this.type;
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.handleSecondaryFuel();
      if (this.sortingNeeded && this.isSorting()) {
         this.sortingNeeded = false;
         this.sortInventory();
      } else if (!this.sortingNeeded && CommonWorldTickHandler.flushTagAndRecipeCaches) {
         this.sortingNeeded = true;
      }

      FloatingLong prev = this.energyContainer.getEnergy().copy();

      for (int i = 0; i < this.recipeCacheLookupMonitors.length; i++) {
         if (!this.recipeCacheLookupMonitors[i].updateAndProcess()) {
            this.activeStates[i] = false;
         }
      }

      boolean isActive = false;

      for (boolean state : this.activeStates) {
         if (state) {
            isActive = true;
            break;
         }
      }

      this.setActive(isActive);
      this.lastUsage = isActive ? prev.minusEqual(this.energyContainer.getEnergy()) : FloatingLong.ZERO;
   }

   public boolean inputProducesOutput(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot, boolean updateCache
   ) {
      return outputSlot.isEmpty() || this.getRecipeForInput(process, fallbackInput, outputSlot, secondaryOutputSlot, updateCache) != null;
   }

   protected abstract boolean isCachedRecipeValid(@Nullable CachedRecipe<RECIPE> cached, @NotNull ItemStack stack);

   @Nullable
   protected RECIPE getRecipeForInput(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot, boolean updateCache
   ) {
      if (!CommonWorldTickHandler.flushTagAndRecipeCaches) {
         CachedRecipe<RECIPE> cached = this.getCachedRecipe(process);
         if (cached != null && this.isCachedRecipeValid(cached, fallbackInput)) {
            return cached.getRecipe();
         }
      }

      RECIPE foundRecipe = this.findRecipe(process, fallbackInput, outputSlot, secondaryOutputSlot);
      if (foundRecipe == null) {
         return null;
      } else {
         if (updateCache) {
            this.recipeCacheLookupMonitors[process].updateCachedRecipe(foundRecipe);
         }

         return foundRecipe;
      }
   }

   @Nullable
   protected abstract RECIPE findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   );

   protected abstract int getNeededInput(RECIPE recipe, ItemStack inputStack);

   @Nullable
   private CachedRecipe<RECIPE> getCachedRecipe(int cacheIndex) {
      return this.recipeCacheLookupMonitors[cacheIndex].getCachedRecipe(cacheIndex);
   }

   public BooleanSupplier getWarningCheck(CachedRecipe.OperationTracker.RecipeError error, int processIndex) {
      return this.errorTracker.getWarningCheck(error, processIndex);
   }

   @Override
   public void clearRecipeErrors(int cacheIndex) {
      Arrays.fill(this.errorTracker.trackedErrors[cacheIndex], false);
   }

   protected void setActiveState(boolean state, int cacheIndex) {
      this.activeStates[cacheIndex] = state;
   }

   protected void handleSecondaryFuel() {
   }

   public abstract boolean isValidInputItem(@NotNull ItemStack stack);

   public int getProgress(int cacheIndex) {
      return this.progress[cacheIndex];
   }

   @Override
   public int getSavedOperatingTicks(int cacheIndex) {
      return this.getProgress(cacheIndex);
   }

   public double getScaledProgress(int i, int process) {
      return (double)this.getProgress(process) * i / this.ticksRequired;
   }

   public void toggleSorting() {
      this.sorting = !this.isSorting();
      this.markForSave();
   }

   @ComputerMethod(
      nameOverride = "isAutoSortEnabled"
   )
   public boolean isSorting() {
      return this.sorting;
   }

   @NotNull
   @ComputerMethod(
      nameOverride = "getEnergyUsage",
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   public FloatingLong getLastUsage() {
      return this.lastUsage;
   }

   @ComputerMethod(
      methodDescription = "Total number of ticks it takes currently for the recipe to complete"
   )
   public int getTicksRequired() {
      return this.ticksRequired;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      if (nbt.m_128425_("progress", 11)) {
         int[] savedProgress = nbt.m_128465_("progress");
         if (this.tier.processes != savedProgress.length) {
            Arrays.fill(this.progress, 0);
         }

         for (int i = 0; i < this.tier.processes && i < savedProgress.length; i++) {
            this.progress[i] = savedProgress[i];
         }
      }
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128365_("progress", new IntArrayTag(Arrays.copyOf(this.progress, this.progress.length)));
   }

   @Override
   public void writeSustainedData(CompoundTag data) {
      data.m_128379_("sorting", this.isSorting());
   }

   @Override
   public void readSustainedData(CompoundTag data) {
      NBTUtils.setBooleanIfPresent(data, "sorting", value -> this.sorting = value);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("sorting", "sorting");
      return remap;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.ticksRequired = MekanismUtils.getTicks(this, 200);
      }
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   @Override
   public boolean isConfigurationDataCompatible(BlockEntityType<?> tileType) {
      if (super.isConfigurationDataCompatible(tileType)) {
         return true;
      } else {
         for (FactoryTier factoryTier : EnumUtils.FACTORY_TIERS) {
            if (factoryTier != this.tier && MekanismTileEntityTypes.getFactoryTile(factoryTier, this.type).get() == tileType) {
               return true;
            }
         }

         return this.type.getBaseMachine().getTileType().get() == tileType;
      }
   }

   public boolean hasSecondaryResourceBar() {
      return false;
   }

   public MachineEnergyContainer<TileEntityFactory<?>> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.trackArray(this.progress);
      this.errorTracker.track(container);
      container.track(SyncableFloatingLong.create(this::getLastUsage, value -> this.lastUsage = value));
      container.track(SyncableBoolean.create(this::isSorting, value -> this.sorting = value));
      container.track(SyncableInt.create(this::getTicksRequired, value -> this.ticksRequired = value));
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof MachineUpgradeData data) {
         this.redstone = data.redstone;
         this.setControlType(data.controlType);
         this.getEnergyContainer().setEnergy(data.energyContainer.getEnergy());
         this.sorting = data.sorting;
         this.energySlot.deserializeNBT(data.energySlot.serializeNBT());
         System.arraycopy(data.progress, 0, this.progress, 0, data.progress.length);

         for (int i = 0; i < data.inputSlots.size(); i++) {
            this.inputSlots.get(i).deserializeNBT((CompoundTag)data.inputSlots.get(i).serializeNBT());
         }

         for (int i = 0; i < data.outputSlots.size(); i++) {
            this.outputSlots.get(i).setStack(data.outputSlots.get(i).getStack());
         }

         for (ITileComponent component : this.getComponents()) {
            component.read(data.components);
         }
      } else {
         super.parseUpgradeData(upgradeData);
      }
   }

   protected void validateValidProcess(int process) throws ComputerException {
      if (process < 0 || process >= this.progress.length) {
         throw new ComputerException("Process: '%d' is out of bounds, as this factory only has '%d' processes (zero indexed).", process, this.progress.length);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void setAutoSort(boolean enabled) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.sorting != enabled) {
         this.sorting = enabled;
         this.markForSave();
      }
   }

   @ComputerMethod
   int getRecipeProgress(int process) throws ComputerException {
      this.validateValidProcess(process);
      return this.getProgress(process);
   }

   @ComputerMethod
   ItemStack getInput(int process) throws ComputerException {
      this.validateValidProcess(process);
      return this.processInfoSlots[process].inputSlot().getStack();
   }

   @ComputerMethod
   ItemStack getOutput(int process) throws ComputerException {
      this.validateValidProcess(process);
      return this.processInfoSlots[process].outputSlot().getStack();
   }

   private void sortInventory() {
      Map<HashedItem, TileEntityFactory.RecipeProcessInfo> processes = new HashMap<>();
      List<TileEntityFactory.ProcessInfo> emptyProcesses = new ArrayList<>();

      for (TileEntityFactory.ProcessInfo processInfo : this.processInfoSlots) {
         IInventorySlot inputSlot = processInfo.inputSlot();
         if (inputSlot.isEmpty()) {
            emptyProcesses.add(processInfo);
         } else {
            ItemStack inputStack = inputSlot.getStack();
            HashedItem item = HashedItem.raw(inputStack);
            TileEntityFactory.RecipeProcessInfo recipeProcessInfo = processes.computeIfAbsent(item, i -> new TileEntityFactory.RecipeProcessInfo());
            recipeProcessInfo.processes.add(processInfo);
            recipeProcessInfo.totalCount = recipeProcessInfo.totalCount + inputStack.m_41613_();
            if (recipeProcessInfo.lazyMinPerSlot == null && !CommonWorldTickHandler.flushTagAndRecipeCaches) {
               CachedRecipe<RECIPE> cachedRecipe = this.getCachedRecipe(processInfo.process());
               if (this.isCachedRecipeValid(cachedRecipe, inputStack)) {
                  recipeProcessInfo.lazyMinPerSlot = () -> Math.max(1, this.getNeededInput(cachedRecipe.getRecipe(), inputStack));
               }
            }
         }
      }

      if (!processes.isEmpty()) {
         for (Entry<HashedItem, TileEntityFactory.RecipeProcessInfo> entry : processes.entrySet()) {
            TileEntityFactory.RecipeProcessInfo recipeProcessInfo = entry.getValue();
            if (recipeProcessInfo.lazyMinPerSlot == null) {
               recipeProcessInfo.lazyMinPerSlot = () -> {
                  HashedItem itemx = entry.getKey();
                  ItemStack largerInput = itemx.createStack(Math.min(itemx.getMaxStackSize(), recipeProcessInfo.totalCount));
                  TileEntityFactory.ProcessInfo processInfox = recipeProcessInfo.processes.get(0);
                  RECIPE recipe = this.getRecipeForInput(
                     processInfox.process(), largerInput, processInfox.outputSlot(), processInfox.secondaryOutputSlot(), true
                  );
                  return recipe != null ? Math.max(1, this.getNeededInput(recipe, largerInput)) : 1;
               };
            }
         }

         if (!emptyProcesses.isEmpty()) {
            this.addEmptySlotsAsTargets(processes, emptyProcesses);
         }

         this.distributeItems(processes);
      }
   }

   private void addEmptySlotsAsTargets(Map<HashedItem, TileEntityFactory.RecipeProcessInfo> processes, List<TileEntityFactory.ProcessInfo> emptyProcesses) {
      for (Entry<HashedItem, TileEntityFactory.RecipeProcessInfo> entry : processes.entrySet()) {
         TileEntityFactory.RecipeProcessInfo recipeProcessInfo = entry.getValue();
         int minPerSlot = recipeProcessInfo.getMinPerSlot();
         int maxSlots = recipeProcessInfo.totalCount / minPerSlot;
         if (maxSlots > 1) {
            int processCount = recipeProcessInfo.processes.size();
            if (maxSlots > processCount) {
               ItemStack sourceStack = entry.getKey().getInternalStack();
               int emptyToAdd = maxSlots - processCount;
               int added = 0;
               List<TileEntityFactory.ProcessInfo> toRemove = new ArrayList<>();
               Iterator var13 = emptyProcesses.iterator();

               while (true) {
                  if (var13.hasNext()) {
                     TileEntityFactory.ProcessInfo emptyProcess = (TileEntityFactory.ProcessInfo)var13.next();
                     if (!this.inputProducesOutput(emptyProcess.process(), sourceStack, emptyProcess.outputSlot(), emptyProcess.secondaryOutputSlot(), true)) {
                        continue;
                     }

                     recipeProcessInfo.processes.add(emptyProcess);
                     toRemove.add(emptyProcess);
                     if (++added < emptyToAdd) {
                        continue;
                     }
                  }

                  emptyProcesses.removeAll(toRemove);
                  if (emptyProcesses.isEmpty()) {
                     return;
                  }
                  break;
               }
            }
         }
      }
   }

   private void distributeItems(Map<HashedItem, TileEntityFactory.RecipeProcessInfo> processes) {
      for (Entry<HashedItem, TileEntityFactory.RecipeProcessInfo> entry : processes.entrySet()) {
         TileEntityFactory.RecipeProcessInfo recipeProcessInfo = entry.getValue();
         int processCount = recipeProcessInfo.processes.size();
         if (processCount != 1) {
            HashedItem item = entry.getKey();
            int maxStackSize = item.getMaxStackSize();
            int numberPerSlot = recipeProcessInfo.totalCount / processCount;
            if (numberPerSlot != maxStackSize) {
               int remainder = recipeProcessInfo.totalCount % processCount;
               int minPerSlot = recipeProcessInfo.getMinPerSlot();
               if (minPerSlot > 1) {
                  int perSlotRemainder = numberPerSlot % minPerSlot;
                  if (perSlotRemainder > 0) {
                     numberPerSlot -= perSlotRemainder;
                     remainder += perSlotRemainder * processCount;
                  }

                  if (numberPerSlot + minPerSlot > maxStackSize) {
                     minPerSlot = maxStackSize - numberPerSlot;
                  }
               }

               for (int i = 0; i < processCount; i++) {
                  TileEntityFactory.ProcessInfo processInfo = recipeProcessInfo.processes.get(i);
                  FactoryInputInventorySlot inputSlot = processInfo.inputSlot();
                  int sizeForSlot = numberPerSlot;
                  if (remainder > 0) {
                     if (remainder > minPerSlot) {
                        sizeForSlot = numberPerSlot + minPerSlot;
                        remainder -= minPerSlot;
                     } else {
                        sizeForSlot = numberPerSlot + remainder;
                        remainder = 0;
                     }
                  }

                  if (inputSlot.isEmpty()) {
                     if (sizeForSlot > 0) {
                        inputSlot.setStackUnchecked(item.createStack(sizeForSlot));
                     }
                  } else if (sizeForSlot == 0) {
                     inputSlot.setEmpty();
                  } else if (inputSlot.getCount() != sizeForSlot) {
                     MekanismUtils.logMismatchedStackSize(sizeForSlot, inputSlot.setStackSize(sizeForSlot, Action.EXECUTE));
                  }
               }
            }
         }
      }
   }

   protected static class ErrorTracker {
      private final List<CachedRecipe.OperationTracker.RecipeError> errorTypes;
      private final IntSet globalTypes;
      private final boolean[][] trackedErrors;
      private final int processes;

      public ErrorTracker(
         List<CachedRecipe.OperationTracker.RecipeError> errorTypes, Set<CachedRecipe.OperationTracker.RecipeError> globalErrorTypes, int processes
      ) {
         this.errorTypes = List.copyOf(errorTypes);
         this.globalTypes = new IntArraySet(globalErrorTypes.size());

         for (int i = 0; i < this.errorTypes.size(); i++) {
            CachedRecipe.OperationTracker.RecipeError error = this.errorTypes.get(i);
            if (globalErrorTypes.contains(error)) {
               this.globalTypes.add(i);
            }
         }

         this.processes = processes;
         this.trackedErrors = new boolean[this.processes][];
         int errors = this.errorTypes.size();

         for (int ix = 0; ix < this.trackedErrors.length; ix++) {
            this.trackedErrors[ix] = new boolean[errors];
         }
      }

      private void track(MekanismContainer container) {
         container.trackArray(this.trackedErrors);
      }

      public void onErrorsChanged(Set<CachedRecipe.OperationTracker.RecipeError> errors, int processIndex) {
         boolean[] processTrackedErrors = this.trackedErrors[processIndex];

         for (int i = 0; i < processTrackedErrors.length; i++) {
            processTrackedErrors[i] = errors.contains(this.errorTypes.get(i));
         }
      }

      private BooleanSupplier getWarningCheck(CachedRecipe.OperationTracker.RecipeError error, int processIndex) {
         if (processIndex >= 0 && processIndex < this.processes) {
            int errorIndex = this.errorTypes.indexOf(error);
            if (errorIndex >= 0) {
               if (this.globalTypes.contains(errorIndex)) {
                  return () -> Arrays.stream(this.trackedErrors).anyMatch(processTrackedErrors -> processTrackedErrors[errorIndex]);
               }

               return () -> this.trackedErrors[processIndex][errorIndex];
            }
         }

         return () -> false;
      }
   }

   public record ProcessInfo(
      int process, @NotNull FactoryInputInventorySlot inputSlot, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
   }

   private static class RecipeProcessInfo {
      private final List<TileEntityFactory.ProcessInfo> processes = new ArrayList<>();
      @Nullable
      private IntSupplier lazyMinPerSlot;
      private int minPerSlot = 1;
      private int totalCount;

      public int getMinPerSlot() {
         if (this.lazyMinPerSlot != null) {
            this.minPerSlot = this.lazyMinPerSlot.getAsInt();
            this.lazyMinPerSlot = null;
         }

         return this.minPerSlot;
      }
   }
}
