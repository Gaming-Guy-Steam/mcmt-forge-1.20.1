package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.text.IHasTextComponent;
import net.minecraft.network.chat.Component;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@NativeTypeRegistration(
   value = IHasTextComponent.class,
   zenCodeName = "mods.mekanism.api.text.HasTextComponent"
)
public class CrTHasTextComponent {
   @Method
   public static Component getTextComponent(IHasTextComponent _this) {
      return _this.getTextComponent();
   }
}
