package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;

@ZenRegister
@TaggableElement("mekanism:infuse_type")
@NativeTypeRegistration(
   value = InfuseType.class,
   zenCodeName = "mods.mekanism.api.chemical.InfuseType"
)
public class CrTInfuseType {
   private CrTInfuseType() {
   }

   @Method
   @Operator(OperatorType.MUL)
   public static ICrTChemicalStack.ICrTInfusionStack makeStack(InfuseType _this, long amount) {
      return new CrTChemicalStack.CrTInfusionStack(_this.getStack(amount));
   }

   @Method
   @Getter("tags")
   public static List<KnownTag<InfuseType>> getTags(InfuseType _this) {
      return CrTUtils.infuseTypeTags().getTagsFor(_this);
   }
}
