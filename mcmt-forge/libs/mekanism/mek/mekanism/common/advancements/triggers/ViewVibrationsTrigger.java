package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ViewVibrationsTrigger extends SimpleCriterionTrigger<ViewVibrationsTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public ViewVibrationsTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected ViewVibrationsTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      return new ViewVibrationsTrigger.TriggerInstance(playerPredicate);
   }

   public void trigger(ServerPlayer player) {
      this.m_66234_(player, ConstantPredicates.alwaysTrue());
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ContextAwarePredicate playerPredicate) {
         super(MekanismCriteriaTriggers.VIEW_VIBRATIONS.m_7295_(), playerPredicate);
      }

      public static ViewVibrationsTrigger.TriggerInstance view() {
         return new ViewVibrationsTrigger.TriggerInstance(ContextAwarePredicate.f_285567_);
      }
   }
}
