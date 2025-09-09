package mekanism.common.network.to_server;

import java.util.function.BiFunction;
import java.util.function.Function;
import mekanism.common.MekanismLang;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.item.interfaces.IGuiItem;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.multiblock.TileEntityBoilerCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiButtonPress implements IMekanismPacket {
   private final PacketGuiButtonPress.Type type;
   private PacketGuiButtonPress.ClickedItemButton itemButton;
   private PacketGuiButtonPress.ClickedTileButton tileButton;
   private PacketGuiButtonPress.ClickedEntityButton entityButton;
   private InteractionHand hand;
   private int entityID;
   private int extra;
   private BlockPos tilePosition;

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton buttonClicked, BlockEntity tile) {
      this(buttonClicked, tile.m_58899_());
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton buttonClicked, BlockEntity tile, int extra) {
      this(buttonClicked, tile.m_58899_(), extra);
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton buttonClicked, BlockPos tilePosition) {
      this(buttonClicked, tilePosition, 0);
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedItemButton buttonClicked, InteractionHand hand) {
      this.type = PacketGuiButtonPress.Type.ITEM;
      this.itemButton = buttonClicked;
      this.hand = hand;
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedTileButton buttonClicked, BlockPos tilePosition, int extra) {
      this.type = PacketGuiButtonPress.Type.TILE;
      this.tileButton = buttonClicked;
      this.tilePosition = tilePosition;
      this.extra = extra;
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton buttonClicked, Entity entity) {
      this(buttonClicked, entity.m_19879_());
   }

   public PacketGuiButtonPress(PacketGuiButtonPress.ClickedEntityButton buttonClicked, int entityID) {
      this.type = PacketGuiButtonPress.Type.ENTITY;
      this.entityButton = buttonClicked;
      this.entityID = entityID;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null) {
         if (this.type == PacketGuiButtonPress.Type.ENTITY) {
            Entity entity = player.m_9236_().m_6815_(this.entityID);
            if (entity != null) {
               MenuProvider provider = this.entityButton.getProvider(entity);
               if (provider != null) {
                  NetworkHooks.openScreen(player, provider, buf -> buf.m_130130_(this.entityID));
               }
            }
         } else if (this.type == PacketGuiButtonPress.Type.TILE) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.m_9236_(), this.tilePosition);
            if (tile != null) {
               MenuProvider provider = this.tileButton.getProvider(tile, this.extra);
               if (provider != null) {
                  NetworkHooks.openScreen(player, provider, buf -> {
                     buf.m_130064_(this.tilePosition);
                     buf.m_130130_(this.extra);
                  });
               }
            }
         } else if (this.type == PacketGuiButtonPress.Type.ITEM) {
            ItemStack stack = player.m_21120_(this.hand);
            if (stack.m_41720_() instanceof IGuiItem) {
               MenuProvider provider = this.itemButton.getProvider(stack, this.hand);
               if (provider != null) {
                  NetworkHooks.openScreen(player, provider, buf -> {
                     buf.m_130068_(this.hand);
                     buf.m_130055_(stack);
                  });
               }
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.type);
      switch (this.type) {
         case ENTITY:
            buffer.m_130068_(this.entityButton);
            buffer.m_130130_(this.entityID);
            break;
         case TILE:
            buffer.m_130068_(this.tileButton);
            buffer.m_130064_(this.tilePosition);
            buffer.m_130130_(this.extra);
            break;
         case ITEM:
            buffer.m_130068_(this.itemButton);
            buffer.m_130068_(this.hand);
      }
   }

   public static PacketGuiButtonPress decode(FriendlyByteBuf buffer) {
      return switch ((PacketGuiButtonPress.Type)buffer.m_130066_(PacketGuiButtonPress.Type.class)) {
         case ENTITY -> new PacketGuiButtonPress(
            (PacketGuiButtonPress.ClickedEntityButton)buffer.m_130066_(PacketGuiButtonPress.ClickedEntityButton.class), buffer.m_130242_()
         );
         case TILE -> new PacketGuiButtonPress(
            (PacketGuiButtonPress.ClickedTileButton)buffer.m_130066_(PacketGuiButtonPress.ClickedTileButton.class), buffer.m_130135_(), buffer.m_130242_()
         );
         case ITEM -> new PacketGuiButtonPress(
            (PacketGuiButtonPress.ClickedItemButton)buffer.m_130066_(PacketGuiButtonPress.ClickedItemButton.class),
            (InteractionHand)buffer.m_130066_(InteractionHand.class)
         );
      };
   }

   public static enum ClickedEntityButton {
      ROBIT_CRAFTING(entity -> MekanismContainerTypes.CRAFTING_ROBIT.getProvider(MekanismLang.ROBIT_CRAFTING, entity)),
      ROBIT_INVENTORY(entity -> MekanismContainerTypes.INVENTORY_ROBIT.getProvider(MekanismLang.ROBIT_INVENTORY, entity)),
      ROBIT_MAIN(entity -> MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, entity)),
      ROBIT_REPAIR(entity -> MekanismContainerTypes.REPAIR_ROBIT.getProvider(MekanismLang.ROBIT_REPAIR, entity)),
      ROBIT_SMELTING(entity -> MekanismContainerTypes.SMELTING_ROBIT.getProvider(MekanismLang.ROBIT_SMELTING, entity));

      private final Function<Entity, MenuProvider> providerFromEntity;

      private ClickedEntityButton(Function<Entity, MenuProvider> providerFromEntity) {
         this.providerFromEntity = providerFromEntity;
      }

      public MenuProvider getProvider(Entity entity) {
         return this.providerFromEntity.apply(entity);
      }
   }

   public static enum ClickedItemButton {
      BACK_BUTTON((stack, hand) -> stack.m_41720_() instanceof IGuiItem guiItem ? guiItem.getContainerType().getProvider(stack.m_41786_(), hand, stack) : null),
      QIO_FREQUENCY_SELECT((stack, hand) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_ITEM.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, hand, stack));

      private final BiFunction<ItemStack, InteractionHand, MenuProvider> providerFromItem;

      private ClickedItemButton(BiFunction<ItemStack, InteractionHand, MenuProvider> providerFromItem) {
         this.providerFromItem = providerFromItem;
      }

      public MenuProvider getProvider(ItemStack stack, InteractionHand hand) {
         return this.providerFromItem.apply(stack, hand);
      }
   }

   public static enum ClickedTileButton {
      BACK_BUTTON((tile, extra) -> {
         AttributeGui attributeGui = Attribute.get(tile.getBlockType(), AttributeGui.class);
         return attributeGui != null ? attributeGui.getProvider(tile) : null;
      }),
      QIO_FREQUENCY_SELECT((tile, extra) -> MekanismContainerTypes.QIO_FREQUENCY_SELECT_TILE.getProvider(MekanismLang.QIO_FREQUENCY_SELECT, tile)),
      DIGITAL_MINER_CONFIG((tile, extra) -> MekanismContainerTypes.DIGITAL_MINER_CONFIG.getProvider(MekanismLang.MINER_CONFIG, tile)),
      TAB_MAIN((tile, extra) -> {
         if (tile instanceof TileEntityInductionCasing) {
            return MekanismContainerTypes.INDUCTION_MATRIX.getProvider(MekanismLang.MATRIX, tile);
         } else {
            return tile instanceof TileEntityBoilerCasing ? MekanismContainerTypes.THERMOELECTRIC_BOILER.getProvider(MekanismLang.BOILER, tile) : null;
         }
      }),
      TAB_STATS((tile, extra) -> {
         if (tile instanceof TileEntityInductionCasing) {
            return MekanismContainerTypes.MATRIX_STATS.getProvider(MekanismLang.MATRIX_STATS, tile);
         } else {
            return tile instanceof TileEntityBoilerCasing ? MekanismContainerTypes.BOILER_STATS.getProvider(MekanismLang.BOILER_STATS, tile) : null;
         }
      });

      private final BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile;

      private ClickedTileButton(BiFunction<TileEntityMekanism, Integer, MenuProvider> providerFromTile) {
         this.providerFromTile = providerFromTile;
      }

      public MenuProvider getProvider(TileEntityMekanism tile, int extra) {
         return this.providerFromTile.apply(tile, extra);
      }
   }

   public static enum Type {
      TILE,
      ITEM,
      ENTITY;
   }
}
