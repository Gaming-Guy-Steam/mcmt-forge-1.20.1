package mekanism.api.radial.mode;

import java.util.Objects;
import mekanism.api.radial.RadialData;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record NestedRadialMode(@NotNull RadialData<?> nestedData, @NotNull Component sliceName, @NotNull ResourceLocation icon, @Nullable EnumColor color)
   implements INestedRadialMode {
   public NestedRadialMode(@NotNull RadialData<?> nestedData, @NotNull Component sliceName, @NotNull ResourceLocation icon) {
      this(nestedData, sliceName, icon, null);
   }

   public NestedRadialMode(@NotNull RadialData<?> nestedData, @NotNull ILangEntry sliceName, @NotNull ResourceLocation icon, @NotNull EnumColor color) {
      this(nestedData, sliceName.translateColored(color), icon, color);
   }

   public NestedRadialMode(@NotNull RadialData<?> nestedData, @NotNull Component sliceName, @NotNull ResourceLocation icon, @Nullable EnumColor color) {
      Objects.requireNonNull(nestedData, "Nested data is required and cannot be null.");
      Objects.requireNonNull(sliceName, "Radial modes must have a slice name.");
      Objects.requireNonNull(icon, "Radial modes must have an icon to display.");
      this.nestedData = nestedData;
      this.sliceName = sliceName;
      this.icon = icon;
      this.color = color;
   }

   @Override
   public boolean hasNestedData() {
      return true;
   }
}
