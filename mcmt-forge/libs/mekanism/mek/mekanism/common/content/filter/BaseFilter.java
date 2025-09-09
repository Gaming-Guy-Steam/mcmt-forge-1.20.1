package mekanism.common.content.filter;

import java.util.Objects;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFilter<FILTER extends BaseFilter<FILTER>> implements IFilter<FILTER> {
   private boolean enabled = true;

   public abstract FILTER clone();

   @Override
   public int hashCode() {
      return Objects.hash(this.getFilterType(), this.enabled);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BaseFilter<?> other = (BaseFilter<?>)o;
         return this.enabled == other.enabled;
      } else {
         return false;
      }
   }

   @Override
   public final boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public final void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      NBTUtils.writeEnum(nbtTags, "type", this.getFilterType());
      nbtTags.m_128379_("enabled", this.isEnabled());
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      NBTUtils.setBooleanIfPresentElse(nbtTags, "enabled", true, this::setEnabled);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      buffer.m_130068_(this.getFilterType());
      buffer.writeBoolean(this.isEnabled());
   }

   @Override
   public void read(FriendlyByteBuf buffer) {
      this.setEnabled(buffer.readBoolean());
   }

   @Nullable
   public static IFilter<?> readFromNBT(CompoundTag nbt) {
      if (nbt.m_128425_("type", 3)) {
         IFilter<?> filter = fromType(FilterType.byIndexStatic(nbt.m_128451_("type")));
         filter.read(nbt);
         return filter;
      } else {
         return null;
      }
   }

   public static IFilter<?> readFromPacket(FriendlyByteBuf dataStream) {
      IFilter<?> filter = fromType((FilterType)dataStream.m_130066_(FilterType.class));
      filter.read(dataStream);
      return filter;
   }

   public static IFilter<?> fromType(FilterType filterType) {
      return (IFilter<?>)(switch (filterType) {
         case MINER_ITEMSTACK_FILTER -> new MinerItemStackFilter();
         case MINER_MODID_FILTER -> new MinerModIDFilter();
         case MINER_TAG_FILTER -> new MinerTagFilter();
         case SORTER_ITEMSTACK_FILTER -> new SorterItemStackFilter();
         case SORTER_MODID_FILTER -> new SorterModIDFilter();
         case SORTER_TAG_FILTER -> new SorterTagFilter();
         case OREDICTIONIFICATOR_ITEM_FILTER -> new OredictionificatorItemFilter();
         case QIO_ITEMSTACK_FILTER -> new QIOItemStackFilter();
         case QIO_MODID_FILTER -> new QIOModIDFilter();
         case QIO_TAG_FILTER -> new QIOTagFilter();
      });
   }
}
