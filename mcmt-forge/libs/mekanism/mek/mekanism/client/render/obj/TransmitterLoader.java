package mekanism.client.render.obj;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.NotNull;

public class TransmitterLoader implements IGeometryLoader<TransmitterModel> {
   public static final TransmitterLoader INSTANCE = new TransmitterLoader();

   private TransmitterLoader() {
   }

   @NotNull
   public TransmitterModel read(@NotNull JsonObject modelContents, @NotNull JsonDeserializationContext deserializationContext) throws JsonParseException {
      ObjModel model = ObjLoader.INSTANCE.read(modelContents, deserializationContext);
      ObjModel glass = null;
      if (modelContents.has("glass")) {
         glass = ObjLoader.INSTANCE.read(modelContents.getAsJsonObject("glass"), deserializationContext);
      }

      return new TransmitterModel(model, glass);
   }
}
