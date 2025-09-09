package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransporterStack {
   public ItemStack itemStack = ItemStack.f_41583_;
   public int progress;
   public EnumColor color = null;
   public boolean initiatedPath = false;
   public Direction idleDir = null;
   public BlockPos originalLocation;
   public BlockPos homeLocation;
   private BlockPos clientNext;
   private BlockPos clientPrev;
   private TransporterStack.Path pathType;
   private List<BlockPos> pathToTarget = new ArrayList<>();

   public static TransporterStack readFromNBT(CompoundTag nbtTags) {
      TransporterStack stack = new TransporterStack();
      stack.read(nbtTags);
      return stack;
   }

   public static TransporterStack readFromUpdate(CompoundTag nbtTags) {
      TransporterStack stack = new TransporterStack();
      stack.readFromUpdateTag(nbtTags);
      return stack;
   }

   public static TransporterStack readFromPacket(FriendlyByteBuf dataStream) {
      TransporterStack stack = new TransporterStack();
      stack.read(dataStream);
      if (stack.progress == 0) {
         stack.progress = 5;
      }

      return stack;
   }

   public void write(LogisticalTransporterBase transporter, FriendlyByteBuf buf) {
      buf.m_130130_(TransporterUtils.getColorIndex(this.color));
      buf.m_130130_(this.progress);
      buf.m_130064_(this.originalLocation);
      buf.m_130068_(this.pathType);
      if (this.pathToTarget.indexOf(transporter.getTilePos()) > 0) {
         buf.writeBoolean(true);
         buf.m_130064_(this.getNext(transporter));
      } else {
         buf.writeBoolean(false);
      }

      buf.m_130064_(this.getPrev(transporter));
      buf.m_130055_(this.itemStack);
   }

   public void read(FriendlyByteBuf dataStream) {
      this.color = TransporterUtils.readColor(dataStream.m_130242_());
      this.progress = dataStream.m_130242_();
      this.originalLocation = dataStream.m_130135_();
      this.pathType = (TransporterStack.Path)dataStream.m_130066_(TransporterStack.Path.class);
      if (dataStream.readBoolean()) {
         this.clientNext = dataStream.m_130135_();
      }

      this.clientPrev = dataStream.m_130135_();
      this.itemStack = dataStream.m_130267_();
   }

   public void writeToUpdateTag(LogisticalTransporterBase transporter, CompoundTag updateTag) {
      updateTag.m_128405_("color", TransporterUtils.getColorIndex(this.color));
      updateTag.m_128405_("progress", this.progress);
      updateTag.m_128365_("originalLocation", NbtUtils.m_129224_(this.originalLocation));
      NBTUtils.writeEnum(updateTag, "pathType", this.pathType);
      if (this.pathToTarget.indexOf(transporter.getTilePos()) > 0) {
         updateTag.m_128365_("clientNext", NbtUtils.m_129224_(this.getNext(transporter)));
      }

      updateTag.m_128365_("clientPrevious", NbtUtils.m_129224_(this.getPrev(transporter)));
      this.itemStack.m_41739_(updateTag);
   }

   public void readFromUpdateTag(CompoundTag updateTag) {
      NBTUtils.setEnumIfPresent(updateTag, "color", TransporterUtils::readColor, color -> this.color = color);
      this.progress = updateTag.m_128451_("progress");
      NBTUtils.setBlockPosIfPresent(updateTag, "originalLocation", coord -> this.originalLocation = coord);
      NBTUtils.setEnumIfPresent(updateTag, "pathType", TransporterStack.Path::byIndexStatic, type -> this.pathType = type);
      NBTUtils.setBlockPosIfPresent(updateTag, "clientNext", coord -> this.clientNext = coord);
      NBTUtils.setBlockPosIfPresent(updateTag, "clientPrevious", coord -> this.clientPrev = coord);
      this.itemStack = ItemStack.m_41712_(updateTag);
   }

   public void write(CompoundTag nbtTags) {
      nbtTags.m_128405_("color", TransporterUtils.getColorIndex(this.color));
      nbtTags.m_128405_("progress", this.progress);
      nbtTags.m_128365_("originalLocation", NbtUtils.m_129224_(this.originalLocation));
      if (this.idleDir != null) {
         NBTUtils.writeEnum(nbtTags, "idleDir", this.idleDir);
      }

      if (this.homeLocation != null) {
         nbtTags.m_128365_("homeLocation", NbtUtils.m_129224_(this.homeLocation));
      }

      NBTUtils.writeEnum(nbtTags, "pathType", this.pathType);
      this.itemStack.m_41739_(nbtTags);
   }

   public void read(CompoundTag nbtTags) {
      NBTUtils.setEnumIfPresent(nbtTags, "color", TransporterUtils::readColor, color -> this.color = color);
      this.progress = nbtTags.m_128451_("progress");
      NBTUtils.setBlockPosIfPresent(nbtTags, "originalLocation", coord -> this.originalLocation = coord);
      NBTUtils.setEnumIfPresent(nbtTags, "idleDir", Direction::m_122376_, dir -> this.idleDir = dir);
      NBTUtils.setBlockPosIfPresent(nbtTags, "homeLocation", coord -> this.homeLocation = coord);
      NBTUtils.setEnumIfPresent(nbtTags, "pathType", TransporterStack.Path::byIndexStatic, type -> this.pathType = type);
      this.itemStack = ItemStack.m_41712_(nbtTags);
   }

   private void setPath(Level world, List<BlockPos> path, TransporterStack.Path type, boolean updateFlowing) {
      if (updateFlowing && this.pathType != TransporterStack.Path.NONE) {
         TransporterManager.remove(world, this);
      }

      this.pathToTarget = path;
      this.pathType = type;
      if (updateFlowing && this.pathType != TransporterStack.Path.NONE) {
         TransporterManager.add(world, this);
      }
   }

   public boolean hasPath() {
      return this.pathToTarget != null && this.pathToTarget.size() >= 2;
   }

   public List<BlockPos> getPath() {
      return this.pathToTarget;
   }

   public TransporterStack.Path getPathType() {
      return this.pathType;
   }

   public TransitRequest.TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min) {
      return this.recalculatePath(request, transporter, min, true);
   }

   public TransitRequest.TransitResponse recalculatePath(TransitRequest request, LogisticalTransporterBase transporter, int min, boolean updateFlowing) {
      return this.recalculatePath(request, transporter, min, updateFlowing, Collections.emptyMap());
   }

   public TransitRequest.TransitResponse recalculatePath(
      TransitRequest request, LogisticalTransporterBase transporter, int min, Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      return this.recalculatePath(request, transporter, min, false, additionalFlowingStacks);
   }

   private TransitRequest.TransitResponse recalculatePath(
      TransitRequest request,
      LogisticalTransporterBase transporter,
      int min,
      boolean updateFlowing,
      Map<Coord4D, Set<TransporterStack>> additionalFlowingStacks
   ) {
      TransporterPathfinder.Destination newPath = TransporterPathfinder.getNewBasePath(transporter, this, request, min, additionalFlowingStacks);
      if (newPath == null) {
         return request.getEmptyResponse();
      } else {
         this.idleDir = null;
         this.setPath(transporter.getTileWorld(), newPath.getPath(), TransporterStack.Path.DEST, updateFlowing);
         this.initiatedPath = true;
         return newPath.getResponse();
      }
   }

   public TransitRequest.TransitResponse recalculateRRPath(
      TransitRequest request, TileEntityLogisticalSorter outputter, LogisticalTransporterBase transporter, int min
   ) {
      return this.recalculateRRPath(request, outputter, transporter, min, true);
   }

   public TransitRequest.TransitResponse recalculateRRPath(
      TransitRequest request, TileEntityLogisticalSorter outputter, LogisticalTransporterBase transporter, int min, boolean updateFlowing
   ) {
      TransporterPathfinder.Destination newPath = TransporterPathfinder.getNewRRPath(transporter, this, request, outputter, min);
      if (newPath == null) {
         return request.getEmptyResponse();
      } else {
         this.idleDir = null;
         this.setPath(transporter.getTileWorld(), newPath.getPath(), TransporterStack.Path.DEST, updateFlowing);
         this.initiatedPath = true;
         return newPath.getResponse();
      }
   }

   public boolean calculateIdle(LogisticalTransporterBase transporter) {
      TransporterPathfinder.IdlePathData newPath = TransporterPathfinder.getIdlePath(transporter, this);
      if (newPath == null) {
         return false;
      } else {
         if (newPath.type() == TransporterStack.Path.HOME) {
            this.idleDir = null;
         }

         this.setPath(transporter.getTileWorld(), newPath.path(), newPath.type(), true);
         this.originalLocation = transporter.getTilePos();
         this.initiatedPath = true;
         return true;
      }
   }

   public boolean isFinal(LogisticalTransporterBase transporter) {
      return this.pathToTarget.indexOf(transporter.getTilePos()) == (this.pathType == TransporterStack.Path.NONE ? 0 : 1);
   }

   public BlockPos getNext(LogisticalTransporterBase transporter) {
      if (!transporter.isRemote()) {
         int index = this.pathToTarget.indexOf(transporter.getTilePos()) - 1;
         return index < 0 ? null : this.pathToTarget.get(index);
      } else {
         return this.clientNext;
      }
   }

   public BlockPos getPrev(LogisticalTransporterBase transporter) {
      if (!transporter.isRemote()) {
         int index = this.pathToTarget.indexOf(transporter.getTilePos()) + 1;
         return index < this.pathToTarget.size() ? this.pathToTarget.get(index) : this.originalLocation;
      } else {
         return this.clientPrev;
      }
   }

   public Direction getSide(LogisticalTransporterBase transporter) {
      Direction side = null;
      if (this.progress < 50) {
         BlockPos prev = this.getPrev(transporter);
         if (prev != null) {
            side = WorldUtils.sideDifference(transporter.getTilePos(), prev);
         }
      } else {
         BlockPos next = this.getNext(transporter);
         if (next != null) {
            side = WorldUtils.sideDifference(next, transporter.getTilePos());
         }
      }

      return side == null ? Direction.DOWN : side;
   }

   @Contract("null, _, _ -> false")
   public boolean canInsertToTransporter(@Nullable LogisticalTransporterBase transmitter, Direction from, @Nullable LogisticalTransporterBase transporterFrom) {
      return transmitter != null && this.canInsertToTransporterNN(transmitter, from, transporterFrom);
   }

   public boolean canInsertToTransporterNN(@NotNull LogisticalTransporterBase transporter, Direction from, @Nullable BlockEntity tileFrom) {
      EnumColor color = transporter.getColor();
      return (color == null || color == this.color) && transporter.canConnectMutual(from.m_122424_(), tileFrom);
   }

   public boolean canInsertToTransporterNN(@NotNull LogisticalTransporterBase transporter, Direction from, @Nullable LogisticalTransporterBase transporterFrom) {
      EnumColor color = transporter.getColor();
      return (color == null || color == this.color) && transporter.canConnectMutual(from.m_122424_(), transporterFrom);
   }

   public BlockPos getDest() {
      return this.pathToTarget.get(0);
   }

   @Nullable
   public Direction getSideOfDest() {
      if (this.hasPath()) {
         BlockPos lastTransporter = this.pathToTarget.get(1);
         return WorldUtils.sideDifference(lastTransporter, this.getDest());
      } else {
         return null;
      }
   }

   public static enum Path {
      DEST,
      HOME,
      NONE;

      private static final TransporterStack.Path[] PATHS = values();

      public static TransporterStack.Path byIndexStatic(int index) {
         return MathUtils.getByIndexMod(PATHS, index);
      }
   }
}
