package mekanism.common.inventory.container.tile;

import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import net.minecraft.world.entity.player.Inventory;

public class FactoryContainer extends MekanismTileContainer<TileEntityFactory<?>> {
   public FactoryContainer(int id, Inventory inv, TileEntityFactory<?> tile) {
      super(MekanismContainerTypes.FACTORY, id, inv, tile);
   }

   @Override
   protected int getInventoryYOffset() {
      if (this.tile.hasSecondaryResourceBar()) {
         return 95;
      } else {
         return this.tile instanceof TileEntitySawingFactory ? 105 : 85;
      }
   }

   @Override
   protected int getInventoryXOffset() {
      return this.tile.tier == FactoryTier.ULTIMATE ? 26 : 8;
   }
}
