package mekanism.common.advancements.triggers;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChangeRobitSkinTrigger extends SimpleCriterionTrigger<ChangeRobitSkinTrigger.TriggerInstance> {
   private final ResourceLocation id;

   public ChangeRobitSkinTrigger(ResourceLocation id) {
      this.id = id;
   }

   @NotNull
   public ResourceLocation m_7295_() {
      return this.id;
   }

   @NotNull
   protected ChangeRobitSkinTrigger.TriggerInstance createInstance(
      @NotNull JsonObject json, @NotNull ContextAwarePredicate playerPredicate, @NotNull DeserializationContext context
   ) {
      ResourceKey<RobitSkin> skin;
      if (json.has("skin")) {
         String name = GsonHelper.m_13906_(json, "skin");
         ResourceLocation registryName = ResourceLocation.m_135820_(name);
         if (registryName == null) {
            throw new JsonSyntaxException("Expected property 'skin' to be a valid resource location, was: '" + name + "'.");
         }

         skin = ResourceKey.m_135785_(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME, registryName);
      } else {
         skin = null;
      }

      return new ChangeRobitSkinTrigger.TriggerInstance(playerPredicate, skin);
   }

   public void trigger(ServerPlayer player, ResourceKey<RobitSkin> skin) {
      this.m_66234_(player, instance -> instance.skin == null || instance.skin == skin);
   }

   public static class TriggerInstance extends AbstractCriterionTriggerInstance {
      @Nullable
      private final ResourceKey<RobitSkin> skin;

      public TriggerInstance(ContextAwarePredicate playerPredicate, @Nullable ResourceKey<RobitSkin> skin) {
         super(MekanismCriteriaTriggers.CHANGE_ROBIT_SKIN.m_7295_(), playerPredicate);
         this.skin = skin;
      }

      @NotNull
      public JsonObject m_7683_(@NotNull SerializationContext context) {
         JsonObject json = super.m_7683_(context);
         if (this.skin != null) {
            json.addProperty("skin", this.skin.m_135782_().toString());
         }

         return json;
      }

      public static ChangeRobitSkinTrigger.TriggerInstance toAny() {
         return new ChangeRobitSkinTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, null);
      }

      public static ChangeRobitSkinTrigger.TriggerInstance toSkin(ResourceKey<RobitSkin> skin) {
         return new ChangeRobitSkinTrigger.TriggerInstance(ContextAwarePredicate.f_285567_, skin);
      }
   }
}
