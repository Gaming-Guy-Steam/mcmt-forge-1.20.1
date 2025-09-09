package mekanism.common.item.block;

import java.util.List;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class ItemBlockTooltip<BLOCK extends Block & IHasDescription> extends ItemBlockMekanism<BLOCK> {
   private final boolean hasDetails;

   public ItemBlockTooltip(BLOCK block, Properties properties) {
      this(block, false, properties);
   }

   public ItemBlockTooltip(BLOCK block) {
      this(block, true, new Properties().m_41487_(1));
   }

   protected ItemBlockTooltip(BLOCK block, boolean hasDetails, Properties properties) {
      super(block, properties);
      this.hasDetails = hasDetails;
   }

   public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
      InventoryUtils.dropItemContents(item, damageSource);
   }

   public boolean m_7429_(@NotNull BlockPlaceContext context, @NotNull BlockState state) {
      AttributeHasBounding hasBounding = Attribute.get(state, AttributeHasBounding.class);
      return (hasBounding == null || WorldUtils.areBlocksValidAndReplaceable(context.m_43725_(), hasBounding.getPositions(context.m_8083_(), state)))
         && super.m_7429_(context, state);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
         tooltip.add(this.m_40614_().getDescription().translate());
      } else if (this.hasDetails && MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         this.addDetails(stack, world, tooltip, flag);
      } else {
         this.addStats(stack, world, tooltip, flag);
         if (this.hasDetails) {
            tooltip.add(
               MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()})
            );
         }

         tooltip.add(
            MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, new Object[]{EnumColor.AQUA, MekanismKeyHandler.descriptionKey.m_90863_()})
         );
      }
   }

   protected void addStats(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
   }

   protected void addDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      ISecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
      this.addTypeDetails(stack, world, tooltip, flag);
      FluidStack fluidStack = StorageUtils.getStoredFluidFromNBT(stack);
      if (!fluidStack.isEmpty()) {
         tooltip.add(
            MekanismLang.GENERIC_STORED_MB
               .translateColored(EnumColor.PINK, new Object[]{fluidStack, EnumColor.GRAY, TextUtils.format((long)fluidStack.getAmount())})
         );
      }

      if (Attribute.has(this.m_40614_(), Attributes.AttributeInventory.class) && stack.m_41720_() instanceof IItemSustainedInventory inventory) {
         tooltip.add(
            MekanismLang.HAS_INVENTORY
               .translateColored(EnumColor.AQUA, new Object[]{EnumColor.GRAY, BooleanStateDisplay.YesNo.of(inventory.hasSustainedInventory(stack))})
         );
      }

      if (Attribute.has(this.m_40614_(), AttributeUpgradeSupport.class)) {
         MekanismUtils.addUpgradesToTooltip(stack, tooltip);
      }
   }

   protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (this.exposesEnergyCap(stack)) {
         StorageUtils.addStoredEnergy(stack, tooltip, false);
      }
   }
}
