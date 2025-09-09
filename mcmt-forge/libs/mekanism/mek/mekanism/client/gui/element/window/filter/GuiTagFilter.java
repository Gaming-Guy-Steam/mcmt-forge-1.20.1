package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTagFilter<FILTER extends ITagFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
   extends GuiTextFilter<FILTER, TILE> {
   protected GuiTagFilter(IGuiWrapper gui, int x, int y, int width, int height, TILE tile, @Nullable FILTER origFilter) {
      super(gui, x, y, width, height, MekanismLang.TAG_FILTER.translate(new Object[0]), tile, origFilter);
   }

   @Override
   protected CharPredicate getInputValidator() {
      return InputValidator.RESOURCE_LOCATION.or(InputValidator.WILDCARD_CHARS);
   }

   @Override
   protected List<Component> getScreenText() {
      List<Component> list = super.getScreenText();
      list.add(MekanismLang.TAG_FILTER_TAG.translate(new Object[]{this.filter.getTagName()}));
      return list;
   }

   @Override
   protected ILangEntry getNoFilterSaveError() {
      return MekanismLang.TAG_FILTER_NO_TAG;
   }

   @Override
   protected boolean setText() {
      String name = this.text.getText();
      if (name.isEmpty()) {
         this.filterSaveFailed(this.getNoFilterSaveError(), new Object[0]);
      } else if (name.equals(this.filter.getTagName())) {
         this.filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG, new Object[0]);
      } else {
         if (this.hasMatchingTargets(name)) {
            this.filter.setTagName(name);
            this.slotDisplay.updateStackList();
            this.text.setText("");
            this.filterSaveSuccess();
            return true;
         }

         this.filterSaveFailed(MekanismLang.TEXT_FILTER_NO_MATCHES, new Object[0]);
      }

      return false;
   }

   protected boolean hasMatchingTargets(String name) {
      return !TagCache.getItemTagStacks(name).isEmpty();
   }

   @NotNull
   @Override
   protected List<ItemStack> getRenderStacks() {
      return this.filter.hasFilter() ? TagCache.getItemTagStacks(this.filter.getTagName()) : Collections.emptyList();
   }
}
