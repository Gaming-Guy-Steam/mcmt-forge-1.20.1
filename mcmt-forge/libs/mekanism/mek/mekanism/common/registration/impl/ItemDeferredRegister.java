package mekanism.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.content.gear.ModuleHelper;
import mekanism.common.item.ItemModule;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ItemDeferredRegister extends WrappedDeferredRegister<Item> {
   private final List<IItemProvider> allItems = new ArrayList<>();

   public ItemDeferredRegister(String modid) {
      super(modid, ForgeRegistries.ITEMS);
   }

   public ItemRegistryObject<Item> register(String name) {
      return this.register(name, Item::new);
   }

   public ItemRegistryObject<Item> registerUnburnable(String name) {
      return this.registerUnburnable(name, Item::new);
   }

   public ItemRegistryObject<Item> register(String name, Rarity rarity) {
      return this.register(name, (Function<Properties, Item>)(properties -> new Item(properties.m_41497_(rarity))));
   }

   public ItemRegistryObject<Item> register(String name, EnumColor color) {
      return this.register(name, (Function<Properties, Item>)(properties -> new Item(properties) {
         @NotNull
         public Component m_7626_(@NotNull ItemStack stack) {
            return TextComponentUtil.build(color, super.m_7626_(stack));
         }
      }));
   }

   public ItemRegistryObject<ItemModule> registerModule(ModuleRegistryObject<?> moduleDataSupplier) {
      return this.register(
         "module_" + moduleDataSupplier.getInternalRegistryName(),
         (Supplier<? extends ItemModule>)(() -> ModuleHelper.get().createModuleItem(moduleDataSupplier, new Properties()))
      );
   }

   public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Function<Properties, ITEM> sup) {
      return this.register(name, (Supplier<? extends ITEM>)(() -> sup.apply(new Properties())));
   }

   public <ITEM extends Item> ItemRegistryObject<ITEM> registerUnburnable(String name, Function<Properties, ITEM> sup) {
      return this.register(name, (Supplier<? extends ITEM>)(() -> sup.apply(new Properties().m_41486_())));
   }

   public <ITEM extends Item> ItemRegistryObject<ITEM> register(String name, Supplier<? extends ITEM> sup) {
      ItemRegistryObject<ITEM> registeredItem = this.register(name, sup, ItemRegistryObject::new);
      this.allItems.add(registeredItem);
      return registeredItem;
   }

   public <ENTITY extends Mob> ItemRegistryObject<ForgeSpawnEggItem> registerSpawnEgg(
      EntityTypeRegistryObject<ENTITY> entityTypeProvider, int primaryColor, int secondaryColor
   ) {
      return this.register(
         entityTypeProvider.getInternalRegistryName() + "_spawn_egg",
         (Function)(props -> new ForgeSpawnEggItem(entityTypeProvider, primaryColor, secondaryColor, props))
      );
   }

   public List<IItemProvider> getAllItems() {
      return Collections.unmodifiableList(this.allItems);
   }
}
