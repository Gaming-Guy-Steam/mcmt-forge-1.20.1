package mekanism.client.render.lib;

import java.util.Arrays;

public record QuickHash(Object... objs) {
   @Override
   public int hashCode() {
      return Arrays.hashCode(this.objs);
   }

   @Override
   public boolean equals(Object obj) {
      return obj == this || obj instanceof QuickHash other && Arrays.deepEquals(this.objs, other.objs);
   }
}
