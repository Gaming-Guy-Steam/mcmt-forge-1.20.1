package mekanism.common.util.text;

import java.util.UUID;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.MekanismClient;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OwnerDisplay implements IHasTextComponent {
   @Nullable
   private final Player player;
   @Nullable
   private final UUID ownerUUID;
   @Nullable
   private final String ownerName;
   private final boolean colorBase;

   private OwnerDisplay(@Nullable Player player, @Nullable UUID ownerUUID, @Nullable String ownerName, boolean colorBase) {
      this.player = player;
      this.ownerUUID = ownerUUID;
      this.ownerName = ownerName;
      this.colorBase = colorBase;
   }

   public static OwnerDisplay of(UUID ownerUUID) {
      return of(null, ownerUUID);
   }

   public static OwnerDisplay of(Player player, UUID ownerUUID) {
      return of(player, ownerUUID, null);
   }

   public static OwnerDisplay of(UUID ownerUUID, String ownerName) {
      return of(null, ownerUUID, ownerName);
   }

   public static OwnerDisplay of(Player player, UUID ownerUUID, String ownerName) {
      return of(player, ownerUUID, ownerName, true);
   }

   public static OwnerDisplay of(Player player, UUID ownerUUID, String ownerName, boolean colorBase) {
      return new OwnerDisplay(player, ownerUUID, ownerName, colorBase);
   }

   @NotNull
   @Override
   public Component getTextComponent() {
      if (this.ownerUUID == null) {
         return MekanismLang.NO_OWNER.translateColored(EnumColor.RED, new Object[0]);
      } else {
         String name = getOwnerName(this.player, this.ownerUUID, this.ownerName);
         Component component;
         if (this.player == null) {
            component = MekanismLang.OWNER.translate(new Object[]{name});
         } else {
            component = MekanismLang.OWNER
               .translate(new Object[]{this.player.m_20148_().equals(this.ownerUUID) ? EnumColor.BRIGHT_GREEN : EnumColor.RED, name});
         }

         return (Component)(this.colorBase ? TextComponentUtil.build(EnumColor.DARK_GRAY, component) : component);
      }
   }

   @Nullable
   public static String getOwnerName(@Nullable Player player, @NotNull UUID ownerUUID, @Nullable String ownerName) {
      if (ownerName != null) {
         return ownerName;
      } else if ((player == null || player.m_9236_().f_46443_) && (player != null || !EffectiveSide.get().isServer())) {
         String name = MekanismClient.clientUUIDMap.get(ownerUUID);
         if (name == null && player != null) {
            if (player.m_20148_().equals(ownerUUID)) {
               name = player.m_36316_().getName();
               MekanismClient.clientUUIDMap.put(ownerUUID, name);
            } else {
               Player owner = player.m_9236_().m_46003_(ownerUUID);
               if (owner == null) {
                  name = "<" + ownerUUID + ">";
               } else {
                  name = owner.m_36316_().getName();
                  MekanismClient.clientUUIDMap.put(ownerUUID, name);
               }
            }
         }

         return name;
      } else {
         return MekanismUtils.getLastKnownUsername(ownerUUID);
      }
   }
}
