package mekanism.common.lib;

import net.minecraftforge.fml.ModContainer;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class Version {
   public int major;
   public int minor;
   public int build;

   public Version(int majorNum, int minorNum, int buildNum) {
      this.major = majorNum;
      this.minor = minorNum;
      this.build = buildNum;
   }

   public Version(ArtifactVersion artifactVersion) {
      this(artifactVersion.getMajorVersion(), artifactVersion.getMinorVersion(), artifactVersion.getIncrementalVersion());
   }

   public Version(ModContainer container) {
      this(container.getModInfo().getVersion());
   }

   public static Version get(String s) {
      String[] split = s.replace('.', ':').split(":");
      if (split.length != 3) {
         return null;
      } else {
         int[] digits = new int[3];

         for (int i = 0; i < digits.length; i++) {
            try {
               digits[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException var5) {
               return null;
            }
         }

         return new Version(digits[0], digits[1], digits[2]);
      }
   }

   public void reset() {
      this.major = 0;
      this.minor = 0;
      this.build = 0;
   }

   public byte comparedState(Version version) {
      if (version.major > this.major) {
         return -1;
      } else if (version.major == this.major) {
         if (version.minor > this.minor) {
            return -1;
         } else {
            return version.minor == this.minor ? (byte)Integer.compare(this.build, version.build) : 1;
         }
      } else {
         return 1;
      }
   }

   @Override
   public String toString() {
      return this.major == 0 && this.minor == 0 && this.build == 0 ? "" : this.major + "." + this.minor + "." + this.build;
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + this.build;
      result = 31 * result + this.major;
      return 31 * result + this.minor;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         Version other = (Version)obj;
         return this.build == other.build && this.major == other.major && this.minor == other.minor;
      } else {
         return false;
      }
   }
}
