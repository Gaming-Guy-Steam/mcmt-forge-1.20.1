package mekanism.api.chemical.attribute;

import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;

public interface ChemicalAttributeValidator {
   ChemicalAttributeValidator DEFAULT = attr -> !attr.needsValidation();
   ChemicalAttributeValidator ALWAYS_ALLOW = attr -> true;

   boolean validate(ChemicalAttribute var1);

   default boolean process(Chemical<?> chemical) {
      return chemical.getAttributes().stream().allMatch(this::validate);
   }

   default boolean process(ChemicalStack<?> stack) {
      return this.process(stack.getType());
   }

   @SafeVarargs
   static ChemicalAttributeValidator create(Class<? extends ChemicalAttribute>... validAttributes) {
      return new ChemicalAttributeValidator.SimpleAttributeValidator(validAttributes, true);
   }

   @SafeVarargs
   static ChemicalAttributeValidator createStrict(Class<? extends ChemicalAttribute>... validAttributes) {
      return new ChemicalAttributeValidator.SimpleAttributeValidator(validAttributes, false);
   }

   public static class SimpleAttributeValidator implements ChemicalAttributeValidator {
      private final Set<Class<? extends ChemicalAttribute>> validTypes;
      private final boolean allowNoValidation;

      SimpleAttributeValidator(Class<? extends ChemicalAttribute>[] attributeTypes, boolean allowNoValidation) {
         this.validTypes = Set.of(attributeTypes);
         this.allowNoValidation = allowNoValidation;
      }

      @Override
      public boolean validate(ChemicalAttribute attribute) {
         return this.validTypes.contains(attribute.getClass()) || this.allowNoValidation && !attribute.needsValidation();
      }
   }
}
