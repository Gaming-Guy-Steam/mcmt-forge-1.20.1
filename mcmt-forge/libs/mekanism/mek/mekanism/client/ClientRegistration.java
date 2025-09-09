package mekanism.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table.Cell;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.client.gui.GuiBoilerStats;
import mekanism.client.gui.GuiChemicalTank;
import mekanism.client.gui.GuiDimensionalStabilizer;
import mekanism.client.gui.GuiDynamicTank;
import mekanism.client.gui.GuiEnergyCube;
import mekanism.client.gui.GuiFluidTank;
import mekanism.client.gui.GuiInductionMatrix;
import mekanism.client.gui.GuiLaserAmplifier;
import mekanism.client.gui.GuiLaserTractorBeam;
import mekanism.client.gui.GuiLogisticalSorter;
import mekanism.client.gui.GuiMatrixStats;
import mekanism.client.gui.GuiModificationStation;
import mekanism.client.gui.GuiModuleTweaker;
import mekanism.client.gui.GuiPersonalStorageTile;
import mekanism.client.gui.GuiQuantumEntangloporter;
import mekanism.client.gui.GuiSPS;
import mekanism.client.gui.GuiSecurityDesk;
import mekanism.client.gui.GuiTeleporter;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.GuiThermoelectricBoiler;
import mekanism.client.gui.item.GuiDictionary;
import mekanism.client.gui.item.GuiPersonalStorageItem;
import mekanism.client.gui.item.GuiPortableTeleporter;
import mekanism.client.gui.item.GuiSeismicReader;
import mekanism.client.gui.machine.GuiAntiprotonicNucleosynthesizer;
import mekanism.client.gui.machine.GuiChemicalCrystallizer;
import mekanism.client.gui.machine.GuiChemicalDissolutionChamber;
import mekanism.client.gui.machine.GuiChemicalInfuser;
import mekanism.client.gui.machine.GuiChemicalOxidizer;
import mekanism.client.gui.machine.GuiChemicalWasher;
import mekanism.client.gui.machine.GuiCombiner;
import mekanism.client.gui.machine.GuiDigitalMiner;
import mekanism.client.gui.machine.GuiDigitalMinerConfig;
import mekanism.client.gui.machine.GuiElectricPump;
import mekanism.client.gui.machine.GuiElectrolyticSeparator;
import mekanism.client.gui.machine.GuiFactory;
import mekanism.client.gui.machine.GuiFluidicPlenisher;
import mekanism.client.gui.machine.GuiFormulaicAssemblicator;
import mekanism.client.gui.machine.GuiFuelwoodHeater;
import mekanism.client.gui.machine.GuiIsotopicCentrifuge;
import mekanism.client.gui.machine.GuiMetallurgicInfuser;
import mekanism.client.gui.machine.GuiNutritionalLiquifier;
import mekanism.client.gui.machine.GuiOredictionificator;
import mekanism.client.gui.machine.GuiPRC;
import mekanism.client.gui.machine.GuiPaintingMachine;
import mekanism.client.gui.machine.GuiPigmentExtractor;
import mekanism.client.gui.machine.GuiPigmentMixer;
import mekanism.client.gui.machine.GuiPrecisionSawmill;
import mekanism.client.gui.machine.GuiResistiveHeater;
import mekanism.client.gui.machine.GuiRotaryCondensentrator;
import mekanism.client.gui.machine.GuiSeismicVibrator;
import mekanism.client.gui.machine.GuiSolarNeutronActivator;
import mekanism.client.gui.qio.GuiPortableQIODashboard;
import mekanism.client.gui.qio.GuiQIODashboard;
import mekanism.client.gui.qio.GuiQIODriveArray;
import mekanism.client.gui.qio.GuiQIOExporter;
import mekanism.client.gui.qio.GuiQIOImporter;
import mekanism.client.gui.qio.GuiQIOItemFrequencySelect;
import mekanism.client.gui.qio.GuiQIORedstoneAdapter;
import mekanism.client.gui.qio.GuiQIOTileFrequencySelect;
import mekanism.client.gui.robit.GuiRobitCrafting;
import mekanism.client.gui.robit.GuiRobitInventory;
import mekanism.client.gui.robit.GuiRobitMain;
import mekanism.client.gui.robit.GuiRobitRepair;
import mekanism.client.gui.robit.GuiRobitSmelting;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.model.ModelArmoredFreeRunners;
import mekanism.client.model.ModelArmoredJetpack;
import mekanism.client.model.ModelAtomicDisassembler;
import mekanism.client.model.ModelEnergyCore;
import mekanism.client.model.ModelFlamethrower;
import mekanism.client.model.ModelFreeRunners;
import mekanism.client.model.ModelIndustrialAlarm;
import mekanism.client.model.ModelJetpack;
import mekanism.client.model.ModelScubaMask;
import mekanism.client.model.ModelScubaTank;
import mekanism.client.model.ModelTransporterBox;
import mekanism.client.model.baked.DigitalMinerBakedModel;
import mekanism.client.model.baked.DriveArrayBakedModel;
import mekanism.client.model.baked.ExtensionBakedModel;
import mekanism.client.model.energycube.EnergyCubeModelLoader;
import mekanism.client.model.robit.RobitModel;
import mekanism.client.particle.JetpackFlameParticle;
import mekanism.client.particle.JetpackSmokeParticle;
import mekanism.client.particle.LaserParticle;
import mekanism.client.particle.RadiationParticle;
import mekanism.client.particle.ScubaBubbleParticle;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.RenderTickHandler;
import mekanism.client.render.armor.FreeRunnerArmor;
import mekanism.client.render.armor.JetpackArmor;
import mekanism.client.render.armor.ScubaMaskArmor;
import mekanism.client.render.armor.ScubaTankArmor;
import mekanism.client.render.entity.RenderFlame;
import mekanism.client.render.entity.RenderRobit;
import mekanism.client.render.hud.MekaSuitEnergyLevel;
import mekanism.client.render.hud.MekanismHUD;
import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.item.MekaSuitBarDecorator;
import mekanism.client.render.item.TransmitterTypeDecorator;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.gear.RenderAtomicDisassembler;
import mekanism.client.render.item.gear.RenderFlameThrower;
import mekanism.client.render.item.gear.RenderFreeRunners;
import mekanism.client.render.item.gear.RenderJetpack;
import mekanism.client.render.item.gear.RenderScubaMask;
import mekanism.client.render.item.gear.RenderScubaTank;
import mekanism.client.render.layer.MekanismArmorLayer;
import mekanism.client.render.layer.MekanismElytraLayer;
import mekanism.client.render.obj.TransmitterLoader;
import mekanism.client.render.tileentity.RenderBin;
import mekanism.client.render.tileentity.RenderDigitalMiner;
import mekanism.client.render.tileentity.RenderDimensionalStabilizer;
import mekanism.client.render.tileentity.RenderDynamicTank;
import mekanism.client.render.tileentity.RenderEnergyCube;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderIndustrialAlarm;
import mekanism.client.render.tileentity.RenderNutritionalLiquifier;
import mekanism.client.render.tileentity.RenderPersonalChest;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderSPS;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.tileentity.RenderThermalEvaporationPlant;
import mekanism.client.render.tileentity.RenderThermoelectricBoiler;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderPressurizedTube;
import mekanism.client.render.transmitter.RenderThermodynamicConductor;
import mekanism.client.render.transmitter.RenderUniversalCable;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.content.gear.shared.ModuleColorModulationUnit;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.item.gear.ItemHDPEElytra;
import mekanism.common.lib.Color;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismEntityTypes;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismModules;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporter;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.ModifyBakingResult;
import net.minecraftforge.client.event.ModelEvent.RegisterAdditional;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.client.event.RegisterColorHandlersEvent.Block;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.SeparateTransformsModel.Baked;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "mekanism",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public class ClientRegistration {
   private static final FieldReflectionHelper<Baked, BakedModel> SEPARATE_PERSPECTIVE_BASE_MODEL = new FieldReflectionHelper<>(
      Baked.class, "baseModel", () -> null
   );
   private static final FieldReflectionHelper<Baked, ImmutableMap<ItemDisplayContext, BakedModel>> SEPARATE_PERSPECTIVE_PERSPECTIVES = new FieldReflectionHelper<>(
      Baked.class, "perspectives", ImmutableMap::of
   );
   private static final Map<ResourceLocation, ClientRegistration.CustomModelRegistryObject> customModels = new ConcurrentHashMap<>();

   @SubscribeEvent
   public static void init(FMLClientSetupEvent event) {
      MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
      MinecraftForge.EVENT_BUS.register(new RenderTickHandler());
      MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, SoundHandler::onTilePlaySound);
      if (ModList.get().isLoaded("jei")) {
         MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, RenderTickHandler::guiOpening);
      }

      HolidayManager.init();
      IModuleHelper moduleHelper = IModuleHelper.INSTANCE;
      moduleHelper.addMekaSuitModuleModels(Mekanism.rl("models/entity/mekasuit_modules.obj"));
      moduleHelper.addMekaSuitModuleModelSpec("jetpack", MekanismModules.JETPACK_UNIT, EquipmentSlot.CHEST);
      moduleHelper.addMekaSuitModuleModelSpec("modulator", MekanismModules.GRAVITATIONAL_MODULATING_UNIT, EquipmentSlot.CHEST);
      moduleHelper.addMekaSuitModuleModelSpec("elytra", MekanismModules.ELYTRA_UNIT, EquipmentSlot.CHEST, LivingEntity::m_21255_);
      event.enqueueWork(
         () -> {
            for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : MekanismFluids.FLUIDS.getAllFluids()) {
               ClientRegistrationUtil.setRenderLayer(RenderType.m_110466_(), fluidRO);
            }

            ClientRegistrationUtil.setPropertyOverride(
               MekanismBlocks.CARDBOARD_BOX,
               Mekanism.rl("storage"),
               (stack, world, entity, seed) -> ((ItemBlockCardboardBox)stack.m_41720_()).getBlockData(world, stack) == null ? 0.0F : 1.0F
            );
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("invalid"), (stack, world, entity, seed) -> {
               ItemCraftingFormula formula = (ItemCraftingFormula)stack.m_41720_();
               return formula.hasInventory(stack) && formula.isInvalid(stack) ? 1.0F : 0.0F;
            });
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.CRAFTING_FORMULA, Mekanism.rl("encoded"), (stack, world, entity, seed) -> {
               ItemCraftingFormula formula = (ItemCraftingFormula)stack.m_41720_();
               return formula.hasInventory(stack) && !formula.isInvalid(stack) ? 1.0F : 0.0F;
            });
            ClientRegistrationUtil.setPropertyOverride(
               MekanismItems.CONFIGURATION_CARD,
               Mekanism.rl("encoded"),
               (stack, world, entity, seed) -> ((ItemConfigurationCard)stack.m_41720_()).hasData(stack) ? 1.0F : 0.0F
            );
            ClientRegistrationUtil.setPropertyOverride(
               MekanismItems.ELECTRIC_BOW,
               Mekanism.rl("pull"),
               (stack, world, entity, seed) -> entity != null && entity.m_21211_() == stack ? (stack.m_41779_() - entity.m_21212_()) / 20.0F : 0.0F
            );
            ClientRegistrationUtil.setPropertyOverride(
               MekanismItems.ELECTRIC_BOW,
               Mekanism.rl("pulling"),
               (stack, world, entity, seed) -> entity != null && entity.m_6117_() && entity.m_21211_() == stack ? 1.0F : 0.0F
            );
            ClientRegistrationUtil.setPropertyOverride(
               MekanismItems.GEIGER_COUNTER,
               Mekanism.rl("radiation"),
               (stack, world, entity, seed) -> entity instanceof Player ? RadiationManager.get().getClientScale().ordinal() : 0.0F
            );
            ClientRegistrationUtil.setPropertyOverride(MekanismItems.HDPE_REINFORCED_ELYTRA, Mekanism.rl("broken"), (stack, world, entity, seed) -> {
               boolean canFly;
               if (entity == null) {
                  canFly = stack.m_41773_() < stack.m_41776_() - 1;
               } else {
                  canFly = ((ItemHDPEElytra)MekanismItems.HDPE_REINFORCED_ELYTRA.get()).canElytraFly(stack, entity);
               }

               return canFly ? 0.0F : 1.0F;
            });
         }
      );
      addCustomModel(MekanismBlocks.QIO_DRIVE_ARRAY, (orig, evt) -> new DriveArrayBakedModel(orig));
      addCustomModel(MekanismBlocks.DIGITAL_MINER, (orig, evt) -> new DigitalMinerBakedModel(orig));
      addLitModel(MekanismItems.MEKA_TOOL);
   }

   @SubscribeEvent
   public static void registerKeybindings(RegisterKeyMappingsEvent event) {
      MekanismKeyHandler.registerKeybindings(event);
   }

   @SubscribeEvent
   public static void registerOverlays(RegisterGuiOverlaysEvent event) {
      event.registerBelowAll("radiation_overlay", RadiationOverlay.INSTANCE);
      event.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "energy_level", MekaSuitEnergyLevel.INSTANCE);
      event.registerAbove(VanillaGuiOverlay.ITEM_NAME.id(), "status_overlay", MekanismStatusOverlay.INSTANCE);
      event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "hud", MekanismHUD.INSTANCE);
   }

   @SubscribeEvent
   public static void registerRenderers(RegisterRenderers event) {
      event.registerEntityRenderer((EntityType)MekanismEntityTypes.ROBIT.get(), RenderRobit::new);
      event.registerEntityRenderer((EntityType)MekanismEntityTypes.FLAME.get(), RenderFlame::new);
      ClientRegistrationUtil.bindTileEntityRenderer(
         event, RenderThermoelectricBoiler::new, MekanismTileEntityTypes.BOILER_CASING, MekanismTileEntityTypes.BOILER_VALVE
      );
      ClientRegistrationUtil.bindTileEntityRenderer(event, RenderDynamicTank::new, MekanismTileEntityTypes.DYNAMIC_TANK, MekanismTileEntityTypes.DYNAMIC_VALVE);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.DIGITAL_MINER.get(), RenderDigitalMiner::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.DIMENSIONAL_STABILIZER.get(), RenderDimensionalStabilizer::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.PERSONAL_CHEST.get(), RenderPersonalChest::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.NUTRITIONAL_LIQUIFIER.get(), RenderNutritionalLiquifier::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.PIGMENT_MIXER.get(), RenderPigmentMixer::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.SEISMIC_VIBRATOR.get(), RenderSeismicVibrator::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.TELEPORTER.get(), RenderTeleporter::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.THERMAL_EVAPORATION_CONTROLLER.get(), RenderThermalEvaporationPlant::new);
      event.registerBlockEntityRenderer(MekanismTileEntityTypes.INDUSTRIAL_ALARM.get(), RenderIndustrialAlarm::new);
      ClientRegistrationUtil.bindTileEntityRenderer(event, RenderSPS::new, MekanismTileEntityTypes.SPS_CASING, MekanismTileEntityTypes.SPS_PORT);
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderBin::new,
         MekanismTileEntityTypes.BASIC_BIN,
         MekanismTileEntityTypes.ADVANCED_BIN,
         MekanismTileEntityTypes.ELITE_BIN,
         MekanismTileEntityTypes.ULTIMATE_BIN,
         MekanismTileEntityTypes.CREATIVE_BIN
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderEnergyCube::new,
         MekanismTileEntityTypes.BASIC_ENERGY_CUBE,
         MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE,
         MekanismTileEntityTypes.ELITE_ENERGY_CUBE,
         MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE,
         MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderFluidTank::new,
         MekanismTileEntityTypes.BASIC_FLUID_TANK,
         MekanismTileEntityTypes.ADVANCED_FLUID_TANK,
         MekanismTileEntityTypes.ELITE_FLUID_TANK,
         MekanismTileEntityTypes.ULTIMATE_FLUID_TANK,
         MekanismTileEntityTypes.CREATIVE_FLUID_TANK
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderLogisticalTransporter::new,
         MekanismTileEntityTypes.RESTRICTIVE_TRANSPORTER,
         MekanismTileEntityTypes.DIVERSION_TRANSPORTER,
         MekanismTileEntityTypes.BASIC_LOGISTICAL_TRANSPORTER,
         MekanismTileEntityTypes.ADVANCED_LOGISTICAL_TRANSPORTER,
         MekanismTileEntityTypes.ELITE_LOGISTICAL_TRANSPORTER,
         MekanismTileEntityTypes.ULTIMATE_LOGISTICAL_TRANSPORTER
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderMechanicalPipe::new,
         MekanismTileEntityTypes.BASIC_MECHANICAL_PIPE,
         MekanismTileEntityTypes.ADVANCED_MECHANICAL_PIPE,
         MekanismTileEntityTypes.ELITE_MECHANICAL_PIPE,
         MekanismTileEntityTypes.ULTIMATE_MECHANICAL_PIPE
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderPressurizedTube::new,
         MekanismTileEntityTypes.BASIC_PRESSURIZED_TUBE,
         MekanismTileEntityTypes.ADVANCED_PRESSURIZED_TUBE,
         MekanismTileEntityTypes.ELITE_PRESSURIZED_TUBE,
         MekanismTileEntityTypes.ULTIMATE_PRESSURIZED_TUBE
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderUniversalCable::new,
         MekanismTileEntityTypes.BASIC_UNIVERSAL_CABLE,
         MekanismTileEntityTypes.ADVANCED_UNIVERSAL_CABLE,
         MekanismTileEntityTypes.ELITE_UNIVERSAL_CABLE,
         MekanismTileEntityTypes.ULTIMATE_UNIVERSAL_CABLE
      );
      ClientRegistrationUtil.bindTileEntityRenderer(
         event,
         RenderThermodynamicConductor::new,
         MekanismTileEntityTypes.BASIC_THERMODYNAMIC_CONDUCTOR,
         MekanismTileEntityTypes.ADVANCED_THERMODYNAMIC_CONDUCTOR,
         MekanismTileEntityTypes.ELITE_THERMODYNAMIC_CONDUCTOR,
         MekanismTileEntityTypes.ULTIMATE_THERMODYNAMIC_CONDUCTOR
      );
   }

   @SubscribeEvent
   public static void registerLayer(RegisterLayerDefinitions event) {
      event.registerLayerDefinition(ModelJetpack.JETPACK_LAYER, ModelJetpack::createLayerDefinition);
      event.registerLayerDefinition(ModelArmoredJetpack.ARMORED_JETPACK_LAYER, ModelArmoredJetpack::createLayerDefinition);
      event.registerLayerDefinition(ModelAtomicDisassembler.DISASSEMBLER_LAYER, ModelAtomicDisassembler::createLayerDefinition);
      event.registerLayerDefinition(ModelEnergyCore.CORE_LAYER, ModelEnergyCore::createLayerDefinition);
      event.registerLayerDefinition(ModelFlamethrower.FLAMETHROWER_LAYER, ModelFlamethrower::createLayerDefinition);
      event.registerLayerDefinition(ModelArmoredFreeRunners.ARMORED_FREE_RUNNER_LAYER, ModelArmoredFreeRunners::createLayerDefinition);
      event.registerLayerDefinition(ModelFreeRunners.FREE_RUNNER_LAYER, ModelFreeRunners::createLayerDefinition);
      event.registerLayerDefinition(ModelIndustrialAlarm.ALARM_LAYER, ModelIndustrialAlarm::createLayerDefinition);
      event.registerLayerDefinition(ModelScubaMask.MASK_LAYER, ModelScubaMask::createLayerDefinition);
      event.registerLayerDefinition(ModelScubaTank.TANK_LAYER, ModelScubaTank::createLayerDefinition);
      event.registerLayerDefinition(ModelTransporterBox.BOX_LAYER, ModelTransporterBox::createLayerDefinition);
   }

   @SubscribeEvent
   public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
      event.registerReloadListener(new RobitSpriteUploader(Minecraft.m_91087_().m_91097_()));
      ClientRegistrationUtil.registerClientReloadListeners(
         event,
         RenderEnergyCubeItem.RENDERER,
         RenderJetpack.ARMORED_RENDERER,
         RenderAtomicDisassembler.RENDERER,
         RenderFlameThrower.RENDERER,
         RenderFreeRunners.RENDERER,
         RenderFreeRunners.ARMORED_RENDERER,
         RenderJetpack.RENDERER,
         RenderScubaMask.RENDERER,
         RenderScubaTank.RENDERER,
         JetpackArmor.ARMORED_JETPACK,
         JetpackArmor.JETPACK,
         FreeRunnerArmor.ARMORED_FREE_RUNNERS,
         FreeRunnerArmor.FREE_RUNNERS,
         ScubaMaskArmor.SCUBA_MASK,
         ScubaTankArmor.SCUBA_TANK
      );
   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public static void registerContainers(RegisterEvent event) {
      event.register(Registries.f_256798_, helper -> {
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MODULE_TWEAKER, GuiModuleTweaker::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DICTIONARY, GuiDictionary::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PORTABLE_TELEPORTER, GuiPortableTeleporter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_READER, GuiSeismicReader::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM, GuiQIOItemFrequencySelect::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PORTABLE_QIO_DASHBOARD, GuiPortableQIODashboard::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MAIN_ROBIT, GuiRobitMain::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INVENTORY_ROBIT, GuiRobitInventory::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SMELTING_ROBIT, GuiRobitSmelting::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CRAFTING_ROBIT, GuiRobitCrafting::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.REPAIR_ROBIT, GuiRobitRepair::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_CRYSTALLIZER, GuiChemicalCrystallizer::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_DISSOLUTION_CHAMBER, GuiChemicalDissolutionChamber::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_INFUSER, GuiChemicalInfuser::new);
         ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.CHEMICAL_INJECTION_CHAMBER);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_OXIDIZER, GuiChemicalOxidizer::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_WASHER, GuiChemicalWasher::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.COMBINER, GuiCombiner::new);
         ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.CRUSHER);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER, GuiDigitalMiner::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DYNAMIC_TANK, GuiDynamicTank::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTRIC_PUMP, GuiElectricPump::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ELECTROLYTIC_SEPARATOR, GuiElectrolyticSeparator::new);
         ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.ENERGIZED_SMELTER);
         ClientRegistrationUtil.registerElectricScreen(MekanismContainerTypes.ENRICHMENT_CHAMBER);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUIDIC_PLENISHER, GuiFluidicPlenisher::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FORMULAIC_ASSEMBLICATOR, GuiFormulaicAssemblicator::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FUELWOOD_HEATER, GuiFuelwoodHeater::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_AMPLIFIER, GuiLaserAmplifier::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LASER_TRACTOR_BEAM, GuiLaserTractorBeam::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.METALLURGIC_INFUSER, GuiMetallurgicInfuser::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.OREDICTIONIFICATOR, GuiOredictionificator::new);
         ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.OSMIUM_COMPRESSOR);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRECISION_SAWMILL, GuiPrecisionSawmill::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PRESSURIZED_REACTION_CHAMBER, GuiPRC::new);
         ClientRegistrationUtil.registerAdvancedElectricScreen(MekanismContainerTypes.PURIFICATION_CHAMBER);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QUANTUM_ENTANGLOPORTER, GuiQuantumEntangloporter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.RESISTIVE_HEATER, GuiResistiveHeater::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ROTARY_CONDENSENTRATOR, GuiRotaryCondensentrator::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SECURITY_DESK, GuiSecurityDesk::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MODIFICATION_STATION, GuiModificationStation::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ISOTOPIC_CENTRIFUGE, GuiIsotopicCentrifuge::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.NUTRITIONAL_LIQUIFIER, GuiNutritionalLiquifier::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, GuiAntiprotonicNucleosynthesizer::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PIGMENT_EXTRACTOR, GuiPigmentExtractor::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PIGMENT_MIXER, GuiPigmentMixer::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PAINTING_MACHINE, GuiPaintingMachine::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SEISMIC_VIBRATOR, GuiSeismicVibrator::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SOLAR_NEUTRON_ACTIVATOR, GuiSolarNeutronActivator::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.TELEPORTER, GuiTeleporter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMAL_EVAPORATION_CONTROLLER, GuiThermalEvaporationController::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_DRIVE_ARRAY, GuiQIODriveArray::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_DASHBOARD, GuiQIODashboard::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_IMPORTER, GuiQIOImporter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_EXPORTER, GuiQIOExporter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_REDSTONE_ADAPTER, GuiQIORedstoneAdapter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.SPS, GuiSPS::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIMENSIONAL_STABILIZER, GuiDimensionalStabilizer::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FACTORY, GuiFactory::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.CHEMICAL_TANK, GuiChemicalTank::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.FLUID_TANK, GuiFluidTank::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.ENERGY_CUBE, GuiEnergyCube::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.INDUCTION_MATRIX, GuiInductionMatrix::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.THERMOELECTRIC_BOILER, GuiThermoelectricBoiler::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_STORAGE_ITEM, GuiPersonalStorageItem::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.PERSONAL_STORAGE_BLOCK, GuiPersonalStorageTile::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.DIGITAL_MINER_CONFIG, GuiDigitalMinerConfig::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.LOGISTICAL_SORTER, GuiLogisticalSorter::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE, GuiQIOTileFrequencySelect::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.BOILER_STATS, GuiBoilerStats::new);
         ClientRegistrationUtil.registerScreen(MekanismContainerTypes.MATRIX_STATS, GuiMatrixStats::new);
      });
   }

   @SubscribeEvent
   public static void registerModelLoaders(RegisterGeometryLoaders event) {
      event.register("robit", RobitModel.Loader.INSTANCE);
      event.register("energy_cube", EnergyCubeModelLoader.INSTANCE);
      event.register("transmitter", TransmitterLoader.INSTANCE);
   }

   @SubscribeEvent
   public static void registerAdditionalModels(RegisterAdditional event) {
      MekanismModelCache.INSTANCE.setup(event);
   }

   @SubscribeEvent
   public static void onModelBake(ModifyBakingResult event) {
      event.getModels().replaceAll((rl, model) -> {
         ClientRegistration.CustomModelRegistryObject obj = customModels.get(new ResourceLocation(rl.m_135827_(), rl.m_135815_()));
         return (BakedModel)(obj == null ? model : obj.createModel(model, event));
      });
   }

   @SubscribeEvent
   public static void onModelBake(BakingCompleted event) {
      MekanismModelCache.INSTANCE.onBake(event);
   }

   @SubscribeEvent
   public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
      event.registerSpriteSet((ParticleType)MekanismParticleTypes.LASER.get(), LaserParticle.Factory::new);
      event.registerSpriteSet((ParticleType)MekanismParticleTypes.JETPACK_FLAME.get(), JetpackFlameParticle.Factory::new);
      event.registerSpriteSet((ParticleType)MekanismParticleTypes.JETPACK_SMOKE.get(), JetpackSmokeParticle.Factory::new);
      event.registerSpriteSet((ParticleType)MekanismParticleTypes.SCUBA_BUBBLE.get(), ScubaBubbleParticle.Factory::new);
      event.registerSpriteSet((ParticleType)MekanismParticleTypes.RADIATION.get(), RadiationParticle.Factory::new);
   }

   @SubscribeEvent
   public static void registerBlockColorHandlers(Block event) {
      ClientRegistrationUtil.registerBlockColorHandler(
         event,
         (state, world, pos, tintIndex) -> {
            if (tintIndex == 1) {
               BaseTier tier = Attribute.getBaseTier(state.m_60734_());
               if (tier != null) {
                  return MekanismRenderer.getColorARGB(tier, 1.0F);
               }
            }

            return -1;
         },
         MekanismBlocks.BASIC_FLUID_TANK,
         MekanismBlocks.ADVANCED_FLUID_TANK,
         MekanismBlocks.ELITE_FLUID_TANK,
         MekanismBlocks.ULTIMATE_FLUID_TANK,
         MekanismBlocks.CREATIVE_FLUID_TANK
      );
      ClientRegistrationUtil.registerBlockColorHandler(
         event,
         (state, world, pos, tintIndex) -> {
            if (pos != null) {
               TileEntityQIOComponent tile = WorldUtils.getTileEntity(TileEntityQIOComponent.class, world, pos);
               if (tile != null) {
                  EnumColor color = tile.getColor();
                  return color == null ? -1 : MekanismRenderer.getColorARGB(color, 1.0F);
               }
            }

            return -1;
         },
         MekanismBlocks.QIO_DRIVE_ARRAY,
         MekanismBlocks.QIO_DASHBOARD,
         MekanismBlocks.QIO_IMPORTER,
         MekanismBlocks.QIO_EXPORTER,
         MekanismBlocks.QIO_REDSTONE_ADAPTER
      );
      ClientRegistrationUtil.registerBlockColorHandler(
         event,
         (state, world, pos, tintIndex) -> {
            if (tintIndex == 1 && pos != null) {
               TileEntityLogisticalTransporter transporter = WorldUtils.getTileEntity(TileEntityLogisticalTransporter.class, world, pos);
               if (transporter != null) {
                  EnumColor renderColor = transporter.getTransmitter().getColor();
                  if (renderColor != null) {
                     return MekanismRenderer.getColorARGB(renderColor, 1.0F);
                  }
               }
            }

            return -1;
         },
         MekanismBlocks.BASIC_LOGISTICAL_TRANSPORTER,
         MekanismBlocks.ADVANCED_LOGISTICAL_TRANSPORTER,
         MekanismBlocks.ELITE_LOGISTICAL_TRANSPORTER,
         MekanismBlocks.ULTIMATE_LOGISTICAL_TRANSPORTER
      );

      for (Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
         if (entry.getKey() instanceof PrimaryResource primaryResource) {
            int tint = primaryResource.getTint();
            ClientRegistrationUtil.registerBlockColorHandler(event, (state, world, pos, index) -> index == 1 ? tint : -1, entry.getValue());
         }
      }
   }

   @SubscribeEvent
   public static void registerItemColorHandlers(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
      ClientRegistrationUtil.registerItemColorHandler(
         event,
         (stack, tintIndex) -> tintIndex == 1 && stack.m_41720_() instanceof ItemBlockFluidTank tank
            ? MekanismRenderer.getColorARGB(tank.getTier().getBaseTier(), 1.0F)
            : -1,
         MekanismBlocks.BASIC_FLUID_TANK,
         MekanismBlocks.ADVANCED_FLUID_TANK,
         MekanismBlocks.ELITE_FLUID_TANK,
         MekanismBlocks.ULTIMATE_FLUID_TANK,
         MekanismBlocks.CREATIVE_FLUID_TANK
      );
      ClientRegistrationUtil.registerBucketColorHandler(event, MekanismFluids.FLUIDS);

      for (Cell<ResourceType, PrimaryResource, ItemRegistryObject<Item>> item : MekanismItems.PROCESSED_RESOURCES.cellSet()) {
         int tint = ((PrimaryResource)item.getColumnKey()).getTint();
         ClientRegistrationUtil.registerItemColorHandler(event, (stack, index) -> index == 1 ? tint : -1, (IItemProvider)item.getValue());
      }

      ClientRegistrationUtil.registerIColoredItemHandler(
         event,
         MekanismItems.PORTABLE_QIO_DASHBOARD,
         MekanismBlocks.QIO_DRIVE_ARRAY,
         MekanismBlocks.QIO_DASHBOARD,
         MekanismBlocks.QIO_IMPORTER,
         MekanismBlocks.QIO_EXPORTER,
         MekanismBlocks.QIO_REDSTONE_ADAPTER
      );
      ClientRegistrationUtil.registerItemColorHandler(
         event,
         (stack, index) -> {
            if (index == 1) {
               IModule<ModuleColorModulationUnit> colorModulation = IModuleHelper.INSTANCE.load(stack, MekanismModules.COLOR_MODULATION_UNIT);
               if (colorModulation != null) {
                  Color color = colorModulation.getCustomInstance().getColor();
                  color = Color.rgbd(
                     color.ad() * color.rd() + (1.0 - color.ad()), color.ad() * color.gd() + (1.0 - color.ad()), color.ad() * color.bd() + (1.0 - color.ad())
                  );
                  return color.argb();
               }
            }

            return -1;
         },
         MekanismItems.MEKASUIT_HELMET,
         MekanismItems.MEKASUIT_BODYARMOR,
         MekanismItems.MEKASUIT_PANTS,
         MekanismItems.MEKASUIT_BOOTS
      );

      for (Entry<IResource, BlockRegistryObject<?, ?>> entry : MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.entrySet()) {
         if (entry.getKey() instanceof PrimaryResource primaryResource) {
            int tint = primaryResource.getTint();
            ClientRegistrationUtil.registerItemColorHandler(event, (stack, index) -> index == 1 ? tint : -1, entry.getValue());
         }
      }
   }

   @SubscribeEvent
   public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
      event.register(MekanismItems.MEKASUIT_HELMET, MekaSuitBarDecorator.INSTANCE);
      event.register(MekanismItems.MEKASUIT_BODYARMOR, MekaSuitBarDecorator.INSTANCE);
      TransmitterTypeDecorator.registerDecorators(
         event,
         MekanismBlocks.BASIC_PRESSURIZED_TUBE,
         MekanismBlocks.ADVANCED_PRESSURIZED_TUBE,
         MekanismBlocks.ELITE_PRESSURIZED_TUBE,
         MekanismBlocks.ULTIMATE_PRESSURIZED_TUBE,
         MekanismBlocks.BASIC_THERMODYNAMIC_CONDUCTOR,
         MekanismBlocks.ADVANCED_THERMODYNAMIC_CONDUCTOR,
         MekanismBlocks.ELITE_THERMODYNAMIC_CONDUCTOR,
         MekanismBlocks.ULTIMATE_THERMODYNAMIC_CONDUCTOR,
         MekanismBlocks.BASIC_UNIVERSAL_CABLE,
         MekanismBlocks.ADVANCED_UNIVERSAL_CABLE,
         MekanismBlocks.ELITE_UNIVERSAL_CABLE,
         MekanismBlocks.ULTIMATE_UNIVERSAL_CABLE
      );
   }

   @SubscribeEvent
   public static void addLayers(AddLayers event) {
      for (String skinName : event.getSkins()) {
         addCustomLayers(EntityType.f_20532_, (PlayerRenderer)event.getSkin(skinName), event.getContext().m_266367_());
      }

      for (Entry<EntityType<?>, EntityRenderer<?>> entry : Minecraft.m_91087_().m_91290_().f_114362_.entrySet()) {
         EntityRenderer<?> renderer = entry.getValue();
         if (renderer instanceof LivingEntityRenderer) {
            EntityType<?> entityType = entry.getKey();
            addCustomLayers(entityType, event.getRenderer(entityType), event.getContext().m_266367_());
         }
      }
   }

   private static <T extends LivingEntity, M extends HumanoidModel<T>> void addCustomLayers(
      EntityType<?> type, @Nullable LivingEntityRenderer<T, M> renderer, ModelManager modelManager
   ) {
      if (renderer != null) {
         HumanoidArmorLayer<T, M, ?> bipedArmorLayer = null;
         boolean hasElytra = false;

         for (RenderLayer<T, M> layerRenderer : renderer.f_115291_) {
            if (layerRenderer != null) {
               Class<?> layerClass = layerRenderer.getClass();
               if (layerClass == HumanoidArmorLayer.class) {
                  bipedArmorLayer = (HumanoidArmorLayer<T, M, ?>)layerRenderer;
                  if (hasElytra) {
                     break;
                  }
               } else if (layerClass == ElytraLayer.class) {
                  hasElytra = true;
                  if (bipedArmorLayer != null) {
                     break;
                  }
               }
            }
         }

         if (bipedArmorLayer != null) {
            renderer.m_115326_(new MekanismArmorLayer(renderer, bipedArmorLayer.f_117071_, bipedArmorLayer.f_117072_, modelManager));
            Mekanism.logger.debug("Added Mekanism Armor Layer to entity of type: {}", RegistryUtils.getName(type));
         }

         if (hasElytra) {
            renderer.m_115326_(new MekanismElytraLayer(renderer, Minecraft.m_91087_().m_167973_()));
            Mekanism.logger.debug("Added Mekanism Elytra Layer to entity of type: {}", RegistryUtils.getName(type));
         }
      }
   }

   public static void addCustomModel(IItemProvider provider, ClientRegistration.CustomModelRegistryObject object) {
      customModels.put(provider.getRegistryName(), object);
   }

   public static void addLitModel(IItemProvider... providers) {
      for (IItemProvider provider : providers) {
         addCustomModel(provider, (orig, evt) -> lightBakedModel(orig));
      }
   }

   private static BakedModel lightBakedModel(BakedModel orig) {
      if (orig instanceof Baked separatePerspectiveModel) {
         SEPARATE_PERSPECTIVE_BASE_MODEL.transformValue(separatePerspectiveModel, Objects::nonNull, ClientRegistration::lightBakedModel);
         SEPARATE_PERSPECTIVE_PERSPECTIVES.transformValue(
            separatePerspectiveModel, v -> !v.isEmpty(), org -> ImmutableMap.copyOf(Maps.transformValues(org, ClientRegistration::lightBakedModel))
         );
         return orig;
      } else {
         return new ExtensionBakedModel.LightedBakedModel(orig);
      }
   }

   @FunctionalInterface
   public interface CustomModelRegistryObject {
      BakedModel createModel(BakedModel original, ModifyBakingResult event);
   }
}
