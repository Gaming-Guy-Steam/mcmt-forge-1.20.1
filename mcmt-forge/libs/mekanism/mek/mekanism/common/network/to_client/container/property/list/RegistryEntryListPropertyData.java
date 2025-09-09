package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;

public class RegistryEntryListPropertyData<V> extends ListPropertyData<V> {
   private final IForgeRegistry<V> registry;

   public RegistryEntryListPropertyData(short property, IForgeRegistry<V> registry, @NotNull List<V> values) {
      super(property, ListType.REGISTRY_ENTRY, values);
      this.registry = registry;
   }

   static <V> RegistryEntryListPropertyData<V> read(short property, FriendlyByteBuf buffer) {
      IForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(buffer.m_130281_());
      return new RegistryEntryListPropertyData<>(property, registry, buffer.m_236845_(r -> r.readRegistryIdUnsafe(registry)));
   }

   @Override
   protected void writeList(FriendlyByteBuf buffer) {
      buffer.m_130085_(this.registry.getRegistryName());
      super.writeList(buffer);
   }

   @Override
   protected void writeListElement(FriendlyByteBuf buffer, V value) {
      buffer.writeRegistryIdUnsafe(this.registry, value);
   }
}
