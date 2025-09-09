package mekanism.api.security;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;

@NothingNullByDefault
public enum SecurityMode implements IIncrementalEnum<SecurityMode>, IHasTextComponent {
   PUBLIC(APILang.PUBLIC, EnumColor.BRIGHT_GREEN),
   PRIVATE(APILang.PRIVATE, EnumColor.RED),
   TRUSTED(APILang.TRUSTED, EnumColor.INDIGO);

   private static final SecurityMode[] MODES = values();
   private final ILangEntry langEntry;
   private final EnumColor color;

   private SecurityMode(ILangEntry langEntry, EnumColor color) {
      this.langEntry = langEntry;
      this.color = color;
   }

   @Override
   public Component getTextComponent() {
      return this.langEntry.translateColored(this.color);
   }

   public SecurityMode byIndex(int index) {
      return byIndexStatic(index);
   }

   public static SecurityMode byIndexStatic(int index) {
      return MathUtils.getByIndexMod(MODES, index);
   }
}
