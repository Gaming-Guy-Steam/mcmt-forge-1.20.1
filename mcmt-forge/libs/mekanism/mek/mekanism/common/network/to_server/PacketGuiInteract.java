package mekanism.common.network.to_server;

import mekanism.api.Upgrade;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.SecurityMode;
import mekanism.common.content.filter.SortableFilterManager;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.IHasDumpButton;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.interfaces.IHasMode;
import mekanism.common.tile.interfaces.IRedstoneControl;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.machine.TileEntityDimensionalStabilizer;
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import mekanism.common.tile.qio.TileEntityQIOImporter;
import mekanism.common.tile.qio.TileEntityQIORedstoneAdapter;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketGuiInteract implements IMekanismPacket {
   private final PacketGuiInteract.Type interactionType;
   private PacketGuiInteract.GuiInteraction interaction;
   private PacketGuiInteract.GuiInteractionItem itemInteraction;
   private PacketGuiInteract.GuiInteractionEntity entityInteraction;
   private BlockPos tilePosition;
   private ItemStack extraItem;
   private int entityID;
   private int extra;

   public PacketGuiInteract(PacketGuiInteract.GuiInteractionEntity interaction, Entity entity) {
      this(interaction, entity, 0);
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteractionEntity interaction, Entity entity, int extra) {
      this(interaction, entity.m_19879_(), extra);
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteractionEntity interaction, int entityID, int extra) {
      this.interactionType = PacketGuiInteract.Type.ENTITY;
      this.entityInteraction = interaction;
      this.entityID = entityID;
      this.extra = extra;
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteraction interaction, BlockEntity tile) {
      this(interaction, tile.m_58899_());
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteraction interaction, BlockEntity tile, int extra) {
      this(interaction, tile.m_58899_(), extra);
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteraction interaction, BlockPos tilePosition) {
      this(interaction, tilePosition, 0);
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteraction interaction, BlockPos tilePosition, int extra) {
      this.interactionType = PacketGuiInteract.Type.INT;
      this.interaction = interaction;
      this.tilePosition = tilePosition;
      this.extra = extra;
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteractionItem interaction, BlockEntity tile, ItemStack stack) {
      this(interaction, tile.m_58899_(), stack);
   }

   public PacketGuiInteract(PacketGuiInteract.GuiInteractionItem interaction, BlockPos tilePosition, ItemStack stack) {
      this.interactionType = PacketGuiInteract.Type.ITEM;
      this.itemInteraction = interaction;
      this.tilePosition = tilePosition;
      this.extraItem = stack;
   }

   @Override
   public void handle(Context context) {
      Player player = context.getSender();
      if (player != null) {
         if (this.interactionType == PacketGuiInteract.Type.ENTITY) {
            Entity entity = player.m_9236_().m_6815_(this.entityID);
            if (entity != null) {
               this.entityInteraction.consume(entity, player, this.extra);
            }
         } else {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.m_9236_(), this.tilePosition);
            if (tile != null) {
               if (this.interactionType == PacketGuiInteract.Type.INT) {
                  this.interaction.consume(tile, player, this.extra);
               } else if (this.interactionType == PacketGuiInteract.Type.ITEM) {
                  this.itemInteraction.consume(tile, player, this.extraItem);
               }
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.interactionType);
      switch (this.interactionType) {
         case ENTITY:
            buffer.m_130068_(this.entityInteraction);
            buffer.m_130130_(this.entityID);
            buffer.m_130130_(this.extra);
            break;
         case INT:
            buffer.m_130068_(this.interaction);
            buffer.m_130064_(this.tilePosition);
            buffer.m_130130_(this.extra);
            break;
         case ITEM:
            buffer.m_130068_(this.itemInteraction);
            buffer.m_130064_(this.tilePosition);
            buffer.m_130055_(this.extraItem);
      }
   }

   public static PacketGuiInteract decode(FriendlyByteBuf buffer) {
      return switch ((PacketGuiInteract.Type)buffer.m_130066_(PacketGuiInteract.Type.class)) {
         case ENTITY -> new PacketGuiInteract(
            (PacketGuiInteract.GuiInteractionEntity)buffer.m_130066_(PacketGuiInteract.GuiInteractionEntity.class), buffer.m_130242_(), buffer.m_130242_()
         );
         case INT -> new PacketGuiInteract(
            (PacketGuiInteract.GuiInteraction)buffer.m_130066_(PacketGuiInteract.GuiInteraction.class), buffer.m_130135_(), buffer.m_130242_()
         );
         case ITEM -> new PacketGuiInteract(
            (PacketGuiInteract.GuiInteractionItem)buffer.m_130066_(PacketGuiInteract.GuiInteractionItem.class), buffer.m_130135_(), buffer.m_130267_()
         );
      };
   }

   public static enum GuiInteraction {
      CONTAINER_STOP_TRACKING((tile, player, extra) -> {
         if (player.f_36096_ instanceof MekanismContainer container) {
            container.stopTracking(extra);
         }
      }),
      CONTAINER_TRACK_EJECTOR((tile, player, extra) -> {
         if (player.f_36096_ instanceof MekanismContainer container && tile instanceof ISideConfiguration sideConfig) {
            container.startTrackingServer(extra, sideConfig.getEjector());
         }
      }),
      CONTAINER_TRACK_SIDE_CONFIG((tile, player, extra) -> {
         if (player.f_36096_ instanceof MekanismContainer container && tile instanceof ISideConfiguration sideConfig) {
            container.startTrackingServer(extra, sideConfig.getConfig());
         }
      }),
      CONTAINER_TRACK_UPGRADES((tile, player, extra) -> {
         if (player.f_36096_ instanceof MekanismContainer container) {
            container.startTrackingServer(extra, tile.getComponent());
         }
      }),
      QIO_REDSTONE_ADAPTER_COUNT((tile, player, extra) -> {
         if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
            redstoneAdapter.handleCountChange(extra.intValue());
         }
      }),
      QIO_REDSTONE_ADAPTER_FUZZY((tile, player, extra) -> {
         if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
            redstoneAdapter.toggleFuzzyMode();
         }
      }),
      QIO_TOGGLE_IMPORT_WITHOUT_FILTER((tile, player, extra) -> {
         if (tile instanceof TileEntityQIOImporter importer) {
            importer.toggleImportWithoutFilter();
         }
      }),
      QIO_TOGGLE_EXPORT_WITHOUT_FILTER((tile, player, extra) -> {
         if (tile instanceof TileEntityQIOExporter exporter) {
            exporter.toggleExportWithoutFilter();
         }
      }),
      AUTO_SORT_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityFactory<?> factory) {
            factory.toggleSorting();
         }
      }),
      DUMP_BUTTON((tile, player, extra) -> {
         if (tile instanceof IHasDumpButton hasDumpButton) {
            hasDumpButton.dump();
         }
      }),
      GAS_MODE_BUTTON((tile, player, extra) -> {
         if (tile instanceof IHasGasMode hasGasMode) {
            hasGasMode.nextMode(extra);
         }
      }),
      AUTO_EJECT_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.toggleAutoEject();
         } else if (tile instanceof TileEntityLogisticalSorter sorter) {
            sorter.toggleAutoEject();
         }
      }),
      AUTO_PULL_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.toggleAutoPull();
         }
      }),
      INVERSE_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.toggleInverse();
         }
      }),
      INVERSE_REQUIRES_REPLACEMENT_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.toggleInverseRequiresReplacement();
         }
      }),
      RESET_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.reset();
         }
      }),
      SILK_TOUCH_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.toggleSilkTouch();
         }
      }),
      START_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.start();
         }
      }),
      STOP_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.stop();
         }
      }),
      SET_RADIUS((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.setRadiusFromPacket(extra);
         }
      }),
      SET_MIN_Y((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.setMinYFromPacket(extra);
         }
      }),
      SET_MAX_Y((tile, player, extra) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.setMaxYFromPacket(extra);
         }
      }),
      MOVE_FILTER_UP((tile, player, extra) -> {
         if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
            manager.moveUp(extra);
         }
      }),
      MOVE_FILTER_DOWN((tile, player, extra) -> {
         if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
            manager.moveDown(extra);
         }
      }),
      MOVE_FILTER_TO_TOP((tile, player, extra) -> {
         if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
            manager.moveToTop(extra);
         }
      }),
      MOVE_FILTER_TO_BOTTOM((tile, player, extra) -> {
         if (tile instanceof ITileFilterHolder<?> filterHolder && filterHolder.getFilterManager() instanceof SortableFilterManager<?> manager) {
            manager.moveToBottom(extra);
         }
      }),
      TOGGLE_FILTER_STATE((tile, player, extra) -> {
         if (tile instanceof ITileFilterHolder<?> filterHolder) {
            filterHolder.getFilterManager().toggleState(extra);
         }
      }),
      REMOVE_UPGRADE((tile, player, extra) -> {
         if (tile.supportsUpgrades()) {
            tile.getComponent().removeUpgrade(Upgrade.byIndexStatic(extra), false);
         }
      }),
      REMOVE_ALL_UPGRADE((tile, player, extra) -> {
         if (tile.supportsUpgrades()) {
            tile.getComponent().removeUpgrade(Upgrade.byIndexStatic(extra), true);
         }
      }),
      NEXT_SECURITY_MODE((tile, player, extra) -> SecurityUtils.get().incrementSecurityMode(player, tile)),
      PREVIOUS_SECURITY_MODE((tile, player, extra) -> SecurityUtils.get().decrementSecurityMode(player, tile)),
      SECURITY_DESK_MODE((tile, player, extra) -> {
         if (tile instanceof TileEntitySecurityDesk desk) {
            desk.setSecurityDeskMode(SecurityMode.byIndexStatic(extra));
         }
      }),
      NEXT_MODE((tile, player, extra) -> {
         if (tile instanceof IHasMode hasMode) {
            hasMode.nextMode();
         }
      }),
      PREVIOUS_MODE((tile, player, extra) -> {
         if (tile instanceof IHasMode hasMode) {
            hasMode.previousMode();
         }
      }),
      NEXT_REDSTONE_CONTROL(
         (tile, player, extra) -> tile.setControlType(tile.getControlType().getNext(mode -> mode != IRedstoneControl.RedstoneControl.PULSE || tile.canPulse()))
      ),
      PREVIOUS_REDSTONE_CONTROL(
         (tile, player, extra) -> tile.setControlType(
            tile.getControlType().getPrevious(mode -> mode != IRedstoneControl.RedstoneControl.PULSE || tile.canPulse())
         )
      ),
      ENCODE_FORMULA((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.encodeFormula();
         }
      }),
      STOCK_CONTROL_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.toggleStockControl();
         }
      }),
      CRAFT_SINGLE((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.craftSingle();
         }
      }),
      CRAFT_ALL((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.craftAll();
         }
      }),
      EMPTY_GRID((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.emptyGrid();
         }
      }),
      FILL_GRID((tile, player, extra) -> {
         if (tile instanceof TileEntityFormulaicAssemblicator assemblicator) {
            assemblicator.fillGrid();
         }
      }),
      ROUND_ROBIN_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityLogisticalSorter sorter) {
            sorter.toggleRoundRobin();
         }
      }),
      SINGLE_ITEM_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntityLogisticalSorter sorter) {
            sorter.toggleSingleItem();
         }
      }),
      CHANGE_COLOR((tile, player, extra) -> {
         if (tile instanceof TileEntityLogisticalSorter sorter) {
            sorter.changeColor(TransporterUtils.readColor(extra));
         }
      }),
      OVERRIDE_BUTTON((tile, player, extra) -> {
         if (tile instanceof TileEntitySecurityDesk desk) {
            desk.toggleOverride();
         }
      }),
      REMOVE_TRUSTED((tile, player, extra) -> {
         if (tile instanceof TileEntitySecurityDesk desk) {
            desk.removeTrusted(extra);
         }
      }),
      SET_TIME((tile, player, extra) -> {
         if (tile instanceof TileEntityLaserAmplifier amplifier) {
            amplifier.setDelay(extra);
         }
      }),
      TOGGLE_CHUNKLOAD((tile, player, extra) -> {
         if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
            stabilizer.toggleChunkLoadingAt(extra / 5, extra % 5);
         }
      }),
      ENABLE_RADIUS_CHUNKLOAD((tile, player, extra) -> {
         if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
            stabilizer.adjustChunkLoadingRadius(extra, true);
         }
      }),
      DISABLE_RADIUS_CHUNKLOAD((tile, player, extra) -> {
         if (tile instanceof TileEntityDimensionalStabilizer stabilizer) {
            stabilizer.adjustChunkLoadingRadius(extra, false);
         }
      });

      private final TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile;

      private GuiInteraction(TriConsumer<TileEntityMekanism, Player, Integer> consumerForTile) {
         this.consumerForTile = consumerForTile;
      }

      public void consume(TileEntityMekanism tile, Player player, int extra) {
         this.consumerForTile.accept(tile, player, extra);
      }
   }

   public static enum GuiInteractionEntity {
      NEXT_SECURITY_MODE((entity, player, extra) -> SecurityUtils.get().incrementSecurityMode(player, entity)),
      PREVIOUS_SECURITY_MODE((entity, player, extra) -> SecurityUtils.get().decrementSecurityMode(player, entity)),
      CONTAINER_STOP_TRACKING((entity, player, extra) -> {
         if (player.f_36096_ instanceof MekanismContainer container) {
            container.stopTracking(extra);
         }
      }),
      CONTAINER_TRACK_SKIN_SELECT((entity, player, extra) -> {
         if (player.f_36096_ instanceof MainRobitContainer container) {
            container.startTrackingServer(extra, container);
         }
      });

      private final TriConsumer<Entity, Player, Integer> consumerForEntity;

      private GuiInteractionEntity(TriConsumer<Entity, Player, Integer> consumerForEntity) {
         this.consumerForEntity = consumerForEntity;
      }

      public void consume(Entity entity, Player player, int extra) {
         this.consumerForEntity.accept(entity, player, extra);
      }
   }

   public static enum GuiInteractionItem {
      DIGITAL_MINER_INVERSE_REPLACE_ITEM((tile, player, stack) -> {
         if (tile instanceof TileEntityDigitalMiner miner) {
            miner.setInverseReplaceTarget(stack.m_41720_());
         }
      }),
      QIO_REDSTONE_ADAPTER_STACK((tile, player, stack) -> {
         if (tile instanceof TileEntityQIORedstoneAdapter redstoneAdapter) {
            redstoneAdapter.handleStackChange(stack);
         }
      });

      private final TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile;

      private GuiInteractionItem(TriConsumer<TileEntityMekanism, Player, ItemStack> consumerForTile) {
         this.consumerForTile = consumerForTile;
      }

      public void consume(TileEntityMekanism tile, Player player, ItemStack stack) {
         this.consumerForTile.accept(tile, player, stack);
      }
   }

   private static enum Type {
      ENTITY,
      ITEM,
      INT;
   }
}
