package mekanism.common.inventory.container.slot;

import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.inventory.warning.ISupportsWarning;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryContainerSlot extends Slot implements IInsertableSlot {
   private static final Container emptyInventory = new SimpleContainer(0);
   private final Consumer<ItemStack> uncheckedSetter;
   private final ContainerSlotType slotType;
   private final BasicInventorySlot slot;
   @Nullable
   private final SlotOverlay slotOverlay;
   @Nullable
   private final Consumer<ISupportsWarning<?>> warningAdder;

   public InventoryContainerSlot(
      BasicInventorySlot slot,
      int x,
      int y,
      ContainerSlotType slotType,
      @Nullable SlotOverlay slotOverlay,
      @Nullable Consumer<ISupportsWarning<?>> warningAdder,
      Consumer<ItemStack> uncheckedSetter
   ) {
      super(emptyInventory, 0, x, y);
      this.slot = slot;
      this.slotType = slotType;
      this.slotOverlay = slotOverlay;
      this.warningAdder = warningAdder;
      this.uncheckedSetter = uncheckedSetter;
   }

   public IInventorySlot getInventorySlot() {
      return this.slot;
   }

   public void addWarnings(ISupportsWarning<?> slot) {
      if (this.warningAdder != null) {
         this.warningAdder.accept(slot);
      }
   }

   @NotNull
   @Override
   public ItemStack insertItem(@NotNull ItemStack stack, Action action) {
      ItemStack remainder = this.slot.insertItem(stack, action, AutomationType.MANUAL);
      if (action.execute() && stack.m_41613_() != remainder.m_41613_()) {
         this.m_6654_();
      }

      return remainder;
   }

   public boolean m_5857_(@NotNull ItemStack stack) {
      if (stack.m_41619_()) {
         return false;
      } else if (this.slot.isEmpty()) {
         return this.insertItem(stack, Action.SIMULATE).m_41613_() < stack.m_41613_();
      } else {
         return this.slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).m_41619_()
            ? false
            : this.slot.isItemValidForInsertion(stack, AutomationType.MANUAL);
      }
   }

   @NotNull
   public ItemStack m_7993_() {
      return this.slot.getStack();
   }

   public boolean m_6657_() {
      return !this.slot.isEmpty();
   }

   public void m_5852_(@NotNull ItemStack stack) {
      this.uncheckedSetter.accept(stack);
      this.m_6654_();
   }

   public void m_6654_() {
      super.m_6654_();
      this.slot.onContentsChanged();
   }

   public int m_6641_() {
      return this.slot.getLimit(ItemStack.f_41583_);
   }

   public int m_5866_(@NotNull ItemStack stack) {
      return this.slot.getLimit(stack);
   }

   public boolean m_8010_(@NotNull Player player) {
      return !this.slot.extractItem(1, Action.SIMULATE, AutomationType.MANUAL).m_41619_();
   }

   @NotNull
   public ItemStack m_6201_(int amount) {
      return this.slot.extractItem(amount, Action.EXECUTE, AutomationType.MANUAL);
   }

   public ContainerSlotType getSlotType() {
      return this.slotType;
   }

   @Nullable
   public SlotOverlay getSlotOverlay() {
      return this.slotOverlay;
   }
}
