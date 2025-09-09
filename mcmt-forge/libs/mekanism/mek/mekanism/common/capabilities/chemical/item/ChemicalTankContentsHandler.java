package mekanism.common.capabilities.chemical.item;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.common.capabilities.DynamicHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.merged.MergedTankContentsHandler;
import mekanism.common.tier.ChemicalTankTier;

@NothingNullByDefault
public class ChemicalTankContentsHandler extends MergedTankContentsHandler<MergedChemicalTank> {
   public static ChemicalTankContentsHandler create(ChemicalTankTier tier) {
      Objects.requireNonNull(tier, "Chemical tank tier cannot be null");
      return new ChemicalTankContentsHandler(tier);
   }

   private ChemicalTankContentsHandler(ChemicalTankTier tier) {
      this.mergedTank = MergedChemicalTank.create(
         new ChemicalTankRateLimitChemicalTank.GasTankRateLimitChemicalTank(
            tier,
            this.gasHandler = new DynamicChemicalHandler.DynamicGasHandler(
               side -> this.gasTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("GasTanks", this.gasTanks)
            )
         ),
         new ChemicalTankRateLimitChemicalTank.InfusionTankRateLimitChemicalTank(
            tier,
            this.infusionHandler = new DynamicChemicalHandler.DynamicInfusionHandler(
               side -> this.infusionTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("InfusionTanks", this.infusionTanks)
            )
         ),
         new ChemicalTankRateLimitChemicalTank.PigmentTankRateLimitChemicalTank(
            tier,
            this.pigmentHandler = new DynamicChemicalHandler.DynamicPigmentHandler(
               side -> this.pigmentTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("PigmentTanks", this.pigmentTanks)
            )
         ),
         new ChemicalTankRateLimitChemicalTank.SlurryTankRateLimitChemicalTank(
            tier,
            this.slurryHandler = new DynamicChemicalHandler.DynamicSlurryHandler(
               side -> this.slurryTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("SlurryTanks", this.slurryTanks)
            )
         )
      );
   }
}
