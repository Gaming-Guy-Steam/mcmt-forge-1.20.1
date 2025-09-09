package mekanism.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class ModelToVoxelShapeUtil {
   public static void main(String[] args) {
      printoutModelFile("/Users/aidancbrady/Documents/Mekanism/src/main/resources/assets/mekanism/models/block/digital_miner.json");
   }

   private static void printoutModelFile(String path) {
      StringBuilder builder = new StringBuilder();

      try (Stream<String> stream = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
         stream.forEach(s -> builder.append(s).append('\n'));
      } catch (IOException var16) {
         var16.printStackTrace();
         return;
      }

      DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
      DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
      JsonObject obj = JsonParser.parseString(builder.toString()).getAsJsonObject();
      if (obj.has("elements")) {
         JsonArray elements = obj.getAsJsonArray("elements");
         printoutObject(elements, df, 0, elements.size());
      } else if (obj.has("children")) {
         JsonObject children = obj.getAsJsonObject("children");
         JsonArray[] childElements = new JsonArray[children.size()];
         int index = 0;

         for (Entry<String, JsonElement> e : children.entrySet()) {
            JsonObject child = e.getValue().getAsJsonObject();
            JsonArray array;
            if (child.has("elements")) {
               array = child.getAsJsonArray("elements");
            } else {
               System.err.println("Unable to parse child: " + e.getKey());
               array = new JsonArray();
            }

            childElements[index++] = array;
         }

         ModelToVoxelShapeUtil.ChildData childData = ModelToVoxelShapeUtil.ChildData.from(childElements);
         int soFar = 0;

         for (JsonArray childElement : childData.childElements) {
            soFar = printoutObject(childElement, df, soFar, childData.totalElements);
         }
      } else {
         System.err.println("Unable to parse model file.");
      }
   }

   private static int printoutObject(JsonArray elementsArray, DecimalFormat df, int soFar, int totalElements) {
      for (JsonElement jsonElement : elementsArray) {
         JsonObject element = jsonElement.getAsJsonObject();
         StringBuilder line = new StringBuilder("box(")
            .append(convertCorner(df, element.getAsJsonArray("from")))
            .append(", ")
            .append(convertCorner(df, element.getAsJsonArray("to")))
            .append(')');
         if (++soFar < totalElements) {
            line.append(',');
         }

         if (element.has("name")) {
            line.append(" // ").append(element.get("name").getAsString());
         }

         System.out.println(line);
      }

      return soFar;
   }

   private static String convertCorner(DecimalFormat df, JsonArray corner) {
      return df.format(corner.get(0).getAsDouble()) + ", " + df.format(corner.get(1).getAsDouble()) + ", " + df.format(corner.get(2).getAsDouble());
   }

   private record ChildData(JsonArray[] childElements, int totalElements) {
      private static ModelToVoxelShapeUtil.ChildData from(JsonArray[] childElements) {
         int elements = 0;

         for (JsonArray childElement : childElements) {
            elements += childElement.size();
         }

         return new ModelToVoxelShapeUtil.ChildData(childElements, elements);
      }
   }
}
