package mekanism.client.model.energycube;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import mekanism.api.RelativeSide;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;

public class EnergyCubeGeometry implements IUnbakedGeometry<EnergyCubeGeometry> {
   private final List<BlockElement> frame;
   private final Map<RelativeSide, List<BlockElement>> leds;
   private final Map<RelativeSide, List<BlockElement>> ports;

   EnergyCubeGeometry(List<BlockElement> frame, Map<RelativeSide, List<BlockElement>> leds, Map<RelativeSide, List<BlockElement>> ports) {
      this.frame = frame;
      this.leds = leds;
      this.ports = ports;
   }

   public BakedModel bake(
      IGeometryBakingContext context,
      ModelBaker baker,
      Function<Material, TextureAtlasSprite> spriteGetter,
      ModelState modelState,
      ItemOverrides overrides,
      ResourceLocation modelLocation
   ) {
      TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
      ResourceLocation renderTypeHint = context.getRenderTypeHint();
      RenderTypeGroup renderTypes = renderTypeHint == null ? RenderTypeGroup.EMPTY : context.getRenderType(renderTypeHint);
      Transformation rootTransform = context.getRootTransform();
      if (!rootTransform.isIdentity()) {
         modelState = new SimpleModelState(modelState.m_6189_().m_121096_(rootTransform), modelState.m_7538_());
      }

      Function<String, TextureAtlasSprite> rawSpriteGetter = spriteGetter.compose(context::getMaterial);
      EnergyCubeGeometry.FaceData frame = this.bakeElement(rawSpriteGetter, modelState, modelLocation, this.frame);
      Map<RelativeSide, EnergyCubeGeometry.FaceData> leds = this.bakeElements(rawSpriteGetter, modelState, modelLocation, this.leds);
      Map<RelativeSide, EnergyCubeGeometry.FaceData> ports = this.bakeElements(rawSpriteGetter, modelState, modelLocation, this.ports);
      return new EnergyCubeBakedModel(
         context.useAmbientOcclusion(),
         context.useBlockLight(),
         context.isGui3d(),
         context.getTransforms(),
         overrides,
         particle,
         frame,
         leds,
         ports,
         renderTypes
      );
   }

   private Map<RelativeSide, EnergyCubeGeometry.FaceData> bakeElements(
      Function<String, TextureAtlasSprite> spriteGetter,
      ModelState modelState,
      ResourceLocation modelLocation,
      Map<RelativeSide, List<BlockElement>> sideBasedElements
   ) {
      Map<RelativeSide, EnergyCubeGeometry.FaceData> sideBasedFaceData = new EnumMap<>(RelativeSide.class);

      for (Entry<RelativeSide, List<BlockElement>> entry : sideBasedElements.entrySet()) {
         EnergyCubeGeometry.FaceData faceData = this.bakeElement(spriteGetter, modelState, modelLocation, entry.getValue());
         sideBasedFaceData.put(entry.getKey(), faceData);
      }

      return sideBasedFaceData;
   }

   private EnergyCubeGeometry.FaceData bakeElement(
      Function<String, TextureAtlasSprite> spriteGetter, ModelState modelState, ResourceLocation modelLocation, List<BlockElement> elements
   ) {
      EnergyCubeGeometry.FaceData data = new EnergyCubeGeometry.FaceData();

      for (BlockElement element : elements) {
         for (Entry<Direction, BlockElementFace> faceEntry : element.f_111310_.entrySet()) {
            BlockElementFace face = faceEntry.getValue();
            TextureAtlasSprite sprite = spriteGetter.apply(face.f_111356_);
            Direction direction = face.f_111354_ == null ? null : modelState.m_6189_().rotateTransform(face.f_111354_);
            data.addFace(direction, BlockModel.m_111437_(element, face, sprite, faceEntry.getKey(), modelState, modelLocation));
         }
      }

      return data;
   }

   static class FaceData {
      private List<BakedQuad> unculledFaces;
      private Map<Direction, List<BakedQuad>> culledFaces;

      public List<BakedQuad> getFaces(@Nullable Direction side) {
         if (side == null) {
            return this.unculledFaces == null ? Collections.emptyList() : this.unculledFaces;
         } else {
            return this.culledFaces == null ? Collections.emptyList() : this.culledFaces.getOrDefault(side, Collections.emptyList());
         }
      }

      public void addFace(@Nullable Direction direction, BakedQuad quad) {
         List<BakedQuad> quads;
         if (direction == null) {
            if (this.unculledFaces == null) {
               this.unculledFaces = new ArrayList<>();
            }

            quads = this.unculledFaces;
         } else {
            if (this.culledFaces == null) {
               this.culledFaces = new EnumMap<>(Direction.class);
            }

            quads = this.culledFaces.computeIfAbsent(direction, dir -> new ArrayList<>());
         }

         quads.add(quad);
      }

      public EnergyCubeGeometry.FaceData transform(QuadTransformation transformation) {
         if (this.unculledFaces == null && this.culledFaces == null) {
            return this;
         } else {
            EnergyCubeGeometry.FaceData transformed = new EnergyCubeGeometry.FaceData();
            if (this.unculledFaces != null) {
               transformed.unculledFaces = QuadUtils.transformBakedQuads(this.unculledFaces, transformation);
            }

            if (this.culledFaces != null) {
               transformed.culledFaces = new EnumMap<>(Direction.class);

               for (Entry<Direction, List<BakedQuad>> entry : this.culledFaces.entrySet()) {
                  transformed.culledFaces.put(entry.getKey(), QuadUtils.transformBakedQuads(entry.getValue(), transformation));
               }
            }

            return transformed;
         }
      }
   }
}
