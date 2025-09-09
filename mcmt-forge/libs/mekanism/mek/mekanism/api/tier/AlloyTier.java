package mekanism.api.tier;

public enum AlloyTier implements ITier {
   INFUSED("infused", BaseTier.ADVANCED),
   REINFORCED("reinforced", BaseTier.ELITE),
   ATOMIC("atomic", BaseTier.ULTIMATE);

   private final BaseTier baseTier;
   private final String name;

   private AlloyTier(String name, BaseTier base) {
      this.baseTier = base;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public BaseTier getBaseTier() {
      return this.baseTier;
   }
}
