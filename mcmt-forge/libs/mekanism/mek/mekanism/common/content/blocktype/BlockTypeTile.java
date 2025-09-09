package mekanism.common.content.blocktype;

import java.util.Set;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.SoundEventRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class BlockTypeTile<TILE extends TileEntityMekanism> extends BlockType {
   private final Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar;

   public BlockTypeTile(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description) {
      super(description);
      this.tileEntityRegistrar = tileEntityRegistrar;
   }

   public TileEntityTypeRegistryObject<TILE> getTileType() {
      return this.tileEntityRegistrar.get();
   }

   public static class BlockTileBuilder<BLOCK extends BlockTypeTile<TILE>, TILE extends TileEntityMekanism, T extends BlockTypeTile.BlockTileBuilder<BLOCK, TILE, T>>
      extends BlockType.BlockTypeBuilder<BLOCK, T> {
      protected BlockTileBuilder(BLOCK holder) {
         super(holder);
      }

      public static <TILE extends TileEntityMekanism> BlockTypeTile.BlockTileBuilder<BlockTypeTile<TILE>, TILE, ?> createBlock(
         Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, ILangEntry description
      ) {
         return new BlockTypeTile.BlockTileBuilder<>(new BlockTypeTile<>(tileEntityRegistrar, description));
      }

      public T withSound(SoundEventRegistryObject<SoundEvent> soundRegistrar) {
         return this.with(new Attribute[]{new AttributeSound(soundRegistrar)});
      }

      public T withGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar) {
         return this.withGui(containerRegistrar, null);
      }

      public T withGui(Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar, @Nullable ILangEntry customName) {
         return this.with(new Attribute[]{new AttributeGui(containerRegistrar, customName)});
      }

      public T withEnergyConfig(FloatingLongSupplier energyUsage, FloatingLongSupplier energyStorage) {
         return this.with(new Attribute[]{new AttributeEnergy(energyUsage, energyStorage)});
      }

      public T withEnergyConfig(FloatingLongSupplier energyStorage) {
         return this.with(new Attribute[]{new AttributeEnergy(null, energyStorage)});
      }

      @SafeVarargs
      public final T with(Attribute.TileAttribute<TILE>... attrs) {
         this.holder.add(attrs);
         return this.self();
      }

      public T withSupportedUpgrades(Set<Upgrade> upgrades) {
         this.holder.add(new Attribute[]{new AttributeUpgradeSupport(upgrades)});
         return this.self();
      }
   }
}
