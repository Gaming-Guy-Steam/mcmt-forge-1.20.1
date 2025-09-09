package mekanism.api.datagen.recipe;

import java.util.Objects;
import net.minecraft.advancements.CriterionTriggerInstance;
import org.jetbrains.annotations.NotNull;

public record RecipeCriterion(@NotNull String name, @NotNull CriterionTriggerInstance criterion) {
   public RecipeCriterion(@NotNull String name, @NotNull CriterionTriggerInstance criterion) {
      Objects.requireNonNull(name, "Criterion must have a name.");
      Objects.requireNonNull(criterion, "Recipe criterion's must have a criterion to match.");
      this.name = name;
      this.criterion = criterion;
   }
}
