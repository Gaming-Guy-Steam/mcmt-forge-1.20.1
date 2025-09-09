package mekanism.api.chemical.attribute;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import org.jetbrains.annotations.Nullable;

public interface IChemicalAttributeContainer<SELF extends IChemicalAttributeContainer<SELF>> {
   boolean has(Class<? extends ChemicalAttribute> var1);

   @Nullable
   <ATTRIBUTE extends ChemicalAttribute> ATTRIBUTE get(Class<ATTRIBUTE> var1);

   Collection<ChemicalAttribute> getAttributes();

   Collection<Class<? extends ChemicalAttribute>> getAttributeTypes();

   default <ATTRIBUTE extends ChemicalAttribute> void ifAttributePresent(Class<ATTRIBUTE> type, Consumer<? super ATTRIBUTE> action) {
      ATTRIBUTE attribute = this.get(type);
      if (attribute != null) {
         action.accept(attribute);
      }
   }

   default <ATTRIBUTE extends ChemicalAttribute> int mapAttributeToInt(Class<ATTRIBUTE> type, ToIntFunction<? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsInt(attribute) : 0;
   }

   default <ATTRIBUTE extends ChemicalAttribute> int mapAttributeToInt(Class<ATTRIBUTE> type, ToIntBiFunction<SELF, ? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsInt((SELF)this, attribute) : 0;
   }

   default <ATTRIBUTE extends ChemicalAttribute> long mapAttributeToLong(Class<ATTRIBUTE> type, ToLongFunction<? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsLong(attribute) : 0L;
   }

   default <ATTRIBUTE extends ChemicalAttribute> long mapAttributeToLong(Class<ATTRIBUTE> type, ToLongBiFunction<SELF, ? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsLong((SELF)this, attribute) : 0L;
   }

   default <ATTRIBUTE extends ChemicalAttribute> double mapAttributeToDouble(Class<ATTRIBUTE> type, ToDoubleFunction<? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsDouble(attribute) : 0.0;
   }

   default <ATTRIBUTE extends ChemicalAttribute> double mapAttributeToDouble(Class<ATTRIBUTE> type, ToDoubleBiFunction<SELF, ? super ATTRIBUTE> mapper) {
      ATTRIBUTE attribute = this.get(type);
      return attribute != null ? mapper.applyAsDouble((SELF)this, attribute) : 0.0;
   }

   default <ATTRIBUTE extends ChemicalAttribute, V> V mapAttribute(Class<ATTRIBUTE> type, Function<? super ATTRIBUTE, ? extends V> mapper, V fallback) {
      ATTRIBUTE attribute = this.get(type);
      return (V)(attribute != null ? mapper.apply(attribute) : fallback);
   }

   default <ATTRIBUTE extends ChemicalAttribute, V> V mapAttribute(Class<ATTRIBUTE> type, BiFunction<SELF, ? super ATTRIBUTE, ? extends V> mapper, V fallback) {
      ATTRIBUTE attribute = this.get(type);
      return (V)(attribute != null ? mapper.apply((SELF)this, attribute) : fallback);
   }
}
