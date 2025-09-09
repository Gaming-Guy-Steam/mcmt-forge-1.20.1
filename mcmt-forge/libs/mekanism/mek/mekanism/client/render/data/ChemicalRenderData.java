package mekanism.client.render.data;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalRenderData<CHEMICAL extends Chemical<CHEMICAL>> extends RenderData {
   public final CHEMICAL chemical;

   protected ChemicalRenderData(BlockPos renderLocation, int width, int height, int length, CHEMICAL chemical) {
      super(renderLocation, width, height, length);
      this.chemical = chemical;
   }

   @Override
   public int getColorARGB(float scale) {
      return MekanismRenderer.getColorARGB(this.chemical, scale, this.isGaseous());
   }

   @Override
   public TextureAtlasSprite getTexture() {
      return MekanismRenderer.getChemicalTexture(this.chemical);
   }

   @Override
   public boolean isGaseous() {
      return false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.chemical);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else {
         return o != null && this.getClass() == o.getClass() && super.equals(o) ? this.chemical == ((ChemicalRenderData)o).chemical : false;
      }
   }

   public static class GasRenderData extends ChemicalRenderData<Gas> {
      public GasRenderData(BlockPos renderLocation, int width, int height, int length, Gas gas) {
         super(renderLocation, width, height, length, gas);
      }

      @Override
      public boolean isGaseous() {
         return true;
      }
   }

   public static class InfusionRenderData extends ChemicalRenderData<InfuseType> {
      public InfusionRenderData(BlockPos renderLocation, int width, int height, int length, InfuseType infuseType) {
         super(renderLocation, width, height, length, infuseType);
      }
   }

   public static class PigmentRenderData extends ChemicalRenderData<Pigment> {
      public PigmentRenderData(BlockPos renderLocation, int width, int height, int length, Pigment pigment) {
         super(renderLocation, width, height, length, pigment);
      }
   }

   public static class SlurryRenderData extends ChemicalRenderData<Slurry> {
      public SlurryRenderData(BlockPos renderLocation, int width, int height, int length, Slurry slurry) {
         super(renderLocation, width, height, length, slurry);
      }
   }
}
