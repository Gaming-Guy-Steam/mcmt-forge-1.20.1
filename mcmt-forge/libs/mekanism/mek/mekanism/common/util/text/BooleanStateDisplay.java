package mekanism.common.util.text;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

@MethodsReturnNonnullByDefault
public abstract class BooleanStateDisplay implements IHasTextComponent {
   protected final boolean value;
   protected final boolean colored;

   protected BooleanStateDisplay(boolean value, boolean colored) {
      this.value = value;
      this.colored = colored;
   }

   protected abstract ILangEntry getLangEntry();

   @Override
   public Component getTextComponent() {
      return this.colored ? this.getLangEntry().translateColored(this.value ? EnumColor.BRIGHT_GREEN : EnumColor.RED) : this.getLangEntry().translate();
   }

   public static class ActiveDisabled extends BooleanStateDisplay {
      private ActiveDisabled(boolean value, boolean colored) {
         super(value, colored);
      }

      public static BooleanStateDisplay.ActiveDisabled of(boolean value) {
         return of(value, false);
      }

      public static BooleanStateDisplay.ActiveDisabled of(boolean value, boolean colored) {
         return new BooleanStateDisplay.ActiveDisabled(value, colored);
      }

      @Override
      protected ILangEntry getLangEntry() {
         return this.value ? MekanismLang.ACTIVE : MekanismLang.DISABLED;
      }
   }

   public static class InputOutput extends BooleanStateDisplay {
      private InputOutput(boolean value, boolean colored) {
         super(value, colored);
      }

      public static BooleanStateDisplay.InputOutput of(boolean value) {
         return of(value, false);
      }

      public static BooleanStateDisplay.InputOutput of(boolean value, boolean colored) {
         return new BooleanStateDisplay.InputOutput(value, colored);
      }

      @Override
      protected ILangEntry getLangEntry() {
         return this.value ? MekanismLang.INPUT : MekanismLang.OUTPUT;
      }
   }

   public static class OnOff extends BooleanStateDisplay {
      private final boolean caps;

      private OnOff(boolean value, boolean colored, boolean caps) {
         super(value, colored);
         this.caps = caps;
      }

      public static BooleanStateDisplay.OnOff of(boolean value) {
         return of(value, false);
      }

      public static BooleanStateDisplay.OnOff of(boolean value, boolean colored) {
         return new BooleanStateDisplay.OnOff(value, colored, false);
      }

      public static BooleanStateDisplay.OnOff caps(boolean value, boolean colored) {
         return new BooleanStateDisplay.OnOff(value, colored, true);
      }

      @Override
      protected ILangEntry getLangEntry() {
         return this.value ? (this.caps ? MekanismLang.ON_CAPS : MekanismLang.ON) : (this.caps ? MekanismLang.OFF_CAPS : MekanismLang.OFF);
      }
   }

   public static class YesNo extends BooleanStateDisplay {
      private YesNo(boolean value, boolean colored) {
         super(value, colored);
      }

      public static BooleanStateDisplay.YesNo of(boolean value) {
         return of(value, false);
      }

      public static BooleanStateDisplay.YesNo of(boolean value, boolean colored) {
         return new BooleanStateDisplay.YesNo(value, colored);
      }

      @Override
      protected ILangEntry getLangEntry() {
         return this.value ? MekanismLang.YES : MekanismLang.NO;
      }
   }
}
