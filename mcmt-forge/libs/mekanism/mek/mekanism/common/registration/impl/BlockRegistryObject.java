package mekanism.common.registration.impl;

import mekanism.api.providers.IBlockProvider;
import mekanism.common.registration.DoubleWrappedRegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class BlockRegistryObject<BLOCK extends Block, ITEM extends Item> extends DoubleWrappedRegistryObject<BLOCK, ITEM> implements IBlockProvider {
   public BlockRegistryObject(RegistryObject<BLOCK> blockRegistryObject, RegistryObject<ITEM> itemRegistryObject) {
      super(blockRegistryObject, itemRegistryObject);
   }

   @NotNull
   @Override
   public BLOCK getBlock() {
      return this.getPrimary();
   }

   @NotNull
   public ITEM m_5456_() {
      return this.getSecondary();
   }
}
