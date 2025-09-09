package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Arrays;
import java.util.Locale;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public class UseGaugeDropperTrigger extends SimpleCriterionTrigger<UseGaugeDropperTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public UseGaugeDropperTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected UseGaugeDropperTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      String actionName = GsonHelper.m_13906_(json, "action");
      UseGaugeDropperTrigger.UseDropperAction action = Arrays.stream(UseGaugeDropperTrigger.UseDropperAction.ACTIONS)
         .filter(a -> a.m_7912_().equals(actionName))
         .findFirst()
         .orElseThrow(() -> new JsonSyntaxException("Unknown dropper use action: " + actionName));
      return new UseGaugeDropperTrigger.TriggerInstance(playerPredicate, action);
   }

   public void trigger(ServerPlayer player, UseGaugeDropperTrigger.UseDropperAction action) {
      this.m_66234_(player, instance -> instance.action == UseGaugeDropperTrigger.UseDropperAction.ANY || instance.action == action);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final UseGaugeDropperTrigger.UseDropperAction action;

      public TriggerInstance(ContextAwarePredicate playerPredicate, UseGaugeDropperTrigger.UseDropperAction action) {
         super(MekanismCriteriaTriggers.USE_GAUGE_DROPPER.m_7295_(), playerPredicate);
         this.action = action;
      }

      @NotNull
      public JsonObject m_7683_(@NotNull SerializationContext context) {
         JsonObject json = super.m_7683_(context);
         json.addProperty("action", this.action.m_7912_());
         return json;
      }

      public static UseGaugeDropperTrigger.TriggerInstance any() {
         return new UseGaugeDropperTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, UseGaugeDropperTrigger.UseDropperAction.ANY);
      }
   }

   public static enum UseDropperAction implements StringRepresentable {
      ANY,
      FILL,
      DRAIN,
      DUMP;

      private static final UseGaugeDropperTrigger.UseDropperAction[] ACTIONS = values();

      @NotNull
      public String m_7912_() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
