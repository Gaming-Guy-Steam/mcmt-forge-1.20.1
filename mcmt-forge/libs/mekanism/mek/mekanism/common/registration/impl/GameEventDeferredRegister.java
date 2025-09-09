package mekanism.common.registration.impl;

import java.util.function.Supplier;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventDeferredRegister extends WrappedDeferredRegister<GameEvent> {
   private final String modid;

   public GameEventDeferredRegister(String modid) {
      super(modid, Registries.f_256827_);
      this.modid = modid;
   }

   public GameEventRegistryObject<GameEvent> register(String name) {
      return this.register(name, 16);
   }

   public GameEventRegistryObject<GameEvent> register(String name, int notificationRadius) {
      return this.register(name, () -> new GameEvent(this.modid + ":" + name, notificationRadius));
   }

   public <GAME_EVENT extends GameEvent> GameEventRegistryObject<GAME_EVENT> register(String name, Supplier<? extends GAME_EVENT> sup) {
      return this.register(name, sup, GameEventRegistryObject::new);
   }
}
