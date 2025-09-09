package mekanism.common.block.transmitter;

import java.util.function.UnaryOperator;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BlockSmallTransmitter extends BlockTransmitter {
   private static final VoxelShape[] SIDES = new VoxelShape[EnumUtils.DIRECTIONS.length];
   private static final VoxelShape[] SIDES_PULL = new VoxelShape[EnumUtils.DIRECTIONS.length];
   private static final VoxelShape[] SIDES_PUSH = new VoxelShape[EnumUtils.DIRECTIONS.length];
   public static final VoxelShape CENTER = m_49796_(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

   public static VoxelShape getSideForType(ConnectionType type, Direction side) {
      if (type == ConnectionType.PUSH) {
         return SIDES_PUSH[side.ordinal()];
      } else {
         return type == ConnectionType.PULL ? SIDES_PULL[side.ordinal()] : SIDES[side.ordinal()];
      }
   }

   protected BlockSmallTransmitter(UnaryOperator<Properties> propertiesModifier) {
      super(propertiesModifier);
   }

   @Override
   protected VoxelShape getCenter() {
      return CENTER;
   }

   @Override
   protected VoxelShape getSide(ConnectionType type, Direction side) {
      return getSideForType(type, side);
   }

   static {
      VoxelShapeUtils.setShape(m_49796_(5.0, 0.0, 5.0, 11.0, 5.0, 11.0), SIDES, true);
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(m_49796_(5.0, 4.0, 5.0, 11.0, 5.0, 11.0), m_49796_(6.0, 2.0, 6.0, 10.0, 4.0, 10.0), m_49796_(4.0, 0.0, 4.0, 12.0, 2.0, 12.0)),
         SIDES_PULL,
         true
      );
      VoxelShapeUtils.setShape(
         VoxelShapeUtils.combine(m_49796_(5.0, 3.0, 5.0, 11.0, 5.0, 11.0), m_49796_(6.0, 1.0, 6.0, 10.0, 3.0, 10.0), m_49796_(7.0, 0.0, 7.0, 9.0, 1.0, 9.0)),
         SIDES_PUSH,
         true
      );
   }
}
