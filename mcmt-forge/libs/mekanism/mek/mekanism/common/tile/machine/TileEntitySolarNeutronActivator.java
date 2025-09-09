package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.chemical.GasInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.ISingleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntitySolarNeutronActivator
   extends TileEntityRecipeMachine<GasToGasRecipe>
   implements IBoundingBlock,
   ISingleRecipeLookupHandler.ChemicalRecipeLookupHandler<Gas, GasStack, GasToGasRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final long MAX_GAS = 10000L;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInput", "getInputCapacity", "getInputNeeded", "getInputFilledPercentage"},
      docPlaceholder = "input tank"
   )
   public IGasTank inputTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   public IGasTank outputTank;
   @SyntheticComputerMethod(
      getter = "getPeakProductionRate"
   )
   private float peakProductionRate;
   @SyntheticComputerMethod(
      getter = "getProductionRate"
   )
   private float productionRate;
   private boolean settingsChecked;
   private boolean needsRainCheck;
   private final IOutputHandler<GasStack> outputHandler;
   private final IInputHandler<GasStack> inputHandler;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   GasInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   GasInventorySlot outputSlot;

   public TileEntitySolarNeutronActivator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.GAS);
      this.configComponent.setupIOConfig(TransmissionType.ITEM, this.inputSlot, this.outputSlot, RelativeSide.FRONT);
      this.configComponent.setupIOConfig(TransmissionType.GAS, this.inputTank, this.outputTank, RelativeSide.FRONT, false, true).setEjecting(true);
      this.configComponent.addDisabledSides(RelativeSide.TOP);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.GAS).setCanTankEject(tank -> tank != this.inputTank);
      this.inputHandler = InputHelper.getInputHandler(this.inputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Gas, GasStack, IGasTank> getInitialGasTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Gas, GasStack, IGasTank> builder = ChemicalTankHelper.forSideGasWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.inputTank = (IGasTank)ChemicalTankBuilder.GAS
            .create(
               10000L,
               ChemicalTankHelper.radioactiveInputTankPredicate(() -> this.outputTank),
               ChemicalTankBuilder.GAS.alwaysTrueBi,
               this::containsRecipe,
               ChemicalAttributeValidator.ALWAYS_ALLOW,
               recipeCacheListener
            )
      );
      builder.addTank(this.outputTank = (IGasTank)ChemicalTankBuilder.GAS.output(10000L, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener, IContentsListener recipeCacheListener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.inputSlot = GasInventorySlot.fill(this.inputTank, listener, 5, 56));
      builder.addSlot(this.outputSlot = GasInventorySlot.drain(this.outputTank, listener, 155, 56));
      this.inputSlot.setSlotType(ContainerSlotType.INPUT);
      this.inputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.outputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   private void recheckSettings() {
      Level world = this.m_58904_();
      if (world != null) {
         BlockPos pos = this.m_58899_();
         Biome b = (Biome)world.m_7062_().m_204214_(pos).m_203334_();
         this.needsRainCheck = b.m_264600_(pos) != Precipitation.NONE;
         float tempEff = 0.3F * (0.8F - b.m_47505_(pos));
         float humidityEff = this.needsRainCheck ? -0.3F * b.getModifiedClimateSettings().f_47683_() : 0.0F;
         this.peakProductionRate = MekanismConfig.general.maxSolarNeutronActivatorRate.get() * (1.0F + tempEff + humidityEff);
         this.settingsChecked = true;
      }
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (!this.settingsChecked) {
         this.recheckSettings();
      }

      this.inputSlot.fillTank();
      this.outputSlot.drainTank();
      this.productionRate = this.recalculateProductionRate();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<GasToGasRecipe, InputRecipeCache.SingleChemical<Gas, GasStack, GasToGasRecipe>> getRecipeType() {
      return MekanismRecipeType.ACTIVATING;
   }

   @Nullable
   public GasToGasRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandler);
   }

   @ComputerMethod
   boolean canSeeSun() {
      return WorldUtils.canSeeSun(this.f_58857_, this.f_58858_.m_7494_());
   }

   private boolean canFunction() {
      return MekanismUtils.canFunction(this) && this.canSeeSun();
   }

   private float recalculateProductionRate() {
      Level world = this.m_58904_();
      if (world != null && this.canFunction()) {
         float brightness = WorldUtils.getSunBrightness(world, 1.0F);
         float production = this.peakProductionRate * brightness;
         if (this.needsRainCheck && (world.m_46471_() || world.m_46470_())) {
            production *= 0.2F;
         }

         return production;
      } else {
         return 0.0F;
      }
   }

   @NotNull
   public CachedRecipe<GasToGasRecipe> createNewCachedRecipe(@NotNull GasToGasRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.chemicalToChemical(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(this::canFunction)
         .setActive(this::setActive)
         .setOnFinish(this::markForSave)
         .setRequiredTicks(() -> this.productionRate > 0.0F && this.productionRate < 1.0F ? (int)Math.ceil(1.0F / this.productionRate) : 1)
         .setBaselineMaxOperations(() -> this.productionRate > 0.0F && this.productionRate < 1.0F ? 1 : (int)this.productionRate);
   }

   @Override
   public int getRedstoneLevel() {
      return MekanismUtils.redstoneLevelFromContents(this.inputTank.getStored(), this.inputTank.getCapacity());
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return type == SubstanceType.GAS;
   }
}
