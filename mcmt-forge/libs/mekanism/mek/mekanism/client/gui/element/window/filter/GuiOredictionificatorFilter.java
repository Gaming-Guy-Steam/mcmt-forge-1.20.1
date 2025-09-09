package mekanism.client.gui.element.window.filter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import mekanism.api.functions.CharPredicate;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.common.MekanismLang;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.InputValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiOredictionificatorFilter extends GuiTextFilter<OredictionificatorItemFilter, TileEntityOredictionificator> {
   public static GuiOredictionificatorFilter create(IGuiWrapper gui, TileEntityOredictionificator tile) {
      return new GuiOredictionificatorFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, null);
   }

   public static GuiOredictionificatorFilter edit(IGuiWrapper gui, TileEntityOredictionificator tile, OredictionificatorItemFilter filter) {
      return new GuiOredictionificatorFilter(gui, (gui.getWidth() - 152) / 2, 15, tile, filter);
   }

   private GuiOredictionificatorFilter(IGuiWrapper gui, int x, int y, TileEntityOredictionificator tile, @Nullable OredictionificatorItemFilter origFilter) {
      super(gui, x, y, 152, 100, MekanismLang.OREDICTIONIFICATOR_FILTER.translate(new Object[0]), tile, origFilter);
   }

   @Override
   protected int getScreenHeight() {
      return 53;
   }

   @Override
   protected int getSlotOffset() {
      return 32;
   }

   @Override
   protected void init() {
      super.init();
      this.addChild(new MekanismImageButton(this.gui(), this.relativeX + 10, this.relativeY + 18, 12, this.getButtonLocation("left"), () -> {
         if (this.filter.hasFilter()) {
            this.filter.previous();
            this.slotDisplay.updateStackList();
         }
      }, this.getOnHover(MekanismLang.LAST_ITEM)));
      this.addChild(new MekanismImageButton(this.gui(), this.relativeX + 10, this.relativeY + 52, 12, this.getButtonLocation("right"), () -> {
         if (this.filter.hasFilter()) {
            this.filter.next();
            this.slotDisplay.updateStackList();
         }
      }, this.getOnHover(MekanismLang.NEXT_ITEM)));
   }

   @Override
   protected CharPredicate getInputValidator() {
      return InputValidator.RESOURCE_LOCATION;
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
         return false;
      } else {
         String modid = "forge";
         String newFilter = name.toLowerCase(Locale.ROOT);
         if (newFilter.contains(":")) {
            String[] split = newFilter.split(":");
            modid = split[0];
            newFilter = split[1];
            if (modid.contains("/")) {
               this.filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_INVALID_NAMESPACE, new Object[0]);
               return false;
            }
         }

         if (newFilter.contains(":")) {
            this.filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_INVALID_PATH, new Object[0]);
            return false;
         } else {
            ResourceLocation filterLocation = new ResourceLocation(modid, newFilter);
            if (this.filter.hasFilter() && this.filter.filterMatches(filterLocation)) {
               this.filterSaveFailed(MekanismLang.TAG_FILTER_SAME_TAG, new Object[0]);
            } else {
               if (TileEntityOredictionificator.isValidTarget(filterLocation)) {
                  this.filter.setFilter(filterLocation);
                  this.slotDisplay.updateStackList();
                  this.text.setText("");
                  return true;
               }

               this.filterSaveFailed(MekanismLang.OREDICTIONIFICATOR_FILTER_UNSUPPORTED_TAG, new Object[0]);
            }

            return false;
         }
      }
   }

   @Override
   protected List<Component> getScreenText() {
      List<Component> list = super.getScreenText();
      if (this.filter.hasFilter()) {
         ItemStack renderStack = this.slotDisplay.getRenderStack();
         if (!renderStack.m_41619_()) {
            list.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translate(new Object[]{renderStack, MekanismUtils.getModId(renderStack)}));
         }

         list.add(TextComponentUtil.getString(this.filter.getFilterText()));
      }

      return list;
   }

   protected OredictionificatorItemFilter createNewFilter() {
      return new OredictionificatorItemFilter();
   }

   @NotNull
   @Override
   protected List<ItemStack> getRenderStacks() {
      ItemStack result = this.filter.getResult();
      return result.m_41619_() ? Collections.emptyList() : Collections.singletonList(result);
   }

   @Nullable
   public GuiFilterSelect<TileEntityOredictionificator> getFilterSelect(IGuiWrapper gui, TileEntityOredictionificator tileEntityOredictionificator) {
      return null;
   }

   @Override
   public boolean hasFilterSelect() {
      return false;
   }
}
