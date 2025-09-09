package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.registries.MekanismDamageTypes;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

public class MekanismDamageTrigger extends SimpleCriterionTrigger<MekanismDamageTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public MekanismDamageTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected MekanismDamageTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      String damage = GsonHelper.m_13906_(json, "damage");
      MekanismDamageTypes.MekanismDamageType damageType = MekanismDamageTypes.DAMAGE_TYPES.get(damage);
      if (damageType == null) {
         throw new JsonSyntaxException("Expected damage to represent a Mekanism damage type.");
      } else {
         return new MekanismDamageTrigger.TriggerInstance(playerPredicate, damageType, GsonHelper.m_13912_(json, "killed"));
      }
   }

   public void trigger(ServerPlayer player, MekanismDamageTypes.MekanismDamageType damageType, boolean hardcoreTotem) {
      this.m_66234_(player, instance -> instance.killed && !player.m_21224_() && !hardcoreTotem ? false : instance.damageType.key() == damageType.key());
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      private final MekanismDamageTypes.MekanismDamageType damageType;
      private final boolean killed;

      public TriggerInstance(ContextAwarePredicate playerPredicate, MekanismDamageTypes.MekanismDamageType damageType, boolean killed) {
         super(MekanismCriteriaTriggers.DAMAGE.m_7295_(), playerPredicate);
         this.damageType = damageType;
         this.killed = killed;
      }

      @NotNull
      public JsonObject m_7683_(@NotNull SerializationContext context) {
         JsonObject json = super.m_7683_(context);
         json.addProperty("damage", this.damageType.registryName().toString());
         json.addProperty("killed", this.killed);
         return json;
      }

      public static MekanismDamageTrigger.TriggerInstance damaged(MekanismDamageTypes.MekanismDamageType damageType) {
         return new MekanismDamageTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, damageType, false);
      }

      public static MekanismDamageTrigger.TriggerInstance killed(MekanismDamageTypes.MekanismDamageType damageType) {
         return new MekanismDamageTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, damageType, true);
      }
   }
}
