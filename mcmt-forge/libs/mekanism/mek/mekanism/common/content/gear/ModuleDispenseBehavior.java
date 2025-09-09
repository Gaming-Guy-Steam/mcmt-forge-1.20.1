package mekanism.common.content.gear;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModuleDispenseBehavior extends OptionalDispenseItemBehavior {
   @NotNull
   protected ItemStack m_7498_(@NotNull BlockSource source, @NotNull ItemStack stack) {
      this.m_123573_(true);
      ICustomModule.ModuleDispenseResult result = this.performBuiltin(source, stack);
      if (result == ICustomModule.ModuleDispenseResult.HANDLED) {
         return stack;
      } else {
         boolean preventDrop = result == ICustomModule.ModuleDispenseResult.FAIL_PREVENT_DROP;

         for (Module<?> module : ModuleHelper.get().loadAll(stack)) {
            if (module.isEnabled()) {
               result = this.onModuleDispense(module, source);
               if (result == ICustomModule.ModuleDispenseResult.HANDLED) {
                  return stack;
               }

               preventDrop |= result == ICustomModule.ModuleDispenseResult.FAIL_PREVENT_DROP;
            }
         }

         if (preventDrop) {
            this.m_123573_(false);
            return stack;
         } else {
            return super.m_7498_(source, stack);
         }
      }
   }

   private <MODULE extends ICustomModule<MODULE>> ICustomModule.ModuleDispenseResult onModuleDispense(IModule<MODULE> module, @NotNull BlockSource source) {
      return module.getCustomInstance().onDispense(module, source);
   }

   protected ICustomModule.ModuleDispenseResult performBuiltin(@NotNull BlockSource source, @NotNull ItemStack stack) {
      return ICustomModule.ModuleDispenseResult.DEFAULT;
   }
}
