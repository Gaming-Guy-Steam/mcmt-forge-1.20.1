package mekanism.api.security;

import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public interface ISecurityUtils {
   ISecurityUtils INSTANCE = ServiceLoader.load(ISecurityUtils.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for ISecurityUtils found"));

   @Contract("_, null -> true")
   boolean canAccess(Player var1, @Nullable ICapabilityProvider var2);

   boolean canAccessObject(Player var1, ISecurityObject var2);

   @Contract("_, null, _ -> true")
   boolean canAccess(@Nullable UUID var1, @Nullable ICapabilityProvider var2, boolean var3);

   boolean canAccessObject(@Nullable UUID var1, ISecurityObject var2, boolean var3);

   boolean moreRestrictive(SecurityMode var1, SecurityMode var2);

   @Nullable
   UUID getOwnerUUID(ICapabilityProvider var1);

   SecurityMode getSecurityMode(@Nullable ICapabilityProvider var1, boolean var2);

   SecurityMode getEffectiveSecurityMode(ISecurityObject var1, boolean var2);

   @Contract("_, null -> true")
   default boolean canAccessOrDisplayError(Player player, @Nullable ICapabilityProvider provider) {
      if (this.canAccess(player, provider)) {
         return true;
      } else {
         if (!player.m_9236_().f_46443_) {
            this.displayNoAccess(player);
         }

         return false;
      }
   }

   void displayNoAccess(Player var1);

   void addSecurityTooltip(ItemStack var1, List<Component> var2);
}
