package mekanism.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@NothingNullByDefault
public class RenderBin extends MekanismTileEntityRenderer<TileEntityBin> {
   private static final Matrix3f FAKE_NORMALS;

   public RenderBin(Context context) {
      super(context);
   }

   protected void render(
      TileEntityBin tile, float partialTick, PoseStack matrix, MultiBufferSource renderer, int light, int overlayLight, ProfilerFiller profiler
   ) {
      Level world = tile.m_58904_();
      BinInventorySlot binSlot = tile.getBinSlot();
      if (world != null && (!binSlot.isEmpty() || binSlot.isLocked())) {
         Direction facing = tile.getDirection();
         BlockPos coverPos = tile.m_58899_().m_121945_(facing);
         Optional<BlockState> blockState = WorldUtils.getBlockState(world, coverPos);
         if (blockState.isEmpty() || !blockState.get().m_60815_() || !blockState.get().m_60783_(world, coverPos, facing.m_122424_())) {
            matrix.m_85836_();
            matrix.m_85850_().m_252943_().set(FAKE_NORMALS);
            switch (facing) {
               case NORTH:
                  matrix.m_85837_(0.71, 0.8, -1.0E-4);
                  matrix.m_252781_(Axis.f_252436_.m_252977_(180.0F));
                  break;
               case SOUTH:
                  matrix.m_85837_(0.29, 0.8, 1.0001);
                  break;
               case WEST:
                  matrix.m_85837_(-1.0E-4, 0.8, 0.29);
                  matrix.m_252781_(Axis.f_252436_.m_252977_(-90.0F));
                  break;
               case EAST:
                  matrix.m_85837_(1.0001, 0.8, 0.71);
                  matrix.m_252781_(Axis.f_252436_.m_252977_(90.0F));
            }

            float scale = 0.025F;
            matrix.m_85841_(scale, scale, 1.0E-4F);
            matrix.m_252880_(8.0F, -8.0F, 8.0F);
            matrix.m_85841_(16.0F, 16.0F, 16.0F);
            light = LevelRenderer.m_109541_(world, tile.m_58899_().m_121945_(facing));
            Minecraft.m_91087_()
               .m_91291_()
               .m_269128_(
                  binSlot.getRenderStack(),
                  ItemDisplayContext.GUI,
                  light,
                  overlayLight,
                  matrix,
                  renderer,
                  world,
                  MathUtils.clampToInt(tile.m_58899_().m_121878_())
               );
            matrix.m_85849_();
            this.renderText(matrix, renderer, light, overlayLight, this.getCount(tile), facing, 0.02F);
         }
      }
   }

   protected Component getCount(TileEntityBin bin) {
      if (bin.getTier() == BinTier.CREATIVE) {
         return MekanismLang.INFINITE.translateColored(EnumColor.WHITE, new Object[0]);
      } else {
         BinInventorySlot slot = bin.getBinSlot();
         return TextComponentUtil.build(slot.isLocked() ? EnumColor.AQUA : EnumColor.WHITE, slot.getCount());
      }
   }

   @Override
   protected String getProfilerSection() {
      return "bin";
   }

   private void renderText(
      @NotNull PoseStack matrix, @NotNull MultiBufferSource renderer, int light, int overlayLight, Component text, Direction side, float maxScale
   ) {
      matrix.m_85836_();
      matrix.m_85837_(0.0, -0.25, 0.0);
      switch (side) {
         case NORTH:
            matrix.m_252880_(1.0F, 1.0F, 1.0F);
            matrix.m_252781_(Axis.f_252436_.m_252977_(180.0F));
            matrix.m_252781_(Axis.f_252529_.m_252977_(90.0F));
            break;
         case SOUTH:
            matrix.m_252880_(0.0F, 1.0F, 0.0F);
            matrix.m_252781_(Axis.f_252529_.m_252977_(90.0F));
            break;
         case WEST:
            matrix.m_252880_(1.0F, 1.0F, 0.0F);
            matrix.m_252781_(Axis.f_252436_.m_252977_(-90.0F));
            matrix.m_252781_(Axis.f_252529_.m_252977_(90.0F));
            break;
         case EAST:
            matrix.m_252880_(0.0F, 1.0F, 1.0F);
            matrix.m_252781_(Axis.f_252436_.m_252977_(90.0F));
            matrix.m_252781_(Axis.f_252529_.m_252977_(90.0F));
      }

      float displayWidth = 1.0F;
      float displayHeight = 1.0F;
      matrix.m_252880_(displayWidth / 2.0F, 1.0F, displayHeight / 2.0F);
      matrix.m_252781_(Axis.f_252529_.m_252977_(-90.0F));
      Font font = this.context.m_173586_();
      int requiredWidth = Math.max(font.m_92852_(text), 1);
      int requiredHeight = 9 + 2;
      float scaler = 0.4F;
      float scaleX = displayWidth / requiredWidth;
      float scale = scaleX * scaler;
      if (maxScale > 0.0F) {
         scale = Math.min(scale, maxScale);
      }

      matrix.m_85841_(scale, -scale, scale);
      int realHeight = (int)Math.floor(displayHeight / scale);
      int realWidth = (int)Math.floor(displayWidth / scale);
      int offsetX = (realWidth - requiredWidth) / 2;
      int offsetY = (realHeight - requiredHeight) / 2;
      font.m_272077_(
         text,
         offsetX - realWidth / 2,
         1 + offsetY - realHeight / 2,
         overlayLight,
         false,
         matrix.m_85850_().m_252922_(),
         renderer,
         DisplayMode.POLYGON_OFFSET,
         0,
         light
      );
      matrix.m_85849_();
   }

   static {
      Vector3f NORMAL = new Vector3f(1.0F, 1.0F, 1.0F);
      NORMAL.normalize();
      FAKE_NORMALS = new Matrix3f().set(new Quaternionf().setAngleAxis(0.0F, NORMAL.x, NORMAL.y, NORMAL.z));
   }
}
