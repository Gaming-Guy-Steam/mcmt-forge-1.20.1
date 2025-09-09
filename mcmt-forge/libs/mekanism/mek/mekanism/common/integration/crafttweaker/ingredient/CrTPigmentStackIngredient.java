package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
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
   value = ChemicalStackIngredient.PigmentStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ChemicalStackIngredient.PigmentStackIngredient"
)
public class CrTPigmentStackIngredient {
   private CrTPigmentStackIngredient() {
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.PigmentStackIngredient from(Pigment instance, long amount) {
      CrTIngredientHelper.assertValid(instance, amount, "PigmentStackIngredients", "pigment");
      return (ChemicalStackIngredient.PigmentStackIngredient)IngredientCreatorAccess.pigment().from(instance, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.PigmentStackIngredient from(ICrTChemicalStack.ICrTPigmentStack instance) {
      CrTIngredientHelper.assertValid(instance, "PigmentStackIngredients");
      return (ChemicalStackIngredient.PigmentStackIngredient)IngredientCreatorAccess.pigment().from((PigmentStack)instance.getImmutableInternal());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.PigmentStackIngredient from(KnownTag<Pigment> pigmentTag, long amount) {
      TagKey<Pigment> tag = CrTIngredientHelper.assertValidAndGet(pigmentTag, amount, "PigmentStackIngredients");
      return (ChemicalStackIngredient.PigmentStackIngredient)IngredientCreatorAccess.pigment().from(tag, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.PigmentStackIngredient from(Many<KnownTag<Pigment>> pigmentTag) {
      return from((KnownTag<Pigment>)pigmentTag.getData(), (long)pigmentTag.getAmount());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.PigmentStackIngredient createMulti(ChemicalStackIngredient.PigmentStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("PigmentStackIngredients", IngredientCreatorAccess.pigment(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(ChemicalStackIngredient.PigmentStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   public static boolean testType(ChemicalStackIngredient.PigmentStackIngredient _this, ICrTChemicalStack.ICrTPigmentStack type) {
      return _this.testType((PigmentStack)type.getInternal());
   }

   @Method
   public static boolean test(ChemicalStackIngredient.PigmentStackIngredient _this, ICrTChemicalStack.ICrTPigmentStack stack) {
      return _this.test((PigmentStack)stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<ICrTChemicalStack.ICrTPigmentStack> getRepresentations(ChemicalStackIngredient.PigmentStackIngredient _this) {
      return CrTUtils.convertPigment(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static ChemicalStackIngredient.PigmentStackIngredient or(
      ChemicalStackIngredient.PigmentStackIngredient _this, ChemicalStackIngredient.PigmentStackIngredient other
   ) {
      return (ChemicalStackIngredient.PigmentStackIngredient)IngredientCreatorAccess.pigment()
         .createMulti(new ChemicalStackIngredient.PigmentStackIngredient[]{_this, other});
   }
}
