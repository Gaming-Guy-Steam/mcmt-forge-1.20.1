package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.Upgrade;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.FloatingLong;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.content.assemblicator.RecipeFormula;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.container.sync.SyncableItemStack;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FormulaicCraftingSlot;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.component.config.slot.InventorySlotInfo;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UpgradeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityFormulaicAssemblicator extends TileEntityConfigurableMachine implements IHasMode {
   private static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.m_122779_();
   private static final Predicate<ItemStack> formulaSlotValidator = stack -> stack.m_41720_() instanceof ItemCraftingFormula;
   private static final int BASE_TICKS_REQUIRED = 40;
   private final CraftingContainer dummyInv = MekanismUtils.getDummyCraftingInv();
   private int ticksRequired = 40;
   private int operatingTicks;
   private boolean usedEnergy = false;
   private boolean autoMode = false;
   private boolean isRecipe = false;
   private boolean stockControl = false;
   private boolean needsOrganize = true;
   private final HashedItem[] stockControlMap = new HashedItem[18];
   private int pulseOperations;
   public RecipeFormula formula;
   @Nullable
   private CraftingRecipe cachedRecipe = null;
   @SyntheticComputerMethod(
      getter = "getExcessRemainingItems"
   )
   NonNullList<ItemStack> lastRemainingItems = EMPTY_LIST;
   private ItemStack lastFormulaStack = ItemStack.f_41583_;
   private ItemStack lastOutputStack = ItemStack.f_41583_;
   private MachineEnergyContainer<TileEntityFormulaicAssemblicator> energyContainer;
   private List<IInventorySlot> craftingGridSlots;
   private List<IInventorySlot> inputSlots;
   private List<IInventorySlot> outputSlots;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getFormulaItem"},
      docPlaceholder = "formula slot"
   )
   BasicInventorySlot formulaSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityFormulaicAssemblicator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.FORMULAIC_ASSEMBLICATOR, pos, state);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);
      this.configComponent.setupItemIOConfig(this.inputSlots, this.outputSlots, this.energySlot, false);
      ConfigInfo itemConfig = this.configComponent.getConfig(TransmissionType.ITEM);
      if (itemConfig != null) {
         itemConfig.addSlotInfo(DataType.EXTRA, new InventorySlotInfo(true, true, this.formulaSlot));
         itemConfig.setDefaults();
      }

      this.configComponent.setupInputConfig(TransmissionType.ENERGY, this.energyContainer);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
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
      this.craftingGridSlots = new ArrayList<>();
      this.inputSlots = new ArrayList<>();
      this.outputSlots = new ArrayList<>();
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.formulaSlot = BasicInventorySlot.at(formulaSlotValidator, listener, 6, 26)).setSlotOverlay(SlotOverlay.FORMULA);

      for (int slotY = 0; slotY < 2; slotY++) {
         for (int slotX = 0; slotX < 9; slotX++) {
            int index = slotY * 9 + slotX;
            InputInventorySlot inputSlot = InputInventorySlot.at(stack -> {
               if (this.formula == null) {
                  return true;
               } else {
                  IntList indices = this.formula.getIngredientIndices(this.f_58857_, stack);
                  if (!indices.isEmpty()) {
                     if (!this.stockControl) {
                        return true;
                     } else {
                        HashedItem stockItem = this.stockControlMap[index];
                        return stockItem == null || ItemHandlerHelper.canItemStacksStack(stockItem.getInternalStack(), stack);
                     }
                  } else {
                     return false;
                  }
               }
            }, BasicInventorySlot.alwaysTrue, listener, 8 + slotX * 18, 98 + slotY * 18);
            builder.addSlot(inputSlot);
            this.inputSlots.add(inputSlot);
         }
      }

      for (int slotY = 0; slotY < 3; slotY++) {
         for (int slotX = 0; slotX < 3; slotX++) {
            IInventorySlot craftingSlot = FormulaicCraftingSlot.at(this::getAutoMode, listener, 26 + slotX * 18, 17 + slotY * 18);
            builder.addSlot(craftingSlot);
            this.craftingGridSlots.add(craftingSlot);
         }
      }

      for (int slotY = 0; slotY < 3; slotY++) {
         for (int slotX = 0; slotX < 2; slotX++) {
            OutputInventorySlot outputSlot = OutputInventorySlot.at(listener, 116 + slotX * 18, 17 + slotY * 18);
            builder.addSlot(outputSlot);
            this.outputSlots.add(outputSlot);
         }
      }

      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 152, 76));
      return builder.build();
   }

   public BasicInventorySlot getFormulaSlot() {
      return this.formulaSlot;
   }

   public void onLoad() {
      super.onLoad();
      if (!this.isRemote()) {
         this.checkFormula();
         this.recalculateRecipe();
         if (this.formula != null && this.stockControl) {
            this.buildStockControlMap();
         }
      }
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
         this.cachedRecipe = null;
         this.recalculateRecipe();
      }

      if (this.formula != null && this.stockControl && this.needsOrganize) {
         this.buildStockControlMap();
         this.organizeStock();
         this.needsOrganize = false;
      }

      this.energySlot.fillContainerOrConvert();
      if (this.getControlType() != IRedstoneControl.RedstoneControl.PULSE) {
         this.pulseOperations = 0;
      } else if (MekanismUtils.canFunction(this)) {
         this.pulseOperations++;
      }

      this.checkFormula();
      if (this.autoMode && this.formula == null) {
         this.nextMode();
      }

      FloatingLong clientEnergyUsed = FloatingLong.ZERO;
      if (this.autoMode
         && this.formula != null
         && (this.getControlType() == IRedstoneControl.RedstoneControl.PULSE && this.pulseOperations > 0 || MekanismUtils.canFunction(this))) {
         boolean canOperate = true;
         if (!this.isRecipe) {
            canOperate = this.moveItemsToGrid();
         }

         if (canOperate) {
            this.isRecipe = true;
            if (this.operatingTicks >= this.ticksRequired) {
               if (this.doSingleCraft()) {
                  this.operatingTicks = 0;
                  if (this.pulseOperations > 0) {
                     this.pulseOperations--;
                  }
               }
            } else {
               FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
               if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                  clientEnergyUsed = this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                  this.operatingTicks++;
               }
            }
         } else {
            this.operatingTicks = 0;
         }
      } else {
         this.operatingTicks = 0;
      }

      this.usedEnergy = !clientEnergyUsed.isZero();
   }

   private void checkFormula() {
      ItemStack formulaStack = this.formulaSlot.getStack();
      if (formulaStack.m_41619_() || !(formulaStack.m_41720_() instanceof ItemCraftingFormula)) {
         this.formula = null;
      } else if (this.formula == null || this.lastFormulaStack != formulaStack) {
         this.loadFormula();
      }

      this.lastFormulaStack = formulaStack;
   }

   private void loadFormula() {
      ItemStack formulaStack = this.formulaSlot.getStack();
      ItemCraftingFormula formulaItem = (ItemCraftingFormula)formulaStack.m_41720_();
      if (formulaItem.isInvalid(formulaStack)) {
         this.formula = null;
      } else {
         NonNullList<ItemStack> formulaInventory = formulaItem.getInventory(formulaStack);
         if (formulaInventory == null) {
            this.formula = null;
         } else {
            RecipeFormula recipe = new RecipeFormula(this.f_58857_, formulaInventory);
            if (recipe.isValidFormula()) {
               if (this.formula == null) {
                  this.formula = recipe;
               } else if (!this.formula.isFormulaEqual(recipe)) {
                  this.formula = recipe;
                  this.operatingTicks = 0;
               }
            } else {
               this.formula = null;
               formulaItem.setInvalid(formulaStack, true);
            }
         }
      }
   }

   @Override
   protected void setChanged(boolean updateComparator) {
      super.setChanged(updateComparator);
      this.recalculateRecipe();
   }

   private void recalculateRecipe() {
      if (this.f_58857_ != null && !this.isRemote()) {
         if (this.formula != null && this.formula.isValidFormula()) {
            this.isRecipe = this.formula.matches(this.f_58857_, this.craftingGridSlots);
            if (this.isRecipe) {
               this.lastOutputStack = this.formula.assemble(this.f_58857_.m_9598_());
               this.lastRemainingItems = this.formula.getRemainingItems();
            } else {
               this.lastOutputStack = ItemStack.f_41583_;
            }
         } else {
            for (int i = 0; i < this.craftingGridSlots.size(); i++) {
               this.dummyInv.m_6836_(i, this.craftingGridSlots.get(i).getStack().m_255036_(1));
            }

            this.lastRemainingItems = EMPTY_LIST;
            if (this.cachedRecipe == null || !this.cachedRecipe.m_5818_(this.dummyInv, this.f_58857_)) {
               this.cachedRecipe = (CraftingRecipe)MekanismRecipeType.getRecipeFor(RecipeType.f_44107_, this.dummyInv, this.f_58857_).orElse(null);
            }

            if (this.cachedRecipe == null) {
               this.lastOutputStack = ItemStack.f_41583_;
            } else {
               this.lastOutputStack = this.cachedRecipe.m_5874_(this.dummyInv, this.f_58857_.m_9598_());
               this.lastRemainingItems = this.cachedRecipe.m_7457_(this.dummyInv);
            }

            this.isRecipe = !this.lastOutputStack.m_41619_();
         }

         this.needsOrganize = true;
      }
   }

   private boolean doSingleCraft() {
      this.recalculateRecipe();
      ItemStack output = this.lastOutputStack;
      if (!output.m_41619_()
         && this.tryMoveToOutput(output, Action.SIMULATE)
         && (this.lastRemainingItems.isEmpty() || this.lastRemainingItems.stream().allMatch(it -> it.m_41619_() || this.tryMoveToOutput(it, Action.SIMULATE)))) {
         this.tryMoveToOutput(output, Action.EXECUTE);

         for (ItemStack remainingItem : this.lastRemainingItems) {
            if (!remainingItem.m_41619_()) {
               this.tryMoveToOutput(remainingItem, Action.EXECUTE);
            }
         }

         for (IInventorySlot craftingSlot : this.craftingGridSlots) {
            if (!craftingSlot.isEmpty()) {
               MekanismUtils.logMismatchedStackSize(craftingSlot.shrinkStack(1, Action.EXECUTE), 1L);
            }
         }

         if (this.formula != null) {
            this.moveItemsToGrid();
         }

         this.markForSave();
         return true;
      } else {
         return false;
      }
   }

   public boolean craftSingle() {
      if (this.formula == null) {
         return this.doSingleCraft();
      } else {
         boolean canOperate = true;
         if (!this.formula.matches(this.m_58904_(), this.craftingGridSlots)) {
            canOperate = this.moveItemsToGrid();
         }

         return canOperate ? this.doSingleCraft() : false;
      }
   }

   private boolean moveItemsToGrid() {
      boolean ret = true;

      for (int i = 0; i < this.craftingGridSlots.size(); i++) {
         IInventorySlot recipeSlot = this.craftingGridSlots.get(i);
         ItemStack recipeStack = recipeSlot.getStack();
         if (!this.formula.isIngredientInPos(this.f_58857_, recipeStack, i)) {
            if (!recipeStack.m_41619_()) {
               ItemStack var9;
               recipeSlot.setStack(var9 = this.tryMoveToInput(recipeStack));
               this.markForSave();
               if (!var9.m_41619_()) {
                  ret = false;
               }
            } else {
               boolean found = false;

               for (int j = this.inputSlots.size() - 1; j >= 0; j--) {
                  IInventorySlot stockSlot = this.inputSlots.get(j);
                  if (!stockSlot.isEmpty()) {
                     ItemStack stockStack = stockSlot.getStack();
                     if (this.formula.isIngredientInPos(this.f_58857_, stockStack, i)) {
                        recipeSlot.setStack(stockStack.m_255036_(1));
                        MekanismUtils.logMismatchedStackSize(stockSlot.shrinkStack(1, Action.EXECUTE), 1L);
                        this.markForSave();
                        found = true;
                        break;
                     }
                  }
               }

               if (!found) {
                  ret = false;
               }
            }
         }
      }

      return ret;
   }

   public void craftAll() {
      while (this.craftSingle()) {
      }
   }

   public void fillGrid() {
      if (this.formula != null) {
         this.moveItemsToGrid();
      }
   }

   public void emptyGrid() {
      if (this.formula == null) {
         this.moveItemsToInput(true);
      }
   }

   private void moveItemsToInput(boolean forcePush) {
      for (int i = 0; i < this.craftingGridSlots.size(); i++) {
         IInventorySlot recipeSlot = this.craftingGridSlots.get(i);
         ItemStack recipeStack = recipeSlot.getStack();
         if (!recipeStack.m_41619_() && (forcePush || this.formula != null && !this.formula.isIngredientInPos(this.m_58904_(), recipeStack, i))) {
            recipeSlot.setStack(this.tryMoveToInput(recipeStack));
         }
      }

      this.markForSave();
   }

   @Override
   public void nextMode() {
      if (this.autoMode) {
         this.operatingTicks = 0;
         this.autoMode = false;
         this.markForSave();
      } else if (this.formula != null) {
         this.moveItemsToInput(false);
         this.autoMode = true;
         this.markForSave();
      }
   }

   @Override
   public void previousMode() {
      this.nextMode();
   }

   @ComputerMethod
   public boolean hasRecipe() {
      return this.isRecipe;
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

   public boolean getStockControl() {
      return this.stockControl;
   }

   public boolean getAutoMode() {
      return this.autoMode;
   }

   public void toggleStockControl() {
      if (!this.isRemote() && this.formula != null) {
         this.stockControl = !this.stockControl;
         if (this.stockControl) {
            this.organizeStock();
            this.needsOrganize = false;
         }
      }
   }

   private void organizeStock() {
      if (this.formula != null) {
         Object2IntMap<HashedItem> storedMap = new Object2IntLinkedOpenHashMap();

         for (IInventorySlot inputSlot : this.inputSlots) {
            if (!inputSlot.isEmpty()) {
               ItemStack stack = inputSlot.getStack();
               HashedItem hashed = HashedItem.create(stack);
               storedMap.mergeInt(hashed, stack.m_41613_(), Integer::sum);
            }
         }

         IntSet unused = new IntArraySet(this.stockControlMap.length);

         for (int i = 0; i < this.inputSlots.size(); i++) {
            HashedItem hashedItem = this.stockControlMap[i];
            if (hashedItem == null) {
               unused.add(i);
            } else {
               IInventorySlot slot = this.inputSlots.get(i);
               int stored = storedMap.getInt(hashedItem);
               if (stored > 0) {
                  int count = Math.min(hashedItem.getMaxStackSize(), stored);
                  if (count == stored) {
                     storedMap.removeInt(hashedItem);
                  } else {
                     storedMap.put(hashedItem, stored - count);
                  }

                  setSlotIfChanged(slot, hashedItem, count);
               } else if (!slot.isEmpty()) {
                  slot.setEmpty();
               }
            }
         }

         boolean empty = storedMap.isEmpty();
         IntIterator var12 = unused.iterator();

         while (var12.hasNext()) {
            int ix = (Integer)var12.next();
            IInventorySlot slot = this.inputSlots.get(ix);
            if (empty) {
               if (!slot.isEmpty()) {
                  slot.setEmpty();
               }
            } else {
               empty = this.setSlotIfChanged(storedMap, slot);
            }
         }

         if (!empty) {
            for (IInventorySlot inputSlotx : this.inputSlots) {
               if (inputSlotx.isEmpty() && this.setSlotIfChanged(storedMap, inputSlotx)) {
                  return;
               }
            }

            if (!storedMap.isEmpty()) {
               Mekanism.logger.error("Critical error: Formulaic Assemblicator had items left over after organizing stock. Impossible!");
            }
         }
      }
   }

   private boolean setSlotIfChanged(Object2IntMap<HashedItem> storedMap, IInventorySlot inputSlot) {
      boolean empty = false;
      ObjectIterator<Entry<HashedItem>> iterator = storedMap.object2IntEntrySet().iterator();
      Entry<HashedItem> next = (Entry<HashedItem>)iterator.next();
      HashedItem item = (HashedItem)next.getKey();
      int stored = next.getIntValue();
      int count = Math.min(item.getMaxStackSize(), stored);
      if (count == stored) {
         iterator.remove();
         empty = storedMap.isEmpty();
      } else {
         next.setValue(stored - count);
      }

      setSlotIfChanged(inputSlot, item, count);
      return empty;
   }

   private static void setSlotIfChanged(IInventorySlot slot, HashedItem item, int count) {
      ItemStack stack = item.createStack(count);
      if (!ItemStack.m_41728_(slot.getStack(), stack)) {
         slot.setStack(stack);
      }
   }

   private void buildStockControlMap() {
      if (this.formula != null) {
         for (int i = 0; i < 9; i++) {
            int j = i * 2;
            ItemStack stack = this.formula.getInputStack(i);
            if (stack.m_41619_()) {
               this.stockControlMap[j] = null;
               this.stockControlMap[j + 1] = null;
            } else {
               HashedItem hashedItem = HashedItem.create(stack);
               this.stockControlMap[j] = hashedItem;
               this.stockControlMap[j + 1] = hashedItem;
            }
         }
      }
   }

   private ItemStack tryMoveToInput(ItemStack stack) {
      return InventoryUtils.insertItem(this.inputSlots, stack, Action.EXECUTE, AutomationType.INTERNAL);
   }

   private boolean tryMoveToOutput(ItemStack stack, Action action) {
      stack = InventoryUtils.insertItem(this.outputSlots, stack, action, AutomationType.INTERNAL);
      return stack.m_41619_();
   }

   public void encodeFormula() {
      if (!this.formulaSlot.isEmpty()) {
         ItemStack formulaStack = this.formulaSlot.getStack();
         if (formulaStack.m_41720_() instanceof ItemCraftingFormula item && !item.hasInventory(formulaStack)) {
            RecipeFormula formula = new RecipeFormula(this.f_58857_, this.craftingGridSlots);
            if (formula.isValidFormula()) {
               item.setInventory(formulaStack, formula.input);
               this.markForSave();
            }
         }
      }
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      this.autoMode = nbt.m_128471_("auto");
      this.operatingTicks = nbt.m_128451_("progress");
      this.pulseOperations = nbt.m_128451_("pulse");
      this.stockControl = nbt.m_128471_("stockControl");
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128379_("auto", this.autoMode);
      nbtTags.m_128405_("progress", this.operatingTicks);
      nbtTags.m_128405_("pulse", this.pulseOperations);
      nbtTags.m_128379_("stockControl", this.stockControl);
   }

   @Override
   public boolean canPulse() {
      return true;
   }

   @Override
   public void recalculateUpgrades(Upgrade upgrade) {
      super.recalculateUpgrades(upgrade);
      if (upgrade == Upgrade.SPEED) {
         this.ticksRequired = MekanismUtils.getTicks(this, 40);
      }
   }

   @NotNull
   @Override
   public List<Component> getInfo(@NotNull Upgrade upgrade) {
      return UpgradeUtils.getMultScaledInfo(this, upgrade);
   }

   public MachineEnergyContainer<TileEntityFormulaicAssemblicator> getEnergyContainer() {
      return this.energyContainer;
   }

   public boolean usedEnergy() {
      return this.usedEnergy;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(this::getAutoMode, value -> this.autoMode = value));
      container.track(SyncableInt.create(this::getOperatingTicks, value -> this.operatingTicks = value));
      container.track(SyncableInt.create(this::getTicksRequired, value -> this.ticksRequired = value));
      container.track(SyncableBoolean.create(this::hasRecipe, value -> this.isRecipe = value));
      container.track(SyncableBoolean.create(this::getStockControl, value -> this.stockControl = value));
      container.track(SyncableBoolean.create(this::usedEnergy, value -> this.usedEnergy = value));
      container.track(SyncableBoolean.create(() -> this.formula != null, hasFormula -> {
         if (hasFormula) {
            if (this.formula == null && this.isRemote()) {
               this.formula = new RecipeFormula(this.m_58904_(), NonNullList.m_122780_(9, ItemStack.f_41583_));
            }
         } else {
            this.formula = null;
         }
      }));

      for (int i = 0; i < 9; i++) {
         int index = i;
         container.track(SyncableItemStack.create(() -> this.formula == null ? ItemStack.f_41583_ : (ItemStack)this.formula.input.get(index), stack -> {
            if (!stack.m_41619_() && this.formula == null && this.isRemote()) {
               this.formula = new RecipeFormula(this.m_58904_(), NonNullList.m_122780_(9, ItemStack.f_41583_));
            }

            if (this.formula != null) {
               this.formula.setStack(this.m_58904_(), index, stack);
            }
         }));
      }
   }

   @ComputerMethod
   ItemStack getCraftingInputSlot(int slot) throws ComputerException {
      if (slot >= 0 && slot < this.craftingGridSlots.size()) {
         return this.craftingGridSlots.get(slot).getStack();
      } else {
         throw new ComputerException("Crafting Input Slot '%d' is out of bounds, must be between 0 and %d.", slot, this.craftingGridSlots.size());
      }
   }

   @ComputerMethod
   int getCraftingOutputSlots() {
      return this.outputSlots.size();
   }

   @ComputerMethod
   ItemStack getCraftingOutputSlot(int slot) throws ComputerException {
      int size = this.getCraftingOutputSlots();
      if (slot >= 0 && slot < size) {
         return this.outputSlots.get(slot).getStack();
      } else {
         throw new ComputerException("Crafting Output Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
      }
   }

   @ComputerMethod
   boolean hasValidFormula() {
      return this.formula != null && this.formula.isValidFormula();
   }

   @ComputerMethod(
      nameOverride = "getSlots"
   )
   int computerGetSlots() {
      return this.inputSlots.size();
   }

   @ComputerMethod
   ItemStack getItemInSlot(int slot) throws ComputerException {
      int size = this.computerGetSlots();
      if (slot >= 0 && slot < size) {
         return this.inputSlots.get(slot).getStack();
      } else {
         throw new ComputerException("Slot '%d' is out of bounds, must be between 0 and %d.", slot, size);
      }
   }

   @ComputerMethod(
      nameOverride = "encodeFormula",
      requiresPublicSecurity = true,
      methodDescription = "Requires an unencoded formula in the formula slot and a valid recipe"
   )
   void computerEncodeFormula() throws ComputerException {
      this.validateSecurityIsPublic();
      ItemStack formulaStack = this.formulaSlot.getStack();
      if (!(!formulaStack.m_41619_() && formulaStack.m_41720_() instanceof ItemCraftingFormula craftingFormula)) {
         throw new ComputerException("No formula found.");
      } else if (this.formula != null && this.formula.isValidFormula() || craftingFormula.hasInventory(formulaStack)) {
         throw new ComputerException("Formula has already been encoded.");
      } else if (!this.hasRecipe()) {
         throw new ComputerException("Encoding formulas require that there is a valid recipe to actually encode.");
      } else {
         this.encodeFormula();
      }
   }

   @ComputerMethod(
      nameOverride = "emptyGrid",
      requiresPublicSecurity = true,
      methodDescription = "Requires auto mode to be disabled"
   )
   void computerEmptyGrid() throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.autoMode) {
         throw new ComputerException("Emptying the grid requires Auto-Mode to be disabled.");
      } else {
         this.emptyGrid();
      }
   }

   @ComputerMethod(
      nameOverride = "fillGrid",
      requiresPublicSecurity = true,
      methodDescription = "Requires auto mode to be disabled"
   )
   void computerFillGrid() throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.autoMode) {
         throw new ComputerException("Filling the grid requires Auto-Mode to be disabled.");
      } else {
         this.fillGrid();
      }
   }

   private void validateCanCraft() throws ComputerException {
      this.validateSecurityIsPublic();
      if (!this.hasRecipe()) {
         throw new ComputerException("Unable to perform craft as there is currently no matching recipe in the grid.");
      } else if (this.autoMode) {
         throw new ComputerException("Unable to perform craft as Auto-Mode is enabled.");
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires recipe and auto mode to be disabled"
   )
   void craftSingleItem() throws ComputerException {
      this.validateCanCraft();
      this.craftSingle();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires recipe and auto mode to be disabled"
   )
   void craftAvailableItems() throws ComputerException {
      this.validateCanCraft();
      this.craftAll();
   }

   private void validateHasValidFormula(String operation) throws ComputerException {
      this.validateSecurityIsPublic();
      if (!this.hasValidFormula()) {
         throw new ComputerException("%s requires a valid formula.", operation);
      }
   }

   @ComputerMethod(
      nameOverride = "getStockControl",
      requiresPublicSecurity = true,
      methodDescription = "Requires valid encoded formula"
   )
   boolean computerGetStockControl() throws ComputerException {
      this.validateHasValidFormula("Stock Control");
      return this.getStockControl();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires valid encoded formula"
   )
   void setStockControl(boolean mode) throws ComputerException {
      this.validateHasValidFormula("Stock Control");
      if (this.stockControl != mode) {
         this.toggleStockControl();
      }
   }

   @ComputerMethod(
      nameOverride = "getAutoMode",
      requiresPublicSecurity = true,
      methodDescription = "Requires valid encoded formula"
   )
   boolean computerGetAutoMode() throws ComputerException {
      this.validateHasValidFormula("Auto-Mode");
      return this.getAutoMode();
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Requires valid encoded formula"
   )
   void setAutoMode(boolean mode) throws ComputerException {
      this.validateHasValidFormula("Auto-Mode");
      if (this.autoMode != mode) {
         this.nextMode();
      }
   }
}
