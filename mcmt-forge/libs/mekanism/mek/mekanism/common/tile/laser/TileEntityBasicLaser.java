package mekanism.common.tile.laser;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.LaserEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.item.gear.ItemAtomicDisassembler;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.network.to_client.PacketLaserHitBlock;
import mekanism.common.particle.LaserParticleData;
import mekanism.common.registries.MekanismDamageTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityBasicLaser extends TileEntityMekanism {
   protected LaserEnergyContainer energyContainer;
   @SyntheticComputerMethod(
      getter = "getDiggingPos"
   )
   private BlockPos digging;
   private FloatingLong diggingProgress = FloatingLong.ZERO;
   private FloatingLong lastFired = FloatingLong.ZERO;

   public TileEntityBasicLaser(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
      super(blockProvider, pos, state);
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      this.addInitialEnergyContainers(builder, listener);
      return builder.build();
   }

   protected abstract void addInitialEnergyContainers(EnergyContainerHelper builder, IContentsListener listener);

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      FloatingLong firing = this.energyContainer.extract(this.toFire(), Action.SIMULATE, AutomationType.INTERNAL);
      if (!firing.isZero()) {
         if (!firing.equals(this.lastFired) || !this.getActive()) {
            this.setActive(true);
            this.lastFired = firing;
            this.sendUpdatePacket();
         }

         Direction direction = this.getDirection();
         Level level = this.getWorldNN();
         Pos3D from = Pos3D.create(this).centre().translate(direction, 0.501);
         Pos3D to = from.translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
         BlockHitResult result = level.m_45547_(new ClipContext(from, to, Block.OUTLINE, Fluid.NONE, null));
         if (result.m_6662_() != Type.MISS) {
            to = new Pos3D(result.m_82450_());
         }

         float laserEnergyScale = this.getEnergyScale(firing);
         FloatingLong remainingEnergy = firing.copy();
         List<Entity> hitEntities = level.m_45976_(Entity.class, Pos3D.getAABB(from, to));
         if (hitEntities.isEmpty()) {
            this.setEmittingRedstone(false);
         } else {
            this.setEmittingRedstone(true);
            hitEntities.sort(Comparator.comparing(entity -> entity.m_20238_(from)));
            FloatingLong energyPerDamage = MekanismConfig.general.laserEnergyPerDamage.get();

            for (Entity entity : hitEntities) {
               if (entity.m_6673_(MekanismDamageTypes.LASER.source(level))) {
                  remainingEnergy = FloatingLong.ZERO;
                  to = from.adjustPosition(direction, entity);
                  break;
               }

               if (!(entity instanceof ItemEntity item) || !this.handleHitItem(item)) {
                  boolean updateEnergyScale = false;
                  FloatingLong value = remainingEnergy.divide(energyPerDamage);
                  float damage = value.floatValue();
                  float health = 0.0F;
                  if (entity instanceof LivingEntity livingEntity) {
                     boolean updateDamage = false;
                     if (livingEntity.m_21254_() && livingEntity.m_21211_().canPerformAction(ToolActions.SHIELD_BLOCK)) {
                        Vec3 viewVector = livingEntity.m_20252_(1.0F);
                        Vec3 vectorTo = from.m_82505_(livingEntity.m_20182_()).m_82541_();
                        vectorTo = new Vec3(vectorTo.f_82479_, 0.0, vectorTo.f_82481_);
                        if (vectorTo.m_82526_(viewVector) < 0.0) {
                           float damageBlocked = this.damageShield(level, livingEntity, livingEntity.m_21211_(), damage, 2);
                           if (damageBlocked > 0.0F) {
                              if (livingEntity instanceof ServerPlayer player) {
                                 MekanismCriteriaTriggers.BLOCK_LASER.trigger(player);
                              }

                              remainingEnergy = remainingEnergy.minusEqual(energyPerDamage.multiply((double)damageBlocked));
                              if (remainingEnergy.isZero()) {
                                 to = from.adjustPosition(direction, entity);
                                 break;
                              }

                              updateDamage = true;
                           }
                        }
                     }

                     double dissipationPercent = 0.0;
                     double refractionPercent = 0.0;

                     for (ItemStack armor : livingEntity.m_6168_()) {
                        if (!armor.m_41619_()) {
                           Optional<ILaserDissipation> capability = armor.getCapability(Capabilities.LASER_DISSIPATION).resolve();
                           if (capability.isPresent()) {
                              ILaserDissipation laserDissipation = capability.get();
                              dissipationPercent += laserDissipation.getDissipationPercent();
                              refractionPercent += laserDissipation.getRefractionPercent();
                              if (dissipationPercent >= 1.0) {
                                 break;
                              }
                           }
                        }
                     }

                     if (dissipationPercent > 0.0) {
                        dissipationPercent = Math.min(dissipationPercent, 1.0);
                        remainingEnergy = remainingEnergy.timesEqual(FloatingLong.create(1.0 - dissipationPercent));
                        if (remainingEnergy.isZero()) {
                           to = from.adjustPosition(direction, entity);
                           break;
                        }

                        updateDamage = true;
                     }

                     if (refractionPercent > 0.0) {
                        refractionPercent = Math.min(refractionPercent, 1.0);
                        FloatingLong refractedEnergy = remainingEnergy.multiply(FloatingLong.create(refractionPercent));
                        value = remainingEnergy.subtract(refractedEnergy).divide(energyPerDamage);
                        damage = value.floatValue();
                        updateDamage = false;
                        updateEnergyScale = true;
                     }

                     if (updateDamage) {
                        value = remainingEnergy.divide(energyPerDamage);
                        damage = value.floatValue();
                     }

                     health = livingEntity.m_21223_();
                  }

                  if (damage > 0.0F) {
                     if (!entity.m_5825_()) {
                        entity.m_20254_(value.intValue());
                     }

                     int totemTimesUsed = -1;
                     if (entity instanceof ServerPlayer player) {
                        MinecraftServer server = entity.m_20194_();
                        if (server != null && server.m_7035_()) {
                           totemTimesUsed = player.m_8951_().m_13015_(Stats.f_12982_.m_12902_(Items.f_42747_));
                        }
                     }

                     boolean damaged = entity.m_6469_(MekanismDamageTypes.LASER.source(level), damage);
                     if (damaged) {
                        if (entity instanceof LivingEntity livingEntity) {
                           damage = Math.min(damage, Math.max(0.0F, health - livingEntity.m_21223_()));
                           if (entity instanceof ServerPlayer playerx) {
                              boolean hardcoreTotem = totemTimesUsed != -1
                                 && totemTimesUsed < playerx.m_8951_().m_13015_(Stats.f_12982_.m_12902_(Items.f_42747_));
                              MekanismCriteriaTriggers.DAMAGE.trigger(playerx, MekanismDamageTypes.LASER, hardcoreTotem);
                           }
                        }

                        remainingEnergy = remainingEnergy.minusEqual(energyPerDamage.multiply((double)damage));
                        if (remainingEnergy.isZero()) {
                           to = from.adjustPosition(direction, entity);
                           break;
                        }

                        updateEnergyScale = true;
                     }
                  }

                  if (updateEnergyScale) {
                     float energyScale = this.getEnergyScale(remainingEnergy);
                     if (laserEnergyScale - energyScale > 0.01) {
                        Pos3D entityPos = from.adjustPosition(direction, entity);
                        this.sendLaserDataToPlayers(new LaserParticleData(direction, entityPos.distance(from), laserEnergyScale), from);
                        laserEnergyScale = energyScale;
                        from = entityPos;
                     }
                  }
               }
            }
         }

         this.sendLaserDataToPlayers(new LaserParticleData(direction, to.distance(from), laserEnergyScale), from);
         if (!remainingEnergy.isZero() && result.m_6662_() != Type.MISS) {
            BlockPos hitPos = result.m_82425_();
            if (!hitPos.equals(this.digging)) {
               this.digging = result.m_6662_() == Type.MISS ? null : hitPos;
               this.diggingProgress = FloatingLong.ZERO;
            }

            Optional<ILaserReceptor> capability = CapabilityUtils.getCapability(
                  WorldUtils.getTileEntity(level, hitPos), Capabilities.LASER_RECEPTOR, result.m_82434_()
               )
               .resolve();
            if (capability.isPresent() && !capability.get().canLasersDig()) {
               capability.get().receiveLaserEnergy(remainingEnergy);
            } else {
               BlockState hitState = level.m_8055_(hitPos);
               float hardness = hitState.m_60800_(level, hitPos);
               if (hardness >= 0.0F) {
                  this.diggingProgress = this.diggingProgress.plusEqual(remainingEnergy);
                  if (this.diggingProgress.compareTo(MekanismConfig.general.laserEnergyNeededPerHardness.get().multiply((double)hardness)) >= 0) {
                     if (MekanismConfig.general.aestheticWorldDamage.get()) {
                        MekFakePlayer.withFakePlayer((ServerLevel)level, to.m_7096_(), to.m_7098_(), to.m_7094_(), dummy -> {
                           dummy.setEmulatingUUID(this.getOwnerUUID());
                           BreakEvent event = new BreakEvent(level, hitPos, hitState, dummy);
                           if (!MinecraftForge.EVENT_BUS.post(event)) {
                              if (hitState.m_60734_() instanceof TntBlock && hitState.isFlammable(level, hitPos, result.m_82434_())) {
                                 hitState.onCaughtFire(level, hitPos, result.m_82434_(), null);
                                 level.m_7471_(hitPos, false);
                              } else {
                                 this.handleBreakBlock(hitState, hitPos, dummy, ItemAtomicDisassembler.fullyChargedStack());
                              }
                           }

                           return null;
                        });
                     }

                     this.diggingProgress = FloatingLong.ZERO;
                  } else {
                     Mekanism.packetHandler().sendToAllTracking(new PacketLaserHitBlock(result), this);
                  }
               }
            }
         } else {
            this.digging = null;
            this.diggingProgress = FloatingLong.ZERO;
         }

         this.energyContainer.extract(firing, Action.EXECUTE, AutomationType.INTERNAL);
      } else if (this.getActive()) {
         this.setActive(false);
         if (!this.diggingProgress.isZero()) {
            this.diggingProgress = FloatingLong.ZERO;
         }

         if (!this.lastFired.isZero()) {
            this.lastFired = FloatingLong.ZERO;
            this.sendUpdatePacket();
         }
      }
   }

   private float damageShield(Level level, LivingEntity livingEntity, ItemStack activeStack, float damage, int absorptionRatio) {
      float damageBlocked = damage;
      float effectiveDamage = damage / absorptionRatio;
      if (effectiveDamage >= 1.0F) {
         ShieldBlockEvent event = ForgeHooks.onShieldBlock(livingEntity, MekanismDamageTypes.LASER.source(level), effectiveDamage);
         if (event.isCanceled()) {
            return 0.0F;
         }

         if (event.shieldTakesDamage()) {
            int durabilityNeeded = 1 + Mth.m_14143_(effectiveDamage);
            int activeDurability = activeStack.m_41776_() - activeStack.m_41773_();
            InteractionHand hand = livingEntity.m_7655_();
            activeStack.m_41622_(durabilityNeeded, livingEntity, entity -> {
               entity.m_21190_(hand);
               if (livingEntity instanceof Player playerx) {
                  ForgeEventFactory.onPlayerDestroyItem(playerx, activeStack, hand);
               }
            });
            if (activeStack.m_41619_()) {
               if (hand == InteractionHand.MAIN_HAND) {
                  livingEntity.m_8061_(EquipmentSlot.MAINHAND, ItemStack.f_41583_);
               } else {
                  livingEntity.m_8061_(EquipmentSlot.OFFHAND, ItemStack.f_41583_);
               }

               livingEntity.m_5810_();
               livingEntity.m_5496_(SoundEvents.f_12347_, 0.8F, 0.8F + 0.4F * level.f_46441_.m_188501_());
               int unblockedDamage = (durabilityNeeded - activeDurability) * absorptionRatio;
               damageBlocked = Math.max(0.0F, damage - unblockedDamage);
            }
         }
      }

      if (livingEntity instanceof ServerPlayer player && damageBlocked > 0.0F && damageBlocked < 3.4028235E37F) {
         player.m_36222_(Stats.f_12932_, Math.round(damageBlocked * 10.0F));
      }

      return damageBlocked;
   }

   private float getEnergyScale(FloatingLong energy) {
      return Math.min(energy.divide(MekanismConfig.usage.laser.get()).divide(10L).floatValue(), 0.6F);
   }

   private void sendLaserDataToPlayers(LaserParticleData data, Vec3 from) {
      if (!this.isRemote() && this.f_58857_ instanceof ServerLevel serverWorld) {
         for (ServerPlayer player : serverWorld.m_6907_()) {
            serverWorld.m_8624_(player, data, true, from.f_82479_, from.f_82480_, from.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }

   protected void setEmittingRedstone(boolean foundEntity) {
   }

   protected boolean handleHitItem(ItemEntity entity) {
      return false;
   }

   protected void handleBreakBlock(BlockState state, BlockPos hitPos, Player player, ItemStack tool) {
      net.minecraft.world.level.block.Block.dropResources(state, this.f_58857_, hitPos, WorldUtils.getTileEntity(this.f_58857_, hitPos), player, tool, false);
      this.breakBlock(state, hitPos);
   }

   protected final void breakBlock(BlockState state, BlockPos hitPos) {
      this.f_58857_.m_7471_(hitPos, false);
      this.f_58857_.m_220407_(GameEvent.f_157794_, hitPos, Context.m_223719_(null, state));
      this.f_58857_.m_46796_(2001, hitPos, net.minecraft.world.level.block.Block.m_49956_(state));
   }

   protected FloatingLong toFire() {
      return FloatingLong.MAX_VALUE;
   }

   @Override
   public void m_142466_(@NotNull CompoundTag nbt) {
      super.m_142466_(nbt);
      NBTUtils.setFloatingLongIfPresent(nbt, "lastFired", value -> this.lastFired = value);
   }

   @Override
   public void m_183515_(@NotNull CompoundTag nbtTags) {
      super.m_183515_(nbtTags);
      nbtTags.m_128359_("lastFired", this.lastFired.toString());
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      updateTag.m_128359_("lastFired", this.lastFired.toString());
      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      NBTUtils.setFloatingLongIfPresent(tag, "lastFired", fired -> this.lastFired = fired);
   }

   public LaserEnergyContainer getEnergyContainer() {
      return this.energyContainer;
   }
}
