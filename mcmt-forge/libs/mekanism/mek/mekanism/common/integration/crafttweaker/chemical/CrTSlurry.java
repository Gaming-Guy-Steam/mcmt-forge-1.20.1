package mekanism.common.integration.crafttweaker.chemical;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import com.blamejared.crafttweaker_annotations.annotations.TaggableElement;
import java.util.List;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Nullable;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;

@ZenRegister
@TaggableElement("mekanism:slurry")
@NativeTypeRegistration(
   value = Slurry.class,
   zenCodeName = "mods.mekanism.api.chemical.Slurry"
)
public class CrTSlurry {
   private CrTSlurry() {
   }

   @Method
   @Operator(OperatorType.MUL)
   public static ICrTChemicalStack.ICrTSlurryStack makeStack(Slurry _this, long amount) {
      return new CrTChemicalStack.CrTSlurryStack(_this.getStack(amount));
   }

   @Method
   @Getter("tags")
   public static List<KnownTag<Slurry>> getTags(Slurry _this) {
      return CrTUtils.slurryTags().getTagsFor(_this);
   }

   @Method
   @Nullable
   public static KnownTag<Item> getOreTag(Slurry _this) {
      TagKey<Item> oreTag = _this.getOreTag();
      return oreTag == null ? null : CrTUtils.itemTags().tag(oreTag.f_203868_());
   }
}
