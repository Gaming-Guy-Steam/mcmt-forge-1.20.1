package mekanism.common.item.block;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.item.ItemStackEnergyHandler;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockEnergyCube
   extends ItemBlockTooltip<BlockEnergyCube>
   implements IItemSustainedInventory,
   CreativeTabDeferredRegister.ICustomCreativeTabContents {
   public ItemBlockEnergyCube(BlockEnergyCube block) {
      super(block);
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.energyCube());
   }

   @NotNull
   public EnergyCubeTier getTier() {
      return Attribute.getTier(this.m_40614_(), EnergyCubeTier.class);
   }

   @Override
   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredEnergy(stack, tooltip, true);
      tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, new Object[]{EnumColor.GRAY, EnergyDisplay.of(this.getTier().getMaxEnergy())}));
      super.m_7373_(stack, world, tooltip, flag);
   }

   @Override
   protected void addTypeDetails(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
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
   public void addItems(Output tabOutput) {
      EnergyCubeTier tier = this.getTier();
      if (tier == EnergyCubeTier.CREATIVE) {
         tabOutput.m_246342_(this.withEnergyCubeSideConfig(DataType.INPUT));
         tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(this.withEnergyCubeSideConfig(DataType.OUTPUT), tier.getMaxEnergy()));
      } else {
         tabOutput.m_246342_(StorageUtils.getFilledEnergyVariant(new ItemStack(this), tier.getMaxEnergy()));
      }
   }

   @Override
   public boolean addDefault() {
      return this.getTier() != EnergyCubeTier.CREATIVE;
   }

   private ItemStack withEnergyCubeSideConfig(DataType dataType) {
      CompoundTag sideConfig = new CompoundTag();

      for (RelativeSide side : EnumUtils.SIDES) {
         NBTUtils.writeEnum(sideConfig, "side" + side.ordinal(), dataType);
      }

      CompoundTag configNBT = new CompoundTag();
      configNBT.m_128365_("config" + TransmissionType.ENERGY.ordinal(), sideConfig);
      ItemStack stack = new ItemStack(this);
      ItemDataUtils.setCompound(stack, "componentConfig", configNBT);
      return stack;
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      ItemCapabilityWrapper.ItemCapability capability = RateLimitEnergyHandler.create(this.getTier());
      int index = IntStream.range(0, capabilities.size()).filter(i -> capabilities.get(i) instanceof ItemStackEnergyHandler).findFirst().orElse(-1);
      if (index != -1) {
         capabilities.set(index, capability);
      } else {
         capabilities.add(capability);
      }
   }
}
