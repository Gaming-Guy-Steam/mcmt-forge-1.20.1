package mekanism.client.render.obj;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.Nullable;

public class TransmitterModel implements IUnbakedGeometry<TransmitterModel> {
   private final ObjModel internal;
   @Nullable
   private final ObjModel glass;

   public TransmitterModel(ObjModel internalModel, @Nullable ObjModel glass) {
      this.internal = internalModel;
      this.glass = glass;
   }

   public BakedModel bake(
      IGeometryBakingContext owner,
      ModelBaker baker,
      Function<Material, TextureAtlasSprite> spriteGetter,
      ModelState modelTransform,
      ItemOverrides overrides,
      ResourceLocation modelLocation
   ) {
      return new TransmitterBakedModel(this.internal, this.glass, owner, baker, spriteGetter, modelTransform, overrides, modelLocation);
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
      this.internal.resolveParents(modelGetter, context);
      if (this.glass != null) {
         this.glass.resolveParents(modelGetter, context);
      }
   }
}
