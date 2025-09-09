package mekanism.common.registration.impl;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.type.MekanismContainerType;
import mekanism.common.inventory.container.type.MekanismItemContainerType;
import mekanism.common.registration.WrappedRegistryObject;
import mekanism.common.util.RegistryUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class ContainerTypeRegistryObject<CONTAINER extends AbstractContainerMenu> extends WrappedRegistryObject<MenuType<CONTAINER>> {
   public ContainerTypeRegistryObject(RegistryObject<MenuType<CONTAINER>> registryObject) {
      super(registryObject);
   }

   ContainerTypeRegistryObject<CONTAINER> setRegistryObject(RegistryObject<MenuType<CONTAINER>> registryObject) {
      this.registryObject = registryObject;
      return this;
   }

   @Nullable
   public MenuProvider getProvider(ILangEntry name, Object object) {
      return this.getProvider(name.translate(), object);
   }

   @Nullable
   public MenuProvider getProvider(Component name, Object object) {
      MenuConstructor provider = null;
      MenuType<CONTAINER> containerType = this.get();
      if (containerType instanceof MekanismContainerType<?, CONTAINER> mekanismContainerType) {
         provider = mekanismContainerType.create(object);
      }

      if (provider == null) {
         Mekanism.logger.info("Unable to create container for type: {}", RegistryUtils.getName(containerType));
      }

      return provider == null ? null : new ContainerProvider(name, provider);
   }

   @Nullable
   public MenuProvider getProvider(ILangEntry name, InteractionHand hand, ItemStack stack) {
      return this.getProvider(name.translate(), hand, stack);
   }

   @Nullable
   public MenuProvider getProvider(Component name, InteractionHand hand, ItemStack stack) {
      MenuConstructor provider = null;
      MenuType<CONTAINER> containerType = this.get();
      if (containerType instanceof MekanismItemContainerType<?, ?> mekanismItemContainerType) {
         provider = mekanismItemContainerType.create(hand, stack);
      }

      if (provider == null) {
         Mekanism.logger.info("Unable to create container for type: {}", RegistryUtils.getName(containerType));
      }

      return provider == null ? null : new ContainerProvider(name, provider);
   }

   public void tryOpenGui(ServerPlayer player, InteractionHand hand, ItemStack stack) {
      MenuProvider provider = this.getProvider(stack.m_41786_(), hand, stack);
      if (provider != null) {
         NetworkHooks.openScreen(player, provider, buf -> {
            buf.m_130068_(hand);
            buf.m_130055_(stack);
         });
      }
   }
}
