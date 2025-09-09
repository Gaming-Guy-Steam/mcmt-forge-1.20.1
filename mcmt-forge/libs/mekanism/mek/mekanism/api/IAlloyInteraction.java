package mekanism.api;

import mekanism.api.tier.AlloyTier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import org.jetbrains.annotations.NotNull;

@AutoRegisterCapability
public interface IAlloyInteraction {
   void onAlloyInteraction(Player var1, ItemStack var2, @NotNull AlloyTier var3);
}
