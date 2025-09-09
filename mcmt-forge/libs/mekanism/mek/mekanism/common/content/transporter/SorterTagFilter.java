package mekanism.common.content.transporter;

import java.util.Objects;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class SorterTagFilter extends SorterFilter<SorterTagFilter> implements ITagFilter<SorterTagFilter> {
   private String tagName;

   public SorterTagFilter() {
   }

   public SorterTagFilter(SorterTagFilter filter) {
      super(filter);
      this.tagName = filter.tagName;
   }

   @Override
   public Finder getFinder() {
      return Finder.tag(this.tagName);
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128359_("tagName", this.tagName);
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      this.tagName = nbtTags.m_128461_("tagName");
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.m_130070_(this.tagName);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.tagName = BasePacketHandler.readString(dataStream);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.tagName);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         SorterTagFilter other = (SorterTagFilter)o;
         return this.tagName.equals(other.tagName);
      } else {
         return false;
      }
   }

   public SorterTagFilter clone() {
      return new SorterTagFilter(this);
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.SORTER_TAG_FILTER;
   }

   @Override
   public void setTagName(String name) {
      this.tagName = name;
   }

   @Override
   public String getTagName() {
      return this.tagName;
   }
}
