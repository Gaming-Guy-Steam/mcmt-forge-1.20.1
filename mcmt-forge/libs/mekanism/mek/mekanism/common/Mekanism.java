package mekanism.common;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.providers.IItemProvider;
import mekanism.client.IncompleteRecipeScanner;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.base.IModModule;
import mekanism.common.base.KeySync;
import mekanism.common.base.MekFakePlayer;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.base.PlayerState;
import mekanism.common.base.TagCache;
import mekanism.common.command.CommandMek;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.command.builders.Builders;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.MekanismModConfig;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerValidator;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.content.evaporation.EvaporationValidator;
import mekanism.common.content.gear.MekaSuitDispenseBehavior;
import mekanism.common.content.gear.ModuleDispenseBehavior;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.content.matrix.MatrixMultiblockData;
import mekanism.common.content.matrix.MatrixValidator;
import mekanism.common.content.network.BoxedChemicalNetwork;
import mekanism.common.content.network.EnergyNetwork;
import mekanism.common.content.network.FluidNetwork;
import mekanism.common.content.qio.QIOGlobalItemLookup;
import mekanism.common.content.sps.SPSCache;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSValidator;
import mekanism.common.content.tank.TankCache;
import mekanism.common.content.tank.TankMultiblockData;
import mekanism.common.content.tank.TankValidator;
import mekanism.common.content.transporter.PathfinderCache;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.loot.MekanismLootFunctions;
import mekanism.common.item.predicate.FullCanteenItemPredicate;
import mekanism.common.item.predicate.MaxedModuleContainerItemPredicate;
import mekanism.common.lib.MekAnnotationScanner;
import mekanism.common.lib.Version;
import mekanism.common.lib.frequency.FrequencyManager;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.lib.multiblock.MultiblockCache;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.lib.transmitter.TransmitterNetworkRegistry;
import mekanism.common.network.PacketHandler;
import mekanism.common.network.to_client.PacketTransmitterUpdate;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.condition.ConditionExistsCondition;
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismCreativeTabs;
import mekanism.common.registries.MekanismDataSerializers;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismGameEvents;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismHeightProviderTypes;
import mekanism.common.registries.MekanismInfuseTypes;
import mekanism.common.registries.MekanismIntProviderTypes;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.registries.MekanismPlacementModifiers;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tags.MekanismTags;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.machine.TileEntityOredictionificator;
import mekanism.common.world.GenHandler;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.level.LevelEvent.Load;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.slf4j.Logger;

