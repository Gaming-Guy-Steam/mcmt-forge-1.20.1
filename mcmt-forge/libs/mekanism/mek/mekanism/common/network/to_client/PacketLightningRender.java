package mekanism.common.network.to_client;

import java.util.function.BooleanSupplier;
import mekanism.client.render.RenderTickHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketLightningRender implements IMekanismPacket {
   private final PacketLightningRender.LightningPreset preset;
   private final Vec3 start;
   private final Vec3 end;
   private final int renderer;
   private final int segments;

   public PacketLightningRender(PacketLightningRender.LightningPreset preset, int renderer, Vec3 start, Vec3 end, int segments) {
      this.preset = preset;
      this.renderer = renderer;
      this.start = start;
      this.end = end;
      this.segments = segments;
   }

   @Override
   public void handle(Context context) {
      if (this.preset.shouldAdd.getAsBoolean()) {
         RenderTickHandler.renderBolt(this.renderer, this.preset.boltCreator.create(this.start, this.end, this.segments));
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.preset);
      buffer.m_130130_(this.renderer);
      BasePacketHandler.writeVector3d(buffer, this.start);
      BasePacketHandler.writeVector3d(buffer, this.end);
      buffer.m_130130_(this.segments);
   }

   public static PacketLightningRender decode(FriendlyByteBuf buffer) {
      PacketLightningRender.LightningPreset preset = (PacketLightningRender.LightningPreset)buffer.m_130066_(PacketLightningRender.LightningPreset.class);
      int renderer = buffer.m_130242_();
      Vec3 start = BasePacketHandler.readVector3d(buffer);
      Vec3 end = BasePacketHandler.readVector3d(buffer);
      int segments = buffer.m_130242_();
      return new PacketLightningRender(preset, renderer, start, end, segments);
   }

   @FunctionalInterface
   public interface BoltCreator {
      BoltEffect create(Vec3 start, Vec3 end, int segments);
   }

   public static enum LightningPreset {
      MAGNETIC_ATTRACTION(
         MekanismConfig.client.renderMagneticAttractionParticles,
         (start, end, segments) -> new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, start, end, segments)
            .size(0.04F)
            .lifespan(8)
            .spawn(BoltEffect.SpawnFunction.noise(8.0F, 4.0F))
      ),
      TOOL_AOE(
         MekanismConfig.client.renderToolAOEParticles,
         (start, end, segments) -> new BoltEffect(BoltEffect.BoltRenderInfo.ELECTRICITY, start, end, segments)
            .size(0.015F)
            .lifespan(12)
            .spawn(BoltEffect.SpawnFunction.NO_DELAY)
      );

      private final BooleanSupplier shouldAdd;
      private final PacketLightningRender.BoltCreator boltCreator;

      private LightningPreset(BooleanSupplier shouldAdd, PacketLightningRender.BoltCreator boltCreator) {
         this.shouldAdd = shouldAdd;
         this.boltCreator = boltCreator;
      }
   }
}
