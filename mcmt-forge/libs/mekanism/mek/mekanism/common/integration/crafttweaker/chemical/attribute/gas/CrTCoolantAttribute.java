package mekanism.common.integration.crafttweaker.chemical.attribute.gas;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.integration.LazyGasProvider;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = GasAttributes.Coolant.class,
   zenCodeName = "mods.mekanism.attribute.gas.CoolantAttribute"
)
public class CrTCoolantAttribute {
   private CrTCoolantAttribute() {
   }

   @StaticExpansionMethod
   public static GasAttributes.CooledCoolant cooled(Supplier<Gas> heatedGas, double thermalEnthalpy, double conductivity) {
      return new GasAttributes.CooledCoolant(new LazyGasProvider(heatedGas), thermalEnthalpy, conductivity);
   }

   @StaticExpansionMethod
   public static GasAttributes.HeatedCoolant heated(Supplier<Gas> cooledGas, double thermalEnthalpy, double conductivity) {
      return new GasAttributes.HeatedCoolant(new LazyGasProvider(cooledGas), thermalEnthalpy, conductivity);
   }

   @Method
   @Getter("thermalEnthalpy")
   public static double getThermalEnthalpy(GasAttributes.Coolant _this) {
      return _this.getThermalEnthalpy();
   }

   @Method
   @Getter("conductivity")
   public static double getConductivity(GasAttributes.Coolant _this) {
      return _this.getConductivity();
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = GasAttributes.CooledCoolant.class,
      zenCodeName = "mods.mekanism.attribute.gas.CooledCoolantAttribute"
   )
   public static class CrTCooledCoolantAttribute {
      private CrTCooledCoolantAttribute() {
      }

      @Method
      public static Gas getHeatedGas(GasAttributes.CooledCoolant _this) {
         return _this.getHeatedGas();
      }
   }

   @ZenRegister
   @NativeTypeRegistration(
      value = GasAttributes.HeatedCoolant.class,
      zenCodeName = "mods.mekanism.attribute.gas.HeatedCoolantAttribute"
   )
   public static class CrTHeatedCoolantAttribute {
      private CrTHeatedCoolantAttribute() {
      }

      @Method
      public static Gas getCooledGas(GasAttributes.HeatedCoolant _this) {
         return _this.getCooledGas();
      }
   }
}
