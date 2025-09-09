package mekanism.common.block.states;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StatePredicate;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockStateHelper {
   public static final BooleanProperty storageProperty = BooleanProperty.m_61465_("storage");
   public static final EnumProperty<FluidLogType> FLUID_LOGGED = EnumProperty.m_61587_("fluid_logged", FluidLogType.class);
   public static final StatePredicate NEVER_PREDICATE = (state, world, pos) -> false;
   public static final StatePredicate ALWAYS_PREDICATE = (state, world, pos) -> true;

   private BlockStateHelper() {
   }

   public static BlockState getDefaultState(@NotNull BlockState state) {
      Block block = state.m_60734_();

      for (Attribute attr : Attribute.getAll(block)) {
         if (attr instanceof AttributeState atr) {
            state = atr.getDefaultState(state);
         }
      }

      if (block instanceof IStateFluidLoggable fluidLoggable) {
         state = fluidLoggable.setState(state, Fluids.f_76191_);
      }

      if (block instanceof IStateStorage) {
         state = (BlockState)state.m_61124_(storageProperty, false);
      }

      return state;
   }

   public static void fillBlockStateContainer(Block block, Builder<Block, BlockState> builder) {
      List<Property<?>> properties = new ArrayList<>();

      for (Attribute attr : Attribute.getAll(block)) {
         if (attr instanceof AttributeState atr) {
            atr.fillBlockStateContainer(block, properties);
         }
      }

      if (block instanceof IStateStorage) {
         properties.add(storageProperty);
      }

      if (block instanceof IStateFluidLoggable fluidLoggable) {
         properties.add(fluidLoggable.getFluidLoggedProperty());
      }

      if (!properties.isEmpty()) {
         builder.m_61104_(properties.toArray(new Property[0]));
      }
   }

   public static Properties applyLightLevelAdjustments(Properties properties) {
      return applyLightLevelAdjustments(
         properties, state -> state.m_60734_() instanceof IStateFluidLoggable fluidLoggable ? fluidLoggable.getFluidLightLevel(state) : 0
      );
   }

   public static Properties applyLightLevelAdjustments(Properties properties, ToIntFunction<BlockState> toApply) {
      ToIntFunction<BlockState> existingLightLevelFunction = properties.f_60886_;
      return properties.m_60953_(state -> Math.max(existingLightLevelFunction.applyAsInt(state), toApply.applyAsInt(state)));
   }

   @Contract("_, null, _ -> null")
   public static BlockState getStateForPlacement(Block block, @Nullable BlockState state, BlockPlaceContext context) {
      return getStateForPlacement(block, state, context.m_43725_(), context.m_8083_(), context.m_43723_(), context.m_43719_());
   }

   @Contract("_, null, _, _, _, _ -> null")
   public static BlockState getStateForPlacement(
      Block block, @Nullable BlockState state, @NotNull LevelAccessor world, @NotNull BlockPos pos, @Nullable Player player, @NotNull Direction face
   ) {
      if (state == null) {
         return null;
      } else {
         for (Attribute attr : Attribute.getAll(block)) {
            if (attr instanceof AttributeState atr) {
               state = atr.getStateForPlacement(block, state, world, pos, player, face);
            }
         }

         if (block instanceof IStateFluidLoggable fluidLoggable) {
            FluidState fluidState = world.m_6425_(pos);
            state = fluidLoggable.setState(state, fluidState.m_76152_());
         }

         return state;
      }
   }

   public static BlockState copyStateData(BlockState oldState, @Nullable IBlockProvider newBlockProvider) {
      return newBlockProvider == null ? oldState : copyStateData(oldState, newBlockProvider.getBlock().m_49966_());
   }

   public static BlockState copyStateData(BlockState oldState, BlockState newState) {
      Block oldBlock = oldState.m_60734_();
      Block newBlock = newState.m_60734_();

      for (Attribute attr : Attribute.getAll(oldBlock)) {
         if (attr instanceof AttributeState atr) {
            newState = atr.copyStateData(oldState, newState);
         }
      }

      if (oldBlock instanceof IStateStorage && newBlock instanceof IStateStorage) {
         newState = (BlockState)newState.m_61124_(storageProperty, (Boolean)oldState.m_61143_(storageProperty));
      }

      if (newBlock instanceof IStateFluidLoggable newFluidLoggable) {
         FluidState oldFluidState = oldState.m_60819_();
         if (!oldFluidState.m_76178_()) {
            newState = newFluidLoggable.setState(newState, oldFluidState.m_76152_());
         }
      }

      return newState;
   }
}
