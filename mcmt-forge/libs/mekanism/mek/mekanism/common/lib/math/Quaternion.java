package mekanism.common.lib.math;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Quaternion {
   public static final Quaternion ONE = new Quaternion(0.0, 0.0, 0.0, 1.0);
   private double x;
   private double y;
   private double z;
   private double w;

   public Quaternion(double x, double y, double z, double w) {
      this.set(x, y, z, w);
   }

   public Quaternion(Vec3 axis, double angle, boolean degrees) {
      if (degrees) {
         angle *= Math.PI / 180.0;
      }

      double sin = Math.sin(angle / 2.0);
      this.set(axis.m_7096_() * sin, axis.m_7098_() * sin, axis.m_7094_() * sin, Math.cos(angle / 2.0));
   }

   public Quaternion(double xAngle, double yAngle, double zAngle, boolean degrees) {
      if (degrees) {
         xAngle *= Math.PI / 180.0;
         yAngle *= Math.PI / 180.0;
         zAngle *= Math.PI / 180.0;
      }

      double sinX = Math.sin(0.5 * xAngle);
      double cosX = Math.cos(0.5 * xAngle);
      double sinY = Math.sin(0.5 * yAngle);
      double cosY = Math.cos(0.5 * yAngle);
      double sinZ = Math.sin(0.5 * zAngle);
      double cosZ = Math.cos(0.5 * zAngle);
      this.x = sinX * cosY * cosZ + cosX * sinY * sinZ;
      this.y = cosX * sinY * cosZ - sinX * cosY * sinZ;
      this.z = sinX * sinY * cosZ + cosX * cosY * sinZ;
      this.w = cosX * cosY * cosZ - sinX * sinY * sinZ;
   }

   @Override
   public boolean equals(Object obj) {
      return this == obj ? true : obj instanceof Quaternion other && this.x == other.x && this.y == other.y && this.z == other.z && this.w == other.w;
   }

   @Override
   public int hashCode() {
      int i = Double.hashCode(this.x);
      i = 31 * i + Double.hashCode(this.y);
      i = 31 * i + Double.hashCode(this.z);
      return 31 * i + Double.hashCode(this.w);
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public double getW() {
      return this.w;
   }

   public Quaternion multiply(Quaternion other) {
      double prevX = this.getX();
      double prevY = this.getY();
      double prevZ = this.getZ();
      double prevW = this.getW();
      double otherX = other.getX();
      double otherY = other.getY();
      double otherZ = other.getZ();
      double otherW = other.getW();
      this.x = prevW * otherX + prevX * otherW + prevY * otherZ - prevZ * otherY;
      this.y = prevW * otherY - prevX * otherZ + prevY * otherW + prevZ * otherX;
      this.z = prevW * otherZ + prevX * otherY - prevY * otherX + prevZ * otherW;
      this.w = prevW * otherW - prevX * otherX - prevY * otherY - prevZ * otherZ;
      return this;
   }

   public Quaternion multiply(double val) {
      return this.set(this.x * val, this.y * val, this.z * val, this.w * val);
   }

   public Quaternion conjugate() {
      return this.set(-this.x, -this.y, -this.z, this.w);
   }

   public Quaternion set(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
      return this;
   }

   public double magnitude() {
      return this.getX() * this.getX() + this.getY() * this.getY() + this.getZ() * this.getZ() + this.getW() * this.getW();
   }

   public Quaternion normalize() {
      double mag = this.magnitude();
      if (mag > 1.0E-6F) {
         this.multiply(Mth.m_264555_(mag));
      } else {
         this.multiply(0.0);
      }

      return this;
   }

   public Quaternion copy() {
      return new Quaternion(this.x, this.y, this.z, this.w);
   }

   public Pos3D rotate(Vec3 vec) {
      return new Pos3D(vec).transform(this);
   }

   public static Pos3D rotate(Vec3 vec, Vec3 axis, double angle) {
      return new Quaternion(axis, angle, true).rotate(vec);
   }
}
