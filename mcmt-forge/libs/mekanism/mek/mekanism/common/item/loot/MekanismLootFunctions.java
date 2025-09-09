package mekanism.common.item.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class MekanismLootFunctions {
   public static final DeferredRegister<LootItemFunctionType> REGISTER = DeferredRegister.create(Registries.f_257015_, "mekanism");
   public static final RegistryObject<LootItemFunctionType> PERSONAL_STORAGE_LOOT_FUNC = REGISTER.register(
      "personal_storage_contents", () -> new LootItemFunctionType(new PersonalStorageContentsLootFunction.PersonalStorageLootFunctionSerializer())
   );
}
