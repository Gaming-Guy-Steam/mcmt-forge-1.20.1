package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.ITagFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;

public class MinerTagFilter extends MinerFilter<MinerTagFilter> implements ITagFilter<MinerTagFilter> {
   private String tagName;

   public MinerTagFilter(String tagName) {
      this.tagName = tagName;
   }

   public MinerTagFilter() {
   }

   public MinerTagFilter(MinerTagFilter filter) {
      super(filter);
      this.tagName = filter.tagName;
   }

   @Override
   public boolean canFilter(BlockState state) {
      return state.m_204343_().anyMatch(tag -> WildcardMatcher.matches(this.tagName, (TagKey<?>)tag));
   }

   @Override
   public boolean hasBlacklistedElement() {
      return TagCache.tagHasMinerBlacklisted(this.tagName);
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
         MinerTagFilter other = (MinerTagFilter)o;
         return this.tagName.equals(other.tagName);
      } else {
         return false;
      }
   }

   public MinerTagFilter clone() {
      return new MinerTagFilter(this);
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.MINER_TAG_FILTER;
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
