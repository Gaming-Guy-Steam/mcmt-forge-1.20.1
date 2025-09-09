package mekanism.common.item.block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.ITier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.security.item.ItemStackSecurityObject;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemBlockMekanism<BLOCK extends Block> extends BlockItem {
   @NotNull
   private final BLOCK block;

   public ItemBlockMekanism(@NotNull BLOCK block, Properties properties) {
      super(block, properties);
      this.block = block;
   }

   @NotNull
   public BLOCK m_40614_() {
      return this.block;
   }

   public ITier getTier() {
      return null;
   }

   public TextColor getTextColor(ItemStack stack) {
      ITier tier = this.getTier();
      return tier == null ? null : tier.getBaseTier().getColor();
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      TextColor color = this.getTextColor(stack);
      return (Component)(color == null ? super.m_7626_(stack) : TextComponentUtil.build(color, super.m_7626_(stack)));
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return this.exposesEnergyCap(oldStack) && this.exposesEnergyCap(newStack)
         ? slotChanged || oldStack.m_41720_() != newStack.m_41720_()
         : super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return this.exposesEnergyCap(oldStack) && this.exposesEnergyCap(newStack)
         ? oldStack.m_41720_() != newStack.m_41720_()
         : super.shouldCauseBlockBreakReset(oldStack, newStack);
   }

   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      if (Attribute.has(this.block, Attributes.AttributeSecurity.class)) {
         capabilities.add(new ItemStackSecurityObject());
      }

      if (this.exposesEnergyCap(stack)) {
         AttributeEnergy attributeEnergy = Attribute.get(this.block, AttributeEnergy.class);
         FloatingLongSupplier maxEnergy;
         if (Attribute.matches(this.block, AttributeUpgradeSupport.class, attribute -> attribute.supportedUpgrades().contains(Upgrade.ENERGY))) {
            maxEnergy = new ItemBlockMekanism.UpgradeBasedFloatingLongCache(stack, attributeEnergy::getStorage);
         } else {
            maxEnergy = attributeEnergy::getStorage;
         }

         capabilities.add(RateLimitEnergyHandler.create(maxEnergy, BasicEnergyContainer.manualOnly, this.getEnergyCapInsertPredicate()));
      }
   }

   protected Predicate<AutomationType> getEnergyCapInsertPredicate() {
      return BasicEnergyContainer.alwaysTrue;
   }

   protected boolean exposesEnergyCap(ItemStack stack) {
      return Attribute.has(this.block, AttributeEnergy.class) && !stack.m_41753_();
   }

   protected boolean areCapabilityConfigsLoaded(ItemStack stack) {
      return !this.exposesEnergyCap(stack) ? true : MekanismConfig.storage.isLoaded() && MekanismConfig.usage.isLoaded();
   }

   public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
      if (!this.areCapabilityConfigsLoaded(stack)) {
         return super.initCapabilities(stack, nbt);
      } else {
         List<ItemCapabilityWrapper.ItemCapability> capabilities = new ArrayList<>();
         this.gatherCapabilities(capabilities, stack, nbt);
         return (ICapabilityProvider)(capabilities.isEmpty()
            ? super.initCapabilities(stack, nbt)
            : new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapabilityWrapper.ItemCapability[]::new)));
      }
   }

   private static class UpgradeBasedFloatingLongCache implements FloatingLongSupplier {
      private final ItemStack stack;
      private final FloatingLongSupplier baseStorage;
      @Nullable
      private CompoundTag lastNBT;
      private FloatingLong value;

      private UpgradeBasedFloatingLongCache(ItemStack stack, FloatingLongSupplier baseStorage) {
         this.stack = stack;
         if (ItemDataUtils.hasData(stack, "componentUpgrade", 10)) {
            this.lastNBT = ItemDataUtils.getCompound(stack, "componentUpgrade").m_6426_();
         } else {
            this.lastNBT = null;
         }

         this.baseStorage = baseStorage;
         this.value = MekanismUtils.getMaxEnergy(this.stack, this.baseStorage.get());
      }

      @NotNull
      @Override
      public FloatingLong get() {
         if (ItemDataUtils.hasData(this.stack, "componentUpgrade", 10)) {
            CompoundTag upgrades = ItemDataUtils.getCompound(this.stack, "componentUpgrade");
            if (this.lastNBT == null || !this.lastNBT.equals(upgrades)) {
               this.lastNBT = upgrades.m_6426_();
               this.value = MekanismUtils.getMaxEnergy(this.stack, this.baseStorage.get());
            }
         } else if (this.lastNBT != null) {
            this.lastNBT = null;
            this.value = MekanismUtils.getMaxEnergy(this.stack, this.baseStorage.get());
         }

         return this.value;
      }
   }
}
