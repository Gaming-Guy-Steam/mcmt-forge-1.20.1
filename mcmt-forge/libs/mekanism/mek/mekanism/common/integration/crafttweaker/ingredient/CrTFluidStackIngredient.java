package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.IData;
import com.blamejared.crafttweaker.api.data.converter.JSONConverter;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient.CompoundFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient.FluidTagWithAmountIngredient;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.creator.IFluidStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Operator;
import org.openzen.zencode.java.ZenCodeType.OperatorType;
import org.openzen.zencode.java.ZenCodeType.StaticExpansionMethod;

@ZenRegister
@NativeTypeRegistration(
   value = FluidStackIngredient.class,
   zenCodeName = "mods.mekanism.api.ingredient.FluidStackIngredient"
)
public class CrTFluidStackIngredient {
   private CrTFluidStackIngredient() {
   }

   @StaticExpansionMethod
   public static FluidStackIngredient from(Fluid fluid, int amount) {
      CrTIngredientHelper.assertValidAmount("FluidStackIngredients", amount);
      if (fluid == Fluids.f_76191_) {
         throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty fluid.");
      } else {
         return IngredientCreatorAccess.fluid().from(fluid, amount);
      }
   }

   @StaticExpansionMethod
   public static FluidStackIngredient from(IFluidStack instance) {
      if (instance.isEmpty()) {
         throw new IllegalArgumentException("FluidStackIngredients cannot be created from an empty stack.");
      } else {
         return IngredientCreatorAccess.fluid().from((FluidStack)instance.getImmutableInternal());
      }
   }

   @StaticExpansionMethod
   public static FluidStackIngredient from(KnownTag<Fluid> fluidTag, int amount) {
      TagKey<Fluid> tag = CrTIngredientHelper.assertValidAndGet(fluidTag, amount, "FluidStackIngredients");
      return IngredientCreatorAccess.fluid().from(tag, amount);
   }

   @StaticExpansionMethod
   public static FluidStackIngredient from(Many<KnownTag<Fluid>> fluidTag) {
      return from((KnownTag<Fluid>)fluidTag.getData(), fluidTag.getAmount());
   }

   @StaticExpansionMethod
   public static FluidStackIngredient from(CTFluidIngredient ingredient) {
      IFluidStackIngredientCreator ingredientCreator = IngredientCreatorAccess.fluid();
      return (FluidStackIngredient)ingredient.mapTo(
         fluidStack -> ingredientCreator.from((FluidStack)fluidStack.getInternal()), ingredientCreator::from, ingredientCreator::from
      );
   }

   @StaticExpansionMethod
   public static FluidStackIngredient createMulti(FluidStackIngredient... ingredients) {
      return CrTIngredientHelper.createMulti("FluidStackIngredients", IngredientCreatorAccess.fluid(), ingredients);
   }

   @Method
   @Caster(
      implicit = true
   )
   public static IData asIData(FluidStackIngredient _this) {
      return JSONConverter.convert(_this.serialize());
   }

   @Method
   @Caster(
      implicit = true
   )
   public static CTFluidIngredient asCTFluidIngredient(FluidStackIngredient _this) {
      if (_this instanceof FluidStackIngredientCreator.SingleFluidStackIngredient single) {
         return new com.blamejared.crafttweaker.api.fluid.CTFluidIngredient.FluidStackIngredient(IFluidStack.of(single.getInputRaw().copy()));
      } else if (_this instanceof FluidStackIngredientCreator.TaggedFluidStackIngredient tagged) {
         return new FluidTagWithAmountIngredient(((KnownTag)CrTUtils.fluidTags().tag(tagged.getTag())).withAmount(tagged.getRawAmount()));
      } else if (_this instanceof FluidStackIngredientCreator.MultiFluidStackIngredient multi) {
         return new CompoundFluidIngredient(multi.getIngredients().stream().map(CrTFluidStackIngredient::asCTFluidIngredient).collect(Collectors.toList()));
      } else {
         CrTConstants.CRT_LOGGER.error("Unknown fluid ingredient type {}, this should never happen. Returning empty.", _this.getClass().getName());
         return (CTFluidIngredient)CTFluidIngredient.EMPTY.get();
      }
   }

   @Method
   public static boolean testType(FluidStackIngredient _this, IFluidStack type) {
      return _this.testType((FluidStack)type.getInternal());
   }

   @Method
   public static boolean test(FluidStackIngredient _this, IFluidStack stack) {
      return _this.test((FluidStack)stack.getInternal());
   }

   @Method
   @Getter("representations")
   public static List<IFluidStack> getRepresentations(FluidStackIngredient _this) {
      return CrTUtils.convertFluids(_this.getRepresentations());
   }

   @Method
   @Operator(OperatorType.OR)
   public static FluidStackIngredient or(FluidStackIngredient _this, FluidStackIngredient other) {
      return IngredientCreatorAccess.fluid().createMulti(new FluidStackIngredient[]{_this, other});
   }
}
