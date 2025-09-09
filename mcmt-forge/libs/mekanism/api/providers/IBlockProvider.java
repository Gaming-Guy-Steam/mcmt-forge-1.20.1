package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IBlockProvider extends IItemProvider {
   Block getBlock();

   @Override
   default ResourceLocation getRegistryName() {
      return ForgeRegistries.BLOCKS.getKey(this.getBlock());
   }

   @Override
   default String getTranslationKey() {
      return this.getBlock().m_7705_();
   }
}
