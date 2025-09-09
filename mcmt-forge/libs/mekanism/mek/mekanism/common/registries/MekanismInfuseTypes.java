package mekanism.common.registries;

import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.InfuseTypeDeferredRegister;
import mekanism.common.registration.impl.InfuseTypeRegistryObject;

public class MekanismInfuseTypes {
   public static final InfuseTypeDeferredRegister INFUSE_TYPES = new InfuseTypeDeferredRegister("mekanism");
   public static final InfuseTypeRegistryObject<InfuseType> CARBON = INFUSE_TYPES.register("carbon", 2894892);
   public static final InfuseTypeRegistryObject<InfuseType> REDSTONE = INFUSE_TYPES.register("redstone", 11732229);
   public static final InfuseTypeRegistryObject<InfuseType> DIAMOND = INFUSE_TYPES.register("diamond", 7138776);
   public static final InfuseTypeRegistryObject<InfuseType> REFINED_OBSIDIAN = INFUSE_TYPES.register("refined_obsidian", 8126701);
   public static final InfuseTypeRegistryObject<InfuseType> GOLD = INFUSE_TYPES.register("gold", 15912295);
   public static final InfuseTypeRegistryObject<InfuseType> TIN = INFUSE_TYPES.register("tin", 13421785);
   public static final InfuseTypeRegistryObject<InfuseType> FUNGI = INFUSE_TYPES.register("fungi", Mekanism.rl("infuse_type/fungi"), 7628138);
   public static final InfuseTypeRegistryObject<InfuseType> BIO = INFUSE_TYPES.register("bio", Mekanism.rl("infuse_type/bio"), 5916208);

   private MekanismInfuseTypes() {
   }
}
