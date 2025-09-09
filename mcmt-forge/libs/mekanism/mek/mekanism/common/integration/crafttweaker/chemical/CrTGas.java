package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.gas.Gas;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;

@ZenRegister
@TaggableElement("mekanism:gas")
@NativeTypeRegistration(
   value = Gas.class,
   zenCodeName = "mods.mekanism.api.chemical.Gas"
)
public class CrTGas {
   private CrTGas() {
   }

   @Method
   @Operator(OperatorType.MUL)
   public static ICrTChemicalStack.ICrTGasStack makeStack(Gas _this, long amount) {
      return new CrTChemicalStack.CrTGasStack(_this.getStack(amount));
   }

   @Method
   @Getter("tags")
   public static List<KnownTag<Gas>> getTags(Gas _this) {
      return CrTUtils.gasTags().getTagsFor(_this);
   }
}
