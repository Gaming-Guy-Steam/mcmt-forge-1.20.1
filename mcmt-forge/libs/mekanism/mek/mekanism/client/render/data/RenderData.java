package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.lib.multiblock.MultiblockData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class RenderData {
   public final BlockPos location;
   public final int height;
   public final int length;
   public final int width;

   protected RenderData(BlockPos renderLocation, int width, int height, int length) {
      this.location = renderLocation;
      this.width = width;
      this.height = height;
      this.length = length;
   }

   public abstract TextureAtlasSprite getTexture();

   public abstract boolean isGaseous();

   public abstract int getColorARGB(float scale);

   public int calculateGlowLight(int light) {
      return light;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.height, this.length, this.width);
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof RenderData data && data.height == this.height && data.length == this.length && data.width == this.width;
   }

   public static class Builder<DATA_TYPE extends RenderData> {
      @Nullable
      private final Chemical<?> chemical;
      private final FluidStack fluid;
      @Nullable
      private BlockPos location;
      private int height;
      private int length;
      private int width;

      private Builder(@Nullable Chemical<?> chemical, FluidStack fluid) {
         this.chemical = chemical;
         this.fluid = fluid;
      }

      public static <CHEMICAL extends Chemical<CHEMICAL>> RenderData.Builder<ChemicalRenderData<CHEMICAL>> create(ChemicalStack<CHEMICAL> chemical) {
         if (chemical.isEmpty()) {
            throw new IllegalArgumentException("Chemical may not be empty");
         } else {
            return new RenderData.Builder<>(chemical.getType(), FluidStack.EMPTY);
         }
      }

      public static RenderData.Builder<FluidRenderData> create(FluidStack fluid) {
         if (fluid.isEmpty()) {
            throw new IllegalArgumentException("Fluid may not be empty");
         } else {
            return new RenderData.Builder<>(null, fluid);
         }
      }

      public RenderData.Builder<DATA_TYPE> location(BlockPos renderLocation) {
         this.location = renderLocation;
         return this;
      }

      public RenderData.Builder<DATA_TYPE> height(int height) {
         this.height = height;
         return this;
      }

      public RenderData.Builder<DATA_TYPE> length(int length) {
         this.length = length;
         return this;
      }

      public RenderData.Builder<DATA_TYPE> width(int width) {
         this.width = width;
         return this;
      }

      public RenderData.Builder<DATA_TYPE> dimensions(int width, int height, int length) {
         return this.width(width).height(height).length(length);
      }

      public RenderData.Builder<DATA_TYPE> of(MultiblockData multiblock) {
         return this.location(Objects.requireNonNull(multiblock.renderLocation, "Render location may not be null."))
            .height(multiblock.height() - 2)
            .length(multiblock.length())
            .width(multiblock.width());
      }

      public DATA_TYPE build() {
         if (this.location == null) {
            throw new IllegalStateException("Incomplete render data builder, no render location set.");
         } else {
            RenderData data;
            if (!this.fluid.isEmpty()) {
               data = new FluidRenderData(this.location, this.width, this.height, this.length, this.fluid);
            } else if (this.chemical instanceof Gas gas) {
               data = new ChemicalRenderData.GasRenderData(this.location, this.width, this.height, this.length, gas);
            } else if (this.chemical instanceof InfuseType infuseType) {
               data = new ChemicalRenderData.InfusionRenderData(this.location, this.width, this.height, this.length, infuseType);
            } else if (this.chemical instanceof Pigment pigment) {
               data = new ChemicalRenderData.PigmentRenderData(this.location, this.width, this.height, this.length, pigment);
            } else {
               if (!(this.chemical instanceof Slurry slurry)) {
                  throw new IllegalStateException("Incomplete render data builder, missing or unknown chemical or fluid.");
               }

               data = new ChemicalRenderData.SlurryRenderData(this.location, this.width, this.height, this.length, slurry);
            }

            return (DATA_TYPE)data;
         }
      }
   }
}
