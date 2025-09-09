package mekanism.common.lib.multiblock;

import java.util.Collection;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;

public interface IValveHandler {
   default void writeValves(CompoundTag updateTag) {
      ListTag valves = new ListTag();

      for (IValveHandler.ValveData valveData : this.getValveData()) {
         if (valveData.activeTicks > 0) {
            CompoundTag valveNBT = new CompoundTag();
            valveNBT.m_128365_("position", NbtUtils.m_129224_(valveData.location));
            NBTUtils.writeEnum(valveNBT, "side", valveData.side);
            valves.add(valveNBT);
         }
      }

      updateTag.m_128365_("valve", valves);
   }

   default void readValves(CompoundTag updateTag) {
      this.getValveData().clear();
      if (updateTag.m_128425_("valve", 9)) {
         ListTag valves = updateTag.m_128437_("valve", 10);

         for (int i = 0; i < valves.size(); i++) {
            CompoundTag valveNBT = valves.m_128728_(i);
            NBTUtils.setBlockPosIfPresent(valveNBT, "position", pos -> {
               Direction side = Direction.m_122376_(valveNBT.m_128451_("side"));
               this.getValveData().add(new IValveHandler.ValveData(pos, side));
            });
         }
      }
   }

   default void triggerValveTransfer(IMultiblock<?> multiblock) {
      if (multiblock.getMultiblock().isFormed()) {
         for (IValveHandler.ValveData data : this.getValveData()) {
            if (multiblock.getTilePos().equals(data.location)) {
               data.onTransfer();
               break;
            }
         }
      }
   }

   Collection<IValveHandler.ValveData> getValveData();

   public static class ValveData {
      public final BlockPos location;
      public final Direction side;
      public boolean prevActive;
      public int activeTicks;

      public ValveData(BlockPos location, Direction side) {
         this.location = location;
         this.side = side;
      }

      public void onTransfer() {
         this.activeTicks = 30;
      }

      @Override
      public int hashCode() {
         int code = 1;
         code = 31 * code + this.side.ordinal();
         return 31 * code + this.location.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
         return obj instanceof IValveHandler.ValveData other && other.side == this.side && other.location.equals(this.location);
      }
   }
}
