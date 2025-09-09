package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.item.RateLimitGasHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.CapabilityItem;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismGases;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFlamethrower extends CapabilityItem implements IItemHUDProvider, IModeItem, IGasItem, CreativeTabDeferredRegister.ICustomCreativeTabContents {
   public ItemFlamethrower(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.RARE).setNoRepair());
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.flamethrower());
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredGas(stack, tooltip, true, false);
      tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, new Object[]{this.getMode(stack)}));
   }

   public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
      return slotChanged || oldStack.m_41720_() != newStack.m_41720_();
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
      tabOutput.m_246342_(ChemicalUtil.getFilledVariant(new ItemStack(this), MekanismConfig.gear.flamethrowerMaxGas, MekanismGases.HYDROGEN));
   }

   public ItemFlamethrower.FlamethrowerMode getMode(ItemStack stack) {
      return ItemFlamethrower.FlamethrowerMode.byIndexStatic(ItemDataUtils.getInt(stack, "mode"));
   }

   public void setMode(ItemStack stack, ItemFlamethrower.FlamethrowerMode mode) {
      ItemDataUtils.setInt(stack, "mode", mode.ordinal());
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
            MekanismConfig.gear.flamethrowerFillRate,
            MekanismConfig.gear.flamethrowerMaxGas,
            ChemicalTankBuilder.GAS.notExternal,
            ChemicalTankBuilder.GAS.alwaysTrueBi,
            gas -> gas == MekanismGases.HYDROGEN.getChemical()
         )
      );
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      boolean hasGas = false;
      Optional<IGasHandler> capability = stack.getCapability(Capabilities.GAS_HANDLER).resolve();
      if (capability.isPresent()) {
         IGasHandler gasHandlerItem = capability.get();
         if (gasHandlerItem.getTanks() > 0) {
            GasStack storedGas = gasHandlerItem.getChemicalInTank(0);
            if (!storedGas.isEmpty()) {
               list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, new Object[]{EnumColor.ORANGE, storedGas.getAmount()}));
               hasGas = true;
            }
         }
      }

      if (!hasGas) {
         list.add(MekanismLang.FLAMETHROWER_STORED.translateColored(EnumColor.GRAY, new Object[]{EnumColor.ORANGE, MekanismLang.NO_GAS}));
      }

      list.add(MekanismLang.MODE.translate(new Object[]{this.getMode(stack)}));
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      ItemFlamethrower.FlamethrowerMode mode = this.getMode(stack);
      ItemFlamethrower.FlamethrowerMode newMode = mode.adjust(shift);
      if (mode != newMode) {
         this.setMode(stack, newMode);
         displayChange.sendMessage(player, () -> MekanismLang.FLAMETHROWER_MODE_CHANGE.translate(new Object[]{newMode}));
      }
   }

   @NotNull
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      return this.getMode(stack).getTextComponent();
   }

   public boolean m_8120_(@NotNull ItemStack stack) {
      return false;
   }

   public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
      return false;
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return false;
   }

   @NothingNullByDefault
   public static enum FlamethrowerMode implements IIncrementalEnum<ItemFlamethrower.FlamethrowerMode>, IHasTextComponent {
      COMBAT(MekanismLang.FLAMETHROWER_COMBAT, EnumColor.YELLOW),
      HEAT(MekanismLang.FLAMETHROWER_HEAT, EnumColor.ORANGE),
      INFERNO(MekanismLang.FLAMETHROWER_INFERNO, EnumColor.DARK_RED);

      private static final ItemFlamethrower.FlamethrowerMode[] MODES = values();
      private final ILangEntry langEntry;
      private final EnumColor color;

      private FlamethrowerMode(ILangEntry langEntry, EnumColor color) {
         this.langEntry = langEntry;
         this.color = color;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translateColored(this.color);
      }

      public ItemFlamethrower.FlamethrowerMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public static ItemFlamethrower.FlamethrowerMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
