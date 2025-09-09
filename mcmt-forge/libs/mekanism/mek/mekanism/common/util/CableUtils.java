package mekanism.common.util;

import java.util.EnumSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class CableUtils {
   private CableUtils() {
   }

   public static void emit(IEnergyContainer energyContainer, BlockEntity from) {
      emit(EnumSet.allOf(Direction.class), energyContainer, from);
   }

   public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, BlockEntity from) {
      emit(outputSides, energyContainer, from, energyContainer.getMaxEnergy());
   }

   public static void emit(Set<Direction> outputSides, IEnergyContainer energyContainer, BlockEntity from, FloatingLong maxOutput) {
      if (!energyContainer.isEmpty() && !maxOutput.isZero()) {
         energyContainer.extract(
            emit(outputSides, energyContainer.extract(maxOutput, Action.SIMULATE, AutomationType.INTERNAL), from), Action.EXECUTE, AutomationType.INTERNAL
         );
      }
   }

   public static FloatingLong emit(Set<Direction> sides, FloatingLong energyToSend, BlockEntity from) {
      if (!energyToSend.isZero() && !sides.isEmpty()) {
         EnergyAcceptorTarget target = new EnergyAcceptorTarget(6);
         EmitUtils.forEachSide(
            from.m_58904_(),
            from.m_58899_(),
            sides,
            (acceptor, side) -> EnergyCompatUtils.getLazyStrictEnergyHandler(acceptor, side.m_122424_()).ifPresent(target::addHandler)
         );
         return target.getHandlerCount() > 0 ? EmitUtils.sendToAcceptors(target, energyToSend) : FloatingLong.ZERO;
      } else {
         return FloatingLong.ZERO;
      }
   }
}
