package mekanism.client.render;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.RenderType.CompositeState.CompositeStateBuilder;
import net.minecraft.resources.ResourceLocation;

public class MekanismRenderType extends RenderType {
   private static final TransparencyStateShard BLADE_TRANSPARENCY = new TransparencyStateShard("mek_blade_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA);
   }, () -> {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
   });
   private static final TransparencyStateShard PARTICLE_TRANSPARENCY = new TransparencyStateShard("mek_particle_transparency", () -> {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
   }, RenderSystem::disableBlend);
   private static final ShaderStateShard PARTICLE_SHADER = new ShaderStateShard(GameRenderer::m_172829_);
   public static final RenderType MEK_LIGHTNING = m_173215_(
      "mek_lightning",
      DefaultVertexFormat.f_85815_,
      Mode.QUADS,
      256,
      false,
      true,
      CompositeState.m_110628_().m_173292_(f_173091_).m_110685_(f_110136_).m_110691_(false)
   );
   public static final RenderType MEK_GUI_FADE = m_173215_(
      "mek_gui_fade",
      DefaultVertexFormat.f_85815_,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.m_110628_().m_173292_(f_285573_).m_110685_(f_110139_).m_110663_(f_285579_).m_110691_(false)
   );
   public static final Function<ResourceLocation, RenderType> STANDARD = Util.m_143827_(
      resourceLocation -> createStandard("mek_standard", resourceLocation, UnaryOperator.identity(), false)
   );
   public static final Function<ResourceLocation, RenderType> STANDARD_TRANSLUCENT_TARGET = Util.m_143827_(
      resourceLocation -> createStandard("mek_standard_translucent_target", resourceLocation, state -> state.m_110675_(RenderType.f_110125_), true)
   );
   public static final Function<ResourceLocation, RenderType> ALARM = Util.m_143827_(
      resourceLocation -> createStandard("mek_alarm", resourceLocation, state -> state.m_110661_(f_110110_).m_110675_(RenderType.f_110125_), true)
   );
   public static final Function<ResourceLocation, RenderType> JETPACK_GLASS = Util.m_143827_(
      resourceLocation -> createStandard(
         "mek_jetpack_glass", resourceLocation, state -> state.m_173290_(new TextureStateShard(resourceLocation, true, false)), false
      )
   );
   public static final Function<ResourceLocation, RenderType> BLADE = Util.m_143827_(
      resourceLocation -> {
         CompositeState state = CompositeState.m_110628_()
            .m_173292_(f_173073_)
            .m_173290_(new TextureStateShard(resourceLocation, false, false))
            .m_110685_(BLADE_TRANSPARENCY)
            .m_110691_(true);
         return m_173215_("mek_blade", DefaultVertexFormat.f_85812_, Mode.QUADS, 256, true, false, state);
      }
   );
   public static final Function<ResourceLocation, RenderType> FLAME = Util.m_143827_(
      resourceLocation -> {
         CompositeState state = CompositeState.m_110628_()
            .m_173292_(MekanismShaders.FLAME.shard)
            .m_173290_(new TextureStateShard(resourceLocation, false, false))
            .m_110685_(f_110139_)
            .m_110691_(true);
         return m_173215_("mek_flame", DefaultVertexFormat.f_85818_, Mode.QUADS, 256, true, false, state);
      }
   );
   public static final RenderType NUTRITIONAL_PARTICLE = m_173215_(
      "mek_nutritional_particle",
      DefaultVertexFormat.f_85813_,
      Mode.QUADS,
      256,
      false,
      false,
      CompositeState.m_110628_().m_173292_(PARTICLE_SHADER).m_173290_(f_110146_).m_110685_(PARTICLE_TRANSPARENCY).m_110671_(f_110152_).m_110691_(false)
   );
   public static final RenderType MEKASUIT = m_173215_(
      "mekasuit",
      DefaultVertexFormat.f_85812_,
      Mode.QUADS,
      131072,
      true,
      false,
      CompositeState.m_110628_().m_173292_(MekanismShaders.MEKASUIT.shard).m_173290_(f_110146_).m_110671_(f_110152_).m_110677_(f_110154_).m_110691_(true)
   );
   public static final Function<ResourceLocation, RenderType> SPS = Util.m_143827_(
      resourceLocation -> {
         CompositeState state = CompositeState.m_110628_()
            .m_173292_(MekanismShaders.SPS.shard)
            .m_173290_(new TextureStateShard(resourceLocation, false, false))
            .m_110685_(f_110136_)
            .m_110675_(RenderType.f_110125_)
            .m_110691_(true);
         return m_173215_("mek_sps", DefaultVertexFormat.f_85818_, Mode.QUADS, 256, true, true, state);
      }
   );

   private MekanismRenderType(
      String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState
   ) {
      super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
   }

   private static RenderType createStandard(
      String name, ResourceLocation resourceLocation, UnaryOperator<CompositeStateBuilder> stateModifier, boolean sortOnUpload
   ) {
      CompositeState state = stateModifier.apply(
            CompositeState.m_110628_().m_173292_(f_173073_).m_173290_(new TextureStateShard(resourceLocation, false, false)).m_110685_(f_110139_)
         )
         .m_110691_(true);
      return m_173215_(name, DefaultVertexFormat.f_85812_, Mode.QUADS, 256, true, sortOnUpload, state);
   }
}
