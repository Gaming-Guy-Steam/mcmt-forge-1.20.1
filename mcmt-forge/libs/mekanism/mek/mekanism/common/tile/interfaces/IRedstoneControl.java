package mekanism.common.tile.interfaces;

import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;

public interface IRedstoneControl {
   IRedstoneControl.RedstoneControl getControlType();

   void setControlType(IRedstoneControl.RedstoneControl type);

   boolean isPowered();

   boolean wasPowered();

   boolean canPulse();

   @NothingNullByDefault
   public static enum RedstoneControl implements IIncrementalEnum<IRedstoneControl.RedstoneControl>, IHasTextComponent {
      DISABLED(MekanismLang.REDSTONE_CONTROL_DISABLED),
      HIGH(MekanismLang.REDSTONE_CONTROL_HIGH),
      LOW(MekanismLang.REDSTONE_CONTROL_LOW),
      PULSE(MekanismLang.REDSTONE_CONTROL_PULSE);

      private static final IRedstoneControl.RedstoneControl[] MODES = values();
      private final ILangEntry langEntry;

      private RedstoneControl(ILangEntry langEntry) {
         this.langEntry = langEntry;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate();
      }

      public IRedstoneControl.RedstoneControl byIndex(int index) {
         return byIndexStatic(index);
      }

      public static IRedstoneControl.RedstoneControl byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
