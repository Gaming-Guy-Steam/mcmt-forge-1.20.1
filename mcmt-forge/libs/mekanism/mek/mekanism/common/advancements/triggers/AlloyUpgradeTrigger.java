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

public class AlloyUpgradeTrigger extends SimpleCriterionTrigger<AlloyUpgradeTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public AlloyUpgradeTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected AlloyUpgradeTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      return new AlloyUpgradeTrigger.TriggerInstance(playerPredicate);
   }

   public void trigger(ServerPlayer player) {
      this.m_66234_(player, ConstantPredicates.alwaysTrue());
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ContextAwarePredicate playerPredicate) {
         super(MekanismCriteriaTriggers.ALLOY_UPGRADE.m_7295_(), playerPredicate);
      }

      public static AlloyUpgradeTrigger.TriggerInstance upgraded() {
         return new AlloyUpgradeTrigger.TriggerInstance(ContextAwarePredicate.f_285567_);
      }
   }
}
