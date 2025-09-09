package mekanism.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class Chunk3D extends ChunkPos {
   public final ResourceKey<Level> dimension;

   public Chunk3D(ResourceKey<Level> dimension, int x, int z) {
      super(x, z);
      this.dimension = dimension;
   }

   public Chunk3D(ResourceKey<Level> dimension, long chunkPos) {
      this(dimension, ChunkPos.m_45592_(chunkPos), ChunkPos.m_45602_(chunkPos));
   }

   public Chunk3D(ResourceKey<Level> dimension, ChunkPos chunkPos) {
      this(dimension, chunkPos.f_45578_, chunkPos.f_45579_);
   }

   public Chunk3D(Coord4D coord) {
      this(coord.dimension, SectionPos.m_123171_(coord.getX()), SectionPos.m_123171_(coord.getZ()));
   }

   public Set<Chunk3D> expand(int chunkRadius) {
      if (chunkRadius < 0) {
         throw new IllegalArgumentException("Chunk radius cannot be negative.");
      } else if (chunkRadius == 1) {
         return Collections.singleton(this);
      } else {
         Set<Chunk3D> ret = new HashSet<>();

         for (int i = this.f_45578_ - chunkRadius; i <= this.f_45578_ + chunkRadius; i++) {
            for (int j = this.f_45579_ - chunkRadius; j <= this.f_45579_ + chunkRadius; j++) {
               ret.add(new Chunk3D(this.dimension, i, j));
            }
         }

         return ret;
      }
   }

   @NotNull
   public String toString() {
      return "[Chunk3D: " + this.f_45578_ + ", " + this.f_45579_ + ", dim=" + this.dimension.m_135782_() + "]";
   }

   public boolean equals(Object obj) {
      return obj instanceof Chunk3D other && other.f_45578_ == this.f_45578_ && other.f_45579_ == this.f_45579_ && other.dimension == this.dimension;
   }

   public int hashCode() {
      int code = 1;
      code = 31 * code + this.f_45578_;
      code = 31 * code + this.f_45579_;
      return 31 * code + this.dimension.hashCode();
   }
}
