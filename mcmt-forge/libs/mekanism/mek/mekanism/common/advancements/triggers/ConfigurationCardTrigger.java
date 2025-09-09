package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class ConfigurationCardTrigger extends SimpleCriterionTrigger<ConfigurationCardTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public ConfigurationCardTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected ConfigurationCardTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      return new ConfigurationCardTrigger.TriggerInstance(playerPredicate, GsonHelper.m_13912_(json, "copy"));
   }

   public void trigger(ServerPlayer player, boolean copy) {
      this.m_66234_(player, instance -> instance.copy == copy);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final boolean copy;

      public TriggerInstance(ContextAwarePredicate playerPredicate, boolean copy) {
         super(MekanismCriteriaTriggers.CONFIGURATION_CARD.m_7295_(), playerPredicate);
         this.copy = copy;
      }

      @NotNull
      public JsonObject m_7683_(@NotNull SerializationContext context) {
         JsonObject json = super.m_7683_(context);
         json.addProperty("copy", this.copy);
         return json;
      }

      public static ConfigurationCardTrigger.TriggerInstance copy() {
         return new ConfigurationCardTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, true);
      }

      public static ConfigurationCardTrigger.TriggerInstance paste() {
         return new ConfigurationCardTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, false);
      }
   }
}
