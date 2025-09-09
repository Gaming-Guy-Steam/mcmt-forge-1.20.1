package mekanism.common.block.attribute;

import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AttributeStateFacing implements AttributeState {
   private final DirectionProperty facingProperty;
   private final AttributeStateFacing.FacePlacementType placementType;
   private final boolean canRotate;

   public AttributeStateFacing() {
      this(true);
   }

   public AttributeStateFacing(boolean canRotate) {
      this(BlockStateProperties.f_61374_, canRotate);
   }

   public AttributeStateFacing(DirectionProperty facingProperty) {
      this(facingProperty, true);
   }

   public AttributeStateFacing(DirectionProperty facingProperty, boolean canRotate) {
      this(facingProperty, AttributeStateFacing.FacePlacementType.PLAYER_LOCATION, canRotate);
   }

   public AttributeStateFacing(DirectionProperty facingProperty, AttributeStateFacing.FacePlacementType placementType) {
      this(facingProperty, placementType, true);
   }

   public AttributeStateFacing(DirectionProperty facingProperty, AttributeStateFacing.FacePlacementType placementType, boolean canRotate) {
      this.facingProperty = facingProperty;
      this.placementType = placementType;
      this.canRotate = canRotate;
   }

   public boolean canRotate() {
      return this.canRotate;
   }

   public Direction getDirection(BlockState state) {
      return (Direction)state.m_61143_(this.getFacingProperty());
   }

   public BlockState setDirection(@NotNull BlockState state, Direction newDirection) {
      return this.supportsDirection(newDirection) ? (BlockState)state.m_61124_(this.getFacingProperty(), newDirection) : state;
   }

   @NotNull
   public DirectionProperty getFacingProperty() {
      return this.facingProperty;
   }

   @NotNull
   public AttributeStateFacing.FacePlacementType getPlacementType() {
      return this.placementType;
   }

   public Collection<Direction> getSupportedDirections() {
      return this.getFacingProperty().m_6908_();
   }

   public boolean supportsDirection(Direction direction) {
      return this.getSupportedDirections().contains(direction);
   }

   @Override
   public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
      properties.add(this.getFacingProperty());
   }

   @Override
   public BlockState copyStateData(BlockState oldState, BlockState newState) {
      AttributeStateFacing newStateFacingAttribute = Attribute.get(newState, AttributeStateFacing.class);
      if (newStateFacingAttribute != null) {
         DirectionProperty oldFacingProperty = Attribute.get(oldState, AttributeStateFacing.class).getFacingProperty();
         newState = (BlockState)newState.m_61124_(newStateFacingAttribute.getFacingProperty(), (Direction)oldState.m_61143_(oldFacingProperty));
      }

      return newState;
   }

   @Contract("_, null, _, _, _, _ -> null")
   @Override
   public BlockState getStateForPlacement(
      Block block, @Nullable BlockState state, @NotNull LevelAccessor world, @NotNull BlockPos pos, @Nullable Player player, @NotNull Direction face
   ) {
      if (state == null) {
         return null;
      } else {
         AttributeStateFacing blockFacing = Attribute.get(block, AttributeStateFacing.class);
         Direction newDirection = Direction.SOUTH;
         if (blockFacing.getPlacementType() == AttributeStateFacing.FacePlacementType.PLAYER_LOCATION) {
            if (blockFacing.supportsDirection(Direction.DOWN) && blockFacing.supportsDirection(Direction.UP)) {
               float rotationPitch = player == null ? 0.0F : player.m_146909_();
               int height = Math.round(rotationPitch);
               if (height >= 65) {
                  newDirection = Direction.UP;
               } else if (height <= -65) {
                  newDirection = Direction.DOWN;
               }
            }

            if (newDirection != Direction.DOWN && newDirection != Direction.UP) {
               float placementYaw = player == null ? 0.0F : player.m_146908_();
               int side = Mth.m_14107_(placementYaw * 4.0F / 360.0F + 0.5) & 3;

               newDirection = switch (side) {
                  case 0 -> Direction.NORTH;
                  case 1 -> Direction.EAST;
                  case 2 -> Direction.SOUTH;
                  case 3 -> Direction.WEST;
                  default -> newDirection;
               };
            }
         } else {
            newDirection = blockFacing.supportsDirection(face) ? face : Direction.NORTH;
         }

         return blockFacing.setDirection(state, newDirection);
      }
   }

   public static BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation) {
      return rotate(state, rotation);
   }

   public static BlockState rotate(BlockState state, Rotation rotation) {
      AttributeStateFacing blockFacing = Attribute.get(state, AttributeStateFacing.class);
      return blockFacing != null && blockFacing.canRotate() ? rotate(blockFacing, blockFacing.getFacingProperty(), state, rotation) : state;
   }

   public static BlockState mirror(BlockState state, Mirror mirror) {
      AttributeStateFacing blockFacing = Attribute.get(state, AttributeStateFacing.class);
      if (blockFacing != null && blockFacing.canRotate()) {
         DirectionProperty property = blockFacing.getFacingProperty();
         return rotate(blockFacing, property, state, mirror.m_54846_((Direction)state.m_61143_(property)));
      } else {
         return state;
      }
   }

   private static BlockState rotate(AttributeStateFacing blockFacing, DirectionProperty property, BlockState state, Rotation rotation) {
      return blockFacing.setDirection(state, rotation.m_55954_((Direction)state.m_61143_(property)));
   }

   public static enum FacePlacementType {
      PLAYER_LOCATION,
      SELECTED_FACE;
   }
}
