package mekanism.common.item.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.ClientSidePersonalStorageInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.tile.TileEntityPersonalStorage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.util.thread.EffectiveSide;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public class PersonalStorageContentsLootFunction implements LootItemFunction {
   private static final PersonalStorageContentsLootFunction INSTANCE = new PersonalStorageContentsLootFunction();
   private static final Set<LootContextParam<?>> REFERENCED_PARAMS = Set.of(LootContextParams.f_81462_);

   private PersonalStorageContentsLootFunction() {
   }

   public static Builder builder() {
      return () -> INSTANCE;
   }

   public LootItemFunctionType m_7162_() {
      return (LootItemFunctionType)MekanismLootFunctions.PERSONAL_STORAGE_LOOT_FUNC.get();
   }

   public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
      BlockEntity blockEntity = (BlockEntity)lootContext.m_165124_(LootContextParams.f_81462_);
      if (blockEntity instanceof TileEntityPersonalStorage personalStorage && !personalStorage.isInventoryEmpty()) {
         List<IInventorySlot> tileSlots = personalStorage.getInventorySlots(null);
         AbstractPersonalStorageItemInventory destInv;
         if (EffectiveSide.get().isClient()) {
            destInv = new ClientSidePersonalStorageInventory();
         } else {
            destInv = PersonalStorageManager.getInventoryFor(itemStack).orElseThrow(() -> new IllegalStateException("Inventory not available?!"));
         }

         for (int i = 0; i < tileSlots.size(); i++) {
            IInventorySlot tileSlot = tileSlots.get(i);
            if (!tileSlot.isEmpty()) {
               destInv.setStackInSlot(i, tileSlot.getStack().m_41777_());
            }
         }
      }

      return itemStack;
   }

   public Set<LootContextParam<?>> m_6231_() {
      return REFERENCED_PARAMS;
   }

   public static class PersonalStorageLootFunctionSerializer implements Serializer<PersonalStorageContentsLootFunction> {
      public void serialize(JsonObject pJson, PersonalStorageContentsLootFunction pValue, JsonSerializationContext pSerializationContext) {
      }

      public PersonalStorageContentsLootFunction deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         return PersonalStorageContentsLootFunction.INSTANCE;
      }
   }
}
