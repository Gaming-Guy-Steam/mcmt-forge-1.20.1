package mekanism.api.inventory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
public interface IHashedItem {
   ItemStack getInternalStack();

   ItemStack createStack(int var1);

   default Item getItem() {
      return this.getInternalStack().m_41720_();
   }

   default int getMaxStackSize() {
      return this.getInternalStack().m_41741_();
   }

   @Nullable
   default CompoundTag getInternalTag() {
      return this.getInternalStack().m_41783_();
   }
}
