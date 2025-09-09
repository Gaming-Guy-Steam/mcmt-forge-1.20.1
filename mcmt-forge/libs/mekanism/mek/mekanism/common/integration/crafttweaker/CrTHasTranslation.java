package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.text.IHasTranslationKey;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = IHasTranslationKey.class,
   zenCodeName = "mods.mekanism.api.text.HasTranslation"
)
public class CrTHasTranslation {
   @Method
   @Getter("translationKey")
   public static String getTranslationKey(IHasTranslationKey _this) {
      return _this.getTranslationKey();
   }
}
