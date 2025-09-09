package mekanism.common.item;

import java.util.List;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.security.item.ItemStackOwnerObject;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.item.PortableQIODashboardContainer;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemPortableQIODashboard extends CapabilityItem implements IFrequencyItem, IGuiItem, IItemSustainedInventory, IColoredItem {
   public ItemPortableQIODashboard(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.RARE));
   }

   public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
      InventoryUtils.dropItemContents(item, damageSource);
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      ISecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
      MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
      tooltip.add(
         MekanismLang.HAS_INVENTORY
            .translateColored(EnumColor.AQUA, new Object[]{EnumColor.GRAY, BooleanStateDisplay.YesNo.of(this.hasSustainedInventory(stack))})
      );
      super.m_7373_(stack, world, tooltip, flag);
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
      return SecurityUtils.get().claimOrOpenGui(world, player, hand, this.getContainerType()::tryOpenGui);
   }

   @Override
   public ContainerTypeRegistryObject<PortableQIODashboardContainer> getContainerType() {
      return MekanismContainerTypes.PORTABLE_QIO_DASHBOARD;
   }

   @Override
   public void setFrequency(ItemStack stack, Frequency frequency) {
      IFrequencyItem.super.setFrequency(stack, frequency);
      this.setColor(stack, frequency == null ? null : ((QIOFrequency)frequency).getColor());
   }

   @Override
   public FrequencyType<?> getFrequencyType() {
      return FrequencyType.QIO;
   }

   public void m_6883_(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
      super.m_6883_(stack, level, entity, slotId, isSelected);
      if (!level.f_46443_ && level.m_46467_() % 100L == 0L) {
         EnumColor frequencyColor = this.getFrequency(stack) instanceof QIOFrequency frequency ? frequency.getColor() : null;
         EnumColor color = this.getColor(stack);
         if (color != frequencyColor) {
            this.setColor(stack, frequencyColor);
         }
      }
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      capabilities.add(new ItemStackOwnerObject());
      super.gatherCapabilities(capabilities, stack, nbt);
   }
}
