package mekanism.common.inventory.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedIntValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectedWindowData {
   public static final SelectedWindowData UNSPECIFIED = new SelectedWindowData(SelectedWindowData.WindowType.UNSPECIFIED);
   @NotNull
   public final SelectedWindowData.WindowType type;
   public final byte extraData;

   public SelectedWindowData(@NotNull SelectedWindowData.WindowType type) {
      this(type, (byte)0);
   }

   public SelectedWindowData(@NotNull SelectedWindowData.WindowType type, byte extraData) {
      this.type = Objects.requireNonNull(type);
      this.extraData = this.type.isValid(extraData) ? extraData : 0;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SelectedWindowData other = (SelectedWindowData)o;
         return this.extraData == other.extraData && this.type == other.type;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.type, this.extraData);
   }

   public void updateLastPosition(int x, int y) {
      String saveName = this.type.getSaveName(this.extraData);
      if (saveName != null) {
         SelectedWindowData.CachedWindowPosition cachedPosition = MekanismConfig.client.lastWindowPositions.get(saveName);
         if (cachedPosition != null) {
            boolean changed = false;
            CachedIntValue cachedX = cachedPosition.x();
            if (cachedX.get() != x) {
               cachedX.set(x);
               changed = true;
            }

            CachedIntValue cachedY = cachedPosition.y();
            if (cachedY.get() != y) {
               cachedY.set(y);
               changed = true;
            }

            if (changed) {
               MekanismConfig.client.save();
            }
         }
      }
   }

   public SelectedWindowData.WindowPosition getLastPosition() {
      String saveName = this.type.getSaveName(this.extraData);
      if (saveName != null) {
         SelectedWindowData.CachedWindowPosition cachedPosition = MekanismConfig.client.lastWindowPositions.get(saveName);
         if (cachedPosition != null) {
            return new SelectedWindowData.WindowPosition(cachedPosition.x().get(), cachedPosition.y().get());
         }
      }

      return new SelectedWindowData.WindowPosition(Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

   public record CachedWindowPosition(CachedIntValue x, CachedIntValue y) {
   }

   public record WindowPosition(int x, int y) {
   }

   public static enum WindowType {
      COLOR("color"),
      CONFIRMATION("confirmation"),
      CRAFTING("crafting", (byte)3),
      MEKA_SUIT_HELMET("mekaSuitHelmet"),
      RENAME("rename"),
      SKIN_SELECT("skinSelect"),
      SIDE_CONFIG("sideConfig"),
      TRANSPORTER_CONFIG("transporterConfig"),
      UPGRADE("upgrade"),
      UNSPECIFIED(null);

      @Nullable
      private final String saveName;
      private final byte maxData;

      private WindowType(@Nullable String saveName) {
         this(saveName, (byte)1);
      }

      private WindowType(@Nullable String saveName, byte maxData) {
         this.saveName = saveName;
         this.maxData = maxData;
      }

      @Nullable
      String getSaveName(byte extraData) {
         return this.maxData == 1 ? this.saveName : this.saveName + extraData;
      }

      public List<String> getSavePaths() {
         if (this.saveName == null) {
            return Collections.emptyList();
         } else if (this.maxData == 1) {
            return Collections.singletonList(this.saveName);
         } else {
            List<String> savePaths = new ArrayList<>();

            for (int i = 0; i < this.maxData; i++) {
               savePaths.add(this.saveName + i);
            }

            return savePaths;
         }
      }

      public boolean isValid(byte extraData) {
         return extraData >= 0 && extraData < this.maxData;
      }
   }
}
