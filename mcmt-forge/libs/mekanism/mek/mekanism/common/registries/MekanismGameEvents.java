package mekanism.common.registries;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mekanism.common.registration.impl.GameEventDeferredRegister;
import mekanism.common.registration.impl.GameEventRegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

public class MekanismGameEvents {
   public static final GameEventDeferredRegister GAME_EVENTS = new GameEventDeferredRegister("mekanism");
   public static final GameEventRegistryObject<GameEvent> SEISMIC_VIBRATION = GAME_EVENTS.register("seismic_vibration", 64);
   public static final GameEventRegistryObject<GameEvent> JETPACK_BURN = GAME_EVENTS.register("jetpack_burn");
   public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE = GAME_EVENTS.register("gravity_modulate");
   public static final GameEventRegistryObject<GameEvent> GRAVITY_MODULATE_BOOSTED = GAME_EVENTS.register("gravity_modulate_boosted", 32);

   private MekanismGameEvents() {
   }

   public static void addFrequencies() {
      if (VibrationSystem.f_279561_ instanceof Object2IntOpenHashMap<GameEvent> frequencyForEvent) {
         frequencyForEvent.put(JETPACK_BURN.get(), 4);
         frequencyForEvent.put(GRAVITY_MODULATE.get(), 4);
         frequencyForEvent.put(GRAVITY_MODULATE_BOOSTED.get(), 5);
         frequencyForEvent.put(SEISMIC_VIBRATION.get(), 10);
      }
   }
}
