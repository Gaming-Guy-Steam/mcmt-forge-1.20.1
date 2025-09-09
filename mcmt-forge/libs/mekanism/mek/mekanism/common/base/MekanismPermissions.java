package mekanism.common.base;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent.Nodes;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContextKey;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionType;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import net.minecraftforge.server.permission.nodes.PermissionNode.PermissionResolver;
import org.jetbrains.annotations.Nullable;

public class MekanismPermissions {
   private static final List<PermissionNode<?>> NODES_TO_REGISTER = new ArrayList<>();
   private static final PermissionResolver<Boolean> PLAYER_IS_OP = (player, uuid, context) -> player != null && player.m_20310_(2);
   private static final PermissionResolver<Boolean> ALWAYS_TRUE = (player, uuid, context) -> true;
   public static final PermissionNode<Boolean> BYPASS_SECURITY = node(
      "bypass_security", PermissionTypes.BOOLEAN, (player, uuid, context) -> player != null && player.f_8924_.m_6846_().m_11303_(player.m_36316_())
   );
   public static final MekanismPermissions.CommandPermissionNode COMMAND = new MekanismPermissions.CommandPermissionNode(
      node("command", PermissionTypes.BOOLEAN, (player, uuid, contexts) -> player != null && player.m_20310_(0)), 0
   );
   public static final MekanismPermissions.CommandPermissionNode COMMAND_BUILD = nodeOpCommand("build");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_BUILD_REMOVE = nodeSubCommand(COMMAND_BUILD, "remove");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_CHUNK = nodeOpCommand("chunk");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_CHUNK_CLEAR = nodeSubCommand(COMMAND_CHUNK, "clear");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_CHUNK_FLUSH = nodeSubCommand(COMMAND_CHUNK, "flush");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_CHUNK_UNWATCH = nodeSubCommand(COMMAND_CHUNK, "unwatch");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_CHUNK_WATCH = nodeSubCommand(COMMAND_CHUNK, "watch");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_DEBUG = nodeOpCommand("debug");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_FORCE_RETROGEN = nodeOpCommand("force_retrogen");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION = nodeOpCommand("radiation");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_ADD = nodeSubCommand(COMMAND_RADIATION, "add");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY = nodeSubCommand(COMMAND_RADIATION, "add_entity");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY_OTHERS = nodeSubCommand(COMMAND_RADIATION_ADD_ENTITY, "others");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_GET = nodeSubCommand(COMMAND_RADIATION, "get");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_HEAL = nodeSubCommand(COMMAND_RADIATION, "heal");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_HEAL_OTHERS = nodeSubCommand(COMMAND_RADIATION_HEAL, "others");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_REDUCE = nodeSubCommand(COMMAND_RADIATION, "reduce");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_REDUCE_OTHERS = nodeSubCommand(COMMAND_RADIATION_REDUCE, "others");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_RADIATION_REMOVE_ALL = nodeSubCommand(COMMAND_RADIATION, "remove.all");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_TEST_RULES = nodeOpCommand("test_rules");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_TP = nodeOpCommand("tp");
   public static final MekanismPermissions.CommandPermissionNode COMMAND_TP_POP = nodeOpCommand("tp_pop");

   private static MekanismPermissions.CommandPermissionNode nodeOpCommand(String nodeName) {
      PermissionNode<Boolean> node = node("command." + nodeName, PermissionTypes.BOOLEAN, PLAYER_IS_OP);
      return new MekanismPermissions.CommandPermissionNode(node, 2);
   }

   private static MekanismPermissions.CommandPermissionNode nodeSubCommand(MekanismPermissions.CommandPermissionNode parent, String nodeName) {
      PermissionNode<Boolean> node = subNode(parent.node, nodeName, ALWAYS_TRUE);
      return new MekanismPermissions.CommandPermissionNode(node, parent.fallbackLevel);
   }

   private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName) {
      return subNode(parent, nodeName, (player, uuid, context) -> getPermission(player, uuid, parent, context));
   }

   private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName, MekanismPermissions.ResultTransformer<T> defaultRestrictionIncrease) {
      return subNode(parent, nodeName, (player, uuid, context) -> {
         T result = getPermission(player, uuid, parent, context);
         return defaultRestrictionIncrease.transform(player, uuid, result, context);
      });
   }

   private static <T> PermissionNode<T> subNode(PermissionNode<T> parent, String nodeName, PermissionResolver<T> defaultResolver) {
      String fullParentName = parent.getNodeName();
      String parentName = fullParentName.substring(fullParentName.indexOf(46) + 1);
      return node(parentName + "." + nodeName, parent.getType(), defaultResolver);
   }

   @SafeVarargs
   private static <T> PermissionNode<T> node(
      String nodeName, PermissionType<T> type, PermissionResolver<T> defaultResolver, PermissionDynamicContextKey<T>... dynamics
   ) {
      PermissionNode<T> node = new PermissionNode("mekanism", nodeName, type, defaultResolver, dynamics);
      NODES_TO_REGISTER.add(node);
      return node;
   }

   public static void registerPermissionNodes(Nodes event) {
      event.addNodes(NODES_TO_REGISTER);
   }

   private static <T> T getPermission(@Nullable ServerPlayer player, UUID playerUUID, PermissionNode<T> node, PermissionDynamicContext<?>... context) {
      return (T)(player == null ? PermissionAPI.getOfflinePermission(playerUUID, node, context) : PermissionAPI.getPermission(player, node, context));
   }

   public record CommandPermissionNode(PermissionNode<Boolean> node, int fallbackLevel) implements Predicate<CommandSourceStack> {
      public boolean test(CommandSourceStack source) {
         return source.m_6761_(this.fallbackLevel)
            || source.f_81288_ instanceof ServerPlayer player && (Boolean)PermissionAPI.getPermission(player, this.node, new PermissionDynamicContext[0]);
      }
   }

   @FunctionalInterface
   private interface ResultTransformer<T> {
      T transform(@Nullable ServerPlayer player, UUID playerUUID, T resolved, PermissionDynamicContext<?>... context);
   }
}
