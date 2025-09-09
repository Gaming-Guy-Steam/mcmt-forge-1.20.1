package mekanism.common.integration.crafttweaker.chemical.attribute.gas;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.api.math.FloatingLong;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = GasAttributes.Fuel.class,
   zenCodeName = "mods.mekanism.attribute.gas.FuelAttribute"
)
public class CrTFuelAttribute {
   private CrTFuelAttribute() {
   }

   @StaticExpansionMethod
   public static GasAttributes.Fuel create(int burnTicks, FloatingLong energyDensity) {
      FloatingLong density = energyDensity.copyAsConst();
      return new GasAttributes.Fuel(burnTicks, density);
   }

   @Method
   @Getter("burnTicks")
   public static int getBurnTicks(GasAttributes.Fuel _this) {
      return _this.getBurnTicks();
   }

   @Method
   @Getter("energyPerTick")
   public static FloatingLong getEnergyPerTick(GasAttributes.Fuel _this) {
      return _this.getEnergyPerTick();
   }
}
