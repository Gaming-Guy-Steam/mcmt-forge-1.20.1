package mekanism.common.tile.prefab;

import java.util.List;
import java.util.function.BiPredicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ItemStackConstantChemicalToItemStackCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.ILongInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.upgrade.AdvancedMachineUpgradeData;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StatUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TileEntityAdvancedElectricMachine
   extends TileEntityProgressMachine<ItemStackGasToItemStackRecipe>
   implements IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<Gas, GasStack, ItemStackGasToItemStackRecipe>,
   IRecipeLookupHandler.ConstantUsageRecipeLookupHandler {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final int BASE_TICKS_REQUIRED = 200;
   public static final long MAX_GAS = 210L;
   private final ItemStackConstantChemicalToItemStackCachedRecipe.ChemicalUsageMultiplier gasUsageMultiplier;
   private double gasPerTickMeanMultiplier = 1.0;
   private long baseTotalUsage;
   private long usedSoFar;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getChemical", "getChemicalCapacity", "getChemicalNeeded", "getChemicalFilledPercentage"},
      docPlaceholder = "gas tank"
   )
   public IGasTank gasTank;
   protected final IOutputHandler<ItemStack> outputHandler;
   protected final IInputHandler<ItemStack> itemInputHandler;
   protected final ILongInputHandler<GasStack> gasInputHandler;
   private MachineEnergyContainer<TileEntityAdvancedElectricMachine> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInput"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutput"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getChemicalItem"},
      docPlaceholder = "secondary input slot"
   )
   GasInventorySlot secondarySlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityAdvancedElectricMachine(IBlockProvider blockProvider, BlockPos pos, BlockState state, int ticksRequired) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, ticksRequired);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS, TransmissionType.ENERGY);
      this.configComponent.setupItemIOExtraConfig(this.inputSlot, this.outputSlot, this.secondarySlot, this.energySlot);
      if (this.allowExtractingChemical()) {
         this.configComponent.setupIOConfig(TransmissionType.GAS, this.gasTank, RelativeSide.RIGHT).setCanEject(false);
      } else {
         this.configComponent.setupInputConfig(TransmissionType.GAS, this.gasTank);
      }

      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.itemInputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.gasInputHandler = InputHelper.getConstantInputHandler(this.gasTank);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
      this.baseTotalUsage = this.baseTicksRequired;
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
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      BiPredicate<Gas, AutomationType> canExtract = this.allowExtractingChemical() ? ChemicalTankBuilder.GAS.alwaysTrueBi : ChemicalTankBuilder.GAS.notExternal;
      builder.addTank(
         this.gasTank = (IGasTank)ChemicalTankBuilder.GAS
            .create(
               210L, canExtract, (gas, automationType) -> this.containsRecipeBA(this.inputSlot.getStack(), gas), this::containsRecipeB, recipeCacheListener
            )
      );
      return builder.build();
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener, IContentsListener recipeCacheListener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addContainer(this.energyContainer = MachineEnergyContainer.input(this, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(
            this.inputSlot = InputInventorySlot.at(
               item -> this.containsRecipeAB(item, this.gasTank.getStack()), this::containsRecipeA, recipeCacheListener, 64, 17
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.secondarySlot = GasInventorySlot.fillOrConvert(this.gasTank, this::m_58904_, listener, 64, 53));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 116, 35))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 39, 35));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.secondarySlot.fillTankOrConvert();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   protected boolean allowExtractingChemical() {
      return !this.useStatisticalMechanics();
   }

   protected boolean useStatisticalMechanics() {
      return false;
   }

   @Nullable
   public ItemStackGasToItemStackRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.itemInputHandler, this.gasInputHandler);
   }

   @NotNull
   public CachedRecipe<ItemStackGasToItemStackRecipe> createNewCachedRecipe(@NotNull ItemStackGasToItemStackRecipe recipe, int cacheIndex) {
      return new ItemStackConstantChemicalToItemStackCachedRecipe(
            recipe,
            this.recheckAllRecipeErrors,
            this.itemInputHandler,
            this.gasInputHandler,
            this.gasUsageMultiplier,
            used -> this.usedSoFar = used,
            this.outputHandler
         )
         .setErrorsChanged(this::onErrorsChanged)
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(this::setOperatingTicks);
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED || upgrade == Upgrade.GAS && this.supportsUpgrade(Upgrade.GAS)) {
         if (this.useStatisticalMechanics()) {
            this.gasPerTickMeanMultiplier = MekanismUtils.getGasPerTickMeanMultiplier(this);
         } else {
            this.baseTotalUsage = MekanismUtils.getBaseUsage(this, this.baseTicksRequired);
         }
      }
   }

   @NotNull
   public AdvancedMachineUpgradeData getUpgradeData() {
      return new AdvancedMachineUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.getOperatingTicks(),
         this.usedSoFar,
         this.gasTank,
         this.secondarySlot,
         this.energySlot,
         this.inputSlot,
         this.outputSlot,
         this.getComponents()
      );
   }

   public MachineEnergyContainer<TileEntityAdvancedElectricMachine> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public boolean isConfigurationDataCompatible(BlockEntityType<?> tileType) {
      return super.isConfigurationDataCompatible(tileType) || MekanismUtils.isSameTypeFactory(this.getBlockType(), tileType);
   }

   @Override
   public long getSavedUsedSoFar(int cacheIndex) {
      return this.usedSoFar;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.usedSoFar = nbt.m_128454_("usedSoFar");
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128356_("usedSoFar", this.usedSoFar);
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Empty the contents of the gas tank into the environment"
   )
   void dumpChemical() throws ComputerException {
      this.validateSecurityIsPublic();
      this.gasTank.setEmpty();
   }
}
