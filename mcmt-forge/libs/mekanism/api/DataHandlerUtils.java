package mekanism.api;

import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.IFluidTank;

@NothingNullByDefault
public class DataHandlerUtils {
   private DataHandlerUtils() {
   }

   public static void readContainers(List<? extends INBTSerializable<CompoundTag>> containers, ListTag storedContainers) {
      readContents(containers, storedContainers, getTagByType(containers));
   }

   public static ListTag writeContainers(List<? extends INBTSerializable<CompoundTag>> containers) {
      return writeContents(containers, getTagByType(containers));
   }

   public static void readContents(List<? extends INBTSerializable<CompoundTag>> contents, ListTag storedContents, String key) {
      int size = contents.size();

      for (int tagCount = 0; tagCount < storedContents.size(); tagCount++) {
         CompoundTag tagCompound = storedContents.m_128728_(tagCount);
         byte id = tagCompound.m_128445_(key);
         if (id >= 0 && id < size) {
            contents.get(id).deserializeNBT(tagCompound);
         }
      }
   }

   public static ListTag writeContents(List<? extends INBTSerializable<CompoundTag>> contents, String key) {
      ListTag storedContents = new ListTag();

      for (int tank = 0; tank < contents.size(); tank++) {
         CompoundTag tagCompound = (CompoundTag)contents.get(tank).serializeNBT();
         if (!tagCompound.m_128456_()) {
            tagCompound.m_128344_(key, (byte)tank);
            storedContents.add(tagCompound);
         }
      }

      return storedContents;
   }

   private static String getTagByType(List<? extends INBTSerializable<CompoundTag>> containers) {
      if (containers.isEmpty()) {
         return "Container";
      } else {
         INBTSerializable<CompoundTag> obj = (INBTSerializable<CompoundTag>)containers.get(0);
         if (obj instanceof IChemicalTank || obj instanceof IFluidTank) {
            return "Tank";
         } else if (obj instanceof IHeatCapacitor || obj instanceof IEnergyContainer) {
            return "Container";
         } else {
            return obj instanceof IInventorySlot ? "Slot" : "Container";
         }
      }
   }

   public static int getMaxId(ListTag storedContents, String key) {
      int maxId = -1;

      for (int tagCount = 0; tagCount < storedContents.size(); tagCount++) {
         byte id = storedContents.m_128728_(tagCount).m_128445_(key);
         if (id > maxId) {
            maxId = id;
         }
      }

      return maxId + 1;
   }
}
