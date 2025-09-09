package mekanism.common.tile.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityItemStackGasToItemStackFactory
   extends TileEntityItemToItemFactory<ItemStackGasToItemStackRecipe>
   implements IHasDumpButton,
   IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<Gas, GasStack, ItemStackGasToItemStackRecipe>,
   IRecipeLookupHandler.ConstantUsageRecipeLookupHandler {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final Set<CachedRecipe.OperationTracker.RecipeError> GLOBAL_ERROR_TYPES = Set.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT
   );
   private final ILongInputHandler<GasStack> gasInputHandler;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getChemicalItem"},
      docPlaceholder = "chemical item (extra) slot"
   )
   GasInventorySlot extraSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getChemical", "getChemicalCapacity", "getChemicalNeeded", "getChemicalFilledPercentage"},
      docPlaceholder = "gas tank"
   )
   IGasTank gasTank;
   private final ItemStackConstantChemicalToItemStackCachedRecipe.ChemicalUsageMultiplier gasUsageMultiplier;
   private final long[] usedSoFar;
   private double gasPerTickMeanMultiplier = 1.0;
   private long baseTotalUsage;

   public TileEntityItemStackGasToItemStackFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
      this.gasInputHandler = InputHelper.getConstantInputHandler(this.gasTank);
      this.configComponent.addSupported(TransmissionType.GAS);
      if (this.allowExtractingChemical()) {
         this.configComponent.setupIOConfig(TransmissionType.GAS, this.gasTank, RelativeSide.RIGHT).setCanEject(false);
      } else {
         this.configComponent.setupInputConfig(TransmissionType.GAS, this.gasTank);
      }

      this.baseTotalUsage = 200L;
      this.usedSoFar = new long[this.tier.processes];
      if (this.useStatisticalMechanics()) {
         this.gasUsageMultiplier = (usedSoFar, operatingTicks) -> StatUtils.inversePoisson(this.gasPerTickMeanMultiplier);
      } else {
         this.gasUsageMultiplier = (usedSoFar, operatingTicks) -> {
            long baseRemaining = this.baseTotalUsage - usedSoFar;
            int remainingTicks = this.getTicksRequired() - operatingTicks;
            if (baseRemaining < remainingTicks) {
               return 0L;
            } else {
               return baseRemaining == remainingTicks ? 1L : Math.max(MathUtils.clampToLong((double)baseRemaining / remainingTicks), 0L);
            }
         };
      }
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      if (this.allowExtractingChemical()) {
         this.gasTank = (IGasTank)ChemicalTankBuilder.GAS.create(210L * this.tier.processes, this::containsRecipeB, this.markAllMonitorsChanged(listener));
      } else {
         this.gasTank = (IGasTank)ChemicalTankBuilder.GAS.input(210L * this.tier.processes, this::containsRecipeB, this.markAllMonitorsChanged(listener));
      }

      builder.addTank(this.gasTank);
      return builder.build();
   }

   @Override
   protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
      super.addSlots(builder, listener, updateSortingListener);
      builder.addSlot(this.extraSlot = GasInventorySlot.fillOrConvert(this.gasTank, this::m_58904_, listener, 7, 57));
   }

   public IGasTank getGasTank() {
      return this.gasTank;
   }

   @Nullable
   protected GasInventorySlot getExtraSlot() {
      return this.extraSlot;
   }

   @Override
   public boolean isValidInputItem(@NotNull ItemStack stack) {
      return this.containsRecipeA(stack);
   }

   protected int getNeededInput(ItemStackGasToItemStackRecipe recipe, ItemStack inputStack) {
      return MathUtils.clampToInt(recipe.getItemInput().getNeededAmount(inputStack));
   }

   @Override
   protected boolean isCachedRecipeValid(@Nullable CachedRecipe<ItemStackGasToItemStackRecipe> cached, @NotNull ItemStack stack) {
      if (cached == null) {
         return false;
      } else {
         ItemStackGasToItemStackRecipe cachedRecipe = cached.getRecipe();
         return cachedRecipe.getItemInput().testType(stack) && (this.gasTank.isEmpty() || cachedRecipe.getChemicalInput().testType(this.gasTank.getType()));
      }
   }

   protected ItemStackGasToItemStackRecipe findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
      GasStack stored = this.gasTank.getStack();
      ItemStack output = outputSlot.getStack();
      return this.getRecipeType()
         .getInputCache()
         .findTypeBasedRecipe(this.f_58857_, fallbackInput, stored, recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, stored), output));
   }

   @Override
   protected void handleSecondaryFuel() {
      this.extraSlot.fillTankOrConvert();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> getRecipeType() {
      return switch (this.type) {
         case INJECTING -> MekanismRecipeType.INJECTING;
         case PURIFYING -> MekanismRecipeType.PURIFYING;
         default -> MekanismRecipeType.COMPRESSING;
      };
   }

   private boolean allowExtractingChemical() {
      return Attribute.get(this.blockProvider, AttributeFactoryType.class).getFactoryType() == FactoryType.COMPRESSING;
   }

   private boolean useStatisticalMechanics() {
      return this.type == FactoryType.INJECTING || this.type == FactoryType.PURIFYING;
   }

   @Nullable
   public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandlers[cacheIndex], this.gasInputHandler);
   }

   @NotNull
   public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
      return new ItemStackConstantChemicalToItemStackCachedRecipe(
            recipe,
            this.recheckAllRecipeErrors[cacheIndex],
            this.inputHandlers[cacheIndex],
            this.gasInputHandler,
            this.gasUsageMultiplier,
            used -> this.usedSoFar[cacheIndex] = used,
            this.outputHandlers[cacheIndex]
         )
         .setErrorsChanged(errors -> this.errorTracker.onErrorsChanged(errors, cacheIndex))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(active -> this.setActiveState(active, cacheIndex))
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(operatingTicks -> this.progress[cacheIndex] = operatingTicks);
   }

   @Override
   public boolean hasSecondaryResourceBar() {
      return true;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      if (nbt.m_128425_("usedSoFar", 12)) {
         long[] savedUsed = nbt.m_128467_("usedSoFar");
         if (this.tier.processes != savedUsed.length) {
            Arrays.fill(this.usedSoFar, 0L);
         }

         for (int i = 0; i < this.tier.processes && i < savedUsed.length; i++) {
            this.usedSoFar[i] = savedUsed[i];
         }
      } else {
         Arrays.fill(this.usedSoFar, 0L);
      }
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128365_("usedSoFar", new LongArrayTag(Arrays.copyOf(this.usedSoFar, this.usedSoFar.length)));
   }

   @Override
   public long getSavedUsedSoFar(int cacheIndex) {
      return this.usedSoFar[cacheIndex];
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED || upgrade == Upgrade.GAS && this.supportsUpgrade(Upgrade.GAS)) {
         if (this.useStatisticalMechanics()) {
            this.gasPerTickMeanMultiplier = MekanismUtils.getGasPerTickMeanMultiplier(this);
         } else {
            this.baseTotalUsage = MekanismUtils.getBaseUsage(this, 200);
         }
      }
   }

   @Override
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof AdvancedMachineUpgradeData data) {
         super.parseUpgradeData(upgradeData);
         this.gasTank.deserializeNBT(data.stored.serializeNBT());
         this.extraSlot.deserializeNBT(data.gasSlot.serializeNBT());
         System.arraycopy(data.usedSoFar, 0, this.usedSoFar, 0, data.usedSoFar.length);
      } else {
         Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
      }
   }

   @NotNull
   public AdvancedMachineUpgradeData getUpgradeData() {
      return new AdvancedMachineUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.progress,
         this.usedSoFar,
         this.gasTank,
         this.extraSlot,
         this.energySlot,
         this.inputSlots,
         this.outputSlots,
         this.isSorting(),
         this.getComponents()
      );
   }

   @Override
   public void dump() {
      this.gasTank.setEmpty();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Empty the contents of the gas tank into the environment"
   )
   void dumpChemical() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dump();
   }
}
