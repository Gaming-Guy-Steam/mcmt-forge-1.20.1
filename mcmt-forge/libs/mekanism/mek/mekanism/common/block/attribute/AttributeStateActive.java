package mekanism.common.block.attribute;

import java.util.List;
import mekanism.common.block.states.BlockStateHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class AttributeStateActive implements AttributeState {
   private static final BooleanProperty activeProperty = BooleanProperty.m_61465_("active");
   private final int ambientLight;

   AttributeStateActive(int ambientLight) {
      this.ambientLight = ambientLight;
   }

   public boolean isActive(BlockState state) {
      return (Boolean)state.m_61143_(activeProperty);
   }

   public BlockState setActive(@NotNull BlockState state, boolean active) {
      return (BlockState)state.m_61124_(activeProperty, active);
   }

   @Override
   public BlockState copyStateData(BlockState oldState, BlockState newState) {
      if (Attribute.has(newState, AttributeStateActive.class)) {
         newState = (BlockState)newState.m_61124_(activeProperty, (Boolean)oldState.m_61143_(activeProperty));
      }

      return newState;
   }

   @Override
   public BlockState getDefaultState(@NotNull BlockState state) {
      return (BlockState)state.m_61124_(activeProperty, false);
   }

   @Override
   public void fillBlockStateContainer(Block block, List<Property<?>> properties) {
      properties.add(activeProperty);
   }

   @Override
   public void adjustProperties(Properties props) {
      if (this.ambientLight > 0) {
         BlockStateHelper.applyLightLevelAdjustments(props, state -> this.isActive(state) ? this.ambientLight : 0);
      }
   }
}
