package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import mekanism.api.DataHandlerUtils;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemRecipeData implements RecipeUpgradeData<ItemRecipeData> {
   private final List<IInventorySlot> slots;

   ItemRecipeData(ListTag slots) {
      this(readContents(slots));
   }

   ItemRecipeData(List<IInventorySlot> slots) {
      this.slots = slots;
   }

   @Nullable
   public ItemRecipeData merge(ItemRecipeData other) {
      List<IInventorySlot> allSlots = new ArrayList<>(this.slots);
      allSlots.addAll(other.slots);
      return new ItemRecipeData(allSlots);
   }

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.slots.isEmpty()) {
         return true;
      } else {
         Item item = stack.m_41720_();
         List<IInventorySlot> stackSlots = new ArrayList<>();
         if (item instanceof ItemBlockPersonalStorage) {
            PersonalStorageManager.createSlots(stackSlots::add, BasicInventorySlot.alwaysTrueBi, null);
            return applyToStack(this.slots, stackSlots, toWrite -> PersonalStorageManager.createInventoryFor(stack, stackSlots));
         } else {
            boolean isBin = item instanceof ItemBlockBin;
            Optional<IItemHandler> capability = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve();
            if (capability.isPresent()) {
               IItemHandler itemHandler = capability.get();
               int i = 0;

               for (int slots = itemHandler.getSlots(); i < slots; i++) {
                  int slot = i;
                  stackSlots.add(
                     new ItemRecipeData.DummyInventorySlot(itemHandler.getSlotLimit(slot), itemStack -> itemHandler.isItemValid(slot, itemStack), isBin)
                  );
               }
            } else if (item instanceof BlockItem blockItem) {
               TileEntityMekanism tile = this.getTileFromBlock(blockItem.m_40614_());
               if (tile == null || !tile.persistInventory()) {
                  return false;
               }

               int i = 0;

               for (int slots = tile.getSlots(); i < slots; i++) {
                  int slot = i;
                  stackSlots.add(new ItemRecipeData.DummyInventorySlot(tile.getSlotLimit(slot), itemStack -> tile.isItemValid(slot, itemStack), isBin));
               }
            } else {
               if (!(item instanceof ItemRobit)) {
                  if (item instanceof IItemSustainedInventory sustainedInventory) {
                     for (IInventorySlot slot : this.slots) {
                        if (!slot.isEmpty()) {
                           sustainedInventory.setSustainedInventory(DataHandlerUtils.writeContainers(this.slots), stack);
                           return true;
                        }
                     }

                     return true;
                  }

                  return false;
               }

               for (int slotY = 0; slotY < 3; slotY++) {
                  for (int slotX = 0; slotX < 9; slotX++) {
                     stackSlots.add(new ItemRecipeData.DummyInventorySlot(64, BasicInventorySlot.alwaysTrue, false));
                  }
               }

               stackSlots.add(new ItemRecipeData.DummyInventorySlot(64, itemStack -> {
                  if (EnergyCompatUtils.hasStrictEnergyHandler(itemStack)) {
                     return true;
                  } else {
                     ItemStackToEnergyRecipe foundRecipe = MekanismRecipeType.ENERGY_CONVERSION.getInputCache().findTypeBasedRecipe(null, itemStack);
                     return foundRecipe != null && !foundRecipe.getOutput(itemStack).isZero();
                  }
               }, false));
               stackSlots.add(
                  new ItemRecipeData.DummyInventorySlot(64, itemStack -> MekanismRecipeType.SMELTING.getInputCache().containsInput(null, itemStack), false)
               );
               stackSlots.add(new ItemRecipeData.DummyInventorySlot(64, BasicInventorySlot.alwaysTrue, false));
            }

            return applyToStack(this.slots, stackSlots, toWrite -> ((IItemSustainedInventory)item).setSustainedInventory(toWrite, stack));
         }
      }
   }

   static boolean applyToStack(List<IInventorySlot> dataSlots, List<IInventorySlot> stackSlots, Consumer<ListTag> stackWriter) {
      return applyToStack(dataSlots, stackSlots, t -> {
         stackWriter.accept(t);
         return true;
      });
   }

   private static boolean applyToStack(List<IInventorySlot> dataSlots, List<IInventorySlot> stackSlots, Predicate<ListTag> stackWriter) {
      if (stackSlots.isEmpty()) {
         return true;
      } else {
         IMekanismInventory outputHandler = new IMekanismInventory() {
            @NotNull
            @Override
            public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
               return stackSlots;
            }

            @Override
            public void onContentsChanged() {
            }
         };
         boolean hasData = false;

         for (IInventorySlot slot : dataSlots) {
            if (!slot.isEmpty()) {
               if (!ItemHandlerHelper.insertItemStacked(outputHandler, slot.getStack(), false).m_41619_()) {
                  return false;
               }

               hasData = true;
            }
         }

         return hasData ? stackWriter.test(DataHandlerUtils.writeContainers(stackSlots)) : true;
      }
   }

   public static List<IInventorySlot> readContents(@Nullable ListTag contents) {
      if (contents != null && !contents.isEmpty()) {
         int count = DataHandlerUtils.getMaxId(contents, "Slot");
         List<IInventorySlot> slots = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            slots.add(new ItemRecipeData.DummyInventorySlot());
         }

         DataHandlerUtils.readContainers(slots, contents);
         return slots;
      } else {
         return Collections.emptyList();
      }
   }

   private static class DummyInventorySlot extends BasicInventorySlot {
      private DummyInventorySlot() {
         this(Integer.MAX_VALUE, alwaysTrue, true);
      }

      private DummyInventorySlot(int capacity, Predicate<ItemStack> validator, boolean isBin) {
         super(capacity, alwaysTrueBi, alwaysTrueBi, validator, null, 0, 0);
         if (isBin) {
            this.obeyStackLimit = false;
         }
      }
   }
}
