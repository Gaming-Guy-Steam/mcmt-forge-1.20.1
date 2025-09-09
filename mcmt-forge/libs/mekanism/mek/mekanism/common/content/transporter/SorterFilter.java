package mekanism.common.content.transporter;

import java.util.Objects;
import mekanism.api.text.EnumColor;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.lib.inventory.Finder;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.TransporterUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SorterFilter<FILTER extends SorterFilter<FILTER>> extends BaseFilter<FILTER> {
   public static final int MAX_LENGTH = 48;
   @SyntheticComputerMethod(
      getter = "getColor",
      setter = "setColor",
      threadSafeGetter = true,
      threadSafeSetter = true
   )
   public EnumColor color;
   @SyntheticComputerMethod(
      getter = "getAllowDefault",
      setter = "setAllowDefault",
      threadSafeGetter = true,
      threadSafeSetter = true
   )
   public boolean allowDefault;
   @SyntheticComputerMethod(
      getter = "getSizeMode",
      setter = "setSizeMode",
      threadSafeSetter = true,
      threadSafeGetter = true
   )
   public boolean sizeMode;
   @SyntheticComputerMethod(
      getter = "getMin",
      threadSafeGetter = true
   )
   public int min;
   @SyntheticComputerMethod(
      getter = "getMax",
      threadSafeGetter = true
   )
   public int max;

   protected SorterFilter() {
   }

   protected SorterFilter(FILTER filter) {
      this.allowDefault = filter.allowDefault;
      this.color = filter.color;
      this.sizeMode = filter.sizeMode;
      this.min = filter.min;
      this.max = filter.max;
   }

   public abstract Finder getFinder();

   public TransitRequest mapInventory(BlockEntity tile, Direction side, boolean singleItem) {
      return this.sizeMode && !singleItem
         ? TransitRequest.definedItem(tile, side, this.min, this.max, this.getFinder())
         : TransitRequest.definedItem(tile, side, singleItem ? 1 : 64, this.getFinder());
   }

   @Override
   public CompoundTag write(CompoundTag nbtTags) {
      super.write(nbtTags);
      nbtTags.m_128379_("allowDefault", this.allowDefault);
      nbtTags.m_128405_("color", TransporterUtils.getColorIndex(this.color));
      nbtTags.m_128379_("sizeMode", this.sizeMode);
      nbtTags.m_128405_("min", this.min);
      nbtTags.m_128405_("max", this.max);
      return nbtTags;
   }

   @Override
   public void read(CompoundTag nbtTags) {
      super.read(nbtTags);
      NBTUtils.setBooleanIfPresent(nbtTags, "allowDefault", value -> this.allowDefault = value);
      NBTUtils.setEnumIfPresent(nbtTags, "color", TransporterUtils::readColor, color -> this.color = color);
      NBTUtils.setBooleanIfPresent(nbtTags, "sizeMode", value -> this.sizeMode = value);
      NBTUtils.setIntIfPresent(nbtTags, "min", value -> this.min = value);
      NBTUtils.setIntIfPresent(nbtTags, "max", value -> this.max = value);
   }

   @Override
   public void write(FriendlyByteBuf buffer) {
      super.write(buffer);
      buffer.writeBoolean(this.allowDefault);
      buffer.m_130130_(TransporterUtils.getColorIndex(this.color));
      buffer.writeBoolean(this.sizeMode);
      buffer.m_130130_(this.min);
      buffer.m_130130_(this.max);
   }

   @Override
   public void read(FriendlyByteBuf dataStream) {
      super.read(dataStream);
      this.allowDefault = dataStream.readBoolean();
      this.color = TransporterUtils.readColor(dataStream.m_130242_());
      this.sizeMode = dataStream.readBoolean();
      this.min = dataStream.m_130242_();
      this.max = dataStream.m_130242_();
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), this.color, this.allowDefault, this.sizeMode, this.min, this.max);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass() && super.equals(o)) {
         SorterFilter<?> other = (SorterFilter<?>)o;
         return this.allowDefault == other.allowDefault
            && this.sizeMode == other.sizeMode
            && this.min == other.min
            && this.max == other.max
            && this.color == other.color;
      } else {
         return false;
      }
   }

   @ComputerMethod(
      threadSafe = true
   )
   void setMinMax(int min, int max) throws ComputerException {
      if (min >= 0 && max >= 0 && min <= max && max <= 64) {
         this.min = min;
         this.max = max;
      } else {
         throw new ComputerException("Invalid or min/max: 0 <= min <= max <= 64");
      }
   }

   @ComputerMethod(
      threadSafe = true
   )
   public abstract FILTER clone();
}
