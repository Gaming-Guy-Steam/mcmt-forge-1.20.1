package mekanism.client.gui.element.window.filter;

import java.util.Locale;
import mekanism.api.functions.CharPredicate;
import mekanism.api.functions.CharUnaryOperator;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.common.content.filter.IFilter;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public abstract class GuiTextFilter<FILTER extends IFilter<FILTER>, TILE extends TileEntityMekanism & ITileFilterHolder<? super FILTER>>
   extends GuiFilter<FILTER, TILE> {
   protected GuiTextField text;

   protected GuiTextFilter(IGuiWrapper gui, int x, int y, int width, int height, Component filterName, TILE tile, @Nullable FILTER origFilter) {
      super(gui, x, y, width, height, filterName, tile, origFilter);
   }

   @Override
   protected void init() {
      super.init();
      this.text = this.addChild(new GuiTextField(this.gui(), this.relativeX + 31, this.relativeY + 4 + this.getScreenHeight(), this.getScreenWidth() - 4, 12));
      this.text.setMaxLength(48);
      this.text
         .setInputValidator(this.getInputValidator())
         .setInputTransformer(this.getInputTransformer())
         .configureDigitalInput(this::setText)
         .setEditable(true);
      this.text.m_93692_(true);
   }

   @Override
   protected void validateAndSave() {
      if (this.text.getText().isEmpty() || this.setText()) {
         super.validateAndSave();
      }
   }

   protected abstract CharPredicate getInputValidator();

   @Nullable
   protected CharUnaryOperator getInputTransformer() {
      return c -> c >= 'A' && c <= 'Z' ? Character.toString(c).toLowerCase(Locale.ROOT).charAt(0) : c;
   }

   protected abstract boolean setText();
}
