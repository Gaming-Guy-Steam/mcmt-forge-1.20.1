package mekanism.common.integration.crafttweaker.projecte;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import org.openzen.zencode.java.ZenCodeType.Caster;
import org.openzen.zencode.java.ZenCodeType.Expansion;

public class CrTNSSCastingExpansion {
   private CrTNSSCastingExpansion() {
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @Expansion("crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Gas>")
   public static class GasTagExpansion {
      private GasTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Gas> _this) {
         return CrTNSSResolverExpansion.fromGasTag(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(Gas.class)
   public static class ICrTGasExpansion {
      private ICrTGasExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(Gas _this) {
         return CrTNSSResolverExpansion.fromGas(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTGasStack.class)
   public static class ICrTGasStackExpansion {
      private ICrTGasStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTChemicalStack.ICrTGasStack _this) {
         return CrTNSSResolverExpansion.fromGas(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(InfuseType.class)
   public static class ICrTInfuseTypeExpansion {
      private ICrTInfuseTypeExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(InfuseType _this) {
         return CrTNSSResolverExpansion.fromInfuseType(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTInfusionStack.class)
   public static class ICrTInfusionStackExpansion {
      private ICrTInfusionStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTChemicalStack.ICrTInfusionStack _this) {
         return CrTNSSResolverExpansion.fromInfuseType(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(Pigment.class)
   public static class ICrTPigmentExpansion {
      private ICrTPigmentExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(Pigment _this) {
         return CrTNSSResolverExpansion.fromPigment(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTPigmentStack.class)
   public static class ICrTPigmentStackExpansion {
      private ICrTPigmentStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTChemicalStack.ICrTPigmentStack _this) {
         return CrTNSSResolverExpansion.fromPigment(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(Slurry.class)
   public static class ICrTSlurryExpansion {
      private ICrTSlurryExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(Slurry _this) {
         return CrTNSSResolverExpansion.fromSlurry(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @TypedExpansion(ICrTChemicalStack.ICrTSlurryStack.class)
   public static class ICrTSlurryStackExpansion {
      private ICrTSlurryStackExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(ICrTChemicalStack.ICrTSlurryStack _this) {
         return CrTNSSResolverExpansion.fromSlurry(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @Expansion("crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.InfuseType>")
   public static class InfuseTypeTagExpansion {
      private InfuseTypeTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<InfuseType> _this) {
         return CrTNSSResolverExpansion.fromInfuseTypeTag(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @Expansion("crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Pigment>")
   public static class PigmentTagExpansion {
      private PigmentTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Pigment> _this) {
         return CrTNSSResolverExpansion.fromPigmentTag(_this);
      }
   }

   @ZenRegister(
      modDeps = {"projecte"}
   )
   @Expansion("crafttweaker.api.tag.type.KnownTag<mods.mekanism.api.chemical.Slurry>")
   public static class SlurryTagExpansion {
      private SlurryTagExpansion() {
      }

      @Caster(
         implicit = true
      )
      public static NormalizedSimpleStack asNormalizedSimpleStack(KnownTag<Slurry> _this) {
         return CrTNSSResolverExpansion.fromSlurryTag(_this);
      }
   }
}
