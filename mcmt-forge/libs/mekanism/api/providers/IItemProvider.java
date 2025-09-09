package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IItemProvider extends IBaseProvider, ItemLike {
   default ItemStack getItemStack() {
      return this.getItemStack(1);
   }

   default ItemStack getItemStack(int size) {
      return new ItemStack(this.m_5456_(), size);
   }

   @Override
   default ResourceLocation getRegistryName() {
      return ForgeRegistries.ITEMS.getKey(this.m_5456_());
   }

   @Override
   default String getTranslationKey() {
      return this.m_5456_().m_5524_();
   }
}
