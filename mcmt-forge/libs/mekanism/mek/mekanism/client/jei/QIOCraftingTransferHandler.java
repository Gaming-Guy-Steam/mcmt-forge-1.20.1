package mekanism.client.jei;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteArraySet;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOCraftingTransferHelper;
import mekanism.common.content.qio.QIOCraftingWindow;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.inventory.container.slot.HotBarSlot;
import mekanism.common.inventory.container.slot.MainInventorySlot;
import mekanism.common.inventory.slot.CraftingWindowInventorySlot;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.to_server.PacketQIOFillCraftingWindow;
import mekanism.common.util.MekanismUtils;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QIOCraftingTransferHandler<CONTAINER extends QIOItemViewerContainer> implements IRecipeTransferHandler<CONTAINER, CraftingRecipe> {
   private final IRecipeTransferHandlerHelper handlerHelper;
   private final Class<CONTAINER> containerClass;
   private final Function<HashedItem, String> recipeUUIDFunction;

   public QIOCraftingTransferHandler(IRecipeTransferHandlerHelper handlerHelper, IStackHelper stackHelper, Class<CONTAINER> containerClass) {
      this.handlerHelper = handlerHelper;
      this.containerClass = containerClass;
      this.recipeUUIDFunction = hashed -> stackHelper.getUniqueIdentifierForStack(hashed.getInternalStack(), UidContext.Recipe);
   }

   public Class<CONTAINER> getContainerClass() {
      return this.containerClass;
   }

   public Optional<MenuType<CONTAINER>> getMenuType() {
      return Optional.empty();
   }

   public RecipeType<CraftingRecipe> getRecipeType() {
      return RecipeTypes.CRAFTING;
   }

   @Nullable
   public IRecipeTransferError transferRecipe(
      CONTAINER container, CraftingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer
   ) {
      byte selectedCraftingGrid = container.getSelectedCraftingGrid();
      if (selectedCraftingGrid == -1) {
         return this.handlerHelper.createInternalError();
      } else {
         QIOCraftingWindow craftingWindow = container.getCraftingWindow(selectedCraftingGrid);
         byte nonEmptyCraftingSlots = 0;
         if (!doTransfer) {
            CraftingContainer dummy = MekanismUtils.getDummyCraftingInv();

            for (int slot = 0; slot < 9; slot++) {
               CraftingWindowInventorySlot inputSlot = craftingWindow.getInputSlot(slot);
               if (!inputSlot.isEmpty()) {
                  dummy.m_6836_(slot, inputSlot.getStack().m_255036_(1));
                  nonEmptyCraftingSlots++;
               }
            }

            if (recipe.m_5818_(dummy, player.m_9236_())) {
               return null;
            }
         }

         List<IRecipeSlotView> slotViews = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);
         int maxInputCount = slotViews.size();
         if (maxInputCount > 9) {
            Mekanism.logger.warn("Error evaluating recipe transfer handler for recipe: {}, had more than 9 inputs: {}", recipe.m_6423_(), maxInputCount);
            return this.handlerHelper.createInternalError();
         } else {
            int inputCount = 0;

            record TrackedIngredients(IRecipeSlotView view, Set<HashedItem> representations) {
            }

            Byte2ObjectMap<TrackedIngredients> hashedIngredients = new Byte2ObjectArrayMap(maxInputCount);

            for (int index = 0; index < maxInputCount; index++) {
               IRecipeSlotView slotView = slotViews.get(index);
               List<ItemStack> validIngredients = slotView.getIngredients(VanillaTypes.ITEM_STACK).toList();
               if (!validIngredients.isEmpty()) {
                  inputCount++;
                  LinkedHashSet<HashedItem> representations = new LinkedHashSet<>(validIngredients.size());
                  ItemStack displayed = slotView.getDisplayedIngredient(VanillaTypes.ITEM_STACK).orElse(ItemStack.f_41583_);
                  if (!displayed.m_41619_()) {
                     representations.add(HashedItem.raw(displayed));
                  }

                  for (ItemStack validIngredient : validIngredients) {
                     if (!validIngredient.m_41619_()) {
                        representations.add(HashedItem.raw(validIngredient));
                     }
                  }

                  hashedIngredients.put((byte)index, new TrackedIngredients(slotView, representations));
               }
            }

            QIOCraftingTransferHelper qioTransferHelper = container.getTransferHelper(player, craftingWindow);
            if (qioTransferHelper.isInvalid()) {
               Mekanism.logger.warn("Error initializing QIO transfer handler for crafting window: {}", selectedCraftingGrid);
               return this.handlerHelper.createInternalError();
            } else {
               Map<HashedItem, ByteList> matchedItems = new HashMap<>(inputCount);
               ByteSet missingSlots = new ByteArraySet(inputCount);
               ObjectIterator var36 = hashedIngredients.byte2ObjectEntrySet().iterator();

               while (var36.hasNext()) {
                  Entry<TrackedIngredients> entry = (Entry<TrackedIngredients>)var36.next();
                  boolean matchFound = false;

                  for (HashedItem validInput : ((TrackedIngredients)entry.getValue()).representations()) {
                     QIOCraftingTransferHelper.HashedItemSource source = qioTransferHelper.getSource(validInput);
                     if (source != null && source.hasMoreRemaining()) {
                        source.matchFound();
                        matchFound = true;
                        matchedItems.computeIfAbsent(validInput, item -> new ByteArrayList()).add(entry.getByteKey());
                        break;
                     }
                  }

                  if (!matchFound) {
                     missingSlots.add(entry.getByteKey());
                  }
               }

               if (!missingSlots.isEmpty()) {
                  Map<HashedItem, String> cachedIngredientUUIDs = new HashMap<>();

                  for (java.util.Map.Entry<HashedItem, QIOCraftingTransferHelper.HashedItemSource> entry : qioTransferHelper.reverseLookup.entrySet()) {
                     QIOCraftingTransferHelper.HashedItemSource source = entry.getValue();
                     if (source.hasMoreRemaining()) {
                        HashedItem storedHashedItem = entry.getKey();
                        Item storedItemType = storedHashedItem.getItem();
                        String storedItemUUID = null;
                        ByteIterator missingIterator = missingSlots.iterator();

                        while (missingIterator.hasNext()) {
                           byte indexx = missingIterator.nextByte();

                           for (HashedItem validIngredientx : ((TrackedIngredients)hashedIngredients.get(indexx)).representations()) {
                              if (storedItemType == validIngredientx.getItem()) {
                                 if (storedItemUUID == null) {
                                    storedItemUUID = this.recipeUUIDFunction.apply(storedHashedItem);
                                 }

                                 String ingredientUUID = cachedIngredientUUIDs.computeIfAbsent(validIngredientx, this.recipeUUIDFunction);
                                 if (storedItemUUID.equals(ingredientUUID)) {
                                    source.matchFound();
                                    missingIterator.remove();
                                    matchedItems.computeIfAbsent(storedHashedItem, item -> new ByteArrayList()).add(indexx);
                                    break;
                                 }
                              }
                           }

                           if (!source.hasMoreRemaining()) {
                              break;
                           }
                        }

                        if (missingSlots.isEmpty()) {
                           break;
                        }
                     }
                  }

                  if (!missingSlots.isEmpty()) {
                     List<IRecipeSlotView> missing = missingSlots.intStream()
                        .mapToObj(slotx -> ((TrackedIngredients)hashedIngredients.get((byte)slotx)).view())
                        .toList();
                     return this.handlerHelper.createUserErrorForMissingSlots(MekanismLang.JEI_MISSING_ITEMS.translate(new Object[0]), missing);
                  }
               }

               if (doTransfer || nonEmptyCraftingSlots > 0 && nonEmptyCraftingSlots >= qioTransferHelper.getEmptyInventorySlots()) {
                  int toTransfer;
                  if (!maxTransfer) {
                     toTransfer = 1;
                  } else {
                     long maxToTransfer = Long.MAX_VALUE;

                     for (java.util.Map.Entry<HashedItem, ByteList> entryx : matchedItems.entrySet()) {
                        HashedItem hashedItem = entryx.getKey();
                        QIOCraftingTransferHelper.HashedItemSource source = qioTransferHelper.getSource(hashedItem);
                        if (source == null) {
                           return this.invalidSource(hashedItem);
                        }

                        int maxStack = hashedItem.getMaxStackSize();
                        long max = maxStack == 1 ? maxToTransfer : Math.min(maxToTransfer, (long)maxStack);
                        maxToTransfer = Math.min(max, source.getAvailable() / entryx.getValue().size());
                     }

                     toTransfer = MathUtils.clampToInt(maxToTransfer);
                  }

                  QIOFrequency frequency = container.getFrequency();
                  Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources = new Byte2ObjectArrayMap(inputCount);
                  Map<QIOCraftingTransferHelper.HashedItemSource, List<List<QIOCraftingTransferHelper.SingularHashedItemSource>>> shuffleLookup = (Map<QIOCraftingTransferHelper.HashedItemSource, List<List<QIOCraftingTransferHelper.SingularHashedItemSource>>>)(frequency
                        == null
                     ? Collections.emptyMap()
                     : new HashMap<>(inputCount));

                  for (java.util.Map.Entry<HashedItem, ByteList> entryx : matchedItems.entrySet()) {
                     HashedItem hashedItem = entryx.getKey();
                     QIOCraftingTransferHelper.HashedItemSource source = qioTransferHelper.getSource(hashedItem);
                     if (source == null) {
                        return this.invalidSource(hashedItem);
                     }

                     int transferAmount = Math.min(toTransfer, hashedItem.getMaxStackSize());
                     ByteListIterator var63 = entryx.getValue().iterator();

                     while (var63.hasNext()) {
                        byte slotx = (Byte)var63.next();
                        List<QIOCraftingTransferHelper.SingularHashedItemSource> actualSources = source.use(transferAmount);
                        if (actualSources.isEmpty()) {
                           return this.invalidSource(hashedItem);
                        }

                        sources.put(slotx, actualSources);
                        if (frequency != null) {
                           int elements = entryx.getValue().size();
                           if (elements == 1) {
                              shuffleLookup.put(source, Collections.singletonList(actualSources));
                           } else {
                              shuffleLookup.computeIfAbsent(source, s -> new ArrayList<>(elements)).add(actualSources);
                           }
                        }
                     }
                  }

                  if (!hasRoomToShuffle(
                     qioTransferHelper, frequency, craftingWindow, container.getHotBarSlots(), container.getMainInventorySlots(), shuffleLookup
                  )) {
                     return this.handlerHelper.createUserErrorWithTooltip(MekanismLang.JEI_INVENTORY_FULL.translate(new Object[0]));
                  }

                  if (doTransfer) {
                     Mekanism.packetHandler().sendToServer(new PacketQIOFillCraftingWindow(recipe.m_6423_(), maxTransfer, sources));
                  }
               }

               return null;
            }
         }
      }
   }

   private IRecipeTransferError invalidSource(@NotNull HashedItem type) {
      Mekanism.logger.warn("Error finding source for: {} with nbt: {}. This should not be possible.", type.getItem(), type.getInternalTag());
      return this.handlerHelper.createInternalError();
   }

   private static boolean hasRoomToShuffle(
      QIOCraftingTransferHelper qioTransferHelper,
      @Nullable QIOFrequency frequency,
      QIOCraftingWindow craftingWindow,
      List<HotBarSlot> hotBarSlots,
      List<MainInventorySlot> mainInventorySlots,
      Map<QIOCraftingTransferHelper.HashedItemSource, List<List<QIOCraftingTransferHelper.SingularHashedItemSource>>> shuffleLookup
   ) {
      Object2IntMap<HashedItem> leftOverInput = new Object2IntArrayMap(9);

      for (byte slotIndex = 0; slotIndex < 9; slotIndex++) {
         IInventorySlot slot = craftingWindow.getInputSlot(slotIndex);
         if (!slot.isEmpty()) {
            HashedItem type = HashedItem.raw(slot.getStack());
            QIOCraftingTransferHelper.HashedItemSource source = qioTransferHelper.getSource(type);
            if (source == null) {
               return false;
            }

            int remaining = source.getSlotRemaining(slotIndex);
            if (remaining > 0) {
               leftOverInput.mergeInt(type, remaining, Integer::sum);
            }
         }
      }

      if (!leftOverInput.isEmpty()) {
         QIOCraftingTransferHelper.BaseSimulatedInventory simulatedInventory = new QIOCraftingTransferHelper.BaseSimulatedInventory(
            hotBarSlots, mainInventorySlots
         ) {
            @Override
            protected int getRemaining(int slot, ItemStack currentStored) {
               QIOCraftingTransferHelper.HashedItemSource sourcex = qioTransferHelper.getSource(HashedItem.raw(currentStored));
               return sourcex == null ? currentStored.m_41613_() : sourcex.getSlotRemaining((byte)(slot + 9));
            }
         };
         Object2IntMap<HashedItem> stillLeftOver = simulatedInventory.shuffleInputs(leftOverInput, frequency != null);
         if (stillLeftOver == null) {
            return false;
         }

         if (!stillLeftOver.isEmpty() && frequency != null) {
            int availableItemTypes = frequency.getTotalItemTypeCapacity() - frequency.getTotalItemTypes(true);
            long availableItemSpace = frequency.getTotalItemCountCapacity() - frequency.getTotalItemCount();
            Object2BooleanMap<QIOCraftingTransferHelper.HashedItemSource> usedQIOSource = new Object2BooleanArrayMap(shuffleLookup.size());

            for (java.util.Map.Entry<QIOCraftingTransferHelper.HashedItemSource, List<List<QIOCraftingTransferHelper.SingularHashedItemSource>>> entry : shuffleLookup.entrySet()) {
               QIOCraftingTransferHelper.HashedItemSource sourcex = entry.getKey();
               boolean usedQIO = false;

               for (List<QIOCraftingTransferHelper.SingularHashedItemSource> usedSources : entry.getValue()) {
                  for (QIOCraftingTransferHelper.SingularHashedItemSource usedSource : usedSources) {
                     UUID qioSource = usedSource.getQioSource();
                     if (qioSource != null) {
                        availableItemSpace += usedSource.getUsed();
                        if (sourcex.getQIORemaining(qioSource) == 0L) {
                           availableItemTypes++;
                           usedQIO = true;
                        }
                     }
                  }
               }

               usedQIOSource.put(sourcex, usedQIO);
            }

            ObjectIterator var26 = stillLeftOver.object2IntEntrySet().iterator();

            while (var26.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem> entry = (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<HashedItem>)var26.next();
               availableItemSpace -= entry.getIntValue();
               if (availableItemSpace <= 0L) {
                  return false;
               }

               QIOCraftingTransferHelper.HashedItemSource sourcex = qioTransferHelper.getSource((HashedItem)entry.getKey());
               if (sourcex == null) {
                  return false;
               }

               if (sourcex.hasQIOSources()) {
                  if (usedQIOSource.containsKey(sourcex) && usedQIOSource.getBoolean(sourcex)) {
                     if (--availableItemTypes <= 0) {
                        return false;
                     }
                  }
               } else if (--availableItemTypes <= 0) {
                  return false;
               }
            }
         }
      }

      return true;
   }
}
