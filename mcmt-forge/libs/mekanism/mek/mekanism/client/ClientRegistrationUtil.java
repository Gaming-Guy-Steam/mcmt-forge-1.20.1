package mekanism.client;

import java.lang.ref.WeakReference;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.machine.GuiAdvancedElectricMachine;
import mekanism.client.gui.machine.GuiElectricMachine;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.block.interfaces.IColoredBlock;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.item.interfaces.IColoredItem;
import mekanism.common.registration.impl.ContainerTypeRegistryObject;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.prefab.TileEntityAdvancedElectricMachine;
import mekanism.common.tile.prefab.TileEntityElectricMachine;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterColorHandlersEvent.Block;
import net.minecraftforge.client.event.RegisterColorHandlersEvent.Item;
import net.minecraftforge.client.model.DynamicFluidContainerModel.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientRegistrationUtil {
   private static final BlockColor COLORED_BLOCK_COLOR = (state, world, pos, tintIndex) -> state.m_60734_() instanceof IColoredBlock coloredBlock
      ? MekanismRenderer.getColorARGB(coloredBlock.getColor(), 1.0F)
      : -1;
   private static final ItemColor COLORED_BLOCK_ITEM_COLOR = (stack, tintIndex) -> stack.m_41720_() instanceof BlockItem blockItem
         && blockItem.m_40614_() instanceof IColoredBlock coloredBlock
      ? MekanismRenderer.getColorARGB(coloredBlock.getColor(), 1.0F)
      : -1;
   private static final ItemColor COLORED_ITEM_COLOR = (stack, tintIndex) -> {
      if (tintIndex == 1 && stack.m_41720_() instanceof IColoredItem coloredItem) {
         EnumColor color = coloredItem.getColor(stack);
         return color != null ? MekanismRenderer.getColorARGB(color, 1.0F) : -11184811;
      } else {
         return -1;
      }
   };
   private static final ItemColor BUCKET_ITEM_COLOR = new Colors();

   private ClientRegistrationUtil() {
   }

   @SafeVarargs
   public static <T extends BlockEntity> void bindTileEntityRenderer(
      RegisterRenderers event, BlockEntityRendererProvider<T> rendererProvider, TileEntityTypeRegistryObject<? extends T>... tileEntityTypeROs
   ) {
      if (tileEntityTypeROs.length == 0) {
         throw new IllegalArgumentException("No renderers provided.");
      } else {
         if (tileEntityTypeROs.length == 1) {
            event.registerBlockEntityRenderer(tileEntityTypeROs[0].get(), rendererProvider);
         } else {
            BlockEntityRendererProvider<T> provider = new BlockEntityRendererProvider<T>() {
               @Nullable
               private WeakReference<Context> cachedContext;
               @Nullable
               private WeakReference<BlockEntityRenderer<T>> cachedRenderer;

               @NotNull
               public BlockEntityRenderer<T> m_173570_(@NotNull Context context) {
                  BlockEntityRenderer<T> renderer = this.cachedRenderer == null ? null : this.cachedRenderer.get();
                  if (this.cachedContext == null || this.cachedContext.get() != context || renderer == null) {
                     renderer = rendererProvider.m_173570_(context);
                     this.cachedContext = new WeakReference<>(context);
                     this.cachedRenderer = new WeakReference<>(renderer);
                  }

                  return renderer;
               }
            };

            for (TileEntityTypeRegistryObject<? extends T> tileTypeRO : tileEntityTypeROs) {
               event.registerBlockEntityRenderer(tileTypeRO.get(), provider);
            }
         }
      }
   }

   public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event, PreparableReloadListener... listeners) {
      for (PreparableReloadListener listener : listeners) {
         event.registerReloadListener(listener);
      }
   }

   public static <C extends AbstractContainerMenu, U extends Screen & MenuAccess<C>> void registerScreen(
      ContainerTypeRegistryObject<C> type, ScreenConstructor<C, U> factory
   ) {
      MenuScreens.m_96206_(type.get(), factory);
   }

   public static <TILE extends TileEntityElectricMachine, C extends MekanismTileContainer<TILE>> void registerElectricScreen(
      ContainerTypeRegistryObject<C> type
   ) {
      registerScreen(type, new ScreenConstructor<C, GuiElectricMachine<TILE, C>>() {
         @NotNull
         public GuiElectricMachine<TILE, C> create(@NotNull C container, @NotNull Inventory inv, @NotNull Component title) {
            return new GuiElectricMachine<>(container, inv, title);
         }
      });
   }

   public static <TILE extends TileEntityAdvancedElectricMachine, C extends MekanismTileContainer<TILE>> void registerAdvancedElectricScreen(
      ContainerTypeRegistryObject<C> type
   ) {
      registerScreen(type, new ScreenConstructor<C, GuiAdvancedElectricMachine<TILE, C>>() {
         @NotNull
         public GuiAdvancedElectricMachine<TILE, C> create(@NotNull C container, @NotNull Inventory inv, @NotNull Component title) {
            return new GuiAdvancedElectricMachine<>(container, inv, title);
         }
      });
   }

   public static void registerKeyBindings(RegisterKeyMappingsEvent event, KeyMapping... keys) {
      for (KeyMapping key : keys) {
         event.register(key);
      }
   }

   public static void setPropertyOverride(IItemProvider itemProvider, ResourceLocation override, ItemPropertyFunction propertyGetter) {
      ItemProperties.register(itemProvider.m_5456_(), override, propertyGetter);
   }

   public static void registerItemColorHandler(Item event, ItemColor itemColor, IItemProvider... items) {
      for (IItemProvider itemProvider : items) {
         event.register(itemColor, new ItemLike[]{itemProvider.m_5456_()});
      }
   }

   public static void registerBlockColorHandler(Block event, BlockColor blockColor, IBlockProvider... blocks) {
      for (IBlockProvider blockProvider : blocks) {
         event.register(blockColor, new net.minecraft.world.level.block.Block[]{blockProvider.getBlock()});
      }
   }

   public static void registerBucketColorHandler(Item event, FluidDeferredRegister register) {
      for (FluidRegistryObject<? extends FluidDeferredRegister.MekanismFluidType, ?, ?, ?, ?> fluidRO : register.getAllFluids()) {
         event.register(BUCKET_ITEM_COLOR, new ItemLike[]{fluidRO.getBucket()});
      }
   }

   public static void registerIColoredBlockHandler(RegisterColorHandlersEvent event, IBlockProvider... blocks) {
      if (event instanceof Block blockEvent) {
         registerBlockColorHandler(blockEvent, COLORED_BLOCK_COLOR, blocks);
      } else if (event instanceof Item itemEvent) {
         registerItemColorHandler(itemEvent, COLORED_BLOCK_ITEM_COLOR, blocks);
      }
   }

   public static void registerIColoredItemHandler(Item event, IItemProvider... items) {
      registerItemColorHandler(event, COLORED_ITEM_COLOR, items);
   }

   public static void setRenderLayer(RenderType type, FluidRegistryObject<?, ?, ?, ?, ?>... fluidROs) {
      for (FluidRegistryObject<?, ?, ?, ?, ?> fluidRO : fluidROs) {
         ItemBlockRenderTypes.setRenderLayer(fluidRO.getStillFluid(), type);
         ItemBlockRenderTypes.setRenderLayer(fluidRO.getFlowingFluid(), type);
      }
   }
}
