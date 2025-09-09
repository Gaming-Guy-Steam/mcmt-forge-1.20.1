package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.game.Game;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.Collection;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@TypedExpansion(Game.class)
public class CrTGameExpansion {
   @Method
   @Getter("gases")
   public static Collection<Gas> getGases(Game _this) {
      return MekanismAPI.gasRegistry().getValues();
   }

   @Method
   @Getter("infuseTypes")
   public static Collection<InfuseType> getInfuseTypes(Game _this) {
      return MekanismAPI.infuseTypeRegistry().getValues();
   }

   @Method
   @Getter("pigments")
   public static Collection<Pigment> getPigments(Game _this) {
      return MekanismAPI.pigmentRegistry().getValues();
   }

   @Method
   @Getter("slurries")
   public static Collection<Slurry> getSlurries(Game _this) {
      return MekanismAPI.slurryRegistry().getValues();
   }

   @Method
   @Getter("modules")
   public static Collection<ModuleData<?>> getModules(Game _this) {
      return MekanismAPI.moduleRegistry().getValues();
   }

   @Method
   @Getter("robitSkins")
   public static Collection<RobitSkin> getRobitSkins(Game _this) {
      return CraftTweakerAPI.getAccessibleElementsProvider().registryAccess().m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME).m_123024_().toList();
   }
}
