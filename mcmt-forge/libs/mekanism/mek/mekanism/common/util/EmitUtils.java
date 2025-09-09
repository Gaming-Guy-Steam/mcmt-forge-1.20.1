package mekanism.common.util;

import java.util.function.BiConsumer;
import mekanism.api.math.FloatingLong;
import mekanism.common.lib.distribution.FloatingLongSplitInfo;
import mekanism.common.lib.distribution.IntegerSplitInfo;
import mekanism.common.lib.distribution.LongSplitInfo;
import mekanism.common.lib.distribution.SplitInfo;
import mekanism.common.lib.distribution.Target;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EmitUtils {
   private EmitUtils() {
   }

   private static <HANDLER, TYPE extends Number & Comparable<TYPE>, EXTRA, TARGET extends Target<HANDLER, TYPE, EXTRA>> TYPE sendToAcceptors(
      TARGET availableTargets, SplitInfo<TYPE> splitInfo, EXTRA toSend
   ) {
      if (availableTargets.getHandlerCount() == 0) {
         return splitInfo.getTotalSent();
      } else {
         availableTargets.sendPossible(toSend, splitInfo);

         while (splitInfo.amountPerChanged) {
            splitInfo.amountPerChanged = false;
            availableTargets.shiftNeeded(splitInfo);
         }

         availableTargets.sendRemainingSplit(splitInfo);
         return splitInfo.getTotalSent();
      }
   }

   public static <HANDLER, EXTRA, TARGET extends Target<HANDLER, Integer, EXTRA>> int sendToAcceptors(TARGET availableTargets, int amountToSplit, EXTRA toSend) {
      return sendToAcceptors(availableTargets, new IntegerSplitInfo(amountToSplit, availableTargets.getHandlerCount()), toSend);
   }

   public static <HANDLER, EXTRA, TARGET extends Target<HANDLER, Long, EXTRA>> long sendToAcceptors(TARGET availableTargets, long amountToSplit, EXTRA toSend) {
      return sendToAcceptors(availableTargets, new LongSplitInfo(amountToSplit, availableTargets.getHandlerCount()), toSend);
   }

   public static <HANDLER, TARGET extends Target<HANDLER, FloatingLong, FloatingLong>> FloatingLong sendToAcceptors(
      TARGET availableTargets, FloatingLong amountToSplit
   ) {
      return sendToAcceptors(availableTargets, new FloatingLongSplitInfo(amountToSplit, availableTargets.getHandlerCount()), amountToSplit);
   }

   public static void forEachSide(Level world, BlockPos center, Iterable<Direction> sides, BiConsumer<BlockEntity, Direction> action) {
      if (sides != null) {
         for (Direction side : sides) {
            BlockEntity tile = WorldUtils.getTileEntity(world, center.m_121945_(side));
            if (tile != null) {
               action.accept(tile, side);
            }
         }
      }
   }
}
