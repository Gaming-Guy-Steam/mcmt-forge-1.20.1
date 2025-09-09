package mekanism.common.util;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.api.text.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.lib.security.SecurityFrequency;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class SecurityUtils implements ISecurityUtils {
   public static SecurityUtils get() {
      return (SecurityUtils)INSTANCE;
   }

   private boolean isOp(Player p) {
      Objects.requireNonNull(p, "Player may not be null.");
      return MekanismConfig.general.opsBypassRestrictions.get()
         && p instanceof ServerPlayer player
         && (Boolean)PermissionAPI.getPermission(player, MekanismPermissions.BYPASS_SECURITY, new PermissionDynamicContext[0]);
   }

   @Nullable
   @Override
   public UUID getOwnerUUID(ICapabilityProvider provider) {
      Objects.requireNonNull(provider, "Capability provider may not be null.");
      return provider.getCapability(Capabilities.OWNER_OBJECT).resolve().map(IOwnerObject::getOwnerUUID).orElse(null);
   }

   @Override
   public boolean canAccess(Player player, @Nullable ICapabilityProvider provider) {
      return this.isOp(player) || this.canAccess(player.m_20148_(), provider, player.m_9236_().f_46443_);
   }

   @Override
   public boolean canAccessObject(Player player, ISecurityObject security) {
      return this.isOp(player) || this.canAccessObject(player.m_20148_(), security, player.m_9236_().f_46443_);
   }

   @Override
   public boolean canAccess(@Nullable UUID player, @Nullable ICapabilityProvider provider, boolean isClient) {
      if (MekanismConfig.general.allowProtection.get() && provider != null) {
         Optional<ISecurityObject> securityCapability = provider.getCapability(Capabilities.SECURITY_OBJECT).resolve();
         if (securityCapability.isEmpty()) {
            Optional<IOwnerObject> ownerCapability = provider.getCapability(Capabilities.OWNER_OBJECT).resolve();
            if (!ownerCapability.isPresent()) {
               return true;
            } else {
               UUID owner = ownerCapability.get().getOwnerUUID();
               return owner == null || owner.equals(player);
            }
         } else {
            return this.canAccessObject(player, securityCapability.get(), isClient);
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean canAccessObject(@Nullable UUID player, @NotNull ISecurityObject security, boolean isClient) {
      Objects.requireNonNull(security, "Security object may not be null.");
      if (!MekanismConfig.general.allowProtection.get()) {
         return true;
      } else {
         UUID owner = security.getOwnerUUID();
         if (owner != null && !owner.equals(player)) {
            return switch (this.getEffectiveSecurityMode(security, isClient)) {
               case PUBLIC -> true;
               case PRIVATE -> false;
               case TRUSTED -> {
                  if (player == null) {
                     yield false;
                  } else if (isClient) {
                     yield true;
                  } else {
                     SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null).getFrequency(owner);
                     yield frequency != null && frequency.getTrustedUUIDs().contains(player);
                  }
               }
            };
         } else {
            return true;
         }
      }
   }

   @Override
   public boolean moreRestrictive(SecurityMode base, SecurityMode overridden) {
      Objects.requireNonNull(base, "Base security mode may not be null.");
      Objects.requireNonNull(base, "Override security mode may not be null.");

      return switch (overridden) {
         case PUBLIC -> false;
         case PRIVATE -> base != SecurityMode.PRIVATE;
         case TRUSTED -> base == SecurityMode.PUBLIC;
      };
   }

   public SecurityData getFinalData(ISecurityObject securityObject, boolean isClient) {
      if (!MekanismConfig.general.allowProtection.get()) {
         return SecurityData.DUMMY;
      } else {
         SecurityData data = this.getData(securityObject.getOwnerUUID(), isClient);
         SecurityMode mode = securityObject.getSecurityMode();
         return data.override() && this.moreRestrictive(mode, data.mode()) ? data : new SecurityData(mode, false);
      }
   }

   private SecurityData getData(@Nullable UUID uuid, boolean isClient) {
      if (uuid == null) {
         return SecurityData.DUMMY;
      } else if (isClient) {
         return MekanismClient.clientSecurityMap.getOrDefault(uuid, SecurityData.DUMMY);
      } else {
         SecurityFrequency frequency = FrequencyType.SECURITY.getManager(null).getFrequency(uuid);
         return frequency == null ? SecurityData.DUMMY : new SecurityData(frequency);
      }
   }

   @Override
   public SecurityMode getSecurityMode(@Nullable ICapabilityProvider provider, boolean isClient) {
      return provider != null && MekanismConfig.general.allowProtection.get()
         ? provider.getCapability(Capabilities.SECURITY_OBJECT)
            .map(security -> this.getEffectiveSecurityMode(security, isClient))
            .orElseGet(() -> provider.getCapability(Capabilities.OWNER_OBJECT).isPresent() ? SecurityMode.PRIVATE : SecurityMode.PUBLIC)
         : SecurityMode.PUBLIC;
   }

   @Override
   public SecurityMode getEffectiveSecurityMode(ISecurityObject securityObject, boolean isClient) {
      Objects.requireNonNull(securityObject, "Security object may not be null.");
      return this.getFinalData(securityObject, isClient).mode();
   }

   public void incrementSecurityMode(Player player, ICapabilityProvider provider) {
      provider.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
         if (security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getNext());
         }
      });
   }

   public void decrementSecurityMode(Player player, ICapabilityProvider provider) {
      provider.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
         if (security.ownerMatches(player)) {
            security.setSecurityMode(security.getSecurityMode().getPrevious());
         }
      });
   }

   public InteractionResultHolder<ItemStack> claimOrOpenGui(
      Level level, Player player, InteractionHand hand, TriConsumer<ServerPlayer, InteractionHand, ItemStack> openGui
   ) {
      ItemStack stack = player.m_21120_(hand);
      if (!this.tryClaimItem(level, player, stack)) {
         if (!this.canAccessOrDisplayError(player, stack)) {
            return InteractionResultHolder.m_19100_(stack);
         }

         if (!level.f_46443_) {
            openGui.accept((ServerPlayer)player, hand, stack);
         }
      }

      return InteractionResultHolder.m_19092_(stack, level.f_46443_);
   }

   public boolean tryClaimItem(Level level, Player player, ItemStack stack) {
      Optional<IOwnerObject> capability = stack.getCapability(Capabilities.OWNER_OBJECT).resolve();
      if (capability.isPresent()) {
         IOwnerObject ownerObject = capability.get();
         if (ownerObject.getOwnerUUID() == null) {
            if (!level.f_46443_) {
               ownerObject.setOwnerUUID(player.m_20148_());
               Mekanism.packetHandler().sendToAll(new PacketSecurityUpdate(player.m_20148_()));
               player.m_213846_(MekanismUtils.logFormat(MekanismLang.NOW_OWN));
            }

            return true;
         }
      }

      return false;
   }

   @Override
   public void displayNoAccess(Player player) {
      Objects.requireNonNull(player, "Player may not be null.");
      player.m_213846_(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.NO_ACCESS));
   }

   public void addOwnerTooltip(ItemStack stack, List<Component> tooltip) {
      stack.getCapability(Capabilities.OWNER_OBJECT)
         .ifPresent(ownerObject -> tooltip.add(OwnerDisplay.of(MekanismUtils.tryGetClientPlayer(), ownerObject.getOwnerUUID()).getTextComponent()));
   }

   @Override
   public void addSecurityTooltip(ItemStack stack, List<Component> tooltip) {
      Objects.requireNonNull(stack, "Stack to add tooltip for may not be null.");
      Objects.requireNonNull(tooltip, "List of tooltips to add to may not be null.");
      this.addOwnerTooltip(stack, tooltip);
      stack.getCapability(Capabilities.SECURITY_OBJECT).ifPresent(security -> {
         SecurityData data = this.getFinalData(security, true);
         tooltip.add(MekanismLang.SECURITY.translateColored(EnumColor.GRAY, new Object[]{data.mode()}));
         if (data.override()) {
            tooltip.add(MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED, new Object[0]));
         }
      });
   }

   public void securityChanged(Set<Player> playersUsing, ICapabilityProvider target, SecurityMode old, SecurityMode mode) {
      if (this.moreRestrictive(old, mode) && !playersUsing.isEmpty()) {
         ObjectIterator var5 = new ObjectOpenHashSet(playersUsing).iterator();

         while (var5.hasNext()) {
            Player player = (Player)var5.next();
            if (!this.canAccess(player, target)) {
               player.m_6915_();
            }
         }
      }
   }
}
