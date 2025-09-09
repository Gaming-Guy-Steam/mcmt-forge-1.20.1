package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig.Type;

public class StorageConfig extends BaseMekanismConfig {
   private final ForgeConfigSpec configSpec;
   public final CachedFloatingLongValue enrichmentChamber;
   public final CachedFloatingLongValue osmiumCompressor;
   public final CachedFloatingLongValue combiner;
   public final CachedFloatingLongValue crusher;
   public final CachedFloatingLongValue metallurgicInfuser;
   public final CachedFloatingLongValue purificationChamber;
   public final CachedFloatingLongValue energizedSmelter;
   public final CachedFloatingLongValue digitalMiner;
   public final CachedFloatingLongValue electricPump;
   public final CachedFloatingLongValue chargePad;
   public final CachedFloatingLongValue rotaryCondensentrator;
   public final CachedFloatingLongValue oxidationChamber;
   public final CachedFloatingLongValue chemicalInfuser;
   public final CachedFloatingLongValue chemicalInjectionChamber;
   public final CachedFloatingLongValue electrolyticSeparator;
   public final CachedFloatingLongValue precisionSawmill;
   public final CachedFloatingLongValue chemicalDissolutionChamber;
   public final CachedFloatingLongValue chemicalWasher;
   public final CachedFloatingLongValue chemicalCrystallizer;
   public final CachedFloatingLongValue seismicVibrator;
   public final CachedFloatingLongValue pressurizedReactionBase;
   public final CachedFloatingLongValue fluidicPlenisher;
   public final CachedFloatingLongValue laser;
   public final CachedFloatingLongValue laserAmplifier;
   public final CachedFloatingLongValue laserTractorBeam;
   public final CachedFloatingLongValue formulaicAssemblicator;
   public final CachedFloatingLongValue teleporter;
   public final CachedFloatingLongValue modificationStation;
   public final CachedFloatingLongValue isotopicCentrifuge;
   public final CachedFloatingLongValue nutritionalLiquifier;
   public final CachedFloatingLongValue antiprotonicNucleosynthesizer;
   public final CachedFloatingLongValue pigmentExtractor;
   public final CachedFloatingLongValue pigmentMixer;
   public final CachedFloatingLongValue paintingMachine;
   public final CachedFloatingLongValue spsPort;
   public final CachedFloatingLongValue dimensionalStabilizer;

   StorageConfig() {
      Builder builder = new Builder();
      builder.comment("Machine Energy Storage Config. This config is synced from server to client.").push("storage");
      this.enrichmentChamber = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "enrichmentChamber", FloatingLong.createConst(20000L)
      );
      this.osmiumCompressor = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "osmiumCompressor", FloatingLong.createConst(80000L)
      );
      this.combiner = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "combiner", FloatingLong.createConst(40000L));
      this.crusher = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "crusher", FloatingLong.createConst(20000L));
      this.metallurgicInfuser = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "metallurgicInfuser", FloatingLong.createConst(20000L)
      );
      this.purificationChamber = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "purificationChamber", FloatingLong.createConst(80000L)
      );
      this.energizedSmelter = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "energizedSmelter", FloatingLong.createConst(20000L)
      );
      this.digitalMiner = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "digitalMiner", FloatingLong.createConst(50000L));
      this.electricPump = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "electricPump", FloatingLong.createConst(40000L));
      this.chargePad = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "chargePad", FloatingLong.createConst(2048000L));
      this.rotaryCondensentrator = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "rotaryCondensentrator", FloatingLong.createConst(20000L)
      );
      this.oxidationChamber = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "oxidationChamber", FloatingLong.createConst(80000L)
      );
      this.chemicalInfuser = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "chemicalInfuser", FloatingLong.createConst(80000L));
      this.chemicalInjectionChamber = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "chemicalInjectionChamber", FloatingLong.createConst(160000L)
      );
      this.electrolyticSeparator = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "electrolyticSeparator", FloatingLong.createConst(160000L)
      );
      this.precisionSawmill = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "precisionSawmill", FloatingLong.createConst(20000L)
      );
      this.chemicalDissolutionChamber = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "chemicalDissolutionChamber", FloatingLong.createConst(160000L)
      );
      this.chemicalWasher = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "chemicalWasher", FloatingLong.createConst(80000L));
      this.chemicalCrystallizer = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "chemicalCrystallizer", FloatingLong.createConst(160000L)
      );
      this.seismicVibrator = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "seismicVibrator", FloatingLong.createConst(20000L));
      this.pressurizedReactionBase = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "pressurizedReactionBase", FloatingLong.createConst(2000L)
      );
      this.fluidicPlenisher = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "fluidicPlenisher", FloatingLong.createConst(40000L)
      );
      this.laser = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "laser", FloatingLong.createConst(2000000L));
      this.laserAmplifier = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "laserAmplifier", FloatingLong.createConst(5000000000L)
      );
      this.laserTractorBeam = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "laserTractorBeam", FloatingLong.createConst(5000000000L)
      );
      this.formulaicAssemblicator = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "formulaicAssemblicator", FloatingLong.createConst(40000L)
      );
      this.teleporter = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "teleporter", FloatingLong.createConst(5000000L));
      this.modificationStation = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "modificationStation", FloatingLong.createConst(40000L)
      );
      this.isotopicCentrifuge = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "isotopicCentrifuge", FloatingLong.createConst(80000L)
      );
      this.nutritionalLiquifier = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "nutritionalLiquifier", FloatingLong.createConst(40000L)
      );
      this.antiprotonicNucleosynthesizer = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules). Also defines max process rate.", "antiprotonicNucleosynthesizer", FloatingLong.createConst(1000000000L)
      );
      this.pigmentExtractor = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "pigmentExtractor", FloatingLong.createConst(40000L)
      );
      this.pigmentMixer = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "pigmentMixer", FloatingLong.createConst(80000L));
      this.paintingMachine = CachedFloatingLongValue.define(this, builder, "Base energy storage (Joules).", "paintingMachine", FloatingLong.createConst(40000L));
      this.spsPort = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules). Also defines max output rate.", "spsPort", FloatingLong.createConst(1000000000L)
      );
      this.dimensionalStabilizer = CachedFloatingLongValue.define(
         this, builder, "Base energy storage (Joules).", "dimensionalStabilizer", FloatingLong.createConst(40000L)
      );
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "machine-storage";
   }

   @Override
   public ForgeConfigSpec getConfigSpec() {
      return this.configSpec;
   }

   @Override
   public Type getConfigType() {
      return Type.SERVER;
   }

   @Override
   public boolean addToContainer() {
      return false;
   }
}
