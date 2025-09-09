package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class TOPFluidElement extends FluidElement implements IElement {
   public TOPFluidElement(@NotNull FluidStack stored, int capacity) {
      super(stored, capacity);
   }

   public void toBytes(FriendlyByteBuf buf) {
      buf.writeFluidStack(this.stored);
      buf.m_130130_(this.capacity);
   }

   public ResourceLocation getID() {
      return LookingAtUtils.FLUID;
   }

   public static class Factory implements IElementFactory {
      public TOPFluidElement createElement(FriendlyByteBuf buf) {
         return new TOPFluidElement(buf.readFluidStack(), buf.m_130242_());
      }

      public ResourceLocation getId() {
         return LookingAtUtils.FLUID;
      }
   }
}
