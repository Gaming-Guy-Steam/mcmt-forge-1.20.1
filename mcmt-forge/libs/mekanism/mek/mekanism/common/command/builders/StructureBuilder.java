package mekanism.common.command.builders;

import java.util.function.Consumer;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class StructureBuilder {
   protected final int sizeX;
   protected final int sizeY;
   protected final int sizeZ;

   protected StructureBuilder(int sizeX, int sizeY, int sizeZ) {
      this.sizeX = sizeX;
      this.sizeY = sizeY;
      this.sizeZ = sizeZ;
   }

   protected abstract void build(Level world, BlockPos start, boolean empty);

   protected void buildFrame(Level world, BlockPos start) {
      this.buildPartialFrame(world, start, -1);
   }

   protected void buildPartialFrame(Level world, BlockPos start, int cutoff) {
      for (int x = 0; x < this.sizeX; x++) {
         if (x > cutoff && x < this.sizeX - 1 - cutoff) {
            world.m_46597_(start.m_7918_(x, 0, 0), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(x, this.sizeY - 1, 0), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(x, 0, this.sizeZ - 1), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(x, this.sizeY - 1, this.sizeZ - 1), this.getCasing().m_49966_());
         }
      }

      for (int y = 0; y < this.sizeY; y++) {
         if (y > cutoff && y < this.sizeY - 1 - cutoff) {
            world.m_46597_(start.m_7918_(0, y, 0), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(this.sizeX - 1, y, 0), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(0, y, this.sizeZ - 1), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(this.sizeX - 1, y, this.sizeZ - 1), this.getCasing().m_49966_());
         }
      }

      for (int z = 0; z < this.sizeZ; z++) {
         if (z > cutoff && z < this.sizeZ - 1 - cutoff) {
            world.m_46597_(start.m_7918_(0, 0, z), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(this.sizeX - 1, 0, z), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(0, this.sizeY - 1, z), this.getCasing().m_49966_());
            world.m_46597_(start.m_7918_(this.sizeX - 1, this.sizeY - 1, z), this.getCasing().m_49966_());
         }
      }
   }

   protected void buildWalls(Level world, BlockPos start) {
      for (int x = 1; x < this.sizeX - 1; x++) {
         for (int z = 1; z < this.sizeZ - 1; z++) {
            BlockPos pos = new BlockPos(x, 0, z);
            world.m_46597_(start.m_121955_(pos), this.getFloorBlock(pos).m_49966_());
            pos = new BlockPos(x, this.sizeY - 1, z);
            world.m_46597_(start.m_121955_(pos), this.getRoofBlock(pos).m_49966_());
         }
      }

      for (int y = 1; y < this.sizeY - 1; y++) {
         for (int x = 1; x < this.sizeZ - 1; x++) {
            BlockPos pos = new BlockPos(x, y, 0);
            world.m_46597_(start.m_121955_(pos), this.getWallBlock(pos).m_49966_());
            pos = new BlockPos(x, y, this.sizeZ - 1);
            world.m_46597_(start.m_121955_(pos), this.getWallBlock(pos).m_49966_());
         }

         for (int z = 1; z < this.sizeZ - 1; z++) {
            BlockPos pos = new BlockPos(0, y, z);
            world.m_46597_(start.m_121955_(pos), this.getWallBlock(pos).m_49966_());
            pos = new BlockPos(this.sizeZ - 1, y, z);
            world.m_46597_(start.m_121955_(pos), this.getWallBlock(pos).m_49966_());
         }
      }
   }

   protected void buildInteriorLayers(Level world, BlockPos start, int yMin, int yMax, Block block) {
      for (int y = yMin; y <= yMax; y++) {
         this.buildInteriorLayer(world, start, y, block);
      }
   }

   protected void buildInteriorLayer(Level world, BlockPos start, int yLevel, Block block) {
      for (int x = 1; x < this.sizeX - 1; x++) {
         for (int z = 1; z < this.sizeZ - 1; z++) {
            world.m_46597_(start.m_7918_(x, yLevel, z), block.m_49966_());
         }
      }
   }

   protected void buildPlane(Level world, BlockPos start, int x1, int z1, int x2, int z2, int yLevel, Block block) {
      for (int x = x1; x < x2 - 1; x++) {
         for (int z = z1; z < z2 - 1; z++) {
            world.m_46597_(start.m_7918_(x, yLevel, z), block.m_49966_());
         }
      }
   }

   protected void buildColumn(Level world, BlockPos start, BlockPos pos, int height, Block block) {
      for (int y = 0; y < height; y++) {
         world.m_46597_(start.m_121955_(pos).m_7918_(0, y, 0), block.m_49966_());
      }
   }

   protected <T extends BlockEntity> void buildColumn(
      Level world, BlockPos start, BlockPos pos, int height, Block block, Class<T> tileClass, Consumer<T> tileConsumer
   ) {
      for (int y = 0; y < height; y++) {
         BlockPos position = start.m_121955_(pos).m_7918_(0, y, 0);
         world.m_46597_(position, block.m_49966_());
         T tile = WorldUtils.getTileEntity(tileClass, world, position);
         if (tile != null) {
            tileConsumer.accept(tile);
         }
      }
   }

   protected Block getWallBlock(BlockPos pos) {
      return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
   }

   protected Block getFloorBlock(BlockPos pos) {
      return this.getCasing();
   }

   protected Block getRoofBlock(BlockPos pos) {
      return this.getWallBlock(pos);
   }

   protected abstract Block getCasing();
}
