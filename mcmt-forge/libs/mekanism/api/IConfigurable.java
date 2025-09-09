package mekanism.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IConfigurable {
   InteractionResult onSneakRightClick(Player var1);

   InteractionResult onRightClick(Player var1);
}
