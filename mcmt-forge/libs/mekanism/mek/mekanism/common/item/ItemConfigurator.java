package mekanism.common.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IConfigurable;
import mekanism.api.IIncrementalEnum;
import mekanism.api.RelativeSide;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.api.radial.IRadialDataHelper;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.radial.IRadialEnumModeItem;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.ConfigInfo;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurator extends ItemEnergized implements IRadialEnumModeItem<ItemConfigurator.ConfiguratorMode>, IItemHUDProvider {
   public static final Lazy<RadialData<ItemConfigurator.ConfiguratorMode>> LAZY_RADIAL_DATA = Lazy.of(
      () -> IRadialDataHelper.INSTANCE.dataForEnum(Mekanism.rl("configurator_mode"), ItemConfigurator.ConfiguratorMode.class)
   );

   public ItemConfigurator(Properties properties) {
      super(MekanismConfig.gear.configuratorChargeRate, MekanismConfig.gear.configuratorMaxEnergy, properties.m_41497_(Rarity.UNCOMMON));
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, world, tooltip, flag);
      tooltip.add(MekanismLang.STATE.translateColored(EnumColor.PINK, new Object[]{this.getMode(stack)}));
   }

   @NotNull
   public Component m_7626_(@NotNull ItemStack stack) {
      return TextComponentUtil.build(EnumColor.AQUA, super.m_7626_(stack));
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      Level world = context.m_43725_();
      if (!world.f_46443_ && player != null) {
         BlockPos pos = context.m_8083_();
         Direction side = context.m_43719_();
         ItemStack stack = context.m_43722_();
         BlockEntity tile = WorldUtils.getTileEntity(world, pos);
         ItemConfigurator.ConfiguratorMode mode = this.getMode(stack);
         if (mode.isConfigurating()) {
            TransmissionType transmissionType = Objects.requireNonNull(mode.getTransmission(), "Configurating state requires transmission type");
            if (tile instanceof ISideConfiguration config && config.getConfig().supports(transmissionType)) {
               ConfigInfo info = config.getConfig().getConfig(transmissionType);
               if (info != null) {
                  RelativeSide relativeSide = RelativeSide.fromDirections(config.getDirection(), side);
                  DataType dataType = info.getDataType(relativeSide);
                  if (!player.m_6144_()) {
                     player.m_5661_(
                        MekanismLang.CONFIGURATOR_VIEW_MODE
                           .translateColored(
                              EnumColor.GRAY, new Object[]{transmissionType, dataType.getColor(), dataType, dataType.getColor().getColoredName()}
                           ),
                        true
                     );
                  } else {
                     if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
                        return InteractionResult.FAIL;
                     }

                     if (!player.m_7500_()) {
                        IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                        FloatingLong energyPerConfigure = MekanismConfig.gear.configuratorEnergyPerConfigure.get();
                        if (energyContainer == null
                           || energyContainer.extract(energyPerConfigure, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerConfigure)) {
                           return InteractionResult.FAIL;
                        }

                        energyContainer.extract(energyPerConfigure, Action.EXECUTE, AutomationType.MANUAL);
                     }

                     dataType = info.incrementDataType(relativeSide);
                     if (dataType != dataType) {
                        player.m_5661_(
                           MekanismLang.CONFIGURATOR_TOGGLE_MODE
                              .translateColored(
                                 EnumColor.GRAY, new Object[]{transmissionType, dataType.getColor(), dataType, dataType.getColor().getColoredName()}
                              ),
                           true
                        );
                        config.getConfig().sideChanged(transmissionType, relativeSide);
                     }
                  }
               }

               return InteractionResult.SUCCESS;
            }

            if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
               return InteractionResult.FAIL;
            }

            Optional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE, side).resolve();
            if (capability.isPresent()) {
               IConfigurable config = capability.get();
               if (player.m_6144_()) {
                  return config.onSneakRightClick(player);
               }

               return config.onRightClick(player);
            }
         } else if (mode == ItemConfigurator.ConfiguratorMode.EMPTY) {
            if (tile instanceof IMekanismInventory inv && inv.hasInventory()) {
               if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
                  return InteractionResult.FAIL;
               }

               boolean creative = player.m_7500_();
               if (tile instanceof TileEntityBin bin && bin.getTier() == BinTier.CREATIVE) {
                  if (creative) {
                     bin.getBinSlot().setEmpty();
                     return InteractionResult.SUCCESS;
                  }

                  return InteractionResult.FAIL;
               }

               IEnergyContainer energyContainer = creative ? null : StorageUtils.getEnergyContainer(stack, 0);
               if (!creative && energyContainer == null) {
                  return InteractionResult.FAIL;
               }

               FloatingLong energyPerItemDump = MekanismConfig.gear.configuratorEnergyPerItem.get();

               for (IInventorySlot inventorySlot : inv.getInventorySlots(null)) {
                  if (!inventorySlot.isEmpty()) {
                     if (!creative) {
                        if (energyContainer.extract(energyPerItemDump, Action.SIMULATE, AutomationType.MANUAL).smallerThan(energyPerItemDump)) {
                           break;
                        }

                        energyContainer.extract(energyPerItemDump, Action.EXECUTE, AutomationType.MANUAL);
                     }

                     InventoryUtils.dropStack(inventorySlot.getStack().m_41777_(), slotStack -> Block.m_152435_(world, pos, side, slotStack));
                     inventorySlot.setEmpty();
                  }
               }

               return InteractionResult.SUCCESS;
            }
         } else {
            if (mode == ItemConfigurator.ConfiguratorMode.ROTATE) {
               if (tile instanceof TileEntityMekanism tileMekanism) {
                  if (!tileMekanism.isDirectional()) {
                     return InteractionResult.PASS;
                  }

                  if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
                     return InteractionResult.FAIL;
                  }

                  if (Attribute.matches(tileMekanism.getBlockType(), AttributeStateFacing.class, AttributeStateFacing::canRotate)) {
                     if (!player.m_6144_()) {
                        tileMekanism.setFacing(side);
                     } else if (player.m_6144_()) {
                        tileMekanism.setFacing(side.m_122424_());
                     }
                  }
               }

               return InteractionResult.SUCCESS;
            }

            if (mode == ItemConfigurator.ConfiguratorMode.WRENCH) {
               return InteractionResult.PASS;
            }
         }
      }

      return InteractionResult.PASS;
   }

   public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
      return this.getMode(stack) == ItemConfigurator.ConfiguratorMode.WRENCH;
   }

   @Override
   public void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      list.add(MekanismLang.MODE.translateColored(EnumColor.PINK, new Object[]{this.getMode(stack)}));
   }

   @Override
   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      ItemConfigurator.ConfiguratorMode mode = this.getMode(stack);
      ItemConfigurator.ConfiguratorMode newMode = mode.adjust(shift);
      if (mode != newMode) {
         this.setMode(stack, player, newMode);
         displayChange.sendMessage(player, () -> MekanismLang.CONFIGURE_STATE.translate(new Object[]{newMode}));
      }
   }

   @NotNull
   @Override
   public Component getScrollTextComponent(@NotNull ItemStack stack) {
      return this.getMode(stack).getTextComponent();
   }

   @Override
   public String getModeSaveKey() {
      return "state";
   }

   @NotNull
   @Override
   public RadialData<ItemConfigurator.ConfiguratorMode> getRadialData(ItemStack stack) {
      return (RadialData<ItemConfigurator.ConfiguratorMode>)LAZY_RADIAL_DATA.get();
   }

   public ItemConfigurator.ConfiguratorMode getModeByIndex(int ordinal) {
      return ItemConfigurator.ConfiguratorMode.byIndexStatic(ordinal);
   }

   @NothingNullByDefault
   public static enum ConfiguratorMode implements IIncrementalEnum<ItemConfigurator.ConfiguratorMode>, IHasTextComponent, IRadialMode {
      CONFIGURATE_ITEMS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ITEM, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_FLUIDS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.FLUID, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_GASES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.GAS, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_INFUSE_TYPES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.INFUSION, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_PIGMENTS(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.PIGMENT, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_SLURRIES(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.SLURRY, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_ENERGY(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.ENERGY, EnumColor.BRIGHT_GREEN, true, null),
      CONFIGURATE_HEAT(MekanismLang.CONFIGURATOR_CONFIGURATE, TransmissionType.HEAT, EnumColor.BRIGHT_GREEN, true, null),
      EMPTY(MekanismLang.CONFIGURATOR_EMPTY, null, EnumColor.DARK_RED, false, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "empty.png")),
      ROTATE(MekanismLang.CONFIGURATOR_ROTATE, null, EnumColor.YELLOW, false, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "rotate.png")),
      WRENCH(MekanismLang.CONFIGURATOR_WRENCH, null, EnumColor.PINK, false, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_RADIAL, "wrench.png"));

      private static final ItemConfigurator.ConfiguratorMode[] MODES = values();
      private final ILangEntry langEntry;
      @Nullable
      private final TransmissionType transmissionType;
      private final EnumColor color;
      private final boolean configurating;
      private final ResourceLocation icon;

      private ConfiguratorMode(
         ILangEntry langEntry, @Nullable TransmissionType transmissionType, EnumColor color, boolean configurating, @Nullable ResourceLocation icon
      ) {
         this.langEntry = langEntry;
         this.transmissionType = transmissionType;
         this.color = color;
         this.configurating = configurating;
         if (transmissionType == null) {
            this.icon = Objects.requireNonNull(icon, "Icon should only be null if there is a transmission type present.");
         } else {
            this.icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, transmissionType.getTransmission() + ".png");
         }
      }

      @Override
      public Component getTextComponent() {
         return this.transmissionType == null
            ? this.langEntry.translateColored(this.color)
            : this.langEntry.translateColored(this.color, this.transmissionType);
      }

      @Override
      public EnumColor color() {
         return this.color;
      }

      public boolean isConfigurating() {
         return this.configurating;
      }

      @Nullable
      public TransmissionType getTransmission() {
         return this.transmissionType;
      }

      @NotNull
      public ItemConfigurator.ConfiguratorMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public static ItemConfigurator.ConfiguratorMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }

      @NotNull
      @Override
      public Component sliceName() {
         return (Component)(this.configurating && this.transmissionType != null
            ? this.transmissionType.getLangEntry().translateColored(this.color)
            : this.getTextComponent());
      }

      @NotNull
      @Override
      public ResourceLocation icon() {
         return this.icon;
      }
   }
}
