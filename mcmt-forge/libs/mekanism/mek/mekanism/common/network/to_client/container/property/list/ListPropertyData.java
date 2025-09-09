package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import java.util.function.Function;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf.Reader;
import org.jetbrains.annotations.NotNull;

public abstract class ListPropertyData<TYPE> extends PropertyData {
   @NotNull
   protected final List<TYPE> values;
   private final ListType listType;

   public ListPropertyData(short property, ListType listType, @NotNull List<TYPE> values) {
      super(PropertyType.LIST, property);
      this.listType = listType;
      this.values = values;
   }

   public static <TYPE> ListPropertyData<TYPE> readList(short property, FriendlyByteBuf buffer) {
      return (ListPropertyData<TYPE>)(switch ((ListType)buffer.m_130066_(ListType.class)) {
         case STRING -> StringListPropertyData.read(property, buffer::m_236845_);
         case FILTER -> FilterListPropertyData.read(property, buffer::m_236845_);
         case FREQUENCY -> FrequencyListPropertyData.read(property, buffer::m_236845_);
         case REGISTRY_ENTRY -> RegistryEntryListPropertyData.read(property, buffer);
         case RESOURCE_KEY -> ResourceKeyListPropertyData.read(property, buffer);
      });
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.values);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.m_130068_(this.listType);
      this.writeList(buffer);
   }

   protected void writeList(FriendlyByteBuf buffer) {
      buffer.m_236828_(this.values, this::writeListElement);
   }

   protected abstract void writeListElement(FriendlyByteBuf buffer, TYPE value);

   interface ListPropertyReader<TYPE> extends Function<Reader<TYPE>, List<TYPE>> {
   }
}
