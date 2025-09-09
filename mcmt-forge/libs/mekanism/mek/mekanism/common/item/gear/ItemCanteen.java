package mekanism.common.item.gear;

import java.util.List;
import java.util.Optional;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.CapabilityItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCanteen extends CapabilityItem implements CreativeTabDeferredRegister.ICustomCreativeTabContents {
   public ItemCanteen(Properties properties) {
      super(properties.m_41497_(Rarity.UNCOMMON).m_41487_(1).setNoRepair());
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      StorageUtils.addStoredFluid(stack, tooltip, true, MekanismLang.EMPTY);
   }

   public boolean m_142522_(@NotNull ItemStack stack) {
      return true;
   }

   public int m_142158_(@NotNull ItemStack stack) {
      return StorageUtils.getBarWidth(stack);
   }

   public int m_142159_(@NotNull ItemStack stack) {
      return FluidUtils.getRGBDurabilityForDisplay(stack).orElse(0);
   }

   @Override
   public void addItems(Output tabOutput) {
      tabOutput.m_246342_(FluidUtils.getFilledVariant(new ItemStack(this), MekanismConfig.gear.canteenMaxStorage, MekanismFluids.NUTRITIONAL_PASTE));
   }

   @NotNull
   public ItemStack m_5922_(@NotNull ItemStack stack, @NotNull Level world, @NotNull LivingEntity entityLiving) {
      if (!world.f_46443_ && entityLiving instanceof Player player) {
         int needed = Math.min(20 - player.m_36324_().m_38702_(), this.getFluid(stack).getAmount() / MekanismConfig.general.nutritionalPasteMBPerFood.get());
         if (needed > 0) {
            player.m_36324_().m_38707_(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
            FluidUtil.getFluidHandler(stack)
               .ifPresent(handler -> handler.drain(needed * MekanismConfig.general.nutritionalPasteMBPerFood.get(), FluidAction.EXECUTE));
            entityLiving.m_146850_(GameEvent.f_223704_);
         }
      }

      return stack;
   }

   public int m_8105_(@NotNull ItemStack stack) {
      return 32;
   }

   @NotNull
   public UseAnim m_6164_(@NotNull ItemStack stack) {
      return UseAnim.DRINK;
   }

   @Override
   protected boolean areCapabilityConfigsLoaded() {
      return super.areCapabilityConfigsLoaded() && MekanismConfig.gear.isLoaded();
   }

   @Override
   protected void gatherCapabilities(List<ItemCapabilityWrapper.ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
      super.gatherCapabilities(capabilities, stack, nbt);
      capabilities.add(
         RateLimitFluidHandler.create(
            MekanismConfig.gear.canteenTransferRate,
            MekanismConfig.gear.canteenMaxStorage,
            BasicFluidTank.alwaysTrueBi,
            BasicFluidTank.alwaysTrueBi,
            fluid -> fluid.getFluid() == MekanismFluids.NUTRITIONAL_PASTE.getFluid()
         )
      );
   }

   private FluidStack getFluid(ItemStack stack) {
      Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
      if (capability.isPresent()) {
         IFluidHandlerItem fluidHandlerItem = capability.get();
         if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
            IExtendedFluidTank fluidTank = fluidHandler.getFluidTank(0, null);
            if (fluidTank != null) {
               return fluidTank.getFluid();
            }
         }

         return fluidHandlerItem.getFluidInTank(0);
      } else {
         return FluidStack.EMPTY;
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
      if (!playerIn.m_7500_() && playerIn.m_36391_(false) && this.getFluid(playerIn.m_21120_(handIn)).getAmount() >= 50) {
         playerIn.m_6672_(handIn);
      }

      return InteractionResultHolder.m_19090_(playerIn.m_21120_(handIn));
   }
}
