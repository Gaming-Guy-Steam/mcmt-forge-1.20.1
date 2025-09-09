package mekanism.common.content.blocktype;

import mekanism.common.tier.FactoryTier;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockShapes {
   public static final VoxelShape[] ELECTROLYTIC_SEPARATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] DIGITAL_MINER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_CRYSTALLIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] PRESSURIZED_REACTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] METALLURGIC_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_WASHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_OXIDIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_INFUSER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_DISSOLUTION_CHAMBER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] ROTARY_CONDENSENTRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] FLUIDIC_PLENISHER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] ELECTRIC_PUMP = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] SOLAR_NEUTRON_ACTIVATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHARGEPAD = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] FLUID_TANK = new VoxelShape[]{box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)};
   public static final VoxelShape[] LASER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] LASER_AMPLIFIER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] RESISTIVE_HEATER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] SEISMIC_VIBRATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] PERSONAL_CHEST = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] QUANTUM_ENTANGLOPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] LOGISTICAL_SORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] SECURITY_DESK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CHEMICAL_TANK = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] INDUSTRIAL_ALARM = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] QIO_DASHBOARD = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] QIO_DRIVE_ARRAY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] QIO_IMPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] QIO_EXPORTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] QIO_REDSTONE_ADAPTER = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] RADIOACTIVE_WASTE_BARREL = FLUID_TANK;
   public static final VoxelShape[] MODIFICATION_STATION = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] ISOTOPIC_CENTRIFUGE = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] SUPERCHARGED_COIL = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape[] ANTIPROTONIC_NUCLEOSYNTHESIZER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] PIGMENT_MIXER = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] SMELTING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] ENRICHING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] CRUSHING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] COMPRESSING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] COMBINING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] PURIFYING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] INJECTING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] INFUSING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
   public static final VoxelShape[] SAWING_FACTORY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

   private BlockShapes() {
   }

   private static VoxelShape box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      return Block.m_49796_(minX, minY, minZ, maxX, maxY, maxZ);
   }

   public static VoxelShape[] getShape(FactoryTier tier, FactoryType type) {
      return switch (type) {
         case SMELTING -> SMELTING_FACTORY;
         case ENRICHING -> ENRICHING_FACTORY;
         case CRUSHING -> CRUSHING_FACTORY;
         case COMPRESSING -> COMPRESSING_FACTORY;
         case COMBINING -> COMBINING_FACTORY;
         case PURIFYING -> PURIFYING_FACTORY;
         case INJECTING -> INJECTING_FACTORY;
         case INFUSING -> INFUSING_FACTORY;
         case SAWING -> SAWING_FACTORY;
      };
   }

   static {
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.rotate(
            VoxelShapeUtils.combine(
               box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
               box(15.0, 3.0, 3.0, 16.0, 13.0, 13.0),
               box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
               box(4.0, 4.0, 0.0, 12.0, 12.0, 1.0),
               box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),
               box(1.0, 4.0, 7.0, 3.0, 11.0, 9.0),
               box(7.0, 4.0, 1.0, 8.0, 11.0, 3.0),
               box(7.0, 4.0, 13.0, 8.0, 11.0, 15.0),
               box(8.0, 4.0, 0.0, 16.0, 16.0, 16.0),
               box(0.0, 4.0, 9.0, 7.0, 14.0, 16.0),
               box(0.0, 4.0, 0.0, 7.0, 14.0, 7.0),
               box(7.0, 10.0, 7.5, 8.0, 11.0, 8.5),
               box(4.0, 12.0, 7.5, 7.0, 13.0, 8.5),
               box(3.0, 12.0, 7.5, 4.0, 15.0, 8.5),
               box(3.0, 15.0, 3.0, 4.0, 16.0, 13.0),
               box(3.0, 14.0, 3.0, 4.0, 15.0, 4.0),
               box(3.0, 14.0, 12.0, 4.0, 15.0, 13.0),
               box(6.0, 10.0, 7.5, 7.0, 12.0, 8.5)
            ),
            Rotation.CLOCKWISE_90
         ),
         ELECTROLYTIC_SEPARATOR
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(16.0, 17.0, -13.0, 30.0, 27.0, -11.0),
            box(1.0, 17.0, -13.0, 15.0, 27.0, -11.0),
            box(-14.0, 17.0, -13.0, 0.0, 27.0, -11.0),
            box(17.0, 19.0, -11.0, 29.0, 25.0, -10.0),
            box(2.0, 19.0, -11.0, 14.0, 25.0, -10.0),
            box(-13.0, 19.0, -11.0, -1.0, 25.0, -10.0),
            box(-1.0, 21.0, -11.0, 2.0, 23.0, -10.0),
            box(14.0, 21.0, -11.0, 17.0, 23.0, -10.0),
            box(4.0, 21.0, -11.0, 12.0, 23.0, -9.0),
            box(3.0, 11.3498, -16.0, 13.0, 12.3498, -11.0),
            box(4.0, 10.3498, -15.0, 12.0, 11.3498, -11.0),
            box(5.0, 10.0, -13.25, 11.0, 12.0, -9.0),
            box(-8.0, 3.0, -9.0, 24.0, 32.0, 3.0),
            box(17.0, 26.99, 4.0, 24.0, 31.99, 19.0),
            box(-8.0, 26.99, 4.0, -1.0, 31.99, 19.0),
            box(0.0, 26.99, 3.0, 16.0, 31.99, 16.0),
            box(0.0, 26.99, 17.0, 16.0, 31.99, 20.0),
            box(-8.0, 21.0, 4.0, 24.0, 26.0, 19.0),
            box(-8.0, 15.0, 4.0, 24.0, 20.0, 19.0),
            box(-8.0, 9.0, 4.0, 24.0, 14.0, 19.0),
            box(-8.0, 3.0, 4.0, 24.0, 8.0, 19.0),
            box(-8.0, 3.0, 19.99, 24.0, 32.0, 31.99),
            box(-7.0, 15.0, 3.0, 23.0, 31.0, 20.0),
            box(-7.0, 4.0, 3.0, 23.0, 15.0, 20.0),
            box(-9.0, 24.0, -6.0, -8.0, 29.0, 0.0),
            box(-13.0, 24.0, -8.0, -8.0, 29.0, -6.0),
            box(-13.0, 24.0, 0.0, -8.0, 29.0, 2.0),
            box(-9.0, 24.0, 23.0, -8.0, 29.0, 29.0),
            box(-13.0, 24.0, 21.0, -8.0, 29.0, 23.0),
            box(-13.0, 24.0, 29.0, -8.0, 29.0, 31.0),
            box(24.0, 24.0, -6.0, 25.0, 29.0, 0.0),
            box(24.0, 24.0, 0.0, 29.0, 29.0, 2.0),
            box(24.0, 24.0, -8.0, 29.0, 29.0, -6.0),
            box(24.0, 24.0, 23.0, 25.0, 29.0, 29.0),
            box(24.0, 24.0, 29.0, 29.0, 29.0, 31.0),
            box(24.0, 24.0, 21.0, 29.0, 29.0, 23.0),
            box(5.0, 2.0, -6.0, 11.0, 4.0, 5.0),
            box(5.0, 1.0, 5.0, 11.0, 3.0, 11.0),
            box(23.0, 5.0, 5.0, 31.0, 11.0, 11.0),
            box(-15.0, 5.0, 5.0, -7.0, 11.0, 11.0),
            box(-14.0, 2.0, -7.0, -10.0, 30.0, 1.0),
            box(-14.0, 2.0, 22.0, -10.0, 30.0, 30.0),
            box(26.0, 2.0, 22.0, 30.0, 30.0, 30.0),
            box(26.0, 2.0, -7.0, 30.0, 30.0, 1.0),
            box(-15.0, 0.0, -8.0, -8.0, 2.0, 2.0),
            box(-15.0, 0.0, 21.0, -8.0, 2.0, 31.0),
            box(24.0, 0.0, 21.0, 31.0, 2.0, 31.0),
            box(24.0, 0.0, -8.0, 31.0, 2.0, 2.0),
            box(-9.0, 4.0, 4.0, -8.0, 12.0, 12.0),
            box(-16.0, 4.0, 4.0, -15.0, 12.0, 12.0),
            box(24.0, 4.0, 4.0, 25.0, 12.0, 12.0),
            box(31.0, 4.0, 4.0, 32.0, 12.0, 12.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(17.0, 16.5, -12.005, 18.0, 17.5, -11.005),
            box(2.0, 16.5, -12.005, 3.0, 17.5, -11.005),
            box(-13.0, 16.5, -12.005, -12.0, 17.5, -11.005)
         ),
         DIGITAL_MINER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 5.0, 14.0, 16.0, 7.0, 16.0),
            box(0.0, 5.0, 2.0, 2.0, 7.0, 14.0),
            box(0.0, 5.0, 0.0, 16.0, 7.0, 2.0),
            box(14.0, 5.0, 2.0, 16.0, 7.0, 14.0),
            box(3.0, 5.0, 3.0, 13.0, 6.0, 13.0),
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            box(0.0, 7.0, 0.0, 1.0, 11.0, 1.0),
            box(0.0, 7.0, 15.0, 1.0, 11.0, 16.0),
            box(15.0, 7.0, 15.0, 16.0, 11.0, 16.0),
            box(15.0, 7.0, 0.0, 16.0, 11.0, 1.0),
            box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
            box(0.0, 11.0, 0.0, 16.0, 16.0, 16.0),
            box(6.0, 9.0, 8.0, 7.0, 11.0, 9.0),
            box(9.0, 8.0, 9.0, 10.0, 11.0, 10.0),
            box(7.5, 7.0, 6.0, 8.5, 11.0, 7.0),
            box(1.0, 7.0, 1.0, 15.0, 11.0, 15.0)
         ),
         CHEMICAL_CRYSTALLIZER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 4.0, 6.0, 16.0, 16.0, 16.0),
            box(10.0, 4.0, 1.0, 12.0, 15.0, 6.0),
            box(13.0, 4.0, 1.0, 15.0, 15.0, 6.0),
            box(1.0, 4.0, 2.0, 10.0, 14.0, 6.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(12.0, 13.0, 2.0, 13.0, 14.0, 6.0),
            box(12.0, 11.0, 2.0, 13.0, 12.0, 6.0),
            box(12.0, 9.0, 2.0, 13.0, 10.0, 6.0),
            box(12.0, 7.0, 2.0, 13.0, 8.0, 6.0),
            box(12.0, 5.0, 2.0, 13.0, 6.0, 6.0)
         ),
         PRESSURIZED_REACTION_CHAMBER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(11.0, 11.0, 5.0, 12.0, 16.0, 8.0),
            box(4.0, 11.0, 5.0, 5.0, 16.0, 8.0),
            box(2.0, 7.0, 2.0, 14.0, 8.0, 8.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(0.0, 4.0, 8.0, 16.0, 16.0, 16.0),
            box(1.0, 12.0, 1.0, 15.0, 15.0, 8.0),
            box(1.0, 8.0, 1.0, 15.0, 11.0, 8.0),
            box(1.0, 4.0, 1.0, 15.0, 7.0, 8.0),
            box(13.0, 11.0, 2.0, 14.0, 12.0, 3.0),
            box(2.0, 11.0, 2.0, 3.0, 12.0, 3.0)
         ),
         METALLURGIC_INFUSER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(4.0, 13.0, 4.0, 12.0, 15.0, 12.0),
            box(5.0, 4.0, 5.0, 11.0, 13.0, 10.0),
            box(0.0, 4.0, 0.0, 7.0, 14.0, 8.0),
            box(9.0, 4.0, 0.0, 16.0, 14.0, 8.0),
            box(0.0, 4.0, 10.0, 16.0, 14.0, 16.0),
            box(13.0, 5.0, 8.0, 15.0, 11.0, 10.0),
            box(7.0, 7.0, 3.0, 9.0, 9.0, 5.0),
            box(7.0, 6.0, 1.0, 9.0, 7.0, 2.0),
            box(7.0, 8.0, 1.0, 9.0, 9.0, 2.0),
            box(7.0, 10.0, 1.0, 9.0, 11.0, 2.0),
            box(7.0, 12.0, 1.0, 9.0, 13.0, 2.0),
            box(1.0, 14.0, 4.0, 2.0, 15.0, 5.0),
            box(1.0, 14.0, 6.0, 2.0, 15.0, 7.0),
            box(13.0, 14.0, 4.0, 14.0, 15.0, 12.0),
            box(1.0, 5.0, 8.0, 3.0, 11.0, 10.0),
            box(3.0, 15.0, 3.0, 13.0, 16.0, 13.0),
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0)
         ),
         CHEMICAL_WASHER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(9.0, 4.0, 2.0, 13.0, 5.0, 14.0),
            box(8.0, 5.0, 7.0, 14.0, 16.0, 15.0),
            box(8.0, 5.0, 1.0, 14.0, 16.0, 6.0),
            box(7.0, 7.0, 9.0, 8.0, 10.0, 12.0),
            box(0.0, 4.0, 0.0, 7.0, 16.0, 16.0),
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(9.0, 6.0, 6.0, 13.0, 15.0, 7.0),
            box(13.0, 5.0, 5.0, 15.0, 11.0, 11.0),
            box(15.0, 3.0, 3.0, 16.0, 13.0, 13.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
         ),
         CHEMICAL_OXIDIZER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            box(2.0, 5.0, 1.0, 14.0, 12.0, 8.0),
            box(9.0, 13.5, 4.0, 10.0, 14.5, 6.0),
            box(7.0, 5.0, 13.0, 9.0, 11.0, 15.0),
            box(3.0, 5.0, 6.0, 13.0, 11.0, 9.0),
            box(8.0, 8.0, 9.0, 9.0, 9.0, 13.0),
            box(1.0, 5.0, 5.0, 2.0, 11.0, 9.0),
            box(6.0, 13.5, 4.0, 7.0, 14.5, 6.0),
            box(7.0, 14.0, 13.0, 9.0, 15.0, 14.0),
            box(11.0, 13.5, 6.5, 13.0, 14.5, 7.5),
            box(4.0, 4.0, 0.0, 12.0, 12.0, 1.0),
            box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
            box(3.0, 13.5, 7.5, 4.0, 14.5, 9.5),
            box(14.0, 5.0, 5.0, 15.0, 11.0, 9.0),
            box(12.0, 13.5, 7.5, 13.0, 14.5, 9.5),
            box(6.0, 11.5, 4.0, 7.0, 13.5, 5.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),
            box(3.0, 13.5, 6.5, 5.0, 14.5, 7.5),
            box(7.0, 14.0, 10.0, 9.0, 15.0, 11.0),
            box(5.0, 12.5, 5.5, 11.0, 15.5, 8.5),
            box(9.0, 11.5, 4.0, 10.0, 13.5, 5.0),
            box(11.0, 12.0, 2.0, 12.0, 13.0, 3.0),
            box(9.0, 12.0, 2.0, 10.0, 13.0, 3.0),
            box(6.0, 12.0, 2.0, 7.0, 13.0, 3.0),
            box(4.0, 12.0, 2.0, 5.0, 13.0, 3.0),
            box(9.0, 5.0, 9.0, 15.0, 16.0, 15.0),
            box(1.0, 5.0, 9.0, 7.0, 16.0, 15.0)
         ),
         CHEMICAL_INFUSER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
            box(0.0, 3.0, 4.0, 1.0, 11.0, 12.0),
            box(15.0, 3.0, 3.0, 16.0, 13.0, 13.0),
            box(0.0, 7.0, 15.0, 16.0, 15.0, 16.0),
            box(1.0, 7.0, 14.0, 15.0, 15.0, 15.0),
            box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 12.0, 0.0, 16.0, 13.0, 15.0),
            box(4.0, 13.0, 4.0, 12.0, 15.0, 14.0),
            box(1.0, 13.0, 1.0, 2.0, 15.0, 2.0),
            box(14.0, 13.0, 1.0, 15.0, 15.0, 2.0),
            box(1.0, 7.0, 1.0, 15.0, 12.0, 14.0)
         ),
         CHEMICAL_DISSOLUTION_CHAMBER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
            box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 13.0, 0.0, 16.0, 14.0, 16.0),
            box(7.5, 11.0, 7.5, 8.5, 13.0, 8.5),
            box(4.0, 14.0, 4.0, 12.0, 15.0, 12.0),
            box(7.0, 5.0, 5.0, 9.0, 11.0, 11.0),
            box(9.0, 5.0, 1.0, 15.0, 13.0, 15.0),
            box(1.0, 5.0, 1.0, 7.0, 13.0, 15.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            box(0.0, 3.0, 3.0, 1.0, 13.0, 13.0),
            box(14.0, 14.0, 14.0, 15.0, 15.0, 15.0),
            box(14.0, 14.0, 1.0, 15.0, 15.0, 2.0),
            box(1.0, 14.0, 1.0, 2.0, 15.0, 2.0),
            box(1.0, 14.0, 14.0, 2.0, 15.0, 15.0),
            box(7.0, 11.0, 2.0, 9.0, 12.0, 3.0),
            box(7.0, 9.0, 2.0, 9.0, 10.0, 3.0),
            box(7.0, 7.0, 2.0, 9.0, 8.0, 3.0),
            box(7.0, 5.0, 2.0, 9.0, 6.0, 3.0),
            box(7.0, 7.0, 13.0, 9.0, 8.0, 14.0),
            box(7.0, 9.0, 13.0, 9.0, 10.0, 14.0),
            box(7.0, 11.0, 13.0, 9.0, 12.0, 14.0),
            box(7.0, 5.0, 13.0, 9.0, 6.0, 14.0)
         ),
         ROTARY_CONDENSENTRATOR
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(3.0, 1.0, 3.0, 13.0, 13.0, 13.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),
            box(5.0, 5.0, 13.0, 11.0, 11.0, 15.0),
            box(2.0, 13.0, 2.0, 14.0, 14.0, 14.0),
            box(3.0, 15.0, 3.0, 13.0, 16.0, 13.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(4.0, 14.0, 4.0, 12.0, 15.0, 12.0),
            box(2.0, 6.0, 6.0, 3.0, 10.0, 10.0),
            box(13.0, 6.0, 6.0, 14.0, 10.0, 10.0)
         ),
         FLUIDIC_PLENISHER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.rotate(
            VoxelShapeUtils.combine(
               box(4.0, 1.0, 4.0, 12.0, 12.0, 12.0),
               box(10.0, 10.0, 1.0, 11.0, 11.0, 5.0),
               box(5.0, 10.0, 1.0, 6.0, 11.0, 5.0),
               box(5.0, 5.0, 1.0, 6.0, 6.0, 5.0),
               box(10.0, 5.0, 1.0, 11.0, 6.0, 5.0),
               box(5.0, 0.0, 5.0, 11.0, 15.0, 11.0),
               box(4.0, 15.0, 4.0, 12.0, 16.0, 12.0),
               box(4.0, 13.0, 4.0, 12.0, 14.0, 12.0),
               box(6.0, 6.0, 1.0, 10.0, 10.0, 4.0),
               box(4.0, 4.0, 0.0, 12.0, 12.0, 1.0)
            ),
            Rotation.CLOCKWISE_180
         ),
         ELECTRIC_PUMP
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(6.0, 27.0, 4.0, 10.0, 28.0, 14.0),
            box(7.0, 26.0, 8.0, 9.0, 27.0, 14.0),
            box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            box(0.0, 5.0, 0.0, 16.0, 14.0, 16.0),
            box(2.0, 4.0, 2.0, 14.0, 5.0, 14.0),
            box(1.0, 4.0, 3.0, 2.0, 5.0, 5.0),
            box(1.0, 4.0, 6.0, 2.0, 5.0, 10.0),
            box(1.0, 4.0, 11.0, 2.0, 5.0, 13.0),
            box(14.0, 4.0, 11.0, 15.0, 5.0, 13.0),
            box(14.0, 4.0, 6.0, 15.0, 5.0, 10.0),
            box(14.0, 4.0, 3.0, 15.0, 5.0, 5.0),
            box(6.0, 4.0, 14.0, 10.0, 5.0, 16.0),
            box(4.0, 3.0, 0.0, 12.0, 11.0, 1.0),
            box(6.0, 14.0, 14.0, 10.0, 28.0, 16.0),
            box(5.0, 14.0, 3.0, 11.0, 15.0, 9.0),
            box(7.0, 14.0, 9.0, 9.0, 15.0, 14.0),
            box(6.0, 14.0, 12.0, 10.0, 16.0, 13.0),
            box(6.0, 14.0, 10.0, 10.0, 16.0, 11.0),
            box(5.0, 14.0, 10.0, 6.0, 15.0, 11.0),
            box(5.0, 14.0, 12.0, 6.0, 15.0, 13.0),
            box(10.0, 14.0, 10.0, 11.0, 15.0, 11.0),
            box(10.0, 14.0, 12.0, 11.0, 15.0, 13.0),
            box(7.0, 25.0, 5.0, 9.0, 26.0, 7.0),
            box(6.0, 26.0, 4.0, 10.0, 27.0, 8.0),
            box(4.0, 28.0, 0.0, 12.0, 29.0, 16.0),
            box(12.0, 29.0, 0.0, 14.0, 30.0, 16.0),
            box(14.0, 30.0, 0.0, 16.5, 31.0, 16.0),
            box(2.0, 29.0, 0.0, 4.0, 30.0, 16.0),
            box(-0.5, 30.0, 0.0, 2.0, 31.0, 16.0)
         ),
         SOLAR_NEUTRON_ACTIVATOR
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            box(9.0, 1.0, 12.0, 10.0, 5.0, 15.0),
            box(6.0, 1.0, 12.0, 7.0, 5.0, 15.0),
            box(5.0, 5.0, 12.0, 11.0, 11.0, 15.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0)
         ),
         CHARGEPAD
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(5.0, 2.0, 4.0, 11.0, 9.0, 12.0),
            box(8.995, 10.0, 7.5, 9.995, 13.0, 8.5),
            box(11.0, 8.0, 4.0, 12.0, 9.0, 12.0),
            box(11.0, 6.0, 4.0, 12.0, 7.0, 12.0),
            box(11.0, 4.0, 4.0, 12.0, 5.0, 12.0),
            box(11.0, 2.0, 4.0, 12.0, 3.0, 12.0),
            box(4.0, 8.0, 4.0, 5.0, 9.0, 12.0),
            box(4.0, 6.0, 4.0, 5.0, 7.0, 12.0),
            box(4.0, 4.0, 4.0, 5.0, 5.0, 12.0),
            box(4.0, 2.0, 4.0, 5.0, 3.0, 12.0),
            box(5.0, 9.0, 5.0, 11.0, 10.0, 11.0),
            box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0),
            box(10.995, 3.0, 9.0, 11.995, 8.0, 10.0),
            box(10.995, 3.0, 6.0, 11.995, 8.0, 7.0),
            box(4.005, 3.0, 6.0, 5.005, 8.0, 7.0),
            box(4.005, 3.0, 9.0, 5.005, 8.0, 10.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(5.0, 1.0, 5.0, 11.0, 2.0, 11.0),
            box(6.0, 13.0, 6.0, 10.0, 14.0, 10.0),
            box(6.0, 11.0, 6.0, 10.0, 12.0, 10.0)
         ),
         LASER,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(1.0, 1.0, 1.0, 15.0, 15.0, 15.0),
            box(0.0, 3.0, 3.0, 1.0, 13.0, 13.0),
            box(3.0, 3.0, 15.0, 13.0, 13.0, 16.0),
            box(15.0, 3.0, 3.0, 16.0, 13.0, 13.0),
            box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0),
            box(3.0, 3.0, 0.0, 13.0, 13.0, 1.0),
            box(3.0, 15.0, 3.0, 13.0, 16.0, 13.0)
         ),
         LASER_AMPLIFIER,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            box(4.0, 13.0, 1.5, 5.0, 14.0, 14.5),
            box(6.0, 13.0, 1.5, 7.0, 14.0, 14.5),
            box(9.0, 13.0, 1.5, 10.0, 14.0, 14.5),
            box(11.0, 13.0, 1.5, 12.0, 14.0, 14.5),
            box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0),
            box(13.0, 7.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 7.0, 0.0, 3.0, 16.0, 16.0),
            box(3.0, 6.0, 1.0, 13.0, 15.0, 2.0),
            box(3.0, 6.0, 3.0, 13.0, 15.0, 4.0),
            box(3.0, 6.0, 5.0, 13.0, 15.0, 6.0),
            box(3.0, 6.0, 7.0, 13.0, 15.0, 8.0),
            box(3.0, 6.0, 8.0, 13.0, 15.0, 9.0),
            box(3.0, 6.0, 10.0, 13.0, 15.0, 11.0),
            box(3.0, 6.0, 12.0, 13.0, 15.0, 13.0),
            box(3.0, 6.0, 14.0, 13.0, 15.0, 15.0)
         ),
         RESISTIVE_HEATER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 5.0, 3.0),
            box(0.0, 0.0, 13.0, 16.0, 5.0, 16.0),
            box(0.0, 0.0, 3.0, 3.0, 5.0, 13.0),
            box(13.0, 0.0, 3.0, 16.0, 5.0, 13.0),
            box(6.0, 5.0, 14.0, 10.0, 30.0, 16.0),
            box(0.0, 30.0, 0.0, 16.0, 32.0, 16.0),
            box(4.0, 28.0, 6.0, 5.0, 29.0, 7.0),
            box(4.0, 28.0, 8.0, 5.0, 29.0, 9.0),
            box(4.0, 28.0, 10.0, 5.0, 29.0, 11.0),
            box(4.0, 28.0, 12.0, 5.0, 29.0, 13.0),
            box(11.0, 28.0, 12.0, 12.0, 29.0, 13.0),
            box(11.0, 28.0, 10.0, 12.0, 29.0, 11.0),
            box(11.0, 28.0, 8.0, 12.0, 29.0, 9.0),
            box(3.0, 29.0, 3.0, 13.0, 30.0, 14.0),
            box(4.0, 0.0, 4.0, 12.0, 2.0, 12.0),
            box(5.0, 2.0, 5.0, 11.0, 3.0, 11.0),
            box(5.0, 25.0, 5.0, 11.0, 29.0, 14.0),
            box(6.0, 13.0, 6.0, 10.0, 15.0, 10.0),
            box(6.0, 18.0, 6.0, 10.0, 25.0, 10.0),
            box(7.0, 13.0, 10.0, 9.0, 15.0, 14.0),
            box(7.0, 2.0, 7.0, 9.0, 18.0, 9.0),
            box(4.0, 5.0, 15.01, 12.0, 13.0, 16.01),
            box(4.0, 5.0, 16.01, 12.0, 13.0, 16.01),
            box(11.0, 28.0, 6.0, 12.0, 29.0, 7.0),
            box(1.0, 5.0, 1.0, 2.0, 30.0, 15.0),
            box(1.0, 5.0, 14.0, 15.0, 30.0, 15.0),
            box(14.0, 5.0, 1.0, 15.0, 30.0, 15.0)
         ),
         SEISMIC_VIBRATOR
      );
      VoxelShapeUtils.setShape(VoxelShapeUtils.combine(box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0), box(7.0, 7.0, 0.0, 9.0, 11.0, 1.0)), PERSONAL_CHEST);
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 3.0, 3.0, 3.0),
            box(0.0, 0.0, 13.0, 3.0, 3.0, 16.0),
            box(13.0, 0.0, 13.0, 16.0, 3.0, 16.0),
            box(13.0, 0.0, 0.0, 16.0, 3.0, 3.0),
            box(13.0, 13.0, 0.0, 16.0, 16.0, 3.0),
            box(0.0, 13.0, 0.0, 3.0, 16.0, 3.0),
            box(0.0, 13.0, 13.0, 3.0, 16.0, 16.0),
            box(13.0, 13.0, 13.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 3.0, 1.0, 1.0, 13.0),
            box(0.0, 15.0, 3.0, 1.0, 16.0, 13.0),
            box(15.0, 15.0, 3.0, 16.0, 16.0, 13.0),
            box(15.0, 0.0, 3.0, 16.0, 1.0, 13.0),
            box(3.0, 0.0, 0.0, 13.0, 1.0, 1.0),
            box(3.0, 15.0, 0.0, 13.0, 16.0, 1.0),
            box(3.0, 15.0, 15.0, 13.0, 16.0, 16.0),
            box(3.0, 0.0, 15.0, 13.0, 1.0, 16.0),
            box(15.0, 3.0, 15.0, 16.0, 13.0, 16.0),
            box(0.0, 3.0, 15.0, 1.0, 13.0, 16.0),
            box(0.0, 3.0, 0.0, 1.0, 13.0, 1.0),
            box(15.0, 3.0, 0.0, 16.0, 13.0, 1.0),
            box(1.0, 13.0, 3.0, 3.0, 15.0, 13.0),
            box(13.0, 13.0, 3.0, 15.0, 15.0, 13.0),
            box(13.0, 1.0, 3.0, 15.0, 3.0, 13.0),
            box(1.0, 1.0, 3.0, 3.0, 3.0, 13.0),
            box(3.0, 1.0, 13.0, 13.0, 3.0, 15.0),
            box(3.0, 1.0, 1.0, 13.0, 3.0, 3.0),
            box(3.0, 13.0, 1.0, 13.0, 15.0, 3.0),
            box(3.0, 13.0, 13.0, 13.0, 15.0, 15.0),
            box(1.0, 3.0, 13.0, 3.0, 13.0, 15.0),
            box(1.0, 3.0, 1.0, 3.0, 13.0, 3.0),
            box(13.0, 3.0, 1.0, 15.0, 13.0, 3.0),
            box(13.0, 3.0, 13.0, 15.0, 13.0, 15.0),
            box(4.0, 4.0, 0.0, 12.0, 12.0, 1.0),
            box(15.0, 4.0, 4.0, 16.0, 12.0, 12.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),
            box(0.0, 4.0, 4.0, 1.0, 12.0, 12.0),
            box(4.0, 15.0, 4.0, 12.0, 16.0, 12.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0)
         ),
         QUANTUM_ENTANGLOPORTER,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.rotate(
            VoxelShapeUtils.combine(
               box(2.0, 2.0, 15.0, 14.0, 14.0, 16.0),
               box(7.0, 10.0, 2.0, 9.0, 12.0, 3.0),
               box(7.0, 4.0, 2.0, 9.0, 6.0, 3.0),
               box(4.0, 4.0, 13.0, 12.0, 12.0, 14.0),
               box(4.0, 4.0, 11.0, 12.0, 12.0, 12.0),
               box(4.0, 4.0, 9.0, 12.0, 12.0, 10.0),
               box(4.0, 4.0, 7.0, 12.0, 12.0, 8.0),
               box(4.0, 4.0, 5.0, 12.0, 12.0, 6.0),
               box(4.0, 4.0, 3.0, 12.0, 12.0, 4.0),
               box(4.0, 4.0, 1.0, 12.0, 12.0, 2.0),
               box(7.0, 11.0, 4.0, 9.0, 13.0, 5.0),
               box(7.0, 3.0, 4.0, 9.0, 5.0, 5.0),
               box(3.0, 3.0, 14.0, 13.0, 13.0, 15.0),
               box(7.0, 11.0, 9.01, 9.0, 13.0, 14.01),
               box(7.0, 3.0, 9.01, 9.0, 5.0, 14.01),
               box(3.0, 3.0, 0.0, 13.0, 13.0, 1.0),
               box(5.0, 5.0, 1.0, 11.0, 11.0, 15.0),
               box(7.0, 12.0, 5.0, 9.0, 13.0, 9.0),
               box(7.0, 3.0, 5.0, 9.0, 4.0, 9.0),
               box(11.005, 6.5, 4.0, 12.005, 9.5, 11.0),
               box(3.995, 6.5, 4.0, 4.995, 9.5, 11.0),
               box(4.0, 4.0, 16.0, 12.0, 12.0, 17.0),
               box(11.5, 7.5, 8.0, 12.5, 8.5, 9.0),
               box(3.5, 7.5, 8.0, 4.5, 8.5, 9.0),
               box(3.5, 7.5, 6.0, 4.5, 8.5, 7.0),
               box(11.5, 7.5, 6.0, 12.5, 8.5, 7.0)
            ),
            Direction.NORTH
         ),
         LOGISTICAL_SORTER,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 6.0, 0.0, 16.0, 13.0, 16.0),
            box(1.0, 5.0, 1.0, 15.0, 6.0, 15.0),
            box(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
            box(1.0, 14.0, 11.0, 15.0, 24.0, 13.0),
            box(2.0, 15.0, 10.99, 14.0, 23.0, 10.99),
            box(2.0, 13.5, 11.5, 3.0, 14.5, 12.5),
            box(2.0, 16.0, 13.0, 14.0, 22.0, 14.0),
            box(4.0, 13.0, 10.0, 12.0, 14.0, 14.0),
            box(7.0, 14.0, 13.0, 9.0, 21.0, 14.0),
            box(3.0, 13.0, 2.0, 13.0, 14.0, 7.0)
         ),
         SECURITY_DESK
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(6.0, 13.0, 4.0, 10.0, 14.0, 5.0),
            box(10.0, 13.0, 4.0, 12.0, 16.0, 5.0),
            box(11.0, 13.0, 5.0, 12.0, 16.0, 11.0),
            box(4.0, 13.0, 11.0, 12.0, 16.0, 12.0),
            box(4.0, 13.0, 5.0, 5.0, 16.0, 11.0),
            box(4.0, 13.0, 4.0, 6.0, 16.0, 5.0),
            box(3.0, 1.0, 3.0, 13.0, 13.0, 13.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(6.5, 14.0, 6.5, 9.5, 15.0, 9.5),
            box(7.0, 12.0, 7.0, 9.0, 14.0, 9.0)
         ),
         CHEMICAL_TANK
      );
      VoxelShapeUtils.setShape(VoxelShapeUtils.combine(box(5.0, 15.0, 5.0, 11.0, 16.0, 11.0), box(6.0, 11.0, 6.0, 10.0, 15.0, 10.0)), INDUSTRIAL_ALARM, true);
      VoxelShapeUtils.setShape(VoxelShapeUtils.combine(box(1.0, 15.0, 1.0, 15.0, 16.0, 15.0)), QIO_DASHBOARD, true);
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 12.0, 0.0, 16.0, 16.0, 9.0),
            box(0.0, 0.0, 9.0, 16.0, 16.0, 16.0),
            box(0.0, 6.0, 0.0, 16.0, 9.0, 9.0),
            box(0.0, 0.0, 0.0, 16.0, 3.0, 9.0),
            box(0.0, 3.0, 0.0, 1.0, 6.0, 1.0),
            box(0.0, 9.0, 0.0, 1.0, 12.0, 1.0),
            box(15.0, 3.0, 0.0, 16.0, 6.0, 1.0),
            box(15.0, 9.0, 0.0, 16.0, 12.0, 1.0)
         ),
         QIO_DRIVE_ARRAY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(5.0, 4.0, 5.0, 11.0, 5.0, 6.0),
            box(5.0, 4.0, 10.0, 11.0, 5.0, 11.0),
            box(5.0, 4.0, 6.0, 6.0, 5.0, 10.0),
            box(10.0, 4.0, 6.0, 11.0, 5.0, 10.0),
            box(10.0, 2.0, 6.0, 11.0, 3.0, 10.0),
            box(5.0, 2.0, 6.0, 6.0, 3.0, 10.0),
            box(5.0, 2.0, 10.0, 11.0, 3.0, 11.0),
            box(5.0, 2.0, 5.0, 11.0, 3.0, 6.0),
            box(9.0, 1.0, 6.0, 10.0, 6.0, 7.0),
            box(9.0, 1.0, 9.0, 10.0, 6.0, 10.0),
            box(6.0, 1.0, 9.0, 7.0, 6.0, 10.0),
            box(6.0, 1.0, 6.0, 7.0, 6.0, 7.0),
            box(9.0, 8.0, 9.0, 10.0, 9.0, 10.0),
            box(6.0, 9.0, 6.0, 7.0, 10.0, 7.0),
            box(7.5, 1.0, 7.5, 8.5, 6.0, 8.5),
            box(5.0, 6.0, 5.0, 11.0, 7.0, 11.0),
            box(9.0, 7.0, 9.0, 10.0, 8.0, 10.0),
            box(6.0, 7.0, 6.0, 7.0, 9.0, 7.0)
         ),
         QIO_IMPORTER,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(5.0, 4.0, 5.0, 11.0, 5.0, 6.0),
            box(5.0, 4.0, 10.0, 11.0, 5.0, 11.0),
            box(5.0, 4.0, 6.0, 6.0, 5.0, 10.0),
            box(10.0, 4.0, 6.0, 11.0, 5.0, 10.0),
            box(10.0, 2.0, 6.0, 11.0, 3.0, 10.0),
            box(5.0, 2.0, 6.0, 6.0, 3.0, 10.0),
            box(5.0, 2.0, 10.0, 11.0, 3.0, 11.0),
            box(5.0, 2.0, 5.0, 11.0, 3.0, 6.0),
            box(9.0, 1.0, 6.0, 10.0, 6.0, 7.0),
            box(9.0, 1.0, 9.0, 10.0, 6.0, 10.0),
            box(6.0, 1.0, 9.0, 7.0, 6.0, 10.0),
            box(6.0, 1.0, 6.0, 7.0, 6.0, 7.0),
            box(7.0, 7.0, 7.0, 9.0, 8.0, 9.0),
            box(7.0, 8.01, 7.0, 9.0, 9.01, 9.0),
            box(7.5, 1.0, 7.5, 8.5, 6.0, 8.5),
            box(5.0, 6.0, 5.0, 11.0, 7.0, 11.0),
            box(6.0, 8.0, 6.0, 10.0, 9.0, 10.0),
            box(9.0, 9.0, 9.0, 10.0, 10.0, 10.0),
            box(6.0, 9.0, 9.0, 7.0, 10.0, 10.0),
            box(6.0, 9.0, 6.0, 7.0, 10.0, 7.0),
            box(9.0, 9.0, 6.0, 10.0, 10.0, 7.0)
         ),
         QIO_EXPORTER,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(5.0, 2.0, 5.0, 11.0, 3.0, 6.0),
            box(5.0, 2.0, 10.0, 11.0, 3.0, 11.0),
            box(5.0, 2.0, 6.0, 6.0, 3.0, 10.0),
            box(10.0, 2.0, 6.0, 11.0, 3.0, 10.0),
            box(9.0, 1.0, 6.0, 10.0, 4.0, 7.0),
            box(9.0, 1.0, 9.0, 10.0, 4.0, 10.0),
            box(6.0, 1.0, 9.0, 7.0, 4.0, 10.0),
            box(6.0, 1.0, 6.0, 7.0, 4.0, 7.0),
            box(7.5, 1.0, 7.5, 8.5, 4.0, 8.5),
            box(5.0, 4.0, 5.0, 11.0, 5.0, 11.0),
            box(6.0, 8.0, 6.0, 10.0, 9.0, 10.0),
            box(7.0, 5.0, 7.0, 9.0, 11.0, 9.0)
         ),
         QIO_REDSTONE_ADAPTER,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(-16.0, 13.0, 0.0, 16.0, 16.0, 16.0),
            box(13.0, 0.0, 0.0, 16.0, 13.0, 3.0),
            box(0.0, 0.0, 0.0, 3.0, 13.0, 3.0),
            box(-16.0, 8.0, 0.0, 0.0, 11.0, 16.0),
            box(0.0, 8.0, 3.0, 16.0, 11.0, 16.0),
            box(0.0, 0.0, 3.0, 16.0, 6.0, 16.0),
            box(-16.0, 0.0, 0.0, 0.0, 6.0, 16.0),
            box(-15.0, 11.0, 1.0, 0.0, 13.0, 15.0),
            box(-15.0, 6.0, 1.0, 0.0, 8.0, 15.0),
            box(0.0, 11.0, 3.0, 15.0, 13.0, 15.0),
            box(0.0, 6.0, 3.0, 15.0, 8.0, 15.0),
            box(1.0, 17.5, 10.5, 15.0, 25.5, 12.5),
            box(2.0, 17.0, 11.0, 3.0, 18.0, 12.0),
            box(2.0, 15.0, 1.0, 14.0, 17.0, 7.0),
            box(6.0, 16.0, 11.0, 10.0, 22.0, 13.0),
            box(5.0, 16.0, 10.0, 11.0, 17.0, 14.0),
            box(-14.0, 16.0, 2.0, -1.0, 17.0, 14.0),
            box(-14.0, 16.0, 14.0, -11.0, 30.0, 16.0),
            box(-14.0, 6.0, 14.0, -11.0, 16.0, 16.0),
            box(-4.0, 16.0, 14.0, -1.0, 30.0, 16.0),
            box(-4.0, 6.0, 14.0, -1.0, 16.0, 16.0),
            box(-11.0, 23.0, 8.0, -10.0, 27.0, 9.0),
            box(-11.0, 22.0, 8.0, -10.0, 23.0, 9.0),
            box(-5.0, 23.0, 8.0, -4.0, 27.0, 9.0),
            box(-5.0, 22.0, 8.0, -4.0, 23.0, 9.0),
            box(-13.0, 27.0, 7.0, -2.0, 28.0, 10.0),
            box(-11.0, 28.0, 6.0, -4.0, 30.0, 11.0),
            box(-14.0, 28.0, 6.0, -11.0, 30.0, 14.0),
            box(-4.0, 28.0, 6.0, -1.0, 30.0, 14.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0)
         ),
         MODIFICATION_STATION
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(1.0, 2.0, 14.0, 2.0, 3.0, 15.0),
            box(1.0, 2.0, 1.0, 2.0, 3.0, 2.0),
            box(14.0, 2.0, 1.0, 15.0, 3.0, 2.0),
            box(14.0, 2.0, 14.0, 15.0, 3.0, 15.0),
            box(3.0, 2.0, 3.0, 13.0, 16.0, 13.0),
            box(3.0, 27.0, 3.0, 13.0, 30.0, 13.0),
            box(3.0, 16.0, 3.0, 6.0, 27.0, 6.0),
            box(10.0, 16.0, 3.0, 13.0, 27.0, 6.0),
            box(3.0, 16.0, 10.0, 6.0, 27.0, 13.0),
            box(10.0, 16.0, 10.0, 13.0, 27.0, 13.0),
            box(4.0, 16.0, 4.0, 12.0, 27.0, 12.0),
            box(2.0, 9.0, 2.0, 14.0, 10.0, 14.0),
            box(2.0, 7.0, 2.0, 14.0, 8.0, 14.0),
            box(2.0, 5.0, 2.0, 14.0, 6.0, 14.0),
            box(2.0, 3.0, 2.0, 14.0, 4.0, 14.0),
            box(2.0, 10.0, 10.0, 3.0, 14.0, 11.0),
            box(4.0, 30.0, 4.0, 6.0, 32.0, 6.0),
            box(5.0, 30.0, 10.0, 6.0, 32.0, 11.0),
            box(6.0, 30.0, 6.0, 10.0, 31.0, 10.0),
            box(4.0, 4.0, 0.0, 12.0, 12.0, 1.0),
            box(5.0, 5.0, 1.0, 11.0, 11.0, 3.0),
            box(6.0, 18.0, 7.0, 7.0, 25.0, 8.0),
            box(6.0, 17.0, 7.0, 9.0, 18.0, 8.0),
            box(8.0, 15.0, 7.0, 9.0, 17.0, 8.0)
         ),
         ISOTOPIC_CENTRIFUGE
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(5.0, 1.0, 5.0, 11.0, 10.0, 11.0),
            box(4.995, 8.0, 7.0, 5.995, 13.0, 8.0),
            box(10.005, 8.0, 8.0, 11.005, 13.0, 9.0),
            box(7.0, 15.0, 7.0, 9.0, 16.0, 9.0),
            box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            box(6.0, 10.0, 6.0, 10.0, 15.0, 10.0),
            box(7.0, 3.0, 4.0, 9.0, 8.0, 6.0),
            box(7.0, 3.0, 10.0, 9.0, 8.0, 12.0),
            box(4.0, 3.0, 7.0, 6.0, 8.0, 9.0),
            box(10.0, 3.0, 7.0, 12.0, 8.0, 9.0)
         ),
         SUPERCHARGED_COIL,
         true,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 5.0, 0.0, 16.0, 8.0, 16.0),
            box(1.0, 3.0, 1.0, 15.0, 5.0, 15.0),
            box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
            box(12.0, 8.0, 0.0, 16.0, 16.0, 10.0),
            box(11.0, 9.0, 4.0, 12.0, 14.0, 9.0),
            box(4.0, 9.0, 4.0, 5.0, 14.0, 9.0),
            box(5.0, 11.0, 6.0, 6.0, 12.0, 7.0),
            box(10.0, 11.0, 6.0, 11.0, 12.0, 7.0),
            box(0.0, 8.0, 10.0, 16.0, 16.0, 13.0),
            box(0.0, 13.0, 13.0, 16.0, 16.0, 16.0),
            box(1.0, 11.0, 13.0, 15.0, 12.0, 15.0),
            box(1.0, 9.0, 13.0, 15.0, 10.0, 15.0),
            box(0.0, 8.0, 0.0, 4.0, 16.0, 10.0),
            box(6.0, 8.0, 13.0, 10.0, 13.0, 16.0),
            box(6.0, 3.0, 15.0, 10.0, 5.0, 16.0),
            box(2.0, 8.0, 13.0, 3.0, 13.0, 14.0),
            box(4.0, 8.0, 13.0, 5.0, 13.0, 14.0),
            box(11.0, 8.0, 13.0, 12.0, 13.0, 14.0),
            box(13.0, 8.0, 13.0, 14.0, 13.0, 14.0),
            box(4.0, 4.0, 15.0, 12.0, 12.0, 16.0),
            box(4.0, 8.0, 1.0, 12.0, 15.0, 10.0)
         ),
         ANTIPROTONIC_NUCLEOSYNTHESIZER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            box(5.0, 27.0, 4.0, 11.0, 28.0, 16.0),
            box(6.0, 28.0, 5.0, 10.0, 30.0, 9.0),
            box(1.0, 16.0, 1.0, 15.0, 18.0, 13.0),
            box(2.0, 16.0, 12.0, 14.0, 20.0, 16.0),
            box(6.0, 20.0, 13.0, 10.0, 30.0, 15.0),
            box(5.0, 30.0, 4.0, 11.0, 32.0, 16.0),
            box(7.0, 18.0, 6.0, 9.0, 27.0, 8.0)
         ),
         PIGMENT_MIXER
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(0.0, 0.0, 5.0, 16.0, 4.0, 16.0),
            box(1.0, 0.0, 4.0, 15.0, 15.0, 15.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(11.0, 4.0, 5.0, 16.0, 16.0, 10.0),
            box(0.0, 4.0, 5.0, 5.0, 16.0, 10.0),
            box(11.0, 4.0, 11.0, 16.0, 16.0, 16.0),
            box(0.0, 4.0, 11.0, 5.0, 16.0, 16.0)
         ),
         SMELTING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(1.0, 7.0, 4.0, 15.0, 14.0, 14.0),
            box(2.0, 4.0, 4.0, 14.0, 7.0, 10.0),
            box(5.0, 5.0, 10.0, 11.0, 7.0, 14.0),
            box(0.0, 0.0, 4.0, 16.0, 4.0, 16.0),
            box(0.0, 14.0, 4.0, 16.0, 16.0, 16.0),
            box(0.0, 6.0, 4.0, 2.0, 14.0, 16.0),
            box(14.0, 6.0, 4.0, 16.0, 14.0, 16.0)
         ),
         ENRICHING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(2.0, 0.0, 7.0, 14.0, 4.0, 12.0),
            box(2.0, 10.0, 7.0, 14.0, 12.0, 12.0),
            box(3.0, 4.0, 7.0, 13.0, 12.0, 12.0),
            box(0.0, 12.0, 4.0, 16.0, 16.0, 16.0),
            box(1.0, 0.0, 4.0, 15.0, 12.0, 7.0),
            box(1.0, 0.0, 12.0, 15.0, 12.0, 15.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0)
         ),
         CRUSHING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(0.0, 0.0, 12.0, 16.0, 16.0, 16.0),
            box(0.0, 12.0, 4.0, 16.0, 16.0, 11.0),
            box(0.0, 0.0, 4.0, 16.0, 4.0, 11.0),
            box(1.0, 0.0, 4.0, 15.0, 15.0, 12.0),
            box(0.0, 5.0, 5.0, 16.0, 11.0, 12.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0)
         ),
         COMPRESSING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 12.0, 4.0, 16.0, 16.0, 16.0),
            box(5.0, 4.0, 4.0, 11.0, 12.0, 14.0),
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(0.0, 0.0, 4.0, 16.0, 4.0, 16.0),
            box(1.0, 4.0, 5.0, 15.0, 7.0, 15.0),
            box(1.0, 8.0, 5.0, 15.0, 11.0, 15.0),
            box(3.0, 11.0, 12.0, 13.0, 12.0, 14.0),
            box(3.0, 11.0, 6.0, 13.0, 12.0, 8.0)
         ),
         COMBINING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 12.0, 4.0, 16.0, 16.0, 16.0),
            box(1.0, 0.0, 4.0, 15.0, 15.0, 15.0),
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(11.0, 1.0, 5.0, 16.0, 12.0, 10.0),
            box(0.0, 1.0, 5.0, 5.0, 12.0, 10.0),
            box(0.0, 1.0, 11.0, 16.0, 12.0, 16.0)
         ),
         PURIFYING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 4.0, 12.0, 12.0, 16.0),
            box(0.0, 0.0, 4.0, 16.0, 4.0, 16.0),
            box(0.0, 4.0, 4.0, 7.0, 16.0, 16.0),
            box(9.0, 4.0, 4.0, 16.0, 16.0, 16.0),
            box(7.0, 12.0, 12.0, 9.0, 14.0, 13.0),
            box(7.0, 12.0, 9.0, 9.0, 14.0, 10.0),
            box(7.0, 12.0, 6.0, 9.0, 14.0, 7.0)
         ),
         INJECTING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 4.0, 16.0, 4.0, 16.0),
            box(2.0, 4.0, 4.0, 14.0, 13.0, 14.0),
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(0.0, 13.0, 4.0, 16.0, 16.0, 16.0),
            box(0.0, 9.0, 4.0, 4.0, 12.0, 16.0),
            box(12.0, 9.0, 4.0, 16.0, 12.0, 16.0),
            box(0.0, 5.0, 4.0, 5.0, 8.0, 16.0),
            box(11.0, 5.0, 4.0, 16.0, 8.0, 16.0)
         ),
         INFUSING_FACTORY
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 4.0),
            box(4.0, 4.0, 14.0, 12.0, 12.0, 16.0),
            box(0.0, 5.0, 5.0, 7.0, 16.0, 16.0),
            box(9.0, 5.0, 5.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 4.0, 16.0, 4.0, 16.0),
            box(1.0, 4.0, 4.0, 15.0, 15.0, 15.0)
         ),
         SAWING_FACTORY
      );
   }
}
