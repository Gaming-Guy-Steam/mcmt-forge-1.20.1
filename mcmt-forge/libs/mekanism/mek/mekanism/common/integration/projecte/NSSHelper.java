package mekanism.common.integration.projecte;

import com.google.gson.JsonParseException;
import moze_intel.projecte.api.imc.NSSCreatorInfo;
import moze_intel.projecte.api.nss.NSSCreator;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.InterModComms;

public class NSSHelper {
   private static final NSSCreator gasCreator = gasName -> gasName.startsWith("#")
      ? NSSGas.createTag(getResourceLocation(gasName.substring(1), "gas tag"))
      : NSSGas.createGas(getResourceLocation(gasName, "gas"));
   private static final NSSCreator infuseTypeCreator = infuseTypeName -> infuseTypeName.startsWith("#")
      ? NSSInfuseType.createTag(getResourceLocation(infuseTypeName.substring(1), "infuse type tag"))
      : NSSInfuseType.createInfuseType(getResourceLocation(infuseTypeName, "infuse type"));
   private static final NSSCreator pigmentCreator = pigmentName -> pigmentName.startsWith("#")
      ? NSSPigment.createTag(getResourceLocation(pigmentName.substring(1), "pigment tag"))
      : NSSPigment.createPigment(getResourceLocation(pigmentName, "pigment"));
   private static final NSSCreator slurryCreator = slurryName -> slurryName.startsWith("#")
      ? NSSSlurry.createTag(getResourceLocation(slurryName.substring(1), "slurry tag"))
      : NSSSlurry.createSlurry(getResourceLocation(slurryName, "slurry"));

   public static void init() {
      register("GAS", gasCreator);
      register("INFUSE_TYPE", infuseTypeCreator);
      register("PIGMENT", pigmentCreator);
      register("SLURRY", slurryCreator);
   }

   private static void register(String key, NSSCreator creator) {
      InterModComms.sendTo("projecte", "register_nss_serializer", () -> new NSSCreatorInfo(key, creator));
   }

   private static ResourceLocation getResourceLocation(String s, String type) throws JsonParseException {
      try {
         return new ResourceLocation(s);
      } catch (ResourceLocationException var3) {
         throw new JsonParseException("Malformed " + type + " ID", var3);
      }
   }
}
