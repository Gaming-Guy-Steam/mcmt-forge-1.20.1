package mekanism.common.integration.crafttweaker;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.mod.Mod;
import com.blamejared.crafttweaker_annotations.annotations.TypedExpansion;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.gear.ModuleData;
import mekanism.api.robit.RobitSkin;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;
import org.openzen.zencode.java.ZenCodeType.Getter;
import org.openzen.zencode.java.ZenCodeType.Method;

@ZenRegister
@TypedExpansion(Mod.class)
public class CrTModExpansion {
   @Method
   @Getter("gases")
   public static Collection<Gas> getGases(Mod _this) {
      return getModSpecific(_this, MekanismAPI.gasRegistry());
   }

   @Method
   @Getter("infuseTypes")
   public static Collection<InfuseType> getInfuseTypes(Mod _this) {
      return getModSpecific(_this, MekanismAPI.infuseTypeRegistry());
   }

   @Method
   @Getter("pigments")
   public static Collection<Pigment> getPigments(Mod _this) {
      return getModSpecific(_this, MekanismAPI.pigmentRegistry());
   }

   @Method
   @Getter("slurries")
   public static Collection<Slurry> getSlurries(Mod _this) {
      return getModSpecific(_this, MekanismAPI.slurryRegistry());
   }

   @Method
   @Getter("modules")
   public static Collection<ModuleData<?>> getModules(Mod _this) {
      return getModSpecific(_this, MekanismAPI.moduleRegistry());
   }

   @Method
   @Getter("robitSkins")
   public static Collection<RobitSkin> getRobitSkins(Mod _this) {
      return getModSpecific(_this, CraftTweakerAPI.getAccessibleElementsProvider().registryAccess().m_175515_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME).m_6579_());
   }

   private static <TYPE> Collection<TYPE> getModSpecific(Mod mod, IForgeRegistry<TYPE> registry) {
      return getModSpecific(mod, registry.getEntries());
   }

   private static <TYPE> Collection<TYPE> getModSpecific(Mod mod, Set<Entry<ResourceKey<TYPE>, TYPE>> allElements) {
      String modid = mod.id();
      return allElements.stream().filter(entry -> entry.getKey().m_135782_().m_135827_().equals(modid)).map(Entry::getValue).toList();
   }
}
