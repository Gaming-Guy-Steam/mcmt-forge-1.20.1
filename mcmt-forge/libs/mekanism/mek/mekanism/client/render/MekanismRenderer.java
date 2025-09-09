package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.SupportsColorMap;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.model.baked.DigitalMinerBakedModel;
import mekanism.client.render.data.FluidRenderData;
import mekanism.client.render.data.ValveRenderData;
import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDimensionalStabilizer;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "mekanism",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class MekanismRenderer {
   public static TextureAtlasSprite energyIcon;
   public static TextureAtlasSprite heatIcon;
   public static TextureAtlasSprite whiteIcon;
   public static TextureAtlasSprite teleporterPortal;
   public static TextureAtlasSprite redstonePulse;
   public static final Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);

   public static TextureAtlasSprite getBaseFluidTexture(@NotNull Fluid fluid, @NotNull MekanismRenderer.FluidTextureType type) {
      IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluid);
      ResourceLocation spriteLocation;
      if (type == MekanismRenderer.FluidTextureType.STILL) {
         spriteLocation = properties.getStillTexture();
      } else {
         spriteLocation = properties.getFlowingTexture();
      }

      return getSprite(spriteLocation);
   }

   public static TextureAtlasSprite getFluidTexture(@NotNull FluidStack fluidStack, @NotNull MekanismRenderer.FluidTextureType type) {
      IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(fluidStack.getFluid());
      ResourceLocation spriteLocation;
      if (type == MekanismRenderer.FluidTextureType.STILL) {
         spriteLocation = properties.getStillTexture(fluidStack);
      } else {
         spriteLocation = properties.getFlowingTexture(fluidStack);
      }

      return getSprite(spriteLocation);
   }

   public static TextureAtlasSprite getChemicalTexture(@NotNull Chemical<?> chemical) {
      return getSprite(chemical.getIcon());
   }

   public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
      return (TextureAtlasSprite)Minecraft.m_91087_().m_91258_(TextureAtlas.f_118259_).apply(spriteLocation);
   }

   public static void renderObject(
      @Nullable MekanismRenderer.Model3D object,
      @NotNull PoseStack matrix,
      VertexConsumer buffer,
      int argb,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera,
      BlockPos renderPos
   ) {
      if (object != null) {
         renderObject(object, matrix, buffer, argb, light, overlay, faceDisplay, camera, Vec3.m_82528_(renderPos));
      }
   }

   public static void renderObject(
      @Nullable MekanismRenderer.Model3D object,
      @NotNull PoseStack matrix,
      VertexConsumer buffer,
      int argb,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera
   ) {
      renderObject(object, matrix, buffer, argb, light, overlay, faceDisplay, camera, (Vec3)null);
   }

   public static void renderObject(
      @Nullable MekanismRenderer.Model3D object,
      @NotNull PoseStack matrix,
      VertexConsumer buffer,
      int argb,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera,
      @Nullable Vec3 renderPos
   ) {
      if (object != null) {
         RenderResizableCuboid.renderCube(object, matrix, buffer, argb, light, overlay, faceDisplay, camera, renderPos);
      }
   }

   public static void renderObject(
      @Nullable MekanismRenderer.Model3D object,
      @NotNull PoseStack matrix,
      VertexConsumer buffer,
      int[] colors,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera
   ) {
      renderObject(object, matrix, buffer, colors, light, overlay, faceDisplay, camera, null);
   }

   public static void renderObject(
      @Nullable MekanismRenderer.Model3D object,
      @NotNull PoseStack matrix,
      VertexConsumer buffer,
      int[] colors,
      int light,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera,
      @Nullable Vec3 renderPos
   ) {
      if (object != null) {
         RenderResizableCuboid.renderCube(object, matrix, buffer, colors, light, overlay, faceDisplay, camera, renderPos);
      }
   }

   public static void renderValves(
      PoseStack matrix,
      VertexConsumer buffer,
      Set<IValveHandler.ValveData> valves,
      FluidRenderData data,
      float fluidHeight,
      BlockPos pos,
      int glow,
      int overlay,
      RenderResizableCuboid.FaceDisplay faceDisplay,
      Camera camera
   ) {
      for (IValveHandler.ValveData valveData : valves) {
         ValveRenderData valveRenderData = ValveRenderData.get(data, valveData);
         MekanismRenderer.Model3D valveModel = ModelRenderer.getValveModel(valveRenderData, fluidHeight);
         if (valveModel != null) {
            matrix.m_85836_();
            matrix.m_252880_(
               valveData.location.m_123341_() - pos.m_123341_(),
               valveData.location.m_123342_() - pos.m_123342_(),
               valveData.location.m_123343_() - pos.m_123343_()
            );
            renderObject(valveModel, matrix, buffer, valveRenderData.getColorARGB(), glow, overlay, faceDisplay, camera, valveData.location);
            matrix.m_85849_();
         }
      }
   }

   public static void resetColor(GuiGraphics guiGraphics) {
      guiGraphics.m_280246_(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static float getRed(int color) {
      return ARGB32.m_13665_(color) / 255.0F;
   }

   public static float getGreen(int color) {
      return ARGB32.m_13667_(color) / 255.0F;
   }

   public static float getBlue(int color) {
      return ARGB32.m_13669_(color) / 255.0F;
   }

   public static float getAlpha(int color) {
      return ARGB32.m_13655_(color) / 255.0F;
   }

   public static void color(GuiGraphics guiGraphics, int color) {
      guiGraphics.m_280246_(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
   }

   public static void color(GuiGraphics guiGraphics, ColorAtlas.ColorRegistryObject colorRO) {
      color(guiGraphics, colorRO.get());
   }

   public static void color(GuiGraphics guiGraphics, Color color) {
      guiGraphics.m_280246_(color.rf(), color.gf(), color.bf(), color.af());
   }

   public static void color(GuiGraphics guiGraphics, @NotNull FluidStack fluid) {
      if (!fluid.isEmpty()) {
         color(guiGraphics, IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid));
      }
   }

   public static void color(GuiGraphics guiGraphics, @NotNull ChemicalStack<?> chemicalStack) {
      if (!chemicalStack.isEmpty()) {
         color(guiGraphics, chemicalStack.getType());
      }
   }

   public static void color(GuiGraphics guiGraphics, @NotNull Chemical<?> chemical) {
      if (!chemical.isEmptyType()) {
         int color = chemical.getTint();
         guiGraphics.m_280246_(getRed(color), getGreen(color), getBlue(color), 1.0F);
      }
   }

   public static void color(GuiGraphics guiGraphics, @Nullable SupportsColorMap color) {
      color(guiGraphics, color, 1.0F);
   }

   public static void color(GuiGraphics guiGraphics, @Nullable SupportsColorMap color, float alpha) {
      if (color != null) {
         guiGraphics.m_280246_(color.getColor(0), color.getColor(1), color.getColor(2), alpha);
      }
   }

   public static int getColorARGB(SupportsColorMap color, float alpha) {
      return getColorARGB(color.getRgbCode()[0], color.getRgbCode()[1], color.getRgbCode()[2], alpha);
   }

   public static int getColorARGB(@NotNull FluidStack fluidStack) {
      return IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
   }

   public static int getColorARGB(@NotNull FluidStack fluidStack, float fluidScale) {
      if (fluidStack.isEmpty()) {
         return -1;
      } else {
         int color = getColorARGB(fluidStack);
         return MekanismUtils.lighterThanAirGas(fluidStack)
            ? getColorARGB(ARGB32.m_13665_(color), ARGB32.m_13667_(color), ARGB32.m_13669_(color), Math.min(1.0F, fluidScale + 0.2F))
            : color;
      }
   }

   public static int getColorARGB(@NotNull ChemicalStack<?> stack, float scale, boolean gaseous) {
      return getColorARGB(stack.getType(), scale, gaseous);
   }

   public static int getColorARGB(@NotNull Chemical<?> chemical, float scale, boolean gaseous) {
      if (chemical.isEmptyType()) {
         return -1;
      } else {
         int color = chemical.getTint();
         return getColorARGB(ARGB32.m_13665_(color), ARGB32.m_13667_(color), ARGB32.m_13669_(color), gaseous ? Math.min(1.0F, scale + 0.2F) : 1.0F);
      }
   }

   public static int getColorARGB(float red, float green, float blue, float alpha) {
      return getColorARGB((int)(255.0F * red), (int)(255.0F * green), (int)(255.0F * blue), alpha);
   }

   public static int getColorARGB(int red, int green, int blue, float alpha) {
      if (alpha < 0.0F) {
         alpha = 0.0F;
      } else if (alpha > 1.0F) {
         alpha = 1.0F;
      }

      return ARGB32.m_13660_((int)(255.0F * alpha), red, green, blue);
   }

   public static int calculateGlowLight(int combinedLight, @NotNull FluidStack fluid) {
      return fluid.isEmpty() ? combinedLight : calculateGlowLight(combinedLight, fluid.getFluid().getFluidType().getLightLevel(fluid));
   }

   public static int calculateGlowLight(int combinedLight, int glow) {
      return combinedLight & -65536 | Math.max(Math.min(glow, 15) << 4, combinedLight & 65535);
   }

   public static void renderColorOverlay(GuiGraphics guiGraphics, int x, int y, int color) {
      guiGraphics.m_285944_(RenderType.m_286086_(), x, y, guiGraphics.m_280182_(), guiGraphics.m_280206_(), color);
   }

   public static float getPartialTick() {
      return Minecraft.m_91087_().m_91296_();
   }

   public static void rotate(PoseStack matrix, Direction facing, float north, float south, float west, float east) {
      switch (facing) {
         case NORTH:
            matrix.m_252781_(Axis.f_252436_.m_252977_(north));
            break;
         case SOUTH:
            matrix.m_252781_(Axis.f_252436_.m_252977_(south));
            break;
         case WEST:
            matrix.m_252781_(Axis.f_252436_.m_252977_(west));
            break;
         case EAST:
            matrix.m_252781_(Axis.f_252436_.m_252977_(east));
      }
   }

   private static <T extends Enum<T> & SupportsColorMap> void parseColorAtlas(ResourceLocation rl, T[] elements) {
      List<Color> parsed = ColorAtlas.load(rl, elements.length);
      if (parsed.size() < elements.length) {
         Mekanism.logger.error("Failed to parse color atlas: {}.", rl);
      } else {
         for (int i = 0; i < elements.length; i++) {
            Color color = parsed.get(i);
            if (color != null) {
               elements[i].setColorFromAtlas(color.rgbArray());
            }
         }
      }
   }

   @SubscribeEvent
   public static void onStitch(Post event) {
      TextureAtlas map = event.getAtlas();
      if (map.m_118330_().equals(TextureAtlas.f_118259_)) {
         for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            overlays.put(type, map.m_118316_(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay")));
         }

         whiteIcon = map.m_118316_(Mekanism.rl("block/overlay/overlay_white"));
         energyIcon = map.m_118316_(Mekanism.rl("liquid/energy"));
         heatIcon = map.m_118316_(Mekanism.rl("liquid/heat"));
         redstonePulse = map.m_118316_(Mekanism.rl("icon/redstone_control_pulse"));
         teleporterPortal = map.m_118316_(Mekanism.rl("block/teleporter_portal"));
         DigitalMinerBakedModel.onStitch(event);
         RenderLogisticalTransporter.onStitch(map);
         RenderTransmitterBase.onStitch();
         ModelRenderer.resetCachedModels();
         RenderDigitalMiner.resetCachedVisuals();
         RenderDimensionalStabilizer.resetCachedVisuals();
         RenderFluidTank.resetCachedModels();
         RenderNutritionalLiquifier.resetCachedModels();
         RenderPigmentMixer.resetCached();
         RenderMechanicalPipe.onStitch();
         RenderSeismicVibrator.resetCached();
         RenderTickHandler.resetCached();
         RenderTeleporter.resetCachedModels();
         parseColorAtlas(Mekanism.rl("textures/colormap/primary.png"), EnumUtils.COLORS);
         parseColorAtlas(Mekanism.rl("textures/colormap/tiers.png"), EnumUtils.TIERS);
         SpecialColors.GUI_OBJECTS.parse(Mekanism.rl("textures/colormap/gui_objects.png"));
         SpecialColors.GUI_TEXT.parse(Mekanism.rl("textures/colormap/gui_text.png"));
         GuiElementHolder.updateBackgroundColor();
      }
   }

   public static enum FluidTextureType {
      STILL,
      FLOWING;
   }

   public static class LazyModel implements Supplier<MekanismRenderer.Model3D> {
      private final Supplier<MekanismRenderer.Model3D> supplier;
      @Nullable
      private MekanismRenderer.Model3D model;

      public LazyModel(Supplier<MekanismRenderer.Model3D> supplier) {
         this.supplier = supplier;
      }

      public void reset() {
         this.model = null;
      }

      public MekanismRenderer.Model3D get() {
         if (this.model == null) {
            this.model = this.supplier.get();
         }

         return this.model;
      }
   }

   public static class Model3D {
      public float minX;
      public float minY;
      public float minZ;
      public float maxX;
      public float maxY;
      public float maxZ;
      private final MekanismRenderer.Model3D.SpriteInfo[] textures = new MekanismRenderer.Model3D.SpriteInfo[6];
      private final boolean[] renderSides = new boolean[]{true, true, true, true, true, true};

      public MekanismRenderer.Model3D setSideRender(Predicate<Direction> shouldRender) {
         for (Direction direction : EnumUtils.DIRECTIONS) {
            this.setSideRender(direction, shouldRender.test(direction));
         }

         return this;
      }

      public MekanismRenderer.Model3D setSideRender(Direction side, boolean value) {
         this.renderSides[side.ordinal()] = value;
         return this;
      }

      public MekanismRenderer.Model3D copy() {
         MekanismRenderer.Model3D copy = new MekanismRenderer.Model3D();
         System.arraycopy(this.textures, 0, copy.textures, 0, this.textures.length);
         System.arraycopy(this.renderSides, 0, copy.renderSides, 0, this.renderSides.length);
         return copy.bounds(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
      }

      @Nullable
      public MekanismRenderer.Model3D.SpriteInfo getSpriteToRender(Direction side) {
         int ordinal = side.ordinal();
         return this.renderSides[ordinal] ? this.textures[ordinal] : null;
      }

      public MekanismRenderer.Model3D shrink(float amount) {
         return this.grow(-amount);
      }

      public MekanismRenderer.Model3D grow(float amount) {
         return this.bounds(this.minX - amount, this.minY - amount, this.minZ - amount, this.maxX + amount, this.maxY + amount, this.maxZ + amount);
      }

      public MekanismRenderer.Model3D xBounds(float min, float max) {
         this.minX = min;
         this.maxX = max;
         return this;
      }

      public MekanismRenderer.Model3D yBounds(float min, float max) {
         this.minY = min;
         this.maxY = max;
         return this;
      }

      public MekanismRenderer.Model3D zBounds(float min, float max) {
         this.minZ = min;
         this.maxZ = max;
         return this;
      }

      public MekanismRenderer.Model3D bounds(float min, float max) {
         return this.bounds(min, min, min, max, max, max);
      }

      public MekanismRenderer.Model3D bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
         return this.xBounds(minX, maxX).yBounds(minY, maxY).zBounds(minZ, maxZ);
      }

      public MekanismRenderer.Model3D prepSingleFaceModelSize(Direction face) {
         this.bounds(0.0F, 1.0F);

         return switch (face) {
            case NORTH -> this.zBounds(-0.01F, -0.001F);
            case SOUTH -> this.zBounds(1.001F, 1.01F);
            case WEST -> this.xBounds(-0.01F, -0.001F);
            case EAST -> this.xBounds(1.001F, 1.01F);
            case DOWN -> this.yBounds(-0.01F, -0.001F);
            case UP -> this.yBounds(1.001F, 1.01F);
            default -> throw new IncompatibleClassChangeError();
         };
      }

      public MekanismRenderer.Model3D prepFlowing(@NotNull FluidStack fluid) {
         MekanismRenderer.Model3D.SpriteInfo still = new MekanismRenderer.Model3D.SpriteInfo(
            MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.STILL), 16
         );
         MekanismRenderer.Model3D.SpriteInfo flowing = new MekanismRenderer.Model3D.SpriteInfo(
            MekanismRenderer.getFluidTexture(fluid, MekanismRenderer.FluidTextureType.FLOWING), 8
         );
         return this.setTextures(still, still, flowing, flowing, flowing, flowing);
      }

      public MekanismRenderer.Model3D setTexture(Direction side, @Nullable MekanismRenderer.Model3D.SpriteInfo spriteInfo) {
         this.textures[side.ordinal()] = spriteInfo;
         return this;
      }

      public MekanismRenderer.Model3D setTexture(TextureAtlasSprite tex) {
         return this.setTexture(tex, 16);
      }

      public MekanismRenderer.Model3D setTexture(TextureAtlasSprite tex, int size) {
         Arrays.fill(this.textures, new MekanismRenderer.Model3D.SpriteInfo(tex, size));
         return this;
      }

      public MekanismRenderer.Model3D setTextures(
         MekanismRenderer.Model3D.SpriteInfo down,
         MekanismRenderer.Model3D.SpriteInfo up,
         MekanismRenderer.Model3D.SpriteInfo north,
         MekanismRenderer.Model3D.SpriteInfo south,
         MekanismRenderer.Model3D.SpriteInfo west,
         MekanismRenderer.Model3D.SpriteInfo east
      ) {
         this.textures[0] = down;
         this.textures[1] = up;
         this.textures[2] = north;
         this.textures[3] = south;
         this.textures[4] = west;
         this.textures[5] = east;
         return this;
      }

      public interface ModelBoundsSetter {
         MekanismRenderer.Model3D set(float min, float max);
      }

      public record SpriteInfo(TextureAtlasSprite sprite, int size) {
         public float getU(float u) {
            return this.sprite.m_118367_(u * this.size);
         }

         public float getV(float v) {
            return this.sprite.m_118393_(v * this.size);
         }
      }
   }
}
