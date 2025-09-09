package mekanism.common.integration.crafttweaker.chemical.attribute;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = ChemicalAttribute.class,
   zenCodeName = "mods.mekanism.attribute.ChemicalAttribute"
)
public class CrTChemicalAttribute {
   private CrTChemicalAttribute() {
   }

   @Method
   public static boolean needsValidation(ChemicalAttribute _this) {
      return _this.needsValidation();
   }
}
