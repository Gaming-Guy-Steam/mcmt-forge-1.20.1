package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.tags.TagKey;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = ChemicalStackIngredient.SlurryStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ChemicalStackIngredient.SlurryStackIngredient"
)
public class CrTSlurryStackIngredient {
   private CrTSlurryStackIngredient() {
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.SlurryStackIngredient from(Slurry instance, long amount) {
      CrTIngredientHelper.assertValid(instance, amount, "SlurryStackIngredients", "slurry");
      return (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry().from(instance, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.SlurryStackIngredient from(ICrTChemicalStack.ICrTSlurryStack instance) {
      CrTIngredientHelper.assertValid(instance, "SlurryStackIngredients");
      return (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry().from((SlurryStack)instance.getImmutableInternal());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.SlurryStackIngredient from(KnownTag<Slurry> slurryTag, long amount) {
      TagKey<Slurry> tag = CrTIngredientHelper.assertValidAndGet(slurryTag, amount, "SlurryStackIngredients");
      return (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry().from(tag, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.SlurryStackIngredient from(Many<KnownTag<Slurry>> slurryTag) {
      return from((KnownTag<Slurry>)slurryTag.getData(), (long)slurryTag.getAmount());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.SlurryStackIngredient createMulti(ChemicalStackIngredient.SlurryStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("SlurryStackIngredients", IngredientCreatorAccess.slurry(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(ChemicalStackIngredient.SlurryStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   public static boolean testType(ChemicalStackIngredient.SlurryStackIngredient _this, ICrTChemicalStack.ICrTSlurryStack type) {
      return _this.testType((SlurryStack)type.getInternal());
   }

   @Method
   public static boolean test(ChemicalStackIngredient.SlurryStackIngredient _this, ICrTChemicalStack.ICrTSlurryStack stack) {
      return _this.test((SlurryStack)stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<ICrTChemicalStack.ICrTSlurryStack> getRepresentations(ChemicalStackIngredient.SlurryStackIngredient _this) {
      return CrTUtils.convertSlurry(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static ChemicalStackIngredient.SlurryStackIngredient or(
      ChemicalStackIngredient.SlurryStackIngredient _this, ChemicalStackIngredient.SlurryStackIngredient other
   ) {
      return (ChemicalStackIngredient.SlurryStackIngredient)IngredientCreatorAccess.slurry()
         .createMulti(new ChemicalStackIngredient.SlurryStackIngredient[]{_this, other});
   }
}
