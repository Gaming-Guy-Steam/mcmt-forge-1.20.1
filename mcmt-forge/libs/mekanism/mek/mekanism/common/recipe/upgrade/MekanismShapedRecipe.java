package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

@NothingNullByDefault
public class MekanismShapedRecipe extends WrappedShapedRecipe {
   public MekanismShapedRecipe(ShapedRecipe internal) {
      super(internal);
   }

   public RecipeSerializer<?> m_7707_() {
      return (RecipeSerializer<?>)MekanismRecipeSerializers.MEK_DATA.get();
   }

   @Override
   public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
      ItemStack resultItem = this.m_8043_(registryAccess);
      if (resultItem.m_41619_()) {
         return ItemStack.f_41583_;
      } else {
         ItemStack toReturn = resultItem.m_41777_();
         List<ItemStack> nbtInputs = new ArrayList<>();

         for (int i = 0; i < inv.m_6643_(); i++) {
            ItemStack stack = inv.m_8020_(i);
            if (!stack.m_41619_() && stack.m_41782_()) {
               nbtInputs.add(stack);
            }
         }

         if (nbtInputs.isEmpty()) {
            return toReturn;
         } else {
            Set<RecipeUpgradeType> supportedTypes = RecipeUpgradeData.getSupportedTypes(toReturn);
            if (supportedTypes.isEmpty()) {
               return toReturn;
            } else {
               Map<RecipeUpgradeType, List<RecipeUpgradeData<?>>> upgradeInfo = new EnumMap<>(RecipeUpgradeType.class);

               for (ItemStack stack : nbtInputs) {
                  for (RecipeUpgradeType supportedType : RecipeUpgradeData.getSupportedTypes(stack)) {
                     if (supportedTypes.contains(supportedType)) {
                        RecipeUpgradeData<?> data = RecipeUpgradeData.getUpgradeData(supportedType, stack);
                        if (data != null) {
                           upgradeInfo.computeIfAbsent(supportedType, type -> new ArrayList<>()).add(data);
                        }
                     }
                  }
               }

               for (Entry<RecipeUpgradeType, List<RecipeUpgradeData<?>>> entry : upgradeInfo.entrySet()) {
                  List<RecipeUpgradeData<?>> upgradeData = entry.getValue();
                  if (!upgradeData.isEmpty()) {
                     RecipeUpgradeData<?> data = RecipeUpgradeData.mergeUpgradeData(upgradeData);
                     if (data == null || !data.applyToStack(toReturn)) {
                        return ItemStack.f_41583_;
                     }
                  }
               }

               return toReturn;
            }
         }
      }
   }
}
