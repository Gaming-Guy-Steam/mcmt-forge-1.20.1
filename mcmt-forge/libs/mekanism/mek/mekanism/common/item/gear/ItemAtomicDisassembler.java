package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMaps;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IDisableableEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.mekatool.ModuleExcavationEscalationUnit;
import mekanism.common.content.gear.mekatool.ModuleVeinMiningUnit;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.lib.radial.IRadialEnumModeItem;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemAtomicDisassembler
   extends ItemEnergized
   implements IItemHUDProvider,
   IRadialEnumModeItem<ItemAtomicDisassembler.DisassemblerMode>,
   IAttributeRefresher {
   public static final Set<ToolAction> ALWAYS_SUPPORTED_ACTIONS = Set.of(
      ToolActions.AXE_DIG, ToolActions.HOE_DIG, ToolActions.SHOVEL_DIG, ToolActions.PICKAXE_DIG, ToolActions.SWORD_DIG
   );
   private static final Lazy<RadialData<ItemAtomicDisassembler.DisassemblerMode>> LAZY_RADIAL_DATA = Lazy.of(
      () -> IRadialDataHelper.INSTANCE.dataForEnum(Mekanism.rl("disassembler_mode"), ItemAtomicDisassembler.DisassemblerMode.NORMAL)
   );
   private final AttributeCache attributeCache = new AttributeCache(
      this, MekanismConfig.gear.disassemblerMaxDamage, MekanismConfig.gear.disassemblerAttackSpeed
   );

   public static ItemStack fullyChargedStack() {
      ItemAtomicDisassembler disassembler = MekanismItems.ATOMIC_DISASSEMBLER.get();
      ItemStack stack = new ItemStack(disassembler);
      return StorageUtils.getFilledEnergyVariant(stack, disassembler.getMaxEnergy(stack));
   }

   public ItemAtomicDisassembler(Properties properties) {
      super(MekanismConfig.gear.disassemblerChargeRate, MekanismConfig.gear.disassemblerMaxEnergy, properties.m_41497_(Rarity.RARE).setNoRepair());
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.disassembler());
   }

   public boolean m_8096_(@NotNull BlockState state) {
      return true;
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      ItemAtomicDisassembler.DisassemblerMode mode = this.getMode(stack);
      tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, new Object[]{mode}));
      tooltip.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.INDIGO, new Object[]{mode.getEfficiency()}));
   }

   public boolean canPerformAction(ItemStack stack, ToolAction action) {
      if (ALWAYS_SUPPORTED_ACTIONS.contains(action)) {
         IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
         if (energyContainer != null) {
            FloatingLong energyRequired = this.getDestroyEnergy(stack, 0.0F);
            FloatingLong energyAvailable = energyContainer.getEnergy();
            return energyRequired.smallerOrEqual(energyAvailable) || !energyAvailable.divide(energyRequired).isZero();
         }
      }

      return false;
   }

   public boolean m_7579_(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer != null && !energyContainer.isEmpty()) {
         energyContainer.extract(MekanismConfig.gear.disassemblerEnergyUsageWeapon.get(), Action.EXECUTE, AutomationType.MANUAL);
      }

      return true;
   }

   public float m_8102_(@NotNull ItemStack stack, @NotNull BlockState state) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer == null) {
         return 0.0F;
      } else {
         FloatingLong energyRequired = this.getDestroyEnergy(stack, state.f_60599_);
         FloatingLong energyAvailable = energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL);
         return energyAvailable.smallerThan(energyRequired)
            ? ItemAtomicDisassembler.DisassemblerMode.NORMAL.getEfficiency() * energyAvailable.divide(energyRequired).floatValue()
            : this.getMode(stack).getEfficiency();
      }
   }

   public boolean m_6813_(@NotNull ItemStack stack, @NotNull Level world, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity entityliving) {
      IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
      if (energyContainer != null) {
         energyContainer.extract(this.getDestroyEnergy(stack, state.m_60800_(world, pos)), Action.EXECUTE, AutomationType.MANUAL);
      }

      return true;
   }

   public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
      if (!player.m_9236_().f_46443_ && !player.m_7500_()) {
         IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
         if (energyContainer != null && this.getMode(stack) == ItemAtomicDisassembler.DisassemblerMode.VEIN) {
            Level world = player.m_9236_();
            BlockState state = world.m_8055_(pos);
            FloatingLong baseDestroyEnergy = this.getDestroyEnergy(stack);
            FloatingLong energyRequired = this.getDestroyEnergy(baseDestroyEnergy, state.m_60800_(world, pos));
            if (energyContainer.extract(energyRequired, Action.SIMULATE, AutomationType.MANUAL).greaterOrEqual(energyRequired)
               && ModuleVeinMiningUnit.canVeinBlock(state)
               && state.m_204336_(MekanismTags.Blocks.ATOMIC_DISASSEMBLER_ORE)) {
               Object2IntMap<BlockPos> found = ModuleVeinMiningUnit.findPositions(
                  world, Map.of(pos, state), 0, Reference2BooleanMaps.singleton(state.m_60734_(), true)
               );
               MekanismUtils.veinMineArea(
                  energyContainer,
                  energyRequired,
                  world,
                  pos,
                  (ServerPlayer)player,
                  stack,
                  this,
                  found,
                  hardness -> FloatingLong.ZERO,
                  (hardness, distance, bs) -> this.getDestroyEnergy(baseDestroyEnergy, hardness).multiply(0.5 * Math.pow(distance, 1.5))
               );
            }
         }

         return super.onBlockStartBreak(stack, pos, player);
      } else {
         return super.onBlockStartBreak(stack, pos, player);
      }
   }

   private FloatingLong getDestroyEnergy(ItemStack itemStack, float hardness) {
      return this.getDestroyEnergy(this.getDestroyEnergy(itemStack), hardness);
   }

   private FloatingLong getDestroyEnergy(FloatingLong baseDestroyEnergy, float hardness) {
      return hardness == 0.0F ? baseDestroyEnergy.divide(2L) : baseDestroyEnergy;
   }

   private FloatingLong getDestroyEnergy(ItemStack itemStack) {
      return MekanismConfig.gear.disassemblerEnergyUsage.get().multiply((long)this.getMode(itemStack).getEfficiency());
   }

   @Override
   public String getModeSaveKey() {
      return "mode";
   }

   public ItemAtomicDisassembler.DisassemblerMode getModeByIndex(int ordinal) {
      return ItemAtomicDisassembler.DisassemblerMode.byIndexStatic(ordinal);
   }

   @NotNull
   @Override
   public RadialData<ItemAtomicDisassembler.DisassemblerMode> getRadialData(ItemStack stack) {
      return (RadialData<ItemAtomicDisassembler.DisassemblerMode>)LAZY_RADIAL_DATA.get();
   }

   @NotNull
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
      if (slot == EquipmentSlot.MAINHAND) {
         IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
         FloatingLong energy = energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
         FloatingLong energyCost = MekanismConfig.gear.disassemblerEnergyUsageWeapon.get();
         if (energy.greaterOrEqual(energyCost)) {
            return this.attributeCache.get();
         } else {
            int minDamage = MekanismConfig.gear.disassemblerMinDamage.get();
            int damageDifference = MekanismConfig.gear.disassemblerMaxDamage.get() - minDamage;
            double damage = minDamage + damageDifference * energy.divideToLevel(energyCost);
            Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(Attributes.f_22281_, new AttributeModifier(f_41374_, "Weapon modifier", damage, Operation.ADDITION));
            builder.put(
               Attributes.f_22283_, new AttributeModifier(f_41375_, "Weapon modifier", MekanismConfig.gear.disassemblerAttackSpeed.get(), Operation.ADDITION)
            );
            return builder.build();
         }
      } else {
         return super.getAttributeModifiers(slot, stack);
      }
   }

   @Override
   public void addToBuilder(Builder<Attribute, AttributeModifier> builder) {
      builder.put(Attributes.f_22281_, new AttributeModifier(f_41374_, "Weapon modifier", MekanismConfig.gear.disassemblerMaxDamage.get(), Operation.ADDITION));
      builder.put(
         Attributes.f_22283_, new AttributeModifier(f_41375_, "Weapon modifier", MekanismConfig.gear.disassemblerAttackSpeed.get(), Operation.ADDITION)
      );
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      ItemAtomicDisassembler.DisassemblerMode mode = this.getMode(stack);
      list.add(MekanismLang.MODE.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, mode}));
      list.add(MekanismLang.DISASSEMBLER_EFFICIENCY.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, mode.getEfficiency()}));
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      ItemAtomicDisassembler.DisassemblerMode mode = this.getMode(stack);
      ItemAtomicDisassembler.DisassemblerMode newMode = mode.adjust(shift);
      if (mode != newMode) {
         this.setMode(stack, player, newMode);
         displayChange.sendMessage(
            player, () -> MekanismLang.DISASSEMBLER_MODE_CHANGE.translate(new Object[]{EnumColor.INDIGO, newMode, EnumColor.AQUA, newMode.getEfficiency()})
         );
      }
   }

   @NotNull
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      ItemAtomicDisassembler.DisassemblerMode mode = this.getMode(stack);
      return MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.INDIGO, new Object[]{mode, EnumColor.AQUA, mode.getEfficiency()});
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
   public static enum DisassemblerMode implements IDisableableEnum<ItemAtomicDisassembler.DisassemblerMode>, IHasTextComponent, IRadialMode {
      NORMAL(MekanismLang.RADIAL_EXCAVATION_SPEED_NORMAL, 20, () -> true, EnumColor.BRIGHT_GREEN, ModuleExcavationEscalationUnit.ExcavationMode.NORMAL.icon()),
      SLOW(
         MekanismLang.RADIAL_EXCAVATION_SPEED_SLOW,
         8,
         MekanismConfig.gear.disassemblerSlowMode,
         EnumColor.PINK,
         ModuleExcavationEscalationUnit.ExcavationMode.SLOW.icon()
      ),
      FAST(
         MekanismLang.RADIAL_EXCAVATION_SPEED_FAST,
         128,
         MekanismConfig.gear.disassemblerFastMode,
         EnumColor.RED,
         ModuleExcavationEscalationUnit.ExcavationMode.EXTREME.icon()
      ),
      VEIN(
         MekanismLang.RADIAL_VEIN_NORMAL,
         20,
         MekanismConfig.gear.disassemblerVeinMining,
         EnumColor.AQUA,
         MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "vein_normal.png")
      ),
      OFF(MekanismLang.RADIAL_EXCAVATION_SPEED_OFF, 0, () -> true, EnumColor.WHITE, ModuleExcavationEscalationUnit.ExcavationMode.OFF.icon());

      private static final ItemAtomicDisassembler.DisassemblerMode[] MODES = values();
      private final BooleanSupplier checkEnabled;
      private final ILangEntry langEntry;
      private final int efficiency;
      private final EnumColor color;
      private final ResourceLocation icon;

      private DisassemblerMode(ILangEntry langEntry, int efficiency, BooleanSupplier checkEnabled, EnumColor color, ResourceLocation icon) {
         this.langEntry = langEntry;
         this.efficiency = efficiency;
         this.checkEnabled = checkEnabled;
         this.color = color;
         this.icon = icon;
      }

      public static ItemAtomicDisassembler.DisassemblerMode byIndexStatic(int index) {
         ItemAtomicDisassembler.DisassemblerMode mode = MathUtils.getByIndexMod(MODES, index);
         return mode.isEnabled() ? mode : NORMAL;
      }

      public ItemAtomicDisassembler.DisassemblerMode byIndex(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translate(this.color);
      }

      @NotNull
      @Override
      public Component sliceName() {
         return this.getTextComponent();
      }

      public int getEfficiency() {
         return this.efficiency;
      }

      @Override
      public boolean isEnabled() {
         return this.checkEnabled.getAsBoolean();
      }

      @NotNull
      @Override
      public ResourceLocation icon() {
         return this.icon;
      }

      @Override
      public EnumColor color() {
         return this.color;
      }
   }
}
