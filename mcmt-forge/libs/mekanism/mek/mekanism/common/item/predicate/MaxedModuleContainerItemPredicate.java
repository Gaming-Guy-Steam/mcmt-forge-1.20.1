package mekanism.common.item.predicate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap.Entry;
import java.util.Set;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class MaxedModuleContainerItemPredicate<ITEM extends Item & IModuleContainerItem> extends CustomItemPredicate {
   public static final ResourceLocation ID = Mekanism.rl("maxed_module_container");
   private final Set<ModuleData<?>> supportedModules;
   private final ITEM item;

   public MaxedModuleContainerItemPredicate(ITEM item) {
      this.item = item;
      this.supportedModules = IModuleHelper.INSTANCE.getSupported(new ItemStack(item));
   }

   @Override
   protected ResourceLocation getID() {
      return ID;
   }

   @Override
   public boolean m_45049_(@NotNull ItemStack stack) {
      if (stack.m_41720_() == this.item) {
         Reference2IntMap<ModuleData<?>> installedCounts = ModuleHelper.get().loadAllCounts(stack);
         if (installedCounts.keySet().containsAll(this.supportedModules)) {
            ObjectIterator var3 = installedCounts.reference2IntEntrySet().iterator();

            while (var3.hasNext()) {
               Entry<ModuleData<?>> entry = (Entry<ModuleData<?>>)var3.next();
               if (entry.getIntValue() != ((ModuleData)entry.getKey()).getMaxStackSize()) {
                  return false;
               }
            }

            return true;
         }
      }

      return false;
   }

   @NotNull
   @Override
   public JsonObject serializeToJson() {
      JsonObject object = super.serializeToJson();
      object.addProperty("item", RegistryUtils.getName(this.item).toString());
      return object;
   }

   public static MaxedModuleContainerItemPredicate<?> fromJson(JsonObject json) {
      String itemName = GsonHelper.m_13906_(json, "item");
      Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
      if (item instanceof IModuleContainerItem) {
         return new MaxedModuleContainerItemPredicate((ITEM)item);
      } else {
         throw new JsonParseException("Specified item is not a module container item.");
      }
   }
}
