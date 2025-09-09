package mekanism.common.lib;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import mekanism.common.Mekanism;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper.UnableToFindFieldException;

public class FieldReflectionHelper<CLASS, TYPE> {
   private final Class<CLASS> clazz;
   private final String fieldName;
   private final Supplier<TYPE> fallback;
   private Field field;

   public FieldReflectionHelper(Class<CLASS> clazz, String fieldName, Supplier<TYPE> fallback) {
      this.clazz = clazz;
      this.fieldName = fieldName;
      this.fallback = fallback;
   }

   public TYPE getValue(CLASS input) {
      if (this.field == null) {
         try {
            this.field = ObfuscationReflectionHelper.findField(this.clazz, this.fieldName);
         } catch (UnableToFindFieldException var4) {
            Mekanism.logger.error("Error getting {} {} field.", new Object[]{this.clazz.getSimpleName(), this.fieldName, var4});
            return this.fallback.get();
         }
      }

      try {
         return (TYPE)this.field.get(input);
      } catch (IllegalAccessException var3) {
         Mekanism.logger.error("Error accessing {} {} field.", new Object[]{this.clazz.getSimpleName(), this.fieldName, var3});
         return this.fallback.get();
      }
   }

   public void transformValue(CLASS input, Predicate<TYPE> shouldTransform, UnaryOperator<TYPE> valueTransformer) {
      TYPE value = this.getValue(input);
      if (this.field != null && shouldTransform.test(value)) {
         value = valueTransformer.apply(value);

         try {
            this.field.set(input, value);
         } catch (IllegalAccessException var6) {
            Mekanism.logger.error("Error accessing {} {} field.", new Object[]{this.clazz.getSimpleName(), this.fieldName, var6});
         }
      }
   }
}
