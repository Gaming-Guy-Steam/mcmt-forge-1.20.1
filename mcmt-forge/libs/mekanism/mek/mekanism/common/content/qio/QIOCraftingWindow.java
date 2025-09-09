package mekanism.common.content.qio;

import com.blamejared.recipestages.RecipeStagesUtil;
import com.blamejared.recipestages.recipes.IStagedRecipe;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.IInsertableSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowOutputInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOCraftingWindow implements IContentsListener {
   private static final SelectedWindowData[] WINDOWS = new SelectedWindowData[3];
   private final CraftingWindowInventorySlot[] inputSlots = new CraftingWindowInventorySlot[9];
   private final QIOCraftingWindow.ReplacementHelper replacementHelper = new QIOCraftingWindow.ReplacementHelper();
   private final QIOCraftingWindow.RemainderHelper remainderHelper = new QIOCraftingWindow.RemainderHelper();
   private final CraftingWindowOutputInventorySlot outputSlot;
   private final QIOCraftingWindow.QIOCraftingInventory craftingInventory;
   private final IQIOCraftingWindowHolder holder;
   private final SelectedWindowData windowData;
   private final byte windowIndex;
   @Nullable
   private CraftingRecipe lastRecipe;
   private boolean isCrafting;
   private boolean changedWhileCrafting;

   public QIOCraftingWindow(IQIOCraftingWindowHolder holder, byte windowIndex) {
      this.windowIndex = windowIndex;
      this.holder = holder;
      this.windowData = WINDOWS[windowIndex];

      for (int slotIndex = 0; slotIndex < 9; slotIndex++) {
         this.inputSlots[slotIndex] = CraftingWindowInventorySlot.input(this, this.holder);
      }

      this.outputSlot = CraftingWindowOutputInventorySlot.create(this);
      this.craftingInventory = new QIOCraftingWindow.QIOCraftingInventory();
   }

   public SelectedWindowData getWindowData() {
      return this.windowData;
   }

   public byte getWindowIndex() {
      return this.windowIndex;
   }

   public CraftingWindowInventorySlot getInputSlot(int slot) {
      if (slot >= 0 && slot < 9) {
         return this.inputSlots[slot];
      } else {
         throw new IllegalArgumentException("Input slot out of range");
      }
   }

   public CraftingWindowOutputInventorySlot getOutputSlot() {
      return this.outputSlot;
   }

   public boolean isOutput(@NotNull ItemStack stack) {
      return ItemHandlerHelper.canItemStacksStack(this.outputSlot.getStack(), stack);
   }

   @Override
   public void onContentsChanged() {
      if (this.isCrafting) {
         this.changedWhileCrafting = true;
      } else {
         Level world = this.holder.getHolderWorld();
         if (world != null && !world.f_46443_) {
            this.updateOutputSlot(world);
         }
      }
   }

   public void invalidateRecipe() {
      this.lastRecipe = null;
      if (!this.outputSlot.isEmpty()) {
         this.outputSlot.setEmpty();
      }

      Level world = this.holder.getHolderWorld();
      if (world != null && !world.f_46443_) {
         this.updateOutputSlot(world);
      }
   }

   private void updateOutputSlot(@NotNull Level world) {
      if (world.m_7654_() != null) {
         if (this.craftingInventory.m_7983_()) {
            if (!this.outputSlot.isEmpty()) {
               this.outputSlot.setEmpty();
            }
         } else if (this.lastRecipe != null && this.lastRecipe.m_5818_(this.craftingInventory, world)) {
            this.outputSlot.setStack(this.assembleRecipe(this.lastRecipe, world.m_9598_()));
         } else {
            CraftingRecipe recipe = (CraftingRecipe)MekanismRecipeType.getRecipeFor(RecipeType.f_44107_, this.craftingInventory, world).orElse(null);
            if (recipe != this.lastRecipe) {
               if (recipe == null) {
                  if (!this.outputSlot.isEmpty()) {
                     this.outputSlot.setEmpty();
                  }
               } else {
                  this.lastRecipe = recipe;
                  this.outputSlot.setStack(this.assembleRecipe(this.lastRecipe, world.m_9598_()));
               }
            }
         }
      }
   }

   private ItemStack assembleRecipe(CraftingRecipe recipe, RegistryAccess registryAccess) {
      return Mekanism.hooks.RecipeStagesLoaded && recipe instanceof IStagedRecipe stagedRecipe
         ? stagedRecipe.forceAssemble(this.craftingInventory, registryAccess)
         : recipe.m_5874_(this.craftingInventory, registryAccess);
   }

   public boolean canViewRecipe(@NotNull ServerPlayer player) {
      if (this.lastRecipe == null) {
         return false;
      } else {
         return Mekanism.hooks.RecipeStagesLoaded && !RecipeStagesUtil.hasStageForRecipe(this.lastRecipe, player)
            ? false
            : this.lastRecipe.m_5598_() || !player.m_9236_().m_46469_().m_46207_(GameRules.f_46151_) || player.m_8952_().m_12709_(this.lastRecipe);
      }
   }

   @Contract("null, _ -> false")
   private boolean validateAndUnlockRecipe(@Nullable Level world, @NotNull Player player) {
      if (world != null && this.lastRecipe != null && this.lastRecipe.m_5818_(this.craftingInventory, world)) {
         if (this.lastRecipe != null && !this.lastRecipe.m_5598_()) {
            if (player instanceof ServerPlayer serverPlayer
               && world.m_46469_().m_46207_(GameRules.f_46151_)
               && !serverPlayer.m_8952_().m_12709_(this.lastRecipe)) {
               return false;
            }

            player.m_7281_(Collections.singleton(this.lastRecipe));
         }

         return true;
      } else {
         return false;
      }
   }

   private void craftingStarted(@NotNull Player player) {
      this.isCrafting = true;
      ForgeHooks.setCraftingPlayer(player);
   }

   private void craftingFinished(@NotNull Level world) {
      ForgeHooks.setCraftingPlayer(null);
      this.isCrafting = false;
      if (this.changedWhileCrafting) {
         this.changedWhileCrafting = false;
         this.updateOutputSlot(world);
      }
   }

   private int calculateMaxCraftAmount(@NotNull ItemStack stack, @Nullable QIOFrequency frequency) {
      int outputSize = stack.m_41613_();
      int inputSize = 64;

      for (CraftingWindowInventorySlot inputSlot : this.inputSlots) {
         int count = inputSlot.getCount();
         if (count > 0 && count < inputSize) {
            inputSize = count;
            if (count == 1) {
               break;
            }
         }
      }

      if (inputSize > 1) {
         return inputSize * outputSize;
      } else if (frequency == null) {
         return outputSize;
      } else {
         int maxToCraft = stack.m_41741_();
         if (outputSize < maxToCraft) {
            maxToCraft -= maxToCraft % outputSize;
         }

         return maxToCraft;
      }
   }

   private void useInput(IInventorySlot inputSlot) {
      MekanismUtils.logMismatchedStackSize(inputSlot.shrinkStack(1, Action.EXECUTE), 1L);
   }

   public void emptyTo(boolean toPlayerInv, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
      if (toPlayerInv) {
         this.emptyTo(toTransfer -> {
            ItemStack remainder = MekanismContainer.insertItem(hotBarSlots, toTransfer, true, this.windowData);
            remainder = MekanismContainer.insertItem(mainInventorySlots, remainder, true, this.windowData);
            remainder = MekanismContainer.insertItem(hotBarSlots, remainder, false, this.windowData);
            return MekanismContainer.insertItem(mainInventorySlots, remainder, false, this.windowData);
         });
      } else {
         QIOFrequency frequency = this.holder.getFrequency();
         if (frequency != null) {
            this.emptyTo(frequency::addItem);
         }
      }
   }

   private void emptyTo(UnaryOperator<ItemStack> inserter) {
      for (CraftingWindowInventorySlot inputSlot : this.inputSlots) {
         if (!inputSlot.isEmpty()) {
            ItemStack toTransfer = inputSlot.extractItem(inputSlot.getCount(), Action.SIMULATE, AutomationType.INTERNAL);
            if (!toTransfer.m_41619_()) {
               ItemStack remainder = inserter.apply(toTransfer);
               inputSlot.extractItem(toTransfer.m_41613_() - remainder.m_41613_(), Action.EXECUTE, AutomationType.INTERNAL);
            }
         }
      }
   }

   public void performCraft(@NotNull Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
      if (this.lastRecipe != null && !this.outputSlot.isEmpty()) {
         Level world = this.holder.getHolderWorld();
         if (this.validateAndUnlockRecipe(world, player)) {
            QIOFrequency frequency = this.holder.getFrequency();
            this.craftingStarted(player);
            ItemStack result = this.outputSlot.getStack().m_41777_();
            Item resultItem = result.m_41720_();
            resultItem.m_7836_(result, world, player);
            Stat<Item> itemCraftedStat = Stats.f_12981_.m_12902_(resultItem);
            int maxToCraft = this.calculateMaxCraftAmount(result, frequency);
            int amountPerCraft = result.m_41613_();
            int crafted = 0;
            this.remainderHelper.reset();
            this.replacementHelper.reset();
            boolean recheckOutput = false;
            QIOCraftingWindow.LastInsertTarget lastInsertTarget = new QIOCraftingWindow.LastInsertTarget();

            for (NonNullList<ItemStack> remaining = this.lastRecipe.m_7457_(this.craftingInventory); crafted < maxToCraft; crafted += amountPerCraft) {
               if (recheckOutput && this.changedWhileCrafting) {
                  recheckOutput = false;
                  this.changedWhileCrafting = false;
                  CraftingRecipe oldRecipe = this.lastRecipe;
                  this.updateOutputSlot(world);
                  if (oldRecipe != this.lastRecipe) {
                     break;
                  }

                  ItemStack updatedOutput = this.outputSlot.getStack();
                  if (updatedOutput.m_41619_() || updatedOutput.m_41720_() != resultItem) {
                     break;
                  }

                  ItemStack potentialUpdatedOutput = updatedOutput.m_41777_();
                  resultItem.m_7836_(potentialUpdatedOutput, world, player);
                  if (!ItemStack.m_41728_(result, potentialUpdatedOutput)) {
                     break;
                  }

                  remaining = this.lastRecipe.m_7457_(this.craftingInventory);
               }

               ItemStack simulatedRemainder = MekanismContainer.insertItemCheckAll(hotBarSlots, result, this.windowData, Action.SIMULATE);
               simulatedRemainder = MekanismContainer.insertItemCheckAll(mainInventorySlots, simulatedRemainder, this.windowData, Action.SIMULATE);
               if (!simulatedRemainder.m_41619_()) {
                  break;
               }

               ItemStack toInsert = lastInsertTarget.tryInserting(hotBarSlots, mainInventorySlots, this.windowData, result);
               if (!toInsert.m_41619_()) {
                  player.m_36176_(toInsert, false);
               }

               boolean stopCrafting = false;

               for (int index = 0; index < remaining.size(); index++) {
                  ItemStack remainder = (ItemStack)remaining.get(index);
                  CraftingWindowInventorySlot inputSlot = this.inputSlots[index];
                  if (inputSlot.getCount() > 1) {
                     this.useInput(inputSlot);
                  } else if (inputSlot.getCount() == 1) {
                     if (frequency != null && !this.remainderHelper.isStackStillValid(world, remainder, index)) {
                        ItemStack current = inputSlot.getStack();
                        ItemStack removed = frequency.removeItem(current, 1);
                        if (removed.m_41619_()) {
                           this.useInput(inputSlot);
                           this.replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current);
                           stopCrafting = true;
                        }
                     } else {
                        this.useInput(inputSlot);
                        recheckOutput = true;
                     }
                  } else if (!remainder.m_41619_()) {
                     recheckOutput = true;
                  }

                  this.addRemainingItem(player, frequency, inputSlot, remainder, true);
               }

               if (stopCrafting) {
                  crafted += amountPerCraft;
                  break;
               }
            }

            if (crafted > 0) {
               player.m_6278_(itemCraftedStat, crafted);
            }

            this.craftingFinished(world);
         }
      }
   }

   @NotNull
   public ItemStack performCraft(@NotNull Player player, @NotNull ItemStack result, int amountCrafted) {
      if (amountCrafted != 0 && this.lastRecipe != null && !result.m_41619_()) {
         Level world = this.holder.getHolderWorld();
         if (!this.validateAndUnlockRecipe(world, player)) {
            return ItemStack.f_41583_;
         } else {
            QIOFrequency frequency = this.holder.getFrequency();
            this.craftingStarted(player);
            result.m_41678_(world, player, amountCrafted);
            NonNullList<ItemStack> remaining = this.lastRecipe.m_7457_(this.craftingInventory);
            this.remainderHelper.reset();
            this.replacementHelper.reset();

            for (int index = 0; index < remaining.size(); index++) {
               ItemStack remainder = (ItemStack)remaining.get(index);
               CraftingWindowInventorySlot inputSlot = this.inputSlots[index];
               if (inputSlot.getCount() > 1) {
                  this.useInput(inputSlot);
               } else if (inputSlot.getCount() == 1) {
                  if (frequency != null && !this.remainderHelper.isStackStillValid(world, remainder, index)) {
                     ItemStack current = inputSlot.getStack();
                     ItemStack removed = frequency.removeItem(current, 1);
                     if (removed.m_41619_()) {
                        this.useInput(inputSlot);
                        this.replacementHelper.findEquivalentItem(world, frequency, inputSlot, index, current);
                     }
                  } else {
                     this.useInput(inputSlot);
                  }
               }

               this.addRemainingItem(player, frequency, inputSlot, remainder, false);
            }

            this.craftingFinished(world);
            return result;
         }
      } else {
         return ItemStack.f_41583_;
      }
   }

   private void addRemainingItem(Player player, @Nullable QIOFrequency frequency, IInventorySlot slot, @NotNull ItemStack remainder, boolean copyIfNeeded) {
      int toInsert = remainder.m_41613_();
      remainder = slot.insertItem(remainder, Action.EXECUTE, AutomationType.INTERNAL);
      if (!remainder.m_41619_()) {
         if (copyIfNeeded && toInsert == remainder.m_41613_()) {
            remainder = remainder.m_41777_();
         }

         if (!player.m_150109_().m_36054_(remainder)) {
            if (frequency != null) {
               remainder = frequency.addItem(remainder);
               if (remainder.m_41619_()) {
                  return;
               }
            }

            player.m_36176_(remainder, false);
         }
      }
   }

   static {
      for (byte tableIndex = 0; tableIndex < WINDOWS.length; tableIndex++) {
         WINDOWS[tableIndex] = new SelectedWindowData(SelectedWindowData.WindowType.CRAFTING, tableIndex);
      }
   }

   private static class LastInsertTarget {
      private boolean wasHotBar = true;
      private int lastIndex;

      public ItemStack tryInserting(List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots, SelectedWindowData windowData, ItemStack toInsert) {
         toInsert = this.insertItem(hotBarSlots, toInsert, true, true, windowData);
         toInsert = this.insertItem(mainInventorySlots, toInsert, true, false, windowData);
         toInsert = this.insertItem(hotBarSlots, toInsert, false, true, windowData);
         return this.insertItem(mainInventorySlots, toInsert, false, false, windowData);
      }

      @NotNull
      private <SLOT extends Slot & IInsertableSlot> ItemStack insertItem(
         List<SLOT> slots, @NotNull ItemStack stack, boolean ignoreEmpty, boolean isHotBar, @Nullable SelectedWindowData selectedWindow
      ) {
         if (stack.m_41619_()) {
            return stack;
         } else {
            int i = ignoreEmpty && this.wasHotBar == isHotBar ? this.lastIndex : 0;

            for (int slotCount = slots.size(); i < slotCount; i++) {
               SLOT slot = (SLOT)slots.get(i);
               if (ignoreEmpty == slot.m_6657_() && slot.exists(selectedWindow)) {
                  stack = slot.insertItem(stack, Action.EXECUTE);
                  if (stack.m_41619_()) {
                     this.wasHotBar = isHotBar;
                     this.lastIndex = i;
                     break;
                  }
               }
            }

            return stack;
         }
      }
   }

   private class QIOCraftingInventory implements CraftingContainer {
      public int m_6643_() {
         return QIOCraftingWindow.this.inputSlots.length;
      }

      public boolean m_7983_() {
         return Arrays.stream(QIOCraftingWindow.this.inputSlots).allMatch(BasicInventorySlot::isEmpty);
      }

      @NotNull
      public ItemStack m_8020_(int index) {
         if (index >= 0 && index < this.m_6643_()) {
            IInventorySlot inputSlot = QIOCraftingWindow.this.getInputSlot(index);
            if (!inputSlot.isEmpty()) {
               return inputSlot.getStack().m_41777_();
            }
         }

         return ItemStack.f_41583_;
      }

      @NotNull
      public List<ItemStack> m_280657_() {
         List<ItemStack> items = new ArrayList<>(this.m_39347_() * this.m_39346_());

         for (CraftingWindowInventorySlot inputSlot : QIOCraftingWindow.this.inputSlots) {
            items.add(inputSlot.getStack().m_41777_());
         }

         return List.copyOf(items);
      }

      @NotNull
      public ItemStack m_8016_(int index) {
         if (index >= 0 && index < this.m_6643_()) {
            IInventorySlot inputSlot = QIOCraftingWindow.this.getInputSlot(index);
            ItemStack stored = inputSlot.getStack();
            inputSlot.setEmpty();
            return stored;
         } else {
            return ItemStack.f_41583_;
         }
      }

      @NotNull
      public ItemStack m_7407_(int index, int count) {
         return index >= 0 && index < this.m_6643_()
            ? QIOCraftingWindow.this.getInputSlot(index).extractItem(count, Action.EXECUTE, AutomationType.INTERNAL)
            : ItemStack.f_41583_;
      }

      public void m_6836_(int index, @NotNull ItemStack stack) {
         if (index >= 0 && index < this.m_6643_()) {
            QIOCraftingWindow.this.getInputSlot(index).setStack(stack);
         }
      }

      public void m_6596_() {
      }

      public boolean m_6542_(@NotNull Player player) {
         return true;
      }

      public void m_6211_() {
         for (CraftingWindowInventorySlot inputSlot : QIOCraftingWindow.this.inputSlots) {
            inputSlot.setEmpty();
         }
      }

      public int m_39346_() {
         return 3;
      }

      public int m_39347_() {
         return 3;
      }

      public void m_5809_(@NotNull StackedContents helper) {
         boolean copyNeeded = helper.getClass() != StackedContents.class;

         for (CraftingWindowInventorySlot inputSlot : QIOCraftingWindow.this.inputSlots) {
            ItemStack stack = inputSlot.getStack();
            helper.m_36466_(copyNeeded ? stack.m_41777_() : stack);
         }
      }
   }

   private class RemainderHelper {
      private final CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();
      private boolean updated;

      public void reset() {
         if (this.updated) {
            this.updated = false;
            this.dummy.m_6211_();
         }
      }

      private void updateInputs(@NotNull ItemStack remainder) {
         if (!this.updated && !remainder.m_41619_()) {
            for (int index = 0; index < QIOCraftingWindow.this.inputSlots.length; index++) {
               this.dummy.m_6836_(index, QIOCraftingWindow.this.inputSlots[index].getStack().m_255036_(1));
            }

            this.updated = true;
         }
      }

      public void updateInputsWithReplacement(int index, ItemStack old) {
         if (!this.updated) {
            for (int i = 0; i < QIOCraftingWindow.this.inputSlots.length; i++) {
               ItemStack stack = i == index ? old : QIOCraftingWindow.this.inputSlots[i].getStack();
               this.dummy.m_6836_(i, stack.m_255036_(1));
            }

            this.updated = true;
         }
      }

      public boolean isStackStillValid(Level world, ItemStack stack, int index) {
         this.updateInputs(stack);
         ItemStack old = this.dummy.m_8020_(index);
         this.dummy.m_6836_(index, stack.m_255036_(1));
         if (QIOCraftingWindow.this.lastRecipe != null && QIOCraftingWindow.this.lastRecipe.m_5818_(this.dummy, world)) {
            return true;
         } else {
            this.dummy.m_6836_(index, old);
            return false;
         }
      }
   }

   private class ReplacementHelper {
      private final Int2ObjectMap<Ingredient> slotIngredients = new Int2ObjectArrayMap(QIOCraftingWindow.this.inputSlots.length);
      private boolean mapped;
      private boolean invalid;

      public void reset() {
         if (this.mapped) {
            this.mapped = false;
            this.invalid = false;
            this.slotIngredients.clear();
         }
      }

      public void findEquivalentItem(Level world, @NotNull QIOFrequency frequency, CraftingWindowInventorySlot slot, int index, ItemStack used) {
         this.mapRecipe(index, used);
         if (!this.invalid) {
            Ingredient usedIngredient = (Ingredient)this.slotIngredients.getOrDefault(index, Ingredient.f_43901_);
            if (usedIngredient.test(used)) {
               for (ItemStack item : usedIngredient.m_43908_()) {
                  if (!item.m_41619_()) {
                     if (!usedIngredient.isVanilla() && this.testEquivalentItem(world, frequency, slot, index, usedIngredient, HashedItem.raw(item))) {
                        return;
                     }

                     for (HashedItem type : frequency.getTypesForItem(item.m_41720_())) {
                        if (this.testEquivalentItem(world, frequency, slot, index, usedIngredient, type)) {
                           return;
                        }
                     }
                  }
               }
            }
         }
      }

      private boolean testEquivalentItem(
         Level world, @NotNull QIOFrequency frequency, CraftingWindowInventorySlot slot, int index, Ingredient usedIngredient, HashedItem replacementType
      ) {
         if (frequency.isStoring(replacementType) && usedIngredient.test(replacementType.getInternalStack())) {
            ItemStack replacement = replacementType.createStack(1);
            ItemStack old = QIOCraftingWindow.this.remainderHelper.dummy.m_8020_(index);
            if (QIOCraftingWindow.this.remainderHelper.isStackStillValid(world, replacement, index)) {
               if (slot.insertItem(replacement, Action.SIMULATE, AutomationType.INTERNAL).m_41619_()) {
                  ItemStack removed = frequency.removeByType(replacementType, 1);
                  if (!removed.m_41619_()) {
                     ItemStack stack = slot.insertItem(removed, Action.EXECUTE, AutomationType.INTERNAL);
                     if (!stack.m_41619_()) {
                        Mekanism.logger
                           .error(
                              "Failed to insert item ({} with NBT: {}) into crafting window: {}.",
                              new Object[]{removed.m_41720_(), removed.m_41783_(), QIOCraftingWindow.this.windowIndex}
                           );
                     }

                     return true;
                  }
               }

               QIOCraftingWindow.this.remainderHelper.dummy.m_6836_(index, old);
            }

            return false;
         } else {
            return false;
         }
      }

      private void mapRecipe(int index, ItemStack used) {
         if (!this.mapped) {
            this.mapped = true;
            if (QIOCraftingWindow.this.lastRecipe == null || QIOCraftingWindow.this.lastRecipe.m_5598_()) {
               this.invalid = true;
               return;
            }

            NonNullList<Ingredient> ingredients = QIOCraftingWindow.this.lastRecipe.m_7527_();
            if (ingredients.isEmpty()) {
               this.invalid = true;
               return;
            }

            QIOCraftingWindow.this.remainderHelper.updateInputsWithReplacement(index, used);
            IntFunction<ItemStack> itemGetter = i -> {
               if (i == index) {
                  return used;
               } else {
                  return i >= 0 && i < QIOCraftingWindow.this.inputSlots.length ? QIOCraftingWindow.this.inputSlots[i].getStack() : ItemStack.f_41583_;
               }
            };
            if (QIOCraftingWindow.this.lastRecipe instanceof IShapedRecipe<?> shapedRecipe) {
               this.mapShapedRecipe(shapedRecipe, ingredients, itemGetter);
            } else {
               this.mapShapelessRecipe(ingredients, itemGetter);
            }
         }
      }

      private void mapShapedRecipe(IShapedRecipe<?> shapedRecipe, NonNullList<Ingredient> ingredients, IntFunction<ItemStack> itemGetter) {
         int recipeWidth = shapedRecipe.getRecipeWidth();
         int recipeHeight = shapedRecipe.getRecipeHeight();

         for (int columnStart = 0; columnStart <= 3 - recipeWidth; columnStart++) {
            for (int rowStart = 0; rowStart <= 3 - recipeHeight; rowStart++) {
               if (this.mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, true, itemGetter)
                  || this.mapShapedRecipe(ingredients, columnStart, rowStart, recipeWidth, recipeHeight, false, itemGetter)) {
                  return;
               }
            }
         }

         this.invalid = true;
      }

      private boolean mapShapedRecipe(
         NonNullList<Ingredient> ingredients,
         int columnStart,
         int rowStart,
         int recipeWidth,
         int recipeHeight,
         boolean mirrored,
         IntFunction<ItemStack> itemGetter
      ) {
         for (int actualColumn = 0; actualColumn < 3; actualColumn++) {
            for (int actualRow = 0; actualRow < 3; actualRow++) {
               int column = actualColumn - columnStart;
               int row = actualRow - rowStart;
               Ingredient ingredient = Ingredient.f_43901_;
               if (column >= 0 && row >= 0 && column < recipeWidth && row < recipeHeight) {
                  if (mirrored) {
                     ingredient = (Ingredient)ingredients.get(recipeWidth - column - 1 + row * recipeWidth);
                  } else {
                     ingredient = (Ingredient)ingredients.get(column + row * recipeWidth);
                  }
               }

               int index = actualColumn + actualRow * 3;
               if (!ingredient.test(itemGetter.apply(index))) {
                  this.slotIngredients.clear();
                  return false;
               }

               this.slotIngredients.put(index, ingredient);
            }
         }

         return true;
      }

      private void mapShapelessRecipe(NonNullList<Ingredient> ingredients, IntFunction<ItemStack> itemGetter) {
         Int2IntMap actualLookup = new Int2IntArrayMap(QIOCraftingWindow.this.inputSlots.length);
         List<ItemStack> inputs = new ArrayList<>(QIOCraftingWindow.this.inputSlots.length);

         for (int index = 0; index < QIOCraftingWindow.this.inputSlots.length; index++) {
            ItemStack stack = itemGetter.apply(index);
            if (!stack.m_41619_()) {
               actualLookup.put(inputs.size(), index);
               inputs.add(stack);
            }
         }

         int[] matches = RecipeMatcher.findMatches(inputs, ingredients);
         if (matches != null) {
            for (int ingredientIndex = 0; ingredientIndex < matches.length; ingredientIndex++) {
               int actualSlot = actualLookup.getOrDefault(matches[ingredientIndex], -1);
               if (actualSlot == -1) {
                  this.invalid = true;
                  return;
               }

               this.slotIngredients.put(actualSlot, (Ingredient)ingredients.get(ingredientIndex));
            }
         } else {
            this.invalid = true;
         }
      }
   }
}
