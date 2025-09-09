package mekanism.common.network.to_server;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOCraftingTransferHelper;
import mekanism.common.content.qio.QIOServerCraftingTransferHandler;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketQIOFillCraftingWindow implements IMekanismPacket {
   private final Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources;
   private final ResourceLocation recipeID;
   private final boolean maxTransfer;

   public PacketQIOFillCraftingWindow(
      ResourceLocation recipeID, boolean maxTransfer, Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources
   ) {
      this.recipeID = recipeID;
      this.sources = sources;
      this.maxTransfer = maxTransfer;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && player.f_36096_ instanceof QIOItemViewerContainer container) {
         byte selectedCraftingGrid = container.getSelectedCraftingGrid(player.m_20148_());
         if (selectedCraftingGrid == -1) {
            Mekanism.logger.warn("Received transfer request from: {}, but they do not currently have a crafting window open.", player);
         } else {
            Optional<? extends Recipe<?>> optionalRecipe = MekanismRecipeType.byKey(player.m_9236_(), this.recipeID);
            if (optionalRecipe.isPresent()) {
               Recipe<?> recipe = (Recipe<?>)optionalRecipe.get();
               if (recipe instanceof CraftingRecipe craftingRecipe) {
                  QIOServerCraftingTransferHandler.tryTransfer(container, selectedCraftingGrid, player, this.recipeID, craftingRecipe, this.sources);
               } else {
                  Mekanism.logger
                     .warn(
                        "Received transfer request from: {}, but the type ({}) of the specified recipe was not a crafting recipe.", player, recipe.getClass()
                     );
               }
            } else {
               Mekanism.logger.warn("Received transfer request from: {}, but could not find specified recipe.", player);
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130085_(this.recipeID);
      buffer.writeBoolean(this.maxTransfer);
      buffer.writeByte((byte)this.sources.size());
      ObjectIterator var2 = this.sources.byte2ObjectEntrySet().iterator();

      while (var2.hasNext()) {
         Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>> entry = (Entry<List<QIOCraftingTransferHelper.SingularHashedItemSource>>)var2.next();
         buffer.writeByte(entry.getByteKey());
         List<QIOCraftingTransferHelper.SingularHashedItemSource> slotSources = (List<QIOCraftingTransferHelper.SingularHashedItemSource>)entry.getValue();
         if (this.maxTransfer) {
            buffer.m_130130_(slotSources.size());
         }

         for (QIOCraftingTransferHelper.SingularHashedItemSource source : slotSources) {
            byte sourceSlot = source.getSlot();
            buffer.writeByte(sourceSlot);
            if (this.maxTransfer) {
               buffer.m_130130_(source.getUsed());
            }

            if (sourceSlot == -1) {
               UUID qioSource = source.getQioSource();
               if (qioSource == null) {
                  throw new IllegalStateException("Invalid QIO crafting window transfer source.");
               }

               buffer.m_130077_(qioSource);
            }
         }
      }
   }

   public static PacketQIOFillCraftingWindow decode(FriendlyByteBuf buffer) {
      ResourceLocation recipeID = buffer.m_130281_();
      boolean maxTransfer = buffer.readBoolean();
      byte slotCount = buffer.readByte();
      Byte2ObjectMap<List<QIOCraftingTransferHelper.SingularHashedItemSource>> sources = new Byte2ObjectArrayMap(slotCount);

      for (byte slot = 0; slot < slotCount; slot++) {
         byte targetSlot = buffer.readByte();
         int subSourceCount = maxTransfer ? buffer.m_130242_() : 1;
         List<QIOCraftingTransferHelper.SingularHashedItemSource> slotSources = new ArrayList<>(subSourceCount);
         sources.put(targetSlot, slotSources);

         for (int i = 0; i < subSourceCount; i++) {
            byte sourceSlot = buffer.readByte();
            int count = maxTransfer ? buffer.m_130242_() : 1;
            if (sourceSlot == -1) {
               slotSources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(buffer.m_130259_(), count));
            } else {
               slotSources.add(new QIOCraftingTransferHelper.SingularHashedItemSource(sourceSlot, count));
            }
         }
      }

      return new PacketQIOFillCraftingWindow(recipeID, maxTransfer, sources);
   }
}
