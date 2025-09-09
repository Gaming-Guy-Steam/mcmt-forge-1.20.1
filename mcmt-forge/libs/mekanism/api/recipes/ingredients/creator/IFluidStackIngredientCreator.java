package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IFluidProvider;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public interface IFluidStackIngredientCreator extends IIngredientCreator<Fluid, FluidStack, FluidStackIngredient> {
   default FluidStackIngredient from(IFluidProvider provider, int amount) {
      Objects.requireNonNull(provider, "FluidStackIngredients cannot be created from a null fluid provider.");
      return this.from(provider.getFluidStack(amount));
   }

   default FluidStackIngredient from(Fluid instance, int amount) {
      return this.from(new FluidStack(instance, amount));
   }
}
