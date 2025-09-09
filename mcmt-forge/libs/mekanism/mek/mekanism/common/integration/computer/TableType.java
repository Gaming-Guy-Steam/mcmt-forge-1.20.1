package mekanism.common.integration.computer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import mekanism.common.util.MekCodecs;

public record TableType(String description, String humanName, Map<String, TableType.FieldType> fields, Class<?> extendedFrom) {
   public static Codec<TableType> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("description").forGetter(TableType::description),
            Codec.STRING.fieldOf("humanName").forGetter(TableType::humanName),
            Codec.unboundedMap(Codec.STRING, TableType.FieldType.CODEC).optionalFieldOf("fields", Collections.emptyMap()).forGetter(TableType::fields),
            MekCodecs.CLASS_TO_STRING_CODEC.optionalFieldOf("extends", null).forGetter(TableType::extendedFrom)
         )
         .apply(instance, TableType::new)
   );
   public static Codec<Map<Class<?>, TableType>> TABLE_MAP_CODEC = Codec.unboundedMap(MekCodecs.CLASS_TO_STRING_CODEC, CODEC);

   public static TableType.Builder builder(Class<?> clazz, String description) {
      return new TableType.Builder(clazz, description);
   }

   public static class Builder {
      private final Class<?> clazz;
      private final String description;
      private final String humanName;
      private final Map<String, TableType.FieldType> fields = new LinkedHashMap<>();
      private Class<?> extendedFrom = null;

      private Builder(Class<?> clazz, String description) {
         this.clazz = clazz;
         this.description = description;
         this.humanName = MethodHelpData.getHumanType(clazz, ComputerMethodFactory.NO_CLASSES);
      }

      public TableType.Builder extendedFrom(Class<?> c) {
         this.extendedFrom = c;
         return this;
      }

      public TableType.Builder addField(String name, Class<?> javaType, String description, Class<?>... javaExtra) {
         if (javaExtra == null) {
            javaExtra = ComputerMethodFactory.NO_CLASSES;
         }

         this.fields.put(name, new TableType.FieldType(description, javaType, MethodHelpData.getHumanType(javaType, javaExtra), javaExtra));
         return this;
      }

      public TableType build(Map<Class<?>, TableType> destination) {
         TableType tableType = new TableType(this.description, this.humanName, new LinkedHashMap<>(this.fields), this.extendedFrom);
         destination.put(this.clazz, tableType);
         return tableType;
      }
   }

   public record FieldType(String description, Class<?> javaType, String type, Class<?>[] javaExtra) {
      public static final Codec<TableType.FieldType> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
               Codec.STRING.fieldOf("description").forGetter(TableType.FieldType::description),
               MekCodecs.CLASS_TO_STRING_CODEC.fieldOf("javaType").forGetter(TableType.FieldType::javaType),
               Codec.STRING.fieldOf("type").forGetter(TableType.FieldType::type),
               MekCodecs.optionalClassArrayCodec("javaExtra").forGetter(TableType.FieldType::javaExtra)
            )
            .apply(instance, TableType.FieldType::new)
      );
   }
}
