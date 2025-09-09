package mekanism.common.config;

import mekanism.api.math.FloatingLong;
import mekanism.common.config.value.CachedFloatingLongValue;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.fml.config.ModConfig.Type;

public class UsageConfig extends BaseMekanismConfig {
   private static final String TELEPORTER_CATEGORY = "teleporter";
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
   public final CachedFloatingLongValue precisionSawmill;
   public final CachedFloatingLongValue chemicalDissolutionChamber;
   public final CachedFloatingLongValue chemicalWasher;
   public final CachedFloatingLongValue chemicalCrystallizer;
   public final CachedFloatingLongValue seismicVibrator;
   public final CachedFloatingLongValue pressurizedReactionBase;
   public final CachedFloatingLongValue fluidicPlenisher;
   public final CachedFloatingLongValue laser;
   public final CachedFloatingLongValue formulaicAssemblicator;
   public final CachedFloatingLongValue modificationStation;
   public final CachedFloatingLongValue isotopicCentrifuge;
   public final CachedFloatingLongValue nutritionalLiquifier;
   public final CachedFloatingLongValue antiprotonicNucleosynthesizer;
   public final CachedFloatingLongValue pigmentExtractor;
   public final CachedFloatingLongValue pigmentMixer;
   public final CachedFloatingLongValue paintingMachine;
   public final CachedFloatingLongValue dimensionalStabilizer;
   public final CachedFloatingLongValue teleporterBase;
   public final CachedFloatingLongValue teleporterDistance;
   public final CachedFloatingLongValue teleporterDimensionPenalty;

   UsageConfig() {
      Builder builder = new Builder();
      builder.comment("Machine Energy Usage Config. This config is synced from server to client.").push("usage");
      this.enrichmentChamber = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "enrichmentChamber", FloatingLong.createConst(50L)
      );
      this.osmiumCompressor = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "osmiumCompressor", FloatingLong.createConst(100L)
      );
      this.combiner = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "combiner", FloatingLong.createConst(50L));
      this.crusher = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "crusher", FloatingLong.createConst(50L));
      this.metallurgicInfuser = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "metallurgicInfuser", FloatingLong.createConst(50L)
      );
      this.purificationChamber = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "purificationChamber", FloatingLong.createConst(200L)
      );
      this.energizedSmelter = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "energizedSmelter", FloatingLong.createConst(50L)
      );
      this.digitalMiner = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "digitalMiner", FloatingLong.createConst(1000L));
      this.electricPump = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "electricPump", FloatingLong.createConst(100L));
      this.chargePad = CachedFloatingLongValue.define(
         this, builder, "Energy that can be transferred at once per charge operation (Joules).", "chargePad", FloatingLong.createConst(1024000L)
      );
      this.rotaryCondensentrator = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "rotaryCondensentrator", FloatingLong.createConst(50L)
      );
      this.oxidationChamber = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "oxidationChamber", FloatingLong.createConst(200L)
      );
      this.chemicalInfuser = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "chemicalInfuser", FloatingLong.createConst(200L)
      );
      this.chemicalInjectionChamber = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "chemicalInjectionChamber", FloatingLong.createConst(400L)
      );
      this.precisionSawmill = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "precisionSawmill", FloatingLong.createConst(50L)
      );
      this.chemicalDissolutionChamber = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "chemicalDissolutionChamber", FloatingLong.createConst(400L)
      );
      this.chemicalWasher = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "chemicalWasher", FloatingLong.createConst(200L)
      );
      this.chemicalCrystallizer = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "chemicalCrystallizer", FloatingLong.createConst(400L)
      );
      this.seismicVibrator = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "seismicVibrator", FloatingLong.createConst(50L)
      );
      this.pressurizedReactionBase = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "pressurizedReactionBase", FloatingLong.createConst(5L)
      );
      this.fluidicPlenisher = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "fluidicPlenisher", FloatingLong.createConst(100L)
      );
      this.laser = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "laser", FloatingLong.createConst(10000L));
      this.formulaicAssemblicator = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "formulaicAssemblicator", FloatingLong.createConst(100L)
      );
      this.modificationStation = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "modificationStation", FloatingLong.createConst(100L)
      );
      this.isotopicCentrifuge = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "isotopicCentrifuge", FloatingLong.createConst(200L)
      );
      this.nutritionalLiquifier = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "nutritionalLiquifier", FloatingLong.createConst(200L)
      );
      this.antiprotonicNucleosynthesizer = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "antiprotonicNucleosynthesizer", FloatingLong.createConst(100000L)
      );
      this.pigmentExtractor = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "pigmentExtractor", FloatingLong.createConst(200L)
      );
      this.pigmentMixer = CachedFloatingLongValue.define(this, builder, "Energy per operation tick (Joules).", "pigmentMixer", FloatingLong.createConst(200L));
      this.paintingMachine = CachedFloatingLongValue.define(
         this, builder, "Energy per operation tick (Joules).", "paintingMachine", FloatingLong.createConst(100L)
      );
      this.dimensionalStabilizer = CachedFloatingLongValue.define(
         this, builder, "Energy per chunk per tick (Joules).", "dimensionalStabilizer", FloatingLong.createConst(5000L)
      );
      builder.comment("Teleporter").push("teleporter");
      this.teleporterBase = CachedFloatingLongValue.define(
         this, builder, "Base Joules cost for a teleportation.", "teleporterBase", FloatingLong.createConst(1000L)
      );
      this.teleporterDistance = CachedFloatingLongValue.define(
         this,
         builder,
         "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2).",
         "teleporterDistance",
         FloatingLong.createConst(10L)
      );
      this.teleporterDimensionPenalty = CachedFloatingLongValue.define(
         this,
         builder,
         "Flat additional cost for interdimensional teleportation. Distance is still taken into account minimizing energy cost based on dimension scales.",
         "teleporterDimensionPenalty",
         FloatingLong.createConst(10000L)
      );
      builder.pop();
      builder.pop();
      this.configSpec = builder.build();
   }

   @Override
   public String getFileName() {
      return "machine-usage";
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
