package mekanism.common.content.blocktype;

import java.util.function.Supplier;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeFactoryType;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeSound;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.particles.ParticleTypes;

public class Factory<TILE extends TileEntityFactory<?>> extends Machine.FactoryMachine<TILE> {
   private final Machine.FactoryMachine<?> origMachine;

   public Factory(
      Supplier<TileEntityTypeRegistryObject<TILE>> tileEntityRegistrar,
      Supplier<ContainerTypeRegistryObject<? extends MekanismContainer>> containerRegistrar,
      Machine.FactoryMachine<?> origMachine,
      FactoryTier tier
   ) {
      super(tileEntityRegistrar, MekanismLang.DESCRIPTION_FACTORY, origMachine.getFactoryType());
      this.origMachine = origMachine;
      this.setMachineData(tier);
      this.add(new Attribute[]{new AttributeGui(containerRegistrar, null), new AttributeTier<>(tier)});
      if (tier.ordinal() < EnumUtils.FACTORY_TIERS.length - 1) {
         this.add(
            new Attribute[]{
               new AttributeUpgradeable(() -> MekanismBlocks.getFactory(EnumUtils.FACTORY_TIERS[tier.ordinal() + 1], origMachine.getFactoryType()))
            }
         );
      }
   }

   private void setMachineData(FactoryTier tier) {
      this.setFrom(this.origMachine, new Class[]{AttributeSound.class, AttributeFactoryType.class, AttributeUpgradeSupport.class});
      AttributeEnergy origEnergy = this.origMachine.get(AttributeEnergy.class);
      this.add(
         new Attribute[]{
            new AttributeEnergy(
               origEnergy::getUsage, () -> origEnergy.getConfigStorage().multiply(0.5).max(origEnergy.getUsage()).multiply((long)tier.processes)
            )
         }
      );
   }

   public static class FactoryBuilder<FACTORY extends Factory<TILE>, TILE extends TileEntityFactory<?>, T extends Machine.MachineBuilder<FACTORY, TILE, T>>
      extends BlockTypeTile.BlockTileBuilder<FACTORY, TILE, T> {
      protected FactoryBuilder(FACTORY holder) {
         super(holder);
      }

      public static <TILE extends TileEntityFactory<?>> Factory.FactoryBuilder<Factory<TILE>, TILE, ?> createFactory(
         Supplier<?> tileEntityRegistrar, FactoryType type, FactoryTier tier
      ) {
         Factory.FactoryBuilder<Factory<TILE>, TILE, ?> builder = new Factory.FactoryBuilder<>(
            new Factory<>((Supplier<TileEntityTypeRegistryObject<TILE>>)tileEntityRegistrar, () -> MekanismContainerTypes.FACTORY, type.getBaseMachine(), tier)
         );
         builder.withComputerSupport(tier, type.getRegistryNameComponentCapitalized() + "Factory");
         builder.withCustomShape(BlockShapes.getShape(tier, type));
         builder.replace(
            new Attribute[]{
               new AttributeParticleFX()
                  .addDense(
                     ParticleTypes.f_123762_,
                     5,
                     rand -> new Pos3D(rand.m_188501_() * 0.7F - 0.3F, rand.m_188501_() * 0.1F + 0.7F, rand.m_188501_() * 0.7F - 0.3F)
                  )
            }
         );
         return builder;
      }
   }
}
