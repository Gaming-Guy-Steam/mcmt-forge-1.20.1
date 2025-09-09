package mekanism.common.registration.impl;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.FriendlyByteBuf.Reader;
import net.minecraft.network.FriendlyByteBuf.Writer;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.jetbrains.annotations.NotNull;

public class DataSerializerDeferredRegister extends WrappedDeferredRegister<EntityDataSerializer<?>> {
   public DataSerializerDeferredRegister(String modid) {
      super(modid, Keys.ENTITY_DATA_SERIALIZERS);
   }

   public <T extends Enum<T>> DataSerializerRegistryObject<T> registerEnum(String name, Class<T> enumClass) {
      return this.register(name, () -> EntityDataSerializer.m_238090_(enumClass));
   }

   public <T> DataSerializerRegistryObject<T> registerSimple(String name, Writer<T> writer, Reader<T> reader) {
      return this.register(name, () -> EntityDataSerializer.m_238095_(writer, reader));
   }

   public <T> DataSerializerRegistryObject<T> register(String name, Writer<T> writer, Reader<T> reader, UnaryOperator<T> copier) {
      return this.register(name, () -> new EntityDataSerializer<T>() {
         public void m_6856_(@NotNull FriendlyByteBuf buffer, @NotNull T value) {
            writer.accept(buffer, value);
         }

         @NotNull
         public T m_6709_(@NotNull FriendlyByteBuf buffer) {
            return (T)reader.apply(buffer);
         }

         @NotNull
         public T m_7020_(@NotNull T value) {
            return (T)copier.apply(value);
         }
      });
   }

   public <T> DataSerializerRegistryObject<T> register(String name, Supplier<EntityDataSerializer<T>> sup) {
      return this.register(name, sup, DataSerializerRegistryObject::new);
   }
}
