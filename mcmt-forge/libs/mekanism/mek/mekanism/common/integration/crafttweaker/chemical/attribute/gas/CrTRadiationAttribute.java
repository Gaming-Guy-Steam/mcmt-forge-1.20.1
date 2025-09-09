package mekanism.common.integration.crafttweaker.chemical.attribute.gas;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = GasAttributes.Radiation.class,
   zenCodeName = "mods.mekanism.attribute.gas.RadiationAttribute"
)
public class CrTRadiationAttribute {
   private CrTRadiationAttribute() {
   }

   @StaticExpansionMethod
   public static GasAttributes.Radiation create(double radioactivity) {
      return new GasAttributes.Radiation(radioactivity);
   }

   @Method
   @Getter("radioactivity")
   public static double getRadioactivity(GasAttributes.Radiation _this) {
      return _this.getRadioactivity();
   }
}
