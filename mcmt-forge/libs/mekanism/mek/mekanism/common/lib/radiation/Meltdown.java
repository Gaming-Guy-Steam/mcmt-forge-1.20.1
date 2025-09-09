package mekanism.common.lib.radiation;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import mekanism.common.util.WorldUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion.BlockInteraction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

public class Meltdown {
   private static final int DURATION = 100;
   private final BlockPos minPos;
   private final BlockPos maxPos;
   private final double magnitude;
   private final double chance;
   private final UUID multiblockID;
   private final float radius;
   private int ticksExisted;

   public Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID) {
      this(minPos, maxPos, magnitude, chance, radius, multiblockID, 0);
   }

   private Meltdown(BlockPos minPos, BlockPos maxPos, double magnitude, double chance, float radius, UUID multiblockID, int ticksExisted) {
      this.minPos = minPos;
      this.maxPos = maxPos;
      this.magnitude = magnitude;
      this.chance = chance;
      this.radius = radius;
      this.multiblockID = multiblockID;
      this.ticksExisted = ticksExisted;
   }

   public static Meltdown load(CompoundTag tag) {
      return new Meltdown(
         NbtUtils.m_129239_(tag.m_128469_("min")),
         NbtUtils.m_129239_(tag.m_128469_("max")),
         tag.m_128459_("magnitude"),
         tag.m_128459_("chance"),
         tag.m_128457_("radius"),
         tag.m_128342_("inventoryID"),
         tag.m_128451_("age")
      );
   }

   public void write(CompoundTag tag) {
      tag.m_128365_("min", NbtUtils.m_129224_(this.minPos));
      tag.m_128365_("max", NbtUtils.m_129224_(this.maxPos));
      tag.m_128347_("magnitude", this.magnitude);
      tag.m_128347_("chance", this.chance);
      tag.m_128350_("radius", this.radius);
      tag.m_128362_("inventoryID", this.multiblockID);
      tag.m_128405_("age", this.ticksExisted);
   }

   public boolean update(Level world) {
      this.ticksExisted++;
      if (world.f_46441_.m_188502_() % 10 == 0 && world.f_46441_.m_188500_() < this.magnitude * this.chance) {
         int x = Mth.m_216271_(world.f_46441_, this.minPos.m_123341_(), this.maxPos.m_123341_());
         int y = Mth.m_216271_(world.f_46441_, this.minPos.m_123342_(), this.maxPos.m_123342_());
         int z = Mth.m_216271_(world.f_46441_, this.minPos.m_123343_(), this.maxPos.m_123343_());
         BlockInteraction mode = world.m_46469_().m_46207_(GameRules.f_254629_) ? BlockInteraction.DESTROY_WITH_DECAY : BlockInteraction.DESTROY;
         this.createExplosion(world, x, y, z, this.radius, true, mode);
      }

      return WorldUtils.isBlockLoaded(world, this.minPos) && WorldUtils.isBlockLoaded(world, this.maxPos) ? this.ticksExisted >= 100 : true;
   }

   private void createExplosion(Level world, double x, double y, double z, float radius, boolean causesFire, BlockInteraction mode) {
      Explosion explosion = new Meltdown.MeltdownExplosion(world, x, y, z, radius, causesFire, mode, this.multiblockID);
      ObjectArrayList<BlockPos> toBlow = new ObjectArrayList();

      for (int j = 0; j < 16; j++) {
         for (int k = 0; k < 16; k++) {
            for (int l = 0; l < 16; l++) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d0 = j / 7.5 - 1.0;
                  double d1 = k / 7.5 - 1.0;
                  double d2 = l / 7.5 - 1.0;
                  double d3 = Mth.m_184648_(d0, d1, d2);
                  d0 /= d3;
                  d1 /= d3;
                  d2 /= d3;
                  float f = radius * (0.7F + world.f_46441_.m_188501_() * 0.6F);
                  double d4 = x;
                  double d6 = y;

                  for (double d8 = z; f > 0.0F; f -= 0.22500001F) {
                     BlockPos pos = BlockPos.m_274561_(d4, d6, d8);
                     BlockState blockstate = world.m_8055_(pos);
                     FluidState fluidstate = blockstate.m_60819_();
                     if (!blockstate.m_60795_() || !fluidstate.m_76178_()) {
                        f -= (
                              Math.max(blockstate.getExplosionResistance(world, pos, explosion), fluidstate.getExplosionResistance(world, pos, explosion))
                                 + 0.3F
                           )
                           * 0.3F;
                     }

                     if (f > 0.0F
                        && this.minPos.m_123341_() <= d4
                        && this.minPos.m_123342_() <= d6
                        && this.minPos.m_123343_() <= d8
                        && d4 <= this.maxPos.m_123341_()
                        && d6 <= this.maxPos.m_123342_()
                        && d8 <= this.maxPos.m_123343_()) {
                        toBlow.add(pos);
                     }

                     d4 += d0 * 0.3;
                     d6 += d1 * 0.3;
                     d8 += d2 * 0.3;
                  }
               }
            }
         }
      }

      if (!ForgeEventFactory.onExplosionStart(world, explosion)) {
         explosion.m_46061_();
         explosion.m_46075_(true);
      }

      Util.m_214673_(toBlow, world.f_46441_);
      List<Pair<ItemStack, BlockPos>> drops = new ArrayList<>();
      ObjectListIterator var35 = toBlow.iterator();

      while (var35.hasNext()) {
         BlockPos toExplode = (BlockPos)var35.next();
         BlockState state = world.m_8055_(toExplode);
         if (!state.m_60795_()) {
            if (state.canDropFromExplosion(world, toExplode, explosion) && world instanceof ServerLevel level) {
               BlockEntity tileentity = state.m_155947_() ? world.m_7702_(toExplode) : null;
               Builder lootContextBuilder = new Builder(level)
                  .m_287286_(LootContextParams.f_81460_, Vec3.m_82512_(toExplode))
                  .m_287286_(LootContextParams.f_81463_, ItemStack.f_41583_)
                  .m_287289_(LootContextParams.f_81462_, tileentity)
                  .m_287289_(LootContextParams.f_81455_, null);
               if (mode == BlockInteraction.DESTROY_WITH_DECAY) {
                  lootContextBuilder.m_287286_(LootContextParams.f_81464_, radius);
               }

               state.m_222967_(level, toExplode, ItemStack.f_41583_, false);
               state.m_287290_(lootContextBuilder).forEach(stack -> addBlockDrops(drops, stack, toExplode));
            }

            state.onBlockExploded(world, toExplode, explosion);
         }
      }

      for (Pair<ItemStack, BlockPos> pair : drops) {
         Block.m_49840_(world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
      }
   }

   private static void addBlockDrops(List<Pair<ItemStack, BlockPos>> dropPositions, ItemStack stack, BlockPos pos) {
      int i = 0;

      for (int size = dropPositions.size(); i < size; i++) {
         Pair<ItemStack, BlockPos> pair = dropPositions.get(i);
         ItemStack itemstack = (ItemStack)pair.getFirst();
         if (ItemEntity.m_32026_(itemstack, stack)) {
            ItemStack itemstack1 = ItemEntity.m_32029_(itemstack, stack, 16);
            dropPositions.set(i, Pair.of(itemstack1, (BlockPos)pair.getSecond()));
            if (stack.m_41619_()) {
               return;
            }
         }
      }

      dropPositions.add(Pair.of(stack, pos));
   }

   public static class MeltdownExplosion extends Explosion {
      private final UUID multiblockID;

      private MeltdownExplosion(Level world, double x, double y, double z, float radius, boolean causesFire, BlockInteraction mode, UUID multiblockID) {
         super(world, null, null, null, x, y, z, radius, causesFire, mode);
         this.multiblockID = multiblockID;
      }

      public UUID getMultiblockID() {
         return this.multiblockID;
      }
   }
}
