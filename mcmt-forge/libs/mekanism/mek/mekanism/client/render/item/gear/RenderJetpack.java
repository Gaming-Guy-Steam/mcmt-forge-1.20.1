package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelJetpack;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderJetpack extends MekanismISTER {
   public static final RenderJetpack RENDERER = new RenderJetpack(false);
   public static final RenderJetpack ARMORED_RENDERER = new RenderJetpack(true);
   private final boolean armored;
   private ModelJetpack jetpack;

   private RenderJetpack(boolean armored) {
      this.armored = armored;
   }

   @Override
   public void m_6213_(@NotNull ResourceManager resourceManager) {
      if (this.armored) {
         this.jetpack = new ModelArmoredJetpack(this.getEntityModels());
      } else {
         this.jetpack = new ModelJetpack(this.getEntityModels());
      }
   }

   @Override
   public void m_108829_(
      @NotNull ItemStack stack,
      @NotNull ItemDisplayContext displayContext,
      @NotNull PoseStack matrix,
      @NotNull MultiBufferSource renderer,
      int light,
      int overlayLight
   ) {
      matrix.m_85836_();
      matrix.m_85837_(0.5, 0.5, 0.5);
      matrix.m_252781_(Axis.f_252403_.m_252977_(180.0F));
      this.jetpack.render(matrix, renderer, light, overlayLight, stack.m_41790_());
      matrix.m_85849_();
   }
}
