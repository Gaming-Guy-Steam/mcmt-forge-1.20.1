package mekanism.api.robit;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface IRobit extends ISecurityObject {
   ResourceKey<RobitSkin> getSkin();

   boolean setSkin(ResourceKey<RobitSkin> var1, @Nullable Player var2);
}
