package mekanism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelPart.Polygon;
import net.minecraft.client.model.geom.ModelPart.Vertex;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class MekanismJavaModel extends Model {
   public MekanismJavaModel(Function<ResourceLocation, RenderType> renderType) {
      super(renderType);
   }

   protected static VertexConsumer getVertexConsumer(@NotNull MultiBufferSource renderer, @NotNull RenderType renderType, boolean hasEffect) {
      return ItemRenderer.m_115222_(renderer, renderType, false, hasEffect);
   }

   protected static void setRotation(ModelPart model, float x, float y, float z) {
      model.f_104203_ = x;
      model.f_104204_ = y;
      model.f_104205_ = z;
   }

   protected static void renderPartsToBuffer(
      List<ModelPart> parts,
      PoseStack poseStack,
      @NotNull VertexConsumer vertexConsumer,
      int light,
      int overlayLight,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      for (ModelPart part : parts) {
         part.m_104306_(poseStack, vertexConsumer, light, overlayLight, red, green, blue, alpha);
      }
   }

   protected static List<ModelPart> getRenderableParts(ModelPart root, ModelPartData... modelPartData) {
      List<ModelPart> parts = new ArrayList<>(modelPartData.length);

      for (ModelPartData partData : modelPartData) {
         parts.add(partData.getFromRoot(root));
      }

      return parts;
   }

   protected static LayerDefinition createLayerDefinition(int textureWidth, int textureHeight, ModelPartData... parts) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.m_171576_();

      for (ModelPartData part : parts) {
         part.addToDefinition(partdefinition);
      }

      return LayerDefinition.m_171565_(meshdefinition, textureWidth, textureHeight);
   }

   protected static void renderPartsAsWireFrame(
      List<ModelPart> parts, PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int red, int green, int blue, int alpha
   ) {
      for (ModelPart part : parts) {
         renderWireFrame(part, poseStack, vertexConsumer, red, green, blue, alpha);
      }
   }

   public static void renderWireFrame(ModelPart part, PoseStack matrix, VertexConsumer vertexConsumer, int red, int green, int blue, int alpha) {
      if (part.f_104207_) {
         part.m_171309_(matrix, (pose, name, cubeIndex, cube) -> {
            Matrix4f matrix4f = pose.m_252922_();

            for (Polygon quad : cube.f_104341_) {
               Vector3f normal = new Vector3f(quad.f_104360_);
               normal.mul(pose.m_252943_());
               float normalX = normal.x();
               float normalY = normal.y();
               float normalZ = normal.z();
               Vector4f vertex = getVertex(matrix4f, quad.f_104359_[0]);
               Vector4f vertex2 = getVertex(matrix4f, quad.f_104359_[1]);
               Vector4f vertex3 = getVertex(matrix4f, quad.f_104359_[2]);
               Vector4f vertex4 = getVertex(matrix4f, quad.f_104359_[3]);
               vertexConsumer.m_5483_(vertex.x(), vertex.y(), vertex.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex2.x(), vertex2.y(), vertex2.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex3.x(), vertex3.y(), vertex3.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex4.x(), vertex4.y(), vertex4.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex2.x(), vertex2.y(), vertex2.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex3.x(), vertex3.y(), vertex3.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex.x(), vertex.y(), vertex.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
               vertexConsumer.m_5483_(vertex4.x(), vertex4.y(), vertex4.z()).m_6122_(red, green, blue, alpha).m_5601_(normalX, normalY, normalZ).m_5752_();
            }
         });
      }
   }

   private static Vector4f getVertex(Matrix4f matrix4f, Vertex vertex) {
      Vector4f vector4f = new Vector4f(vertex.f_104371_.x() / 16.0F, vertex.f_104371_.y() / 16.0F, vertex.f_104371_.z() / 16.0F, 1.0F);
      return vector4f.mul(matrix4f);
   }
}
