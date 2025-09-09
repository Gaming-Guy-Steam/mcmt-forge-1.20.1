package mekanism.common.util;

import java.util.List;
import java.util.Optional;
import mekanism.api.RelativeSide;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporter;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public final class TransporterUtils {
   public static final List<EnumColor> colors = List.of(
      EnumColor.DARK_BLUE,
      EnumColor.DARK_GREEN,
      EnumColor.DARK_AQUA,
      EnumColor.DARK_RED,
      EnumColor.PURPLE,
      EnumColor.INDIGO,
      EnumColor.BRIGHT_GREEN,
      EnumColor.AQUA,
      EnumColor.RED,
      EnumColor.PINK,
      EnumColor.YELLOW,
      EnumColor.BLACK
   );

   private TransporterUtils() {
   }

   @Nullable
   public static EnumColor readColor(int inputColor) {
      return inputColor == -1 ? null : colors.get(inputColor);
   }

   public static int getColorIndex(@Nullable EnumColor color) {
      return color == null ? -1 : colors.indexOf(color);
   }

   public static boolean isValidAcceptorOnSide(BlockEntity tile, Direction side) {
      return tile instanceof TileEntityTransmitter transmitter && TransmissionType.ITEM.checkTransmissionType(transmitter)
         ? false
         : InventoryUtils.isItemHandler(tile, side.m_122424_());
   }

   public static EnumColor increment(EnumColor color) {
      if (color == null) {
         return colors.get(0);
      } else {
         int index = colors.indexOf(color);
         return index == colors.size() - 1 ? null : colors.get(index + 1);
      }
   }

   public static EnumColor decrement(EnumColor color) {
      if (color == null) {
         return colors.get(colors.size() - 1);
      } else {
         int index = colors.indexOf(color);
         return index == 0 ? null : colors.get(index - 1);
      }
   }

   public static void drop(LogisticalTransporterBase transporter, TransporterStack stack) {
      BlockPos blockPos = transporter.getTilePos();
      if (stack.hasPath()) {
         float[] pos = getStackPosition(transporter, stack, 0.0F);
         blockPos = blockPos.m_7918_(Mth.m_14143_(pos[0]), Mth.m_14143_(pos[1]), Mth.m_14143_(pos[2]));
      }

      TransporterManager.remove(transporter.getTileWorld(), stack);
      Block.m_49840_(transporter.getTileWorld(), blockPos, stack.itemStack);
   }

   public static float[] getStackPosition(LogisticalTransporterBase transporter, TransporterStack stack, float partial) {
      Direction side = stack.getSide(transporter);
      float progress = (stack.progress + partial) / 100.0F - 0.5F;
      return new float[]{0.5F + side.m_122429_() * progress, 0.25F + side.m_122430_() * progress, 0.5F + side.m_122431_() * progress};
   }

   public static void incrementColor(LogisticalTransporter tile) {
      EnumColor color = tile.getColor();
      if (color == null) {
         tile.setColor(colors.get(0));
      } else {
         int index = colors.indexOf(color);
         if (index == colors.size() - 1) {
            tile.setColor(null);
         } else {
            tile.setColor(colors.get(index + 1));
         }
      }
   }

   public static boolean canInsert(BlockEntity tile, EnumColor color, ItemStack itemStack, Direction side, boolean force) {
      if (force && tile instanceof TileEntityLogisticalSorter sorter) {
         return sorter.canSendHome(itemStack);
      } else {
         if (!force && tile instanceof ISideConfiguration config && config.getEjector().hasStrictInput()) {
            Direction tileSide = config.getDirection();
            EnumColor configColor = config.getEjector().getInputColor(RelativeSide.fromDirections(tileSide, side.m_122424_()));
            if (configColor != null && configColor != color) {
               return false;
            }
         }

         Optional<IItemHandler> capability = CapabilityUtils.getCapability(tile, ForgeCapabilities.ITEM_HANDLER, side.m_122424_()).resolve();
         if (capability.isPresent()) {
            IItemHandler inventory = capability.get();
            int i = 0;

            for (int slots = inventory.getSlots(); i < slots; i++) {
               ItemStack rejects = inventory.insertItem(i, itemStack, true);
               if (TransporterManager.didEmit(itemStack, rejects)) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}
