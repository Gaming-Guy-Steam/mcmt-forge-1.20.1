package mekanism.common.item.gear;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFreeRunners extends ItemSpecialArmor implements IItemHUDProvider, IModeItem, CreativeTabDeferredRegister.ICustomCreativeTabContents {
   private static final ItemFreeRunners.FreeRunnerMaterial FREE_RUNNER_MATERIAL = new ItemFreeRunners.FreeRunnerMaterial();

   public ItemFreeRunners(Properties properties) {
      this(FREE_RUNNER_MATERIAL, properties);
   }

   public ItemFreeRunners(ArmorMaterial material, Properties properties) {
      super(material, Type.BOOTS, properties.m_41497_(Rarity.RARE).setNoRepair());
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.freeRunners());
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredEnergy(stack, tooltip, true);
      tooltip.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, new Object[]{this.getMode(stack).getTextComponent()}));
   }

   @Override
   public void addItems(Output tabOutput) {
      tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MekanismConfig.gear.freeRunnerMaxEnergy));
   }

   public boolean canWalkOnPowderedSnow(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
      return true;
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

   @Override
   protected boolean areCapabilityConfigsLoaded() {
      return super.areCapabilityConfigsLoaded() && MekanismConfig.gear.isLoaded();
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(
         RateLimitEnergyHandler.create(
            MekanismConfig.gear.freeRunnerChargeRate, MekanismConfig.gear.freeRunnerMaxEnergy, BasicEnergyContainer.manualOnly, BasicEnergyContainer.alwaysTrue
         )
      );
   }

   public ItemFreeRunners.FreeRunnerMode getMode(ItemStack itemStack) {
      return ItemFreeRunners.FreeRunnerMode.byIndexStatic(ItemDataUtils.getInt(itemStack, "mode"));
   }

   public void setMode(ItemStack itemStack, ItemFreeRunners.FreeRunnerMode mode) {
      ItemDataUtils.setInt(itemStack, "mode", mode.ordinal());
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      if (slotType == this.m_40402_()) {
         list.add(MekanismLang.FREE_RUNNERS_MODE.translateColored(EnumColor.GRAY, new Object[]{this.getMode(stack).getTextComponent()}));
         StorageUtils.addStoredEnergy(stack, list, true, MekanismLang.FREE_RUNNERS_STORED);
      }
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      ItemFreeRunners.FreeRunnerMode mode = this.getMode(stack);
      ItemFreeRunners.FreeRunnerMode newMode = mode.adjust(shift);
      if (mode != newMode) {
         this.setMode(stack, newMode);
         displayChange.sendMessage(player, () -> MekanismLang.FREE_RUNNER_MODE_CHANGE.translate(new Object[]{newMode}));
      }
   }

   @Override
   public boolean supportsSlotType(ItemStack stack, @NotNull EquipmentSlot slotType) {
      return slotType == this.m_40402_();
   }

   public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
      return this instanceof ItemArmoredFreeRunners
         ? super.getDefaultTooltipHideFlags(stack)
         : super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.m_41809_();
   }

   @NothingNullByDefault
   protected static class FreeRunnerMaterial extends BaseSpecialArmorMaterial {
      public String m_6082_() {
         return "mekanism:free_runners";
      }
   }

   @NothingNullByDefault
   public static enum FreeRunnerMode implements IIncrementalEnum<ItemFreeRunners.FreeRunnerMode>, IHasTextComponent {
      NORMAL(MekanismLang.FREE_RUNNER_NORMAL, EnumColor.DARK_GREEN, true, true),
      SAFETY(MekanismLang.FREE_RUNNER_SAFETY, EnumColor.ORANGE, true, false),
      DISABLED(MekanismLang.FREE_RUNNER_DISABLED, EnumColor.DARK_RED, false, false);

      private static final ItemFreeRunners.FreeRunnerMode[] MODES = values();
      private final boolean preventsFallDamage;
      private final boolean providesStepBoost;
      private final ILangEntry langEntry;
      private final EnumColor color;

      private FreeRunnerMode(ILangEntry langEntry, EnumColor color, boolean preventsFallDamage, boolean providesStepBoost) {
         this.preventsFallDamage = preventsFallDamage;
         this.providesStepBoost = providesStepBoost;
         this.langEntry = langEntry;
         this.color = color;
      }

      public boolean preventsFallDamage() {
         return this.preventsFallDamage;
      }

      public boolean providesStepBoost() {
         return this.providesStepBoost;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translateColored(this.color);
      }

      public ItemFreeRunners.FreeRunnerMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public static ItemFreeRunners.FreeRunnerMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
