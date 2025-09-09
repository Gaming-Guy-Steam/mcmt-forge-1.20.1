package mekanism.common.registries;

import java.util.function.BooleanSupplier;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.prefab.BlockBase;
import mekanism.common.block.transmitter.BlockMechanicalPipe;
import mekanism.common.block.transmitter.BlockSmallTransmitter;
import mekanism.common.block.transmitter.BlockTransmitter;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registration.impl.CreativeTabRegistryObject;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismCreativeTabs {
   public static final CreativeTabDeferredRegister CREATIVE_TABS = new CreativeTabDeferredRegister("mekanism", MekanismCreativeTabs::addToExistingTabs);
   public static final CreativeTabRegistryObject MEKANISM = CREATIVE_TABS.registerMain(
      MekanismLang.MEKANISM, MekanismBlocks.METALLURGIC_INFUSER, builder -> builder.withSearchBar().m_257501_((displayParameters, output) -> {
         CreativeTabDeferredRegister.addToDisplay(MekanismItems.ITEMS, output);
         CreativeTabDeferredRegister.addToDisplay(MekanismBlocks.BLOCKS, output);
         CreativeTabDeferredRegister.addToDisplay(MekanismFluids.FLUIDS, output);
         addFilledTanks(output, true);
      })
   );

   private static void addFilledTanks(Output output, boolean chemical) {
      if (MekanismConfig.general.isLoaded()) {
         if (MekanismConfig.general.prefilledFluidTanks.get()) {
            int capacity = FluidTankTier.CREATIVE.getStorage();

            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
               if (fluid.m_7444_(fluid.m_76145_())) {
                  output.m_246342_(FluidUtils.getFilledVariant(MekanismBlocks.CREATIVE_FLUID_TANK.getItemStack(), capacity, () -> fluid));
               }
            }
         }

         if (chemical) {
            addFilled(MekanismConfig.general.prefilledGasTanks, MekanismAPI.gasRegistry(), output);
            addFilled(MekanismConfig.general.prefilledInfusionTanks, MekanismAPI.infuseTypeRegistry(), output);
            addFilled(MekanismConfig.general.prefilledPigmentTanks, MekanismAPI.pigmentRegistry(), output);
            addFilled(MekanismConfig.general.prefilledSlurryTanks, MekanismAPI.slurryRegistry(), output);
         }
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>> void addFilled(BooleanSupplier shouldAdd, IForgeRegistry<CHEMICAL> registry, Output tabOutput) {
      if (shouldAdd.getAsBoolean()) {
         long capacity = ChemicalTankTier.CREATIVE.getStorage();

         for (CHEMICAL type : registry.getValues()) {
            if (!type.isHidden()) {
               tabOutput.m_246342_(ChemicalUtil.getFilledVariant(MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack(), capacity, type));
            }
         }
      }
   }

   private static void addToExistingTabs(BuildCreativeModeTabContentsEvent event) {
      ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
      if (tabKey == CreativeModeTabs.f_256788_) {
         CreativeTabDeferredRegister.addToDisplay(
            event,
            MekanismBlocks.SALT_BLOCK,
            MekanismBlocks.BRONZE_BLOCK,
            MekanismBlocks.STEEL_BLOCK,
            MekanismBlocks.CHARCOAL_BLOCK,
            MekanismBlocks.REFINED_OBSIDIAN_BLOCK,
            MekanismBlocks.REFINED_GLOWSTONE_BLOCK
         );

         for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            if (resource.getResourceBlockInfo() != null) {
               CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource));
            }
         }
      } else if (tabKey == CreativeModeTabs.f_256776_) {
         CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.SALT_BLOCK);

         for (OreBlockType oreType : MekanismBlocks.ORES.values()) {
            CreativeTabDeferredRegister.addToDisplay(event, oreType.stone(), oreType.deepslate());
         }
      } else if (tabKey == CreativeModeTabs.f_256791_) {
         for (IBlockProvider blockProvider : MekanismBlocks.BLOCKS.getAllBlocks()) {
            Block block = blockProvider.getBlock();
            if (block instanceof BlockBase<?> base && base.getType() instanceof Machine || block instanceof BlockTransmitter) {
               CreativeTabDeferredRegister.addToDisplay(event, block);
            }
         }

         CreativeTabDeferredRegister.addToDisplay(
            event,
            MekanismBlocks.SECURITY_DESK,
            MekanismBlocks.RADIOACTIVE_WASTE_BARREL,
            MekanismBlocks.PERSONAL_CHEST,
            MekanismBlocks.PERSONAL_BARREL,
            MekanismBlocks.CHARGEPAD,
            MekanismBlocks.LASER,
            MekanismBlocks.LASER_AMPLIFIER,
            MekanismBlocks.LASER_AMPLIFIER,
            MekanismBlocks.QUANTUM_ENTANGLOPORTER,
            MekanismBlocks.OREDICTIONIFICATOR,
            MekanismBlocks.FUELWOOD_HEATER,
            MekanismBlocks.MODIFICATION_STATION,
            MekanismBlocks.QIO_DRIVE_ARRAY,
            MekanismBlocks.QIO_DASHBOARD,
            MekanismBlocks.QIO_EXPORTER,
            MekanismBlocks.QIO_IMPORTER,
            MekanismBlocks.QIO_REDSTONE_ADAPTER
         );
      } else if (tabKey == CreativeModeTabs.f_257028_) {
         CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.INDUSTRIAL_ALARM);

         for (IBlockProvider blockProviderx : MekanismBlocks.BLOCKS.getAllBlocks()) {
            Block block = blockProviderx.getBlock();
            if (Attribute.has(block, Attributes.AttributeComparator.class) || block instanceof BlockSmallTransmitter || block instanceof BlockMechanicalPipe) {
               CreativeTabDeferredRegister.addToDisplay(event, block);
            }
         }

         CreativeTabDeferredRegister.addToDisplay(event, MekanismBlocks.DIVERSION_TRANSPORTER);
      } else if (tabKey == CreativeModeTabs.f_256869_) {
         CreativeTabDeferredRegister.addToDisplay(
            event,
            MekanismItems.CONFIGURATOR,
            MekanismItems.NETWORK_READER,
            MekanismItems.DOSIMETER,
            MekanismItems.GEIGER_COUNTER,
            MekanismItems.DICTIONARY,
            MekanismItems.CONFIGURATION_CARD,
            MekanismItems.GAUGE_DROPPER,
            MekanismItems.CRAFTING_FORMULA,
            MekanismItems.PORTABLE_QIO_DASHBOARD,
            MekanismItems.ATOMIC_DISASSEMBLER,
            MekanismItems.MEKA_TOOL,
            MekanismItems.SCUBA_MASK,
            MekanismItems.SCUBA_TANK,
            MekanismItems.FREE_RUNNERS,
            MekanismItems.ARMORED_FREE_RUNNERS,
            MekanismItems.JETPACK,
            MekanismItems.ARMORED_JETPACK,
            MekanismItems.HDPE_REINFORCED_ELYTRA,
            MekanismItems.HAZMAT_MASK,
            MekanismItems.HAZMAT_GOWN,
            MekanismItems.HAZMAT_PANTS,
            MekanismItems.HAZMAT_BOOTS,
            MekanismBlocks.CARDBOARD_BOX,
            MekanismItems.BASIC_TIER_INSTALLER,
            MekanismItems.ADVANCED_TIER_INSTALLER,
            MekanismItems.ELITE_TIER_INSTALLER,
            MekanismItems.ULTIMATE_TIER_INSTALLER,
            MekanismItems.SPEED_UPGRADE,
            MekanismItems.ENERGY_UPGRADE,
            MekanismItems.FILTER_UPGRADE,
            MekanismItems.MUFFLING_UPGRADE,
            MekanismItems.GAS_UPGRADE,
            MekanismItems.ANCHOR_UPGRADE,
            MekanismItems.STONE_GENERATOR_UPGRADE,
            MekanismBlocks.BASIC_FLUID_TANK,
            MekanismBlocks.ADVANCED_FLUID_TANK,
            MekanismBlocks.ELITE_FLUID_TANK,
            MekanismBlocks.ULTIMATE_FLUID_TANK,
            MekanismBlocks.CREATIVE_FLUID_TANK
         );
         CreativeTabDeferredRegister.addToDisplay(MekanismFluids.FLUIDS, event);
         addFilledTanks(event, false);
      } else if (tabKey == CreativeModeTabs.f_256797_) {
         CreativeTabDeferredRegister.addToDisplay(
            event,
            MekanismItems.ATOMIC_DISASSEMBLER,
            MekanismItems.FLAMETHROWER,
            MekanismItems.ELECTRIC_BOW,
            MekanismItems.MEKA_TOOL,
            MekanismItems.MEKASUIT_HELMET,
            MekanismItems.MEKASUIT_BODYARMOR,
            MekanismItems.MEKASUIT_PANTS,
            MekanismItems.MEKASUIT_BOOTS,
            MekanismItems.ARMORED_FREE_RUNNERS,
            MekanismItems.ARMORED_JETPACK
         );
      } else if (tabKey == CreativeModeTabs.f_256839_) {
         MekanismItems.CANTEEN.get().addItems(event);
      } else if (tabKey == CreativeModeTabs.f_256968_) {
         CreativeTabDeferredRegister.addToDisplay(
            event,
            MekanismItems.MODULE_BASE,
            MekanismItems.INFUSED_ALLOY,
            MekanismItems.REINFORCED_ALLOY,
            MekanismItems.ATOMIC_ALLOY,
            MekanismItems.BASIC_CONTROL_CIRCUIT,
            MekanismItems.ADVANCED_CONTROL_CIRCUIT,
            MekanismItems.ELITE_CONTROL_CIRCUIT,
            MekanismItems.ULTIMATE_CONTROL_CIRCUIT,
            MekanismItems.ENRICHED_CARBON,
            MekanismItems.ENRICHED_REDSTONE,
            MekanismItems.ENRICHED_DIAMOND,
            MekanismItems.ENRICHED_OBSIDIAN,
            MekanismItems.ENRICHED_GOLD,
            MekanismItems.ENRICHED_TIN,
            MekanismItems.BIO_FUEL,
            MekanismItems.SUBSTRATE,
            MekanismItems.HDPE_PELLET,
            MekanismItems.HDPE_ROD,
            MekanismItems.HDPE_SHEET,
            MekanismItems.ANTIMATTER_PELLET,
            MekanismItems.PLUTONIUM_PELLET,
            MekanismItems.POLONIUM_PELLET,
            MekanismItems.REPROCESSED_FISSILE_FRAGMENT,
            MekanismItems.ELECTROLYTIC_CORE,
            MekanismItems.TELEPORTATION_CORE,
            MekanismItems.ENRICHED_IRON,
            MekanismItems.SAWDUST,
            MekanismItems.SALT,
            MekanismItems.DYE_BASE,
            MekanismItems.FLUORITE_GEM,
            MekanismItems.FLUORITE_DUST,
            MekanismItems.YELLOW_CAKE_URANIUM,
            MekanismItems.DIRTY_NETHERITE_SCRAP,
            MekanismItems.NETHERITE_DUST,
            MekanismItems.CHARCOAL_DUST,
            MekanismItems.COAL_DUST,
            MekanismItems.SULFUR_DUST,
            MekanismItems.BRONZE_DUST,
            MekanismItems.LAPIS_LAZULI_DUST,
            MekanismItems.QUARTZ_DUST,
            MekanismItems.EMERALD_DUST,
            MekanismItems.DIAMOND_DUST,
            MekanismItems.STEEL_DUST,
            MekanismItems.OBSIDIAN_DUST,
            MekanismItems.REFINED_OBSIDIAN_DUST,
            MekanismItems.BRONZE_NUGGET,
            MekanismItems.STEEL_NUGGET,
            MekanismItems.REFINED_OBSIDIAN_NUGGET,
            MekanismItems.REFINED_GLOWSTONE_NUGGET,
            MekanismItems.BRONZE_INGOT,
            MekanismItems.STEEL_INGOT,
            MekanismItems.REFINED_OBSIDIAN_INGOT,
            MekanismItems.REFINED_GLOWSTONE_INGOT
         );

         for (IItemProvider item : MekanismItems.PROCESSED_RESOURCES.values()) {
            CreativeTabDeferredRegister.addToDisplay(event, item);
         }
      }
   }
}
