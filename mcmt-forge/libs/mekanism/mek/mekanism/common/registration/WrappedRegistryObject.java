package mekanism.common.registration;

import java.util.Objects;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryObject;

@NothingNullByDefault
public class WrappedRegistryObject<T> implements Supplier<T>, INamedEntry {
   protected RegistryObject<T> registryObject;

   protected WrappedRegistryObject(RegistryObject<T> registryObject) {
      this.registryObject = registryObject;
   }

   @Override
   public T get() {
      return (T)this.registryObject.get();
   }

   @Override
   public String getInternalRegistryName() {
      return this.registryObject.getId().m_135815_();
   }

   public ResourceKey<T> key() {
      return Objects.requireNonNull(this.registryObject.getKey(), "Resource key should not be null");
   }
}
