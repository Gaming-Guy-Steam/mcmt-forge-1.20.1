package mekanism.common.lib;

import java.util.List;
import mekanism.common.content.transporter.TransporterPathfinder;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record SidedBlockPos(BlockPos pos, Direction side) {
   public SidedBlockPos(BlockPos pos, Direction side) {
      pos = pos.m_7949_();
      this.pos = pos;
      this.side = side;
   }

   public static SidedBlockPos get(TransporterPathfinder.Destination destination) {
      List<BlockPos> path = destination.getPath();
      BlockPos pos = path.get(0);
      Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
      return new SidedBlockPos(pos, sideOfDest);
   }

   @Nullable
   public static SidedBlockPos deserialize(CompoundTag tag) {
      if (tag.m_128425_("x", 3) && tag.m_128425_("y", 3) && tag.m_128425_("z", 3) && tag.m_128425_("side", 3)) {
         BlockPos pos = new BlockPos(tag.m_128451_("x"), tag.m_128451_("y"), tag.m_128451_("z"));
         Direction side = Direction.m_122376_(tag.m_128451_("side"));
         return new SidedBlockPos(pos, side);
      } else {
         return null;
      }
   }

   public CompoundTag serialize() {
      CompoundTag target = new CompoundTag();
      target.m_128405_("x", this.pos.m_123341_());
      target.m_128405_("y", this.pos.m_123342_());
      target.m_128405_("z", this.pos.m_123343_());
      NBTUtils.writeEnum(target, "side", this.side);
      return target;
   }
}
