package mekanism.common.registration.impl;

import java.util.function.UnaryOperator;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;

public class ModuleDeferredRegister extends WrappedDeferredRegister<ModuleData<?>> {
   public ModuleDeferredRegister(String modid) {
      super(modid, MekanismAPI.MODULE_REGISTRY_NAME);
   }

   public ModuleRegistryObject<?> registerMarker(String name, IItemProvider itemProvider, UnaryOperator<ModuleData.ModuleDataBuilder<?>> builderModifier) {
      return this.register(name, builderModifier.apply(ModuleData.ModuleDataBuilder.marker(itemProvider)));
   }

   public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(
      String name, NonNullSupplier<MODULE> supplier, IItemProvider itemProvider
   ) {
      return this.register(name, supplier, itemProvider, UnaryOperator.identity());
   }

   public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(
      String name, NonNullSupplier<MODULE> supplier, IItemProvider itemProvider, UnaryOperator<ModuleData.ModuleDataBuilder<MODULE>> builderModifier
   ) {
      return this.register(name, builderModifier.apply(ModuleData.ModuleDataBuilder.custom(supplier, itemProvider)));
   }

   public ModuleRegistryObject<?> registerEnchantBased(
      String name, NonNullSupplier<Enchantment> enchantment, IItemProvider itemProvider, UnaryOperator<ModuleData.ModuleDataBuilder<?>> builderModifier
   ) {
      return this.register(name, builderModifier.apply(ModuleData.ModuleDataBuilder.custom(() -> new EnchantmentBasedModule() {
         @NotNull
         @Override
         public Enchantment getEnchantment() {
            return (Enchantment)enchantment.get();
         }
      }, itemProvider)));
   }

   public <MODULE extends ICustomModule<MODULE>> ModuleRegistryObject<MODULE> register(String name, ModuleData.ModuleDataBuilder<MODULE> builder) {
      return this.register(name, () -> new ModuleData<>(builder), ModuleRegistryObject::new);
   }
}
