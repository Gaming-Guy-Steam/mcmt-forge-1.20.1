package mekanism.client.render.transmitter;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.EnumColor;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderResizableCuboid;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.transmitter.DiversionTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RenderLogisticalTransporter extends RenderTransmitterBase<TileEntityLogisticalTransporterBase> {
   private static final Map<Direction, MekanismRenderer.Model3D> cachedOverlays = new EnumMap<>(Direction.class);
   private static final int DIVERSION_OVERLAY_ARGB = MekanismRenderer.getColorARGB(255, 255, 255, 0.8F);
   @Nullable
   private static MekanismRenderer.Model3D.SpriteInfo gunpowderIcon;
   @Nullable
   private static MekanismRenderer.Model3D.SpriteInfo torchOffIcon;
   @Nullable
   private static MekanismRenderer.Model3D.SpriteInfo torchOnIcon;
   private final ModelTransporterBox modelBox;
   private final RenderLogisticalTransporter.LazyItemRenderer itemRenderer = new RenderLogisticalTransporter.LazyItemRenderer();

   public RenderLogisticalTransporter(Context context) {
      super(context);
      this.modelBox = new ModelTransporterBox(context.m_173585_());
   }

   public static void onStitch(TextureAtlas map) {
      cachedOverlays.clear();
      gunpowderIcon = new MekanismRenderer.Model3D.SpriteInfo(map.m_118316_(new ResourceLocation("minecraft", "item/gunpowder")), 16);
      torchOffIcon = new MekanismRenderer.Model3D.SpriteInfo(map.m_118316_(new ResourceLocation("minecraft", "block/redstone_torch_off")), 16);
      torchOnIcon = new MekanismRenderer.Model3D.SpriteInfo(map.m_118316_(new ResourceLocation("minecraft", "block/redstone_torch")), 16);
   }

   protected void render(
      TileEntityLogisticalTransporterBase tile,
      float partialTick,
      PoseStack matrix,
      MultiBufferSource renderer,
      int light,
      int overlayLight,
      ProfilerFiller profiler
   ) {
      LogisticalTransporterBase transporter = tile.getTransmitter();
      BlockPos pos = tile.m_58899_();
      if (!MekanismConfig.client.opaqueTransmitters.get()) {
         Collection<TransporterStack> inTransit = transporter.getTransit();
         if (!inTransit.isEmpty()) {
            matrix.m_85836_();
            this.itemRenderer.init(tile.m_58904_(), pos);
            float partial = partialTick * transporter.tier.getSpeed();

            for (TransporterStack stack : this.getReducedTransit(inTransit)) {
               float[] stackPos = TransporterUtils.getStackPosition(transporter, stack, partial);
               matrix.m_85836_();
               matrix.m_252880_(stackPos[0], stackPos[1], stackPos[2]);
               matrix.m_85841_(0.75F, 0.75F, 0.75F);
               this.itemRenderer.renderAsStack(matrix, renderer, stack.itemStack, light);
               matrix.m_85849_();
               if (stack.color != null) {
                  this.modelBox.render(matrix, renderer, 15728880, overlayLight, stackPos[0], stackPos[1], stackPos[2], stack.color);
               }
            }

            matrix.m_85849_();
         }
      }

      if (transporter instanceof DiversionTransporter diversionTransporter) {
         Player player = Minecraft.m_91087_().f_91074_;
         ItemStack itemStack = player.m_150109_().m_36056_();
         if (!itemStack.m_41619_() && itemStack.m_41720_() instanceof ItemConfigurator) {
            BlockHitResult rayTraceResult = MekanismUtils.rayTrace(player);
            if (rayTraceResult.m_6662_() != Type.MISS && rayTraceResult.m_82425_().equals(pos)) {
               Direction side = tile.getSideLookingAt(player, rayTraceResult.m_82434_());
               matrix.m_85836_();
               matrix.m_85841_(0.5F, 0.5F, 0.5F);
               matrix.m_85837_(0.5, 0.5, 0.5);
               MekanismRenderer.renderObject(
                  this.getOverlayModel(diversionTransporter, side),
                  matrix,
                  renderer.m_6299_(Sheets.m_110792_()),
                  DIVERSION_OVERLAY_ARGB,
                  15728880,
                  overlayLight,
                  RenderResizableCuboid.FaceDisplay.FRONT,
                  this.getCamera()
               );
               matrix.m_85849_();
            }
         }
      }
   }

   @Override
   protected String getProfilerSection() {
      return "logisticalTransporter";
   }

   protected boolean shouldRenderTransmitter(TileEntityLogisticalTransporterBase tile, Vec3 camera) {
      return super.shouldRenderTransmitter(tile, camera) || tile instanceof TileEntityDiversionTransporter;
   }

   private Collection<TransporterStack> getReducedTransit(Collection<TransporterStack> inTransit) {
      Collection<TransporterStack> reducedTransit = new ArrayList<>();
      Set<RenderLogisticalTransporter.TransportInformation> information = new ObjectOpenHashSet();

      for (TransporterStack stack : inTransit) {
         if (stack != null && !stack.itemStack.m_41619_() && information.add(new RenderLogisticalTransporter.TransportInformation(stack))) {
            reducedTransit.add(stack);
         }
      }

      return reducedTransit;
   }

   private MekanismRenderer.Model3D getOverlayModel(DiversionTransporter transporter, Direction side) {
      MekanismRenderer.Model3D model = cachedOverlays.computeIfAbsent(
         side, face -> new MekanismRenderer.Model3D().prepSingleFaceModelSize(face).setSideRender(direction -> direction == face)
      );

      return model.setTexture(side, switch (transporter.modes[side.ordinal()]) {
         case DISABLED -> gunpowderIcon;
         case HIGH -> torchOnIcon;
         case LOW -> torchOffIcon;
      });
   }

   private static class LazyItemRenderer {
      @Nullable
      private ItemEntity entityItem;
      @Nullable
      private EntityRenderer<? super ItemEntity> renderer;

      public void init(Level world, BlockPos pos) {
         if (this.entityItem == null) {
            this.entityItem = new ItemEntity(EntityType.f_20461_, world);
         } else {
            this.entityItem.m_284535_(world);
         }

         this.entityItem.m_6034_(pos.m_123341_() + 0.5, pos.m_123342_() + 0.5, pos.m_123343_() + 0.5);
         this.entityItem.f_31985_ = 0;
      }

      private void renderAsStack(PoseStack matrix, MultiBufferSource buffer, ItemStack stack, int light) {
         if (this.entityItem != null) {
            if (this.renderer == null) {
               this.renderer = Minecraft.m_91087_().m_91290_().m_114382_(this.entityItem);
            }

            this.entityItem.m_32045_(stack);
            this.renderer.m_7392_(this.entityItem, 0.0F, 0.0F, matrix, buffer, light);
         }
      }
   }

   private static class TransportInformation {
      @Nullable
      private final EnumColor color;
      private final HashedItem item;
      private final int progress;

      private TransportInformation(TransporterStack transporterStack) {
         this.progress = transporterStack.progress;
         this.color = transporterStack.color;
         this.item = HashedItem.create(transporterStack.itemStack);
      }

      @Override
      public int hashCode() {
         int code = 1;
         code = 31 * code + this.progress;
         code = 31 * code + this.item.hashCode();
         if (this.color != null) {
            code = 31 * code + this.color.hashCode();
         }

         return code;
      }

      @Override
      public boolean equals(Object obj) {
         return obj == this
            ? true
            : obj instanceof RenderLogisticalTransporter.TransportInformation other
               && this.progress == other.progress
               && this.color == other.color
               && this.item.equals(other.item);
      }
   }
}
