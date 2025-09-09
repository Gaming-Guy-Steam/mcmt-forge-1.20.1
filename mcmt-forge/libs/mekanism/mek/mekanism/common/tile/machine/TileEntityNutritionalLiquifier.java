package mekanism.common.tile.machine;

import java.util.Collections;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.OneInputCachedRecipe;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.inputs.InputHelper;
import mekanism.api.recipes.outputs.IOutputHandler;
import mekanism.api.recipes.outputs.OutputHelper;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.inventory.warning.WarningTracker;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityNutritionalLiquifier extends TileEntityProgressMachine<ItemStackToFluidRecipe> {
   private static final List<CachedRecipe.OperationTracker.RecipeError> TRACKED_ERROR_TYPES = List.of(
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_ENERGY,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT,
      CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE,
      CachedRecipe.OperationTracker.RecipeError.INPUT_DOESNT_PRODUCE_OUTPUT
   );
   private static final int MAX_FLUID = 10000;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerFluidTankWrapper.class,
      methodNames = {"getOutput", "getOutputCapacity", "getOutputNeeded", "getOutputFilledPercentage"},
      docPlaceholder = "output tank"
   )
   public IExtendedFluidTank fluidTank;
   private final IOutputHandler<FluidStack> outputHandler;
   private final IInputHandler<ItemStack> inputHandler;
   private MachineEnergyContainer<TileEntityNutritionalLiquifier> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInput"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getContainerFillItem"},
      docPlaceholder = "fillable container slot"
   )
   FluidInventorySlot containerFillSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "filled container output slot"
   )
   OutputInventorySlot outputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;
   @Nullable
   private HashedItem lastPasteItem;
   private float lastPasteScale;

   public TileEntityNutritionalLiquifier(BlockPos pos, BlockState state) {
      super(MekanismBlocks.NUTRITIONAL_LIQUIFIER, pos, state, TRACKED_ERROR_TYPES, 100);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.FLUID, TransmissionType.ENERGY);
      this.configComponent
         .setupItemIOConfig(List.of(this.inputSlot, this.containerFillSlot), Collections.singletonList(this.outputSlot), this.energySlot, false);
      this.configComponent.setupOutputConfig(TransmissionType.FLUID, this.fluidTank, RelativeSide.RIGHT);
      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.FLUID);
      this.inputHandler = InputHelper.getInputHandler(this.inputSlot, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT);
      this.outputHandler = OutputHelper.getOutputHandler(this.fluidTank, CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_OUTPUT_SPACE);
   }

   @NotNull
   @Override
   public IFluidTankHolder getInitialFluidTanks(IContentsListener listener, IContentsListener recipeCacheListener) {
      FluidTankHelper builder = FluidTankHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addTank(this.fluidTank = BasicFluidTank.output(10000, listener));
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
      builder.addSlot(this.inputSlot = InputInventorySlot.at(stack -> {
            if (!stack.m_41720_().m_41472_()) {
               return false;
            } else {
               FoodProperties food = stack.getFoodProperties(null);
               return food != null && food.m_38744_() > 0;
            }
         }, recipeCacheListener, 26, 36))
         .tracksWarnings(
            slot -> slot.warning(
               WarningTracker.WarningType.NO_MATCHING_RECIPE, this.getWarningCheck(CachedRecipe.OperationTracker.RecipeError.NOT_ENOUGH_INPUT)
            )
         );
      builder.addSlot(this.containerFillSlot = FluidInventorySlot.drain(this.fluidTank, listener, 155, 25));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 155, 56));
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 155, 5));
      this.containerFillSlot.setSlotOverlay(SlotOverlay.PLUS);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      this.containerFillSlot.drainTank(this.outputSlot);
      this.recipeCacheLookupMonitor.updateAndProcess();
      boolean needsPacket = false;
      float pasteScale = MekanismUtils.getScale(this.lastPasteScale, this.fluidTank);
      if (pasteScale != this.lastPasteScale) {
         this.lastPasteScale = pasteScale;
         needsPacket = true;
      }

      if (this.inputSlot.isEmpty()) {
         if (this.lastPasteItem != null) {
            this.lastPasteItem = null;
            needsPacket = true;
         }
      } else {
         HashedItem item = HashedItem.raw(this.inputSlot.getStack());
         if (!item.equals(this.lastPasteItem)) {
            this.lastPasteItem = item.recreate();
            needsPacket = true;
         }
      }

      if (needsPacket) {
         this.sendUpdatePacket();
      }
   }

   @NotNull
   public MekanismRecipeType<ItemStackToFluidRecipe, IInputRecipeCache> getRecipeType() {
      return null;
   }

   @Nullable
   public ItemStackToFluidRecipe getRecipe(int cacheIndex) {
      ItemStack stack = this.inputHandler.getInput();
      if (!stack.m_41619_() && stack.m_41720_().m_41472_()) {
         FoodProperties food = stack.getFoodProperties(null);
         return food != null && food.m_38744_() != 0
            ? new NutritionalLiquifierIRecipe(
               stack.m_41720_(), IngredientCreatorAccess.item().from(stack, 1), MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(food.m_38744_() * 50)
            )
            : null;
      } else {
         return null;
      }
   }

   @NotNull
   public CachedRecipe<ItemStackToFluidRecipe> createNewCachedRecipe(@NotNull ItemStackToFluidRecipe recipe, int cacheIndex) {
      return OneInputCachedRecipe.itemToFluid(recipe, this.recheckAllRecipeErrors, this.inputHandler, this.outputHandler)
         .setErrorsChanged(x$0 -> this.onErrorsChanged(x$0))
         .setCanHolderFunction(() -> MekanismUtils.canFunction(this))
         .setActive(this::setActive)
         .setEnergyRequirements(this.energyContainer::getEnergyPerTick, this.energyContainer)
         .setRequiredTicks(this::getTicksRequired)
         .setOnFinish(this::markForSave)
         .setOperatingTicksChanged(x$0 -> this.setOperatingTicks(x$0));
   }

   public MachineEnergyContainer<TileEntityNutritionalLiquifier> getEnergyContainer() {
      return this.energyContainer;
   }

   public ItemStack getRenderStack() {
      return this.lastPasteItem == null ? ItemStack.f_41583_ : this.lastPasteItem.getInternalStack();
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128365_("fluid", this.fluidTank.serializeNBT());
      CompoundTag item = new CompoundTag();
      if (this.lastPasteItem != null) {
         NBTUtils.writeRegistryEntry(item, "id", ForgeRegistries.ITEMS, this.lastPasteItem.getItem());
         CompoundTag tag = this.lastPasteItem.getInternalTag();
         if (tag != null) {
            item.m_128365_("tag", tag.m_6426_());
         }
      }

      updateTag.m_128365_("Item", item);
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setCompoundIfPresent(tag, "fluid", nbt -> this.fluidTank.deserializeNBT(nbt));
      NBTUtils.setCompoundIfPresent(tag, "Item", nbt -> {
         if (nbt.m_128456_()) {
            this.lastPasteItem = null;
         } else if (nbt.m_128425_("id", 8)) {
            ResourceLocation id = ResourceLocation.m_135820_(nbt.m_128461_("id"));
            if (id != null) {
               Item item = (Item)ForgeRegistries.ITEMS.getValue(id);
               if (item != null && item != Items.f_41852_) {
                  ItemStack stack = new ItemStack(item);
                  if (nbt.m_128425_("tag", 10)) {
                     stack.m_41751_(nbt.m_128469_("tag"));
                  }

                  this.lastPasteItem = HashedItem.raw(stack);
               }
            }
         }
      });
   }

   @ComputerMethod(
      methodDescription = "Get the energy used in the last tick by the machine"
   )
   public FloatingLong getEnergyUsage() {
      return this.getActive() ? this.energyContainer.getEnergyPerTick() : FloatingLong.ZERO;
   }
}
