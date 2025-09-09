package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IModuleContainerItem extends IItemHUDProvider {
   default List<Module<?>> getModules(ItemStack stack) {
      return ModuleHelper.get().loadAll(stack);
   }

   @Nullable
   default <MODULE extends ICustomModule<MODULE>> IModule<MODULE> getModule(ItemStack stack, IModuleDataProvider<MODULE> typeProvider) {
      return IModuleHelper.INSTANCE.load(stack, typeProvider);
   }

   default boolean supportsModule(ItemStack stack, IModuleDataProvider<?> typeProvider) {
      return IModuleHelper.INSTANCE.getSupported(stack).contains(typeProvider.getModuleData());
   }

   default void addModuleDetails(ItemStack stack, List<Component> tooltip) {
      for (Module<?> module : this.getModules(stack)) {
         ModuleData<?> data = module.getData();
         if (module.getInstalledCount() > 1) {
            Component amount = MekanismLang.GENERIC_FRACTION.translate(new Object[]{module.getInstalledCount(), data.getMaxStackSize()});
            tooltip.add(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, new Object[]{data, amount}));
         } else {
            tooltip.add(TextComponentUtil.build(EnumColor.GRAY, data));
         }
      }
   }

   default boolean hasModule(ItemStack stack, IModuleDataProvider<?> type) {
      CompoundTag modules = ItemDataUtils.getCompound(stack, "modules");
      return modules.m_128425_(type.getRegistryName().toString(), 10);
   }

   default boolean isModuleEnabled(ItemStack stack, IModuleDataProvider<?> type) {
      IModule<?> module = this.getModule(stack, type);
      return module != null && module.isEnabled();
   }

   default void removeModule(ItemStack stack, ModuleData<?> type) {
      Module<?> module = ModuleHelper.get().load(stack, type);
      if (module != null) {
         if (module.getInstalledCount() > 1) {
            module.setInstalledCount(module.getInstalledCount() - 1);
            module.save(null);
            module.onRemoved(false);
         } else {
            CompoundTag modules = ItemDataUtils.getCompound(stack, "modules");
            modules.m_128473_(type.getRegistryName().toString());
            module.onRemoved(true);
         }
      }
   }

   default void addModule(ItemStack stack, ModuleData<?> type) {
      Module<?> module = ModuleHelper.get().load(stack, type);
      if (module == null) {
         ItemDataUtils.getOrAddCompound(stack, "modules").m_128365_(type.getRegistryName().toString(), new CompoundTag());
         ModuleHelper.get().load(stack, type).onAdded(true);
      } else {
         module.setInstalledCount(module.getInstalledCount() + 1);
         module.save(null);
         module.onAdded(false);
      }
   }

   @Override
   default void addHUDStrings(List<Component> list, Player player, ItemStack stack, EquipmentSlot slotType) {
      for (Module<?> module : this.getModules(stack)) {
         if (module.renderHUD()) {
            module.addHUDStrings(player, list);
         }
      }
   }

   default List<IHUDElement> getHUDElements(Player player, ItemStack stack) {
      List<IHUDElement> ret = new ArrayList<>();

      for (Module<?> module : this.getModules(stack)) {
         if (module.renderHUD()) {
            module.addHUDElements(player, ret);
         }
      }

      return ret;
   }
}
