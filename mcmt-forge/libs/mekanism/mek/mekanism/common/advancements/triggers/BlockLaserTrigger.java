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

public class BlockLaserTrigger extends SimpleCriterionTrigger<BlockLaserTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public BlockLaserTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected BlockLaserTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      return new BlockLaserTrigger.TriggerInstance(playerPredicate);
   }

   public void trigger(ServerPlayer player) {
      this.m_66234_(player, ConstantPredicates.alwaysTrue());
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      public TriggerInstance(ContextAwarePredicate playerPredicate) {
         super(MekanismCriteriaTriggers.BLOCK_LASER.m_7295_(), playerPredicate);
      }

      public static BlockLaserTrigger.TriggerInstance block() {
         return new BlockLaserTrigger.TriggerInstance(ContextAwarePredicate.f_285567_);
      }
   }
}
