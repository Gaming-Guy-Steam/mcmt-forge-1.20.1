package mekanism.common.item;

import java.util.List;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.robit.RobitSkin;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.security.item.ItemStackSecurityObject;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.TileEntityChargepad;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class ItemRobit extends ItemEnergized implements IItemSustainedInventory {
   public ItemRobit(Properties properties) {
      super(() -> EntityRobit.MAX_ENERGY.multiply(0.005), () -> EntityRobit.MAX_ENERGY, properties.m_41497_(Rarity.RARE));
   }

   public void onDestroyed(@NotNull ItemEntity item, @NotNull DamageSource damageSource) {
      InventoryUtils.dropItemContents(item, damageSource);
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      tooltip.add(MekanismLang.ROBIT_NAME.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, this.getRobitName(stack)}));
      tooltip.add(
         MekanismLang.ROBIT_SKIN.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, RobitSkin.getTranslatedName(this.getRobitSkin(stack))})
      );
      ISecurityUtils.INSTANCE.addSecurityTooltip(stack, tooltip);
      tooltip.add(
         MekanismLang.HAS_INVENTORY
            .translateColored(EnumColor.AQUA, new Object[]{EnumColor.GRAY, BooleanStateDisplay.YesNo.of(this.hasSustainedInventory(stack))})
      );
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         TileEntityMekanism chargepad = WorldUtils.getTileEntity(TileEntityChargepad.class, world, pos);
         if (chargepad != null && !chargepad.getActive()) {
            if (!world.f_46443_) {
               ItemStack stack = context.m_43722_();
               EntityRobit robit = EntityRobit.create(world, pos.m_123341_() + 0.5, pos.m_123342_() + 0.1, pos.m_123343_() + 0.5);
               if (robit == null) {
                  return InteractionResult.FAIL;
               }

               robit.setHome(chargepad.getTileCoord());
               IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
               if (energyContainer != null) {
                  robit.getEnergyContainer().setEnergy(energyContainer.getEnergy());
               }

               ISecurityUtils securityUtils = ISecurityUtils.INSTANCE;
               UUID ownerUUID = securityUtils.getOwnerUUID(stack);
               if (ownerUUID == null) {
                  robit.setOwnerUUID(player.m_20148_());
                  Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.m_20148_()));
               } else {
                  robit.setOwnerUUID(ownerUUID);
               }

               robit.setSustainedInventory(this.getSustainedInventory(stack));
               robit.m_6593_(this.getRobitName(stack));
               robit.setSecurityMode(stack.getCapability(Capabilities.SECURITY_OBJECT).map(ISecurityObject::getSecurityMode).orElse(SecurityMode.PUBLIC));
               robit.setSkin(this.getRobitSkin(stack), player);
               world.m_7967_(robit);
               world.m_142346_(player, GameEvent.f_157810_, robit.m_20183_());
               stack.m_41774_(1);
               CriteriaTriggers.f_10580_.m_68256_((ServerPlayer)player, robit);
            }

            return InteractionResult.m_19078_(world.f_46443_);
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public void setName(ItemStack stack, Component name) {
      ItemDataUtils.setString(stack, "name", Serializer.m_130703_(name));
   }

   private Component getRobitName(ItemStack stack) {
      String name = ItemDataUtils.getString(stack, "name");
      return name.isEmpty() ? MekanismLang.ROBIT.translate(new Object[0]) : Serializer.m_130701_(name);
   }

   public void setSkin(ItemStack stack, ResourceKey<RobitSkin> skin) {
      ItemDataUtils.setString(stack, "skin", skin.m_135782_().toString());
   }

   public ResourceKey<RobitSkin> getRobitSkin(ItemStack stack) {
      String skin = ItemDataUtils.getString(stack, "skin");
      if (!skin.isEmpty()) {
         ResourceLocation rl = ResourceLocation.m_135820_(skin);
         if (rl != null) {
            return ResourceKey.m_135785_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, rl);
         }
      }

      return MekanismRobitSkins.BASE;
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      capabilities.add(new ItemStackSecurityObject());
      super.gatherCapabilities(capabilities, stack, nbt);
   }
}
