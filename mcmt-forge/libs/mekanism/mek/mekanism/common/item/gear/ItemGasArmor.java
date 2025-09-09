package mekanism.common.item.gear;

import java.util.List;
import java.util.function.LongSupplier;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.providers.IGasProvider;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemGasArmor extends ItemSpecialArmor implements IGasItem, CreativeTabDeferredRegister.ICustomCreativeTabContents {
   protected ItemGasArmor(ArmorMaterial material, Type armorType, Properties properties) {
      super(material, armorType, properties.m_41497_(Rarity.RARE).setNoRepair().m_41487_(1));
   }

   protected abstract CachedLongValue getMaxGas();

   protected abstract LongSupplier getFillRate();

   protected abstract IGasProvider getGasType();

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredGas(stack, tooltip, true, false);
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return ChemicalUtil.getRGBDurabilityForDisplay(stack);
   }

   @Override
   public void addItems(Output tabOutput) {
      tabOutput.m_246342_(ChemicalUtil.getFilledVariant(new ItemStack(this), this.getMaxGas(), this.getGasType()));
   }

   @Override
   protected boolean areCapabilityConfigsLoaded() {
      return super.areCapabilityConfigsLoaded() && MekanismConfig.gear.isLoaded();
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(
         RateLimitGasHandler.create(
            this.getFillRate(),
            this.getMaxGas(),
            ChemicalTankBuilder.GAS.notExternal,
            ChemicalTankBuilder.GAS.alwaysTrueBi,
            gas -> gas == this.getGasType().getChemical()
         )
      );
   }
}
