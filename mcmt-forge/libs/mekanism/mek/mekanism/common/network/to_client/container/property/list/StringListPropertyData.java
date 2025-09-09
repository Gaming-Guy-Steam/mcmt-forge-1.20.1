package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import mekanism.common.network.BasePacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public class StringListPropertyData extends ListPropertyData<String> {
   public StringListPropertyData(short property, @NotNull List<String> values) {
      super(property, ListType.STRING, values);
   }

   static StringListPropertyData read(short property, ListPropertyData.ListPropertyReader<String> reader) {
      return new StringListPropertyData(property, reader.apply(BasePacketHandler::readString));
   }

   protected void writeListElement(FriendlyByteBuf buffer, String value) {
      buffer.m_130070_(value);
   }
}
