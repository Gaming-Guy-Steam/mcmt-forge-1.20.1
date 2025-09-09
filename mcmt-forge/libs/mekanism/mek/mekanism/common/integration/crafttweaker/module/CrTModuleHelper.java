package mekanism.common.integration.crafttweaker.module;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import java.util.List;
import java.util.Set;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;
import org.openzen.zencode.java.ZenCodeType.Nullable;

@ZenRegister
@Name("mods.mekanism.api.gear.ModuleHelper")
public class CrTModuleHelper {
   @Method
   public static Set<ModuleData<?>> getSupported(ItemStack container) {
      return IModuleHelper.INSTANCE.getSupported(container);
   }

   @Method
   public static Set<Item> getSupported(ModuleData<?> type) {
      return IModuleHelper.INSTANCE.getSupported(type);
   }

   @Method
   public static boolean isEnabled(ItemStack container, ModuleData<?> type) {
      return IModuleHelper.INSTANCE.isEnabled(container, type);
   }

   @Method
   @Nullable
   public static <MODULE extends ICustomModule<MODULE>> IModule<MODULE> load(ItemStack container, ModuleData<MODULE> type) {
      return IModuleHelper.INSTANCE.load(container, type);
   }

   @Method
   public static List<IModule> loadAll(ItemStack container) {
      return IModuleHelper.INSTANCE.loadAll(container);
   }

   @Method
   public static List<ModuleData<?>> loadAllTypes(ItemStack container) {
      return IModuleHelper.INSTANCE.loadAllTypes(container);
   }
}
