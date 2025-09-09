package mekanism.common.recipe.condition;

import com.google.gson.JsonObject;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.ComparableVersion;

public class ModVersionLoadedCondition implements ICondition {
   private static final ResourceLocation NAME = Mekanism.rl("mod_version_loaded");
   private final String minVersion;
   private final String modid;

   public ModVersionLoadedCondition(String modid, String version) {
      this.modid = modid;
      this.minVersion = version;
   }

   public ResourceLocation getID() {
      return NAME;
   }

   public boolean test(IContext context) {
      return ModList.get()
         .getModContainerById(this.modid)
         .filter(
            modContainer -> new ComparableVersion(this.minVersion).compareTo(new ComparableVersion(modContainer.getModInfo().getVersion().toString())) <= 0
         )
         .isPresent();
   }

   @Override
   public String toString() {
      return "mod_version_loaded(\"" + this.modid + "\", \"" + this.minVersion + "\")";
   }

   public static class Serializer implements IConditionSerializer<ModVersionLoadedCondition> {
      public static final ModVersionLoadedCondition.Serializer INSTANCE = new ModVersionLoadedCondition.Serializer();

      private Serializer() {
      }

      public void write(JsonObject json, ModVersionLoadedCondition value) {
         json.addProperty("modid", value.modid);
         json.addProperty("minVersion", value.minVersion);
      }

      public ModVersionLoadedCondition read(JsonObject json) {
         return new ModVersionLoadedCondition(GsonHelper.m_13906_(json, "modid"), GsonHelper.m_13906_(json, "minVersion"));
      }

      public ResourceLocation getID() {
         return ModVersionLoadedCondition.NAME;
      }
   }
}
