package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;

@ZenRegister
@TaggableElement("mekanism:pigment")
@NativeTypeRegistration(
   value = Pigment.class,
   zenCodeName = "mods.mekanism.api.chemical.Pigment"
)
public class CrTPigment {
   private CrTPigment() {
   }

   @Method
   @Operator(OperatorType.MUL)
   public static ICrTChemicalStack.ICrTPigmentStack makeStack(Pigment _this, long amount) {
      return new CrTChemicalStack.CrTPigmentStack(_this.getStack(amount));
   }

   @Method
   @Getter("tags")
   public static List<KnownTag<Pigment>> getTags(Pigment _this) {
      return CrTUtils.pigmentTags().getTagsFor(_this);
   }
}
