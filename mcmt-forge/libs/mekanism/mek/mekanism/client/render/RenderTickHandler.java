package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import mekanism.api.RelativeSide;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.GuiRadialSelector;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.client.event.RenderHighlightEvent.Block;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.ScreenEvent.Opening;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderTickHandler {
   public final Minecraft minecraft = Minecraft.m_91087_();
   private static final Map<BlockState, List<Vertex[]>> cachedWireFrames = new HashMap<>();
   private static final Map<Direction, Map<TransmissionType, MekanismRenderer.Model3D>> cachedOverlays = new EnumMap<>(Direction.class);
   private static final Map<RenderType, List<RenderTickHandler.LazyRender>> transparentRenderers = new HashMap<>();
   private static final BoltRenderer boltRenderer = new BoltRenderer();
   private boolean outliningArea = false;

   public static void clearQueued() {
      RadiationOverlay.INSTANCE.resetRadiation();
      transparentRenderers.clear();
   }

   public static void resetCached() {
      cachedOverlays.clear();
      cachedWireFrames.clear();
   }

   public static void renderBolt(Object renderer, BoltEffect bolt) {
      boltRenderer.update(renderer, bolt, MekanismRenderer.getPartialTick());
   }

   public static void guiOpening(Opening event) {
      if (event.getCurrentScreen() instanceof GuiMekanism<?> screen && event.getNewScreen() instanceof IRecipesGui) {
         screen.switchingToJEI = true;
      }
   }

   public static void addTransparentRenderer(RenderType renderType, RenderTickHandler.LazyRender render) {
      transparentRenderers.computeIfAbsent(renderType, r -> new ArrayList<>()).add(render);
   }

   @SubscribeEvent
   public void renderWorld(RenderLevelStageEvent event) {
      record TransparentRenderInfo(RenderType renderType, List<RenderTickHandler.LazyRender> renders, double closest) {
      }

      if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
         this.renderStage(event, !transparentRenderers.isEmpty(), (camera, renderer, poseStack, renderTick, partialTick) -> {
            ProfilerFiller profiler = this.minecraft.m_91307_();
            profiler.m_6180_("delayedMekanismTranslucentBERs");
            Consumer<TransparentRenderInfo> renderInfoConsumer = info -> {
               VertexConsumer buffer = renderer.m_6299_(info.renderType);

               for (RenderTickHandler.LazyRender transparentRender : info.renders) {
                  String profilerSection = transparentRender.getProfilerSection();
                  if (profilerSection != null) {
                     profiler.m_6180_(profilerSection);
                  }

                  transparentRender.render(camera, buffer, poseStack, renderTick, partialTick, profiler);
                  if (profilerSection != null) {
                     profiler.m_7238_();
                  }
               }

               renderer.m_109912_(info.renderType);
            };
            if (transparentRenderers.size() == 1) {
               for (Entry<RenderType, List<RenderTickHandler.LazyRender>> entry : transparentRenderers.entrySet()) {
                  renderInfoConsumer.accept(new TransparentRenderInfo(entry.getKey(), entry.getValue(), 0.0));
               }
            } else {
               transparentRenderers.entrySet().stream().map(entryx -> {
                  List<RenderTickHandler.LazyRender> renders = (List<RenderTickHandler.LazyRender>)entryx.getValue();
                  double closest = Double.MAX_VALUE;

                  for (RenderTickHandler.LazyRender render : renders) {
                     Vec3 renderPos = render.getCenterPos(partialTick);
                     if (renderPos != null) {
                        double distanceSqr = camera.m_90583_().m_82557_(renderPos);
                        if (distanceSqr < closest) {
                           closest = distanceSqr;
                        }
                     }
                  }

                  return new TransparentRenderInfo((RenderType)entryx.getKey(), renders, closest);
               }).sorted(Comparator.comparingDouble(info -> -info.closest)).forEachOrdered(renderInfoConsumer);
            }

            transparentRenderers.clear();
            profiler.m_7238_();
         });
      } else if (event.getStage() == Stage.AFTER_PARTICLES && boltRenderer.hasBoltsToRender()) {
         this.renderStage(event, boltRenderer.hasBoltsToRender(), (camera, renderer, poseStack, renderTick, partialTick) -> {
            boltRenderer.render(partialTick, poseStack, renderer);
            renderer.m_109912_(MekanismRenderType.MEK_LIGHTNING);
         });
      }
   }

   private void renderStage(RenderLevelStageEvent event, boolean shouldRender, RenderTickHandler.StageRenderer renderer) {
      if (shouldRender) {
         Camera camera = event.getCamera();
         PoseStack matrix = event.getPoseStack();
         matrix.m_85836_();
         Vec3 camVec = camera.m_90583_();
         matrix.m_85837_(-camVec.f_82479_, -camVec.f_82480_, -camVec.f_82481_);
         renderer.render(camera, this.minecraft.m_91269_().m_110104_(), matrix, event.getRenderTick(), event.getPartialTick());
         matrix.m_85849_();
      }
   }

   @SubscribeEvent
   public void renderCrosshair(Pre event) {
      if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()
         && this.minecraft.f_91080_ instanceof GuiRadialSelector screen
         && screen.shouldHideCrosshair()) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void renderArm(RenderArmEvent event) {
      AbstractClientPlayer player = event.getPlayer();
      ItemStack chestStack = player.m_6844_(EquipmentSlot.CHEST);
      if (chestStack.m_41720_() instanceof ItemMekaSuitArmor armorItem) {
         MekaSuitArmor armor = (MekaSuitArmor)((ISpecialGear)IClientItemExtensions.of(armorItem)).getGearModel(Type.CHESTPLATE);
         PlayerRenderer renderer = (PlayerRenderer)Minecraft.m_91087_().m_91290_().m_114382_(player);
         PlayerModel<AbstractClientPlayer> model = (PlayerModel<AbstractClientPlayer>)renderer.m_7200_();
         model.m_8009_(true);
         boolean rightHand = event.getArm() == HumanoidArm.RIGHT;
         if (rightHand) {
            model.f_102816_ = ArmPose.EMPTY;
         } else {
            model.f_102815_ = ArmPose.EMPTY;
         }

         model.f_102608_ = 0.0F;
         model.f_102817_ = false;
         model.f_102818_ = 0.0F;
         model.m_6973_(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
         armor.renderArm(
            model, event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), OverlayTexture.f_118083_, player, chestStack, rightHand
         );
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void tickEnd(RenderTickEvent event) {
      if (event.phase == Phase.END) {
         Player player = this.minecraft.f_91074_;
         Level world;
         if (player != null && (world = player.m_9236_()) != null && !this.minecraft.m_91104_() && this.minecraft.f_91072_ != null) {
            for (Player p : world.m_6907_()) {
               if (Mekanism.playerState.isJetpackOn(p)) {
                  Pos3D playerPos = new Pos3D(p).translate(0.0, p.m_20192_(), 0.0);
                  Vec3 playerMotion = p.m_20184_();
                  float random = (world.f_46441_.m_188501_() - 0.5F) * 0.1F;
                  float xRot;
                  if (p.m_6047_()) {
                     xRot = 20.0F;
                     playerPos = playerPos.translate(0.0, 0.125, 0.0);
                  } else {
                     float f = p.m_20998_(event.renderTickTime);
                     if (p.m_21255_()) {
                        float f1 = p.m_21256_() + event.renderTickTime;
                        float f2 = Mth.m_14036_(f1 * f1 / 100.0F, 0.0F, 1.0F);
                        xRot = f2 * (-90.0F - p.m_146909_());
                     } else {
                        float f3 = p.m_20069_() ? -90.0F - p.m_146909_() : -90.0F;
                        xRot = Mth.m_14179_(f, 0.0F, f3);
                     }

                     xRot = -xRot;
                     Pos3D eyeAdjustments;
                     if (!p.m_21255_() || p == player && this.minecraft.f_91066_.m_92176_().m_90612_()) {
                        if (p.m_6067_()) {
                           eyeAdjustments = new Pos3D(0.0, p.m_20192_(), 0.0).xRot(xRot).yRot(p.f_20883_).translate(0.0, 0.5, 0.0);
                        } else {
                           eyeAdjustments = new Pos3D(0.0, p.m_20192_(), 0.0).xRot(xRot).yRot(p.f_20883_);
                        }
                     } else {
                        eyeAdjustments = new Pos3D(0.0, p.m_20236_(Pose.STANDING), 0.0).xRot(xRot).yRot(p.f_20883_);
                     }

                     playerPos = new Pos3D(
                        p.m_20185_() + eyeAdjustments.f_82479_, p.m_20186_() + eyeAdjustments.f_82480_, p.m_20189_() + eyeAdjustments.f_82481_
                     );
                  }

                  Pos3D vLeft = new Pos3D(-0.43, -0.55, -0.54).xRot(xRot).yRot(p.f_20883_);
                  this.renderJetpackSmoke(world, playerPos.translate(vLeft, playerMotion), vLeft.scale(0.2).translate(playerMotion, vLeft.scale(random)));
                  Pos3D vRight = new Pos3D(0.43, -0.55, -0.54).xRot(xRot).yRot(p.f_20883_);
                  this.renderJetpackSmoke(world, playerPos.translate(vRight, playerMotion), vRight.scale(0.2).translate(playerMotion, vRight.scale(random)));
                  Pos3D vCenter = new Pos3D((world.f_46441_.m_188501_() - 0.5) * 0.4, -0.86, -0.3).xRot(xRot).yRot(p.f_20883_);
                  this.renderJetpackSmoke(world, playerPos.translate(vCenter, playerMotion), vCenter.scale(0.2).translate(playerMotion));
               }

               if (world.m_46467_() % 4L == 0L) {
                  if (p.m_20069_() && Mekanism.playerState.isScubaMaskOn(p)) {
                     Pos3D vec = new Pos3D(0.4, 0.4, 0.4).multiply(p.m_20252_(1.0F)).translate(0.0, -0.2, 0.0);
                     Pos3D motion = vec.scale(0.2).translate(p.m_20184_());
                     Pos3D v = new Pos3D(p).translate(0.0, p.m_20192_(), 0.0).translate(vec);
                     world.m_7106_(
                        (ParticleOptions)MekanismParticleTypes.SCUBA_BUBBLE.get(),
                        v.f_82479_,
                        v.f_82480_,
                        v.f_82481_,
                        motion.f_82479_,
                        motion.f_82480_ + 0.2,
                        motion.f_82481_
                     );
                  }

                  if (!p.f_20911_ && !Mekanism.playerState.isFlamethrowerOn(p)) {
                     ItemStack currentItem = p.m_21205_();
                     if (!currentItem.m_41619_() && currentItem.m_41720_() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem)) {
                        boolean rightHanded = p.m_5737_() == HumanoidArm.RIGHT;
                        Pos3D flameVec;
                        if (player == p && this.minecraft.f_91066_.m_92176_().m_90612_()) {
                           flameVec = new Pos3D(1.0, 1.0, 1.0)
                              .multiply(p.m_20252_(event.renderTickTime))
                              .yRot(rightHanded ? 15.0F : -15.0F)
                              .translate(0.0, p.m_20192_() - 0.1, 0.0);
                        } else {
                           double flameXCoord = rightHanded ? -0.2 : 0.2;
                           double flameYCoord = 1.0;
                           double flameZCoord = 1.2;
                           if (p.m_6047_()) {
                              flameYCoord -= 0.65;
                              flameZCoord -= 0.15;
                           }

                           flameVec = new Pos3D(flameXCoord, flameYCoord, flameZCoord).yRot(p.f_20883_);
                        }

                        Vec3 motion = p.m_20184_();
                        Vec3 flameMotion = new Vec3(motion.m_7096_(), p.m_20096_() ? 0.0 : motion.m_7098_(), motion.m_7094_());
                        Vec3 mergedVec = p.m_20182_().m_82549_(flameVec);
                        world.m_7106_(
                           (ParticleOptions)MekanismParticleTypes.JETPACK_FLAME.get(),
                           mergedVec.f_82479_,
                           mergedVec.f_82480_,
                           mergedVec.f_82481_,
                           flameMotion.f_82479_,
                           flameMotion.f_82480_,
                           flameMotion.f_82481_
                        );
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onBlockHover(Block event) {
      Player player = this.minecraft.f_91074_;
      if (player != null) {
         BlockHitResult rayTraceResult = event.getTarget();
         if (rayTraceResult.m_6662_() != net.minecraft.world.phys.HitResult.Type.MISS) {
            Level world = player.m_9236_();
            BlockPos pos = rayTraceResult.m_82425_();
            MultiBufferSource renderer = event.getMultiBufferSource();
            Camera info = event.getCamera();
            PoseStack matrix = event.getPoseStack();
            ProfilerFiller profiler = world.m_46473_();
            BlockState blockState = world.m_8055_(pos);
            profiler.m_6180_("areaMineOutline");
            if (!this.outliningArea) {
               ItemStack stack = player.m_21205_();
               if (!stack.m_41619_() && stack.m_41720_() instanceof IBlastingItem tool) {
                  Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(world, player, stack, pos, blockState);
                  if (!blocks.isEmpty()) {
                     this.outliningArea = true;
                     Vec3 renderView = info.m_90583_();
                     LevelRenderer levelRenderer = event.getLevelRenderer();
                     Lazy<VertexConsumer> lineConsumer = Lazy.of(() -> renderer.m_6299_(RenderType.m_110504_()));

                     for (Entry<BlockPos, BlockState> block : blocks.entrySet()) {
                        BlockPos blastingTarget = block.getKey();
                        if (!pos.equals(blastingTarget)
                           && !ForgeHooksClient.onDrawHighlight(levelRenderer, info, rayTraceResult, event.getPartialTick(), matrix, renderer)) {
                           levelRenderer.m_109637_(
                              matrix,
                              (VertexConsumer)lineConsumer.get(),
                              player,
                              renderView.f_82479_,
                              renderView.f_82480_,
                              renderView.f_82481_,
                              blastingTarget,
                              block.getValue()
                           );
                        }
                     }

                     this.outliningArea = false;
                  }
               }
            }

            profiler.m_7238_();
            boolean shouldCancel = false;
            profiler.m_6180_("mekOutline");
            if (!blockState.m_60795_() && world.m_6857_().m_61937_(pos)) {
               BlockPos actualPos = pos;
               BlockState actualState = blockState;
               if (blockState.m_60734_() instanceof BlockBounding) {
                  TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
                  if (tile != null && tile.hasReceivedCoords()) {
                     actualPos = tile.getMainPos();
                     actualState = world.m_8055_(actualPos);
                  }
               }

               AttributeCustomSelectionBox customSelectionBox = Attribute.get(actualState, AttributeCustomSelectionBox.class);
               if (customSelectionBox != null) {
                  RenderTickHandler.WireFrameRenderer renderWireFrame = null;
                  if (customSelectionBox.isJavaModel()) {
                     BlockEntity tile = WorldUtils.getTileEntity(world, actualPos);
                     if (tile != null
                        && Minecraft.m_91087_().m_167982_().m_112265_(tile) instanceof IWireFrameRenderer wireFrameRenderer
                        && wireFrameRenderer.hasSelectionBox(actualState)) {
                        renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) -> {
                           if (wireFrameRenderer.isCombined()) {
                              this.renderQuadsWireFrame(state, buffer, matrixStack.m_85850_().m_252922_(), world.f_46441_, red, green, blue, alpha);
                           }

                           wireFrameRenderer.renderWireFrame(tile, event.getPartialTick(), matrixStack, buffer, red, green, blue, alpha);
                        };
                     }
                  } else {
                     renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) -> this.renderQuadsWireFrame(
                        state, buffer, matrixStack.m_85850_().m_252922_(), world.f_46441_, red, green, blue, alpha
                     );
                  }

                  if (renderWireFrame != null) {
                     matrix.m_85836_();
                     Vec3 viewPosition = info.m_90583_();
                     matrix.m_85837_(
                        actualPos.m_123341_() - viewPosition.f_82479_,
                        actualPos.m_123342_() - viewPosition.f_82480_,
                        actualPos.m_123343_() - viewPosition.f_82481_
                     );
                     renderWireFrame.render(renderer.m_6299_(RenderType.m_110504_()), matrix, actualState, 0, 0, 0, 102);
                     matrix.m_85849_();
                     shouldCancel = true;
                  }
               }
            }

            profiler.m_7238_();
            ItemStack stack = player.m_21205_();
            if (stack.m_41619_() || !(stack.m_41720_() instanceof ItemConfigurator)) {
               stack = player.m_21206_();
               if (stack.m_41619_() || !(stack.m_41720_() instanceof ItemConfigurator)) {
                  if (shouldCancel) {
                     event.setCanceled(true);
                  }

                  return;
               }
            }

            profiler.m_6180_("configurableMachine");
            ItemConfigurator.ConfiguratorMode state = ((ItemConfigurator)stack.m_41720_()).getMode(stack);
            if (state.isConfigurating()) {
               TransmissionType type = Objects.requireNonNull(state.getTransmission(), "Configurating state requires transmission type");
               if (WorldUtils.getTileEntity(world, pos) instanceof ISideConfiguration configurable) {
                  TileComponentConfig config = configurable.getConfig();
                  if (config.supports(type)) {
                     Direction face = rayTraceResult.m_82434_();
                     DataType dataType = config.getDataType(type, RelativeSide.fromDirections(configurable.getDirection(), face));
                     if (dataType != null) {
                        Vec3 viewPosition = info.m_90583_();
                        matrix.m_85836_();
                        matrix.m_85837_(
                           pos.m_123341_() - viewPosition.f_82479_, pos.m_123342_() - viewPosition.f_82480_, pos.m_123343_() - viewPosition.f_82481_
                        );
                        MekanismRenderer.renderObject(
                           this.getOverlayModel(face, type),
                           matrix,
                           renderer.m_6299_(Sheets.m_110792_()),
                           MekanismRenderer.getColorARGB(dataType.getColor(), 0.6F),
                           15728880,
                           OverlayTexture.f_118083_,
                           RenderResizableCuboid.FaceDisplay.FRONT,
                           info
                        );
                        matrix.m_85849_();
                     }
                  }
               }
            }

            profiler.m_7238_();
            if (shouldCancel) {
               event.setCanceled(true);
            }
         }
      }
   }

   private void renderQuadsWireFrame(BlockState state, VertexConsumer buffer, Matrix4f matrix, RandomSource rand, int red, int green, int blue, int alpha) {
      List<Vertex[]> allVertices = cachedWireFrames.computeIfAbsent(state, s -> {
         BakedModel bakedModel = Minecraft.m_91087_().m_91289_().m_110910_(s);
         ModelData modelData = ModelData.EMPTY;
         List<Vertex[]> vertices = new ArrayList<>();

         for (Direction direction : EnumUtils.DIRECTIONS) {
            QuadUtils.unpack(bakedModel.getQuads(s, direction, rand, modelData, null)).stream().map(Quad::getVertices).forEach(vertices::add);
         }

         QuadUtils.unpack(bakedModel.getQuads(s, null, rand, modelData, null)).stream().map(Quad::getVertices).forEach(vertices::add);
         return vertices;
      });
      renderVertexWireFrame(allVertices, buffer, matrix, red, green, blue, alpha);
   }

   public static void renderVertexWireFrame(List<Vertex[]> allVertices, VertexConsumer buffer, Matrix4f matrix, int red, int green, int blue, int alpha) {
      for (Vertex[] vertices : allVertices) {
         Vector4f vertex = getVertex(matrix, vertices[0]);
         Vector3f normal = vertices[0].getNormal();
         Vector4f vertex2 = getVertex(matrix, vertices[1]);
         Vector3f normal2 = vertices[1].getNormal();
         Vector4f vertex3 = getVertex(matrix, vertices[2]);
         Vector3f normal3 = vertices[2].getNormal();
         Vector4f vertex4 = getVertex(matrix, vertices[3]);
         Vector3f normal4 = vertices[3].getNormal();
         buffer.m_5483_(vertex.x(), vertex.y(), vertex.z()).m_6122_(red, green, blue, alpha).m_5601_(normal.x(), normal.y(), normal.z()).m_5752_();
         buffer.m_5483_(vertex2.x(), vertex2.y(), vertex2.z()).m_6122_(red, green, blue, alpha).m_5601_(normal2.x(), normal2.y(), normal2.z()).m_5752_();
         buffer.m_5483_(vertex3.x(), vertex3.y(), vertex3.z()).m_6122_(red, green, blue, alpha).m_5601_(normal3.x(), normal3.y(), normal3.z()).m_5752_();
         buffer.m_5483_(vertex4.x(), vertex4.y(), vertex4.z()).m_6122_(red, green, blue, alpha).m_5601_(normal4.x(), normal4.y(), normal4.z()).m_5752_();
         buffer.m_5483_(vertex2.x(), vertex2.y(), vertex2.z()).m_6122_(red, green, blue, alpha).m_5601_(normal2.x(), normal2.y(), normal2.z()).m_5752_();
         buffer.m_5483_(vertex3.x(), vertex3.y(), vertex3.z()).m_6122_(red, green, blue, alpha).m_5601_(normal3.x(), normal3.y(), normal3.z()).m_5752_();
         buffer.m_5483_(vertex.x(), vertex.y(), vertex.z()).m_6122_(red, green, blue, alpha).m_5601_(normal.x(), normal.y(), normal.z()).m_5752_();
         buffer.m_5483_(vertex4.x(), vertex4.y(), vertex4.z()).m_6122_(red, green, blue, alpha).m_5601_(normal4.x(), normal4.y(), normal4.z()).m_5752_();
      }
   }

   private static Vector4f getVertex(Matrix4f matrix4f, Vertex vertex) {
      Vector4f vector4f = new Vector4f((float)vertex.getPos().m_7096_(), (float)vertex.getPos().m_7098_(), (float)vertex.getPos().m_7094_(), 1.0F);
      return vector4f.mul(matrix4f);
   }

   private void renderJetpackSmoke(Level world, Vec3 pos, Vec3 motion) {
      world.m_7106_(
         (ParticleOptions)MekanismParticleTypes.JETPACK_FLAME.get(),
         pos.f_82479_,
         pos.f_82480_,
         pos.f_82481_,
         motion.f_82479_,
         motion.f_82480_,
         motion.f_82481_
      );
      world.m_7106_(
         (ParticleOptions)MekanismParticleTypes.JETPACK_SMOKE.get(),
         pos.f_82479_,
         pos.f_82480_,
         pos.f_82481_,
         motion.f_82479_,
         motion.f_82480_,
         motion.f_82481_
      );
   }

   private MekanismRenderer.Model3D getOverlayModel(Direction side, TransmissionType type) {
      return cachedOverlays.computeIfAbsent(side, s -> new EnumMap<>(TransmissionType.class))
         .computeIfAbsent(type, t -> new MekanismRenderer.Model3D().setTexture(MekanismRenderer.overlays.get(t)).prepSingleFaceModelSize(side));
   }

   @FunctionalInterface
   public interface LazyRender {
      void render(Camera camera, VertexConsumer buffer, PoseStack poseStack, int renderTick, float partialTick, ProfilerFiller profiler);

      @Nullable
      default Vec3 getCenterPos(float partialTick) {
         return null;
      }

      @Nullable
      default String getProfilerSection() {
         return null;
      }
   }

   @FunctionalInterface
   private interface StageRenderer {
      void render(Camera camera, BufferSource renderer, PoseStack poseStack, int renderTick, float partialTick);
   }

   @FunctionalInterface
   private interface WireFrameRenderer {
      void render(VertexConsumer buffer, PoseStack matrix, BlockState state, int red, int green, int blue, int alpha);
   }
}
