package mekanism.common.tile.machine;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
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
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityMetallurgicInfuser
   extends TileEntityProgressMachine<MetallurgicInfuserRecipe>
   implements IHasDumpButton,
   IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<InfuseType, InfusionStack, MetallurgicInfuserRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   public static final long MAX_INFUSE = 1000L;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInfuseType", "getInfuseTypeCapacity", "getInfuseTypeNeeded", "getInfuseTypeFilledPercentage"},
      docPlaceholder = "infusion buffer"
   )
   public IInfusionTank infusionTank;
   private final IOutputHandler<ItemStack> outputHandler;
   private final IInputHandler<InfusionStack> infusionInputHandler;
   private final IInputHandler<ItemStack> itemInputHandler;
   private MachineEnergyContainer<TileEntityMetallurgicInfuser> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInfuseTypeItem"},
      docPlaceholder = "infusion (extra) input slot"
   )
   InfusionInventorySlot infusionSlot;
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
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityMetallurgicInfuser(BlockPos pos, BlockState state) {
      super(MekanismBlocks.METALLURGIC_INFUSER, pos, state, TRACKED_ERROR_TYPES, 200);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY, TransmissionType.INFUSION);
      this.configComponent.setupItemIOExtraConfig(this.inputSlot, this.outputSlot, this.infusionSlot, this.energySlot);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.configComponent.setupIOConfig(TransmissionType.INFUSION, this.infusionTank, RelativeSide.RIGHT).setCanEject(false);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
      this.infusionInputHandler = InputHelper.getInputHandler(this.infusionTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.itemInputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.outputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(
      IContentsListener listener, IContentsListener recipeCacheListener
   ) {
      ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.infusionTank = (IInfusionTank)ChemicalTankBuilder.INFUSION
            .create(
               1000L,
               ChemicalTankBuilder.INFUSION.alwaysTrueBi,
               (infuseType, automationType) -> this.containsRecipeBA(this.inputSlot.getStack(), infuseType),
               this::containsRecipeB,
               recipeCacheListener
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
      builder.addSlot(this.infusionSlot = InfusionInventorySlot.fillOrConvert(this.infusionTank, this::m_58904_, listener, 17, 35));
      builder.addSlot(
            this.inputSlot = InputInventorySlot.at(
               item -> this.containsRecipeAB(item, this.infusionTank.getStack()), this::containsRecipeA, recipeCacheListener, 51, 43
            )
         )
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 109, 43))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_SPACE_IN_OUTPUT, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE)
            )
         );
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.infusionSlot.fillTankOrConvert();
      this.recipeCacheLookupMonitor.updateAndProcess();
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<MetallurgicInfuserRecipe, InputRecipeCache.ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> getRecipeType() {
      return MekanismRecipeType.METALLURGIC_INFUSING;
   }

   @Nullable
   public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.itemInputHandler, this.infusionInputHandler);
   }

   @NotNull
   public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@NotNull MetallurgicInfuserRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.itemChemicalToItem(recipe, this.recheckAllRecipeErrors, this.itemInputHandler, this.infusionInputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   @NotNull
   public MetallurgicInfuserUpgradeData getUpgradeData() {
      return new MetallurgicInfuserUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.getOperatingTicks(),
         this.infusionTank,
         this.infusionSlot,
         this.energySlot,
         this.inputSlot,
         this.outputSlot,
         this.getComponents()
      );
   }

   public MachineEnergyContainer<TileEntityMetallurgicInfuser> getEnergyContainer() {
      return this.energyContainer;
   }

   @Override
   public boolean isConfigurationDataCompatible(BlockEntityType<?> tileType) {
      return super.isConfigurationDataCompatible(tileType) || MekanismUtils.isSameTypeFactory(this.getBlockType(), tileType);
   }

   @Override
   public void dump() {
      this.infusionTank.setEmpty();
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   void dumpInfuseType() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dump();
   }
}
