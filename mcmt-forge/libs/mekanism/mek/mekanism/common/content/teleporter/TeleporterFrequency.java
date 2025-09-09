package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.frequency.IColorableFrequency;
import mekanism.common.tile.interfaces.ITileWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class TeleporterFrequency extends Frequency implements IColorableFrequency {
   private final Set<Coord4D> activeCoords = new ObjectOpenHashSet();
   private EnumColor color = EnumColor.PURPLE;

   public TeleporterFrequency(String n, @Nullable UUID uuid) {
      super(FrequencyType.TELEPORTER, n, uuid);
   }

   public TeleporterFrequency() {
      super(FrequencyType.TELEPORTER);
   }

   public Set<Coord4D> getActiveCoords() {
      return this.activeCoords;
   }

   @Override
   public int getSyncHash() {
      int code = super.getSyncHash();
      return 31 * code + this.color.ordinal();
   }

   @Override
   public EnumColor getColor() {
      return this.color;
   }

   @Override
   public void setColor(EnumColor color) {
      if (this.color != color) {
         this.color = color;
         this.dirty = true;
      }
   }

   @Override
   public boolean update(BlockEntity tile) {
      boolean changedData = super.update(tile);
      this.activeCoords.add(this.getCoord(tile));
      return changedData;
   }

   @Override
   public boolean onDeactivate(BlockEntity tile) {
      boolean changedData = super.onDeactivate(tile);
      this.activeCoords.remove(this.getCoord(tile));
      return changedData;
   }

   private Coord4D getCoord(BlockEntity tile) {
      return tile instanceof ITileWrapper tileWrapper ? tileWrapper.getTileCoord() : new Coord4D(tile);
   }

   public Coord4D getClosestCoords(Coord4D coord) {
      Coord4D closest = null;

      for (Coord4D iterCoord : this.activeCoords) {
         if (!iterCoord.equals(coord)) {
            if (closest == null) {
               closest = iterCoord;
            } else if (coord.dimension != closest.dimension && coord.dimension == iterCoord.dimension) {
               closest = iterCoord;
            } else if ((coord.dimension != closest.dimension || coord.dimension == iterCoord.dimension)
               && coord.distanceTo(closest) > coord.distanceTo(iterCoord)) {
               closest = iterCoord;
            }
         }
      }

      return closest;
   }

   @Override
   protected void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setEnumIfPresent(nbtTags, "color", EnumColor::byIndexStatic, color -> this.color = color);
   }

   @Override
   protected void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.color = (EnumColor)dataStream.m_130066_(EnumColor.class);
   }

   @Override
   public void write(CompoundTag nbtTags) {
      super.write(nbtTags);
      NBTUtils.writeEnum(nbtTags, "color", this.color);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.m_130068_(this.color);
   }
}
