package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;

@NothingNullByDefault
public class RenderPersonalChest extends MekanismTileEntityRenderer<TileEntityPersonalChest> {
   private static final ResourceLocation texture = MekanismUtils.getResource(MekanismUtils.ResourceType.TEXTURE_BLOCKS, "models/personal_chest.png");
   private final ModelPart lid;
   private final ModelPart bottom;
   private final ModelPart lock;

   public RenderPersonalChest(Context context) {
      super(context);
      ModelPart modelpart = context.m_173582_(ModelLayers.f_171275_);
      this.bottom = modelpart.m_171324_("bottom");
      this.lid = modelpart.m_171324_("lid");
      this.lock = modelpart.m_171324_("lock");
   }

   protected void render(
      TileEntityPersonalChest tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      matrix.m_85836_();
      if (!tile.m_58901_()) {
         matrix.m_85837_(0.5, 0.5, 0.5);
         matrix.m_252781_(Axis.f_252436_.m_252977_(-tile.getDirection().m_122435_()));
         matrix.m_85837_(-0.5, -0.5, -0.5);
      }

      float lidAngle = 1.0F - tile.m_6683_(partialTick);
      lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
      VertexConsumer builder = renderer.m_6299_(RenderType.m_110452_(texture));
      this.lid.f_104203_ = -(lidAngle * (float) (Math.PI / 2));
      this.lock.f_104203_ = this.lid.f_104203_;
      this.lid.m_104301_(matrix, builder, light, overlayLight);
      this.lock.m_104301_(matrix, builder, light, overlayLight);
      this.bottom.m_104301_(matrix, builder, light, overlayLight);
      matrix.m_85849_();
   }

   @Override
   protected String getProfilerSection() {
      return "personalChest";
   }
}
