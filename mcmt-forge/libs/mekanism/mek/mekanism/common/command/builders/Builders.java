package mekanism.common.command.builders;

import mekanism.common.registries.MekanismBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class Builders {
   private Builders() {
   }

   public static class BoilerBuilder extends StructureBuilder {
      public BoilerBuilder() {
         super(18, 18, 18);
      }

      @Override
      public void build(Level world, BlockPos start, boolean empty) {
         this.buildFrame(world, start);
         this.buildWalls(world, start);
         this.buildInteriorLayers(world, start, 2, 14, Blocks.f_50016_);
         this.buildInteriorLayer(world, start, 16, Blocks.f_50016_);
         if (empty) {
            this.buildInteriorLayer(world, start, 1, Blocks.f_50016_);
            this.buildInteriorLayer(world, start, 15, Blocks.f_50016_);
         } else {
            this.buildInteriorLayer(world, start, 1, MekanismBlocks.SUPERHEATING_ELEMENT.getBlock());
            this.buildInteriorLayer(world, start, 15, MekanismBlocks.PRESSURE_DISPERSER.getBlock());
         }
      }

      @Override
      protected Block getCasing() {
         return MekanismBlocks.BOILER_CASING.getBlock();
      }
   }

   public static class EvaporationBuilder extends StructureBuilder {
      public EvaporationBuilder() {
         super(4, 18, 4);
      }

      @Override
      public void build(Level world, BlockPos start, boolean empty) {
         this.buildFrame(world, start);
         this.buildWalls(world, start);
         this.buildInteriorLayers(world, start, 1, 17, Blocks.f_50016_);
         world.m_46597_(start.m_7918_(1, 1, 0), MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER.getBlock().m_49966_());
      }

      @Override
      protected Block getCasing() {
         return MekanismBlocks.THERMAL_EVAPORATION_BLOCK.getBlock();
      }
   }

   public static class MatrixBuilder extends StructureBuilder {
      public MatrixBuilder() {
         super(18, 18, 18);
      }

      @Override
      public void build(Level world, BlockPos start, boolean empty) {
         this.buildFrame(world, start);
         this.buildWalls(world, start);
         if (empty) {
            this.buildInteriorLayers(world, start, 1, 16, Blocks.f_50016_);
         } else {
            this.buildInteriorLayers(world, start, 1, 15, MekanismBlocks.ULTIMATE_INDUCTION_CELL.getBlock());
            this.buildInteriorLayer(world, start, 16, MekanismBlocks.ULTIMATE_INDUCTION_PROVIDER.getBlock());
         }
      }

      @Override
      protected Block getCasing() {
         return MekanismBlocks.INDUCTION_CASING.getBlock();
      }
   }

   public static class SPSBuilder extends StructureBuilder {
      public SPSBuilder() {
         super(7, 7, 7);
      }

      @Override
      protected void build(Level world, BlockPos start, boolean empty) {
         this.buildPartialFrame(world, start, 1);
         this.buildWalls(world, start);
         this.buildInteriorLayers(world, start, 1, 5, Blocks.f_50016_);

         for (int x = -2; x < 2; x++) {
            for (int y = -2; y < 2; y++) {
               for (int z = -2; z < 2; z++) {
                  if (x == -1 == (y == -1) == (z == -1) == (x == 0) == (y == 0) != (z == 0) && (x != -1 && x != 0 || y != -1 && y != 0 || z != -1 && z != 0)) {
                     world.m_46597_(
                        start.m_7918_(x < 0 ? this.sizeX + x : x, y < 0 ? this.sizeY + y : y, z < 0 ? this.sizeZ + z : z), this.getCasing().m_49966_()
                     );
                  }
               }
            }
         }
      }

      @Override
      protected Block getCasing() {
         return MekanismBlocks.SPS_CASING.getBlock();
      }
   }

   public static class TankBuilder extends StructureBuilder {
      public TankBuilder() {
         super(18, 18, 18);
      }

      @Override
      public void build(Level world, BlockPos start, boolean empty) {
         this.buildFrame(world, start);
         this.buildWalls(world, start);
         this.buildInteriorLayers(world, start, 1, 16, Blocks.f_50016_);
      }

      @Override
      protected Block getCasing() {
         return MekanismBlocks.DYNAMIC_TANK.getBlock();
      }
   }
}
