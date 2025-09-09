package mekanism.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidSlurryToSlurryRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.chemical.ChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IItemStackIngredientCreator;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.SmeltingIRecipe;
import mekanism.common.recipe.lookup.cache.ChemicalCrystallizerInputRecipeCache;
import mekanism.common.recipe.lookup.cache.IInputRecipeCache;
import mekanism.common.recipe.lookup.cache.InputRecipeCache;
import mekanism.common.recipe.lookup.cache.RotaryInputRecipeCache;
import mekanism.common.registration.impl.RecipeTypeDeferredRegister;
import mekanism.common.registration.impl.RecipeTypeRegistryObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(
   modid = "mekanism"
)
public class MekanismRecipeType<RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache>
   implements RecipeType<RECIPE>,
   IMekanismRecipeTypeProvider<RECIPE, INPUT_CACHE> {
   public static final RecipeTypeDeferredRegister RECIPE_TYPES = new RecipeTypeDeferredRegister("mekanism");
   public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> CRUSHING = register(
      "crushing", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> ENRICHING = register(
      "enriching", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToItemStackRecipe, InputRecipeCache.SingleItem<ItemStackToItemStackRecipe>> SMELTING = register(
      "smelting", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToItemStackRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ChemicalInfuserRecipe, InputRecipeCache.EitherSideChemical<Gas, GasStack, ChemicalInfuserRecipe>> CHEMICAL_INFUSING = register(
      "chemical_infusing", InputRecipeCache.EitherSideChemical::new
   );
   public static final RecipeTypeRegistryObject<CombinerRecipe, InputRecipeCache.DoubleItem<CombinerRecipe>> COMBINING = register(
      "combining", recipeType -> new InputRecipeCache.DoubleItem<>(recipeType, CombinerRecipe::getMainInput, CombinerRecipe::getExtraInput)
   );
   public static final RecipeTypeRegistryObject<ElectrolysisRecipe, InputRecipeCache.SingleFluid<ElectrolysisRecipe>> SEPARATING = register(
      "separating", recipeType -> new InputRecipeCache.SingleFluid<>(recipeType, ElectrolysisRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<FluidSlurryToSlurryRecipe, InputRecipeCache.FluidChemical<Slurry, SlurryStack, FluidSlurryToSlurryRecipe>> WASHING = register(
      "washing",
      recipeType -> new InputRecipeCache.FluidChemical<>(
         recipeType, FluidChemicalToChemicalRecipe::getFluidInput, FluidChemicalToChemicalRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<FluidToFluidRecipe, InputRecipeCache.SingleFluid<FluidToFluidRecipe>> EVAPORATING = register(
      "evaporating", recipeType -> new InputRecipeCache.SingleFluid<>(recipeType, FluidToFluidRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<GasToGasRecipe, InputRecipeCache.SingleChemical<Gas, GasStack, GasToGasRecipe>> ACTIVATING = register(
      "activating", recipeType -> new InputRecipeCache.SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<GasToGasRecipe, InputRecipeCache.SingleChemical<Gas, GasStack, GasToGasRecipe>> CENTRIFUGING = register(
      "centrifuging", recipeType -> new InputRecipeCache.SingleChemical<>(recipeType, ChemicalToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ChemicalCrystallizerRecipe, ChemicalCrystallizerInputRecipeCache> CRYSTALLIZING = register(
      "crystallizing", ChemicalCrystallizerInputRecipeCache::new
   );
   public static final RecipeTypeRegistryObject<ChemicalDissolutionRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ChemicalDissolutionRecipe>> DISSOLUTION = register(
      "dissolution",
      recipeType -> new InputRecipeCache.ItemChemical<>(recipeType, ChemicalDissolutionRecipe::getItemInput, ChemicalDissolutionRecipe::getGasInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> COMPRESSING = register(
      "compressing",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> PURIFYING = register(
      "purifying",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<ItemStackGasToItemStackRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, ItemStackGasToItemStackRecipe>> INJECTING = register(
      "injecting",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<NucleosynthesizingRecipe, InputRecipeCache.ItemChemical<Gas, GasStack, NucleosynthesizingRecipe>> NUCLEOSYNTHESIZING = register(
      "nucleosynthesizing",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<ItemStackToEnergyRecipe, InputRecipeCache.SingleItem<ItemStackToEnergyRecipe>> ENERGY_CONVERSION = register(
      "energy_conversion", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToEnergyRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, InputRecipeCache.SingleItem<ItemStackToGasRecipe>> GAS_CONVERSION = register(
      "gas_conversion", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToGasRecipe, InputRecipeCache.SingleItem<ItemStackToGasRecipe>> OXIDIZING = register(
      "oxidizing", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToInfuseTypeRecipe, InputRecipeCache.SingleItem<ItemStackToInfuseTypeRecipe>> INFUSION_CONVERSION = register(
      "infusion_conversion", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<ItemStackToPigmentRecipe, InputRecipeCache.SingleItem<ItemStackToPigmentRecipe>> PIGMENT_EXTRACTING = register(
      "pigment_extracting", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, ItemStackToChemicalRecipe::getInput)
   );
   public static final RecipeTypeRegistryObject<PigmentMixingRecipe, InputRecipeCache.EitherSideChemical<Pigment, PigmentStack, PigmentMixingRecipe>> PIGMENT_MIXING = register(
      "pigment_mixing", InputRecipeCache.EitherSideChemical::new
   );
   public static final RecipeTypeRegistryObject<MetallurgicInfuserRecipe, InputRecipeCache.ItemChemical<InfuseType, InfusionStack, MetallurgicInfuserRecipe>> METALLURGIC_INFUSING = register(
      "metallurgic_infusing",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<PaintingRecipe, InputRecipeCache.ItemChemical<Pigment, PigmentStack, PaintingRecipe>> PAINTING = register(
      "painting",
      recipeType -> new InputRecipeCache.ItemChemical<>(
         recipeType, ItemStackChemicalToItemStackRecipe::getItemInput, ItemStackChemicalToItemStackRecipe::getChemicalInput
      )
   );
   public static final RecipeTypeRegistryObject<PressurizedReactionRecipe, InputRecipeCache.ItemFluidChemical<Gas, GasStack, PressurizedReactionRecipe>> REACTION = register(
      "reaction",
      recipeType -> new InputRecipeCache.ItemFluidChemical<>(
         (MekanismRecipeType<RECIPE, ?>)recipeType,
         PressurizedReactionRecipe::getInputSolid,
         PressurizedReactionRecipe::getInputFluid,
         PressurizedReactionRecipe::getInputGas
      )
   );
   public static final RecipeTypeRegistryObject<RotaryRecipe, RotaryInputRecipeCache> ROTARY = register("rotary", RotaryInputRecipeCache::new);
   public static final RecipeTypeRegistryObject<SawmillRecipe, InputRecipeCache.SingleItem<SawmillRecipe>> SAWING = register(
      "sawing", recipeType -> new InputRecipeCache.SingleItem<>(recipeType, SawmillRecipe::getInput)
   );
   private List<RECIPE> cachedRecipes = Collections.emptyList();
   private final ResourceLocation registryName;
   private final INPUT_CACHE inputCache;

   private static <RECIPE extends MekanismRecipe, INPUT_CACHE extends IInputRecipeCache> RecipeTypeRegistryObject<RECIPE, INPUT_CACHE> register(
      String name, Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator
   ) {
      return RECIPE_TYPES.register(name, () -> new MekanismRecipeType<>(name, inputCacheCreator));
   }

   public static void clearCache() {
      for (IMekanismRecipeTypeProvider<?, ?> recipeTypeProvider : RECIPE_TYPES.getAllRecipeTypes()) {
         recipeTypeProvider.getRecipeType().clearCaches();
      }
   }

   public static boolean checkIncompleteRecipes(MinecraftServer server) {
      return checkIncompleteRecipes(server.m_129894_(), server.m_206579_());
   }

   public static boolean checkIncompleteRecipes(RecipeManager recipeManager, RegistryAccess registryAccess) {
      boolean foundIncompleteRecipes = false;

      for (IMekanismRecipeTypeProvider<?, ?> recipeTypeProvider : RECIPE_TYPES.getAllRecipeTypes()) {
         MekanismRecipeType<?, ?> recipeType = recipeTypeProvider.getRecipeType();
         foundIncompleteRecipes |= recipeType.checkMyIncompleteRecipes(recipeManager, registryAccess);
      }

      return foundIncompleteRecipes;
   }

   private MekanismRecipeType(String name, Function<MekanismRecipeType<RECIPE, INPUT_CACHE>, INPUT_CACHE> inputCacheCreator) {
      this.registryName = Mekanism.rl(name);
      this.inputCache = inputCacheCreator.apply(this);
   }

   @Override
   public String toString() {
      return this.registryName.toString();
   }

   @Override
   public ResourceLocation getRegistryName() {
      return this.registryName;
   }

   @Override
   public MekanismRecipeType<RECIPE, INPUT_CACHE> getRecipeType() {
      return this;
   }

   private void clearCaches() {
      this.cachedRecipes = Collections.emptyList();
      this.inputCache.clear();
   }

   @Override
   public INPUT_CACHE getInputCache() {
      return this.inputCache;
   }

   @NotNull
   @Override
   public List<RECIPE> getRecipes(@Nullable Level world) {
      if (world == null) {
         if (FMLEnvironment.dist.isClient()) {
            world = MekanismClient.tryGetClientWorld();
         } else {
            world = ServerLifecycleHooks.getCurrentServer().m_129783_();
         }

         if (world == null) {
            return Collections.emptyList();
         }
      }

      if (this.cachedRecipes.isEmpty()) {
         List<RECIPE> recipes = this.getRecipesUncached(world.m_7465_(), world.m_9598_());
         this.cachedRecipes = recipes.stream().filter(recipe -> !recipe.m_142505_()).toList();
      }

      return this.cachedRecipes;
   }

   @NotNull
   private List<RECIPE> getRecipesUncached(RecipeManager recipeManager, RegistryAccess registryAccess) {
      List<RECIPE> recipes = recipeManager.m_44013_(this);
      if (this == SMELTING.get()) {
         recipes = new ArrayList<>(recipes);

         for (SmeltingRecipe smeltingRecipe : recipeManager.m_44013_(RecipeType.f_44108_)) {
            ItemStack recipeOutput = smeltingRecipe.m_8043_(registryAccess);
            if (!smeltingRecipe.m_5598_() && !smeltingRecipe.m_142505_() && !recipeOutput.m_41619_()) {
               NonNullList<Ingredient> ingredients = smeltingRecipe.m_7527_();
               if (!ingredients.isEmpty()) {
                  IItemStackIngredientCreator ingredientCreator = IngredientCreatorAccess.item();
                  ItemStackIngredient input = ingredientCreator.from(ingredients.stream().map(ingredientCreator::from));
                  recipes.add((RECIPE)(new SmeltingIRecipe(smeltingRecipe.m_6423_(), input, recipeOutput)));
               }
            }
         }
      }

      return recipes;
   }

   private boolean checkMyIncompleteRecipes(RecipeManager recipeManager, RegistryAccess registryAccess) {
      boolean incomplete = false;

      for (MekanismRecipe recipe : this.getRecipesUncached(recipeManager, registryAccess)) {
         if (recipe.m_142505_()) {
            Mekanism.logger.error("Incomplete recipe detected: {}", recipe.m_6423_());
            incomplete = true;
            recipe.logMissingTags();
         }
      }

      return incomplete;
   }

   public static <C extends Container, RECIPE_TYPE extends Recipe<C>> Optional<RECIPE_TYPE> getRecipeFor(
      RecipeType<RECIPE_TYPE> recipeType, C inventory, Level level
   ) {
      return level.m_7465_().m_44015_(recipeType, inventory, level).filter(recipe -> recipe.m_5598_() || !recipe.m_142505_());
   }

   public static Optional<? extends Recipe<?>> byKey(Level level, ResourceLocation id) {
      return level.m_7465_().m_44043_(id).filter(recipe -> recipe.m_5598_() || !recipe.m_142505_());
   }
}
