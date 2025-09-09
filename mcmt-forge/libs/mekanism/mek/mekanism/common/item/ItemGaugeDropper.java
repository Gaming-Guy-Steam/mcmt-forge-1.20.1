package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.fluid.IExtendedFluidHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.merged.GaugeDropperContentsHandler;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class ItemGaugeDropper extends CapabilityItem {
   public ItemGaugeDropper(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.UNCOMMON));
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return FluidUtils.getRGBDurabilityForDisplay(stack).orElseGet(() -> ChemicalUtil.getRGBDurabilityForDisplay(stack));
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!player.m_6144_()) {
         return InteractionResultHolder.m_19098_(stack);
      } else {
         if (!world.f_46443_) {
            Optional<IFluidHandlerItem> fluidCapability = FluidUtil.getFluidHandler(stack).resolve();
            if (fluidCapability.isPresent()) {
               IFluidHandlerItem fluidHandler = fluidCapability.get();
               if (fluidHandler instanceof IExtendedFluidHandler fluidHandlerItem) {
                  for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                     fluidHandlerItem.setFluidInTank(tank, FluidStack.EMPTY);
                  }
               }
            }

            clearChemicalTanks(stack, GasStack.EMPTY);
            clearChemicalTanks(stack, InfusionStack.EMPTY);
            clearChemicalTanks(stack, PigmentStack.EMPTY);
            clearChemicalTanks(stack, SlurryStack.EMPTY);
         }

         return InteractionResultHolder.m_19092_(stack, world.f_46443_);
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void clearChemicalTanks(ItemStack stack, STACK empty) {
      Optional<IChemicalHandler<CHEMICAL, STACK>> cap = stack.getCapability(ChemicalUtil.getCapabilityForChemical(empty)).resolve();
      if (cap.isPresent()) {
         IChemicalHandler<CHEMICAL, STACK> handler = cap.get();

         for (int tank = 0; tank < handler.getTanks(); tank++) {
            handler.setChemicalInTank(tank, empty);
         }
      }
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredSubstance(stack, tooltip, false);
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(GaugeDropperContentsHandler.create());
   }
}
