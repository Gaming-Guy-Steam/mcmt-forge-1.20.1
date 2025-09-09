package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IFluidProvider extends IBaseProvider {
   Fluid getFluid();

   default FluidStack getFluidStack(int size) {
      return new FluidStack(this.getFluid(), size);
   }

   @Override
   default ResourceLocation getRegistryName() {
      return ForgeRegistries.FLUIDS.getKey(this.getFluid());
   }

   @Override
   default Component getTextComponent() {
      return this.getFluid().getFluidType().getDescription(this.getFluidStack(1));
   }

   @Override
   default String getTranslationKey() {
      return this.getFluid().getFluidType().getDescriptionId();
   }
}
