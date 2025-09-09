package mekanism.common.item.block;

import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.ChemicalTankContentsHandler;
import mekanism.common.content.blocktype.Machine;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tile.TileEntityChemicalTank;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalTank
   extends ItemBlockTooltip<BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>>>
   implements IItemSustainedInventory {
   public ItemBlockChemicalTank(BlockTile.BlockTileModel<TileEntityChemicalTank, Machine<TileEntityChemicalTank>> block) {
      super(block);
   }

   public ChemicalTankTier getTier() {
      return Attribute.getTier(this.m_40614_(), ChemicalTankTier.class);
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      ChemicalTankTier tier = this.getTier();
      StorageUtils.addStoredSubstance(stack, tooltip, tier == ChemicalTankTier.CREATIVE);
      if (tier == ChemicalTankTier.CREATIVE) {
         tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, MekanismLang.INFINITE}));
      } else {
         tooltip.add(MekanismLang.CAPACITY_MB.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, TextUtils.format(tier.getStorage())}));
      }

      super.m_7373_(stack, world, tooltip, flag);
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return ChemicalUtil.hasGas(stack)
         || ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.INFUSION_HANDLER)
         || ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.PIGMENT_HANDLER)
         || ChemicalUtil.hasChemical(stack, ConstantPredicates.alwaysTrue(), Capabilities.SLURRY_HANDLER);
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return ChemicalUtil.getRGBDurabilityForDisplay(stack);
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(ChemicalTankContentsHandler.create(this.getTier()));
   }
}
