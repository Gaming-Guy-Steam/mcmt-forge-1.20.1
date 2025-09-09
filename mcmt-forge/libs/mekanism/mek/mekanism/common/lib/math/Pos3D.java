package mekanism.common.lib.math;

import mekanism.api.Coord4D;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class Pos3D extends Vec3 {
   public Pos3D() {
      this(0.0, 0.0, 0.0);
   }

   public Pos3D(Vec3 vec) {
      super(vec.f_82479_, vec.f_82480_, vec.f_82481_);
   }

   public Pos3D(Coord4D coord) {
      super(coord.getX(), coord.getY(), coord.getZ());
   }

   public Pos3D(double x, double y, double z) {
      super(x, y, z);
   }

   public Pos3D(Entity entity) {
      this(entity.m_20185_(), entity.m_20186_(), entity.m_20189_());
   }

   public static Pos3D create(BlockEntity tile) {
      return create(tile.m_58899_());
   }

   public static Pos3D create(Vec3i vec) {
      return new Pos3D(Vec3.m_82528_(vec));
   }

   public static Pos3D read(CompoundTag tag) {
      return new Pos3D(tag.m_128459_("x"), tag.m_128459_("y"), tag.m_128459_("z"));
   }

   public static Pos3D translateMatrix(double[] matrix, Pos3D translation) {
      double x = translation.f_82479_ * matrix[0] + translation.f_82480_ * matrix[1] + translation.f_82481_ * matrix[2] + matrix[3];
      double y = translation.f_82479_ * matrix[4] + translation.f_82480_ * matrix[5] + translation.f_82481_ * matrix[6] + matrix[7];
      double z = translation.f_82479_ * matrix[8] + translation.f_82480_ * matrix[9] + translation.f_82481_ * matrix[10] + matrix[11];
      return new Pos3D(x, y, z);
   }

   public static double[] getRotationMatrix(float angle, Pos3D axis) {
      return axis.getRotationMatrix(angle);
   }

   public static double anglePreNorm(Pos3D pos1, Pos3D pos2) {
      return Math.acos(pos1.m_82526_(pos2));
   }

   public static AABB getAABB(Vec3 pos1, Vec3 pos2) {
      return new AABB(pos1.f_82479_, pos1.f_82480_, pos1.f_82481_, pos2.f_82479_, pos2.f_82480_, pos2.f_82481_);
   }

   public CompoundTag write(CompoundTag nbtTags) {
      nbtTags.m_128347_("x", this.f_82479_);
      nbtTags.m_128347_("y", this.f_82480_);
      nbtTags.m_128347_("z", this.f_82481_);
      return nbtTags;
   }

   public Pos3D diff(Vec3 vec) {
      return new Pos3D(this.f_82479_ - vec.f_82479_, this.f_82480_ - vec.f_82480_, this.f_82481_ - vec.f_82481_);
   }

   public Coord4D getCoord(ResourceKey<Level> dimension) {
      return new Coord4D((int)this.f_82479_, (int)this.f_82480_, (int)this.f_82481_, dimension);
   }

   public Pos3D centre() {
      return this.translate(0.5, 0.5, 0.5);
   }

   public Pos3D translate(double x, double y, double z) {
      return new Pos3D(this.f_82479_ + x, this.f_82480_ + y, this.f_82481_ + z);
   }

   public Pos3D translate(Vec3 pos) {
      return this.translate(pos.f_82479_, pos.f_82480_, pos.f_82481_);
   }

   public Pos3D translate(Vec3... positions) {
      double x = this.f_82479_;
      double y = this.f_82480_;
      double z = this.f_82481_;

      for (Vec3 position : positions) {
         x += position.f_82479_;
         y += position.f_82480_;
         z += position.f_82481_;
      }

      return new Pos3D(x, y, z);
   }

   public Pos3D translate(Direction direction, double amount) {
      return this.translate(direction.m_122436_().m_123341_() * amount, direction.m_122436_().m_123342_() * amount, direction.m_122436_().m_123343_() * amount);
   }

   public Pos3D translateExcludingSide(Direction direction, double amount) {
      double xPos = this.f_82479_;
      double yPos = this.f_82480_;
      double zPos = this.f_82481_;
      if (direction.m_122434_() != Axis.X) {
         xPos += amount;
      }

      if (direction.m_122434_() != Axis.Y) {
         yPos += amount;
      }

      if (direction.m_122434_() != Axis.Z) {
         zPos += amount;
      }

      return new Pos3D(xPos, yPos, zPos);
   }

   public Pos3D adjustPosition(Direction direction, Entity entity) {
      if (direction.m_122434_() == Axis.X) {
         return new Pos3D(entity.m_20185_(), this.f_82480_, this.f_82481_);
      } else {
         return direction.m_122434_() == Axis.Y
            ? new Pos3D(this.f_82479_, entity.m_20186_(), this.f_82481_)
            : new Pos3D(this.f_82479_, this.f_82480_, entity.m_20189_());
      }
   }

   public double distance(Vec3 pos) {
      return Mth.m_184648_(this.f_82479_ - pos.f_82479_, this.f_82480_ - pos.f_82480_, this.f_82481_ - pos.f_82481_);
   }

   @NotNull
   public Pos3D yRot(float yaw) {
      double yawRadians = Math.toRadians(yaw);
      double xPos = this.f_82479_;
      double zPos = this.f_82481_;
      if (yaw != 0.0F) {
         double cos = Math.cos(yawRadians);
         double sin = Math.sin(yawRadians);
         xPos = this.f_82479_ * cos - this.f_82481_ * sin;
         zPos = this.f_82481_ * cos + this.f_82479_ * sin;
      }

      return new Pos3D(xPos, this.f_82480_, zPos);
   }

   @NotNull
   public Pos3D xRot(float pitch) {
      double pitchRadians = Math.toRadians(pitch);
      double yPos = this.f_82480_;
      double zPos = this.f_82481_;
      if (pitch != 0.0F) {
         double cos = Math.cos(pitchRadians);
         double sin = Math.sin(pitchRadians);
         yPos = this.f_82480_ * cos - this.f_82481_ * sin;
         zPos = this.f_82481_ * cos + this.f_82480_ * sin;
      }

      return new Pos3D(this.f_82479_, yPos, zPos);
   }

   public Pos3D rotate(float yaw, float pitch) {
      return this.rotate(yaw, pitch, 0.0F);
   }

   public Pos3D rotate(float yaw, float pitch, float roll) {
      double yawRadians = Math.toRadians(yaw);
      double pitchRadians = Math.toRadians(pitch);
      double rollRadians = Math.toRadians(roll);
      double xPos = this.f_82479_ * Math.cos(yawRadians) * Math.cos(pitchRadians)
         + this.f_82481_ * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) - Math.sin(yawRadians) * Math.cos(rollRadians))
         + this.f_82480_ * (Math.cos(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) + Math.sin(yawRadians) * Math.sin(rollRadians));
      double zPos = this.f_82479_ * Math.sin(yawRadians) * Math.cos(pitchRadians)
         + this.f_82481_ * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.sin(rollRadians) + Math.cos(yawRadians) * Math.cos(rollRadians))
         + this.f_82480_ * (Math.sin(yawRadians) * Math.sin(pitchRadians) * Math.cos(rollRadians) - Math.cos(yawRadians) * Math.sin(rollRadians));
      double yPos = -this.f_82479_ * Math.sin(pitchRadians)
         + this.f_82481_ * Math.cos(pitchRadians) * Math.sin(rollRadians)
         + this.f_82480_ * Math.cos(pitchRadians) * Math.cos(rollRadians);
      return new Pos3D(xPos, yPos, zPos);
   }

   @NotNull
   public Pos3D multiply(Vec3 pos) {
      return this.multiply(pos.f_82479_, pos.f_82480_, pos.f_82481_);
   }

   @NotNull
   public Pos3D multiply(double x, double y, double z) {
      return new Pos3D(this.f_82479_ * x, this.f_82480_ * y, this.f_82481_ * z);
   }

   @NotNull
   public Pos3D scale(double scale) {
      return this.multiply(scale, scale, scale);
   }

   public Pos3D rotate(float angle, Pos3D axis) {
      return translateMatrix(getRotationMatrix(angle, axis), this);
   }

   public Pos3D transform(Quaternion quaternion) {
      Quaternion q = quaternion.copy();
      q.multiply(new Quaternion(this.f_82479_, this.f_82480_, this.f_82481_, 0.0));
      q.multiply(quaternion.copy().conjugate());
      return new Pos3D(q.getX(), q.getY(), q.getZ());
   }

   public double[] getRotationMatrix(float angle) {
      double[] matrix = new double[16];
      Vec3 axis = this.normalize();
      double x = axis.f_82479_;
      double y = axis.f_82480_;
      double z = axis.f_82481_;
      double angleAsRadian = Math.toRadians(angle);
      float cos = (float)Math.cos(angleAsRadian);
      float ocos = 1.0F - cos;
      float sin = (float)Math.sin(angleAsRadian);
      matrix[0] = x * x * ocos + cos;
      matrix[1] = y * x * ocos + z * sin;
      matrix[2] = x * z * ocos - y * sin;
      matrix[4] = x * y * ocos - z * sin;
      matrix[5] = y * y * ocos + cos;
      matrix[6] = y * z * ocos + x * sin;
      matrix[8] = x * z * ocos + y * sin;
      matrix[9] = y * z * ocos - x * sin;
      matrix[10] = z * z * ocos + cos;
      matrix[15] = 1.0;
      return matrix;
   }

   public double anglePreNorm(Pos3D pos2) {
      return Math.acos(this.m_82526_(pos2));
   }

   @NotNull
   public Pos3D normalize() {
      return new Pos3D(super.m_82541_());
   }

   public Pos3D xCrossProduct() {
      return new Pos3D(0.0, this.f_82481_, -this.f_82480_);
   }

   public Pos3D zCrossProduct() {
      return new Pos3D(-this.f_82480_, this.f_82479_, 0.0);
   }

   public Pos3D getPerpendicular() {
      return this.f_82481_ == 0.0 ? this.zCrossProduct() : this.xCrossProduct();
   }

   public Pos3D floor() {
      return new Pos3D(Math.floor(this.f_82479_), Math.floor(this.f_82480_), Math.floor(this.f_82481_));
   }

   public Pos3D clone() {
      return new Pos3D(this.f_82479_, this.f_82480_, this.f_82481_);
   }

   @NotNull
   public String toString() {
      return "[Pos3D: " + this.f_82479_ + ", " + this.f_82480_ + ", " + this.f_82481_ + "]";
   }

   public boolean equals(Object obj) {
      return obj instanceof Vec3 other && other.f_82479_ == this.f_82479_ && other.f_82480_ == this.f_82480_ && other.f_82481_ == this.f_82481_;
   }

   public int hashCode() {
      int code = 1;
      code = 31 * code + Double.hashCode(this.f_82479_);
      code = 31 * code + Double.hashCode(this.f_82480_);
      return 31 * code + Double.hashCode(this.f_82481_);
   }
}
