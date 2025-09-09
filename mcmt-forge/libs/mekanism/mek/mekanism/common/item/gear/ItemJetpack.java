package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.providers.IGasProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedLongValue;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemJetpack extends ItemGasArmor implements IItemHUDProvider, IModeItem, IJetpackItem {
   private static final ItemJetpack.JetpackMaterial JETPACK_MATERIAL = new ItemJetpack.JetpackMaterial();

   public ItemJetpack(Properties properties) {
      this(JETPACK_MATERIAL, properties);
   }

   public ItemJetpack(ArmorMaterial material, Properties properties) {
      super(material, Type.CHESTPLATE, properties.setNoRepair());
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.jetpack());
   }

   @Override
   protected CachedLongValue getMaxGas() {
      return MekanismConfig.gear.jetpackMaxGas;
   }

   @Override
   protected LongSupplier getFillRate() {
      return MekanismConfig.gear.jetpackFillRate;
   }

   @Override
   protected IGasProvider getGasType() {
      return MekanismGases.HYDROGEN;
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, new Object[]{this.getJetpackMode(stack).getTextComponent()}));
   }

   @Override
   public boolean canUseJetpack(ItemStack stack) {
      return this.hasGas(stack);
   }

   @Override
   public IJetpackItem.JetpackMode getJetpackMode(ItemStack stack) {
      return IJetpackItem.JetpackMode.byIndexStatic(ItemDataUtils.getInt(stack, "mode"));
   }

   @Override
   public void useJetpackFuel(ItemStack stack) {
      this.useGas(stack, 1L);
   }

   public void setMode(ItemStack stack, IJetpackItem.JetpackMode mode) {
      ItemDataUtils.setInt(stack, "mode", mode.ordinal());
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      if (slotType == this.m_40402_()) {
         ItemJetpack jetpack = (ItemJetpack)stack.m_41720_();
         list.add(MekanismLang.JETPACK_MODE.translateColored(EnumColor.DARK_GRAY, new Object[]{jetpack.getJetpackMode(stack)}));
         GasStack stored = GasStack.EMPTY;
         Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
         if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getTanks() > 0) {
               stored = gasHandlerItem.getChemicalInTank(0);
            }
         }

         list.add(MekanismLang.JETPACK_STORED.translateColored(EnumColor.DARK_GRAY, new Object[]{EnumColor.ORANGE, stored.getAmount()}));
      }
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      IJetpackItem.JetpackMode mode = this.getJetpackMode(stack);
      IJetpackItem.JetpackMode newMode = mode.adjust(shift);
      if (mode != newMode) {
         this.setMode(stack, newMode);
         displayChange.sendMessage(player, () -> MekanismLang.JETPACK_MODE_CHANGE.translate(new Object[]{newMode}));
      }
   }

   @Override
   public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return slotType == this.m_40402_();
   }

   public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
      return this instanceof ItemArmoredJetpack
         ? super.getDefaultTooltipHideFlags(stack)
         : super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.m_41809_();
   }

   @NothingNullByDefault
   protected static class JetpackMaterial extends BaseSpecialArmorMaterial {
      public String m_6082_() {
         return "mekanism:jetpack";
      }
   }
}
