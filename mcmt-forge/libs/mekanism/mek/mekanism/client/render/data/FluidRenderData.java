package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;

@NothingNullByDefault
public class FluidRenderData extends RenderData {
   public final FluidStack fluidType;

   public FluidRenderData(BlockPos renderLocation, int width, int height, int length, FluidStack fluidType) {
      super(renderLocation, width, height, length);
      this.fluidType = fluidType;
   }

   @Override
   public boolean isGaseous() {
      return MekanismUtils.lighterThanAirGas(this.fluidType);
   }

   public int getColorARGB() {
      return MekanismRenderer.getColorARGB(this.fluidType);
   }

   @Override
   public int getColorARGB(float scale) {
      return MekanismRenderer.getColorARGB(this.fluidType, scale);
   }

   @Override
   public int calculateGlowLight(int light) {
      return MekanismRenderer.calculateGlowLight(light, this.fluidType);
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return MekanismRenderer.getFluidTexture(this.fluidType, MekanismRenderer.FluidTextureType.STILL);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.fluidType.getFluid(), this.fluidType.getTag());
   }

   @Override
   public boolean equals(Object data) {
      return super.equals(data) && data instanceof FluidRenderData other && this.fluidType.isFluidEqual(other.fluidType);
   }
}
