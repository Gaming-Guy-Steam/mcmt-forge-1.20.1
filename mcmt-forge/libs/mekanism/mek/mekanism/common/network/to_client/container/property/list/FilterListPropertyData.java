package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import mekanism.common.content.filter.BaseFilter;
import mekanism.common.content.filter.IFilter;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class FilterListPropertyData<FILTER extends IFilter<?>> extends ListPropertyData<FILTER> {
   public FilterListPropertyData(short property, @NotNull List<FILTER> values) {
      super(property, ListType.FILTER, values);
   }

   static <FILTER extends IFilter<?>> FilterListPropertyData<FILTER> read(short property, ListPropertyData.ListPropertyReader<FILTER> reader) {
      return new FilterListPropertyData<>(property, reader.apply(buf -> BaseFilter.readFromPacket(buf)));
   }

   protected void writeListElement(FriendlyByteBuf buffer, FILTER value) {
      value.write(buffer);
   }
}
