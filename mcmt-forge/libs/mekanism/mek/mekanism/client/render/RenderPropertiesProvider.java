package mekanism.client.render;

import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.armor.ICustomArmor;
import mekanism.client.render.armor.ISpecialGear;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.common.block.BlockBounding;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class RenderPropertiesProvider {
   private static final ISpecialGear MEKA_SUIT = type -> {
      return switch (type) {
         case HELMET -> MekaSuitArmor.HELMET;
         case CHESTPLATE -> MekaSuitArmor.BODYARMOR;
         case LEGGINGS -> MekaSuitArmor.PANTS;
         case BOOTS -> MekaSuitArmor.BOOTS;
         default -> throw new IncompatibleClassChangeError();
      };
   };
   private static final IClientBlockExtensions PARTICLE_HANDLER = new IClientBlockExtensions() {
      public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
         state.m_60808_(Level, pos)
            .m_83286_(
               (minX, minY, minZ, maxX, maxY, maxZ) -> {
                  double xDif = Math.min(1.0, maxX - minX);
                  double yDif = Math.min(1.0, maxY - minY);
                  double zDif = Math.min(1.0, maxZ - minZ);
                  int xCount = Mth.m_14165_(xDif / 0.25);
                  int yCount = Mth.m_14165_(yDif / 0.25);
                  int zCount = Mth.m_14165_(zDif / 0.25);
                  if (xCount > 0 && yCount > 0 && zCount > 0) {
                     for (int x = 0; x < xCount; x++) {
                        for (int y = 0; y < yCount; y++) {
                           for (int z = 0; z < zCount; z++) {
                              double d4 = (x + 0.5) / xCount;
                              double d5 = (y + 0.5) / yCount;
                              double d6 = (z + 0.5) / zCount;
                              double d7 = d4 * xDif + minX;
                              double d8 = d5 * yDif + minY;
                              double d9 = d6 * zDif + minZ;
                              manager.m_107344_(
                                 new TerrainParticle(
                                       (ClientLevel)Level,
                                       pos.m_123341_() + d7,
                                       pos.m_123342_() + d8,
                                       pos.m_123343_() + d9,
                                       d4 - 0.5,
                                       d5 - 0.5,
                                       d6 - 0.5,
                                       state
                                    )
                                    .updateSprite(state, pos)
                              );
                           }
                        }
                     }
                  }
               }
            );
         return true;
      }
   };

   private RenderPropertiesProvider() {
   }

   public static IClientItemExtensions energyCube() {
      return new RenderPropertiesProvider.MekRenderProperties(RenderEnergyCubeItem.RENDERER);
   }

   public static IClientItemExtensions fluidTank() {
      return new RenderPropertiesProvider.MekRenderProperties(RenderFluidTankItem.RENDERER);
   }

   public static IClientItemExtensions armoredJetpack() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderJetpack.ARMORED_RENDERER, JetpackArmor.ARMORED_JETPACK);
   }

   public static IClientItemExtensions jetpack() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderJetpack.RENDERER, JetpackArmor.JETPACK);
   }

   public static IClientItemExtensions disassembler() {
      return new RenderPropertiesProvider.MekRenderProperties(RenderAtomicDisassembler.RENDERER);
   }

   public static IClientItemExtensions flamethrower() {
      return new RenderPropertiesProvider.MekRenderProperties(RenderFlameThrower.RENDERER);
   }

   public static IClientItemExtensions armoredFreeRunners() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderFreeRunners.ARMORED_RENDERER, FreeRunnerArmor.ARMORED_FREE_RUNNERS);
   }

   public static IClientItemExtensions freeRunners() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderFreeRunners.RENDERER, FreeRunnerArmor.FREE_RUNNERS);
   }

   public static IClientItemExtensions scubaMask() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderScubaMask.RENDERER, ScubaMaskArmor.SCUBA_MASK);
   }

   public static IClientItemExtensions scubaTank() {
      return new RenderPropertiesProvider.MekCustomArmorRenderProperties(RenderScubaTank.RENDERER, ScubaTankArmor.SCUBA_TANK);
   }

   public static IClientItemExtensions mekaSuit() {
      return MEKA_SUIT;
   }

   public static IClientBlockExtensions particles() {
      return PARTICLE_HANDLER;
   }

   public static IClientBlockExtensions boundingParticles() {
      return new IClientBlockExtensions() {
         public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {
            if (target.m_6662_() == Type.BLOCK && target instanceof BlockHitResult blockTarget) {
               BlockPos pos = blockTarget.m_82425_();
               BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
               if (mainPos != null) {
                  BlockState mainState = world.m_8055_(mainPos);
                  if (!mainState.m_60795_()) {
                     AABB axisalignedbb = state.m_60808_(world, pos).m_83215_();
                     double x = pos.m_123341_()
                        + world.f_46441_.m_188500_() * (axisalignedbb.f_82291_ - axisalignedbb.f_82288_ - 0.2)
                        + 0.1
                        + axisalignedbb.f_82288_;
                     double y = pos.m_123342_()
                        + world.f_46441_.m_188500_() * (axisalignedbb.f_82292_ - axisalignedbb.f_82289_ - 0.2)
                        + 0.1
                        + axisalignedbb.f_82289_;
                     double z = pos.m_123343_()
                        + world.f_46441_.m_188500_() * (axisalignedbb.f_82293_ - axisalignedbb.f_82290_ - 0.2)
                        + 0.1
                        + axisalignedbb.f_82290_;
                     Direction side = blockTarget.m_82434_();
                     switch (side) {
                        case DOWN:
                           y = pos.m_123342_() + axisalignedbb.f_82289_ - 0.1;
                           break;
                        case UP:
                           y = pos.m_123342_() + axisalignedbb.f_82292_ + 0.1;
                           break;
                        case NORTH:
                           z = pos.m_123343_() + axisalignedbb.f_82290_ - 0.1;
                           break;
                        case SOUTH:
                           z = pos.m_123343_() + axisalignedbb.f_82293_ + 0.1;
                           break;
                        case WEST:
                           x = pos.m_123341_() + axisalignedbb.f_82288_ - 0.1;
                           break;
                        case EAST:
                           x = pos.m_123341_() + axisalignedbb.f_82291_ + 0.1;
                     }

                     manager.m_107344_(
                        new TerrainParticle((ClientLevel)world, x, y, z, 0.0, 0.0, 0.0, mainState)
                           .updateSprite(mainState, mainPos)
                           .m_107268_(0.2F)
                           .m_6569_(0.6F)
                     );
                     return true;
                  }
               }
            }

            return false;
         }
      };
   }

   public record MekCustomArmorRenderProperties(BlockEntityWithoutLevelRenderer renderer, ICustomArmor gearModel) implements ISpecialGear {
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
         return this.renderer;
      }

      @NotNull
      @Override
      public ICustomArmor getGearModel(net.minecraft.world.item.ArmorItem.Type type) {
         return this.gearModel;
      }
   }

   public record MekRenderProperties(BlockEntityWithoutLevelRenderer renderer) implements IClientItemExtensions {
      public BlockEntityWithoutLevelRenderer getCustomRenderer() {
         return this.renderer;
      }
   }
}
