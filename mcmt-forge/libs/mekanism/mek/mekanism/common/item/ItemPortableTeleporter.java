package mekanism.common.item;

import java.util.List;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.security.item.ItemStackOwnerObject;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemPortableTeleporter extends ItemEnergized implements IFrequencyItem, IGuiItem {
   public ItemPortableTeleporter(Properties properties) {
      super(MekanismConfig.gear.portableTeleporterChargeRate, MekanismConfig.gear.portableTeleporterMaxEnergy, properties.m_41497_(Rarity.RARE));
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      ISecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
      MekanismUtils.addFrequencyItemTooltip(stack, tooltip);
      super.m_7373_(stack, world, tooltip, flag);
   }

   @Override
   public FrequencyType<?> getFrequencyType() {
      return FrequencyType.TELEPORTER;
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
      return SecurityUtils.get().claimOrOpenGui(world, player, hand, this.getContainerType()::tryOpenGui);
   }

   @Override
   public ContainerTypeRegistryObject<?> getContainerType() {
      return MekanismContainerTypes.PORTABLE_TELEPORTER;
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      capabilities.add(new ItemStackOwnerObject());
      super.gatherCapabilities(capabilities, stack, nbt);
   }
}
