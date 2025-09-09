package mekanism.client.render.obj;

import java.util.Collections;
import java.util.Objects;
import mekanism.client.model.data.TransmitterModelData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransmitterModelConfiguration extends VisibleModelConfiguration {
   @NotNull
   private final TransmitterModelConfiguration.IconStatus iconStatus;

   public TransmitterModelConfiguration(IGeometryBakingContext internal, String piece, @NotNull TransmitterModelConfiguration.IconStatus iconStatus) {
      super(internal, Collections.singletonList(piece));
      this.iconStatus = Objects.requireNonNull(iconStatus, "Icon status must be present.");
   }

   @Nullable
   private static Direction directionForPiece(@NotNull String piece) {
      if (piece.endsWith("down")) {
         return Direction.DOWN;
      } else if (piece.endsWith("up")) {
         return Direction.UP;
      } else if (piece.endsWith("north")) {
         return Direction.NORTH;
      } else if (piece.endsWith("south")) {
         return Direction.SOUTH;
      } else if (piece.endsWith("east")) {
         return Direction.EAST;
      } else {
         return piece.endsWith("west") ? Direction.WEST : null;
      }
   }

   private String adjustTextureName(String name) {
      Direction direction = directionForPiece(name);
      if (direction != null) {
         if (this.iconStatus != TransmitterModelConfiguration.IconStatus.NO_SHOW) {
            name = name.contains("glass") ? "#side_glass" : "#side";
         }

         if (MekanismConfig.client.opaqueTransmitters.get()) {
            if (name.startsWith("#side")) {
               return name + "_opaque";
            }

            if (name.startsWith("#center")) {
               return name.contains("glass") ? "#center_glass_opaque" : "#center_opaque";
            }
         }

         return name;
      } else {
         return MekanismConfig.client.opaqueTransmitters.get() && name.startsWith("#side") ? name + "_opaque" : name;
      }
   }

   public static TransmitterModelConfiguration.IconStatus getIconStatus(TransmitterModelData modelData, Direction side, ConnectionType connectionType) {
      if (!(modelData instanceof TransmitterModelData.Diversion) && connectionType == ConnectionType.NONE) {
         return switch (side) {
            case DOWN, UP -> getStatus(modelData, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            case NORTH, SOUTH -> getStatus(modelData, Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST);
            case WEST, EAST -> getStatus(modelData, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH);
            default -> throw new IncompatibleClassChangeError();
         };
      } else {
         return TransmitterModelConfiguration.IconStatus.NO_SHOW;
      }
   }

   private static TransmitterModelConfiguration.IconStatus getStatus(TransmitterModelData modelData, Direction a, Direction b, Direction c, Direction d) {
      boolean hasA = modelData.getConnectionType(a) != ConnectionType.NONE;
      boolean hasB = modelData.getConnectionType(b) != ConnectionType.NONE;
      boolean hasC = modelData.getConnectionType(c) != ConnectionType.NONE;
      boolean hasD = modelData.getConnectionType(d) != ConnectionType.NONE;
      if ((hasA || hasB) != (hasC || hasD)) {
         if (hasA && hasB) {
            return TransmitterModelConfiguration.IconStatus.NO_ROTATION;
         }

         if (hasC && hasD) {
            return TransmitterModelConfiguration.IconStatus.ROTATE_270;
         }
      }

      return TransmitterModelConfiguration.IconStatus.NO_SHOW;
   }

   @Override
   public boolean hasMaterial(@NotNull String name) {
      return this.internal.hasMaterial(this.adjustTextureName(name));
   }

   @NotNull
   @Override
   public Material getMaterial(@NotNull String name) {
      return this.internal.getMaterial(this.adjustTextureName(name));
   }

   public static enum IconStatus {
      NO_ROTATION(0.0F),
      ROTATE_270(270.0F),
      NO_SHOW(0.0F);

      private final float angle;

      private IconStatus(float angle) {
         this.angle = angle * (float) (Math.PI / 180.0);
      }

      public float getAngle() {
         return this.angle;
      }
   }
}
