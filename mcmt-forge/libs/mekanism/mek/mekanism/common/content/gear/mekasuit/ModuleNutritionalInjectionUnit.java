package mekanism.common.content.gear.mekasuit;

import java.util.Optional;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.registries.MekanismFluids;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

@ParametersAreNotNullByDefault
public class ModuleNutritionalInjectionUnit implements ICustomModule<ModuleNutritionalInjectionUnit> {
   private static final ResourceLocation icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "nutritional_injection_unit.png");

   @Override
   public void tickServer(IModule<ModuleNutritionalInjectionUnit> module, Player player) {
      FloatingLong usage = MekanismConfig.gear.mekaSuitEnergyUsageNutritionalInjection.get();
      if (MekanismUtils.isPlayingMode(player) && player.m_36391_(false)) {
         ItemStack container = module.getContainer();
         ItemMekaSuitArmor item = (ItemMekaSuitArmor)container.m_41720_();
         int needed = Math.min(
            20 - player.m_36324_().m_38702_(),
            item.getContainedFluid(container, MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(1)).getAmount()
               / MekanismConfig.general.nutritionalPasteMBPerFood.get()
         );
         int toFeed = Math.min(module.getContainerEnergy().divideToInt(usage), needed);
         if (toFeed > 0) {
            module.useEnergy(player, usage.multiply((long)toFeed));
            FluidUtil.getFluidHandler(container)
               .ifPresent(
                  handler -> handler.drain(
                     MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(toFeed * MekanismConfig.general.nutritionalPasteMBPerFood.get()), FluidAction.EXECUTE
                  )
               );
            player.m_36324_().m_38707_(needed, MekanismConfig.general.nutritionalPasteSaturation.get());
         }
      }
   }

   @Override
   public void addHUDElements(IModule<ModuleNutritionalInjectionUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
      if (module.isEnabled()) {
         ItemStack container = module.getContainer();
         Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(container).resolve();
         if (capability.isPresent()) {
            IFluidHandlerItem handler = capability.get();
            int max = MekanismConfig.gear.mekaSuitNutritionalMaxStorage.getAsInt();
            handler.drain(MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(max), FluidAction.SIMULATE);
         }

         FluidStack stored = ((ItemMekaSuitArmor)container.m_41720_()).getContainedFluid(container, MekanismFluids.NUTRITIONAL_PASTE.getFluidStack(1));
         double ratio = StorageUtils.getRatio(stored.getAmount(), MekanismConfig.gear.mekaSuitNutritionalMaxStorage.get());
         hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(icon, ratio));
      }
   }
}
