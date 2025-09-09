package mekanism.common.content.network.transmitter;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.transporter.TransporterManager;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.lib.transmitter.acceptor.AcceptorCache;
import mekanism.common.network.to_client.PacketTransporterUpdate;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LogisticalTransporterBase extends Transmitter<IItemHandler, InventoryNetwork, LogisticalTransporterBase> {
   protected final Int2ObjectMap<TransporterStack> transit = new Int2ObjectOpenHashMap();
   protected final Int2ObjectMap<TransporterStack> needsSync = new Int2ObjectOpenHashMap();
   public final TransporterTier tier;
   protected int nextId = 0;
   protected int delay = 0;
   protected int delayCount = 0;

   protected LogisticalTransporterBase(TileEntityTransmitter tile, TransporterTier tier) {
      super(tile, TransmissionType.ITEM);
      this.tier = tier;
   }

   public AcceptorCache<IItemHandler> getAcceptorCache() {
      return (AcceptorCache<IItemHandler>)super.getAcceptorCache();
   }

   @Override
   public boolean handlesRedstone() {
      return false;
   }

   public boolean exposesInsertCap(@NotNull Direction side) {
      ConnectionType connectionType = this.getConnectionTypeRaw(side);
      return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL;
   }

   public EnumColor getColor() {
      return null;
   }

   public boolean canEmitTo(Direction side) {
      if (!this.canConnect(side)) {
         return false;
      } else {
         ConnectionType connectionType = this.getConnectionType(side);
         return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PUSH;
      }
   }

   public boolean canReceiveFrom(Direction side) {
      if (!this.canConnect(side)) {
         return false;
      } else {
         ConnectionType connectionType = this.getConnectionType(side);
         return connectionType == ConnectionType.NORMAL || connectionType == ConnectionType.PULL;
      }
   }

   @Override
   public boolean isValidTransmitterBasic(TileEntityTransmitter transmitter, Direction side) {
      return !(
            transmitter.getTransmitter() instanceof LogisticalTransporterBase transporter
               && (this.getColor() == null || transporter.getColor() == null || this.getColor() == transporter.getColor())
         )
         ? false
         : super.isValidTransmitterBasic(transmitter, side);
   }

   @Override
   public boolean isValidAcceptor(BlockEntity tile, Direction side) {
      return super.isValidAcceptor(tile, side) && this.getAcceptorCache().isAcceptorAndListen(tile, side, ForgeCapabilities.ITEM_HANDLER);
   }

   public void onUpdateClient() {
      ObjectIterator var1 = this.transit.values().iterator();

      while (var1.hasNext()) {
         TransporterStack stack = (TransporterStack)var1.next();
         stack.progress = Math.min(100, stack.progress + this.tier.getSpeed());
      }
   }

   public void onUpdateServer() {
      if (this.getTransmitterNetwork() != null) {
         if (this.delay > 0) {
            this.delay--;
         } else {
            this.delay = 3;

            for (Direction side : this.getConnections(ConnectionType.PULL)) {
               BlockEntity tile = WorldUtils.getTileEntity(this.getTileWorld(), this.getTilePos().m_121945_(side));
               if (tile != null) {
                  TransitRequest request = TransitRequest.anyItem(tile, side.m_122424_(), this.tier.getPullAmount());
                  if (!request.isEmpty()) {
                     TransitRequest.TransitResponse response = this.insert(tile, request, this.getColor(), true, 0);
                     if (response.isEmpty()) {
                        this.delayCount++;
                        this.delay = Math.min(40, (int)Math.exp(this.delayCount));
                     } else {
                        response.useAll();
                        this.delay = 10;
                     }
                  }
               }
            }
         }

         if (!this.transit.isEmpty()) {
            InventoryNetwork network = this.getTransmitterNetwork();
            IntSet deletes = new IntOpenHashSet();
            ObjectIterator var16 = this.transit.int2ObjectEntrySet().iterator();

            while (var16.hasNext()) {
               Entry<TransporterStack> entry = (Entry<TransporterStack>)var16.next();
               int stackId = entry.getIntKey();
               TransporterStack stack = (TransporterStack)entry.getValue();
               if (stack.initiatedPath || !stack.itemStack.m_41619_() && this.recalculate(stackId, stack, null)) {
                  int prevProgress = stack.progress;
                  stack.progress = stack.progress + this.tier.getSpeed();
                  if (stack.progress >= 100) {
                     BlockPos prevSet = null;
                     if (stack.hasPath()) {
                        int currentIndex = stack.getPath().indexOf(this.getTilePos());
                        if (currentIndex == 0) {
                           deletes.add(stackId);
                           continue;
                        }

                        BlockPos next = stack.getPath().get(currentIndex - 1);
                        if (next != null) {
                           if (!stack.isFinal(this)) {
                              LogisticalTransporterBase transmitter = network.getTransmitter(next);
                              if (stack.canInsertToTransporter(transmitter, stack.getSide(this), this)) {
                                 transmitter.entityEntering(stack, stack.progress % 100);
                                 deletes.add(stackId);
                                 continue;
                              }

                              prevSet = next;
                           } else if (stack.getPathType() != TransporterStack.Path.NONE) {
                              BlockEntity tile = WorldUtils.getTileEntity(this.getTileWorld(), next);
                              if (tile != null) {
                                 TransitRequest.TransitResponse response = TransitRequest.simple(stack.itemStack)
                                    .addToInventory(tile, stack.getSide(this), 0, stack.getPathType() == TransporterStack.Path.HOME);
                                 if (!response.isEmpty()) {
                                    ItemStack rejected = response.getRejected();
                                    if (rejected.m_41619_()) {
                                       TransporterManager.remove(this.getTileWorld(), stack);
                                       deletes.add(stackId);
                                       continue;
                                    }

                                    stack.itemStack = rejected;
                                 }

                                 prevSet = next;
                              }
                           }
                        }
                     }

                     if (!this.recalculate(stackId, stack, prevSet)) {
                        deletes.add(stackId);
                     } else if (prevSet == null) {
                        stack.progress = 50;
                     } else {
                        stack.progress = 0;
                     }
                  } else if (prevProgress < 50 && stack.progress >= 50) {
                     boolean tryRecalculate;
                     if (stack.isFinal(this)) {
                        TransporterStack.Path pathType = stack.getPathType();
                        if (pathType != TransporterStack.Path.DEST && pathType != TransporterStack.Path.HOME) {
                           tryRecalculate = pathType == TransporterStack.Path.NONE;
                        } else {
                           Direction sidex = stack.getSide(this);
                           ConnectionType connectionType = this.getConnectionType(sidex);
                           tryRecalculate = connectionType != ConnectionType.NORMAL && connectionType != ConnectionType.PUSH
                              || !TransporterUtils.canInsert(
                                 WorldUtils.getTileEntity(this.getTileWorld(), stack.getDest()),
                                 stack.color,
                                 stack.itemStack,
                                 sidex,
                                 pathType == TransporterStack.Path.HOME
                              );
                        }
                     } else {
                        LogisticalTransporterBase nextTransmitter = network.getTransmitter(stack.getNext(this));
                        if (nextTransmitter == null && stack.getPathType() == TransporterStack.Path.NONE && stack.getPath().size() == 2) {
                           ConnectionType connectionType = this.getConnectionType(stack.getSide(this));
                           tryRecalculate = connectionType != ConnectionType.NORMAL && connectionType != ConnectionType.PUSH;
                        } else {
                           tryRecalculate = !stack.canInsertToTransporter(nextTransmitter, stack.getSide(this), this);
                        }
                     }

                     if (tryRecalculate && !this.recalculate(stackId, stack, null)) {
                        deletes.add(stackId);
                     }
                  }
               } else {
                  deletes.add(stackId);
               }
            }

            if (!deletes.isEmpty() || !this.needsSync.isEmpty()) {
               Mekanism.packetHandler().sendToAllTracking(new PacketTransporterUpdate(this, this.needsSync, deletes), this.getTransmitterTile());
               deletes.forEach(this::deleteStack);
               this.needsSync.clear();
               this.getTransmitterTile().markForSave();
            }
         }
      }
   }

   @Override
   public void remove() {
      super.remove();
      if (!this.isRemote()) {
         for (TransporterStack stack : this.getTransit()) {
            TransporterManager.remove(this.getTileWorld(), stack);
         }
      }
   }

   public InventoryNetwork createEmptyNetworkWithID(UUID networkID) {
      return new InventoryNetwork(networkID);
   }

   public InventoryNetwork createNetworkByMerging(Collection<InventoryNetwork> networks) {
      return new InventoryNetwork(networks);
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag(CompoundTag updateTag) {
      updateTag = super.getReducedUpdateTag(updateTag);
      ListTag stacks = new ListTag();
      ObjectIterator var3 = this.transit.int2ObjectEntrySet().iterator();

      while (var3.hasNext()) {
         Entry<TransporterStack> entry = (Entry<TransporterStack>)var3.next();
         CompoundTag tagCompound = new CompoundTag();
         tagCompound.m_128405_("index", entry.getIntKey());
         ((TransporterStack)entry.getValue()).writeToUpdateTag(this, tagCompound);
         stacks.add(tagCompound);
      }

      if (!stacks.isEmpty()) {
         updateTag.m_128365_("Items", stacks);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      this.transit.clear();
      if (tag.m_128425_("Items", 9)) {
         ListTag tagList = tag.m_128437_("Items", 10);

         for (int i = 0; i < tagList.size(); i++) {
            CompoundTag compound = tagList.m_128728_(i);
            TransporterStack stack = TransporterStack.readFromUpdate(compound);
            this.addStack(compound.m_128451_("index"), stack);
         }
      }
   }

   @Override
   public void read(@NotNull CompoundTag nbtTags) {
      super.read(nbtTags);
      this.readFromNBT(nbtTags);
   }

   protected void readFromNBT(CompoundTag nbtTags) {
      if (nbtTags.m_128425_("Items", 9)) {
         ListTag tagList = nbtTags.m_128437_("Items", 10);

         for (int i = 0; i < tagList.size(); i++) {
            this.addStack(this.nextId++, TransporterStack.readFromNBT(tagList.m_128728_(i)));
         }
      }
   }

   @NotNull
   @Override
   public CompoundTag write(@NotNull CompoundTag nbtTags) {
      super.write(nbtTags);
      this.writeToNBT(nbtTags);
      return nbtTags;
   }

   public void writeToNBT(CompoundTag nbtTags) {
      Collection<TransporterStack> transit = this.getTransit();
      if (!transit.isEmpty()) {
         ListTag stacks = new ListTag();

         for (TransporterStack stack : transit) {
            CompoundTag tagCompound = new CompoundTag();
            stack.write(tagCompound);
            stacks.add(tagCompound);
         }

         nbtTags.m_128365_("Items", stacks);
      }
   }

   @Override
   public void takeShare() {
   }

   public double getCost() {
      return (double)TransporterTier.ULTIMATE.getSpeed() / this.tier.getSpeed();
   }

   public Collection<TransporterStack> getTransit() {
      return Collections.unmodifiableCollection(this.transit.values());
   }

   public void deleteStack(int id) {
      this.transit.remove(id);
   }

   public void addStack(int id, TransporterStack s) {
      this.transit.put(id, s);
   }

   private boolean recalculate(int stackId, TransporterStack stack, BlockPos from) {
      boolean noPath = stack.getPathType() == TransporterStack.Path.NONE || stack.recalculatePath(TransitRequest.simple(stack.itemStack), this, 0).isEmpty();
      if (noPath && !stack.calculateIdle(this)) {
         TransporterUtils.drop(this, stack);
         return false;
      } else {
         this.needsSync.put(stackId, stack);
         if (from != null) {
            stack.originalLocation = from;
         }

         return true;
      }
   }

   public TransitRequest.TransitResponse insert(BlockEntity outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
      return this.insert(outputter, request, color, doEmit, stack -> stack.recalculatePath(request, this, min, doEmit));
   }

   public TransitRequest.TransitResponse insertRR(
      TileEntityLogisticalSorter outputter, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min
   ) {
      return this.insert(outputter, request, color, doEmit, stack -> stack.recalculateRRPath(request, outputter, this, min, doEmit));
   }

   private TransitRequest.TransitResponse insert(
      BlockEntity outputter,
      TransitRequest request,
      @Nullable EnumColor color,
      boolean doEmit,
      Function<TransporterStack, TransitRequest.TransitResponse> pathCalculator
   ) {
      BlockPos outputterPos = outputter.m_58899_();
      Direction from = WorldUtils.sideDifference(this.getTilePos(), outputterPos);
      if (from != null && this.canReceiveFrom(from.m_122424_())) {
         TransporterStack stack = this.createInsertStack(outputterPos, color);
         if (stack.canInsertToTransporterNN(this, from, outputter)) {
            return this.updateTransit(doEmit, stack, pathCalculator.apply(stack));
         }
      }

      return request.getEmptyResponse();
   }

   public TransitRequest.TransitResponse insertUnchecked(BlockPos outputterPos, TransitRequest request, @Nullable EnumColor color, boolean doEmit, int min) {
      return this.insertUnchecked(outputterPos, color, doEmit, stack -> stack.recalculatePath(request, this, min, doEmit));
   }

   private TransitRequest.TransitResponse insertUnchecked(
      BlockPos outputterPos, @Nullable EnumColor color, boolean doEmit, Function<TransporterStack, TransitRequest.TransitResponse> pathCalculator
   ) {
      TransporterStack stack = this.createInsertStack(outputterPos, color);
      return this.updateTransit(doEmit, stack, pathCalculator.apply(stack));
   }

   public TransporterStack createInsertStack(BlockPos outputterCoord, @Nullable EnumColor color) {
      TransporterStack stack = new TransporterStack();
      stack.originalLocation = outputterCoord;
      stack.homeLocation = outputterCoord;
      stack.color = color;
      return stack;
   }

   @NotNull
   private TransitRequest.TransitResponse updateTransit(boolean doEmit, TransporterStack stack, TransitRequest.TransitResponse response) {
      if (!response.isEmpty()) {
         stack.itemStack = response.getStack();
         if (doEmit) {
            int stackId = this.nextId++;
            this.addStack(stackId, stack);
            Mekanism.packetHandler().sendToAllTracking(new PacketTransporterUpdate(this, stackId, stack), this.getTransmitterTile());
            this.getTransmitterTile().markForSave();
         }
      }

      return response;
   }

   private void entityEntering(TransporterStack stack, int progress) {
      int stackId = this.nextId++;
      stack.progress = progress;
      this.addStack(stackId, stack);
      this.needsSync.put(stackId, stack);
   }
}
