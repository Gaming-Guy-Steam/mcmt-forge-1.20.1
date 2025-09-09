package mekanism.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
   modid = "mekanism",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class MekanismShaders {
   static final MekanismShaders.ShaderTracker MEKASUIT = new MekanismShaders.ShaderTracker();
   static final MekanismShaders.ShaderTracker SPS = new MekanismShaders.ShaderTracker();
   static final MekanismShaders.ShaderTracker FLAME = new MekanismShaders.ShaderTracker();

   @SubscribeEvent
   public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
      registerShader(event, Mekanism.rl("rendertype_flame"), DefaultVertexFormat.f_85818_, FLAME);
      registerShader(event, Mekanism.rl("rendertype_mekasuit"), DefaultVertexFormat.f_85812_, MEKASUIT);
      registerShader(event, Mekanism.rl("rendertype_sps"), DefaultVertexFormat.f_85818_, SPS);
   }

   private static void registerShader(
      RegisterShadersEvent event, ResourceLocation shaderLocation, VertexFormat vertexFormat, MekanismShaders.ShaderTracker tracker
   ) throws IOException {
      event.registerShader(new ShaderInstance(event.getResourceProvider(), shaderLocation, vertexFormat), tracker::setInstance);
   }

   static class ShaderTracker implements Supplier<ShaderInstance> {
      private ShaderInstance instance;
      final ShaderStateShard shard = new ShaderStateShard(this);

      private ShaderTracker() {
      }

      private void setInstance(ShaderInstance instance) {
         this.instance = instance;
      }

      public ShaderInstance get() {
         return this.instance;
      }
   }
}
