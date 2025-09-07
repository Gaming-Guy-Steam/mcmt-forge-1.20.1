package dev.mcmt.core.tick;

import dev.mcmt.core.comms.MessageBus;
import dev.mcmt.core.effects.SideEffectBuffer;

/**
 * A tick loop unit that can produce side-effects and use the message bus.
 */
public interface TickLoop {
    String id();
    void tick(SideEffectBuffer buffer, MessageBus bus);
}
