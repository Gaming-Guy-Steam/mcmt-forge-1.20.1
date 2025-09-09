package mekanism.common.block;

import mekanism.api.RelativeSide;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockEnergyCube extends BlockTile.BlockTileModel<TileEntityEnergyCube, Machine<TileEntityEnergyCube>> {
   private static final VoxelShape[] bounds = new VoxelShape[256];

   private static int getIndex(int top, int bottom, int front, int back, int left, int right, boolean rotateVertical, boolean rotateHorizontal) {
      return top | bottom << 1 | front << 2 | back << 3 | left << 4 | right << 5 | (rotateVertical ? 1 : 0) << 6 | (rotateHorizontal ? 1 : 0) << 7;
   }

   public BlockEnergyCube(Machine<TileEntityEnergyCube> type) {
      super(type, Properties.m_284310_().m_60913_(2.0F, 2.4F).m_60999_().m_60988_().m_284180_(MapColor.f_283875_));
   }

   @Deprecated
   @Override
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull PathComputationType pathType) {
      return false;
   }

   @Deprecated
   @NotNull
   @Override
   public VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      TileEntityEnergyCube energyCube = WorldUtils.getTileEntity(TileEntityEnergyCube.class, world, pos, true);
      int index;
      if (energyCube == null) {
         index = getIndex(1, 1, 1, 1, 1, 1, false, false);
      } else {
         ConfigInfo energyConfig = energyCube.configComponent.getConfig(TransmissionType.ENERGY);
         if (energyConfig == null) {
            index = getIndex(1, 1, 1, 1, 1, 1, false, false);
         } else {
            Direction facing = Attribute.getFacing(state);
            index = getIndex(
               isSideEnabled(energyConfig, facing, Direction.UP),
               isSideEnabled(energyConfig, facing, Direction.DOWN),
               isSideEnabled(energyConfig, facing, Direction.SOUTH),
               isSideEnabled(energyConfig, facing, Direction.NORTH),
               isSideEnabled(energyConfig, facing, Direction.EAST),
               isSideEnabled(energyConfig, facing, Direction.WEST),
               facing == Direction.EAST || facing == Direction.WEST,
               facing == Direction.DOWN || facing == Direction.UP
            );
         }
      }

      return bounds[index];
   }

   private static int isSideEnabled(ConfigInfo energyConfig, Direction facing, Direction side) {
      ISlotInfo slotInfo = energyConfig.getSlotInfo(RelativeSide.fromDirections(facing, side));
      return slotInfo != null && slotInfo.isEnabled() ? 1 : 0;
   }

   static {
      VoxelShape frame = VoxelShapeUtils.combine(
         m_49796_(0.0, 0.0, 0.0, 3.0, 3.0, 16.0),
         m_49796_(0.0, 3.0, 0.0, 3.0, 16.0, 3.0),
         m_49796_(0.0, 3.0, 13.0, 3.0, 16.0, 16.0),
         m_49796_(0.0, 13.0, 3.0, 3.0, 16.0, 13.0),
         m_49796_(3.0, 0.0, 0.0, 16.0, 3.0, 3.0),
         m_49796_(3.0, 0.0, 13.0, 16.0, 3.0, 16.0),
         m_49796_(3.0, 13.0, 0.0, 16.0, 16.0, 3.0),
         m_49796_(3.0, 13.0, 13.0, 16.0, 16.0, 16.0),
         m_49796_(13.0, 0.0, 3.0, 16.0, 3.0, 13.0),
         m_49796_(13.0, 3.0, 0.0, 16.0, 13.0, 3.0),
         m_49796_(13.0, 3.0, 13.0, 16.0, 13.0, 16.0),
         m_49796_(13.0, 13.0, 3.0, 16.0, 16.0, 13.0),
         m_49796_(12.5, 15.0, 7.5, 13.5, 15.9, 8.5),
         m_49796_(2.5, 15.0, 7.5, 3.5, 15.9, 8.5),
         m_49796_(12.5, 7.5, 0.1, 13.5, 8.5, 1.0),
         m_49796_(2.5, 7.5, 0.1, 3.5, 8.5, 1.0),
         m_49796_(2.5, 0.1, 7.5, 3.5, 1.0, 8.5),
         m_49796_(12.5, 0.1, 7.5, 13.5, 1.0, 8.5),
         m_49796_(12.5, 7.5, 15.0, 13.5, 8.5, 15.9),
         m_49796_(2.5, 7.5, 15.0, 3.5, 8.5, 15.9),
         m_49796_(0.1, 7.5, 2.5, 1.0, 8.5, 3.5),
         m_49796_(0.1, 7.5, 12.5, 1.0, 8.5, 13.5),
         m_49796_(15.0, 7.5, 2.5, 15.9, 8.5, 3.5),
         m_49796_(15.0, 7.5, 12.5, 15.9, 8.5, 13.5)
      );
      VoxelShape frontPanel = VoxelShapeUtils.combine(m_49796_(3.0, 5.0, 14.0, 13.0, 11.0, 15.0), m_49796_(4.0, 4.0, 15.0, 12.0, 12.0, 16.0));
      VoxelShape rightPanel = VoxelShapeUtils.combine(m_49796_(1.0, 5.0, 3.0, 2.0, 11.0, 13.0), m_49796_(0.0, 4.0, 4.0, 1.0, 12.0, 12.0));
      VoxelShape leftPanel = VoxelShapeUtils.combine(m_49796_(14.0, 5.0, 3.0, 15.0, 11.0, 13.0), m_49796_(15.0, 4.0, 4.0, 16.0, 12.0, 12.0));
      VoxelShape backPanel = VoxelShapeUtils.combine(m_49796_(3.0, 5.0, 1.0, 13.0, 11.0, 2.0), m_49796_(4.0, 4.0, 0.0, 12.0, 12.0, 1.0));
      VoxelShape topPanel = VoxelShapeUtils.combine(m_49796_(3.0, 14.0, 5.0, 13.0, 15.0, 11.0), m_49796_(4.0, 15.0, 4.0, 12.0, 16.0, 12.0));
      VoxelShape bottomPanel = VoxelShapeUtils.combine(m_49796_(3.0, 1.0, 5.0, 13.0, 2.0, 11.0), m_49796_(4.0, 0.0, 4.0, 12.0, 1.0, 12.0));
      VoxelShape frameRotated = VoxelShapeUtils.rotate(frame, Rotation.CLOCKWISE_90);
      VoxelShape topRotated = VoxelShapeUtils.rotate(topPanel, Rotation.CLOCKWISE_90);
      VoxelShape bottomRotated = VoxelShapeUtils.rotate(bottomPanel, Rotation.CLOCKWISE_90);
      VoxelShape frameRotatedAlt = VoxelShapeUtils.rotate(frame, Direction.NORTH);
      VoxelShape rightRotated = VoxelShapeUtils.rotate(rightPanel, Direction.NORTH);
      VoxelShape leftRotated = VoxelShapeUtils.rotate(leftPanel, Direction.NORTH);

      for (int rotated = 0; rotated < 3; rotated++) {
         boolean rotateVertical = rotated == 1;
         boolean rotateHorizontal = rotated == 2;
         VoxelShape baseFrame = rotateVertical ? frameRotated : (rotateHorizontal ? frameRotatedAlt : frame);

         for (int top = 0; top < 2; top++) {
            VoxelShape withTop = top == 0 ? baseFrame : Shapes.m_83110_(baseFrame, rotateVertical ? topRotated : topPanel);

            for (int bottom = 0; bottom < 2; bottom++) {
               VoxelShape withBottom = bottom == 0 ? withTop : Shapes.m_83110_(withTop, rotateVertical ? bottomRotated : bottomPanel);

               for (int front = 0; front < 2; front++) {
                  VoxelShape withFront = front == 0 ? withBottom : Shapes.m_83110_(withBottom, frontPanel);

                  for (int back = 0; back < 2; back++) {
                     VoxelShape withBack = back == 0 ? withFront : Shapes.m_83110_(withFront, backPanel);

                     for (int left = 0; left < 2; left++) {
                        VoxelShape withLeft = left == 0 ? withBack : Shapes.m_83110_(withBack, rotateHorizontal ? leftRotated : leftPanel);

                        for (int right = 0; right < 2; right++) {
                           VoxelShape withRight = right == 0 ? withLeft : Shapes.m_83110_(withLeft, rotateHorizontal ? rightRotated : rightPanel);
                           bounds[getIndex(top, bottom, front, back, left, right, rotateVertical, rotateHorizontal)] = withRight;
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
