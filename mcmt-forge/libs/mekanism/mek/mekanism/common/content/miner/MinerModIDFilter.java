package mekanism.common.content.miner;

import java.util.Objects;
import mekanism.common.base.TagCache;
import mekanism.common.content.filter.FilterType;
import mekanism.common.content.filter.IModIDFilter;
import mekanism.common.lib.WildcardMatcher;
import mekanism.common.network.BasePacketHandler;
import mekanism.common.util.RegistryUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

public class MinerModIDFilter extends MinerFilter<MinerModIDFilter> implements IModIDFilter<MinerModIDFilter> {
   private String modID;

   public MinerModIDFilter(String modID) {
      this.modID = modID;
   }

   public MinerModIDFilter() {
   }

   public MinerModIDFilter(MinerModIDFilter filter) {
      super(filter);
      this.modID = filter.modID;
   }

   @Override
   public boolean canFilter(BlockState state) {
      return WildcardMatcher.matches(this.modID, RegistryUtils.getNamespace(state.m_60734_()));
   }

   @Override
   public boolean hasBlacklistedElement() {
      return TagCache.modIDHasMinerBlacklisted(this.modID);
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
         MinerModIDFilter other = (MinerModIDFilter)o;
         return this.modID.equals(other.modID);
      } else {
         return false;
      }
   }

   public MinerModIDFilter clone() {
      return new MinerModIDFilter(this);
   }

   @Override
   public FilterType getFilterType() {
      return FilterType.MINER_MODID_FILTER;
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
