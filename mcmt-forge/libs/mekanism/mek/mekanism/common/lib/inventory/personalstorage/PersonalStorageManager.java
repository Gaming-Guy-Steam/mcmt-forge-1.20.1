package mekanism.common.lib.inventory.personalstorage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import mekanism.api.AutomationType;
import mekanism.api.DataHandlerUtils;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.Mekanism;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.lib.MekanismSavedData;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageManager {
   private static final Map<UUID, PersonalStorageData> STORAGE_BY_PLAYER_UUID = new HashMap<>();

   private static Optional<PersonalStorageData> forOwner(UUID playerUUID) {
      return EffectiveSide.get().isClient()
         ? Optional.empty()
         : Optional.of(
            STORAGE_BY_PLAYER_UUID.computeIfAbsent(
               playerUUID, uuid -> MekanismSavedData.createSavedData(PersonalStorageData::new, "personal_storage" + File.separator + uuid)
            )
         );
   }

   public static Optional<AbstractPersonalStorageItemInventory> getInventoryFor(ItemStack stack) {
      UUID owner = SecurityUtils.get().getOwnerUUID(stack);
      if (owner == null) {
         Mekanism.logger.error("Storage inventory asked for but stack has no owner! {}", stack, new Exception());
         return Optional.empty();
      } else {
         UUID invId = getInventoryId(stack);
         return forOwner(owner).map(data -> {
            AbstractPersonalStorageItemInventory storageItemInventory = data.getOrAddInventory(invId);
            ListTag legacyData = ItemDataUtils.getList(stack, "Items");
            if (!legacyData.isEmpty()) {
               DataHandlerUtils.readContainers(storageItemInventory.getInventorySlots(null), legacyData);
               ItemDataUtils.removeData(stack, "Items");
            }

            return storageItemInventory;
         });
      }
   }

   public static boolean createInventoryFor(ItemStack stack, List<IInventorySlot> contents) {
      UUID owner = SecurityUtils.get().getOwnerUUID(stack);
      if (owner != null && contents.size() == 54) {
         forOwner(owner).ifPresent(inv -> inv.addInventory(getInventoryId(stack), contents));
         return true;
      } else {
         return false;
      }
   }

   public static Optional<AbstractPersonalStorageItemInventory> getInventoryIfPresent(ItemStack stack) {
      UUID owner = SecurityUtils.get().getOwnerUUID(stack);
      UUID invId = getInventoryIdNullable(stack);
      boolean hasLegacyData = ItemDataUtils.hasData(stack, "Items", 9);
      return owner == null || invId == null && !hasLegacyData ? Optional.empty() : getInventoryFor(stack);
   }

   public static void deleteInventory(ItemStack stack) {
      UUID owner = SecurityUtils.get().getOwnerUUID(stack);
      UUID invId = getInventoryIdNullable(stack);
      if (owner != null && invId != null) {
         forOwner(owner).ifPresent(handler -> handler.removeInventory(invId));
      }
   }

   @NotNull
   private static UUID getInventoryId(ItemStack stack) {
      UUID invId = getInventoryIdNullable(stack);
      if (invId == null) {
         invId = UUID.randomUUID();
         ItemDataUtils.setUUID(stack, "personalStorageId", invId);
      }

      return invId;
   }

   @Nullable
   private static UUID getInventoryIdNullable(ItemStack stack) {
      return ItemDataUtils.getUniqueID(stack, "personalStorageId");
   }

   public static void reset() {
      STORAGE_BY_PLAYER_UUID.clear();
   }

   public static void createSlots(
      Consumer<IInventorySlot> slotConsumer, BiPredicate<ItemStack, AutomationType> canInteract, @Nullable IContentsListener listener
   ) {
      for (int slotY = 0; slotY < 6; slotY++) {
         for (int slotX = 0; slotX < 9; slotX++) {
            slotConsumer.accept(BasicInventorySlot.at(canInteract, canInteract, listener, 8 + slotX * 18, 18 + slotY * 18));
         }
      }
   }
}
