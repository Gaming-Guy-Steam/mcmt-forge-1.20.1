package mekanism.client.jei.interfaces;

import java.util.Optional;
import net.minecraft.client.renderer.Rect2i;

public interface IJEIIngredientHelper {
   Optional<?> getIngredient(double mouseX, double mouseY);

   Rect2i getIngredientBounds(double mouseX, double mouseY);
}
