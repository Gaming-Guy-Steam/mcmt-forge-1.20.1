package mekanism.common.block.transmitter;

import mekanism.api.tier.BaseTier;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;

public class BlockPressurizedTube extends BlockSmallTransmitter implements ITypeBlock, IHasTileEntity<TileEntityPressurizedTube> {
   private final TubeTier tier;

   public BlockPressurizedTube(TubeTier tier) {
      super(properties -> properties.m_284180_(tier.getBaseTier().getMapColor()));
      this.tier = tier;
   }

   @Override
   protected BaseTier getBaseTier() {
      return this.tier.getBaseTier();
   }

   @Override
   public BlockType getType() {
      return AttributeTier.getPassthroughType(this.tier);
   }

   @Override
   public TileEntityTypeRegistryObject<TileEntityPressurizedTube> getTileType() {
      return switch (this.tier) {
         case ADVANCED -> MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE;
         case ELITE -> MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE;
         case ULTIMATE -> MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE;
         case BASIC -> MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE;
      };
   }
}
