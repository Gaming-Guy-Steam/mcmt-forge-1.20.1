package mekanism.common.registration;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

public class WrappedDatapackDeferredRegister<T> extends WrappedDeferredRegister<Codec<? extends T>> {
   protected final ResourceKey<Registry<T>> datapackRegistryName;
   private final String modid;

   protected WrappedDatapackDeferredRegister(
      String modid, ResourceKey<? extends Registry<Codec<? extends T>>> serializerRegistryName, ResourceKey<Registry<T>> datapackRegistryName
   ) {
      super(modid, serializerRegistryName);
      this.modid = modid;
      this.datapackRegistryName = datapackRegistryName;
   }

   public Codec<T> createAndRegisterDatapack(IEventBus bus, Function<? super T, Codec<? extends T>> baseCodec) {
      return this.createAndRegisterDatapack(bus, baseCodec, null);
   }

   public Codec<T> createAndRegisterDatapack(IEventBus bus, Function<? super T, Codec<? extends T>> baseCodec, @Nullable Codec<T> networkCodec) {
      Supplier<IForgeRegistry<Codec<? extends T>>> serializerRegistry = this.createAndRegister(bus, builder -> builder.disableSaving().disableSync());
      Codec<T> directCodec = ExtraCodecs.m_184415_(() -> serializerRegistry.get().getCodec()).dispatch(baseCodec, Function.identity());
      bus.addListener(event -> event.dataPackRegistry(this.datapackRegistryName, directCodec, networkCodec));
      return directCodec;
   }

   public ResourceKey<T> dataKey(String name) {
      return ResourceKey.m_135785_(this.datapackRegistryName, new ResourceLocation(this.modid, name));
   }
}
