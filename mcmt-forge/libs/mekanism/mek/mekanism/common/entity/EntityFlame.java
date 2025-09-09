package mekanism.common.entity;

import java.util.Optional;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityFlame extends Projectile implements IEntityAdditionalSpawnData {
   public static final int LIFESPAN = 80;
   private static final int DAMAGE = 10;
   private ItemFlamethrower.FlamethrowerMode mode = ItemFlamethrower.FlamethrowerMode.COMBAT;

   public EntityFlame(EntityType<EntityFlame> type, Level world) {
      super(type, world);
   }

   @Nullable
   public static EntityFlame create(Player player) {
      EntityFlame flame = (EntityFlame)((EntityType)MekanismEntityTypes.FLAME.get()).m_20615_(player.m_9236_());
      if (flame == null) {
         return null;
      } else {
         Pos3D playerPos = new Pos3D(player.m_20185_(), player.m_20188_() - 0.1, player.m_20189_());
         Pos3D flameVec = new Pos3D(1.0, 1.0, 1.0);
         Vec3 lookVec = player.m_20154_();
         flameVec = flameVec.multiply(lookVec).yRot(6.0F);
         Vec3 mergedVec = playerPos.m_82549_(flameVec);
         flame.m_6034_(mergedVec.f_82479_, mergedVec.f_82480_, mergedVec.f_82481_);
         flame.m_5602_(player);
         ItemStack selected = player.m_150109_().m_36056_();
         flame.mode = ((ItemFlamethrower)selected.m_41720_()).getMode(selected);
         flame.m_37251_(player, player.m_146909_(), player.m_146908_(), 0.0F, 0.5F, 1.0F);
         BlockHitResult blockRayTrace = player.m_9236_().m_45547_(new ClipContext(playerPos, mergedVec, Block.OUTLINE, Fluid.NONE, flame));
         if (blockRayTrace.m_6662_() != Type.MISS) {
            flame.m_6532_(blockRayTrace);
         }

         return flame;
      }
   }

   public void m_6075_() {
      if (this.m_6084_()) {
         this.f_19797_++;
         this.f_19854_ = this.m_20185_();
         this.f_19855_ = this.m_20186_();
         this.f_19856_ = this.m_20189_();
         this.f_19860_ = this.m_146909_();
         this.f_19859_ = this.m_146908_();
         Vec3 motion = this.m_20184_();
         this.m_20343_(this.m_20185_() + motion.m_7096_(), this.m_20186_() + motion.m_7098_(), this.m_20189_() + motion.m_7094_());
         this.m_6034_(this.m_20185_(), this.m_20186_(), this.m_20189_());
         this.calculateVector();
         if (this.f_19797_ > 80) {
            this.m_146870_();
         }
      }
   }

   private void calculateVector() {
      Vec3 localVec = new Vec3(this.m_20185_(), this.m_20186_(), this.m_20189_());
      Vec3 motion = this.m_20184_();
      Vec3 motionVec = new Vec3(this.m_20185_() + motion.m_7096_() * 2.0, this.m_20186_() + motion.m_7098_() * 2.0, this.m_20189_() + motion.m_7094_() * 2.0);
      BlockHitResult blockRayTrace = this.m_9236_().m_45547_(new ClipContext(localVec, motionVec, Block.OUTLINE, Fluid.ANY, this));
      localVec = new Vec3(this.m_20185_(), this.m_20186_(), this.m_20189_());
      motionVec = new Vec3(this.m_20185_() + motion.m_7096_(), this.m_20186_() + motion.m_7098_(), this.m_20189_() + motion.m_7094_());
      if (blockRayTrace.m_6662_() != Type.MISS) {
         motionVec = blockRayTrace.m_82450_();
      }

      EntityHitResult entityResult = ProjectileUtil.m_37304_(
         this.m_9236_(), this, localVec, motionVec, this.m_20191_().m_82369_(this.m_20184_()).m_82377_(1.0, 1.0, 1.0), EntitySelector.f_20408_
      );
      this.m_6532_((HitResult)(entityResult == null ? blockRayTrace : entityResult));
   }

   protected void m_5790_(EntityHitResult entityResult) {
      Entity entity = entityResult.m_82443_();
      if (entity instanceof Player player) {
         Entity owner = this.m_19749_();
         if (player.m_150110_().f_35934_ || owner instanceof Player o && !o.m_7099_(player)) {
            return;
         }
      }

      if (!entity.m_5825_()) {
         if (!(entity instanceof ItemEntity item && this.mode == ItemFlamethrower.FlamethrowerMode.HEAT)) {
            this.burn(entity);
         } else if (entity.f_19797_ > 100 && !this.smeltItem(item)) {
            this.burn(entity);
         }
      }

      this.m_146870_();
   }

   protected void m_8060_(@NotNull BlockHitResult blockRayTrace) {
      super.m_8060_(blockRayTrace);
      BlockPos hitPos = blockRayTrace.m_82425_();
      Direction hitSide = blockRayTrace.m_82434_();
      BlockState hitState = this.m_9236_().m_8055_(hitPos);
      boolean hitFluid = !hitState.m_60819_().m_76178_();
      if (!this.m_9236_().f_46443_ && MekanismConfig.general.aestheticWorldDamage.get() && !hitFluid) {
         if (this.mode == ItemFlamethrower.FlamethrowerMode.HEAT) {
            if (this.m_19749_() instanceof Player player) {
               this.smeltBlock(player, hitState, hitPos, hitSide);
            }
         } else if (this.mode == ItemFlamethrower.FlamethrowerMode.INFERNO) {
            Entity owner = this.m_19749_();
            BlockPos sidePos = hitPos.m_121945_(hitSide);
            if (CampfireBlock.m_51321_(hitState)) {
               this.tryPlace(owner, hitPos, hitSide, (BlockState)hitState.m_61124_(BlockStateProperties.f_61443_, true));
            } else if (BaseFireBlock.m_49255_(this.m_9236_(), sidePos, hitSide)) {
               this.tryPlace(owner, sidePos, hitSide, BaseFireBlock.m_49245_(this.m_9236_(), sidePos));
            } else if (hitState.isFlammable(this.m_9236_(), hitPos, hitSide)) {
               hitState.onCaughtFire(this.m_9236_(), hitPos, hitSide, owner instanceof LivingEntity livingEntity ? livingEntity : null);
               if (hitState.m_60734_() instanceof TntBlock) {
                  this.m_9236_().m_7471_(hitPos, false);
               }
            }
         }
      }

      if (hitFluid) {
         this.spawnParticlesAt(this.m_20183_());
         this.m_5496_(SoundEvents.f_11937_, 1.0F, 1.0F);
      }

      this.m_146870_();
   }

   private boolean tryPlace(@Nullable Entity shooter, BlockPos pos, Direction hitSide, BlockState newState) {
      BlockSnapshot blockSnapshot = BlockSnapshot.create(this.m_9236_().m_46472_(), this.m_9236_(), pos);
      this.m_9236_().m_46597_(pos, newState);
      if (ForgeEventFactory.onBlockPlace(shooter, blockSnapshot, hitSide)) {
         this.m_9236_().restoringBlockSnapshots = true;
         blockSnapshot.restore(true, false);
         this.m_9236_().restoringBlockSnapshots = false;
         return false;
      } else {
         return true;
      }
   }

   private boolean smeltItem(ItemEntity item) {
      ItemStack stack = item.m_32055_();
      if (!stack.m_41619_()) {
         Level level = this.m_9236_();
         Optional<SmeltingRecipe> recipe = MekanismRecipeType.getRecipeFor(RecipeType.f_44108_, new SimpleContainer(new ItemStack[]{stack}), level);
         if (recipe.isPresent()) {
            ItemStack result = recipe.get().m_8043_(level.m_9598_());
            item.m_32045_(result.m_255036_(result.m_41613_() * stack.m_41613_()));
            item.f_19797_ = 0;
            this.spawnParticlesAt(item.m_20183_());
            this.m_5496_(SoundEvents.f_11937_, 1.0F, 1.0F);
            return true;
         }
      }

      return false;
   }

   private void smeltBlock(Player shooter, BlockState hitState, BlockPos blockPos, Direction hitSide) {
      if (!hitState.m_60795_()) {
         ItemStack stack = new ItemStack(hitState.m_60734_());
         if (!stack.m_41619_()) {
            Optional<SmeltingRecipe> recipe;
            try {
               recipe = MekanismRecipeType.getRecipeFor(RecipeType.f_44108_, new SimpleContainer(new ItemStack[]{stack}), this.m_9236_());
            } catch (Exception var9) {
               return;
            }

            if (recipe.isPresent() && !this.m_9236_().f_46443_) {
               if (MinecraftForge.EVENT_BUS.post(new BreakEvent(this.m_9236_(), blockPos, hitState, shooter))) {
                  return;
               }

               ItemStack result = recipe.get().m_8043_(this.m_9236_().m_9598_());
               if (!(result.m_41720_() instanceof BlockItem)
                  || !this.tryPlace(shooter, blockPos, hitSide, net.minecraft.world.level.block.Block.m_49814_(result.m_41720_()).m_49966_())) {
                  this.m_9236_().m_7471_(blockPos, false);
                  ItemEntity item = new ItemEntity(
                     this.m_9236_(), blockPos.m_123341_() + 0.5, blockPos.m_123342_() + 0.5, blockPos.m_123343_() + 0.5, result.m_41777_()
                  );
                  item.m_20334_(0.0, 0.0, 0.0);
                  this.m_9236_().m_7967_(item);
               }

               this.m_9236_().m_46796_(2001, blockPos, net.minecraft.world.level.block.Block.m_49956_(hitState));
               this.spawnParticlesAt((ServerLevel)this.m_9236_(), blockPos);
            }
         }
      }
   }

   private void burn(Entity entity) {
      if (!(entity instanceof ItemEntity) || MekanismConfig.gear.flamethrowerDestroyItems.get()) {
         entity.m_20254_(20);
         entity.m_6469_(this.m_269291_().m_269390_(this, this.m_19749_()), 10.0F);
      }
   }

   private void spawnParticlesAt(BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         this.m_9236_()
            .m_7106_(
               ParticleTypes.f_123762_,
               pos.m_123341_() + (this.f_19796_.m_188501_() - 0.5),
               pos.m_123342_() + (this.f_19796_.m_188501_() - 0.5),
               pos.m_123343_() + (this.f_19796_.m_188501_() - 0.5),
               0.0,
               0.0,
               0.0
            );
      }
   }

   private void spawnParticlesAt(ServerLevel world, BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         world.m_8767_(
            ParticleTypes.f_123762_,
            pos.m_123341_() + (this.f_19796_.m_188501_() - 0.5),
            pos.m_123342_() + (this.f_19796_.m_188501_() - 0.5),
            pos.m_123343_() + (this.f_19796_.m_188501_() - 0.5),
            3,
            0.0,
            0.0,
            0.0,
            0.0
         );
      }
   }

   protected void m_8097_() {
   }

   public void m_7378_(@NotNull CompoundTag nbtTags) {
      super.m_7378_(nbtTags);
      NBTUtils.setEnumIfPresent(nbtTags, "mode", ItemFlamethrower.FlamethrowerMode::byIndexStatic, mode -> this.mode = mode);
   }

   public void m_7380_(@NotNull CompoundTag nbtTags) {
      super.m_7380_(nbtTags);
      NBTUtils.writeEnum(nbtTags, "mode", this.mode);
   }

   @NotNull
   public Packet<ClientGamePacketListener> m_5654_() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   public void writeSpawnData(FriendlyByteBuf dataStream) {
      dataStream.m_130068_(this.mode);
   }

   public void readSpawnData(FriendlyByteBuf dataStream) {
      this.mode = (ItemFlamethrower.FlamethrowerMode)dataStream.m_130066_(ItemFlamethrower.FlamethrowerMode.class);
   }
}
