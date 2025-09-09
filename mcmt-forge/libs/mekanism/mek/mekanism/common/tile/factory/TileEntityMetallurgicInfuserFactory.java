package mekanism.common.tile.factory;

import java.util.List;
import java.util.Set;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.TwoInputCachedRecipe;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.chemical.ChemicalTankHelper;
import mekanism.common.capabilities.holder.chemical.IChemicalTankHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.chemical.InfusionInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.IDoubleRecipeLookupHandler;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.upgrade.MetallurgicInfuserUpgradeData;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityMetallurgicInfuserFactory
   extends TileEntityItemToItemFactory<MetallurgicInfuserRecipe>
   implements IHasDumpButton,
   IDoubleRecipeLookupHandler.ItemChemicalRecipeLookupHandler<InfuseType, InfusionStack, MetallurgicInfuserRecipe> {
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
   private final IInputHandler<InfusionStack> infusionInputHandler;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInfuseTypeItem"},
      docPlaceholder = "infusion extra input slot"
   )
   InfusionInventorySlot extraSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerChemicalTankWrapper.class,
      methodNames = {"getInfuseType", "getInfuseTypeCapacity", "getInfuseTypeNeeded", "getInfuseTypeFilledPercentage"},
      docPlaceholder = "infusion buffer"
   )
   IInfusionTank infusionTank;

   public TileEntityMetallurgicInfuserFactory(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state, TRACKED_ERROR_TYPES, GLOBAL_ERROR_TYPES);
      this.infusionInputHandler = InputHelper.getInputHandler(this.infusionTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_SECONDARY_INPUT);
      this.configComponent.addSupported(TransmissionType.INFUSION);
      this.configComponent.setupIOConfig(TransmissionType.INFUSION, this.infusionTank, RelativeSide.RIGHT).setCanEject(false);
   }

   @NotNull
   @Override
   public IChemicalTankHolder<InfuseType, InfusionStack, IInfusionTank> getInitialInfusionTanks(IContentsListener listener) {
      ChemicalTankHelper<InfuseType, InfusionStack, IInfusionTank> builder = ChemicalTankHelper.forSideInfusionWithConfig(this::getDirection, this::getConfig);
      builder.addTank(
         this.infusionTank = (IInfusionTank)ChemicalTankBuilder.INFUSION
            .create(1000L * this.tier.processes, this::containsRecipeB, this.markAllMonitorsChanged(listener))
      );
      return builder.build();
   }

   @Override
   protected void addSlots(InventorySlotHelper builder, IContentsListener listener, IContentsListener updateSortingListener) {
      super.addSlots(builder, listener, updateSortingListener);
      builder.addSlot(this.extraSlot = InfusionInventorySlot.fillOrConvert(this.infusionTank, this::m_58904_, listener, 7, 57));
   }

   public IInfusionTank getInfusionTank() {
      return this.infusionTank;
   }

   @Nullable
   protected InfusionInventorySlot getExtraSlot() {
      return this.extraSlot;
   }

   @Override
   public boolean isValidInputItem(@NotNull ItemStack stack) {
      return this.containsRecipeA(stack);
   }

   protected int getNeededInput(MetallurgicInfuserRecipe recipe, ItemStack inputStack) {
      return MathUtils.clampToInt(recipe.getItemInput().getNeededAmount(inputStack));
   }

   @Override
   protected boolean isCachedRecipeValid(@Nullable CachedRecipe<MetallurgicInfuserRecipe> cached, @NotNull ItemStack stack) {
      if (cached == null) {
         return false;
      } else {
         MetallurgicInfuserRecipe cachedRecipe = cached.getRecipe();
         return cachedRecipe.getItemInput().testType(stack)
            && (this.infusionTank.isEmpty() || cachedRecipe.getChemicalInput().testType(this.infusionTank.getType()));
      }
   }

   protected MetallurgicInfuserRecipe findRecipe(
      int process, @NotNull ItemStack fallbackInput, @NotNull IInventorySlot outputSlot, @Nullable IInventorySlot secondaryOutputSlot
   ) {
      InfusionStack stored = this.infusionTank.getStack();
      ItemStack output = outputSlot.getStack();
      return this.getRecipeType()
         .getInputCache()
         .findTypeBasedRecipe(this.f_58857_, fallbackInput, stored, recipe -> InventoryUtils.areItemsStackable(recipe.getOutput(fallbackInput, stored), output));
   }

   @Override
   protected void handleSecondaryFuel() {
      this.extraSlot.fillTankOrConvert();
   }

   @Override
   public boolean hasSecondaryResourceBar() {
      return true;
   }

   @NotNull
   @Override
   public IMekanismRecipeTypeProvider<MetallurgicInfuserRecipe, InputRecipeCache.ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> getRecipeType() {
      return MekanismRecipeType.METALLURGIC_INFUSING;
   }

   @Nullable
   public MetallurgicInfuserRecipe getRecipe(int cacheIndex) {
      return this.findFirstRecipe(this.inputHandlers[cacheIndex], this.infusionInputHandler);
   }

   @NotNull
   public CachedRecipe<MetallurgicInfuserRecipe> createNewCachedRecipe(@NotNull MetallurgicInfuserRecipe recipe, int cacheIndex) {
      return TwoInputCachedRecipe.itemChemicalToItem(
            recipe, this.recheckAllRecipeErrors[cacheIndex], this.inputHandlers[cacheIndex], this.infusionInputHandler, this.outputHandlers[cacheIndex]
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
   public void parseUpgradeData(@NotNull IUpgradeData upgradeData) {
      if (upgradeData instanceof MetallurgicInfuserUpgradeData data) {
         super.parseUpgradeData(upgradeData);
         this.infusionTank.deserializeNBT(data.stored.serializeNBT());
         this.extraSlot.deserializeNBT(data.infusionSlot.serializeNBT());
      } else {
         Mekanism.logger.warn("Unhandled upgrade data.", new Throwable());
      }
   }

   @NotNull
   public MetallurgicInfuserUpgradeData getUpgradeData() {
      return new MetallurgicInfuserUpgradeData(
         this.redstone,
         this.getControlType(),
         this.getEnergyContainer(),
         this.progress,
         this.infusionTank,
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
      this.infusionTank.setEmpty();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Empty the contents of the infusion buffer into the environment"
   )
   void dumpInfuseType() throws ComputerException {
      this.validateSecurityIsPublic();
      this.dump();
   }
}
