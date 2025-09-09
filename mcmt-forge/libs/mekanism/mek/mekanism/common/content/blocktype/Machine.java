package mekanism.common.content.blocktype;

import java.util.EnumSet;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

public class Machine<TILE extends TileEntityMekanism> extends BlockTypeTile<TILE> {
   public Machine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description) {
      super(tileEntityRegistrar, description);
      this.add(
         new Attribute[]{
            new AttributeParticleFX()
               .add(ParticleTypes.f_123762_, rand -> new Pos3D(rand.m_188501_() * 0.6F - 0.3F, rand.m_188501_() * 6.0F / 16.0F, 0.52))
               .add(DustParticleOptions.f_123656_, rand -> new Pos3D(rand.m_188501_() * 0.6F - 0.3F, rand.m_188501_() * 6.0F / 16.0F, 0.52))
         }
      );
      this.add(
         new Attribute[]{
            Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.INVENTORY, Attributes.SECURITY, Attributes.REDSTONE, Attributes.COMPARATOR
         }
      );
      this.add(new Attribute[]{new AttributeUpgradeSupport(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))});
   }

   public static class FactoryMachine<TILE extends TileEntityMekanism> extends Machine<TILE> {
      public FactoryMachine(Supplier<TileEntityTypeRegistryObject<TILE>> tileEntitySupplier, MekanismLang description, FactoryType factoryType) {
         super(tileEntitySupplier, description);
         this.add(
            new Attribute[]{
               new AttributeFactoryType(factoryType), new AttributeUpgradeable(() -> MekanismBlocks.getFactory(FactoryTier.BASIC, this.getFactoryType()))
            }
         );
      }

      public FactoryType getFactoryType() {
         return this.get(AttributeFactoryType.class).getFactoryType();
      }
   }

   public static class MachineBuilder<MACHINE extends Machine<TILE>, TILE extends TileEntityMekanism, T extends Machine.MachineBuilder<MACHINE, TILE, T>>
      extends BlockTypeTile.BlockTileBuilder<MACHINE, TILE, T> {
      protected MachineBuilder(MACHINE holder) {
         super(holder);
      }

      public static <TILE extends TileEntityMekanism> Machine.MachineBuilder<Machine<TILE>, TILE, ?> createMachine(
         Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description
      ) {
         return new Machine.MachineBuilder<>(new Machine<>(tileEntityRegistrar, description));
      }

      public static <TILE extends TileEntityMekanism> Machine.MachineBuilder<Machine.FactoryMachine<TILE>, TILE, ?> createFactoryMachine(
         Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar, MekanismLang description, FactoryType factoryType
      ) {
         return new Machine.MachineBuilder<>(new Machine.FactoryMachine<>(tileEntityRegistrar, description, factoryType));
      }
   }
}
