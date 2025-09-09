package mekanism.api.text;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

@MethodsReturnNonnullByDefault
public interface ILangEntry extends IHasTranslationKey {
   default MutableComponent translate(Object... args) {
      return TextComponentUtil.smartTranslate(this.getTranslationKey(), args);
   }

   default MutableComponent translateColored(EnumColor color, Object... args) {
      return this.translateColored(color.getColor(), args);
   }

   default MutableComponent translateColored(TextColor color, Object... args) {
      return TextComponentUtil.build(color, this.translate(args));
   }
}
