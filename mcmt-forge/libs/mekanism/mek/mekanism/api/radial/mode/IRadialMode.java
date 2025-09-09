package mekanism.api.radial.mode;

import mekanism.api.text.EnumColor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRadialMode {
   @NotNull
   Component sliceName();

   @NotNull
   ResourceLocation icon();

   @Nullable
   default EnumColor color() {
      return null;
   }
}
