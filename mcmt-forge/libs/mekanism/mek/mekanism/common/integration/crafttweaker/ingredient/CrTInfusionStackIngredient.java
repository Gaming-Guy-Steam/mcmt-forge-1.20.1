package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
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
   value = ChemicalStackIngredient.InfusionStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient"
)
public class CrTInfusionStackIngredient {
   private CrTInfusionStackIngredient() {
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.InfusionStackIngredient from(InfuseType instance, long amount) {
      CrTIngredientHelper.assertValid(instance, amount, "InfusionStackIngredients", "infuse type");
      return (ChemicalStackIngredient.InfusionStackIngredient)IngredientCreatorAccess.infusion().from(instance, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.InfusionStackIngredient from(ICrTChemicalStack.ICrTInfusionStack instance) {
      CrTIngredientHelper.assertValid(instance, "InfusionStackIngredients");
      return (ChemicalStackIngredient.InfusionStackIngredient)IngredientCreatorAccess.infusion().from((InfusionStack)instance.getImmutableInternal());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.InfusionStackIngredient from(KnownTag<InfuseType> infuseTypeTag, long amount) {
      TagKey<InfuseType> tag = CrTIngredientHelper.assertValidAndGet(infuseTypeTag, amount, "InfusionStackIngredients");
      return (ChemicalStackIngredient.InfusionStackIngredient)IngredientCreatorAccess.infusion().from(tag, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.InfusionStackIngredient from(Many<KnownTag<InfuseType>> infuseTypeTag) {
      return from((KnownTag<InfuseType>)infuseTypeTag.getData(), (long)infuseTypeTag.getAmount());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.InfusionStackIngredient createMulti(ChemicalStackIngredient.InfusionStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("InfusionStackIngredients", IngredientCreatorAccess.infusion(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(ChemicalStackIngredient.InfusionStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   public static boolean testType(ChemicalStackIngredient.InfusionStackIngredient _this, ICrTChemicalStack.ICrTInfusionStack type) {
      return _this.testType((InfusionStack)type.getInternal());
   }

   @Method
   public static boolean test(ChemicalStackIngredient.InfusionStackIngredient _this, ICrTChemicalStack.ICrTInfusionStack stack) {
      return _this.test((InfusionStack)stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<ICrTChemicalStack.ICrTInfusionStack> getRepresentations(ChemicalStackIngredient.InfusionStackIngredient _this) {
      return CrTUtils.convertInfusion(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static ChemicalStackIngredient.InfusionStackIngredient or(
      ChemicalStackIngredient.InfusionStackIngredient _this, ChemicalStackIngredient.InfusionStackIngredient other
   ) {
      return (ChemicalStackIngredient.InfusionStackIngredient)IngredientCreatorAccess.infusion()
         .createMulti(new ChemicalStackIngredient.InfusionStackIngredient[]{_this, other});
   }
}
