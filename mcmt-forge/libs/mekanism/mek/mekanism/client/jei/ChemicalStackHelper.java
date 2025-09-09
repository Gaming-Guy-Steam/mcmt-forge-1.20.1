package mekanism.client.jei;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IEmptyStackProvider;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IEmptyGasProvider;
import mekanism.api.chemical.infuse.IEmptyInfusionProvider;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IEmptyPigmentProvider;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.IEmptySlurryProvider;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.MekanismClient;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.recipe.IMekanismRecipeTypeProvider;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.util.ChemicalUtil;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ChemicalStackHelper<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
   implements IIngredientHelper<STACK>,
   IEmptyStackProvider<CHEMICAL, STACK> {
   @Nullable
   private IColorHelper colorHelper;

   void setColorHelper(IColorHelper colorHelper) {
      this.colorHelper = colorHelper;
   }

   protected abstract String getType();

   public String getDisplayName(STACK ingredient) {
      return TextComponentUtil.build(ingredient).getString();
   }

   public String getUniqueId(STACK ingredient, UidContext context) {
      return this.getType().toLowerCase(Locale.ROOT) + ":" + ingredient.getTypeRegistryName();
   }

   public ResourceLocation getResourceLocation(STACK ingredient) {
      return ingredient.getTypeRegistryName();
   }

   public ItemStack getCheatItemStack(STACK ingredient) {
      ItemStack stack = MekanismBlocks.CREATIVE_CHEMICAL_TANK.getItemStack();
      return ChemicalUtil.getFilledVariant(stack, ChemicalTankTier.CREATIVE.getStorage(), ingredient.getType());
   }

   public STACK normalizeIngredient(STACK ingredient) {
      return ChemicalUtil.copyWithAmount(ingredient, 1000L);
   }

   public boolean isValidIngredient(STACK ingredient) {
      return !ingredient.isEmpty();
   }

   public Iterable<Integer> getColors(STACK ingredient) {
      if (this.colorHelper == null) {
         return super.getColors(ingredient);
      } else {
         CHEMICAL chemical = ingredient.getType();
         return this.colorHelper.getColors(MekanismRenderer.getChemicalTexture(chemical), chemical.getTint(), 1);
      }
   }

   public STACK copyIngredient(STACK ingredient) {
      return ChemicalUtil.copy(ingredient);
   }

   public Stream<ResourceLocation> getTagStream(STACK ingredient) {
      return ingredient.getType().getTags().map(TagKey::f_203868_);
   }

   protected abstract IForgeRegistry<CHEMICAL> getRegistry();

   public Optional<ResourceLocation> getTagEquivalent(Collection<STACK> stacks) {
      if (stacks.size() < 2) {
         return Optional.empty();
      } else {
         ITagManager<CHEMICAL> tags = this.getRegistry().tags();
         if (tags == null) {
            return Optional.empty();
         } else {
            Set<CHEMICAL> values = stacks.stream().map(ChemicalStack::getType).collect(Collectors.toSet());
            int expected = values.size();
            return expected != stacks.size()
               ? Optional.empty()
               : tags.stream()
                  .filter(tag -> tag.size() == expected && values.stream().allMatch(tag::contains))
                  .map(tag -> tag.getKey().f_203868_())
                  .findFirst();
         }
      }
   }

   public String getErrorInfo(@Nullable STACK ingredient) {
      if (ingredient == null) {
         ingredient = this.getEmptyStack();
      }

      ToStringHelper toStringHelper = MoreObjects.toStringHelper(GasStack.class);
      CHEMICAL chemical = ingredient.getType();
      toStringHelper.add(this.getType(), chemical.isEmptyType() ? "none" : TextComponentUtil.build(chemical).getString());
      if (!ingredient.isEmpty()) {
         toStringHelper.add("Amount", ingredient.getAmount());
      }

      return toStringHelper.toString();
   }

   @Nullable
   protected IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<CHEMICAL, STACK>, ?> getConversionRecipeType() {
      return null;
   }

   public List<ItemStack> getStacksFor(@NotNull CHEMICAL type, boolean displayConversions) {
      if (type.isEmptyType()) {
         return Collections.emptyList();
      } else {
         Level world = MekanismClient.tryGetClientWorld();
         if (world == null) {
            return Collections.emptyList();
         } else {
            List<ItemStack> stacks = new ArrayList<>();
            stacks.add(ChemicalUtil.getFullChemicalTank(ChemicalTankTier.BASIC, type));
            if (displayConversions) {
               IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<CHEMICAL, STACK>, ?> recipeType = this.getConversionRecipeType();
               if (recipeType != null) {
                  for (ItemStackToChemicalRecipe<CHEMICAL, STACK> recipe : recipeType.getRecipes(world)) {
                     if (recipe.getOutputDefinition().stream().anyMatch(output -> output.isTypeEqual(type))) {
                        stacks.addAll(recipe.getInput().getRepresentations());
                     }
                  }
               }
            }

            return stacks;
         }
      }
   }

   public static class GasStackHelper extends ChemicalStackHelper<Gas, GasStack> implements IEmptyGasProvider {
      @Override
      protected String getType() {
         return "Gas";
      }

      @Override
      protected IForgeRegistry<Gas> getRegistry() {
         return MekanismAPI.gasRegistry();
      }

      @Override
      protected IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<Gas, GasStack>, ?> getConversionRecipeType() {
         return MekanismRecipeType.GAS_CONVERSION;
      }

      public IIngredientType<GasStack> getIngredientType() {
         return MekanismJEI.TYPE_GAS;
      }
   }

   public static class InfusionStackHelper extends ChemicalStackHelper<InfuseType, InfusionStack> implements IEmptyInfusionProvider {
      @Override
      protected String getType() {
         return "Infuse Type";
      }

      @Override
      protected IForgeRegistry<InfuseType> getRegistry() {
         return MekanismAPI.infuseTypeRegistry();
      }

      @Override
      protected IMekanismRecipeTypeProvider<? extends ItemStackToChemicalRecipe<InfuseType, InfusionStack>, ?> getConversionRecipeType() {
         return MekanismRecipeType.INFUSION_CONVERSION;
      }

      public IIngredientType<InfusionStack> getIngredientType() {
         return MekanismJEI.TYPE_INFUSION;
      }
   }

   public static class PigmentStackHelper extends ChemicalStackHelper<Pigment, PigmentStack> implements IEmptyPigmentProvider {
      @Override
      protected IForgeRegistry<Pigment> getRegistry() {
         return MekanismAPI.pigmentRegistry();
      }

      @Override
      protected String getType() {
         return "Pigment";
      }

      public IIngredientType<PigmentStack> getIngredientType() {
         return MekanismJEI.TYPE_PIGMENT;
      }
   }

   public static class SlurryStackHelper extends ChemicalStackHelper<Slurry, SlurryStack> implements IEmptySlurryProvider {
      @Override
      protected IForgeRegistry<Slurry> getRegistry() {
         return MekanismAPI.slurryRegistry();
      }

      @Override
      protected String getType() {
         return "Slurry";
      }

      public IIngredientType<SlurryStack> getIngredientType() {
         return MekanismJEI.TYPE_SLURRY;
      }
   }
}
