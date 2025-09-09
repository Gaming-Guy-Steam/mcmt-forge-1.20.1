package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumSet;
import java.util.function.Supplier;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockPersonalStorage;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateBoilerValveMode;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.AttributeStateOpen;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockShapes;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityIndustrialAlarm;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPersonalBarrel;
import mekanism.common.tile.TileEntityPersonalChest;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.laser.TileEntityLaser;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalInjectionChamber;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityCombiner;
import mekanism.common.tile.machine.TileEntityCrusher;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityEnergizedSmelter;
import mekanism.common.tile.machine.TileEntityEnrichmentChamber;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOsmiumCompressor;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityPurificationChamber;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityDynamicValve;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.tile.multiblock.TileEntityStructuralGlass;
import mekanism.common.tile.multiblock.TileEntitySuperchargedCoil;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationValve;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MekanismBlockTypes {
   private static final FloatingLong RESISTIVE_HEATER_BASE_USAGE = FloatingLong.createConst(100L);
   private static final Table<FactoryTier, FactoryType, Factory<?>> FACTORIES = HashBasedTable.create();
   public static final Machine.FactoryMachine<TileEntityEnrichmentChamber> ENRICHMENT_CHAMBER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.ENRICHMENT_CHAMBER, MekanismLang.DESCRIPTION_ENRICHMENT_CHAMBER, FactoryType.ENRICHING
      )
      .withGui(() -> MekanismContainerTypes.ENRICHMENT_CHAMBER)
      .withSound(MekanismSounds.ENRICHMENT_CHAMBER)
      .withEnergyConfig(MekanismConfig.usage.enrichmentChamber, MekanismConfig.storage.enrichmentChamber)
      .withComputerSupport("enrichmentChamber")
      .build();
   public static final Machine.FactoryMachine<TileEntityCrusher> CRUSHER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.CRUSHER, MekanismLang.DESCRIPTION_CRUSHER, FactoryType.CRUSHING
      )
      .withGui(() -> MekanismContainerTypes.CRUSHER)
      .withSound(MekanismSounds.CRUSHER)
      .withEnergyConfig(MekanismConfig.usage.crusher, MekanismConfig.storage.crusher)
      .withComputerSupport("crusher")
      .build();
   public static final Machine.FactoryMachine<TileEntityEnergizedSmelter> ENERGIZED_SMELTER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.ENERGIZED_SMELTER, MekanismLang.DESCRIPTION_ENERGIZED_SMELTER, FactoryType.SMELTING
      )
      .withGui(() -> MekanismContainerTypes.ENERGIZED_SMELTER)
      .withSound(MekanismSounds.ENERGIZED_SMELTER)
      .withEnergyConfig(MekanismConfig.usage.energizedSmelter, MekanismConfig.storage.energizedSmelter)
      .withComputerSupport("energizedSmelter")
      .build();
   public static final Machine.FactoryMachine<TileEntityPrecisionSawmill> PRECISION_SAWMILL = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.PRECISION_SAWMILL, MekanismLang.DESCRIPTION_PRECISION_SAWMILL, FactoryType.SAWING
      )
      .withGui(() -> MekanismContainerTypes.PRECISION_SAWMILL)
      .withSound(MekanismSounds.PRECISION_SAWMILL)
      .withEnergyConfig(MekanismConfig.usage.precisionSawmill, MekanismConfig.storage.precisionSawmill)
      .withComputerSupport("precisionSawmill")
      .build();
   public static final Machine.FactoryMachine<TileEntityOsmiumCompressor> OSMIUM_COMPRESSOR = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.OSMIUM_COMPRESSOR, MekanismLang.DESCRIPTION_OSMIUM_COMPRESSOR, FactoryType.COMPRESSING
      )
      .withGui(() -> MekanismContainerTypes.OSMIUM_COMPRESSOR)
      .withSound(MekanismSounds.OSMIUM_COMPRESSOR)
      .withEnergyConfig(MekanismConfig.usage.osmiumCompressor, MekanismConfig.storage.osmiumCompressor)
      .withComputerSupport("osmiumCompressor")
      .build();
   public static final Machine.FactoryMachine<TileEntityCombiner> COMBINER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.COMBINER, MekanismLang.DESCRIPTION_COMBINER, FactoryType.COMBINING
      )
      .withGui(() -> MekanismContainerTypes.COMBINER)
      .withSound(MekanismSounds.COMBINER)
      .withEnergyConfig(MekanismConfig.usage.combiner, MekanismConfig.storage.combiner)
      .withComputerSupport("combiner")
      .build();
   public static final Machine.FactoryMachine<TileEntityMetallurgicInfuser> METALLURGIC_INFUSER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.METALLURGIC_INFUSER, MekanismLang.DESCRIPTION_METALLURGIC_INFUSER, FactoryType.INFUSING
      )
      .withGui(() -> MekanismContainerTypes.METALLURGIC_INFUSER)
      .withSound(MekanismSounds.METALLURGIC_INFUSER)
      .withEnergyConfig(MekanismConfig.usage.metallurgicInfuser, MekanismConfig.storage.metallurgicInfuser)
      .withCustomShape(BlockShapes.METALLURGIC_INFUSER)
      .withComputerSupport("metallurgicInfuser")
      .build();
   public static final Machine.FactoryMachine<TileEntityPurificationChamber> PURIFICATION_CHAMBER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.PURIFICATION_CHAMBER, MekanismLang.DESCRIPTION_PURIFICATION_CHAMBER, FactoryType.PURIFYING
      )
      .withGui(() -> MekanismContainerTypes.PURIFICATION_CHAMBER)
      .withSound(MekanismSounds.PURIFICATION_CHAMBER)
      .withEnergyConfig(MekanismConfig.usage.purificationChamber, MekanismConfig.storage.purificationChamber)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
      .withComputerSupport("purificationChamber")
      .build();
   public static final Machine.FactoryMachine<TileEntityChemicalInjectionChamber> CHEMICAL_INJECTION_CHAMBER = Machine.MachineBuilder.createFactoryMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_INJECTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_INJECTION_CHAMBER, FactoryType.INJECTING
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER)
      .withSound(MekanismSounds.CHEMICAL_INJECTION_CHAMBER)
      .withEnergyConfig(MekanismConfig.usage.chemicalInjectionChamber, MekanismConfig.storage.chemicalInjectionChamber)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
      .withComputerSupport("chemicalInjectionChamber")
      .build();
   public static final Machine<TileEntityPressurizedReactionChamber> PRESSURIZED_REACTION_CHAMBER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.PRESSURIZED_REACTION_CHAMBER, MekanismLang.DESCRIPTION_PRESSURIZED_REACTION_CHAMBER
      )
      .withGui(() -> MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER)
      .withSound(MekanismSounds.PRESSURIZED_REACTION_CHAMBER)
      .withEnergyConfig(MekanismConfig.usage.pressurizedReactionBase, MekanismConfig.storage.pressurizedReactionBase)
      .withCustomShape(BlockShapes.PRESSURIZED_REACTION_CHAMBER)
      .withComputerSupport("pressurizedReactionChamber")
      .build();
   public static final Machine<TileEntityChemicalCrystallizer> CHEMICAL_CRYSTALLIZER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER, MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_CRYSTALLIZER)
      .withSound(MekanismSounds.CHEMICAL_CRYSTALLIZER)
      .withEnergyConfig(MekanismConfig.usage.chemicalCrystallizer, MekanismConfig.storage.chemicalCrystallizer)
      .withCustomShape(BlockShapes.CHEMICAL_CRYSTALLIZER)
      .withComputerSupport("chemicalCrystallizer")
      .build();
   public static final Machine<TileEntityChemicalDissolutionChamber> CHEMICAL_DISSOLUTION_CHAMBER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_DISSOLUTION_CHAMBER, MekanismLang.DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER)
      .withSound(MekanismSounds.CHEMICAL_DISSOLUTION_CHAMBER)
      .withEnergyConfig(MekanismConfig.usage.chemicalDissolutionChamber, MekanismConfig.storage.chemicalDissolutionChamber)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING, Upgrade.GAS))
      .withCustomShape(BlockShapes.CHEMICAL_DISSOLUTION_CHAMBER)
      .withComputerSupport("chemicalDissolutionChamber")
      .build();
   public static final Machine<TileEntityChemicalInfuser> CHEMICAL_INFUSER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_INFUSER, MekanismLang.DESCRIPTION_CHEMICAL_INFUSER
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_INFUSER)
      .withSound(MekanismSounds.CHEMICAL_INFUSER)
      .withEnergyConfig(MekanismConfig.usage.chemicalInfuser, MekanismConfig.storage.chemicalInfuser)
      .withCustomShape(BlockShapes.CHEMICAL_INFUSER)
      .withComputerSupport("chemicalInfuser")
      .build();
   public static final Machine<TileEntityChemicalOxidizer> CHEMICAL_OXIDIZER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_OXIDIZER, MekanismLang.DESCRIPTION_CHEMICAL_OXIDIZER
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_OXIDIZER)
      .withSound(MekanismSounds.CHEMICAL_OXIDIZER)
      .withEnergyConfig(MekanismConfig.usage.oxidationChamber, MekanismConfig.storage.oxidationChamber)
      .withCustomShape(BlockShapes.CHEMICAL_OXIDIZER)
      .withComputerSupport("chemicalOxidizer")
      .build();
   public static final Machine<TileEntityChemicalWasher> CHEMICAL_WASHER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.CHEMICAL_WASHER, MekanismLang.DESCRIPTION_CHEMICAL_WASHER
      )
      .withGui(() -> MekanismContainerTypes.CHEMICAL_WASHER)
      .withSound(MekanismSounds.CHEMICAL_WASHER)
      .withEnergyConfig(MekanismConfig.usage.chemicalWasher, MekanismConfig.storage.chemicalWasher)
      .withCustomShape(BlockShapes.CHEMICAL_WASHER)
      .withComputerSupport("chemicalWasher")
      .build();
   public static final Machine<TileEntityRotaryCondensentrator> ROTARY_CONDENSENTRATOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.ROTARY_CONDENSENTRATOR, MekanismLang.DESCRIPTION_ROTARY_CONDENSENTRATOR
      )
      .withGui(() -> MekanismContainerTypes.ROTARY_CONDENSENTRATOR)
      .withSound(MekanismSounds.ROTARY_CONDENSENTRATOR)
      .withEnergyConfig(MekanismConfig.usage.rotaryCondensentrator, MekanismConfig.storage.rotaryCondensentrator)
      .withCustomShape(BlockShapes.ROTARY_CONDENSENTRATOR)
      .withComputerSupport("rotaryCondensentrator")
      .build();
   public static final Machine<TileEntityElectrolyticSeparator> ELECTROLYTIC_SEPARATOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR, MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR
      )
      .withGui(() -> MekanismContainerTypes.ELECTROLYTIC_SEPARATOR)
      .withSound(MekanismSounds.ELECTROLYTIC_SEPARATOR)
      .withEnergyConfig(() -> MekanismConfig.general.FROM_H2.get().multiply(2L), MekanismConfig.storage.electrolyticSeparator)
      .withCustomShape(BlockShapes.ELECTROLYTIC_SEPARATOR)
      .withComputerSupport("electrolyticSeparator")
      .build();
   public static final Machine<TileEntityDigitalMiner> DIGITAL_MINER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.DIGITAL_MINER, MekanismLang.DESCRIPTION_DIGITAL_MINER
      )
      .withGui(() -> MekanismContainerTypes.DIGITAL_MINER)
      .withEnergyConfig(MekanismConfig.usage.digitalMiner, MekanismConfig.storage.digitalMiner)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.ANCHOR, Upgrade.STONE_GENERATOR))
      .withCustomShape(BlockShapes.DIGITAL_MINER)
      .with(new Attribute[]{AttributeCustomSelectionBox.JSON})
      .withBounding((pos, state, builder) -> {
         for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
               for (int z = -1; z <= 1; z++) {
                  if (x != 0 || y != 0 || z != 0) {
                     builder.add(pos.m_7918_(x, y, z));
                  }
               }
            }
         }
      })
      .withComputerSupport("digitalMiner")
      .replace(new Attribute[]{Attributes.ACTIVE})
      .build();
   public static final Machine<TileEntityFormulaicAssemblicator> FORMULAIC_ASSEMBLICATOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.FORMULAIC_ASSEMBLICATOR, MekanismLang.DESCRIPTION_FORMULAIC_ASSEMBLICATOR
      )
      .withGui(() -> MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR)
      .withEnergyConfig(MekanismConfig.usage.formulaicAssemblicator, MekanismConfig.storage.formulaicAssemblicator)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
      .withComputerSupport("formulaicAssemblicator")
      .build();
   public static final Machine<TileEntityElectricPump> ELECTRIC_PUMP = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.ELECTRIC_PUMP, MekanismLang.DESCRIPTION_ELECTRIC_PUMP
      )
      .withGui(() -> MekanismContainerTypes.ELECTRIC_PUMP)
      .withEnergyConfig(MekanismConfig.usage.electricPump, MekanismConfig.storage.electricPump)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.FILTER))
      .withCustomShape(BlockShapes.ELECTRIC_PUMP)
      .withComputerSupport("electricPump")
      .replace(new Attribute[]{Attributes.ACTIVE})
      .build();
   public static final Machine<TileEntityFluidicPlenisher> FLUIDIC_PLENISHER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.FLUIDIC_PLENISHER, MekanismLang.DESCRIPTION_FLUIDIC_PLENISHER
      )
      .withGui(() -> MekanismContainerTypes.FLUIDIC_PLENISHER)
      .withEnergyConfig(MekanismConfig.usage.fluidicPlenisher, MekanismConfig.storage.fluidicPlenisher)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY))
      .withCustomShape(BlockShapes.FLUIDIC_PLENISHER)
      .withComputerSupport("fluidicPlenisher")
      .replace(new Attribute[]{Attributes.ACTIVE})
      .build();
   public static final Machine<TileEntitySolarNeutronActivator> SOLAR_NEUTRON_ACTIVATOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.SOLAR_NEUTRON_ACTIVATOR, MekanismLang.DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR
      )
      .withGui(() -> MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR)
      .without(new Class[]{AttributeParticleFX.class, AttributeUpgradeSupport.class})
      .withCustomShape(BlockShapes.SOLAR_NEUTRON_ACTIVATOR)
      .with(new Attribute[]{AttributeCustomSelectionBox.JSON})
      .withBounding((pos, state, builder) -> builder.add(pos.m_7494_()))
      .withComputerSupport("solarNeutronActivator")
      .replace(new Attribute[]{Attributes.ACTIVE})
      .build();
   public static final Machine<TileEntityTeleporter> TELEPORTER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.TELEPORTER, MekanismLang.DESCRIPTION_TELEPORTER
      )
      .withGui(() -> MekanismContainerTypes.TELEPORTER)
      .withEnergyConfig(MekanismConfig.storage.teleporter)
      .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
      .without(new Class[]{AttributeStateActive.class, AttributeStateFacing.class, AttributeParticleFX.class})
      .withLight(3)
      .withComputerSupport("teleporter")
      .build();
   public static final BlockTypeTile<TileEntityChargepad> CHARGEPAD = BlockTypeTile.BlockTileBuilder.<TileEntityChargepad>createBlock(
         () -> MekanismTileEntityTypes.CHARGEPAD, MekanismLang.DESCRIPTION_CHARGEPAD
      )
      .withEnergyConfig(MekanismConfig.usage.chargePad, MekanismConfig.storage.chargePad)
      .withSound(MekanismSounds.CHARGEPAD)
      .with(new Attribute[]{Attributes.ACTIVE_LIGHT, new AttributeStateFacing()})
      .withCustomShape(BlockShapes.CHARGEPAD)
      .withComputerSupport("chargepad")
      .build();
   public static final BlockTypeTile<TileEntityLaser> LASER = BlockTypeTile.BlockTileBuilder.<TileEntityLaser>createBlock(
         () -> MekanismTileEntityTypes.LASER, MekanismLang.DESCRIPTION_LASER
      )
      .withEnergyConfig(MekanismConfig.usage.laser, MekanismConfig.storage.laser)
      .withSound(MekanismSounds.LASER)
      .with(
         new Attribute[]{
            Attributes.ACTIVE,
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.SECURITY
         }
      )
      .withCustomShape(BlockShapes.LASER)
      .withComputerSupport("laser")
      .build();
   public static final BlockTypeTile<TileEntityLaserAmplifier> LASER_AMPLIFIER = BlockTypeTile.BlockTileBuilder.<TileEntityLaserAmplifier>createBlock(
         () -> MekanismTileEntityTypes.LASER_AMPLIFIER, MekanismLang.DESCRIPTION_LASER_AMPLIFIER
      )
      .withGui(() -> MekanismContainerTypes.LASER_AMPLIFIER)
      .withEnergyConfig(MekanismConfig.storage.laserAmplifier)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            new Attributes.AttributeRedstoneEmitter<>((tile, side) -> tile.getRedstoneLevel()),
            Attributes.REDSTONE,
            Attributes.COMPARATOR,
            Attributes.SECURITY
         }
      )
      .withCustomShape(BlockShapes.LASER_AMPLIFIER)
      .withComputerSupport("laserAmplifier")
      .build();
   public static final BlockTypeTile<TileEntityLaserTractorBeam> LASER_TRACTOR_BEAM = BlockTypeTile.BlockTileBuilder.<TileEntityLaserTractorBeam>createBlock(
         () -> MekanismTileEntityTypes.LASER_TRACTOR_BEAM, MekanismLang.DESCRIPTION_LASER_TRACTOR_BEAM
      )
      .withGui(() -> MekanismContainerTypes.LASER_TRACTOR_BEAM)
      .withEnergyConfig(MekanismConfig.storage.laserTractorBeam)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.COMPARATOR,
            Attributes.SECURITY,
            Attributes.INVENTORY
         }
      )
      .withCustomShape(BlockShapes.LASER_AMPLIFIER)
      .withComputerSupport("laserTractorBeam")
      .build();
   public static final Machine<TileEntityResistiveHeater> RESISTIVE_HEATER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.RESISTIVE_HEATER, MekanismLang.DESCRIPTION_RESISTIVE_HEATER
      )
      .withGui(() -> MekanismContainerTypes.RESISTIVE_HEATER)
      .withEnergyConfig(() -> RESISTIVE_HEATER_BASE_USAGE, null)
      .without(new Class[]{Attributes.AttributeComparator.class, AttributeUpgradeSupport.class})
      .withCustomShape(BlockShapes.RESISTIVE_HEATER)
      .withSound(MekanismSounds.RESISTIVE_HEATER)
      .withComputerSupport("resistiveHeater")
      .replace(new Attribute[]{Attributes.ACTIVE_MELT_LIGHT})
      .build();
   public static final Machine<TileEntitySeismicVibrator> SEISMIC_VIBRATOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.SEISMIC_VIBRATOR, MekanismLang.DESCRIPTION_SEISMIC_VIBRATOR
      )
      .withGui(() -> MekanismContainerTypes.SEISMIC_VIBRATOR)
      .withEnergyConfig(MekanismConfig.usage.seismicVibrator, MekanismConfig.storage.seismicVibrator)
      .without(new Class[]{Attributes.AttributeComparator.class, AttributeParticleFX.class, AttributeUpgradeSupport.class})
      .withCustomShape(BlockShapes.SEISMIC_VIBRATOR)
      .with(new Attribute[]{AttributeCustomSelectionBox.JAVA})
      .withBounding((pos, state, builder) -> builder.add(pos.m_7494_()))
      .withComputerSupport("seismicVibrator")
      .build();
   public static final BlockTypeTile<TileEntityPersonalBarrel> PERSONAL_BARREL = BlockTypeTile.BlockTileBuilder.<TileEntityPersonalBarrel>createBlock(
         () -> MekanismTileEntityTypes.PERSONAL_BARREL, MekanismLang.DESCRIPTION_PERSONAL_BARREL
      )
      .withGui(() -> MekanismContainerTypes.PERSONAL_STORAGE_BLOCK)
      .with(
         new Attribute[]{
            Attributes.SECURITY,
            BlockPersonalStorage.PERSONAL_STORAGE_INVENTORY,
            AttributeStateOpen.INSTANCE,
            new AttributeStateFacing(BlockStateProperties.f_61372_),
            new Attributes.AttributeCustomResistance(-1.0F)
         }
      )
      .withComputerSupport("personalBarrel")
      .build();
   public static final BlockTypeTile<TileEntityPersonalChest> PERSONAL_CHEST = BlockTypeTile.BlockTileBuilder.<TileEntityPersonalChest>createBlock(
         () -> MekanismTileEntityTypes.PERSONAL_CHEST, MekanismLang.DESCRIPTION_PERSONAL_CHEST
      )
      .withGui(() -> MekanismContainerTypes.PERSONAL_STORAGE_BLOCK)
      .with(
         new Attribute[]{
            Attributes.SECURITY, BlockPersonalStorage.PERSONAL_STORAGE_INVENTORY, new AttributeStateFacing(), new Attributes.AttributeCustomResistance(-1.0F)
         }
      )
      .withCustomShape(BlockShapes.PERSONAL_CHEST)
      .withComputerSupport("personalChest")
      .build();
   public static final BlockTypeTile<TileEntityFuelwoodHeater> FUELWOOD_HEATER = BlockTypeTile.BlockTileBuilder.<TileEntityFuelwoodHeater>createBlock(
         () -> MekanismTileEntityTypes.FUELWOOD_HEATER, MekanismLang.DESCRIPTION_FUELWOOD_HEATER
      )
      .withGui(() -> MekanismContainerTypes.FUELWOOD_HEATER)
      .with(
         new Attribute[]{
            Attributes.SECURITY,
            Attributes.INVENTORY,
            Attributes.ACTIVE_MELT_LIGHT,
            new AttributeStateFacing(),
            new AttributeParticleFX()
               .add(ParticleTypes.f_123762_, rand -> new Pos3D(rand.m_188501_() * 0.6F - 0.3F, rand.m_188501_() * 6.0F / 16.0F, -0.52))
               .add(DustParticleOptions.f_123656_, rand -> new Pos3D(rand.m_188501_() * 0.6F - 0.3F, rand.m_188501_() * 6.0F / 16.0F, -0.52))
         }
      )
      .withComputerSupport("fuelwoodHeater")
      .build();
   public static final BlockTypeTile<TileEntityOredictionificator> OREDICTIONIFICATOR = BlockTypeTile.BlockTileBuilder.<TileEntityOredictionificator>createBlock(
         () -> MekanismTileEntityTypes.OREDICTIONIFICATOR, MekanismLang.DESCRIPTION_OREDICTIONIFICATOR
      )
      .withGui(() -> MekanismContainerTypes.OREDICTIONIFICATOR)
      .with(new Attribute[]{Attributes.SECURITY, Attributes.INVENTORY, Attributes.ACTIVE, new AttributeStateFacing(), Attributes.REDSTONE})
      .withComputerSupport("oredictionificator")
      .build();
   public static final BlockTypeTile<TileEntityQuantumEntangloporter> QUANTUM_ENTANGLOPORTER = BlockTypeTile.BlockTileBuilder.<TileEntityQuantumEntangloporter>createBlock(
         () -> MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER, MekanismLang.DESCRIPTION_QUANTUM_ENTANGLOPORTER
      )
      .withGui(() -> MekanismContainerTypes.QUANTUM_ENTANGLOPORTER)
      .withSupportedUpgrades(EnumSet.of(Upgrade.ANCHOR))
      .with(new Attribute[]{new AttributeStateFacing(BlockStateProperties.f_61372_), Attributes.INVENTORY, Attributes.SECURITY, Attributes.REDSTONE})
      .withCustomShape(BlockShapes.QUANTUM_ENTANGLOPORTER)
      .withComputerSupport("quantumEntangloporter")
      .build();
   public static final Machine<TileEntityLogisticalSorter> LOGISTICAL_SORTER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.LOGISTICAL_SORTER, MekanismLang.DESCRIPTION_LOGISTICAL_SORTER
      )
      .withGui(() -> MekanismContainerTypes.LOGISTICAL_SORTER)
      .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
      .with(new Attribute[]{new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE)})
      .withCustomShape(BlockShapes.LOGISTICAL_SORTER)
      .withSound(MekanismSounds.LOGISTICAL_SORTER)
      .withComputerSupport("logisticalSorter")
      .replace(new Attribute[]{Attributes.ACTIVE})
      .build();
   public static final BlockTypeTile<TileEntitySecurityDesk> SECURITY_DESK = BlockTypeTile.BlockTileBuilder.<TileEntitySecurityDesk>createBlock(
         () -> MekanismTileEntityTypes.SECURITY_DESK, MekanismLang.DESCRIPTION_SECURITY_DESK
      )
      .withGui(() -> MekanismContainerTypes.SECURITY_DESK)
      .with(new Attribute[]{Attributes.INVENTORY, new AttributeStateFacing(), new Attributes.AttributeCustomResistance(-1.0F), Attributes.SECURITY})
      .withCustomShape(BlockShapes.SECURITY_DESK)
      .with(new Attribute[]{AttributeCustomSelectionBox.JSON})
      .withBounding((pos, state, builder) -> builder.add(pos.m_7494_()))
      .withComputerSupport("securityDesk")
      .build();
   public static final BlockTypeTile<TileEntityModificationStation> MODIFICATION_STATION = BlockTypeTile.BlockTileBuilder.<TileEntityModificationStation>createBlock(
         () -> MekanismTileEntityTypes.MODIFICATION_STATION, MekanismLang.DESCRIPTION_MODIFICATION_STATION
      )
      .withGui(() -> MekanismContainerTypes.MODIFICATION_STATION)
      .withEnergyConfig(MekanismConfig.usage.modificationStation, MekanismConfig.storage.modificationStation)
      .with(new Attribute[]{Attributes.INVENTORY, new AttributeStateFacing(false), Attributes.REDSTONE, Attributes.SECURITY})
      .withCustomShape(BlockShapes.MODIFICATION_STATION)
      .with(new Attribute[]{AttributeCustomSelectionBox.JSON})
      .withBounding((pos, state, builder) -> {
         builder.add(pos.m_7494_());
         BlockPos rightPos = pos.m_121945_(MekanismUtils.getRight(Attribute.getFacing(state)));
         builder.add(rightPos);
         builder.add(rightPos.m_7494_());
      })
      .withComputerSupport("modificationStation")
      .build();
   public static final Machine<TileEntityIsotopicCentrifuge> ISOTOPIC_CENTRIFUGE = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.ISOTOPIC_CENTRIFUGE, MekanismLang.DESCRIPTION_ISOTOPIC_CENTRIFUGE
      )
      .withGui(() -> MekanismContainerTypes.ISOTOPIC_CENTRIFUGE)
      .withEnergyConfig(MekanismConfig.usage.isotopicCentrifuge, MekanismConfig.storage.isotopicCentrifuge)
      .withSound(MekanismSounds.ISOTOPIC_CENTRIFUGE)
      .withCustomShape(BlockShapes.ISOTOPIC_CENTRIFUGE)
      .withBounding((pos, state, builder) -> builder.add(pos.m_7494_()))
      .withComputerSupport("isotopicCentrifuge")
      .build();
   public static final Machine<TileEntityNutritionalLiquifier> NUTRITIONAL_LIQUIFIER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER, MekanismLang.DESCRIPTION_NUTRITIONAL_LIQUIFIER
      )
      .withGui(() -> MekanismContainerTypes.NUTRITIONAL_LIQUIFIER)
      .withEnergyConfig(MekanismConfig.usage.nutritionalLiquifier, MekanismConfig.storage.nutritionalLiquifier)
      .withSound(MekanismSounds.NUTRITIONAL_LIQUIFIER)
      .withComputerSupport("nutritionalLiquifier")
      .build();
   public static final Machine<TileEntityAntiprotonicNucleosynthesizer> ANTIPROTONIC_NUCLEOSYNTHESIZER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, MekanismLang.DESCRIPTION_ANTIPROTONIC_NUCLEOSYNTHESIZER
      )
      .withGui(() -> MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
      .withEnergyConfig(MekanismConfig.usage.antiprotonicNucleosynthesizer, MekanismConfig.storage.antiprotonicNucleosynthesizer)
      .withSound(MekanismSounds.ANTIPROTONIC_NUCLEOSYNTHESIZER)
      .withSupportedUpgrades(EnumSet.of(Upgrade.MUFFLING))
      .withCustomShape(BlockShapes.ANTIPROTONIC_NUCLEOSYNTHESIZER)
      .withComputerSupport("antiprotonicNucleosynthesizer")
      .build();
   public static final Machine<TileEntityPigmentExtractor> PIGMENT_EXTRACTOR = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.PIGMENT_EXTRACTOR, MekanismLang.DESCRIPTION_PIGMENT_EXTRACTOR
      )
      .withGui(() -> MekanismContainerTypes.PIGMENT_EXTRACTOR)
      .withSound(MekanismSounds.PIGMENT_EXTRACTOR)
      .withEnergyConfig(MekanismConfig.usage.pigmentExtractor, MekanismConfig.storage.pigmentExtractor)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))
      .withComputerSupport("pigmentExtractor")
      .build();
   public static final Machine<TileEntityPigmentMixer> PIGMENT_MIXER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.PIGMENT_MIXER, MekanismLang.DESCRIPTION_PIGMENT_MIXER
      )
      .withGui(() -> MekanismContainerTypes.PIGMENT_MIXER)
      .withSound(MekanismSounds.PIGMENT_MIXER)
      .withEnergyConfig(MekanismConfig.usage.pigmentMixer, MekanismConfig.storage.pigmentMixer)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))
      .withCustomShape(BlockShapes.PIGMENT_MIXER)
      .with(new Attribute[]{AttributeCustomSelectionBox.JAVA})
      .withBounding((pos, state, builder) -> builder.add(pos.m_7494_()))
      .withComputerSupport("pigmentMixer")
      .build();
   public static final Machine<TileEntityPaintingMachine> PAINTING_MACHINE = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.PAINTING_MACHINE, MekanismLang.DESCRIPTION_PAINTING_MACHINE
      )
      .withGui(() -> MekanismContainerTypes.PAINTING_MACHINE)
      .withSound(MekanismSounds.PAINTING_MACHINE)
      .withEnergyConfig(MekanismConfig.usage.paintingMachine, MekanismConfig.storage.paintingMachine)
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING))
      .withComputerSupport("paintingMachine")
      .build();
   public static final Machine<TileEntityDimensionalStabilizer> DIMENSIONAL_STABILIZER = Machine.MachineBuilder.createMachine(
         () -> MekanismTileEntityTypes.DIMENSIONAL_STABILIZER, MekanismLang.DESCRIPTION_DIMENSIONAL_STABILIZER
      )
      .withGui(() -> MekanismContainerTypes.DIMENSIONAL_STABILIZER)
      .without(new Class[]{AttributeStateFacing.class, AttributeParticleFX.class})
      .withEnergyConfig(MekanismConfig.usage.dimensionalStabilizer, MekanismConfig.storage.dimensionalStabilizer)
      .withSupportedUpgrades(EnumSet.of(Upgrade.ENERGY))
      .withComputerSupport("dimensionalStabilizer")
      .build();
   public static final BlockTypeTile<TileEntityQIODriveArray> QIO_DRIVE_ARRAY = BlockTypeTile.BlockTileBuilder.<TileEntityQIODriveArray>createBlock(
         () -> MekanismTileEntityTypes.QIO_DRIVE_ARRAY, MekanismLang.DESCRIPTION_QIO_DRIVE_ARRAY
      )
      .withGui(() -> MekanismContainerTypes.QIO_DRIVE_ARRAY)
      .withCustomShape(BlockShapes.QIO_DRIVE_ARRAY)
      .with(new Attribute[]{new AttributeStateFacing(), Attributes.SECURITY, Attributes.INVENTORY, Attributes.ACTIVE})
      .withComputerSupport("qioDriveArray")
      .build();
   public static final BlockTypeTile<TileEntityQIODashboard> QIO_DASHBOARD = BlockTypeTile.BlockTileBuilder.<TileEntityQIODashboard>createBlock(
         () -> MekanismTileEntityTypes.QIO_DASHBOARD, MekanismLang.DESCRIPTION_QIO_DASHBOARD
      )
      .withGui(() -> MekanismContainerTypes.QIO_DASHBOARD)
      .withCustomShape(BlockShapes.QIO_DASHBOARD)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.SECURITY,
            Attributes.ACTIVE,
            Attributes.INVENTORY
         }
      )
      .withComputerSupport("qioDashboard")
      .build();
   public static final BlockTypeTile<TileEntityQIOImporter> QIO_IMPORTER = BlockTypeTile.BlockTileBuilder.<TileEntityQIOImporter>createBlock(
         () -> MekanismTileEntityTypes.QIO_IMPORTER, MekanismLang.DESCRIPTION_QIO_IMPORTER
      )
      .withGui(() -> MekanismContainerTypes.QIO_IMPORTER)
      .withCustomShape(BlockShapes.QIO_IMPORTER)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.SECURITY,
            Attributes.REDSTONE,
            Attributes.ACTIVE
         }
      )
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED))
      .withComputerSupport("qioImporter")
      .build();
   public static final BlockTypeTile<TileEntityQIOExporter> QIO_EXPORTER = BlockTypeTile.BlockTileBuilder.<TileEntityQIOExporter>createBlock(
         () -> MekanismTileEntityTypes.QIO_EXPORTER, MekanismLang.DESCRIPTION_QIO_EXPORTER
      )
      .withGui(() -> MekanismContainerTypes.QIO_EXPORTER)
      .withCustomShape(BlockShapes.QIO_EXPORTER)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.SECURITY,
            Attributes.REDSTONE,
            Attributes.ACTIVE
         }
      )
      .withSupportedUpgrades(EnumSet.of(Upgrade.SPEED))
      .withComputerSupport("qioExporter")
      .build();
   public static final BlockTypeTile<TileEntityQIORedstoneAdapter> QIO_REDSTONE_ADAPTER = BlockTypeTile.BlockTileBuilder.<TileEntityQIORedstoneAdapter>createBlock(
         () -> MekanismTileEntityTypes.QIO_REDSTONE_ADAPTER, MekanismLang.DESCRIPTION_QIO_REDSTONE_ADAPTER
      )
      .withGui(() -> MekanismContainerTypes.QIO_REDSTONE_ADAPTER)
      .withCustomShape(BlockShapes.QIO_REDSTONE_ADAPTER)
      .with(
         new Attribute[]{
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.SECURITY,
            Attributes.ACTIVE
         }
      )
      .with(new Attributes.AttributeRedstoneEmitter<>(TileEntityQIORedstoneAdapter::getRedstoneLevel))
      .withComputerSupport("qioRedstoneAdapter")
      .build();
   public static final BlockTypeTile<TileEntityDynamicTank> DYNAMIC_TANK = BlockTypeTile.BlockTileBuilder.<TileEntityDynamicTank>createBlock(
         () -> MekanismTileEntityTypes.DYNAMIC_TANK, MekanismLang.DESCRIPTION_DYNAMIC_TANK
      )
      .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK, MekanismLang.DYNAMIC_TANK)
      .with(new Attribute[]{Attributes.INVENTORY})
      .externalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntityDynamicValve> DYNAMIC_VALVE = BlockTypeTile.BlockTileBuilder.<TileEntityDynamicValve>createBlock(
         () -> MekanismTileEntityTypes.DYNAMIC_VALVE, MekanismLang.DESCRIPTION_DYNAMIC_VALVE
      )
      .withGui(() -> MekanismContainerTypes.DYNAMIC_TANK, MekanismLang.DYNAMIC_TANK)
      .with(new Attribute[]{Attributes.INVENTORY, Attributes.COMPARATOR})
      .externalMultiblock()
      .withComputerSupport("dynamicValve")
      .build();
   public static final BlockTypeTile<TileEntityBoilerCasing> BOILER_CASING = BlockTypeTile.BlockTileBuilder.<TileEntityBoilerCasing>createBlock(
         () -> MekanismTileEntityTypes.BOILER_CASING, MekanismLang.DESCRIPTION_BOILER_CASING
      )
      .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER, MekanismLang.BOILER)
      .externalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntityBoilerValve> BOILER_VALVE = BlockTypeTile.BlockTileBuilder.<TileEntityBoilerValve>createBlock(
         () -> MekanismTileEntityTypes.BOILER_VALVE, MekanismLang.DESCRIPTION_BOILER_VALVE
      )
      .withGui(() -> MekanismContainerTypes.THERMOELECTRIC_BOILER, MekanismLang.BOILER)
      .with(new Attribute[]{Attributes.INVENTORY, Attributes.COMPARATOR, new AttributeStateBoilerValveMode()})
      .externalMultiblock()
      .withComputerSupport("boilerValve")
      .build();
   public static final BlockTypeTile<TileEntityPressureDisperser> PRESSURE_DISPERSER = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.PRESSURE_DISPERSER, MekanismLang.DESCRIPTION_PRESSURE_DISPERSER
      )
      .internalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntitySuperheatingElement> SUPERHEATING_ELEMENT = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.SUPERHEATING_ELEMENT, MekanismLang.DESCRIPTION_SUPERHEATING_ELEMENT
      )
      .with(new Attribute[]{Attributes.ACTIVE_LIGHT})
      .internalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntityInductionCasing> INDUCTION_CASING = BlockTypeTile.BlockTileBuilder.<TileEntityInductionCasing>createBlock(
         () -> MekanismTileEntityTypes.INDUCTION_CASING, MekanismLang.DESCRIPTION_INDUCTION_CASING
      )
      .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX, MekanismLang.MATRIX)
      .with(new Attribute[]{Attributes.INVENTORY, Attributes.COMPARATOR})
      .externalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntityInductionPort> INDUCTION_PORT = BlockTypeTile.BlockTileBuilder.<TileEntityInductionPort>createBlock(
         () -> MekanismTileEntityTypes.INDUCTION_PORT, MekanismLang.DESCRIPTION_INDUCTION_PORT
      )
      .withGui(() -> MekanismContainerTypes.INDUCTION_MATRIX, MekanismLang.MATRIX)
      .with(new Attribute[]{Attributes.INVENTORY, Attributes.COMPARATOR, Attributes.ACTIVE})
      .externalMultiblock()
      .withComputerSupport("inductionPort")
      .build();
   public static final BlockTypeTile<TileEntityThermalEvaporationController> THERMAL_EVAPORATION_CONTROLLER = BlockTypeTile.BlockTileBuilder.<TileEntityThermalEvaporationController>createBlock(
         () -> MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER
      )
      .withGui(() -> MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, MekanismLang.EVAPORATION_PLANT)
      .with(new Attribute[]{Attributes.INVENTORY, Attributes.ACTIVE, new AttributeStateFacing(), new Attributes.AttributeCustomResistance(9.0F)})
      .externalMultiblock()
      .withComputerSupport("thermalEvaporationController")
      .build();
   public static final BlockTypeTile<TileEntityThermalEvaporationValve> THERMAL_EVAPORATION_VALVE = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.THERMAL_EVAPORATION_VALVE, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_VALVE
      )
      .with(new Attribute[]{Attributes.COMPARATOR, new Attributes.AttributeCustomResistance(9.0F)})
      .externalMultiblock()
      .withComputerSupport("thermalEvaporationValve")
      .build();
   public static final BlockTypeTile<TileEntityThermalEvaporationBlock> THERMAL_EVAPORATION_BLOCK = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.THERMAL_EVAPORATION_BLOCK, MekanismLang.DESCRIPTION_THERMAL_EVAPORATION_BLOCK
      )
      .with(new Attribute[]{new Attributes.AttributeCustomResistance(9.0F)})
      .externalMultiblock()
      .build();
   public static final BlockType TELEPORTER_FRAME = BlockType.BlockTypeBuilder.createBlock(MekanismLang.DESCRIPTION_TELEPORTER_FRAME).withLight(3).build();
   public static final BlockType STEEL_CASING = BlockType.BlockTypeBuilder.createBlock(MekanismLang.DESCRIPTION_STEEL_CASING).build();
   public static final BlockTypeTile<TileEntityRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.RADIOACTIVE_WASTE_BARREL, MekanismLang.DESCRIPTION_RADIOACTIVE_WASTE_BARREL
      )
      .with(new Attribute[]{Attributes.ACTIVE_LIGHT, new AttributeStateFacing(), Attributes.COMPARATOR})
      .withCustomShape(BlockShapes.RADIOACTIVE_WASTE_BARREL)
      .withComputerSupport("radioactiveWasteBarrel")
      .build();
   public static final BlockTypeTile<TileEntityIndustrialAlarm> INDUSTRIAL_ALARM = BlockTypeTile.BlockTileBuilder.<TileEntityIndustrialAlarm>createBlock(
         () -> MekanismTileEntityTypes.INDUSTRIAL_ALARM, MekanismLang.DESCRIPTION_INDUSTRIAL_ALARM
      )
      .withSound(MekanismSounds.INDUSTRIAL_ALARM)
      .withCustomShape(BlockShapes.INDUSTRIAL_ALARM)
      .with(
         new Attribute[]{
            Attributes.ACTIVE_FULL_LIGHT,
            new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE),
            Attributes.REDSTONE
         }
      )
      .withComputerSupport("industrialAlarm")
      .build();
   public static final BlockTypeTile<TileEntityStructuralGlass> STRUCTURAL_GLASS = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.STRUCTURAL_GLASS, MekanismLang.DESCRIPTION_STRUCTURAL_GLASS
      )
      .with(new Attribute[]{AttributeMultiblock.STRUCTURAL, Attributes.AttributeMobSpawn.NEVER})
      .build();
   public static final BlockTypeTile<TileEntitySPSCasing> SPS_CASING = BlockTypeTile.BlockTileBuilder.<TileEntitySPSCasing>createBlock(
         () -> MekanismTileEntityTypes.SPS_CASING, MekanismLang.DESCRIPTION_SPS_CASING
      )
      .withGui(() -> MekanismContainerTypes.SPS, MekanismLang.SPS)
      .withSound(MekanismSounds.SPS)
      .externalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntitySPSPort> SPS_PORT = BlockTypeTile.BlockTileBuilder.<TileEntitySPSPort>createBlock(
         () -> MekanismTileEntityTypes.SPS_PORT, MekanismLang.DESCRIPTION_SPS_PORT
      )
      .withGui(() -> MekanismContainerTypes.SPS, MekanismLang.SPS)
      .withSound(MekanismSounds.SPS)
      .withEnergyConfig(MekanismConfig.storage.spsPort)
      .with(new Attribute[]{Attributes.ACTIVE, Attributes.COMPARATOR})
      .externalMultiblock()
      .withComputerSupport("spsPort")
      .build();
   public static final BlockTypeTile<TileEntitySuperchargedCoil> SUPERCHARGED_COIL = BlockTypeTile.BlockTileBuilder.createBlock(
         () -> MekanismTileEntityTypes.SUPERCHARGED_COIL, MekanismLang.DESCRIPTION_SUPERCHARGED_COIL
      )
      .with(new Attribute[]{new AttributeStateFacing(BlockStateProperties.f_61372_, AttributeStateFacing.FacePlacementType.SELECTED_FACE)})
      .withCustomShape(BlockShapes.SUPERCHARGED_COIL)
      .internalMultiblock()
      .build();
   public static final BlockTypeTile<TileEntityInductionCell> BASIC_INDUCTION_CELL = createInductionCell(
      InductionCellTier.BASIC, () -> MekanismTileEntityTypes.BASIC_INDUCTION_CELL
   );
   public static final BlockTypeTile<TileEntityInductionCell> ADVANCED_INDUCTION_CELL = createInductionCell(
      InductionCellTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_INDUCTION_CELL
   );
   public static final BlockTypeTile<TileEntityInductionCell> ELITE_INDUCTION_CELL = createInductionCell(
      InductionCellTier.ELITE, () -> MekanismTileEntityTypes.ELITE_INDUCTION_CELL
   );
   public static final BlockTypeTile<TileEntityInductionCell> ULTIMATE_INDUCTION_CELL = createInductionCell(
      InductionCellTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_INDUCTION_CELL
   );
   public static final BlockTypeTile<TileEntityInductionProvider> BASIC_INDUCTION_PROVIDER = createInductionProvider(
      InductionProviderTier.BASIC, () -> MekanismTileEntityTypes.BASIC_INDUCTION_PROVIDER
   );
   public static final BlockTypeTile<TileEntityInductionProvider> ADVANCED_INDUCTION_PROVIDER = createInductionProvider(
      InductionProviderTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_INDUCTION_PROVIDER
   );
   public static final BlockTypeTile<TileEntityInductionProvider> ELITE_INDUCTION_PROVIDER = createInductionProvider(
      InductionProviderTier.ELITE, () -> MekanismTileEntityTypes.ELITE_INDUCTION_PROVIDER
   );
   public static final BlockTypeTile<TileEntityInductionProvider> ULTIMATE_INDUCTION_PROVIDER = createInductionProvider(
      InductionProviderTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_INDUCTION_PROVIDER
   );
   public static final Machine<TileEntityBin> BASIC_BIN = createBin(BinTier.BASIC, () -> MekanismTileEntityTypes.BASIC_BIN, () -> MekanismBlocks.ADVANCED_BIN);
   public static final Machine<TileEntityBin> ADVANCED_BIN = createBin(
      BinTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_BIN, () -> MekanismBlocks.ELITE_BIN
   );
   public static final Machine<TileEntityBin> ELITE_BIN = createBin(BinTier.ELITE, () -> MekanismTileEntityTypes.ELITE_BIN, () -> MekanismBlocks.ULTIMATE_BIN);
   public static final Machine<TileEntityBin> ULTIMATE_BIN = createBin(BinTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_BIN, null);
   public static final Machine<TileEntityBin> CREATIVE_BIN = createBin(BinTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_BIN, null);
   public static final Machine<TileEntityEnergyCube> BASIC_ENERGY_CUBE = createEnergyCube(
      EnergyCubeTier.BASIC, () -> MekanismTileEntityTypes.BASIC_ENERGY_CUBE, () -> MekanismBlocks.ADVANCED_ENERGY_CUBE
   );
   public static final Machine<TileEntityEnergyCube> ADVANCED_ENERGY_CUBE = createEnergyCube(
      EnergyCubeTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE, () -> MekanismBlocks.ELITE_ENERGY_CUBE
   );
   public static final Machine<TileEntityEnergyCube> ELITE_ENERGY_CUBE = createEnergyCube(
      EnergyCubeTier.ELITE, () -> MekanismTileEntityTypes.ELITE_ENERGY_CUBE, () -> MekanismBlocks.ULTIMATE_ENERGY_CUBE
   );
   public static final Machine<TileEntityEnergyCube> ULTIMATE_ENERGY_CUBE = createEnergyCube(
      EnergyCubeTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE, null
   );
   public static final Machine<TileEntityEnergyCube> CREATIVE_ENERGY_CUBE = createEnergyCube(
      EnergyCubeTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE, null
   );
   public static final Machine<TileEntityFluidTank> BASIC_FLUID_TANK = createFluidTank(
      FluidTankTier.BASIC, () -> MekanismTileEntityTypes.BASIC_FLUID_TANK, () -> MekanismBlocks.ADVANCED_FLUID_TANK
   );
   public static final Machine<TileEntityFluidTank> ADVANCED_FLUID_TANK = createFluidTank(
      FluidTankTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_FLUID_TANK, () -> MekanismBlocks.ELITE_FLUID_TANK
   );
   public static final Machine<TileEntityFluidTank> ELITE_FLUID_TANK = createFluidTank(
      FluidTankTier.ELITE, () -> MekanismTileEntityTypes.ELITE_FLUID_TANK, () -> MekanismBlocks.ULTIMATE_FLUID_TANK
   );
   public static final Machine<TileEntityFluidTank> ULTIMATE_FLUID_TANK = createFluidTank(
      FluidTankTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_FLUID_TANK, null
   );
   public static final Machine<TileEntityFluidTank> CREATIVE_FLUID_TANK = createFluidTank(
      FluidTankTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_FLUID_TANK, null
   );
   public static final Machine<TileEntityChemicalTank> BASIC_CHEMICAL_TANK = createChemicalTank(
      ChemicalTankTier.BASIC, () -> MekanismTileEntityTypes.BASIC_CHEMICAL_TANK, () -> MekanismBlocks.ADVANCED_CHEMICAL_TANK
   );
   public static final Machine<TileEntityChemicalTank> ADVANCED_CHEMICAL_TANK = createChemicalTank(
      ChemicalTankTier.ADVANCED, () -> MekanismTileEntityTypes.ADVANCED_CHEMICAL_TANK, () -> MekanismBlocks.ELITE_CHEMICAL_TANK
   );
   public static final Machine<TileEntityChemicalTank> ELITE_CHEMICAL_TANK = createChemicalTank(
      ChemicalTankTier.ELITE, () -> MekanismTileEntityTypes.ELITE_CHEMICAL_TANK, () -> MekanismBlocks.ULTIMATE_CHEMICAL_TANK
   );
   public static final Machine<TileEntityChemicalTank> ULTIMATE_CHEMICAL_TANK = createChemicalTank(
      ChemicalTankTier.ULTIMATE, () -> MekanismTileEntityTypes.ULTIMATE_CHEMICAL_TANK, null
   );
   public static final Machine<TileEntityChemicalTank> CREATIVE_CHEMICAL_TANK = createChemicalTank(
      ChemicalTankTier.CREATIVE, () -> MekanismTileEntityTypes.CREATIVE_CHEMICAL_TANK, null
   );

   private MekanismBlockTypes() {
   }

   public static Factory<?> getFactory(FactoryTier tier, FactoryType type) {
      return (Factory<?>)FACTORIES.get(tier, type);
   }

   private static <TILE extends TileEntityInductionCell> BlockTypeTile<TILE> createInductionCell(
      InductionCellTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile
   ) {
      return BlockTypeTile.BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_CELL)
         .withEnergyConfig(tier::getMaxEnergy)
         .with(new Attribute[]{new AttributeTier<>(tier)})
         .internalMultiblock()
         .build();
   }

   private static <TILE extends TileEntityInductionProvider> BlockTypeTile<TILE> createInductionProvider(
      InductionProviderTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile
   ) {
      return BlockTypeTile.BlockTileBuilder.createBlock(tile, MekanismLang.DESCRIPTION_INDUCTION_PROVIDER)
         .with(new Attribute[]{new AttributeTier<>(tier)})
         .internalMultiblock()
         .build();
   }

   private static <TILE extends TileEntityBin> Machine<TILE> createBin(
      BinTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock
   ) {
      return Machine.MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_BIN)
         .with(new Attribute[]{new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock)})
         .without(new Class[]{AttributeParticleFX.class, Attributes.AttributeSecurity.class, AttributeUpgradeSupport.class, Attributes.AttributeRedstone.class})
         .withComputerSupport(tier, "Bin")
         .build();
   }

   private static <TILE extends TileEntityEnergyCube> Machine<TILE> createEnergyCube(
      EnergyCubeTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock
   ) {
      return Machine.MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_ENERGY_CUBE)
         .withGui(() -> MekanismContainerTypes.ENERGY_CUBE)
         .withEnergyConfig(tier::getMaxEnergy)
         .with(new Attribute[]{new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock), new AttributeStateFacing(BlockStateProperties.f_61372_)})
         .without(new Class[]{AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class})
         .withComputerSupport(tier, "EnergyCube")
         .build();
   }

   private static <TILE extends TileEntityFluidTank> Machine<TILE> createFluidTank(
      FluidTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock
   ) {
      return Machine.MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_FLUID_TANK)
         .withGui(() -> MekanismContainerTypes.FLUID_TANK)
         .withCustomShape(BlockShapes.FLUID_TANK)
         .with(new Attribute[]{new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock)})
         .without(new Class[]{AttributeParticleFX.class, AttributeStateFacing.class, Attributes.AttributeRedstone.class, AttributeUpgradeSupport.class})
         .withComputerSupport(tier, "FluidTank")
         .build();
   }

   private static <TILE extends TileEntityChemicalTank> Machine<TILE> createChemicalTank(
      ChemicalTankTier tier, Supplier<TileEntityTypeRegistryObject<TILE>> tile, Supplier<BlockRegistryObject<?, ?>> upgradeBlock
   ) {
      return Machine.MachineBuilder.createMachine(tile, MekanismLang.DESCRIPTION_CHEMICAL_TANK)
         .withGui(() -> MekanismContainerTypes.CHEMICAL_TANK)
         .withCustomShape(BlockShapes.CHEMICAL_TANK)
         .with(new Attribute[]{new AttributeTier<>(tier), new AttributeUpgradeable(upgradeBlock)})
         .without(new Class[]{AttributeParticleFX.class, AttributeStateActive.class, AttributeUpgradeSupport.class})
         .withComputerSupport(tier, "ChemicalTank")
         .build();
   }

   static {
      for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
         for (FactoryType type : EnumUtils.FACTORY_TYPES) {
            FACTORIES.put(
               tier, type, (Factory)Factory.FactoryBuilder.createFactory(() -> MekanismTileEntityTypes.getFactoryTile(tier, type), type, tier).build()
            );
         }
      }
   }
}
