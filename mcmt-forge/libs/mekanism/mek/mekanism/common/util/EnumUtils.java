package mekanism.common.util;

import mekanism.api.RelativeSide;
import mekanism.api.Upgrade;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.text.EnumColor;
import mekanism.api.tier.BaseTier;
import mekanism.common.content.blocktype.FactoryType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.tile.base.SubstanceType;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;

public class EnumUtils {
   public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
   public static final EquipmentSlot[] HAND_SLOTS = new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
   public static final Direction[] DIRECTIONS = Direction.values();
   public static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
   public static final RelativeSide[] SIDES = RelativeSide.values();
   public static final UnitDisplayUtils.MeasurementUnit[] MEASUREMENT_UNITS = UnitDisplayUtils.MeasurementUnit.values();
   public static final UnitDisplayUtils.FloatingLongMeasurementUnit[] FLOATING_LONG_MEASUREMENT_UNITS = UnitDisplayUtils.FloatingLongMeasurementUnit.values();
   public static final TransmissionType[] TRANSMISSION_TYPES = TransmissionType.values();
   public static final BaseTier[] TIERS = BaseTier.values();
   public static final CableTier[] CABLE_TIERS = CableTier.values();
   public static final TransporterTier[] TRANSPORTER_TIERS = TransporterTier.values();
   public static final ConductorTier[] CONDUCTOR_TIERS = ConductorTier.values();
   public static final TubeTier[] TUBE_TIERS = TubeTier.values();
   public static final PipeTier[] PIPE_TIERS = PipeTier.values();
   public static final ChemicalTankTier[] CHEMICAL_TANK_TIERS = ChemicalTankTier.values();
   public static final FluidTankTier[] FLUID_TANK_TIERS = FluidTankTier.values();
   public static final BinTier[] BIN_TIERS = BinTier.values();
   public static final EnergyCubeTier[] ENERGY_CUBE_TIERS = EnergyCubeTier.values();
   public static final InductionCellTier[] INDUCTION_CELL_TIERS = InductionCellTier.values();
   public static final InductionProviderTier[] INDUCTION_PROVIDER_TIERS = InductionProviderTier.values();
   public static final FactoryTier[] FACTORY_TIERS = FactoryTier.values();
   public static final FactoryType[] FACTORY_TYPES = FactoryType.values();
   public static final Upgrade[] UPGRADES = Upgrade.values();
   public static final SubstanceType[] SUBSTANCES = SubstanceType.values();
   public static final OreType[] ORE_TYPES = OreType.values();
   public static final PrimaryResource[] PRIMARY_RESOURCES = PrimaryResource.values();
   public static final ResourceType[] RESOURCE_TYPES = ResourceType.values();
   public static final EquipmentSlot[] EQUIPMENT_SLOT_TYPES = EquipmentSlot.values();
   public static final ChemicalType[] CHEMICAL_TYPES = ChemicalType.values();
   public static final EnumColor[] COLORS = EnumColor.values();

   private EnumUtils() {
   }
}
