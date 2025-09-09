package mekanism.common.content.gear.mekasuit;

import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.network.distribution.EnergySaveTarget;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

@ParametersAreNotNullByDefault
public class ModuleChargeDistributionUnit implements ICustomModule<ModuleChargeDistributionUnit> {
   private IModuleConfigItem<Boolean> chargeSuit;
   private IModuleConfigItem<Boolean> chargeInventory;

   @Override
   public void init(IModule<ModuleChargeDistributionUnit> module, ModuleConfigItemCreator configItemCreator) {
      this.chargeSuit = configItemCreator.createConfigItem("charge_suit", MekanismLang.MODULE_CHARGE_SUIT, new ModuleBooleanData());
      this.chargeInventory = configItemCreator.createConfigItem("charge_inventory", MekanismLang.MODULE_CHARGE_INVENTORY, new ModuleBooleanData(false));
   }

   @Override
   public void tickServer(IModule<ModuleChargeDistributionUnit> module, Player player) {
      if (this.chargeInventory.get()) {
         this.chargeInventory(module, player);
      }

      if (this.chargeSuit.get()) {
         this.chargeSuit(player);
      }
   }

   private void chargeSuit(Player player) {
      FloatingLong total = FloatingLong.ZERO;
      EnergySaveTarget saveTarget = new EnergySaveTarget(4);

      for (ItemStack stack : player.m_6168_()) {
         IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
         if (energyContainer != null) {
            saveTarget.addDelegate(energyContainer);
            total = total.plusEqual(energyContainer.getEnergy());
         }
      }

      EmitUtils.sendToAcceptors(saveTarget, total);
      saveTarget.save();
   }

   private void chargeInventory(IModule<ModuleChargeDistributionUnit> module, Player player) {
      FloatingLong toCharge = MekanismConfig.gear.mekaSuitInventoryChargeRate.get();
      toCharge = this.charge(module, player, player.m_21205_(), toCharge);
      toCharge = this.charge(module, player, player.m_21206_(), toCharge);
      if (!toCharge.isZero()) {
         for (ItemStack stack : player.m_150109_().f_35974_) {
            if (stack != player.m_21205_() && stack != player.m_21206_()) {
               toCharge = this.charge(module, player, stack, toCharge);
               if (toCharge.isZero()) {
                  break;
               }
            }
         }

         if (!toCharge.isZero() && Mekanism.hooks.CuriosLoaded) {
            Optional<? extends IItemHandler> curiosInventory = CuriosIntegration.getCuriosInventory(player);
            if (curiosInventory.isPresent()) {
               IItemHandler handler = curiosInventory.get();
               int slot = 0;

               for (int slots = handler.getSlots(); slot < slots; slot++) {
                  toCharge = this.charge(module, player, handler.getStackInSlot(slot), toCharge);
                  if (toCharge.isZero()) {
                     break;
                  }
               }
            }
         }
      }
   }

   private FloatingLong charge(IModule<ModuleChargeDistributionUnit> module, Player player, ItemStack stack, FloatingLong amount) {
      if (!stack.m_41619_() && !amount.isZero()) {
         IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(stack);
         if (handler != null) {
            FloatingLong remaining = handler.insertEnergy(amount, Action.SIMULATE);
            if (remaining.smallerThan(amount)) {
               return handler.insertEnergy(module.useEnergy(player, amount.subtract(remaining), false), Action.EXECUTE).add(remaining);
            }
         }
      }

      return amount;
   }
}
