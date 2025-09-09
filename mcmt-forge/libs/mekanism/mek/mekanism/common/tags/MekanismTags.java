package mekanism.common.tags;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.Mekanism;
import mekanism.common.resource.BlockResourceInfo;
import mekanism.common.resource.IResource;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class MekanismTags {
   public static void init() {
      MekanismTags.Items.init();
      MekanismTags.Blocks.init();
      MekanismTags.Biomes.init();
      MekanismTags.DamageTypes.init();
      MekanismTags.Fluids.init();
      MekanismTags.Gases.init();
      MekanismTags.InfuseTypes.init();
      MekanismTags.MobEffects.init();
      MekanismTags.Slurries.init();
      MekanismTags.TileEntityTypes.init();
   }

   private MekanismTags() {
   }

   public static class Biomes {
      public static final TagKey<Biome> SPAWN_ORES = tag("spawn_ores");

      private static void init() {
      }

      private Biomes() {
      }

      private static TagKey<Biome> tag(String name) {
         return TagUtils.createKey(Registries.f_256952_, Mekanism.rl(name));
      }
   }

   public static class Blocks {
      public static final Map<IResource, TagKey<Block>> RESOURCE_STORAGE_BLOCKS = new HashMap<>();
      public static final Map<OreType, TagKey<Block>> ORES = new EnumMap<>(OreType.class);
      public static final TagKey<Block> RELOCATION_NOT_SUPPORTED;
      public static final TagKey<Block> CARDBOARD_BLACKLIST;
      public static final TagKey<Block> MINER_BLACKLIST;
      public static final LazyTagLookup<Block> MINER_BLACKLIST_LOOKUP;
      public static final TagKey<Block> ATOMIC_DISASSEMBLER_ORE;
      public static final TagKey<Block> FARMING_OVERRIDE;
      public static final TagKey<Block> CHESTS_ELECTRIC;
      public static final TagKey<Block> CHESTS_PERSONAL;
      public static final TagKey<Block> BARRELS_PERSONAL;
      public static final TagKey<Block> PERSONAL_STORAGE;
      public static final TagKey<Block> STORAGE_BLOCKS_BRONZE;
      public static final TagKey<Block> STORAGE_BLOCKS_CHARCOAL;
      public static final TagKey<Block> STORAGE_BLOCKS_REFINED_GLOWSTONE;
      public static final TagKey<Block> STORAGE_BLOCKS_REFINED_OBSIDIAN;
      public static final TagKey<Block> STORAGE_BLOCKS_STEEL;
      public static final TagKey<Block> STORAGE_BLOCKS_FLUORITE;

      private static void init() {
      }

      private Blocks() {
      }

      private static TagKey<Block> forgeTag(String name) {
         return BlockTags.create(new ResourceLocation("forge", name));
      }

      private static TagKey<Block> tag(String name) {
         return BlockTags.create(Mekanism.rl(name));
      }

      static {
         for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            if (!resource.isVanilla()) {
               RESOURCE_STORAGE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getRegistrySuffix()));
               BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
               if (rawResource != null) {
                  RESOURCE_STORAGE_BLOCKS.put(rawResource, forgeTag("storage_blocks/" + rawResource.getRegistrySuffix()));
               }
            }
         }

         for (OreType ore : EnumUtils.ORE_TYPES) {
            ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
         }

         RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
         CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
         MINER_BLACKLIST = tag("miner_blacklist");
         MINER_BLACKLIST_LOOKUP = LazyTagLookup.create(ForgeRegistries.BLOCKS, MINER_BLACKLIST);
         ATOMIC_DISASSEMBLER_ORE = tag("atomic_disassembler_ore");
         FARMING_OVERRIDE = tag("farming_override");
         CHESTS_ELECTRIC = forgeTag("chests/electric");
         CHESTS_PERSONAL = forgeTag("chests/personal");
         BARRELS_PERSONAL = forgeTag("barrels/personal");
         PERSONAL_STORAGE = tag("personal_storage");
         STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
         STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
         STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
         STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
         STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
         STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");
      }
   }

   public static class DamageTypes {
      public static final TagKey<DamageType> MEKASUIT_ALWAYS_SUPPORTED = tag("mekasuit_always_supported");
      public static final TagKey<DamageType> IS_PREVENTABLE_MAGIC = tag("is_preventable_magic");

      private static void init() {
      }

      private DamageTypes() {
      }

      private static TagKey<DamageType> tag(String name) {
         return TagUtils.createKey(Registries.f_268580_, Mekanism.rl(name));
      }
   }

   public static class Fluids {
      public static final TagKey<Fluid> BRINE = forgeTag("brine");
      public static final TagKey<Fluid> CHLORINE = forgeTag("chlorine");
      public static final TagKey<Fluid> ETHENE = forgeTag("ethene");
      public static final TagKey<Fluid> HEAVY_WATER = forgeTag("heavy_water");
      public static final TagKey<Fluid> HYDROGEN = forgeTag("hydrogen");
      public static final TagKey<Fluid> HYDROGEN_CHLORIDE = forgeTag("hydrogen_chloride");
      public static final TagKey<Fluid> URANIUM_OXIDE = forgeTag("uranium_oxide");
      public static final TagKey<Fluid> URANIUM_HEXAFLUORIDE = forgeTag("uranium_hexafluoride");
      public static final TagKey<Fluid> LITHIUM = forgeTag("lithium");
      public static final TagKey<Fluid> OXYGEN = forgeTag("oxygen");
      public static final TagKey<Fluid> SODIUM = forgeTag("sodium");
      public static final TagKey<Fluid> SUPERHEATED_SODIUM = forgeTag("superheated_sodium");
      public static final TagKey<Fluid> STEAM = forgeTag("steam");
      public static final TagKey<Fluid> SULFUR_DIOXIDE = forgeTag("sulfur_dioxide");
      public static final TagKey<Fluid> SULFUR_TRIOXIDE = forgeTag("sulfur_trioxide");
      public static final TagKey<Fluid> SULFURIC_ACID = forgeTag("sulfuric_acid");
      public static final TagKey<Fluid> HYDROFLUORIC_ACID = forgeTag("hydrofluoric_acid");
      public static final LazyTagLookup<Fluid> WATER_LOOKUP = LazyTagLookup.create(ForgeRegistries.FLUIDS, FluidTags.f_13131_);
      public static final LazyTagLookup<Fluid> LAVA_LOOKUP = LazyTagLookup.create(ForgeRegistries.FLUIDS, FluidTags.f_13132_);

      private static void init() {
      }

      private Fluids() {
      }

      private static TagKey<Fluid> forgeTag(String name) {
         return FluidTags.create(new ResourceLocation("forge", name));
      }
   }

   public static class Gases {
      public static final TagKey<Gas> WATER_VAPOR = tag("water_vapor");
      public static final TagKey<Gas> WASTE_BARREL_DECAY_BLACKLIST = tag("waste_barrel_decay_blacklist");
      public static final LazyTagLookup<Gas> WASTE_BARREL_DECAY_LOOKUP = LazyTagLookup.create(ChemicalTags.GAS, WASTE_BARREL_DECAY_BLACKLIST);

      private static void init() {
      }

      private Gases() {
      }

      private static TagKey<Gas> tag(String name) {
         return ChemicalTags.GAS.tag(Mekanism.rl(name));
      }
   }

   public static class InfuseTypes {
      public static final TagKey<InfuseType> CARBON = tag("carbon");
      public static final TagKey<InfuseType> REDSTONE = tag("redstone");
      public static final TagKey<InfuseType> DIAMOND = tag("diamond");
      public static final TagKey<InfuseType> REFINED_OBSIDIAN = tag("refined_obsidian");
      public static final TagKey<InfuseType> BIO = tag("bio");
      public static final TagKey<InfuseType> FUNGI = tag("fungi");
      public static final TagKey<InfuseType> GOLD = tag("gold");
      public static final TagKey<InfuseType> TIN = tag("tin");

      private static void init() {
      }

      private InfuseTypes() {
      }

      private static TagKey<InfuseType> tag(String name) {
         return ChemicalTags.INFUSE_TYPE.tag(Mekanism.rl(name));
      }
   }

   public static class Items {
      public static final Table<ResourceType, PrimaryResource, TagKey<Item>> PROCESSED_RESOURCES = HashBasedTable.create();
      public static final Map<IResource, TagKey<Item>> PROCESSED_RESOURCE_BLOCKS = new HashMap<>();
      public static final Map<OreType, TagKey<Item>> ORES = new EnumMap<>(OreType.class);
      public static final TagKey<Item> CONFIGURATORS;
      public static final TagKey<Item> WRENCHES;
      public static final TagKey<Item> TOOLS_WRENCH;
      public static final TagKey<Item> PERSONAL_STORAGE;
      public static final TagKey<Item> BATTERIES;
      public static final TagKey<Item> RODS_PLASTIC;
      public static final TagKey<Item> FUELS;
      public static final TagKey<Item> FUELS_BIO;
      public static final TagKey<Item> SALT;
      public static final TagKey<Item> SAWDUST;
      public static final TagKey<Item> YELLOW_CAKE_URANIUM;
      public static final TagKey<Item> PELLETS_ANTIMATTER;
      public static final TagKey<Item> PELLETS_PLUTONIUM;
      public static final TagKey<Item> PELLETS_POLONIUM;
      public static final TagKey<Item> DUSTS_BRONZE;
      public static final TagKey<Item> DUSTS_CHARCOAL;
      public static final TagKey<Item> DUSTS_COAL;
      public static final TagKey<Item> DUSTS_DIAMOND;
      public static final TagKey<Item> DUSTS_EMERALD;
      public static final TagKey<Item> DUSTS_NETHERITE;
      public static final TagKey<Item> DUSTS_LAPIS;
      public static final TagKey<Item> DUSTS_LITHIUM;
      public static final TagKey<Item> DUSTS_OBSIDIAN;
      public static final TagKey<Item> DUSTS_QUARTZ;
      public static final TagKey<Item> DUSTS_REFINED_OBSIDIAN;
      public static final TagKey<Item> DUSTS_SALT;
      public static final TagKey<Item> DUSTS_STEEL;
      public static final TagKey<Item> DUSTS_SULFUR;
      public static final TagKey<Item> DUSTS_WOOD;
      public static final TagKey<Item> DUSTS_FLUORITE;
      public static final TagKey<Item> NUGGETS_BRONZE;
      public static final TagKey<Item> NUGGETS_REFINED_GLOWSTONE;
      public static final TagKey<Item> NUGGETS_REFINED_OBSIDIAN;
      public static final TagKey<Item> NUGGETS_STEEL;
      public static final TagKey<Item> INGOTS_BRONZE;
      public static final TagKey<Item> INGOTS_REFINED_GLOWSTONE;
      public static final TagKey<Item> INGOTS_REFINED_OBSIDIAN;
      public static final TagKey<Item> INGOTS_STEEL;
      public static final TagKey<Item> STORAGE_BLOCKS_BRONZE;
      public static final TagKey<Item> STORAGE_BLOCKS_CHARCOAL;
      public static final TagKey<Item> STORAGE_BLOCKS_REFINED_GLOWSTONE;
      public static final TagKey<Item> STORAGE_BLOCKS_REFINED_OBSIDIAN;
      public static final TagKey<Item> STORAGE_BLOCKS_STEEL;
      public static final TagKey<Item> STORAGE_BLOCKS_FLUORITE;
      public static final TagKey<Item> CIRCUITS;
      public static final TagKey<Item> CIRCUITS_BASIC;
      public static final TagKey<Item> CIRCUITS_ADVANCED;
      public static final TagKey<Item> CIRCUITS_ELITE;
      public static final TagKey<Item> CIRCUITS_ULTIMATE;
      public static final TagKey<Item> ALLOYS;
      public static final TagKey<Item> ALLOYS_BASIC;
      public static final TagKey<Item> ALLOYS_INFUSED;
      public static final TagKey<Item> ALLOYS_REINFORCED;
      public static final TagKey<Item> ALLOYS_ATOMIC;
      public static final TagKey<Item> FORGE_ALLOYS;
      public static final TagKey<Item> ALLOYS_ADVANCED;
      public static final TagKey<Item> ALLOYS_ELITE;
      public static final TagKey<Item> ALLOYS_ULTIMATE;
      public static final TagKey<Item> ENRICHED;
      public static final TagKey<Item> ENRICHED_CARBON;
      public static final TagKey<Item> ENRICHED_DIAMOND;
      public static final TagKey<Item> ENRICHED_OBSIDIAN;
      public static final TagKey<Item> ENRICHED_REDSTONE;
      public static final TagKey<Item> ENRICHED_GOLD;
      public static final TagKey<Item> ENRICHED_TIN;
      public static final TagKey<Item> DIRTY_DUSTS;
      public static final TagKey<Item> CLUMPS;
      public static final TagKey<Item> SHARDS;
      public static final TagKey<Item> CRYSTALS;
      public static final TagKey<Item> GEMS_FLUORITE;
      public static final TagKey<Item> MEKASUIT_HUD_RENDERER;
      public static final TagKey<Item> COLORABLE_WOOL;
      public static final TagKey<Item> COLORABLE_CARPETS;
      public static final TagKey<Item> COLORABLE_BEDS;
      public static final TagKey<Item> COLORABLE_GLASS;
      public static final TagKey<Item> COLORABLE_GLASS_PANES;
      public static final TagKey<Item> COLORABLE_TERRACOTTA;
      public static final TagKey<Item> COLORABLE_CANDLE;
      public static final TagKey<Item> COLORABLE_CONCRETE;
      public static final TagKey<Item> COLORABLE_CONCRETE_POWDER;
      public static final TagKey<Item> COLORABLE_BANNERS;
      public static final TagKey<Item> ARMORS_HELMETS_HAZMAT;
      public static final TagKey<Item> ARMORS_CHESTPLATES_HAZMAT;
      public static final TagKey<Item> ARMORS_LEGGINGS_HAZMAT;
      public static final TagKey<Item> ARMORS_BOOTS_HAZMAT;

      private static void init() {
      }

      private Items() {
      }

      private static TagKey<Item> forgeTag(String name) {
         return ItemTags.create(new ResourceLocation("forge", name));
      }

      private static TagKey<Item> tag(String name) {
         return ItemTags.create(Mekanism.rl(name));
      }

      static {
         for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            for (ResourceType type : EnumUtils.RESOURCE_TYPES) {
               if (type.usedByPrimary(resource)) {
                  if (!type.isVanilla() && type != ResourceType.DUST) {
                     PROCESSED_RESOURCES.put(type, resource, tag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                  } else {
                     PROCESSED_RESOURCES.put(type, resource, forgeTag(type.getBaseTagPath() + "/" + resource.getRegistrySuffix()));
                  }
               }
            }

            if (!resource.isVanilla()) {
               PROCESSED_RESOURCE_BLOCKS.put(resource, forgeTag("storage_blocks/" + resource.getRegistrySuffix()));
               BlockResourceInfo rawResource = resource.getRawResourceBlockInfo();
               if (rawResource != null) {
                  PROCESSED_RESOURCE_BLOCKS.put(rawResource, forgeTag("storage_blocks/" + rawResource.getRegistrySuffix()));
               }
            }
         }

         for (OreType ore : EnumUtils.ORE_TYPES) {
            ORES.put(ore, forgeTag("ores/" + ore.getResource().getRegistrySuffix()));
         }

         CONFIGURATORS = tag("configurators");
         WRENCHES = forgeTag("wrenches");
         TOOLS_WRENCH = forgeTag("tools/wrench");
         PERSONAL_STORAGE = tag("personal_storage");
         BATTERIES = forgeTag("batteries");
         RODS_PLASTIC = forgeTag("rods/plastic");
         FUELS = forgeTag("fuels");
         FUELS_BIO = forgeTag("fuels/bio");
         SALT = forgeTag("salt");
         SAWDUST = forgeTag("sawdust");
         YELLOW_CAKE_URANIUM = forgeTag("yellow_cake_uranium");
         PELLETS_ANTIMATTER = forgeTag("pellets/antimatter");
         PELLETS_PLUTONIUM = forgeTag("pellets/plutonium");
         PELLETS_POLONIUM = forgeTag("pellets/polonium");
         DUSTS_BRONZE = forgeTag("dusts/bronze");
         DUSTS_CHARCOAL = forgeTag("dusts/charcoal");
         DUSTS_COAL = forgeTag("dusts/coal");
         DUSTS_DIAMOND = forgeTag("dusts/diamond");
         DUSTS_EMERALD = forgeTag("dusts/emerald");
         DUSTS_NETHERITE = forgeTag("dusts/netherite");
         DUSTS_LAPIS = forgeTag("dusts/lapis");
         DUSTS_LITHIUM = forgeTag("dusts/lithium");
         DUSTS_OBSIDIAN = forgeTag("dusts/obsidian");
         DUSTS_QUARTZ = forgeTag("dusts/quartz");
         DUSTS_REFINED_OBSIDIAN = forgeTag("dusts/refined_obsidian");
         DUSTS_SALT = forgeTag("dusts/salt");
         DUSTS_STEEL = forgeTag("dusts/steel");
         DUSTS_SULFUR = forgeTag("dusts/sulfur");
         DUSTS_WOOD = forgeTag("dusts/wood");
         DUSTS_FLUORITE = forgeTag("dusts/fluorite");
         NUGGETS_BRONZE = forgeTag("nuggets/bronze");
         NUGGETS_REFINED_GLOWSTONE = forgeTag("nuggets/refined_glowstone");
         NUGGETS_REFINED_OBSIDIAN = forgeTag("nuggets/refined_obsidian");
         NUGGETS_STEEL = forgeTag("nuggets/steel");
         INGOTS_BRONZE = forgeTag("ingots/bronze");
         INGOTS_REFINED_GLOWSTONE = forgeTag("ingots/refined_glowstone");
         INGOTS_REFINED_OBSIDIAN = forgeTag("ingots/refined_obsidian");
         INGOTS_STEEL = forgeTag("ingots/steel");
         STORAGE_BLOCKS_BRONZE = forgeTag("storage_blocks/bronze");
         STORAGE_BLOCKS_CHARCOAL = forgeTag("storage_blocks/charcoal");
         STORAGE_BLOCKS_REFINED_GLOWSTONE = forgeTag("storage_blocks/refined_glowstone");
         STORAGE_BLOCKS_REFINED_OBSIDIAN = forgeTag("storage_blocks/refined_obsidian");
         STORAGE_BLOCKS_STEEL = forgeTag("storage_blocks/steel");
         STORAGE_BLOCKS_FLUORITE = forgeTag("storage_blocks/fluorite");
         CIRCUITS = forgeTag("circuits");
         CIRCUITS_BASIC = forgeTag("circuits/basic");
         CIRCUITS_ADVANCED = forgeTag("circuits/advanced");
         CIRCUITS_ELITE = forgeTag("circuits/elite");
         CIRCUITS_ULTIMATE = forgeTag("circuits/ultimate");
         ALLOYS = tag("alloys");
         ALLOYS_BASIC = tag("alloys/basic");
         ALLOYS_INFUSED = tag("alloys/infused");
         ALLOYS_REINFORCED = tag("alloys/reinforced");
         ALLOYS_ATOMIC = tag("alloys/atomic");
         FORGE_ALLOYS = forgeTag("alloys");
         ALLOYS_ADVANCED = forgeTag("alloys/advanced");
         ALLOYS_ELITE = forgeTag("alloys/elite");
         ALLOYS_ULTIMATE = forgeTag("alloys/ultimate");
         ENRICHED = tag("enriched");
         ENRICHED_CARBON = tag("enriched/carbon");
         ENRICHED_DIAMOND = tag("enriched/diamond");
         ENRICHED_OBSIDIAN = tag("enriched/obsidian");
         ENRICHED_REDSTONE = tag("enriched/redstone");
         ENRICHED_GOLD = tag("enriched/gold");
         ENRICHED_TIN = tag("enriched/tin");
         DIRTY_DUSTS = tag("dirty_dusts");
         CLUMPS = tag("clumps");
         SHARDS = tag("shards");
         CRYSTALS = tag("crystals");
         GEMS_FLUORITE = forgeTag("gems/fluorite");
         MEKASUIT_HUD_RENDERER = tag("mekasuit_hud_renderer");
         COLORABLE_WOOL = tag("colorable/wool");
         COLORABLE_CARPETS = tag("colorable/carpets");
         COLORABLE_BEDS = tag("colorable/beds");
         COLORABLE_GLASS = tag("colorable/glass");
         COLORABLE_GLASS_PANES = tag("colorable/glass_panes");
         COLORABLE_TERRACOTTA = tag("colorable/terracotta");
         COLORABLE_CANDLE = tag("colorable/candle");
         COLORABLE_CONCRETE = tag("colorable/concrete");
         COLORABLE_CONCRETE_POWDER = tag("colorable/concrete_powder");
         COLORABLE_BANNERS = tag("colorable/banners");
         ARMORS_HELMETS_HAZMAT = forgeTag("armors/hazmat");
         ARMORS_CHESTPLATES_HAZMAT = forgeTag("armors/chestplates/hazmat");
         ARMORS_LEGGINGS_HAZMAT = forgeTag("armors/leggings/hazmat");
         ARMORS_BOOTS_HAZMAT = forgeTag("armors/boots/hazmat");
      }
   }

   public static class MobEffects {
      public static final TagKey<MobEffect> SPEED_UP_BLACKLIST = tag("speed_up_blacklist");
      public static final LazyTagLookup<MobEffect> SPEED_UP_BLACKLIST_LOOKUP = LazyTagLookup.create(ForgeRegistries.MOB_EFFECTS, SPEED_UP_BLACKLIST);

      private static void init() {
      }

      private MobEffects() {
      }

      private static TagKey<MobEffect> tag(String name) {
         return TagUtils.createKey(ForgeRegistries.MOB_EFFECTS, Mekanism.rl(name));
      }
   }

   public static class Slurries {
      public static final TagKey<Slurry> DIRTY = tag("dirty");
      public static final LazyTagLookup<Slurry> DIRTY_LOOKUP = LazyTagLookup.create(ChemicalTags.SLURRY, DIRTY);
      public static final TagKey<Slurry> CLEAN = tag("clean");

      private static void init() {
      }

      private Slurries() {
      }

      private static TagKey<Slurry> tag(String name) {
         return ChemicalTags.SLURRY.tag(Mekanism.rl(name));
      }
   }

   public static class TileEntityTypes {
      public static final TagKey<BlockEntityType<?>> CARDBOARD_BLACKLIST = tag("cardboard_blacklist");
      public static final LazyTagLookup<BlockEntityType<?>> CARDBOARD_BLACKLIST_LOOKUP = LazyTagLookup.create(
         ForgeRegistries.BLOCK_ENTITY_TYPES, CARDBOARD_BLACKLIST
      );
      public static final TagKey<BlockEntityType<?>> RELOCATION_NOT_SUPPORTED = forgeTag("relocation_not_supported");
      public static final TagKey<BlockEntityType<?>> IMMOVABLE = forgeTag("immovable");

      private static void init() {
      }

      private TileEntityTypes() {
      }

      private static TagKey<BlockEntityType<?>> tag(String name) {
         return TagUtils.createKey(ForgeRegistries.BLOCK_ENTITY_TYPES, Mekanism.rl(name));
      }

      private static TagKey<BlockEntityType<?>> forgeTag(String name) {
         return TagUtils.createKey(ForgeRegistries.BLOCK_ENTITY_TYPES, new ResourceLocation("forge", name));
      }
   }
}
