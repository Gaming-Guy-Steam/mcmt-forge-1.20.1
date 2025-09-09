package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.slot.InventoryContainerSlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.warning.ISupportsWarning;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.RegistryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class BasicInventorySlot implements IInventorySlot {
   public static final Predicate<ItemStack> alwaysTrue = ConstantPredicates.alwaysTrue();
   public static final Predicate<ItemStack> alwaysFalse = ConstantPredicates.alwaysFalse();
   public static final BiPredicate<ItemStack, AutomationType> alwaysTrueBi = ConstantPredicates.alwaysTrueBi();
   public static final BiPredicate<ItemStack, AutomationType> manualOnly = (stack, automationType) -> automationType == AutomationType.MANUAL;
   public static final BiPredicate<ItemStack, AutomationType> internalOnly = ConstantPredicates.internalOnly();
   public static final BiPredicate<ItemStack, AutomationType> notExternal = ConstantPredicates.notExternal();
   public static final int DEFAULT_LIMIT = 64;
   protected ItemStack current = ItemStack.f_41583_;
   private final BiPredicate<ItemStack, AutomationType> canExtract;
   private final BiPredicate<ItemStack, AutomationType> canInsert;
   private final Predicate<ItemStack> validator;
   private final int limit;
   @Nullable
   private final IContentsListener listener;
   private final int x;
   private final int y;
   protected boolean obeyStackLimit = true;
   private ContainerSlotType slotType = ContainerSlotType.NORMAL;
   @Nullable
   private SlotOverlay slotOverlay;
   @Nullable
   private Consumer<ISupportsWarning<?>> warningAdder;

   public static BasicInventorySlot at(@Nullable IContentsListener listener, int x, int y) {
      return at(alwaysTrue, listener, x, y);
   }

   public static BasicInventorySlot at(Predicate<ItemStack> validator, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(validator, "Item validity check cannot be null");
      return new BasicInventorySlot(alwaysTrueBi, alwaysTrueBi, validator, listener, x, y);
   }

   public static BasicInventorySlot at(Predicate<ItemStack> canExtract, Predicate<ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      return new BasicInventorySlot(canExtract, canInsert, alwaysTrue, listener, x, y);
   }

   public static BasicInventorySlot at(
      BiPredicate<ItemStack, AutomationType> canExtract, BiPredicate<ItemStack, AutomationType> canInsert, @Nullable IContentsListener listener, int x, int y
   ) {
      Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
      Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
      return new BasicInventorySlot(canExtract, canInsert, alwaysTrue, listener, x, y);
   }

   protected BasicInventorySlot(
      Predicate<ItemStack> canExtract, Predicate<ItemStack> canInsert, Predicate<ItemStack> validator, @Nullable IContentsListener listener, int x, int y
   ) {
      this(
         (stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack),
         (stack, automationType) -> canInsert.test(stack),
         validator,
         listener,
         x,
         y
      );
   }

   protected BasicInventorySlot(
      BiPredicate<ItemStack, AutomationType> canExtract,
      BiPredicate<ItemStack, AutomationType> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      this(64, canExtract, canInsert, validator, listener, x, y);
   }

   protected BasicInventorySlot(
      int limit,
      BiPredicate<ItemStack, AutomationType> canExtract,
      BiPredicate<ItemStack, AutomationType> canInsert,
      Predicate<ItemStack> validator,
      @Nullable IContentsListener listener,
      int x,
      int y
   ) {
      this.limit = limit;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
      this.validator = validator;
      this.listener = listener;
      this.x = x;
      this.y = y;
   }

   @Override
   public ItemStack getStack() {
      return this.current;
   }

   @Override
   public void setStack(ItemStack stack) {
      this.setStack(stack, true);
   }

   protected void setStackUnchecked(ItemStack stack) {
      this.setStack(stack, false);
   }

   private void setStack(ItemStack stack, boolean validateStack) {
      if (stack.m_41619_()) {
         if (this.current.m_41619_()) {
            return;
         }

         this.current = ItemStack.f_41583_;
      } else {
         if (validateStack && !this.isItemValid(stack)) {
            throw new RuntimeException("Invalid stack for slot: " + RegistryUtils.getName(stack.m_41720_()) + " " + stack.m_41613_() + " " + stack.m_41783_());
         }

         this.current = stack.m_41777_();
      }

      this.onContentsChanged();
   }

   @Override
   public ItemStack insertItem(ItemStack stack, Action action, AutomationType automationType) {
      if (!stack.m_41619_() && this.isItemValid(stack) && this.canInsert.test(stack, automationType)) {
         int needed = this.getLimit(stack) - this.getCount();
         if (needed <= 0) {
            return stack;
         } else {
            boolean sameType = false;
            if (!this.isEmpty() && !(sameType = ItemHandlerHelper.canItemStacksStack(this.current, stack))) {
               return stack;
            } else {
               int toAdd = Math.min(stack.m_41613_(), needed);
               if (action.execute()) {
                  if (sameType) {
                     this.current.m_41769_(toAdd);
                     this.onContentsChanged();
                  } else {
                     this.setStackUnchecked(stack.m_255036_(toAdd));
                  }
               }

               return stack.m_255036_(stack.m_41613_() - toAdd);
            }
         }
      } else {
         return stack;
      }
   }

   @Override
   public ItemStack extractItem(int amount, Action action, AutomationType automationType) {
      if (!this.isEmpty() && amount >= 1 && this.canExtract.test(this.current, automationType)) {
         int currentAmount = Math.min(this.getCount(), this.current.m_41741_());
         if (currentAmount < amount) {
            amount = currentAmount;
         }

         ItemStack toReturn = this.current.m_255036_(amount);
         if (action.execute()) {
            this.current.m_41774_(amount);
            this.onContentsChanged();
         }

         return toReturn;
      } else {
         return ItemStack.f_41583_;
      }
   }

   @Override
   public int getLimit(ItemStack stack) {
      return this.obeyStackLimit && !stack.m_41619_() ? Math.min(this.limit, stack.m_41741_()) : this.limit;
   }

   @Override
   public boolean isItemValid(ItemStack stack) {
      return this.validator.test(stack);
   }

   public boolean isItemValidForInsertion(ItemStack stack, AutomationType automationType) {
      return this.validator.test(stack) && this.canInsert.test(stack, automationType);
   }

   @Override
   public void onContentsChanged() {
      if (this.listener != null) {
         this.listener.onContentsChanged();
      }
   }

   @Nullable
   public InventoryContainerSlot createContainerSlot() {
      return new InventoryContainerSlot(this, this.x, this.y, this.slotType, this.slotOverlay, this.warningAdder, this::setStackUnchecked);
   }

   public void setSlotType(ContainerSlotType slotType) {
      this.slotType = slotType;
   }

   public void tracksWarnings(@Nullable Consumer<ISupportsWarning<?>> warningAdder) {
      this.warningAdder = warningAdder;
   }

   public void setSlotOverlay(@Nullable SlotOverlay slotOverlay) {
      this.slotOverlay = slotOverlay;
   }

   @Nullable
   protected final SlotOverlay getSlotOverlay() {
      return this.slotOverlay;
   }

   protected final ContainerSlotType getSlotType() {
      return this.slotType;
   }

   @Override
   public int setStackSize(int amount, Action action) {
      if (this.isEmpty()) {
         return 0;
      } else if (amount <= 0) {
         if (action.execute()) {
            this.setEmpty();
         }

         return 0;
      } else {
         int maxStackSize = this.getLimit(this.current);
         if (amount > maxStackSize) {
            amount = maxStackSize;
         }

         if (this.getCount() != amount && !action.simulate()) {
            this.current.m_41764_(amount);
            this.onContentsChanged();
            return amount;
         } else {
            return amount;
         }
      }
   }

   @Override
   public int growStack(int amount, Action action) {
      int current = this.getCount();
      if (amount > 0) {
         amount = Math.min(amount, this.getLimit(this.current));
      }

      int newSize = this.setStackSize(current + amount, action);
      return newSize - current;
   }

   @Override
   public boolean isEmpty() {
      return this.current.m_41619_();
   }

   @Override
   public int getCount() {
      return this.current.m_41613_();
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      if (!this.isEmpty()) {
         nbt.m_128365_("Item", this.current.serializeNBT());
         if (this.getCount() > this.current.m_41741_()) {
            nbt.m_128405_("SizeOverride", this.getCount());
         }
      }

      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      ItemStack stack = ItemStack.f_41583_;
      if (nbt.m_128425_("Item", 10)) {
         stack = ItemStack.m_41712_(nbt.m_128469_("Item"));
         NBTUtils.setIntIfPresent(nbt, "SizeOverride", stack::m_41764_);
      }

      this.setStackUnchecked(stack);
   }
}
