package mekanism.common.inventory.container.slot;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.BasicInventorySlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VirtualCraftingOutputSlot extends VirtualInventoryContainerSlot implements IHasExtraData {
   @NotNull
   private final QIOCraftingWindow craftingWindow;
   private boolean canCraft;
   private int amountCrafted;

   public VirtualCraftingOutputSlot(
      BasicInventorySlot slot, @Nullable SlotOverlay slotOverlay, Consumer<ItemStack> uncheckedSetter, @NotNull QIOCraftingWindow craftingWindow
   ) {
      super(slot, craftingWindow.getWindowData(), slotOverlay, uncheckedSetter);
      this.craftingWindow = craftingWindow;
   }

   @Override
   public boolean canMergeWith(@NotNull ItemStack stack) {
      return false;
   }

   @Override
   public boolean m_5857_(@NotNull ItemStack stack) {
      return false;
   }

   @NotNull
   @Override
   public ItemStack insertItem(@NotNull ItemStack stack, Action action) {
      return stack;
   }

   @NotNull
   @Override
   public ItemStack m_6201_(int amount) {
      if (amount == 0) {
         return ItemStack.f_41583_;
      } else {
         ItemStack extracted = this.getInventorySlot().getStack().m_41777_();
         this.amountCrafted = this.amountCrafted + extracted.m_41613_();
         return extracted;
      }
   }

   protected void m_7169_(@NotNull ItemStack stack, int amount) {
      this.amountCrafted += amount;
      this.m_5845_(stack);
   }

   protected void m_6405_(int numItemsCrafted) {
      this.amountCrafted += numItemsCrafted;
   }

   public void m_142406_(@NotNull Player player, @NotNull ItemStack stack) {
      ItemStack result = this.craftingWindow.performCraft(player, stack, this.amountCrafted);
      this.amountCrafted = 0;
   }

   @Override
   public boolean m_8010_(@NotNull Player player) {
      return !player.m_9236_().f_46443_ && player instanceof ServerPlayer serverPlayer
         ? this.craftingWindow.canViewRecipe(serverPlayer) && super.m_8010_(player)
         : this.canCraft && super.m_8010_(player);
   }

   @NotNull
   @Override
   public ItemStack m_7993_() {
      return this.canCraft ? super.m_7993_() : ItemStack.f_41583_;
   }

   @Override
   public boolean m_6657_() {
      return this.canCraft && super.m_6657_();
   }

   @NotNull
   @Override
   public ItemStack getStackToRender() {
      return this.canCraft ? super.getStackToRender() : ItemStack.f_41583_;
   }

   @NotNull
   public ItemStack shiftClickSlot(@NotNull Player player, List<HotBarSlot> hotBarSlots, List<MainInventorySlot> mainInventorySlots) {
      this.craftingWindow.performCraft(player, hotBarSlots, mainInventorySlots);
      return ItemStack.f_41583_;
   }

   @Override
   public void addTrackers(Player player, Consumer<ISyncableData> tracker) {
      if (!player.m_9236_().f_46443_ && player instanceof ServerPlayer serverPlayer) {
         tracker.accept(SyncableBoolean.create(() -> this.canCraft = this.craftingWindow.canViewRecipe(serverPlayer), value -> this.canCraft = value));
      } else {
         tracker.accept(SyncableBoolean.create(() -> this.canCraft, value -> this.canCraft = value));
      }
   }
}