@Mod("mekanism")
public class Mekanism {
   public static final String MODID = "mekanism";
   public static final String MOD_NAME = "Mekanism";
   public static final String LOG_TAG = "[Mekanism]";
   public static final PlayerState playerState = new PlayerState();
   private final PacketHandler packetHandler;
   public static final Logger logger = LogUtils.getLogger();
   public static Mekanism instance;
   public static final MekanismHooks hooks = new MekanismHooks();
   public final Version versionNumber;
   public static final MultiblockManager<TankMultiblockData> tankManager = new MultiblockManager<>("dynamicTank", TankCache::new, TankValidator::new);
   public static final MultiblockManager<MatrixMultiblockData> matrixManager = new MultiblockManager<>(
      "inductionMatrix", MultiblockCache::new, MatrixValidator::new
   );
   public static final MultiblockManager<BoilerMultiblockData> boilerManager = new MultiblockManager<>(
      "thermoelectricBoiler", MultiblockCache::new, BoilerValidator::new
   );
   public static final MultiblockManager<EvaporationMultiblockData> evaporationManager = new MultiblockManager<>(
      "evaporation", MultiblockCache::new, EvaporationValidator::new
   );
   public static final MultiblockManager<SPSMultiblockData> spsManager = new MultiblockManager<>("sps", SPSCache::new, SPSValidator::new);
   public static final List<IModModule> modulesLoaded = new ArrayList<>();
   public static final CommonWorldTickHandler worldTickHandler = new CommonWorldTickHandler();
   public static final GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("mekanism.common".getBytes(StandardCharsets.UTF_8)), "[Mekanism]");
   public static final KeySync keyMap = new KeySync();
   public static final Set<Coord4D> activeVibrators = new ObjectOpenHashSet();
   private ReloadListener recipeCacheManager;

   public Mekanism() {
      instance = this;
      MekanismConfig.registerConfigs(ModLoadingContext.get());
      MinecraftForge.EVENT_BUS.addListener(this::onEnergyTransferred);
      MinecraftForge.EVENT_BUS.addListener(this::onChemicalTransferred);
      MinecraftForge.EVENT_BUS.addListener(this::onLiquidTransferred);
      MinecraftForge.EVENT_BUS.addListener(this::onWorldLoad);
      MinecraftForge.EVENT_BUS.addListener(this::onWorldUnload);
      MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
      MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::addReloadListenersLowest);
      MinecraftForge.EVENT_BUS.addListener(this::onTagsReload);
      MinecraftForge.EVENT_BUS.addListener(MekanismPermissions::registerPermissionNodes);
      MinecraftForge.EVENT_BUS.register(IncompleteRecipeScanner.class);
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      modEventBus.addListener(this::commonSetup);
      modEventBus.addListener(this::onConfigLoad);
      modEventBus.addListener(this::imcQueue);
      modEventBus.addListener(this::imcHandle);
      MekanismItems.ITEMS.register(modEventBus);
      MekanismBlocks.BLOCKS.register(modEventBus);
      MekanismFluids.FLUIDS.register(modEventBus);
      MekanismContainerTypes.CONTAINER_TYPES.register(modEventBus);
      MekanismCreativeTabs.CREATIVE_TABS.register(modEventBus);
      MekanismEntityTypes.ENTITY_TYPES.register(modEventBus);
      MekanismTileEntityTypes.TILE_ENTITY_TYPES.register(modEventBus);
      MekanismGameEvents.GAME_EVENTS.register(modEventBus);
      MekanismSounds.SOUND_EVENTS.register(modEventBus);
      MekanismParticleTypes.PARTICLE_TYPES.register(modEventBus);
      MekanismHeightProviderTypes.HEIGHT_PROVIDER_TYPES.register(modEventBus);
      MekanismIntProviderTypes.INT_PROVIDER_TYPES.register(modEventBus);
      MekanismPlacementModifiers.PLACEMENT_MODIFIERS.register(modEventBus);
      MekanismFeatures.FEATURES.register(modEventBus);
      MekanismRecipeType.RECIPE_TYPES.register(modEventBus);
      MekanismRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
      MekanismDataSerializers.DATA_SERIALIZERS.register(modEventBus);
      MekanismLootFunctions.REGISTER.register(modEventBus);
      MekanismGases.GASES.createAndRegisterChemical(modEventBus);
      MekanismInfuseTypes.INFUSE_TYPES.createAndRegisterChemical(modEventBus);
      MekanismPigments.PIGMENTS.createAndRegisterChemical(modEventBus);
      MekanismSlurries.SLURRIES.createAndRegisterChemical(modEventBus);
      MekanismRobitSkins.createAndRegisterDatapack(modEventBus);
      MekanismModules.MODULES.createAndRegister(modEventBus);
      modEventBus.addListener(this::registerEventListener);
      this.versionNumber = new Version(ModLoadingContext.get().getActiveContainer());
      this.packetHandler = new PacketHandler();
      hooks.hookConstructor(modEventBus);
   }

   public static synchronized void addModule(IModModule modModule) {
      modulesLoaded.add(modModule);
   }

   public static PacketHandler packetHandler() {
      return instance.packetHandler;
   }

   private void registerEventListener(RegisterEvent event) {
      ResourceLocation emptyName = rl("empty");
      event.register(MekanismAPI.GAS_REGISTRY_NAME, emptyName, () -> MekanismAPI.EMPTY_GAS);
      event.register(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, emptyName, () -> MekanismAPI.EMPTY_INFUSE_TYPE);
      event.register(MekanismAPI.PIGMENT_REGISTRY_NAME, emptyName, () -> MekanismAPI.EMPTY_PIGMENT);
      event.register(MekanismAPI.SLURRY_REGISTRY_NAME, emptyName, () -> MekanismAPI.EMPTY_SLURRY);
      if (event.getRegistryKey().equals(Keys.RECIPE_SERIALIZERS)) {
         CraftingHelper.register(ConditionExistsCondition.Serializer.INSTANCE);
         CraftingHelper.register(ModVersionLoadedCondition.Serializer.INSTANCE);
      }
   }

   public static ResourceLocation rl(String path) {
      return new ResourceLocation("mekanism", path);
   }

   private void setRecipeCacheManager(ReloadListener manager) {
      if (this.recipeCacheManager == null) {
         this.recipeCacheManager = manager;
      } else {
         logger.warn("Recipe cache manager has already been set.");
      }
   }

   public ReloadListener getRecipeCacheManager() {
      return this.recipeCacheManager;
   }

   private void onTagsReload(TagsUpdatedEvent event) {
      TagCache.resetTagCaches();
   }

   private void addReloadListenersLowest(AddReloadListenerEvent event) {
      event.addListener(this.getRecipeCacheManager());
   }

   private void registerCommands(RegisterCommandsEvent event) {
      BuildCommand.register("boiler", MekanismLang.BOILER, new Builders.BoilerBuilder());
      BuildCommand.register("matrix", MekanismLang.MATRIX, new Builders.MatrixBuilder());
      BuildCommand.register("tank", MekanismLang.DYNAMIC_TANK, new Builders.TankBuilder());
      BuildCommand.register("evaporation", MekanismLang.EVAPORATION_PLANT, new Builders.EvaporationBuilder());
      BuildCommand.register("sps", MekanismLang.SPS, new Builders.SPSBuilder());
      event.getDispatcher().register(CommandMek.register());
   }

   private void serverStopped(ServerStoppedEvent event) {
      playerState.clear(false);
      activeVibrators.clear();
      worldTickHandler.resetChunkData();
      FrequencyType.clear();
      BoilerMultiblockData.hotMap.clear();
      QIOGlobalItemLookup.INSTANCE.reset();
      RadiationManager.get().reset();
      MultiblockManager.reset();
      FrequencyManager.reset();
      TransporterManager.reset();
      PathfinderCache.reset();
      TransmitterNetworkRegistry.reset();
      GenHandler.reset();
      PersonalStorageManager.reset();
   }

   private void imcQueue(InterModEnqueueEvent event) {
      hooks.sendIMCMessages(event);
      MekanismIMC.addModulesToAll(MekanismModules.ENERGY_UNIT);
      MekanismIMC.addMekaSuitModules(MekanismModules.COLOR_MODULATION_UNIT, MekanismModules.LASER_DISSIPATION_UNIT, MekanismModules.RADIATION_SHIELDING_UNIT);
      MekanismIMC.addMekaToolModules(
         MekanismModules.ATTACK_AMPLIFICATION_UNIT,
         MekanismModules.SILK_TOUCH_UNIT,
         MekanismModules.FORTUNE_UNIT,
         MekanismModules.BLASTING_UNIT,
         MekanismModules.VEIN_MINING_UNIT,
         MekanismModules.FARMING_UNIT,
         MekanismModules.SHEARING_UNIT,
         MekanismModules.TELEPORTATION_UNIT,
         MekanismModules.EXCAVATION_ESCALATION_UNIT
      );
      MekanismIMC.addMekaSuitHelmetModules(
         MekanismModules.ELECTROLYTIC_BREATHING_UNIT,
         MekanismModules.INHALATION_PURIFICATION_UNIT,
         MekanismModules.VISION_ENHANCEMENT_UNIT,
         MekanismModules.NUTRITIONAL_INJECTION_UNIT
      );
      MekanismIMC.addMekaSuitBodyarmorModules(
         MekanismModules.JETPACK_UNIT,
         MekanismModules.GRAVITATIONAL_MODULATING_UNIT,
         MekanismModules.CHARGE_DISTRIBUTION_UNIT,
         MekanismModules.DOSIMETER_UNIT,
         MekanismModules.GEIGER_UNIT,
         MekanismModules.ELYTRA_UNIT
      );
      MekanismIMC.addMekaSuitPantsModules(
         MekanismModules.LOCOMOTIVE_BOOSTING_UNIT,
         MekanismModules.GYROSCOPIC_STABILIZATION_UNIT,
         MekanismModules.HYDROSTATIC_REPULSOR_UNIT,
         MekanismModules.MOTORIZED_SERVO_UNIT
      );
      MekanismIMC.addMekaSuitBootsModules(
         MekanismModules.HYDRAULIC_PROPULSION_UNIT, MekanismModules.MAGNETIC_ATTRACTION_UNIT, MekanismModules.FROST_WALKER_UNIT
      );
   }

   private void imcHandle(InterModProcessEvent event) {
      ModuleHelper.get().processIMC(event);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      logger.info("Version {} initializing...", this.versionNumber);
      hooks.hookCommonSetup();
      this.setRecipeCacheManager(new ReloadListener());
      event.enqueueWork(
         () -> {
            MekanismTags.init();
            MekAnnotationScanner.collectScanData();
            MekanismCriteriaTriggers.init();
            ForgeChunkManager.setForcedChunkLoadingCallback("mekanism", TileComponentChunkLoader.ChunkValidationCallback.INSTANCE);
            MekanismFluids.FLUIDS.registerBucketDispenserBehavior();
            registerFluidTankBehaviors(
               MekanismBlocks.BASIC_FLUID_TANK,
               MekanismBlocks.ADVANCED_FLUID_TANK,
               MekanismBlocks.ELITE_FLUID_TANK,
               MekanismBlocks.ULTIMATE_FLUID_TANK,
               MekanismBlocks.CREATIVE_FLUID_TANK
            );
            registerDispenseBehavior(new ModuleDispenseBehavior(), MekanismItems.MEKA_TOOL);
            registerDispenseBehavior(
               new MekaSuitDispenseBehavior(),
               MekanismItems.MEKASUIT_HELMET,
               MekanismItems.MEKASUIT_BODYARMOR,
               MekanismItems.MEKASUIT_PANTS,
               MekanismItems.MEKASUIT_BOOTS
            );
            ItemPredicate.register(FullCanteenItemPredicate.ID, json -> FullCanteenItemPredicate.INSTANCE);
            ItemPredicate.register(MaxedModuleContainerItemPredicate.ID, MaxedModuleContainerItemPredicate::fromJson);
            MekanismGameEvents.addFrequencies();
         }
      );
      MinecraftForge.EVENT_BUS.register(new CommonPlayerTracker());
      MinecraftForge.EVENT_BUS.register(new CommonPlayerTickHandler());
      MinecraftForge.EVENT_BUS.register(worldTickHandler);
      MinecraftForge.EVENT_BUS.register(RadiationManager.get());
      TransmitterNetworkRegistry.initiate();
      this.packetHandler.initialize();
      logger.info("Fake player readout: UUID = {}, name = {}", gameProfile.getId(), gameProfile.getName());
      logger.info("Mod loaded.");
   }

   private static void registerDispenseBehavior(DispenseItemBehavior behavior, IItemProvider... itemProviders) {
      for (IItemProvider itemProvider : itemProviders) {
         DispenserBlock.m_52672_(itemProvider.m_5456_(), behavior);
      }
   }

   private static void registerFluidTankBehaviors(IItemProvider... itemProviders) {
      registerDispenseBehavior(ItemBlockFluidTank.FluidTankItemDispenseBehavior.INSTANCE);

      for (IItemProvider itemProvider : itemProviders) {
         Item item = itemProvider.m_5456_();
         CauldronInteraction.f_175606_.put(item, ItemBlockFluidTank.BasicCauldronInteraction.EMPTY);
         CauldronInteraction.f_175607_.put(item, ItemBlockFluidTank.BasicDrainCauldronInteraction.WATER);
         CauldronInteraction.f_175608_.put(item, ItemBlockFluidTank.BasicDrainCauldronInteraction.LAVA);
      }
   }

   private void onEnergyTransferred(EnergyNetwork.EnergyTransferEvent event) {
      this.packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network), event.network);
   }

   private void onChemicalTransferred(BoxedChemicalNetwork.ChemicalTransferEvent event) {
      this.packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.transferType), event.network);
   }

   private void onLiquidTransferred(FluidNetwork.FluidTransferEvent event) {
      this.packetHandler.sendToReceivers(new PacketTransmitterUpdate(event.network, event.fluidType), event.network);
   }

   private void onConfigLoad(ModConfigEvent configEvent) {
      ModConfig config = configEvent.getConfig();
      if (config.getModId().equals("mekanism") && config instanceof MekanismModConfig mekConfig) {
         mekConfig.clearCache(configEvent);
      }
   }

   private void onWorldLoad(Load event) {
      playerState.init(event.getLevel());
   }

   private void onWorldUnload(Unload event) {
      if (event.getLevel() instanceof ServerLevel level) {
         MekFakePlayer.releaseInstance(level);
      }

      if (event.getLevel() instanceof Level level && MekanismConfig.general.validOredictionificatorFilters.hasInvalidationListeners()) {
         MekanismConfig.general
            .validOredictionificatorFilters
            .removeInvalidationListenersMatching(
               listener -> listener instanceof TileEntityOredictionificator.ODConfigValueInvalidationListener odListener && odListener.isIn(level)
            );
      }
   }
}
