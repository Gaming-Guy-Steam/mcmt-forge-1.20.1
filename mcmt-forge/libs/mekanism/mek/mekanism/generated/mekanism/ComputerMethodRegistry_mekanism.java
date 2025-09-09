package mekanism.generated.mekanism;

import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerMultiblockData$ComputerHandler;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationMultiblockData$ComputerHandler;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import mekanism.common.content.filter.IFilter$ComputerHandler;
import mekanism.common.content.filter.IItemStackFilter;
import mekanism.common.content.filter.IItemStackFilter$ComputerHandler;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.content.filter.IModIDFilter$ComputerHandler;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.content.filter.ITagFilter$ComputerHandler;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixMultiblockData$ComputerHandler;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.content.miner.MinerFilter$ComputerHandler;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorFilter$ComputerHandler;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter$ComputerHandler;
import mekanism.common.content.qio.filter.QIOFilter;
import mekanism.common.content.qio.filter.QIOFilter$ComputerHandler;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSMultiblockData$ComputerHandler;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankMultiblockData$ComputerHandler;
import mekanism.common.content.transporter.SorterFilter;
import mekanism.common.content.transporter.SorterFilter$ComputerHandler;
import mekanism.common.integration.computer.ComputerEnergyHelper;
import mekanism.common.integration.computer.ComputerEnergyHelper$ComputerHandler;
import mekanism.common.integration.computer.ComputerFilterHelper;
import mekanism.common.integration.computer.ComputerFilterHelper$ComputerHandler;
import mekanism.common.integration.computer.FactoryRegistry;
import mekanism.common.integration.computer.IComputerMethodRegistry;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockData$ComputerHandler;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.TileEntityBin$ComputerHandler;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.tile.TileEntityChemicalTank$ComputerHandler;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.TileEntityEnergyCube$ComputerHandler;
import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.TileEntityFluidTank$ComputerHandler;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntityLogisticalSorter$ComputerHandler;
import mekanism.common.tile.TileEntityModificationStation;
import mekanism.common.tile.TileEntityModificationStation$ComputerHandler;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.TileEntityQuantumEntangloporter$ComputerHandler;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel$ComputerHandler;
import mekanism.common.tile.TileEntityTeleporter;
import mekanism.common.tile.TileEntityTeleporter$ComputerHandler;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.TileEntityMekanism$ComputerHandler;
import mekanism.common.tile.base.TileEntityUpdateable;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentConfig$ComputerHandler;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.component.TileComponentEjector$ComputerHandler;
import mekanism.common.tile.component.TileComponentSecurity;
import mekanism.common.tile.component.TileComponentSecurity$ComputerHandler;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.component.TileComponentUpgrade$ComputerHandler;
import mekanism.common.tile.factory.TileEntityCombiningFactory;
import mekanism.common.tile.factory.TileEntityCombiningFactory$ComputerHandler;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.TileEntityFactory$ComputerHandler;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory;
import mekanism.common.tile.factory.TileEntityItemStackGasToItemStackFactory$ComputerHandler;
import mekanism.common.tile.factory.TileEntityItemToItemFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory;
import mekanism.common.tile.factory.TileEntityMetallurgicInfuserFactory$ComputerHandler;
import mekanism.common.tile.factory.TileEntitySawingFactory;
import mekanism.common.tile.factory.TileEntitySawingFactory$ComputerHandler;
import mekanism.common.tile.laser.TileEntityBasicLaser;
import mekanism.common.tile.laser.TileEntityBasicLaser$ComputerHandler;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserAmplifier$ComputerHandler;
import mekanism.common.tile.laser.TileEntityLaserReceptor;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam$ComputerHandler;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer;
import mekanism.common.tile.machine.TileEntityAntiprotonicNucleosynthesizer$ComputerHandler;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer$ComputerHandler;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber;
import mekanism.common.tile.machine.TileEntityChemicalDissolutionChamber$ComputerHandler;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalInfuser$ComputerHandler;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer;
import mekanism.common.tile.machine.TileEntityChemicalOxidizer$ComputerHandler;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityChemicalWasher$ComputerHandler;
import mekanism.common.tile.machine.TileEntityCombiner;
import mekanism.common.tile.machine.TileEntityCombiner$ComputerHandler;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDigitalMiner$ComputerHandler;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer$ComputerHandler;
import mekanism.common.tile.machine.TileEntityElectricPump;
import mekanism.common.tile.machine.TileEntityElectricPump$ComputerHandler;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator$ComputerHandler;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher;
import mekanism.common.tile.machine.TileEntityFluidicPlenisher$ComputerHandler;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator$ComputerHandler;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater;
import mekanism.common.tile.machine.TileEntityFuelwoodHeater$ComputerHandler;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge$ComputerHandler;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser;
import mekanism.common.tile.machine.TileEntityMetallurgicInfuser$ComputerHandler;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier;
import mekanism.common.tile.machine.TileEntityNutritionalLiquifier$ComputerHandler;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.tile.machine.TileEntityOredictionificator$ComputerHandler;
import mekanism.common.tile.machine.TileEntityPaintingMachine;
import mekanism.common.tile.machine.TileEntityPaintingMachine$ComputerHandler;
import mekanism.common.tile.machine.TileEntityPigmentExtractor;
import mekanism.common.tile.machine.TileEntityPigmentExtractor$ComputerHandler;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import mekanism.common.tile.machine.TileEntityPigmentMixer$ComputerHandler;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill;
import mekanism.common.tile.machine.TileEntityPrecisionSawmill$ComputerHandler;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber$ComputerHandler;
import mekanism.common.tile.machine.TileEntityResistiveHeater;
import mekanism.common.tile.machine.TileEntityResistiveHeater$ComputerHandler;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator$ComputerHandler;
import mekanism.common.tile.machine.TileEntitySeismicVibrator;
import mekanism.common.tile.machine.TileEntitySeismicVibrator$ComputerHandler;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator;
import mekanism.common.tile.machine.TileEntitySolarNeutronActivator$ComputerHandler;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityBoilerValve;
import mekanism.common.tile.multiblock.TileEntityBoilerValve$ComputerHandler;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.multiblock.TileEntityInductionPort$ComputerHandler;
import mekanism.common.tile.multiblock.TileEntitySPSCasing;
import mekanism.common.tile.multiblock.TileEntitySPSPort;
import mekanism.common.tile.multiblock.TileEntitySPSPort$ComputerHandler;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine$ComputerHandler;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine$ComputerHandler;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.tile.prefab.TileEntityMultiblock$ComputerHandler;
import mekanism.common.tile.prefab.TileEntityProgressMachine;
import mekanism.common.tile.prefab.TileEntityProgressMachine$ComputerHandler;
import mekanism.common.tile.prefab.TileEntityRecipeMachine;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIOComponent$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIODashboard;
import mekanism.common.tile.qio.TileEntityQIODashboard$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import mekanism.common.tile.qio.TileEntityQIODriveArray$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOExporter$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIOImporter$ComputerHandler;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter$ComputerHandler;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter;
import mekanism.common.tile.transmitter.TileEntityDiversionTransporter$ComputerHandler;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe;
import mekanism.common.tile.transmitter.TileEntityMechanicalPipe$ComputerHandler;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube;
import mekanism.common.tile.transmitter.TileEntityPressurizedTube$ComputerHandler;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import mekanism.common.tile.transmitter.TileEntityUniversalCable$ComputerHandler;

