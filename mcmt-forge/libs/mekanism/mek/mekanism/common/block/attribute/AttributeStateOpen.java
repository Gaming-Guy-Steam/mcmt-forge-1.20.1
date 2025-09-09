package mekanism.common.block.attribute;

import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AttributeStateOpen implements AttributeState {
   public static final AttributeStateOpen INSTANCE = new AttributeStateOpen();

   @Override
   public BlockState copyStateData(BlockState oldState, BlockState newState) {
      if (Attribute.has(newState, AttributeStateOpen.class)) {
         newState = (BlockState)newState.m_61124_(BlockStateProperties.f_61446_, (Boolean)oldState.m_61143_(BlockStateProperties.f_61446_));
      }

      return newState;
   }

   @Override
   public BlockState getDefaultState(@NotNull BlockState state) {
      return (BlockState)state.m_61124_(BlockStateProperties.f_61446_, false);
   }

   @Override
   public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
      properties.add(BlockStateProperties.f_61446_);
   }
}
