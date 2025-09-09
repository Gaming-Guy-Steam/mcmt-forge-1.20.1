package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.CTFluidIngredient;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.ingredient.type.IIngredientList;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTFluidStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTGasStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTInfusionStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTItemStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTPigmentStackIngredient;
import mekanism.common.integration.crafttweaker.ingredient.CrTSlurryStackIngredient;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Expansion;

public class CrTIngredientExpansion {
   private CrTIngredientExpansion() {
   }

   @ZenRegister
   @TypedExpansion(CTFluidIngredient.class)
   public static class CTFluidIngredientExpansion {
      private CTFluidIngredientExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FluidStackIngredient asFluidStackIngredient(CTFluidIngredient _this) {
         return CrTFluidStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<crafttweaker.api.fluid.Fluid>>")
   public static class FluidTagWithAmountExpansion {
      private FluidTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FluidStackIngredient asFluidStackIngredient(Many<KnownTag<Fluid>> _this) {
         return CrTFluidStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Gas>>")
   public static class GasTagWithAmountExpansion {
      private GasTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ChemicalStackIngredient.GasStackIngredient asGasStackIngredient(Many<KnownTag<Gas>> _this) {
         return CrTGasStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IFluidStack.class)
   public static class IFluidStackExpansion {
      private IFluidStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static FluidStackIngredient asFluidStackIngredient(IFluidStack _this) {
         return CrTFluidStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IIngredient.class)
   public static class IIngredientExpansion {
      private IIngredientExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(IIngredient _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IIngredientWithAmount.class)
   public static class IIngredientWithAmountExpansion {
      private IIngredientWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(IIngredientWithAmount _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IItemStack.class)
   public static class IItemStackExpansion {
      private IItemStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(IItemStack _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.InfuseType>>")
   public static class InfuseTypeTagWithAmountExpansion {
      private InfuseTypeTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ChemicalStackIngredient.InfusionStackIngredient asGasStackIngredient(Many<KnownTag<InfuseType>> _this) {
         return CrTInfusionStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(IIngredientList.class)
   public static class IngredientListExpansion {
      private IngredientListExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(IIngredientList _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @TypedExpansion(Item.class)
   public static class ItemExpansion {
      private ItemExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(Item _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.tag.type.KnownTag<crafttweaker.api.item.ItemDefinition>")
   public static class ItemTagExpansion {
      private ItemTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(KnownTag<Item> _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<crafttweaker.api.item.ItemDefinition>>")
   public static class ItemTagWithAmountExpansion {
      private ItemTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ItemStackIngredient asItemStackIngredient(Many<KnownTag<Item>> _this) {
         return CrTItemStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Pigment>>")
   public static class PigmentTagWithAmountExpansion {
      private PigmentTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ChemicalStackIngredient.PigmentStackIngredient asGasStackIngredient(Many<KnownTag<Pigment>> _this) {
         return CrTPigmentStackIngredient.from(_this);
      }
   }

   @ZenRegister
   @Expansion("crafttweaker.api.util.Many<crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Slurry>>")
   public static class SlurryTagWithAmountExpansion {
      private SlurryTagWithAmountExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static ChemicalStackIngredient.SlurryStackIngredient asGasStackIngredient(Many<KnownTag<Slurry>> _this) {
         return CrTSlurryStackIngredient.from(_this);
      }
   }
}
