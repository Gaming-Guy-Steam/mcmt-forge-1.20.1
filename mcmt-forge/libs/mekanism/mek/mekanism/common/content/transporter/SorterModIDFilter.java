package mekanism.common.content.transporter;

import java.util.Objects;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SorterModIDFilter extends SorterFilter<SorterModIDFilter> implements IModIDFilter<SorterModIDFilter> {
   private String modID;

   public SorterModIDFilter() {
   }

   public SorterModIDFilter(SorterModIDFilter filter) {
      super(filter);
      this.modID = filter.modID;
   }

   @Override
   public Finder getFinder() {
      return Finder.modID(this.modID);
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128359_("modID", this.modID);
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      this.modID = nbtTags.m_128461_("modID");
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.m_130070_(this.modID);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.modID = BasePacketHandler.readString(dataStream);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.modID);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         SorterModIDFilter other = (SorterModIDFilter)o;
         return this.modID.equals(other.modID);
      } else {
         return false;
      }
   }

   public SorterModIDFilter clone() {
      return new SorterModIDFilter(this);
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.SORTER_MODID_FILTER;
   }

   @Override
   public void setModID(String id) {
      this.modID = id;
   }

   @Override
   public String getModID() {
      return this.modID;
   }
}
