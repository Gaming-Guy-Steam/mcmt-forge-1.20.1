package mekanism.api.gear;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IModuleHelper {
   IModuleHelper INSTANCE = ServiceLoader.load(IModuleHelper.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IModuleHelper found"));

   Item createModuleItem(IModuleDataProvider<?> var1, Properties var2);

   Set<ModuleData<?>> getSupported(ItemStack var1);

   Set<Item> getSupported(IModuleDataProvider<?> var1);

   Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> var1);

   boolean isEnabled(ItemStack var1, IModuleDataProvider<?> var2);

   @Nullable
   <MODULE extends ICustomModule<MODULE>> IModule<MODULE> load(ItemStack var1, IModuleDataProvider<MODULE> var2);

   List<? extends IModule<?>> loadAll(ItemStack var1);

   <MODULE extends ICustomModule<?>> List<? extends IModule<? extends MODULE>> loadAll(ItemStack var1, Class<MODULE> var2);

   List<ModuleData<?>> loadAllTypes(ItemStack var1);

   IHUDElement hudElement(ResourceLocation var1, Component var2, IHUDElement.HUDColor var3);

   IHUDElement hudElementEnabled(ResourceLocation var1, boolean var2);

   IHUDElement hudElementPercent(ResourceLocation var1, double var2);

   void addMekaSuitModuleModels(ResourceLocation var1);

   default void addMekaSuitModuleModelSpec(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType) {
      this.addMekaSuitModuleModelSpec(name, moduleDataProvider, slotType, ConstantPredicates.alwaysTrue());
   }

   void addMekaSuitModuleModelSpec(String var1, IModuleDataProvider<?> var2, EquipmentSlot var3, Predicate<LivingEntity> var4);
}
