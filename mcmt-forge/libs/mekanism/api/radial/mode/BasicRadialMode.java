package mekanism.api.radial.mode;

import java.util.Objects;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BasicRadialMode(@NotNull Component sliceName, @NotNull ResourceLocation icon, @Nullable EnumColor color) implements IRadialMode {
   public BasicRadialMode(@NotNull Component sliceName, @NotNull ResourceLocation icon) {
      this(sliceName, icon, null);
   }

   public BasicRadialMode(@NotNull ILangEntry sliceName, @NotNull ResourceLocation icon, @NotNull EnumColor color) {
      this(sliceName.translateColored(color), icon, color);
   }

   public BasicRadialMode(@NotNull Component sliceName, @NotNull ResourceLocation icon, @Nullable EnumColor color) {
      Objects.requireNonNull(sliceName, "Radial modes must have a slice name.");
      Objects.requireNonNull(icon, "Radial modes must have an icon to display.");
      this.sliceName = sliceName;
      this.icon = icon;
      this.color = color;
   }
}
