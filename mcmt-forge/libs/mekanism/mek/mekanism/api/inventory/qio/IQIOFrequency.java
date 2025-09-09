package mekanism.api.inventory.qio;

import java.util.function.ObjLongConsumer;
import mekanism.api.Action;
import mekanism.api.IFrequency;
import mekanism.api.inventory.IHashedItem;
import net.minecraft.world.item.ItemStack;

public interface IQIOFrequency extends IFrequency {
   long getStored(ItemStack var1);

   void forAllStored(ObjLongConsumer<ItemStack> var1);

   void forAllHashedStored(ObjLongConsumer<IHashedItem> var1);

   long massInsert(ItemStack var1, long var2, Action var4);

   long massExtract(ItemStack var1, long var2, Action var4);
}
