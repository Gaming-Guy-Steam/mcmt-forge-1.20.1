package mekanism.common.recipe.bin;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.registries.MekanismRecipeSerializers;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.items.ItemHandlerHelper;

@NothingNullByDefault
public class BinInsertRecipe extends BinRecipe {
   public BinInsertRecipe(ResourceLocation id, CraftingBookCategory category) {
      super(id, category);
   }

   public boolean matches(CraftingContainer inv, Level world) {
      ItemStack binStack = ItemStack.f_41583_;
      ItemStack foundType = ItemStack.f_41583_;
      int i = 0;

      for (int slots = inv.m_6643_(); i < slots; i++) {
         ItemStack stackInSlot = inv.m_8020_(i);
         if (!stackInSlot.m_41619_()) {
            if (stackInSlot.m_41720_() instanceof ItemBlockBin) {
               if (!binStack.m_41619_()) {
                  return false;
               }

               binStack = stackInSlot;
            } else if (foundType.m_41619_()) {
               foundType = stackInSlot;
            } else if (!ItemHandlerHelper.canItemStacksStack(foundType, stackInSlot)) {
               return false;
            }
         }
      }

      if (!binStack.m_41619_() && !foundType.m_41619_()) {
         BinInventorySlot slot = convertToSlot(binStack);
         ItemStack remaining = slot.insertItem(foundType, Action.SIMULATE, AutomationType.MANUAL);
         return !ItemStack.m_41728_(remaining, foundType);
      } else {
         return false;
      }
   }

   public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
      ItemStack binStack = ItemStack.f_41583_;
      ItemStack foundType = ItemStack.f_41583_;
      List<ItemStack> foundItems = new ArrayList<>();
      int i = 0;

      for (int slots = inv.m_6643_(); i < slots; i++) {
         ItemStack stackInSlot = inv.m_8020_(i);
         if (!stackInSlot.m_41619_()) {
            if (stackInSlot.m_41720_() instanceof ItemBlockBin) {
               if (!binStack.m_41619_()) {
                  return ItemStack.f_41583_;
               }

               binStack = stackInSlot;
            } else {
               if (foundType.m_41619_()) {
                  foundType = stackInSlot;
               } else if (!ItemHandlerHelper.canItemStacksStack(foundType, stackInSlot)) {
                  return ItemStack.f_41583_;
               }

               foundItems.add(stackInSlot);
            }
         }
      }

      if (!binStack.m_41619_() && !foundType.m_41619_()) {
         binStack = binStack.m_41777_();
         BinInventorySlot slot = convertToSlot(binStack);
         boolean hasInserted = false;

         for (ItemStack stack : foundItems) {
            ItemStack toInsert = stack.m_255036_(1);
            ItemStack remainder = slot.insertItem(toInsert, Action.EXECUTE, AutomationType.MANUAL);
            if (!remainder.m_41619_()) {
               if (hasInserted) {
                  return binStack;
               }

               return ItemStack.f_41583_;
            }

            hasInserted = true;
         }

         ItemDataUtils.setBoolean(binStack, "fromRecipe", true);
         return binStack;
      } else {
         return ItemStack.f_41583_;
      }
   }

   public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
      int slots = inv.m_6643_();
      NonNullList<ItemStack> remainingItems = NonNullList.m_122780_(slots, ItemStack.f_41583_);
      ItemStack binStack = ItemStack.f_41583_;
      ItemStack foundType = ItemStack.f_41583_;
      Int2ObjectMap<ItemStack> foundSlots = new Int2ObjectArrayMap(slots);

      for (int i = 0; i < slots; i++) {
         ItemStack stackInSlot = inv.m_8020_(i);
         if (!stackInSlot.m_41619_()) {
            if (stackInSlot.m_41720_() instanceof ItemBlockBin) {
               if (!binStack.m_41619_()) {
                  return remainingItems;
               }

               binStack = stackInSlot;
            } else {
               if (foundType.m_41619_()) {
                  foundType = stackInSlot;
               } else if (!ItemHandlerHelper.canItemStacksStack(foundType, stackInSlot)) {
                  return remainingItems;
               }

               foundSlots.put(i, stackInSlot);
            }
         }
      }

      if (!binStack.m_41619_() && !foundType.m_41619_()) {
         binStack = binStack.m_41777_();
         BinInventorySlot slot = convertToSlot(binStack);
         ObjectIterator var14 = foundSlots.int2ObjectEntrySet().iterator();

         while (var14.hasNext()) {
            Entry<ItemStack> entry = (Entry<ItemStack>)var14.next();
            ItemStack slotItem = (ItemStack)entry.getValue();
            ItemStack remaining = slot.insertItem(slotItem.m_255036_(1), Action.EXECUTE, AutomationType.MANUAL);
            if (!remaining.m_41619_()) {
               remainingItems.set(entry.getIntKey(), remaining);
            }
         }

         return remainingItems;
      } else {
         return remainingItems;
      }
   }

   public boolean m_8004_(int width, int height) {
      return width * height >= 2;
   }

   public RecipeSerializer<?> m_7707_() {
      return (RecipeSerializer<?>)MekanismRecipeSerializers.BIN_INSERT.get();
   }

   public static void onCrafting(ItemCraftedEvent event) {
      ItemStack result = event.getCrafting();
      if (!result.m_41619_() && result.m_41720_() instanceof ItemBlockBin && ItemDataUtils.getBoolean(result, "fromRecipe")) {
         BinInventorySlot slot = convertToSlot(result);
         ItemStack storedStack = slot.getStack();
         if (!storedStack.m_41619_()) {
            Container craftingMatrix = event.getInventory();
            int i = 0;

            for (int slots = craftingMatrix.m_6643_(); i < slots; i++) {
               ItemStack stack = craftingMatrix.m_8020_(i);
               if (stack.m_41613_() > 1 && ItemHandlerHelper.canItemStacksStack(storedStack, stack)) {
                  ItemStack toInsert = stack.m_255036_(stack.m_41613_() - 1);
                  ItemStack remaining = slot.insertItem(toInsert, Action.EXECUTE, AutomationType.MANUAL);
                  if (remaining.m_41619_()) {
                     craftingMatrix.m_6836_(i, stack.m_255036_(1));
                  } else if (remaining.m_41613_() < toInsert.m_41613_()) {
                     craftingMatrix.m_6836_(i, stack.m_255036_(remaining.m_41613_() + 1));
                  }
               }
            }
         }

         ItemDataUtils.removeData(result, "fromRecipe");
      }
   }
}
