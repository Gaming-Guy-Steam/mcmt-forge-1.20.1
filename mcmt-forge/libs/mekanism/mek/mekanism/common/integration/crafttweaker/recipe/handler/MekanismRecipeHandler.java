package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.component.IRecipeComponent;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents.Processing;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import com.blamejared.crafttweaker.api.tag.manager.type.KnownTagManager;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.ItemStackUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.CrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.recipe.ingredient.IMultiIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import mekanism.common.recipe.ingredient.chemical.MultiChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.SingleChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.TaggedChemicalStackIngredient;
import mekanism.common.recipe.ingredient.creator.FluidStackIngredientCreator;
import mekanism.common.recipe.ingredient.creator.ItemStackIngredientCreator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public abstract class MekanismRecipeHandler<RECIPE extends MekanismRecipe> implements IRecipeHandler<RECIPE> {
   protected static final Object SKIP_OPTIONAL_PARAM = new Object();

   public abstract <U extends Recipe<?>> boolean doesConflict(final IRecipeManager<? super RECIPE> manager, final RECIPE recipe, final U other);

   protected <TYPE, INGREDIENT extends InputIngredient<TYPE>> boolean ingredientConflicts(INGREDIENT a, INGREDIENT b) {
      return a.getRepresentations().stream().anyMatch(b::testType);
   }

   protected <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean chemicalIngredientConflicts(
      ChemicalStackIngredient<CHEMICAL, STACK> a, ChemicalStackIngredient<?, ?> b
   ) {
      return ChemicalType.getTypeFor(a) == ChemicalType.getTypeFor(b) && this.ingredientConflicts(a, (ChemicalStackIngredient<CHEMICAL, STACK>)b);
   }

   protected String buildCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe, Object... params) {
      return this.buildCommandString(manager, "addRecipe", recipe, params);
   }

   protected String buildCommandString(IRecipeManager<? super RECIPE> manager, String method, RECIPE recipe, Object... params) {
      StringBuilder commandString = new StringBuilder(manager.getCommandString())
         .append('.')
         .append(method)
         .append("(\"")
         .append(recipe.m_6423_().m_135815_())
         .append('"');

      for (Object param : params) {
         if (param != SKIP_OPTIONAL_PARAM) {
            commandString.append(", ").append(this.convertParam(param));
         }
      }

      return commandString.append(");").toString();
   }

   private String convertParam(Object param) {
      if (param instanceof ItemStack stack) {
         return ItemStackUtil.getCommandString(stack);
      } else if (param instanceof FluidStack stack) {
         return IFluidStack.of(stack).getCommandString();
      } else if (param instanceof GasStack stack) {
         return new CrTChemicalStack.CrTGasStack(stack).getCommandString();
      } else if (param instanceof InfusionStack stack) {
         return new CrTChemicalStack.CrTInfusionStack(stack).getCommandString();
      } else if (param instanceof PigmentStack stack) {
         return new CrTChemicalStack.CrTPigmentStack(stack).getCommandString();
      } else if (param instanceof SlurryStack stack) {
         return new CrTChemicalStack.CrTSlurryStack(stack).getCommandString();
      } else if (param instanceof BoxedChemicalStack stack) {
         return this.convertParam(stack.getChemicalStack());
      } else if (param instanceof FloatingLong fl) {
         return fl.getDecimal() == 0 ? fl.toString(0) : fl.toString().replaceAll("0*$", "");
      } else if (param instanceof Number || param instanceof Boolean) {
         return param.toString();
      } else if (param instanceof ItemStackIngredient ingredient) {
         return this.convertIngredient(ingredient);
      } else if (param instanceof FluidStackIngredient ingredient) {
         return this.convertIngredient(ingredient);
      } else if (param instanceof ChemicalStackIngredient.GasStackIngredient ingredient) {
         return this.convertIngredient(
            "mods.mekanism.api.ingredient.ChemicalStackIngredient.GasStackIngredient", CrTUtils.gasTags(), ChemicalIngredientDeserializer.GAS, ingredient
         );
      } else if (param instanceof ChemicalStackIngredient.InfusionStackIngredient ingredient) {
         return this.convertIngredient(
            "mods.mekanism.api.ingredient.ChemicalStackIngredient.InfusionStackIngredient",
            CrTUtils.infuseTypeTags(),
            ChemicalIngredientDeserializer.INFUSION,
            ingredient
         );
      } else if (param instanceof ChemicalStackIngredient.PigmentStackIngredient ingredient) {
         return this.convertIngredient(
            "mods.mekanism.api.ingredient.ChemicalStackIngredient.PigmentStackIngredient",
            CrTUtils.pigmentTags(),
            ChemicalIngredientDeserializer.PIGMENT,
            ingredient
         );
      } else if (param instanceof ChemicalStackIngredient.SlurryStackIngredient ingredient) {
         return this.convertIngredient(
            "mods.mekanism.api.ingredient.ChemicalStackIngredient.SlurryStackIngredient",
            CrTUtils.slurryTags(),
            ChemicalIngredientDeserializer.SLURRY,
            ingredient
         );
      } else if (param instanceof List<?> list) {
         return list.isEmpty() ? "Invalid (output) list, no outputs" : this.convertParam(list.get(0));
      } else {
         return param instanceof ElectrolysisRecipe.ElectrolysisRecipeOutput output
            ? this.convertParam(output.left()) + ", " + this.convertParam(output.right())
            : "Unimplemented: " + param;
      }
   }

   @Nullable
   public static String basicImplicitIngredient(Ingredient vanillaIngredient, int amount, JsonElement serialized) {
      return basicImplicitIngredient(vanillaIngredient, amount, serialized, true);
   }

   @Nullable
   public static String basicImplicitIngredient(Ingredient vanillaIngredient, int amount, JsonElement serialized, boolean handleTags) {
      if (serialized.isJsonObject()) {
         JsonObject serializedIngredient = serialized.getAsJsonObject();
         if (vanillaIngredient.isVanilla()) {
            if (serializedIngredient.has("item")) {
               Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(serializedIngredient.get("item").getAsString()));
               return ItemStackUtil.getCommandString(new ItemStack(item, amount));
            }

            if (handleTags && serializedIngredient.has("tag")) {
               KnownTag<Item> tag = CrTUtils.itemTags().tag(serializedIngredient.get("tag").getAsString());
               return amount == 1 ? tag.getCommandString() : tag.withAmount(amount).getCommandString();
            }
         } else if (vanillaIngredient instanceof StrictNBTIngredient) {
            ItemStack stack = CraftingHelper.getItemStack(serializedIngredient, true);
            stack.m_41764_(amount);
            return ItemStackUtil.getCommandString(stack);
         }
      }

      return null;
   }

   private String convertIngredient(ItemStackIngredient ingredient) {
      if (ingredient instanceof ItemStackIngredientCreator.SingleItemStackIngredient single) {
         JsonObject serialized = ingredient.serialize().getAsJsonObject();
         Ingredient vanillaIngredient = single.getInputRaw();
         int amount = GsonHelper.m_13824_(serialized, "amount", 1);
         String rep = basicImplicitIngredient(vanillaIngredient, amount, serialized.get("ingredient"));
         if (rep == null) {
            rep = IIngredient.fromIngredient(vanillaIngredient).getCommandString();
            if (amount > 1) {
               return "mods.mekanism.api.ingredient.ItemStackIngredient.from(" + rep + ", " + amount + ")";
            }
         }

         return rep;
      } else {
         return ingredient instanceof ItemStackIngredientCreator.MultiItemStackIngredient multiIngredient
            ? this.convertMultiIngredient("mods.mekanism.api.ingredient.ItemStackIngredient", multiIngredient, this::convertIngredient)
            : "Unimplemented itemstack ingredient: " + ingredient;
      }
   }

   private String convertIngredient(FluidStackIngredient ingredient) {
      if (ingredient instanceof FluidStackIngredientCreator.SingleFluidStackIngredient) {
         JsonObject serialized = ingredient.serialize().getAsJsonObject();
         return IFluidStack.of(SerializerHelper.deserializeFluid(serialized)).getCommandString();
      } else if (ingredient instanceof FluidStackIngredientCreator.TaggedFluidStackIngredient) {
         JsonObject serialized = ingredient.serialize().getAsJsonObject();
         return CrTUtils.fluidTags().tag(serialized.get("tag").getAsString()).withAmount(serialized.getAsJsonPrimitive("amount").getAsInt()).getCommandString();
      } else {
         return ingredient instanceof FluidStackIngredientCreator.MultiFluidStackIngredient multiIngredient
            ? this.convertMultiIngredient("mods.mekanism.api.ingredient.FluidStackIngredient", multiIngredient, this::convertIngredient)
            : "Unimplemented fluidstack ingredient: " + ingredient;
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> String convertIngredient(
      String crtClass,
      KnownTagManager<CHEMICAL> tagManager,
      ChemicalIngredientDeserializer<CHEMICAL, STACK, ?> deserializer,
      ChemicalStackIngredient<CHEMICAL, STACK> ingredient
   ) {
      if (ingredient instanceof SingleChemicalStackIngredient) {
         JsonObject serialized = ingredient.serialize().getAsJsonObject();
         return this.convertParam(deserializer.deserializeStack(serialized));
      } else if (ingredient instanceof TaggedChemicalStackIngredient) {
         JsonObject serialized = ingredient.serialize().getAsJsonObject();
         KnownTag<CHEMICAL> tag = tagManager.tag(serialized.get("tag").getAsString());
         long amount = serialized.getAsJsonPrimitive("amount").getAsLong();
         return amount > 0L && amount <= 2147483647L
            ? tag.withAmount((int)amount).getCommandString()
            : crtClass + ".from(" + tag.getCommandString() + ", " + amount + ")";
      } else {
         return ingredient instanceof MultiChemicalStackIngredient<CHEMICAL, STACK, ?> multiIngredient
            ? this.convertMultiIngredient(crtClass, multiIngredient, i -> this.convertIngredient(crtClass, tagManager, deserializer, i))
            : "Unimplemented chemical stack ingredient: " + ingredient;
      }
   }

   private <TYPE, INGREDIENT extends InputIngredient<TYPE>> String convertMultiIngredient(
      String crtClass, IMultiIngredient<TYPE, INGREDIENT> multiIngredient, Function<INGREDIENT, String> converter
   ) {
      StringBuilder builder = new StringBuilder(crtClass + ".createMulti(");
      multiIngredient.forEachIngredient(i -> {
         builder.append(converter.apply(i)).append(", ");
         return false;
      });
      builder.setLength(builder.length() - 2);
      builder.append(")");
      return builder.toString();
   }

   protected Optional<IDecomposedRecipe> decompose(Object... importantData) {
      MekanismRecipeHandler.TypeData<ItemStackIngredient, FluidStackIngredient, ChemicalStackIngredient<?, ?>> inputs = new MekanismRecipeHandler.TypeData<>(
         ChemicalType::getTypeFor
      );
      MekanismRecipeHandler.TypeData<IItemStack, IFluidStack, ChemicalStack<?>> outputs = new MekanismRecipeHandler.TypeData<>(ChemicalType::getTypeFor);
      int duration = -1;
      FloatingLong energy = null;

      for (Object data : importantData) {
         if (data instanceof List<?> dataList) {
            if (dataList.size() != 1) {
               return Optional.empty();
            }

            data = dataList.get(0);
         }

         if (data instanceof ItemStackIngredient ingredient) {
            inputs.addItem(ingredient);
         } else if (data instanceof FluidStackIngredient ingredient) {
            inputs.addFluid(ingredient);
         } else if (data instanceof ChemicalStackIngredient<?, ?> ingredient) {
            inputs.addChemical(ingredient);
         } else if (data instanceof ItemStack stack) {
            outputs.addItem(IItemStack.of(stack));
         } else if (data instanceof FluidStack stack) {
            outputs.addFluid(IFluidStack.of(stack));
         } else if (data instanceof ChemicalStack<?> stack) {
            outputs.addChemical(stack);
         } else if (data instanceof BoxedChemicalStack stack) {
            outputs.addChemical(stack.getChemicalStack());
         } else if (data instanceof PressurizedReactionRecipe.PressurizedReactionRecipeOutput output) {
            if (!output.item().m_41619_()) {
               outputs.addItem(IItemStack.of(output.item()));
            }

            if (!output.gas().isEmpty()) {
               outputs.addChemical(output.gas());
            }
         } else if (data instanceof ElectrolysisRecipe.ElectrolysisRecipeOutput output) {
            outputs.addChemical(output.left());
            outputs.addChemical(output.right());
         } else if (data instanceof Integer i) {
            if (duration != -1) {
               return Optional.empty();
            }

            duration = i;
         } else {
            if (!(data instanceof FloatingLong fl)) {
               return Optional.empty();
            }

            if (energy != null) {
               return Optional.empty();
            }

            energy = fl;
         }
      }

      DecomposedRecipeBuilder builder = IDecomposedRecipe.builder();
      inputs.addItemToBuilder(builder, CrTRecipeComponents.ITEM.input()).addFluidToBuilder(builder, CrTRecipeComponents.FLUID.input());
      outputs.addItemToBuilder(builder, CrTRecipeComponents.ITEM.output()).addFluidToBuilder(builder, CrTRecipeComponents.FLUID.output());

      for (CrTRecipeComponents.ChemicalRecipeComponent<?, ?, ?, ?> chemicalComponent : CrTRecipeComponents.CHEMICAL_COMPONENTS) {
         this.addChemicals(builder, inputs, outputs, chemicalComponent);
      }

      if (duration != -1) {
         builder.with(Processing.TIME, duration);
      }

      if (energy != null) {
         builder.with(CrTRecipeComponents.ENERGY, energy);
      }

      return Optional.of(builder.build());
   }

   private <STACK extends ChemicalStack<?>, INGREDIENT extends ChemicalStackIngredient<?, STACK>, CRT_STACK extends ICrTChemicalStack<?, STACK, CRT_STACK>> void addChemicals(
      DecomposedRecipeBuilder builder,
      MekanismRecipeHandler.TypeData<?, ?, ChemicalStackIngredient<?, ?>> inputs,
      MekanismRecipeHandler.TypeData<?, ?, ChemicalStack<?>> outputs,
      CrTRecipeComponents.ChemicalRecipeComponent<?, STACK, INGREDIENT, CRT_STACK> component
   ) {
      List<INGREDIENT> data = inputs.chemicalData.getOrDefault(component.chemicalType(), Collections.emptyList());
      if (!data.isEmpty()) {
         builder.with(component.input(), data);
      }

      List<STACK> outputData = outputs.chemicalData.getOrDefault(component.chemicalType(), Collections.emptyList());
      if (!outputData.isEmpty()) {
         component.withOutput(builder, outputData);
      }
   }

   private static class TypeData<ITEM, FLUID, CHEMICAL> {
      private final List<ITEM> itemData = new ArrayList<>();
      private final List<FLUID> fluidData = new ArrayList<>();
      private final Map<ChemicalType, List<CHEMICAL>> chemicalData = new EnumMap<>(ChemicalType.class);
      private final Function<CHEMICAL, ChemicalType> typeExtractor;

      public TypeData(Function<CHEMICAL, ChemicalType> typeExtractor) {
         this.typeExtractor = typeExtractor;
      }

      private void addItem(ITEM data) {
         this.itemData.add(data);
      }

      private void addFluid(FLUID data) {
         this.fluidData.add(data);
      }

      private void addChemical(CHEMICAL data) {
         this.chemicalData.computeIfAbsent(this.typeExtractor.apply(data), type -> new ArrayList<>()).add(data);
      }

      private MekanismRecipeHandler.TypeData<ITEM, FLUID, CHEMICAL> addItemToBuilder(DecomposedRecipeBuilder builder, IRecipeComponent<ITEM> component) {
         if (!this.itemData.isEmpty()) {
            builder.with(component, this.itemData);
         }

         return this;
      }

      private MekanismRecipeHandler.TypeData<ITEM, FLUID, CHEMICAL> addFluidToBuilder(DecomposedRecipeBuilder builder, IRecipeComponent<FLUID> component) {
         if (!this.fluidData.isEmpty()) {
            builder.with(component, this.fluidData);
         }

         return this;
      }
   }
}
