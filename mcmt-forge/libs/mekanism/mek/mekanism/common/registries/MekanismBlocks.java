package mekanism.common.registries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.api.tier.ITier;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockIndustrialAlarm;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPersonalBarrel;
import mekanism.common.block.BlockPersonalChest;
import mekanism.common.block.BlockRadioactiveWasteBarrel;
import mekanism.common.block.attribute.AttributeTier;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.block.basic.BlockChargepad;
import mekanism.common.block.basic.BlockFluidTank;
import mekanism.common.block.basic.BlockLogisticalSorter;
import mekanism.common.block.basic.BlockResource;
import mekanism.common.block.basic.BlockStructuralGlass;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.prefab.BlockBase;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.block.prefab.BlockFactoryMachine;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.block.transmitter.BlockDiversionTransporter;
import mekanism.common.block.transmitter.BlockLogisticalTransporter;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.block.transmitter.BlockPressurizedTube;
import mekanism.common.block.transmitter.BlockRestrictiveTransporter;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.block.transmitter.BlockUniversalCable;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.content.blocktype.Factory;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.item.block.ItemBlockInductionCell;
import mekanism.common.item.block.ItemBlockInductionProvider;
import mekanism.common.item.block.ItemBlockLaserAmplifier;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.ItemBlockRadioactiveWasteBarrel;
import mekanism.common.item.block.ItemBlockResource;
import mekanism.common.item.block.ItemBlockSecurityDesk;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.block.machine.ItemBlockLaserTractorBeam;
import mekanism.common.item.block.machine.ItemBlockMachine;
import mekanism.common.item.block.machine.ItemBlockQIOComponent;
import mekanism.common.item.block.machine.ItemBlockQuantumEntangloporter;
import mekanism.common.item.block.machine.ItemBlockTeleporter;
import mekanism.common.item.block.transmitter.ItemBlockDiversionTransporter;
import mekanism.common.item.block.transmitter.ItemBlockLogisticalTransporter;
import mekanism.common.item.block.transmitter.ItemBlockMechanicalPipe;
import mekanism.common.item.block.transmitter.ItemBlockPressurizedTube;
import mekanism.common.item.block.transmitter.ItemBlockRestrictiveTransporter;
import mekanism.common.item.block.transmitter.ItemBlockThermodynamicConductor;
import mekanism.common.item.block.transmitter.ItemBlockUniversalCable;
import mekanism.common.registration.impl.BlockDeferredRegister;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.factory.TileEntityFactory;
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
import net.minecraft.stats.Stats;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class MekanismBlocks {
   public static final BlockDeferredRegister BLOCKS = new BlockDeferredRegister("mekanism");
   public static final Map<IResource, BlockRegistryObject<?, ?>> PROCESSED_RESOURCE_BLOCKS = new LinkedHashMap<>();
   public static final Map<OreType, OreBlockType> ORES = new LinkedHashMap<>();
   private static final Table<FactoryTier, FactoryType, BlockRegistryObject<BlockFactoryMachine.BlockFactory<?>, ItemBlockFactory>> FACTORIES = HashBasedTable.create();
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> BRONZE_BLOCK;
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> REFINED_OBSIDIAN_BLOCK;
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> CHARCOAL_BLOCK;
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> REFINED_GLOWSTONE_BLOCK;
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> STEEL_BLOCK;
   public static final BlockRegistryObject<BlockResource, ItemBlockResource> FLUORITE_BLOCK;
   public static final BlockRegistryObject<BlockBin, ItemBlockBin> BASIC_BIN;
   public static final BlockRegistryObject<BlockBin, ItemBlockBin> ADVANCED_BIN;
   public static final BlockRegistryObject<BlockBin, ItemBlockBin> ELITE_BIN;
   public static final BlockRegistryObject<BlockBin, ItemBlockBin> ULTIMATE_BIN;
   public static final BlockRegistryObject<BlockBin, ItemBlockBin> CREATIVE_BIN;
   public static final BlockRegistryObject<BlockBase<BlockType>, ItemBlockTooltip<BlockBase<BlockType>>> TELEPORTER_FRAME;
   public static final BlockRegistryObject<BlockBase<BlockType>, ItemBlockTooltip<BlockBase<BlockType>>> STEEL_CASING;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicTank>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityDynamicTank>>> DYNAMIC_TANK;
   public static final BlockRegistryObject<BlockStructuralGlass<TileEntityStructuralGlass>, ItemBlockTooltip<BlockStructuralGlass<TileEntityStructuralGlass>>> STRUCTURAL_GLASS;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityDynamicValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityDynamicValve>>> DYNAMIC_VALVE;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationController>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationController>>> THERMAL_EVAPORATION_CONTROLLER;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationValve>>> THERMAL_EVAPORATION_VALVE;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityThermalEvaporationBlock>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityThermalEvaporationBlock>>> THERMAL_EVAPORATION_BLOCK;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityInductionCasing>>> INDUCTION_CASING;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityInductionPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityInductionPort>>> INDUCTION_PORT;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> BASIC_INDUCTION_CELL;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ADVANCED_INDUCTION_CELL;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ELITE_INDUCTION_CELL;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> ULTIMATE_INDUCTION_CELL;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> BASIC_INDUCTION_PROVIDER;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ADVANCED_INDUCTION_PROVIDER;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ELITE_INDUCTION_PROVIDER;
   public static final BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> ULTIMATE_INDUCTION_PROVIDER;
   public static final BlockRegistryObject<BlockTile<TileEntitySuperheatingElement, BlockTypeTile<TileEntitySuperheatingElement>>, ItemBlockTooltip<BlockTile<TileEntitySuperheatingElement, BlockTypeTile<TileEntitySuperheatingElement>>>> SUPERHEATING_ELEMENT;
   public static final BlockRegistryObject<BlockTile<TileEntityPressureDisperser, BlockTypeTile<TileEntityPressureDisperser>>, ItemBlockTooltip<BlockTile<TileEntityPressureDisperser, BlockTypeTile<TileEntityPressureDisperser>>>> PRESSURE_DISPERSER;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityBoilerCasing>>> BOILER_CASING;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntityBoilerValve>, ItemBlockTooltip<BlockBasicMultiblock<TileEntityBoilerValve>>> BOILER_VALVE;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntitySecurityDesk, BlockTypeTile<TileEntitySecurityDesk>>, ItemBlockSecurityDesk> SECURITY_DESK;
   public static final BlockRegistryObject<BlockRadioactiveWasteBarrel, ItemBlockRadioactiveWasteBarrel> RADIOACTIVE_WASTE_BARREL;
   public static final BlockRegistryObject<BlockIndustrialAlarm, ItemBlockTooltip<BlockIndustrialAlarm>> INDUSTRIAL_ALARM;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnrichmentChamber, Machine.FactoryMachine<TileEntityEnrichmentChamber>>, ItemBlockMachine> ENRICHMENT_CHAMBER;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityOsmiumCompressor, Machine.FactoryMachine<TileEntityOsmiumCompressor>>, ItemBlockMachine> OSMIUM_COMPRESSOR;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCombiner, Machine.FactoryMachine<TileEntityCombiner>>, ItemBlockMachine> COMBINER;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityCrusher, Machine.FactoryMachine<TileEntityCrusher>>, ItemBlockMachine> CRUSHER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityDigitalMiner, Machine<TileEntityDigitalMiner>>, ItemBlockMachine> DIGITAL_MINER;
   public static final BlockRegistryObject<BlockFactoryMachine.BlockFactoryMachineModel<TileEntityMetallurgicInfuser, Machine.FactoryMachine<TileEntityMetallurgicInfuser>>, ItemBlockMachine> METALLURGIC_INFUSER;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPurificationChamber, Machine.FactoryMachine<TileEntityPurificationChamber>>, ItemBlockMachine> PURIFICATION_CHAMBER;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityEnergizedSmelter, Machine.FactoryMachine<TileEntityEnergizedSmelter>>, ItemBlockMachine> ENERGIZED_SMELTER;
   public static final BlockRegistryObject<BlockTile<TileEntityTeleporter, Machine<TileEntityTeleporter>>, ItemBlockMachine> TELEPORTER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityElectricPump, Machine<TileEntityElectricPump>>, ItemBlockMachine> ELECTRIC_PUMP;
   public static final BlockRegistryObject<BlockPersonalBarrel, ItemBlockPersonalStorage<BlockPersonalBarrel>> PERSONAL_BARREL;
   public static final BlockRegistryObject<BlockPersonalChest, ItemBlockPersonalStorage<BlockPersonalChest>> PERSONAL_CHEST;
   public static final BlockRegistryObject<BlockChargepad, ItemBlockTooltip<BlockChargepad>> CHARGEPAD;
   public static final BlockRegistryObject<BlockLogisticalSorter, ItemBlockMachine> LOGISTICAL_SORTER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityRotaryCondensentrator, Machine<TileEntityRotaryCondensentrator>>, ItemBlockMachine> ROTARY_CONDENSENTRATOR;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalOxidizer, Machine<TileEntityChemicalOxidizer>>, ItemBlockMachine> CHEMICAL_OXIDIZER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalInfuser, Machine<TileEntityChemicalInfuser>>, ItemBlockMachine> CHEMICAL_INFUSER;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityChemicalInjectionChamber, Machine.FactoryMachine<TileEntityChemicalInjectionChamber>>, ItemBlockMachine> CHEMICAL_INJECTION_CHAMBER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityElectrolyticSeparator, Machine<TileEntityElectrolyticSeparator>>, ItemBlockMachine> ELECTROLYTIC_SEPARATOR;
   public static final BlockRegistryObject<BlockFactoryMachine<TileEntityPrecisionSawmill, Machine.FactoryMachine<TileEntityPrecisionSawmill>>, ItemBlockMachine> PRECISION_SAWMILL;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalDissolutionChamber, Machine<TileEntityChemicalDissolutionChamber>>, ItemBlockMachine> CHEMICAL_DISSOLUTION_CHAMBER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalWasher, Machine<TileEntityChemicalWasher>>, ItemBlockMachine> CHEMICAL_WASHER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalCrystallizer, Machine<TileEntityChemicalCrystallizer>>, ItemBlockMachine> CHEMICAL_CRYSTALLIZER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntitySeismicVibrator, Machine<TileEntitySeismicVibrator>>, ItemBlockMachine> SEISMIC_VIBRATOR;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityPressurizedReactionChamber, Machine<TileEntityPressurizedReactionChamber>>, ItemBlockMachine> PRESSURIZED_REACTION_CHAMBER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityIsotopicCentrifuge, Machine<TileEntityIsotopicCentrifuge>>, ItemBlockMachine> ISOTOPIC_CENTRIFUGE;
   public static final BlockRegistryObject<BlockTile<TileEntityNutritionalLiquifier, Machine<TileEntityNutritionalLiquifier>>, ItemBlockMachine> NUTRITIONAL_LIQUIFIER;
   public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> BASIC_FLUID_TANK;
   public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ADVANCED_FLUID_TANK;
   public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ELITE_FLUID_TANK;
   public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> ULTIMATE_FLUID_TANK;
   public static final BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> CREATIVE_FLUID_TANK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityFluidicPlenisher, Machine<TileEntityFluidicPlenisher>>, ItemBlockMachine> FLUIDIC_PLENISHER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityLaser, BlockTypeTile<TileEntityLaser>>, ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityLaser, BlockTypeTile<TileEntityLaser>>>> LASER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityLaserAmplifier, BlockTypeTile<TileEntityLaserAmplifier>>, ItemBlockLaserAmplifier> LASER_AMPLIFIER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityLaserTractorBeam, BlockTypeTile<TileEntityLaserTractorBeam>>, ItemBlockLaserTractorBeam> LASER_TRACTOR_BEAM;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQuantumEntangloporter, BlockTypeTile<TileEntityQuantumEntangloporter>>, ItemBlockQuantumEntangloporter> QUANTUM_ENTANGLOPORTER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntitySolarNeutronActivator, Machine<TileEntitySolarNeutronActivator>>, ItemBlockMachine> SOLAR_NEUTRON_ACTIVATOR;
   public static final BlockRegistryObject<BlockTile<TileEntityOredictionificator, BlockTypeTile<TileEntityOredictionificator>>, ItemBlockMachine> OREDICTIONIFICATOR;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityResistiveHeater, Machine<TileEntityResistiveHeater>>, ItemBlockMachine> RESISTIVE_HEATER;
   public static final BlockRegistryObject<BlockTile<TileEntityFormulaicAssemblicator, Machine<TileEntityFormulaicAssemblicator>>, ItemBlockMachine> FORMULAIC_ASSEMBLICATOR;
   public static final BlockRegistryObject<BlockTile<TileEntityFuelwoodHeater, BlockTypeTile<TileEntityFuelwoodHeater>>, ItemBlockMachine> FUELWOOD_HEATER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityModificationStation, BlockTypeTile<TileEntityModificationStation>>, ItemBlockMachine> MODIFICATION_STATION;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityAntiprotonicNucleosynthesizer, Machine<TileEntityAntiprotonicNucleosynthesizer>>, ItemBlockMachine> ANTIPROTONIC_NUCLEOSYNTHESIZER;
   public static final BlockRegistryObject<BlockTile<TileEntityPigmentExtractor, Machine<TileEntityPigmentExtractor>>, ItemBlockMachine> PIGMENT_EXTRACTOR;
   public static final BlockRegistryObject<BlockTile<TileEntityPigmentMixer, Machine<TileEntityPigmentMixer>>, ItemBlockMachine> PIGMENT_MIXER;
   public static final BlockRegistryObject<BlockTile<TileEntityPaintingMachine, Machine<TileEntityPaintingMachine>>, ItemBlockMachine> PAINTING_MACHINE;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSCasing>, ItemBlockTooltip<BlockBasicMultiblock<TileEntitySPSCasing>>> SPS_CASING;
   public static final BlockRegistryObject<BlockBasicMultiblock<TileEntitySPSPort>, ItemBlockTooltip<BlockBasicMultiblock<TileEntitySPSPort>>> SPS_PORT;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntitySuperchargedCoil, BlockTypeTile<TileEntitySuperchargedCoil>>, ItemBlockTooltip<BlockTile.BlockTileModel<TileEntitySuperchargedCoil, BlockTypeTile<TileEntitySuperchargedCoil>>>> SUPERCHARGED_COIL;
   public static final BlockRegistryObject<BlockTile<TileEntityDimensionalStabilizer, Machine<TileEntityDimensionalStabilizer>>, ItemBlockMachine> DIMENSIONAL_STABILIZER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQIODriveArray, BlockTypeTile<TileEntityQIODriveArray>>, ItemBlockQIOComponent.ItemBlockQIOInventoryComponent> QIO_DRIVE_ARRAY;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQIODashboard, BlockTypeTile<TileEntityQIODashboard>>, ItemBlockQIOComponent.ItemBlockQIOInventoryComponent> QIO_DASHBOARD;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQIOImporter, BlockTypeTile<TileEntityQIOImporter>>, ItemBlockQIOComponent> QIO_IMPORTER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQIOExporter, BlockTypeTile<TileEntityQIOExporter>>, ItemBlockQIOComponent> QIO_EXPORTER;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityQIORedstoneAdapter, BlockTypeTile<TileEntityQIORedstoneAdapter>>, ItemBlockQIOComponent> QIO_REDSTONE_ADAPTER;
   public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> BASIC_ENERGY_CUBE;
   public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ADVANCED_ENERGY_CUBE;
   public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ELITE_ENERGY_CUBE;
   public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> ULTIMATE_ENERGY_CUBE;
   public static final BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> CREATIVE_ENERGY_CUBE;
   public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> BASIC_UNIVERSAL_CABLE;
   public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ADVANCED_UNIVERSAL_CABLE;
   public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ELITE_UNIVERSAL_CABLE;
   public static final BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> ULTIMATE_UNIVERSAL_CABLE;
   public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> BASIC_MECHANICAL_PIPE;
   public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ADVANCED_MECHANICAL_PIPE;
   public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ELITE_MECHANICAL_PIPE;
   public static final BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> ULTIMATE_MECHANICAL_PIPE;
   public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> BASIC_PRESSURIZED_TUBE;
   public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ADVANCED_PRESSURIZED_TUBE;
   public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ELITE_PRESSURIZED_TUBE;
   public static final BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> ULTIMATE_PRESSURIZED_TUBE;
   public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> BASIC_LOGISTICAL_TRANSPORTER;
   public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ADVANCED_LOGISTICAL_TRANSPORTER;
   public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ELITE_LOGISTICAL_TRANSPORTER;
   public static final BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> ULTIMATE_LOGISTICAL_TRANSPORTER;
   public static final BlockRegistryObject<BlockRestrictiveTransporter, ItemBlockRestrictiveTransporter> RESTRICTIVE_TRANSPORTER;
   public static final BlockRegistryObject<BlockDiversionTransporter, ItemBlockDiversionTransporter> DIVERSION_TRANSPORTER;
   public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> BASIC_THERMODYNAMIC_CONDUCTOR;
   public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ADVANCED_THERMODYNAMIC_CONDUCTOR;
   public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ELITE_THERMODYNAMIC_CONDUCTOR;
   public static final BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> ULTIMATE_THERMODYNAMIC_CONDUCTOR;
   public static final BlockRegistryObject<BlockBounding, BlockItem> BOUNDING_BLOCK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> BASIC_CHEMICAL_TANK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ADVANCED_CHEMICAL_TANK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ELITE_CHEMICAL_TANK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> ULTIMATE_CHEMICAL_TANK;
   public static final BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> CREATIVE_CHEMICAL_TANK;
   public static final BlockRegistryObject<BlockCardboardBox, ItemBlockCardboardBox> CARDBOARD_BOX;
   public static final BlockRegistryObject<Block, BlockItem> SALT_BLOCK;

   private MekanismBlocks() {
   }

   private static BlockRegistryObject<BlockResource, ItemBlockResource> registerResourceBlock(BlockResourceInfo resource) {
      return BLOCKS.registerDefaultProperties("block_" + resource.getRegistrySuffix(), () -> new BlockResource(resource), (block, properties) -> {
         if (!block.getResourceInfo().burnsInFire()) {
            properties = properties.m_41486_();
         }

         return new ItemBlockResource(block, properties);
      });
   }

   private static BlockRegistryObject<BlockBin, ItemBlockBin> registerBin(BlockTypeTile<TileEntityBin> type) {
      return registerTieredBlock(type, "_bin", color -> new BlockBin(type, properties -> properties.m_284180_(color)), ItemBlockBin::new);
   }

   private static BlockRegistryObject<BlockTile<TileEntityInductionCell, BlockTypeTile<TileEntityInductionCell>>, ItemBlockInductionCell> registerInductionCell(
      BlockTypeTile<TileEntityInductionCell> type
   ) {
      return registerTieredBlock(
         type,
         "_induction_cell",
         color -> new BlockTile<>(type, (UnaryOperator<Properties>)(properties -> properties.m_284180_(color))),
         ItemBlockInductionCell::new
      );
   }

   private static BlockRegistryObject<BlockTile<TileEntityInductionProvider, BlockTypeTile<TileEntityInductionProvider>>, ItemBlockInductionProvider> registerInductionProvider(
      BlockTypeTile<TileEntityInductionProvider> type
   ) {
      return registerTieredBlock(
         type,
         "_induction_provider",
         color -> new BlockTile<>(type, (UnaryOperator<Properties>)(properties -> properties.m_284180_(color))),
         ItemBlockInductionProvider::new
      );
   }

   private static BlockRegistryObject<BlockFluidTank, ItemBlockFluidTank> registerFluidTank(Machine<TileEntityFluidTank> type) {
      return registerTieredBlock(type, "_fluid_tank", () -> new BlockFluidTank(type), ItemBlockFluidTank::new);
   }

   private static BlockRegistryObject<BlockEnergyCube, ItemBlockEnergyCube> registerEnergyCube(Machine<TileEntityEnergyCube> type) {
      return registerTieredBlock(type, "_energy_cube", () -> new BlockEnergyCube(type), ItemBlockEnergyCube::new);
   }

   private static BlockRegistryObject<BlockUniversalCable, ItemBlockUniversalCable> registerUniversalCable(CableTier tier) {
      return registerTieredBlock(tier, "_universal_cable", () -> new BlockUniversalCable(tier), ItemBlockUniversalCable::new);
   }

   private static BlockRegistryObject<BlockMechanicalPipe, ItemBlockMechanicalPipe> registerMechanicalPipe(PipeTier tier) {
      return registerTieredBlock(tier, "_mechanical_pipe", () -> new BlockMechanicalPipe(tier), ItemBlockMechanicalPipe::new);
   }

   private static BlockRegistryObject<BlockPressurizedTube, ItemBlockPressurizedTube> registerPressurizedTube(TubeTier tier) {
      return registerTieredBlock(tier, "_pressurized_tube", () -> new BlockPressurizedTube(tier), ItemBlockPressurizedTube::new);
   }

   private static BlockRegistryObject<BlockLogisticalTransporter, ItemBlockLogisticalTransporter> registerLogisticalTransporter(TransporterTier tier) {
      return registerTieredBlock(tier, "_logistical_transporter", () -> new BlockLogisticalTransporter(tier), ItemBlockLogisticalTransporter::new);
   }

   private static BlockRegistryObject<BlockThermodynamicConductor, ItemBlockThermodynamicConductor> registerThermodynamicConductor(ConductorTier tier) {
      return registerTieredBlock(tier, "_thermodynamic_conductor", () -> new BlockThermodynamicConductor(tier), ItemBlockThermodynamicConductor::new);
   }

   private static BlockRegistryObject<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>, ItemBlockChemicalTank> registerChemicalTank(
      Machine<TileEntityChemicalTank> type
   ) {
      return registerTieredBlock(
         type,
         "_chemical_tank",
         color -> new BlockTile.BlockTileModel<>(type, (UnaryOperator<Properties>)(properties -> properties.m_284180_(color))),
         ItemBlockChemicalTank::new
      );
   }

   private static <TILE extends TileEntityFactory<?>> BlockRegistryObject<BlockFactoryMachine.BlockFactory<?>, ItemBlockFactory> registerFactory(
      Factory<TILE> type
   ) {
      return registerTieredBlock(
         type, "_" + type.getFactoryType().getRegistryNameComponent() + "_factory", () -> new BlockFactoryMachine.BlockFactory<>(type), ItemBlockFactory::new
      );
   }

   private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(
      BlockType type, String suffix, Function<MapColor, ? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator
   ) {
      ITier tier = type.get(AttributeTier.class).tier();
      return registerTieredBlock(tier, suffix, () -> blockSupplier.apply(tier.getBaseTier().getMapColor()), itemCreator);
   }

   private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(
      BlockType type, String suffix, Supplier<? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator
   ) {
      return registerTieredBlock(type.get(AttributeTier.class).tier(), suffix, blockSupplier, itemCreator);
   }

   private static <BLOCK extends Block, ITEM extends BlockItem> BlockRegistryObject<BLOCK, ITEM> registerTieredBlock(
      ITier tier, String suffix, Supplier<? extends BLOCK> blockSupplier, Function<BLOCK, ITEM> itemCreator
   ) {
      return BLOCKS.register(tier.getBaseTier().getLowerName() + suffix, blockSupplier, itemCreator);
   }

   private static OreBlockType registerOre(OreType ore) {
      String name = ore.getResource().getRegistrySuffix() + "_ore";
      BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> stoneOre = registerBlock(name, () -> new BlockOre(ore));
      BlockRegistryObject<BlockOre, ItemBlockTooltip<BlockOre>> deepslateOre = BLOCKS.registerDefaultProperties(
         "deepslate_" + name,
         () -> new BlockOre(ore, Properties.m_60926_(stoneOre.getBlock()).m_284180_(MapColor.f_283875_).m_60913_(4.5F, 3.0F).m_60918_(SoundType.f_154677_)),
         ItemBlockTooltip::new
      );
      return new OreBlockType(stoneOre, deepslateOre);
   }

   private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(
      String name, Supplier<? extends BLOCK> blockSupplier
   ) {
      return BLOCKS.registerDefaultProperties(name, blockSupplier, (x$0, x$1) -> new ItemBlockTooltip<>(x$0, x$1));
   }

   private static <BLOCK extends Block & IHasDescription> BlockRegistryObject<BLOCK, ItemBlockTooltip<BLOCK>> registerBlock(
      String name, Supplier<? extends BLOCK> blockSupplier, Rarity rarity
   ) {
      return BLOCKS.registerDefaultProperties(name, blockSupplier, (block, props) -> new ItemBlockTooltip<>(block, props.m_41497_(rarity)));
   }

   public static BlockRegistryObject<BlockFactoryMachine.BlockFactory<?>, ItemBlockFactory> getFactory(@NotNull FactoryTier tier, @NotNull FactoryType type) {
      return (BlockRegistryObject<BlockFactoryMachine.BlockFactory<?>, ItemBlockFactory>)FACTORIES.get(tier, type);
   }

   public static BlockRegistryObject<BlockFactoryMachine.BlockFactory<?>, ItemBlockFactory>[] getFactoryBlocks() {
      return FACTORIES.values().toArray(new BlockRegistryObject[0]);
   }

   static {
      for (FactoryTier tier : EnumUtils.FACTORY_TIERS) {
         for (FactoryType type : EnumUtils.FACTORY_TYPES) {
            FACTORIES.put(tier, type, registerFactory(MekanismBlockTypes.getFactory(tier, type)));
         }
      }

      for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
         if (resource.getResourceBlockInfo() != null) {
            PROCESSED_RESOURCE_BLOCKS.put(resource, registerResourceBlock(resource.getResourceBlockInfo()));
         }

         BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
         if (rawResource != null) {
            PROCESSED_RESOURCE_BLOCKS.put(rawResource, registerResourceBlock(rawResource));
         }
      }

      for (OreType ore : EnumUtils.ORE_TYPES) {
         ORES.put(ore, registerOre(ore));
      }

      BRONZE_BLOCK = registerResourceBlock(BlockResourceInfo.BRONZE);
      REFINED_OBSIDIAN_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_OBSIDIAN);
      CHARCOAL_BLOCK = registerResourceBlock(BlockResourceInfo.CHARCOAL);
      REFINED_GLOWSTONE_BLOCK = registerResourceBlock(BlockResourceInfo.REFINED_GLOWSTONE);
      STEEL_BLOCK = registerResourceBlock(BlockResourceInfo.STEEL);
      FLUORITE_BLOCK = registerResourceBlock(BlockResourceInfo.FLUORITE);
      BASIC_BIN = registerBin(MekanismBlockTypes.BASIC_BIN);
      ADVANCED_BIN = registerBin(MekanismBlockTypes.ADVANCED_BIN);
      ELITE_BIN = registerBin(MekanismBlockTypes.ELITE_BIN);
      ULTIMATE_BIN = registerBin(MekanismBlockTypes.ULTIMATE_BIN);
      CREATIVE_BIN = registerBin(MekanismBlockTypes.CREATIVE_BIN);
      TELEPORTER_FRAME = registerBlock(
         "teleporter_frame",
         () -> new BlockBase<>(
            MekanismBlockTypes.TELEPORTER_FRAME,
            (UnaryOperator<Properties>)(properties -> properties.m_60913_(5.0F, 6.0F).m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         )
      );
      STEEL_CASING = registerBlock(
         "steel_casing",
         () -> new BlockBase<>(
            MekanismBlockTypes.STEEL_CASING,
            (UnaryOperator<Properties>)(properties -> properties.m_60913_(3.5F, 9.0F).m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         )
      );
      DYNAMIC_TANK = registerBlock(
         "dynamic_tank",
         () -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_TANK, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_)))
      );
      STRUCTURAL_GLASS = registerBlock("structural_glass", () -> new BlockStructuralGlass<>(MekanismBlockTypes.STRUCTURAL_GLASS));
      DYNAMIC_VALVE = registerBlock(
         "dynamic_valve",
         () -> new BlockBasicMultiblock<>(MekanismBlockTypes.DYNAMIC_VALVE, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_)))
      );
      THERMAL_EVAPORATION_CONTROLLER = registerBlock(
         "thermal_evaporation_controller",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER,
            (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.BRONZE.getMapColor()))
         )
      );
      THERMAL_EVAPORATION_VALVE = registerBlock(
         "thermal_evaporation_valve",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.THERMAL_EVAPORATION_VALVE,
            (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.BRONZE.getMapColor()))
         )
      );
      THERMAL_EVAPORATION_BLOCK = registerBlock(
         "thermal_evaporation_block",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK,
            (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.BRONZE.getMapColor()))
         )
      );
      INDUCTION_CASING = registerBlock(
         "induction_casing",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.INDUCTION_CASING, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283779_))
         )
      );
      INDUCTION_PORT = registerBlock(
         "induction_port",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.INDUCTION_PORT, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283779_))
         )
      );
      BASIC_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.BASIC_INDUCTION_CELL);
      ADVANCED_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ADVANCED_INDUCTION_CELL);
      ELITE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ELITE_INDUCTION_CELL);
      ULTIMATE_INDUCTION_CELL = registerInductionCell(MekanismBlockTypes.ULTIMATE_INDUCTION_CELL);
      BASIC_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.BASIC_INDUCTION_PROVIDER);
      ADVANCED_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ADVANCED_INDUCTION_PROVIDER);
      ELITE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ELITE_INDUCTION_PROVIDER);
      ULTIMATE_INDUCTION_PROVIDER = registerInductionProvider(MekanismBlockTypes.ULTIMATE_INDUCTION_PROVIDER);
      SUPERHEATING_ELEMENT = registerBlock(
         "superheating_element",
         () -> new BlockTile<>(MekanismBlockTypes.SUPERHEATING_ELEMENT, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_)))
      );
      PRESSURE_DISPERSER = registerBlock(
         "pressure_disperser",
         () -> new BlockTile<>(MekanismBlockTypes.PRESSURE_DISPERSER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283875_)))
      );
      BOILER_CASING = registerBlock(
         "boiler_casing",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.BOILER_CASING, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         )
      );
      BOILER_VALVE = registerBlock(
         "boiler_valve",
         () -> new BlockBasicMultiblock<>(
            MekanismBlockTypes.BOILER_VALVE, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         )
      );
      SECURITY_DESK = BLOCKS.register(
         "security_desk",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.SECURITY_DESK, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockSecurityDesk::new
      );
      RADIOACTIVE_WASTE_BARREL = BLOCKS.registerDefaultProperties(
         "radioactive_waste_barrel", BlockRadioactiveWasteBarrel::new, ItemBlockRadioactiveWasteBarrel::new
      );
      INDUSTRIAL_ALARM = BLOCKS.register("industrial_alarm", BlockIndustrialAlarm::new, ItemBlockTooltip::new);
      ENRICHMENT_CHAMBER = BLOCKS.register(
         "enrichment_chamber",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.ENRICHMENT_CHAMBER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      OSMIUM_COMPRESSOR = BLOCKS.register(
         "osmium_compressor",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.OSMIUM_COMPRESSOR, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      COMBINER = BLOCKS.register(
         "combiner",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.COMBINER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      CRUSHER = BLOCKS.register(
         "crusher",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.CRUSHER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      DIGITAL_MINER = BLOCKS.register(
         "digital_miner",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.DIGITAL_MINER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      METALLURGIC_INFUSER = BLOCKS.register(
         "metallurgic_infuser",
         () -> new BlockFactoryMachine.BlockFactoryMachineModel<>(
            MekanismBlockTypes.METALLURGIC_INFUSER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())
         ),
         ItemBlockMachine::new
      );
      PURIFICATION_CHAMBER = BLOCKS.register(
         "purification_chamber",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.PURIFICATION_CHAMBER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      ENERGIZED_SMELTER = BLOCKS.register(
         "energized_smelter",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.ENERGIZED_SMELTER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      TELEPORTER = BLOCKS.register(
         "teleporter",
         () -> new BlockTile<>(
            MekanismBlockTypes.TELEPORTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockTeleporter::new
      );
      ELECTRIC_PUMP = BLOCKS.register(
         "electric_pump",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.ELECTRIC_PUMP, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      PERSONAL_BARREL = BLOCKS.register("personal_barrel", BlockPersonalBarrel::new, block -> new ItemBlockPersonalStorage<>(block, Stats.f_12971_));
      PERSONAL_CHEST = BLOCKS.register("personal_chest", BlockPersonalChest::new, block -> new ItemBlockPersonalStorage<>(block, Stats.f_12968_));
      CHARGEPAD = BLOCKS.register("chargepad", BlockChargepad::new, ItemBlockTooltip::new);
      LOGISTICAL_SORTER = BLOCKS.register("logistical_sorter", BlockLogisticalSorter::new, ItemBlockMachine::new);
      ROTARY_CONDENSENTRATOR = BLOCKS.register(
         "rotary_condensentrator",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.ROTARY_CONDENSENTRATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      CHEMICAL_OXIDIZER = BLOCKS.register(
         "chemical_oxidizer",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.CHEMICAL_OXIDIZER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      CHEMICAL_INFUSER = BLOCKS.register(
         "chemical_infuser",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.CHEMICAL_INFUSER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      CHEMICAL_INJECTION_CHAMBER = BLOCKS.register(
         "chemical_injection_chamber",
         () -> new BlockFactoryMachine<>(
            MekanismBlockTypes.CHEMICAL_INJECTION_CHAMBER, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())
         ),
         ItemBlockMachine::new
      );
      ELECTROLYTIC_SEPARATOR = BLOCKS.register(
         "electrolytic_separator",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.ELECTROLYTIC_SEPARATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      PRECISION_SAWMILL = BLOCKS.register(
         "precision_sawmill",
         () -> new BlockFactoryMachine<>(MekanismBlockTypes.PRECISION_SAWMILL, properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor())),
         ItemBlockMachine::new
      );
      CHEMICAL_DISSOLUTION_CHAMBER = BLOCKS.register(
         "chemical_dissolution_chamber",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.CHEMICAL_DISSOLUTION_CHAMBER,
            (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      CHEMICAL_WASHER = BLOCKS.register(
         "chemical_washer",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.CHEMICAL_WASHER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      CHEMICAL_CRYSTALLIZER = BLOCKS.register(
         "chemical_crystallizer",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.CHEMICAL_CRYSTALLIZER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      SEISMIC_VIBRATOR = BLOCKS.register(
         "seismic_vibrator",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.SEISMIC_VIBRATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      PRESSURIZED_REACTION_CHAMBER = BLOCKS.register(
         "pressurized_reaction_chamber",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.PRESSURIZED_REACTION_CHAMBER,
            (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      ISOTOPIC_CENTRIFUGE = BLOCKS.register(
         "isotopic_centrifuge",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.ISOTOPIC_CENTRIFUGE, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      NUTRITIONAL_LIQUIFIER = BLOCKS.register(
         "nutritional_liquifier",
         () -> new BlockTile<>(
            MekanismBlockTypes.NUTRITIONAL_LIQUIFIER,
            (UnaryOperator<Properties>)(properties -> properties.m_60955_().m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      BASIC_FLUID_TANK = registerFluidTank(MekanismBlockTypes.BASIC_FLUID_TANK);
      ADVANCED_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ADVANCED_FLUID_TANK);
      ELITE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ELITE_FLUID_TANK);
      ULTIMATE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.ULTIMATE_FLUID_TANK);
      CREATIVE_FLUID_TANK = registerFluidTank(MekanismBlockTypes.CREATIVE_FLUID_TANK);
      FLUIDIC_PLENISHER = BLOCKS.register(
         "fluidic_plenisher",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.FLUIDIC_PLENISHER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      LASER = BLOCKS.register(
         "laser",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.LASER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockTooltip::new
      );
      LASER_AMPLIFIER = BLOCKS.register(
         "laser_amplifier",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.LASER_AMPLIFIER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockLaserAmplifier::new
      );
      LASER_TRACTOR_BEAM = BLOCKS.register(
         "laser_tractor_beam",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.LASER_TRACTOR_BEAM, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockLaserTractorBeam::new
      );
      QUANTUM_ENTANGLOPORTER = BLOCKS.register(
         "quantum_entangloporter",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QUANTUM_ENTANGLOPORTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockQuantumEntangloporter::new
      );
      SOLAR_NEUTRON_ACTIVATOR = BLOCKS.register(
         "solar_neutron_activator",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.SOLAR_NEUTRON_ACTIVATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283743_))
         ),
         ItemBlockMachine::new
      );
      OREDICTIONIFICATOR = BLOCKS.register(
         "oredictionificator",
         () -> new BlockTile<>(
            MekanismBlockTypes.OREDICTIONIFICATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      RESISTIVE_HEATER = BLOCKS.register(
         "resistive_heater",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.RESISTIVE_HEATER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283906_))
         ),
         ItemBlockMachine::new
      );
      FORMULAIC_ASSEMBLICATOR = BLOCKS.register(
         "formulaic_assemblicator",
         () -> new BlockTile<>(
            MekanismBlockTypes.FORMULAIC_ASSEMBLICATOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      FUELWOOD_HEATER = BLOCKS.register(
         "fuelwood_heater",
         () -> new BlockTile<>(
            MekanismBlockTypes.FUELWOOD_HEATER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      MODIFICATION_STATION = BLOCKS.register(
         "modification_station",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.MODIFICATION_STATION, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      ANTIPROTONIC_NUCLEOSYNTHESIZER = BLOCKS.register(
         "antiprotonic_nucleosynthesizer",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283906_))
         ),
         ItemBlockMachine::new
      );
      PIGMENT_EXTRACTOR = BLOCKS.register(
         "pigment_extractor",
         () -> new BlockTile<>(
            MekanismBlockTypes.PIGMENT_EXTRACTOR, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      PIGMENT_MIXER = BLOCKS.register(
         "pigment_mixer",
         () -> new BlockTile<>(
            MekanismBlockTypes.PIGMENT_MIXER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      PAINTING_MACHINE = BLOCKS.register(
         "painting_machine",
         () -> new BlockTile<>(
            MekanismBlockTypes.PAINTING_MACHINE, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      SPS_CASING = registerBlock(
         "sps_casing",
         () -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_CASING, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283779_))),
         Rarity.EPIC
      );
      SPS_PORT = registerBlock(
         "sps_port",
         () -> new BlockBasicMultiblock<>(MekanismBlockTypes.SPS_PORT, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283779_))),
         Rarity.EPIC
      );
      SUPERCHARGED_COIL = registerBlock(
         "supercharged_coil",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.SUPERCHARGED_COIL, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283750_))
         ),
         Rarity.EPIC
      );
      DIMENSIONAL_STABILIZER = BLOCKS.register(
         "dimensional_stabilizer",
         () -> new BlockTile<>(
            MekanismBlockTypes.DIMENSIONAL_STABILIZER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(BlockResourceInfo.STEEL.getMapColor()))
         ),
         ItemBlockMachine::new
      );
      QIO_DRIVE_ARRAY = BLOCKS.register(
         "qio_drive_array",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QIO_DRIVE_ARRAY, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283906_))
         ),
         ItemBlockQIOComponent.ItemBlockQIOInventoryComponent::new
      );
      QIO_DASHBOARD = BLOCKS.register(
         "qio_dashboard",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QIO_DASHBOARD, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockQIOComponent.ItemBlockQIOInventoryComponent::new
      );
      QIO_IMPORTER = BLOCKS.register(
         "qio_importer",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QIO_IMPORTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockQIOComponent::new
      );
      QIO_EXPORTER = BLOCKS.register(
         "qio_exporter",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QIO_EXPORTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockQIOComponent::new
      );
      QIO_REDSTONE_ADAPTER = BLOCKS.register(
         "qio_redstone_adapter",
         () -> new BlockTile.BlockTileModel<>(
            MekanismBlockTypes.QIO_REDSTONE_ADAPTER, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283818_))
         ),
         ItemBlockQIOComponent::new
      );
      BASIC_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.BASIC_ENERGY_CUBE);
      ADVANCED_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ADVANCED_ENERGY_CUBE);
      ELITE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ELITE_ENERGY_CUBE);
      ULTIMATE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.ULTIMATE_ENERGY_CUBE);
      CREATIVE_ENERGY_CUBE = registerEnergyCube(MekanismBlockTypes.CREATIVE_ENERGY_CUBE);
      BASIC_UNIVERSAL_CABLE = registerUniversalCable(CableTier.BASIC);
      ADVANCED_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ADVANCED);
      ELITE_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ELITE);
      ULTIMATE_UNIVERSAL_CABLE = registerUniversalCable(CableTier.ULTIMATE);
      BASIC_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.BASIC);
      ADVANCED_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ADVANCED);
      ELITE_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ELITE);
      ULTIMATE_MECHANICAL_PIPE = registerMechanicalPipe(PipeTier.ULTIMATE);
      BASIC_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.BASIC);
      ADVANCED_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ADVANCED);
      ELITE_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ELITE);
      ULTIMATE_PRESSURIZED_TUBE = registerPressurizedTube(TubeTier.ULTIMATE);
      BASIC_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.BASIC);
      ADVANCED_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ADVANCED);
      ELITE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ELITE);
      ULTIMATE_LOGISTICAL_TRANSPORTER = registerLogisticalTransporter(TransporterTier.ULTIMATE);
      RESTRICTIVE_TRANSPORTER = BLOCKS.register("restrictive_transporter", BlockRestrictiveTransporter::new, ItemBlockRestrictiveTransporter::new);
      DIVERSION_TRANSPORTER = BLOCKS.register("diversion_transporter", BlockDiversionTransporter::new, ItemBlockDiversionTransporter::new);
      BASIC_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.BASIC);
      ADVANCED_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ADVANCED);
      ELITE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ELITE);
      ULTIMATE_THERMODYNAMIC_CONDUCTOR = registerThermodynamicConductor(ConductorTier.ULTIMATE);
      BOUNDING_BLOCK = BLOCKS.register("bounding_block", BlockBounding::new);
      BASIC_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.BASIC_CHEMICAL_TANK);
      ADVANCED_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ADVANCED_CHEMICAL_TANK);
      ELITE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ELITE_CHEMICAL_TANK);
      ULTIMATE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.ULTIMATE_CHEMICAL_TANK);
      CREATIVE_CHEMICAL_TANK = registerChemicalTank(MekanismBlockTypes.CREATIVE_CHEMICAL_TANK);
      CARDBOARD_BOX = BLOCKS.register("cardboard_box", BlockCardboardBox::new, ItemBlockCardboardBox::new);
      SALT_BLOCK = BLOCKS.register("block_salt", Properties.m_284310_().m_60978_(0.5F).m_60918_(SoundType.f_56746_).m_280658_(NoteBlockInstrument.SNARE));
   }
}
