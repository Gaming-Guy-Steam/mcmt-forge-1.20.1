package mekanism.common.item;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedFloatingLongValue;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemEnergized extends CapabilityItem implements CreativeTabDeferredRegister.ICustomCreativeTabContents {
   private final FloatingLongSupplier chargeRateSupplier;
   private final FloatingLongSupplier maxEnergySupplier;
   private final Predicate<AutomationType> canExtract;
   private final Predicate<AutomationType> canInsert;

   public ItemEnergized(FloatingLongSupplier chargeRateSupplier, FloatingLongSupplier maxEnergySupplier, Properties properties) {
      this(chargeRateSupplier, maxEnergySupplier, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue, properties);
   }

   public ItemEnergized(
      FloatingLongSupplier chargeRateSupplier,
      FloatingLongSupplier maxEnergySupplier,
      Predicate<AutomationType> canExtract,
      Predicate<AutomationType> canInsert,
      Properties properties
   ) {
      super(properties.m_41487_(1));
      this.chargeRateSupplier = chargeRateSupplier;
      this.maxEnergySupplier = maxEnergySupplier;
      this.canExtract = canExtract;
      this.canInsert = canInsert;
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getEnergyBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return MekanismConfig.client.energyColor.get();
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredEnergy(stack, tooltip, true);
   }

   @Override
   public void addItems(Output tabOutput) {
      if (this.maxEnergySupplier instanceof CachedFloatingLongValue configValue) {
         tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), configValue));
      } else {
         tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), this.maxEnergySupplier.get()));
      }
   }

   protected FloatingLong getMaxEnergy(ItemStack stack) {
      return this.maxEnergySupplier.get();
   }

   protected FloatingLong getChargeRate(ItemStack stack) {
      return this.chargeRateSupplier.get();
   }

   @Override
   protected boolean areCapabilityConfigsLoaded() {
      return super.areCapabilityConfigsLoaded() && MekanismConfig.gear.isLoaded();
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(RateLimitEnergyHandler.create(() -> this.getChargeRate(stack), () -> this.getMaxEnergy(stack), this.canExtract, this.canInsert));
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return slotChanged || oldStack.m_41720_() != newStack.m_41720_();
   }

   public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
      return oldStack.m_41720_() != newStack.m_41720_();
   }
}
