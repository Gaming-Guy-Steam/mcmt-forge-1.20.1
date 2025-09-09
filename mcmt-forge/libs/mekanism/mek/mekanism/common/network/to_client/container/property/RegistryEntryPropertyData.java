package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

public class RegistryEntryPropertyData<V> extends PropertyData {
   private final IForgeRegistry<V> registry;
   private final V value;

   public RegistryEntryPropertyData(short property, IForgeRegistry<V> registry, V value) {
      super(PropertyType.REGISTRY_ENTRY, property);
      this.registry = registry;
      this.value = value;
   }

   public static <V> RegistryEntryPropertyData<V> readRegistryEntry(short property, FriendlyByteBuf buffer) {
      IForgeRegistry<V> registry = RegistryManager.ACTIVE.getRegistry(buffer.m_130281_());
      return new RegistryEntryPropertyData<>(property, registry, (V)buffer.readRegistryIdUnsafe(registry));
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.value);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.writeRegistryId(this.registry, this.value);
   }
}
