package mekanism.common.inventory.warning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class WarningTracker implements IWarningTracker {
   private final Map<WarningTracker.WarningType, List<BooleanSupplier>> warnings = new EnumMap<>(WarningTracker.WarningType.class);

   @Override
   public BooleanSupplier trackWarning(@NotNull WarningTracker.WarningType type, @NotNull BooleanSupplier warningSupplier) {
      this.warnings
         .computeIfAbsent(Objects.requireNonNull(type, "Warning type cannot be null."), t -> new ArrayList<>(type.expectedWarnings))
         .add(Objects.requireNonNull(warningSupplier, "Warning check cannot be null."));
      return warningSupplier;
   }

   @Override
   public void clearTrackedWarnings() {
      this.warnings.clear();
   }

   @Override
   public boolean hasWarning() {
      for (List<BooleanSupplier> warningSuppliers : this.warnings.values()) {
         for (BooleanSupplier warningSupplier : warningSuppliers) {
            if (warningSupplier.getAsBoolean()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public List<Component> getWarnings() {
      List<Component> warningMessages = new ArrayList<>();

      for (Entry<WarningTracker.WarningType, List<BooleanSupplier>> entry : this.warnings.entrySet()) {
         for (BooleanSupplier warningSupplier : entry.getValue()) {
            if (warningSupplier.getAsBoolean()) {
               WarningTracker.WarningType warningType = entry.getKey();
               warningMessages.add(warningType.langEntry.translate());
               if (warningType == WarningTracker.WarningType.NOT_ENOUGH_ENERGY) {
                  return warningMessages;
               }
               break;
            }
         }
      }

      return warningMessages;
   }

   public static enum WarningType {
      INPUT_DOESNT_PRODUCE_OUTPUT(MekanismLang.ISSUE_INPUT_DOESNT_PRODUCE_OUTPUT),
      NO_MATCHING_RECIPE(MekanismLang.ISSUE_NO_MATCHING_RECIPE),
      NO_SPACE_IN_OUTPUT(MekanismLang.ISSUE_NO_SPACE_IN_OUTPUT),
      NO_SPACE_IN_OUTPUT_OVERFLOW(MekanismLang.ISSUE_NO_SPACE_IN_OUTPUT_OVERFLOW),
      NOT_ENOUGH_ENERGY(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY),
      NOT_ENOUGH_ENERGY_REDUCED_RATE(MekanismLang.ISSUE_NOT_ENOUGH_ENERGY_REDUCED_RATE),
      INVALID_OREDICTIONIFICATOR_FILTER(MekanismLang.ISSUE_INVALID_OREDICTIONIFICATOR_FILTER, 4),
      FILTER_HAS_BLACKLISTED_ELEMENT(MekanismLang.ISSUE_FILTER_HAS_BLACKLISTED_ELEMENT, 5);

      private final ILangEntry langEntry;
      private final int expectedWarnings;

      private WarningType(ILangEntry langEntry) {
         this(langEntry, 1);
      }

      private WarningType(ILangEntry langEntry, int expectedWarnings) {
         this.langEntry = langEntry;
         this.expectedWarnings = expectedWarnings;
      }
   }
}
