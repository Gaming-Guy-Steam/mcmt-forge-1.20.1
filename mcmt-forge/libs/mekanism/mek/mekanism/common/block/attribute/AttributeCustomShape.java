package mekanism.common.block.attribute;

import java.util.Arrays;
import net.minecraft.world.phys.shapes.VoxelShape;

public record AttributeCustomShape(VoxelShape[] bounds) implements Attribute {
   @Override
   public boolean equals(Object o) {
      return this == o ? true : o instanceof AttributeCustomShape other && Arrays.equals((Object[])this.bounds, (Object[])other.bounds);
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.bounds);
   }
}
