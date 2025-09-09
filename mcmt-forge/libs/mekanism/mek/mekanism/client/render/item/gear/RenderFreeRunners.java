package mekanism.client.render.item.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.render.item.MekanismISTER;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderFreeRunners extends MekanismISTER {
   public static final RenderFreeRunners RENDERER = new RenderFreeRunners(false);
   public static final RenderFreeRunners ARMORED_RENDERER = new RenderFreeRunners(true);
   private final boolean armored;
   private ModelFreeRunners freeRunners;

   private RenderFreeRunners(boolean armored) {
      this.armored = armored;
   }

   @Override
   public void m_6213_(@NotNull ResourceManager resourceManager) {
      if (this.armored) {
         this.freeRunners = new ModelArmoredFreeRunners(this.getEntityModels());
      } else {
         this.freeRunners = new ModelFreeRunners(this.getEntityModels());
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
      matrix.m_252880_(0.0F, -1.0F, 0.0F);
      this.freeRunners.render(matrix, renderer, light, overlayLight, stack.m_41790_());
      matrix.m_85849_();
   }
}
