package mekanism.api.gear;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public interface IHUDElement {
   @NotNull
   ResourceLocation getIcon();

   @NotNull
   Component getText();

   int getColor();

   public static enum HUDColor {
      REGULAR,
      FADED,
      WARNING,
      DANGER;
   }
}
