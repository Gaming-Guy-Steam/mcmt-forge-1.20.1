package mekanism.client;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class IncompleteRecipeScanner {
   private static final Component RECIPE_WARNING = MekanismLang.LOG_FORMAT
      .translateColored(EnumColor.RED, new Object[]{MekanismLang.MEKANISM, MekanismLang.RECIPE_WARNING.translate(new Object[0])});
   private static boolean foundIncompleteRecipes = false;

   @SubscribeEvent
   public static void recipes(OnDatapackSyncEvent event) {
      ServerPlayer player = event.getPlayer();
      if (player != null) {
         if (foundIncompleteRecipes) {
            sendMessageToPlayer(player);
         }
      } else {
         foundIncompleteRecipes = MekanismRecipeType.checkIncompleteRecipes(event.getPlayerList().m_7873_());
         if (foundIncompleteRecipes) {
            List<ServerPlayer> players = event.getPlayerList().m_11314_();
            if (!players.isEmpty()) {
               players.forEach(IncompleteRecipeScanner::sendMessageToPlayer);
            }
         }
      }
   }

   private static void sendMessageToPlayer(ServerPlayer player) {
      player.m_213846_(RECIPE_WARNING);
   }

   @SubscribeEvent
   public static void serverStarted(ServerStartedEvent event) {
      foundIncompleteRecipes = MekanismRecipeType.checkIncompleteRecipes(event.getServer());
   }
}
