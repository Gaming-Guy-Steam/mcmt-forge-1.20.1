package mekanism.common.network.to_client.container.property.list;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class ResourceKeyListPropertyData<V> extends ListPropertyData<ResourceKey<V>> {
   private final ResourceKey<? extends Registry<V>> registry;

   public ResourceKeyListPropertyData(short property, ResourceKey<? extends Registry<V>> registry, @NotNull List<ResourceKey<V>> values) {
      super(property, ListType.RESOURCE_KEY, values);
      this.registry = registry;
   }

   static <V> ResourceKeyListPropertyData<V> read(short property, FriendlyByteBuf buffer) {
      ResourceKey<? extends Registry<V>> registry = ResourceKey.m_135788_(buffer.m_130281_());
      return new ResourceKeyListPropertyData<>(property, registry, buffer.m_236845_(r -> r.m_236801_(registry)));
   }

   @Override
   protected void writeList(FriendlyByteBuf buffer) {
      buffer.m_130085_(this.registry.m_135782_());
      super.writeList(buffer);
   }

   protected void writeListElement(FriendlyByteBuf buffer, ResourceKey<V> value) {
      buffer.m_236858_(value);
   }
}
