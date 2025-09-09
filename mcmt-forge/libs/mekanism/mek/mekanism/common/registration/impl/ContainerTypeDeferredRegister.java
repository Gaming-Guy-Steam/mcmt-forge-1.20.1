package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.entity.robit.RobitContainer;
import mekanism.common.inventory.container.tile.EmptyTileContainer;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.registration.INamedEntry;
import mekanism.common.registration.WrappedDeferredRegister;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ContainerTypeDeferredRegister extends WrappedDeferredRegister<MenuType<?>> {
   public ContainerTypeDeferredRegister(String modid) {
      super(modid, ForgeRegistries.MENU_TYPES);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(INamedEntry nameProvider, Class<TILE> tileClass) {
      return this.register(nameProvider.getInternalRegistryName(), tileClass);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<MekanismTileContainer<TILE>> register(String name, Class<TILE> tileClass) {
      ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
      MekanismContainerType.IMekanismContainerFactory<TILE, MekanismTileContainer<TILE>> factory = (id, inv, data) -> new MekanismTileContainer<>(
         registryObject, id, inv, data
      );
      return this.register(name, () -> MekanismContainerType.tile(tileClass, factory), registryObject::setRegistryObject);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(INamedEntry nameProvider, Class<TILE> tileClass) {
      return this.registerEmpty(nameProvider.getInternalRegistryName(), tileClass);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registerEmpty(String name, Class<TILE> tileClass) {
      ContainerTypeRegistryObject<EmptyTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
      MekanismContainerType.IMekanismContainerFactory<TILE, EmptyTileContainer<TILE>> factory = (id, inv, data) -> new EmptyTileContainer<>(
         registryObject, id, inv, data
      );
      return this.register(name, () -> MekanismContainerType.tile(tileClass, factory), registryObject::setRegistryObject);
   }

   public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(
      INamedEntry nameProvider, Class<TILE> tileClass, MekanismContainerType.IMekanismContainerFactory<TILE, CONTAINER> factory
   ) {
      return this.register(nameProvider.getInternalRegistryName(), tileClass, factory);
   }

   public <TILE extends TileEntityMekanism, CONTAINER extends MekanismTileContainer<TILE>> ContainerTypeRegistryObject<CONTAINER> register(
      String name, Class<TILE> tileClass, MekanismContainerType.IMekanismContainerFactory<TILE, CONTAINER> factory
   ) {
      return this.register(name, (Supplier<MenuType<CONTAINER>>)(() -> MekanismContainerType.tile(tileClass, factory)));
   }

   public <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> ContainerTypeRegistryObject<CONTAINER> registerEntity(
      String name, Class<ENTITY> entityClass, MekanismContainerType.IMekanismContainerFactory<ENTITY, CONTAINER> factory
   ) {
      return this.register(name, (Supplier<MenuType<CONTAINER>>)(() -> MekanismContainerType.entity(entityClass, factory)));
   }

   public ContainerTypeRegistryObject<RobitContainer> register(String name) {
      ContainerTypeRegistryObject<RobitContainer> registryObject = new ContainerTypeRegistryObject<>(null);
      MekanismContainerType.IMekanismContainerFactory<EntityRobit, RobitContainer> factory = (id, inv, data) -> new RobitContainer(
         registryObject, id, inv, data
      );
      return this.register(name, () -> MekanismContainerType.entity(EntityRobit.class, factory), registryObject::setRegistryObject);
   }

   public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(
      INamedEntry nameProvider, Class<ITEM> itemClass, MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER> factory
   ) {
      return this.register(nameProvider.getInternalRegistryName(), itemClass, factory);
   }

   public <ITEM extends Item, CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(
      String name, Class<ITEM> itemClass, MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER> factory
   ) {
      return this.register(name, (Supplier<MenuType<CONTAINER>>)(() -> MekanismItemContainerType.item(itemClass, factory)));
   }

   public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, IContainerFactory<CONTAINER> factory) {
      return this.register(name, (Supplier<MenuType<CONTAINER>>)(() -> new MenuType(factory, FeatureFlags.f_244377_)));
   }

   public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(
      INamedEntry nameProvider, Supplier<MenuType<CONTAINER>> supplier
   ) {
      return this.register(nameProvider.getInternalRegistryName(), supplier);
   }

   public <CONTAINER extends AbstractContainerMenu> ContainerTypeRegistryObject<CONTAINER> register(String name, Supplier<MenuType<CONTAINER>> supplier) {
      return this.register(name, supplier, ContainerTypeRegistryObject::new);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeDeferredRegister.ContainerBuilder<TILE> custom(INamedEntry nameProvider, Class<TILE> tileClass) {
      return this.custom(nameProvider.getInternalRegistryName(), tileClass);
   }

   public <TILE extends TileEntityMekanism> ContainerTypeDeferredRegister.ContainerBuilder<TILE> custom(String name, Class<TILE> tileClass) {
      return new ContainerTypeDeferredRegister.ContainerBuilder<>(name, tileClass);
   }

   public class ContainerBuilder<TILE extends TileEntityMekanism> {
      private final String name;
      private final Class<TILE> tileClass;
      private int offsetX;
      private int offsetY;
      private int armorSlotsX = -1;
      private int armorSlotsY = -1;
      private int offhandOffset = -1;

      private ContainerBuilder(String name, Class<TILE> tileClass) {
         this.name = name;
         this.tileClass = tileClass;
      }

      public ContainerTypeDeferredRegister.ContainerBuilder<TILE> offset(int offsetX, int offsetY) {
         this.offsetX = offsetX;
         this.offsetY = offsetY;
         return this;
      }

      public ContainerTypeDeferredRegister.ContainerBuilder<TILE> armorSideBar() {
         return this.armorSideBar(-20, 67, 0);
      }

      public ContainerTypeDeferredRegister.ContainerBuilder<TILE> armorSideBar(int armorSlotsX, int armorSlotsY) {
         return this.armorSideBar(armorSlotsX, armorSlotsY, -1);
      }

      public ContainerTypeDeferredRegister.ContainerBuilder<TILE> armorSideBar(int armorSlotsX, int armorSlotsY, int offhandOffset) {
         this.armorSlotsX = armorSlotsX;
         this.armorSlotsY = armorSlotsY;
         this.offhandOffset = offhandOffset;
         return this;
      }

      public ContainerTypeRegistryObject<MekanismTileContainer<TILE>> build() {
         ContainerTypeRegistryObject<MekanismTileContainer<TILE>> registryObject = new ContainerTypeRegistryObject<>(null);
         MekanismContainerType.IMekanismContainerFactory<TILE, MekanismTileContainer<TILE>> factory = (id, inv, data) -> new MekanismTileContainer<TILE>(
            registryObject, id, inv, data
         ) {
            @Override
            protected int getInventoryXOffset() {
               return super.getInventoryXOffset() + ContainerBuilder.this.offsetX;
            }

            @Override
            protected int getInventoryYOffset() {
               return super.getInventoryYOffset() + ContainerBuilder.this.offsetY;
            }

            @Override
            protected void addInventorySlots(@NotNull Inventory inv) {
               super.addInventorySlots(inv);
               if (ContainerBuilder.this.armorSlotsX != -1 && ContainerBuilder.this.armorSlotsY != -1) {
                  this.addArmorSlots(inv, ContainerBuilder.this.armorSlotsX, ContainerBuilder.this.armorSlotsY, ContainerBuilder.this.offhandOffset);
               }
            }
         };
         return ContainerTypeDeferredRegister.this.register(
            this.name, () -> MekanismContainerType.tile(this.tileClass, factory), registryObject::setRegistryObject
         );
      }
   }
}
