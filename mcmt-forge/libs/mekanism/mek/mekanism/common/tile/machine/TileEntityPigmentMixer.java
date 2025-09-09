package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.ChemicalChemicalToChemicalCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
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
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.PigmentInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IEitherSideRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.ChemicalSlotInfo;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IBoundingBlock;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityPigmentMixer
   extends TileEntityRecipeMachine<PigmentMixingRecipe>
   implements IBoundingBlock,
   IEitherSideRecipeLookupHandler.EitherSideChemicalRecipeLookupHandler<Pigment, PigmentStack, PigmentMixingRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY_REDUCED_RATE,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getLeftInput", "getLeftInputCapacity", "getLeftInputNeeded", "getLeftInputFilledPercentage"},
      docPlaceholder = "left pigment tank"
   )
   public IPigmentTank leftInputTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getRightInput", "getRightInputCapacity", "getRightInputNeeded", "getRightInputFilledPercentage"},
      docPlaceholder = "right pigment tank"
   )
   public IPigmentTank rightInputTank;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output pigment tank"
   )
   public IPigmentTank outputTank;
   private FloatingLong clientEnergyUsed = FloatingLong.ZERO;
   private int baselineMaxOperations = 1;
   private final IOutputHandler<PigmentStack> outputHandler;
   private final IInputHandler<PigmentStack> leftInputHandler;
   private final IInputHandler<PigmentStack> rightInputHandler;
   private MachineEnergyContainer<TileEntityPigmentMixer> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getLeftInputItem"},
      docPlaceholder = "left input slot"
   )
   PigmentInventorySlot leftInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   PigmentInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getRightInputItem"},
      docPlaceholder = "right input slot"
   )
   PigmentInventorySlot rightInputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityPigmentMixer(BlockPos pos, BlockState state) {
      super(MekanismBlocks.PIGMENT_MIXER, pos, state, TRACKED_ERROR_TYPES);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.PIGMENT, TransmissionType.ENERGY);
      ConfigInfo itemConfig = this.configComponent.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.INPUT_1, new InventorySlotInfo(true, true, this.leftInputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_2, new InventorySlotInfo(true, true, this.rightInputSlot));
         itemConfig.addSlotInfo(DataType.OUTPUT, new InventorySlotInfo(true, true, this.outputSlot));
         itemConfig.addSlotInfo(DataType.INPUT_OUTPUT, new InventorySlotInfo(true, true, this.leftInputSlot, this.rightInputSlot, this.outputSlot));
         itemConfig.addSlotInfo(DataType.ENERGY, new InventorySlotInfo(true, true, this.energySlot));
         itemConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
         itemConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
         itemConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
         itemConfig.setDataType(DataType.ENERGY, RelativeSide.BACK);
      }

      ConfigInfo pigmentConfig = this.configComponent.getConfig(TransmissionType.PIGMENT);
      if (pigmentConfig != null) {
         pigmentConfig.addSlotInfo(DataType.INPUT_1, new ChemicalSlotInfo.PigmentSlotInfo(true, false, this.leftInputTank));
         pigmentConfig.addSlotInfo(DataType.INPUT_2, new ChemicalSlotInfo.PigmentSlotInfo(true, false, this.rightInputTank));
         pigmentConfig.addSlotInfo(DataType.OUTPUT, new ChemicalSlotInfo.PigmentSlotInfo(false, true, this.outputTank));
         pigmentConfig.addSlotInfo(
            DataType.INPUT_OUTPUT, new ChemicalSlotInfo.PigmentSlotInfo(true, true, this.leftInputTank, this.rightInputTank, this.outputTank)
         );
         pigmentConfig.setDataType(DataType.INPUT_1, RelativeSide.LEFT);
         pigmentConfig.setDataType(DataType.INPUT_2, RelativeSide.RIGHT);
         pigmentConfig.setDataType(DataType.OUTPUT, RelativeSide.FRONT);
         pigmentConfig.setEjecting(true);
      }

      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent
         .setOutputData(this.configComponent, TransmissionType.ITEM, TransmissionType.PIGMENT)
         .setCanTankEject(tank -> tank == this.outputTank);
      this.leftInputHandler = InputHelper.getInputHandler(this.leftInputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_LEFT_INPUT);
      this.rightInputHandler = InputHelper.getInputHandler(this.rightInputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_RIGHT_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<Pigment, PigmentStack, IPigmentTank> getInitialPigmentTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      ChemicalTankHelper<Pigment, PigmentStack, IPigmentTank> builder = ChemicalTankHelper.forSidePigmentWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.leftInputTank = (IPigmentTank)ChemicalTankBuilder.PIGMENT
            .input(1000L, pigment -> this.containsRecipe(pigment, this.rightInputTank.getStack()), this::containsRecipe, recipeCacheListener)
      );
      builder.addTank(
         this.rightInputTank = (IPigmentTank)ChemicalTankBuilder.PIGMENT
            .input(1000L, pigment -> this.containsRecipe(pigment, this.leftInputTank.getStack()), this::containsRecipe, recipeCacheListener)
      );
      builder.addTank(this.outputTank = (IPigmentTank)ChemicalTankBuilder.PIGMENT.output(2000L, listener));
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
      builder.addSlot(this.leftInputSlot = PigmentInventorySlot.fill(this.leftInputTank, listener, 6, 56));
      builder.addSlot(this.rightInputSlot = PigmentInventorySlot.fill(this.rightInputTank, listener, 154, 56));
      builder.addSlot(this.outputSlot = PigmentInventorySlot.drain(this.outputTank, listener, 80, 65));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 154, 14));
      this.leftInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.leftInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.rightInputSlot.setSlotType(ContainerSlotType.INPUT);
      this.rightInputSlot.setSlotOverlay(SlotOverlay.MINUS);
      this.outputSlot.setSlotType(ContainerSlotType.OUTPUT);
      this.outputSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.leftInputSlot.fillTank();
      this.rightInputSlot.fillTank();
      this.outputSlot.drainTank();
      this.clientEnergyUsed = this.recipeCacheLookupMonitor.updateAndProcess(this.energyContainer);
   }

   @NotNull
   @ComputerMethod(
      nameOverride = "getEnergyUsage",
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   public FloatingLong getEnergyUsed() {
      return this.clientEnergyUsed;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<PigmentMixingRecipe, InputRecipeCache.EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> getRecipeType() {
      return MekanismRecipeType.PIGMENT_MIXING;
   }

   @Nullable
   public PigmentMixingRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.leftInputHandler, this.rightInputHandler);
   }

   @NotNull
   public CachedRecipe<PigmentMixingRecipe> createNewCachedRecipe(@NotNull PigmentMixingRecipe recipe, int cacheIndex) {
      return new ChemicalChemicalToChemicalCachedRecipe(recipe, this.recheckAllRecipeErrors, this.leftInputHandler, this.rightInputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setBaselineMaxOperations(() -> this.baselineMaxOperations)
         .setOnFinish(this::markForSave);
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.baselineMaxOperations = (int)Math.pow(2.0, this.upgradeComponent.getUpgrades(Upgrade.SPEED));
      }
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      return new AABB(this.f_58858_.m_7494_(), this.f_58858_.m_7918_(1, 2, 1));
   }

   public MachineEnergyContainer<TileEntityPigmentMixer> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableFloatingLong.create(this::getEnergyUsed, value -> this.clientEnergyUsed = value));
   }
}
