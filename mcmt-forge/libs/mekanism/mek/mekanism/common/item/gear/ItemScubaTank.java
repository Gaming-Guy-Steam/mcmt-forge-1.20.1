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
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemScubaTank extends ItemGasArmor implements IItemHUDProvider, IModeItem {
   private static final ItemScubaTank.ScubaTankMaterial SCUBA_TANK_MATERIAL = new ItemScubaTank.ScubaTankMaterial();

   public ItemScubaTank(Properties properties) {
      super(SCUBA_TANK_MATERIAL, Type.CHESTPLATE, properties);
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.scubaTank());
   }

   @Override
   protected CachedLongValue getMaxGas() {
      return MekanismConfig.gear.scubaMaxGas;
   }

   @Override
   protected LongSupplier getFillRate() {
      return MekanismConfig.gear.scubaFillRate;
   }

   @Override
   protected IGasProvider getGasType() {
      return MekanismGases.OXYGEN;
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      tooltip.add(MekanismLang.FLOWING.translateColored(EnumColor.GRAY, new Object[]{BooleanStateDisplay.YesNo.of(this.getFlowing(stack), true)}));
   }

   public boolean getFlowing(ItemStack stack) {
      return ItemDataUtils.getBoolean(stack, "running");
   }

   public void setFlowing(ItemStack stack, boolean flowing) {
      ItemDataUtils.setBoolean(stack, "running", flowing);
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      if (slotType == this.m_40402_()) {
         ItemScubaTank scubaTank = (ItemScubaTank)stack.m_41720_();
         list.add(
            MekanismLang.SCUBA_TANK_MODE.translateColored(EnumColor.DARK_GRAY, new Object[]{BooleanStateDisplay.OnOff.of(scubaTank.getFlowing(stack), true)})
         );
         GasStack stored = GasStack.EMPTY;
         Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
         if (capability.isPresent()) {
            IGasHandler gasHandlerItem = capability.get();
            if (gasHandlerItem.getTanks() > 0) {
               stored = gasHandlerItem.getChemicalInTank(0);
            }
         }

         list.add(MekanismLang.GENERIC_STORED.translateColored(EnumColor.DARK_GRAY, new Object[]{MekanismGases.OXYGEN, EnumColor.ORANGE, stored.getAmount()}));
      }
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      if (Math.abs(shift) % 2 == 1) {
         boolean newState = !this.getFlowing(stack);
         this.setFlowing(stack, newState);
         displayChange.sendMessage(player, () -> MekanismLang.FLOWING.translate(new Object[]{BooleanStateDisplay.OnOff.of(newState, true)}));
      }
   }

   public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
      return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.m_41809_();
   }

   @Override
   public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return slotType == this.m_40402_();
   }

   @NothingNullByDefault
   protected static class ScubaTankMaterial extends BaseSpecialArmorMaterial {
      public String m_6082_() {
         return "mekanism:scuba_tank";
      }
   }
}