public class ComputerMethodRegistry_mekanism implements IComputerMethodRegistry {
   @Override
   public void register() {
      FactoryRegistry.register(
         TileEntityEnergyCube.class,
         TileEntityEnergyCube$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(
         TileEntityQIOExporter.class,
         TileEntityQIOExporter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class,
         TileEntityQIOFilterHandler.class
      );
      FactoryRegistry.register(
         TileEntityDiversionTransporter.class,
         TileEntityDiversionTransporter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityTransmitter.class,
         TileEntityLogisticalTransporterBase.class
      );
      FactoryRegistry.register(OredictionificatorFilter.class, OredictionificatorFilter$ComputerHandler::new, BaseFilter.class);
      FactoryRegistry.register(
         TileEntityFluidTank.class, TileEntityFluidTank$ComputerHandler::new, TileEntityUpdateable.class, CapabilityTileEntity.class, TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityTeleporter.class,
         TileEntityTeleporter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityPigmentMixer.class,
         TileEntityPigmentMixer$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(
         TileEntityRotaryCondensentrator.class,
         TileEntityRotaryCondensentrator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(BoilerMultiblockData.class, BoilerMultiblockData$ComputerHandler::new, MultiblockData.class);
      FactoryRegistry.register(MinerFilter.class, MinerFilter$ComputerHandler::new, BaseFilter.class);
      FactoryRegistry.register(
         TileEntityDigitalMiner.class,
         TileEntityDigitalMiner$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(SorterFilter.class, SorterFilter$ComputerHandler::new, BaseFilter.class);
      FactoryRegistry.register(
         TileEntityQIODashboard.class,
         TileEntityQIODashboard$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class
      );
      FactoryRegistry.register(
         TileEntitySolarNeutronActivator.class,
         TileEntitySolarNeutronActivator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(
         TileEntityProgressMachine.class,
         TileEntityProgressMachine$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(
         TileEntityAdvancedElectricMachine.class,
         TileEntityAdvancedElectricMachine$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityFluidicPlenisher.class,
         TileEntityFluidicPlenisher$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityMultiblock.class,
         TileEntityMultiblock$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityItemStackGasToItemStackFactory.class,
         TileEntityItemStackGasToItemStackFactory$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityFactory.class,
         TileEntityItemToItemFactory.class
      );
      FactoryRegistry.register(
         TileEntityCombiner.class,
         TileEntityCombiner$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(TankMultiblockData.class, TankMultiblockData$ComputerHandler::new, MultiblockData.class);
      FactoryRegistry.register(
         TileEntityPressurizedReactionChamber.class,
         TileEntityPressurizedReactionChamber$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityLaserAmplifier.class,
         TileEntityLaserAmplifier$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityBasicLaser.class,
         TileEntityLaserReceptor.class
      );
      FactoryRegistry.register(
         TileEntityOredictionificator.class,
         TileEntityOredictionificator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(
         TileEntityMetallurgicInfuser.class,
         TileEntityMetallurgicInfuser$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityPaintingMachine.class,
         TileEntityPaintingMachine$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityBin.class, TileEntityBin$ComputerHandler::new, TileEntityUpdateable.class, CapabilityTileEntity.class, TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityLogisticalSorter.class,
         TileEntityLogisticalSorter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityMetallurgicInfuserFactory.class,
         TileEntityMetallurgicInfuserFactory$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityFactory.class,
         TileEntityItemToItemFactory.class
      );
      FactoryRegistry.register(TileComponentConfig.class, TileComponentConfig$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityCombiningFactory.class,
         TileEntityCombiningFactory$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityFactory.class,
         TileEntityItemToItemFactory.class
      );
      FactoryRegistry.register(
         TileEntityNutritionalLiquifier.class,
         TileEntityNutritionalLiquifier$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityUniversalCable.class,
         TileEntityUniversalCable$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityTransmitter.class
      );
      FactoryRegistry.register(
         TileEntityBoilerValve.class,
         TileEntityBoilerValve$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityMultiblock.class,
         TileEntityBoilerCasing.class
      );
      FactoryRegistry.register(
         TileEntityInductionPort.class,
         TileEntityInductionPort$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityMultiblock.class,
         TileEntityInductionCasing.class
      );
      FactoryRegistry.register(
         TileEntityBasicLaser.class,
         TileEntityBasicLaser$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityMechanicalPipe.class,
         TileEntityMechanicalPipe$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityTransmitter.class
      );
      FactoryRegistry.register(
         TileEntityDimensionalStabilizer.class,
         TileEntityDimensionalStabilizer$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(TileEntityMekanism.class, TileEntityMekanism$ComputerHandler::new, TileEntityUpdateable.class, CapabilityTileEntity.class);
      FactoryRegistry.register(
         TileEntitySPSPort.class,
         TileEntitySPSPort$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityMultiblock.class,
         TileEntitySPSCasing.class
      );
      FactoryRegistry.register(
         TileEntitySawingFactory.class,
         TileEntitySawingFactory$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityFactory.class
      );
      FactoryRegistry.register(
         TileEntityElectricPump.class,
         TileEntityElectricPump$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityModificationStation.class,
         TileEntityModificationStation$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.registerInterface(IFilter.class, IFilter$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityFormulaicAssemblicator.class,
         TileEntityFormulaicAssemblicator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(MatrixMultiblockData.class, MatrixMultiblockData$ComputerHandler::new, MultiblockData.class);
      FactoryRegistry.register(TileComponentUpgrade.class, TileComponentUpgrade$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityAntiprotonicNucleosynthesizer.class,
         TileEntityAntiprotonicNucleosynthesizer$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(QIOFilter.class, QIOFilter$ComputerHandler::new, BaseFilter.class);
      FactoryRegistry.register(
         TileEntityQIOComponent.class,
         TileEntityQIOComponent$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityQuantumEntangloporter.class,
         TileEntityQuantumEntangloporter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(
         TileEntityChemicalWasher.class,
         TileEntityChemicalWasher$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(TileComponentSecurity.class, TileComponentSecurity$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityQIOFilterHandler.class,
         TileEntityQIOFilterHandler$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class
      );
      FactoryRegistry.register(
         TileEntityChemicalOxidizer.class,
         TileEntityChemicalOxidizer$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(TileComponentEjector.class, TileComponentEjector$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityChemicalInfuser.class,
         TileEntityChemicalInfuser$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(
         TileEntityElectrolyticSeparator.class,
         TileEntityElectrolyticSeparator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(ComputerFilterHelper.class, ComputerFilterHelper$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityLaserTractorBeam.class,
         TileEntityLaserTractorBeam$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityBasicLaser.class,
         TileEntityLaserReceptor.class
      );
      FactoryRegistry.register(
         TileEntityResistiveHeater.class,
         TileEntityResistiveHeater$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntitySeismicVibrator.class,
         TileEntitySeismicVibrator$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(MultiblockData.class, MultiblockData$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityPrecisionSawmill.class,
         TileEntityPrecisionSawmill$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityChemicalDissolutionChamber.class,
         TileEntityChemicalDissolutionChamber$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityPigmentExtractor.class,
         TileEntityPigmentExtractor$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityQIOImporter.class,
         TileEntityQIOImporter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class,
         TileEntityQIOFilterHandler.class
      );
      FactoryRegistry.registerInterface(IModIDFilter.class, IModIDFilter$ComputerHandler::new);
      FactoryRegistry.register(
         OredictionificatorItemFilter.class, OredictionificatorItemFilter$ComputerHandler::new, BaseFilter.class, OredictionificatorFilter.class
      );
      FactoryRegistry.register(
         TileEntityPressurizedTube.class,
         TileEntityPressurizedTube$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityTransmitter.class
      );
      FactoryRegistry.register(
         TileEntityChemicalTank.class,
         TileEntityChemicalTank$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(
         TileEntityIsotopicCentrifuge.class,
         TileEntityIsotopicCentrifuge$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class
      );
      FactoryRegistry.register(
         TileEntityElectricMachine.class,
         TileEntityElectricMachine$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(SPSMultiblockData.class, SPSMultiblockData$ComputerHandler::new, MultiblockData.class);
      FactoryRegistry.registerInterface(IItemStackFilter.class, IItemStackFilter$ComputerHandler::new);
      FactoryRegistry.register(
         TileEntityFuelwoodHeater.class,
         TileEntityFuelwoodHeater$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.registerInterface(ITagFilter.class, ITagFilter$ComputerHandler::new);
      FactoryRegistry.register(EvaporationMultiblockData.class, EvaporationMultiblockData$ComputerHandler::new, MultiblockData.class);
      FactoryRegistry.register(
         TileEntityChemicalCrystallizer.class,
         TileEntityChemicalCrystallizer$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class,
         TileEntityRecipeMachine.class,
         TileEntityProgressMachine.class
      );
      FactoryRegistry.register(
         TileEntityFactory.class,
         TileEntityFactory$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityConfigurableMachine.class
      );
      FactoryRegistry.register(
         TileEntityRadioactiveWasteBarrel.class,
         TileEntityRadioactiveWasteBarrel$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class
      );
      FactoryRegistry.register(
         TileEntityQIODriveArray.class,
         TileEntityQIODriveArray$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class
      );
      FactoryRegistry.register(
         TileEntityQIORedstoneAdapter.class,
         TileEntityQIORedstoneAdapter$ComputerHandler::new,
         TileEntityUpdateable.class,
         CapabilityTileEntity.class,
         TileEntityMekanism.class,
         TileEntityQIOComponent.class
      );
      FactoryRegistry.register(ComputerEnergyHelper.class, ComputerEnergyHelper$ComputerHandler::new);
   }
}
