package mekanism.api;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Coord4D {
   private final int x;
   private final int y;
   private final int z;
   public final ResourceKey<Level> dimension;
   private final int hashCode;

   public Coord4D(Entity entity) {
      this(entity.m_20183_(), entity.m_9236_());
   }

   public Coord4D(double x, double y, double z, ResourceKey<Level> dimension) {
      this.x = Mth.m_14107_(x);
      this.y = Mth.m_14107_(y);
      this.z = Mth.m_14107_(z);
      this.dimension = dimension;
      this.hashCode = this.initHashCode();
   }

   public Coord4D(Vec3i pos, Level world) {
      this(pos, world.m_46472_());
   }

   public Coord4D(Vec3i pos, ResourceKey<Level> dimension) {
      this(pos.m_123341_(), pos.m_123342_(), pos.m_123343_(), dimension);
   }

   public Coord4D(BlockEntity tile) {
      this(tile.m_58899_(), Objects.requireNonNull(tile.m_58904_(), "Block entity has no level."));
   }

   public static Coord4D read(CompoundTag tag) {
      return new Coord4D(
         tag.m_128451_("x"),
         tag.m_128451_("y"),
         tag.m_128451_("z"),
         ResourceKey.m_135785_(Registries.f_256858_, new ResourceLocation(tag.m_128461_("dimension")))
      );
   }

   public static Coord4D read(FriendlyByteBuf dataStream) {
      return new Coord4D(dataStream.m_130135_(), ResourceKey.m_135785_(Registries.f_256858_, dataStream.m_130281_()));
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getZ() {
      return this.z;
   }

   public BlockPos getPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128405_("x", this.x);
      nbtTags.m_128405_("y", this.y);
      nbtTags.m_128405_("z", this.z);
      nbtTags.m_128359_("dimension", this.dimension.m_135782_().toString());
      return nbtTags;
   }

   public void write(FriendlyByteBuf dataStream) {
      dataStream.m_130064_(this.getPos());
      dataStream.m_130085_(this.dimension.m_135782_());
   }

   public Coord4D translate(int x, int y, int z) {
      return new Coord4D(this.x + x, this.y + y, this.z + z, this.dimension);
   }

   public Coord4D offset(Direction side) {
      return this.offset(side, 1);
   }

   public Coord4D offset(Direction side, int amount) {
      return side != null && amount != 0
         ? new Coord4D(this.x + side.m_122429_() * amount, this.y + side.m_122430_() * amount, this.z + side.m_122431_() * amount, this.dimension)
         : this;
   }

   public double distanceTo(Coord4D obj) {
      return Math.sqrt(this.distanceToSquared(obj));
   }

   public double distanceToSquared(Coord4D obj) {
      int subX = this.x - obj.x;
      int subY = this.y - obj.y;
      int subZ = this.z - obj.z;
      return subX * subX + subY * subY + subZ * subZ;
   }

   @Override
   public String toString() {
      return "[Coord4D: " + this.x + ", " + this.y + ", " + this.z + ", dim=" + this.dimension.m_135782_() + "]";
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof Coord4D other && other.x == this.x && other.y == this.y && other.z == this.z && other.dimension == this.dimension;
   }

   @Override
   public int hashCode() {
      return this.hashCode;
   }

   private int initHashCode() {
      int code = 1;
      code = 31 * code + this.x;
      code = 31 * code + this.y;
      code = 31 * code + this.z;
      return 31 * code + this.dimension.hashCode();
   }
}
