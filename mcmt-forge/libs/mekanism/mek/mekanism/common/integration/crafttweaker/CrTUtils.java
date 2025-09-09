package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.tag.CraftTweakerTagRegistry;
import com.blamejared.crafttweaker.api.tag.manager.type.KnownTagManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class CrTUtils {
   public static final Function<GasStack, ICrTChemicalStack.ICrTGasStack> GAS_CONVERTER = CrTChemicalStack.CrTGasStack::new;
   public static final Function<InfusionStack, ICrTChemicalStack.ICrTInfusionStack> INFUSION_CONVERTER = CrTChemicalStack.CrTInfusionStack::new;
   public static final Function<PigmentStack, ICrTChemicalStack.ICrTPigmentStack> PIGMENT_CONVERTER = CrTChemicalStack.CrTPigmentStack::new;
   public static final Function<SlurryStack, ICrTChemicalStack.ICrTSlurryStack> SLURRY_CONVERTER = CrTChemicalStack.CrTSlurryStack::new;

   public static ResourceLocation rl(String path) {
      return new ResourceLocation("crafttweaker", path);
   }

   public static ICrTChemicalStack.ICrTGasStack stackFromGas(Gas gas) {
      return new CrTChemicalStack.CrTGasStack(gas.getStack(1L));
   }

   public static ICrTChemicalStack.ICrTInfusionStack stackFromInfuseType(InfuseType infuseType) {
      return new CrTChemicalStack.CrTInfusionStack(infuseType.getStack(1L));
   }

   public static ICrTChemicalStack.ICrTPigmentStack stackFromPigment(Pigment pigment) {
      return new CrTChemicalStack.CrTPigmentStack(pigment.getStack(1L));
   }

   public static ICrTChemicalStack.ICrTSlurryStack stackFromSlurry(Slurry slurry) {
      return new CrTChemicalStack.CrTSlurryStack(slurry.getStack(1L));
   }

   @Nullable
   public static ICrTChemicalStack<?, ?, ?> fromBoxedStack(BoxedChemicalStack stack) {
      if (stack.isEmpty()) {
         return null;
      } else {
         return (ICrTChemicalStack<?, ?, ?>)(switch (stack.getChemicalType()) {
            case GAS -> new CrTChemicalStack.CrTGasStack((GasStack)stack.getChemicalStack());
            case INFUSION -> new CrTChemicalStack.CrTInfusionStack((InfusionStack)stack.getChemicalStack());
            case PIGMENT -> new CrTChemicalStack.CrTPigmentStack((PigmentStack)stack.getChemicalStack());
            case SLURRY -> new CrTChemicalStack.CrTSlurryStack((SlurryStack)stack.getChemicalStack());
         });
      }
   }

   public static <C> Optional<C> getSingleIfPresent(IDecomposedRecipe recipe, IRecipeComponent<C> component) {
      List<C> values = recipe.get(component);
      if (values == null) {
         return Optional.empty();
      } else if (values.size() != 1) {
         String message = String.format(
            "Expected a list with a single element for %s, but got %d-sized list: %s", component.getCommandString(), values.size(), values
         );
         throw new IllegalArgumentException(message);
      } else {
         return Optional.of(values.get(0));
      }
   }

   public static <C> CrTUtils.UnaryTypePair<C> getPair(IDecomposedRecipe recipe, IRecipeComponent<C> component) {
      List<C> list = recipe.getOrThrow(component);
      if (list.size() != 2) {
         String message = String.format(
            "Expected a list with two elements element for %s, but got %d-sized list: %s", component.getCommandString(), list.size(), list
         );
         throw new IllegalArgumentException(message);
      } else {
         return new CrTUtils.UnaryTypePair<>(list.get(0), list.get(1));
      }
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> String describeOutputs(List<STACK> outputs) {
      return outputs.isEmpty() ? "" : describeOutputs(outputs, getConverter(outputs.get(0)));
   }

   public static <TYPE> String describeOutputs(List<TYPE> outputs, Function<TYPE, ?> converter) {
      int size = outputs.size();
      if (size == 0) {
         return "";
      } else if (size == 1) {
         return converter.apply(outputs.get(0)).toString();
      } else {
         StringBuilder description = new StringBuilder();

         for (int i = 0; i < size; i++) {
            if (i > 0) {
               description.append(", or ");
            }

            description.append(converter.apply(outputs.get(i)));
         }

         return description.toString();
      }
   }

   public static <TYPE> TagKey<TYPE> validateTagAndGet(KnownTag<TYPE> tag) {
      if (tag.exists()) {
         return tag.getTagKey();
      } else {
         throw new IllegalArgumentException("Tag " + tag.getCommandString() + " does not exist.");
      }
   }

   public static <TYPE, CRT_TYPE> List<CRT_TYPE> convert(List<TYPE> elements, Function<TYPE, CRT_TYPE> converter) {
      return elements.stream().map(converter).toList();
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> Function<STACK, CRT_STACK> getConverter(
      STACK stack
   ) {
      return switch (ChemicalType.getTypeFor(stack)) {
         case GAS -> GAS_CONVERTER;
         case INFUSION -> INFUSION_CONVERTER;
         case PIGMENT -> PIGMENT_CONVERTER;
         case SLURRY -> SLURRY_CONVERTER;
      };
   }

   public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>> List<CRT_STACK> convertChemical(
      List<STACK> elements
   ) {
      return elements.isEmpty() ? Collections.emptyList() : convert(elements, getConverter(elements.get(0)));
   }

   public static List<ICrTChemicalStack.ICrTGasStack> convertGas(List<GasStack> elements) {
      return convert(elements, GAS_CONVERTER);
   }

   public static List<ICrTChemicalStack.ICrTInfusionStack> convertInfusion(List<InfusionStack> elements) {
      return convert(elements, INFUSION_CONVERTER);
   }

   public static List<ICrTChemicalStack.ICrTPigmentStack> convertPigment(List<PigmentStack> elements) {
      return convert(elements, PIGMENT_CONVERTER);
   }

   public static List<ICrTChemicalStack.ICrTSlurryStack> convertSlurry(List<SlurryStack> elements) {
      return convert(elements, SLURRY_CONVERTER);
   }

   public static List<IItemStack> convertItems(List<ItemStack> elements) {
      return convert(elements, IItemStack::of);
   }

   public static List<IFluidStack> convertFluids(List<FluidStack> elements) {
      return convert(elements, IFluidStack::of);
   }

   public static KnownTagManager<Item> itemTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registries.f_256913_);
   }

   public static KnownTagManager<Fluid> fluidTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(Registries.f_256808_);
   }

   public static KnownTagManager<Gas> gasTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.GAS_REGISTRY_NAME);
   }

   public static KnownTagManager<InfuseType> infuseTypeTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.INFUSE_TYPE_REGISTRY_NAME);
   }

   public static KnownTagManager<Pigment> pigmentTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.PIGMENT_REGISTRY_NAME);
   }

   public static KnownTagManager<Slurry> slurryTags() {
      return CraftTweakerTagRegistry.INSTANCE.knownTagManager(MekanismAPI.SLURRY_REGISTRY_NAME);
   }

   public record UnaryTypePair<TYPE>(TYPE a, TYPE b) {
   }
}
