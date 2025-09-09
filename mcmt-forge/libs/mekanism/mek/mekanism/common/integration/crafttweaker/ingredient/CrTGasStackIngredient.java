package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
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
   value = ChemicalStackIngredient.GasStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient"
)
public class CrTGasStackIngredient {
   private CrTGasStackIngredient() {
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.GasStackIngredient from(Gas instance, long amount) {
      CrTIngredientHelper.assertValid(instance, amount, "GasStackIngredients", "gas");
      return (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().from(instance, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.GasStackIngredient from(ICrTChemicalStack.ICrTGasStack instance) {
      CrTIngredientHelper.assertValid(instance, "GasStackIngredients");
      return (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().from((GasStack)instance.getImmutableInternal());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.GasStackIngredient from(KnownTag<Gas> gasTag, long amount) {
      TagKey<Gas> tag = CrTIngredientHelper.assertValidAndGet(gasTag, amount, "GasStackIngredients");
      return (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas().from(tag, amount);
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.GasStackIngredient from(Many<KnownTag<Gas>> gasTag) {
      return from((KnownTag<Gas>)gasTag.getData(), (long)gasTag.getAmount());
   }

   @StaticExpansionMethod
   public static ChemicalStackIngredient.GasStackIngredient createMulti(ChemicalStackIngredient.GasStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("GasStackIngredients", IngredientCreatorAccess.gas(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(ChemicalStackIngredient.GasStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   public static boolean testType(ChemicalStackIngredient.GasStackIngredient _this, ICrTChemicalStack.ICrTGasStack type) {
      return _this.testType((GasStack)type.getInternal());
   }

   @Method
   public static boolean test(ChemicalStackIngredient.GasStackIngredient _this, ICrTChemicalStack.ICrTGasStack stack) {
      return _this.test((GasStack)stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<ICrTChemicalStack.ICrTGasStack> getRepresentations(ChemicalStackIngredient.GasStackIngredient _this) {
      return CrTUtils.convertGas(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static ChemicalStackIngredient.GasStackIngredient or(
      ChemicalStackIngredient.GasStackIngredient _this, ChemicalStackIngredient.GasStackIngredient other
   ) {
      return (ChemicalStackIngredient.GasStackIngredient)IngredientCreatorAccess.gas()
         .createMulti(new ChemicalStackIngredient.GasStackIngredient[]{_this, other});
   }
}
