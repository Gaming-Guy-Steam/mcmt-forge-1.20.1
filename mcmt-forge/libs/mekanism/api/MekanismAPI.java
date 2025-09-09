package mekanism.api;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.EmptyGas;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.EmptyInfuseType;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.EmptyPigment;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.EmptySlurry;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@NothingNullByDefault
public class MekanismAPI {
   public static final String API_VERSION = "10.4.0";
   public static final String MEKANISM_MODID = "mekanism";
   public static boolean debug = false;
   public static final Logger logger = LogUtils.getLogger();
   public static final ResourceKey<Registry<Gas>> GAS_REGISTRY_NAME = registryKey(Gas.class, "gas");
   public static final ResourceKey<Registry<InfuseType>> INFUSE_TYPE_REGISTRY_NAME = registryKey(InfuseType.class, "infuse_type");
   public static final ResourceKey<Registry<Pigment>> PIGMENT_REGISTRY_NAME = registryKey(Pigment.class, "pigment");
   public static final ResourceKey<Registry<Slurry>> SLURRY_REGISTRY_NAME = registryKey(Slurry.class, "slurry");
   public static final ResourceKey<Registry<ModuleData<?>>> MODULE_REGISTRY_NAME = registryKey(ModuleData.class, "module");
   public static final ResourceKey<Registry<RobitSkin>> ROBIT_SKIN_REGISTRY_NAME = registryKey(RobitSkin.class, "robit_skin");
   public static final ResourceKey<Registry<Codec<? extends RobitSkin>>> ROBIT_SKIN_SERIALIZER_REGISTRY_NAME = codecRegistryKey(
      RobitSkin.class, "robit_skin_serializer"
   );
   @Nullable
   private static IForgeRegistry<Gas> GAS_REGISTRY;
   @Nullable
   private static IForgeRegistry<InfuseType> INFUSE_TYPE_REGISTRY;
   @Nullable
   private static IForgeRegistry<Pigment> PIGMENT_REGISTRY;
   @Nullable
   private static IForgeRegistry<Slurry> SLURRY_REGISTRY;
   @Nullable
   private static IForgeRegistry<ModuleData<?>> MODULE_REGISTRY;
   @Nullable
   private static IForgeRegistry<Codec<? extends RobitSkin>> ROBIT_SKIN_SERIALIZER_REGISTRY;
   public static final Gas EMPTY_GAS = new EmptyGas();
   public static final InfuseType EMPTY_INFUSE_TYPE = new EmptyInfuseType();
   public static final Pigment EMPTY_PIGMENT = new EmptyPigment();
   public static final Slurry EMPTY_SLURRY = new EmptySlurry();

   private MekanismAPI() {
   }

   private static <T> ResourceKey<Registry<T>> registryKey(Class<T> compileTimeTypeValidator, String path) {
      return ResourceKey.m_135788_(new ResourceLocation("mekanism", path));
   }

   private static <T> ResourceKey<Registry<Codec<? extends T>>> codecRegistryKey(Class<T> compileTimeTypeValidator, String path) {
      return ResourceKey.m_135788_(new ResourceLocation("mekanism", path));
   }

   public static IForgeRegistry<Gas> gasRegistry() {
      if (GAS_REGISTRY == null) {
         GAS_REGISTRY = RegistryManager.ACTIVE.getRegistry(GAS_REGISTRY_NAME);
      }

      return GAS_REGISTRY;
   }

   public static IForgeRegistry<InfuseType> infuseTypeRegistry() {
      if (INFUSE_TYPE_REGISTRY == null) {
         INFUSE_TYPE_REGISTRY = RegistryManager.ACTIVE.getRegistry(INFUSE_TYPE_REGISTRY_NAME);
      }

      return INFUSE_TYPE_REGISTRY;
   }

   public static IForgeRegistry<Pigment> pigmentRegistry() {
      if (PIGMENT_REGISTRY == null) {
         PIGMENT_REGISTRY = RegistryManager.ACTIVE.getRegistry(PIGMENT_REGISTRY_NAME);
      }

      return PIGMENT_REGISTRY;
   }

   public static IForgeRegistry<Slurry> slurryRegistry() {
      if (SLURRY_REGISTRY == null) {
         SLURRY_REGISTRY = RegistryManager.ACTIVE.getRegistry(SLURRY_REGISTRY_NAME);
      }

      return SLURRY_REGISTRY;
   }

   public static IForgeRegistry<ModuleData<?>> moduleRegistry() {
      if (MODULE_REGISTRY == null) {
         MODULE_REGISTRY = RegistryManager.ACTIVE.getRegistry(MODULE_REGISTRY_NAME);
      }

      return MODULE_REGISTRY;
   }

   public static IForgeRegistry<Codec<? extends RobitSkin>> robitSkinSerializerRegistry() {
      if (ROBIT_SKIN_SERIALIZER_REGISTRY == null) {
         ROBIT_SKIN_SERIALIZER_REGISTRY = RegistryManager.ACTIVE.getRegistry(ROBIT_SKIN_SERIALIZER_REGISTRY_NAME);
      }

      return ROBIT_SKIN_SERIALIZER_REGISTRY;
   }
}
