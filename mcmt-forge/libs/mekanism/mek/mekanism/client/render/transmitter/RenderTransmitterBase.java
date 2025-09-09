package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.obj.VisibleModelConfiguration;
import mekanism.client.render.tileentity.MekanismTileEntityRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelBakery.ModelBakerImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import org.joml.Vector3f;

@NothingNullByDefault
public abstract class RenderTransmitterBase<TRANSMITTER extends TileEntityTransmitter> extends MekanismTileEntityRenderer<TRANSMITTER> {
   public static final ResourceLocation MODEL_LOCATION = MekanismUtils.getResource(MekanismUtils.ResourceType.MODEL, "transmitter_contents.obj");
   private static final IGeometryBakingContext contentsConfiguration = StandaloneGeometryBakingContext.builder()
      .withGui3d(false)
      .withUseBlockLight(false)
      .withUseAmbientOcclusion(false)
      .build(Mekanism.rl("transmitter_contents"));
   private static final Map<RenderTransmitterBase.ContentsModelData, List<BakedQuad>> contentModelCache = new Object2ObjectOpenHashMap();
   private static final Vector3f NORMAL = (Vector3f)Util.m_137469_(new Vector3f(1.0F, 1.0F, 1.0F), Vector3f::normalize);

   public static void onStitch() {
      contentModelCache.clear();
   }

   private static List<BakedQuad> getBakedQuads(List<String> visible, TextureAtlasSprite icon, Level world) {
      return contentModelCache.computeIfAbsent(
         new RenderTransmitterBase.ContentsModelData(visible, icon),
         modelData -> {
            ModelBakery var10002 = Minecraft.m_91087_().m_91304_().getModelBakery();
            Objects.requireNonNull(var10002);
            ModelBaker baker = new ModelBakerImpl(var10002, (modelLoc, material) -> material.m_119204_(), MODEL_LOCATION);
            List<BakedQuad> bakedQuads = MekanismModelCache.INSTANCE
               .TRANSMITTER_CONTENTS
               .getModel()
               .bake(
                  new VisibleModelConfiguration(contentsConfiguration, modelData.visible),
                  baker,
                  material -> modelData.icon,
                  BlockModelRotation.X0_Y0,
                  ItemOverrides.f_111734_,
                  MODEL_LOCATION
               )
               .getQuads(null, null, world.m_213780_(), ModelData.EMPTY, null);
            List<Quad> unpackedQuads = QuadUtils.unpack(bakedQuads);

            for (Quad unpackedQuad : unpackedQuads) {
               unpackedQuad.vertexTransform(vertex -> vertex.normal(NORMAL));
            }

            return QuadUtils.bake(unpackedQuads);
         }
      );
   }

   protected RenderTransmitterBase(Context context) {
      super(context);
   }

   protected void renderModel(
      TRANSMITTER transmitter, PoseStack matrix, VertexConsumer builder, int rgb, float alpha, int light, int overlayLight, TextureAtlasSprite icon
   ) {
      this.renderModel(
         transmitter,
         matrix,
         builder,
         MekanismRenderer.getRed(rgb),
         MekanismRenderer.getGreen(rgb),
         MekanismRenderer.getBlue(rgb),
         alpha,
         light,
         overlayLight,
         icon,
         Arrays.stream(EnumUtils.DIRECTIONS)
            .map(side -> side.m_7912_() + transmitter.getTransmitter().getConnectionType(side).m_7912_().toUpperCase(Locale.ROOT))
            .toList()
      );
   }

   protected void renderModel(
      TRANSMITTER transmitter,
      PoseStack matrix,
      VertexConsumer builder,
      float red,
      float green,
      float blue,
      float alpha,
      int light,
      int overlayLight,
      TextureAtlasSprite icon,
      List<String> visible
   ) {
      if (!visible.isEmpty()) {
         Pose entry = matrix.m_85850_();

         for (BakedQuad quad : getBakedQuads(visible, icon, transmitter.m_58904_())) {
            builder.putBulkData(entry, quad, red, green, blue, alpha, light, overlayLight, false);
         }
      }
   }

   public boolean shouldRender(TRANSMITTER tile, Vec3 camera) {
      return this.shouldRenderTransmitter(tile, camera) && super.m_142756_(tile, camera);
   }

   protected boolean shouldRenderTransmitter(TRANSMITTER tile, Vec3 camera) {
      return !MekanismConfig.client.opaqueTransmitters.get();
   }

   private record ContentsModelData(List<String> visible, TextureAtlasSprite icon) {
   }
}
