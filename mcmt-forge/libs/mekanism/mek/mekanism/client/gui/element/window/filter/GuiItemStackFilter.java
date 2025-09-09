package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.MekanismLang;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiItemStackFilter<FILTER extends IItemStackFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
   extends GuiFilter<FILTER, TILE> {
   protected GuiItemStackFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
      super(gui, x, y, width, height, MekanismLang.ITEM_FILTER.translate(new Object[0]), tile, origFilter);
   }

   @Override
   protected List<Component> getScreenText() {
      List<Component> list = super.getScreenText();
      if (this.filter.hasFilter()) {
         list.add(this.filter.getItemStack().m_41786_());
      }

      return list;
   }

   @Override
   protected ILangEntry getNoFilterSaveError() {
      return MekanismLang.ITEM_FILTER_NO_ITEM;
   }

   @NotNull
   @Override
   protected List<ItemStack> getRenderStacks() {
      ItemStack stack = this.filter.getItemStack();
      return stack.m_41619_() ? Collections.emptyList() : Collections.singletonList(stack);
   }

   @Nullable
   protected IJEIGhostTarget.IGhostItemConsumer getGhostHandler() {
      return ingredient -> this.setFilterStackWithSound(((ItemStack)ingredient).m_255036_(1));
   }

   @Nullable
   @Override
   protected GuiElement.IClickable getSlotClickHandler() {
      return getHandleClickSlot(this.gui(), NOT_EMPTY, this::setFilterStack);
   }

   private void setFilterStack(@NotNull ItemStack stack) {
      this.filter.setItemStack(stack);
      this.slotDisplay.updateStackList();
   }

   protected void setFilterStackWithSound(@NotNull ItemStack stack) {
      this.setFilterStack(stack);
      playClickSound(SoundEvents.f_12490_);
   }
}
