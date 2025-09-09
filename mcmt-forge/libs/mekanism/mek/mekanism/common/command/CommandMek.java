package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.command.builders.BuildCommand;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.phys.Vec3;

public class CommandMek {
   private static final Map<UUID, Deque<BlockPos>> tpStack = new Object2ObjectOpenHashMap();

   private CommandMek() {
   }

   public static LiteralArgumentBuilder<CommandSourceStack> register() {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_(
                                    "mek"
                                 )
                                 .requires(MekanismPermissions.COMMAND))
                              .then(BuildCommand.COMMAND))
                           .then(ChunkCommand.register()))
                        .then(CommandMek.DebugCommand.register()))
                     .then(ForceRetrogenCommand.register()))
                  .then(RadiationCommand.register()))
               .then(CommandMek.TestRulesCommand.register()))
            .then(CommandMek.TpCommand.register()))
         .then(CommandMek.TppopCommand.register());
   }

   private static class DebugCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("debug").requires(MekanismPermissions.COMMAND_DEBUG))
            .executes(
               ctx -> {
                  MekanismAPI.debug = !MekanismAPI.debug;
                  ((CommandSourceStack)ctx.getSource())
                     .m_288197_(
                        () -> MekanismLang.COMMAND_DEBUG.translateColored(EnumColor.GRAY, new Object[]{BooleanStateDisplay.OnOff.of(MekanismAPI.debug, true)}),
                        true
                     );
                  return 0;
               }
            );
      }
   }

   private static class TestRulesCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("testrules").requires(MekanismPermissions.COMMAND_TEST_RULES)).executes(ctx -> {
            CommandSourceStack source = (CommandSourceStack)ctx.getSource();
            MinecraftServer server = source.m_81377_();
            GameRules rules = server.m_129900_();
            ((BooleanValue)rules.m_46170_(GameRules.f_46133_)).m_46246_(true, server);
            ((BooleanValue)rules.m_46170_(GameRules.f_46134_)).m_46246_(false, server);
            ((BooleanValue)rules.m_46170_(GameRules.f_46140_)).m_46246_(false, server);
            ((BooleanValue)rules.m_46170_(GameRules.f_46150_)).m_46246_(false, server);
            ((BooleanValue)rules.m_46170_(GameRules.f_46132_)).m_46246_(false, server);
            source.m_81372_().m_8615_(2000L);
            source.m_288197_(() -> MekanismLang.COMMAND_TEST_RULES.translateColored(EnumColor.GRAY, new Object[0]), true);
            return 0;
         });
      }
   }

   private static class TpCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("tp").requires(MekanismPermissions.COMMAND_TP.and(cs -> cs.m_81373_() instanceof ServerPlayer)))
            .then(Commands.m_82129_("location", Vec3Argument.m_120841_()).executes(ctx -> {
               CommandSourceStack source = (CommandSourceStack)ctx.getSource();
               ServerPlayer player = source.m_81375_();
               UUID uuid = player.m_20148_();
               Deque<BlockPos> playerLocations = CommandMek.tpStack.computeIfAbsent(uuid, u -> new ArrayDeque<>());
               playerLocations.push(player.m_20183_());
               Coordinates location = Vec3Argument.m_120849_(ctx, "location");
               Vec3 position = location.m_6955_(source);
               player.f_8906_.m_9774_(position.m_7096_(), position.m_7098_(), position.m_7094_(), player.m_146908_(), player.m_146909_());
               source.m_288197_(() -> MekanismLang.COMMAND_TP.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, getPosition(position)}), true);
               return 0;
            }));
      }

      private static Component getPosition(Vec3 pos) {
         return MekanismLang.GENERIC_BLOCK_POS.translate(new Object[]{pos.m_7096_(), pos.m_7098_(), pos.m_7094_()});
      }
   }

   private static class TppopCommand {
      private static final SimpleCommandExceptionType TPOP_EMPTY = new SimpleCommandExceptionType(
         MekanismLang.COMMAND_ERROR_TPOP_EMPTY.translate(new Object[0])
      );

      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("tpop")
               .requires(MekanismPermissions.COMMAND_TP_POP.and(cs -> cs.m_81373_() instanceof ServerPlayer)))
            .executes(
               ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  ServerPlayer player = source.m_81375_();
                  UUID uuid = player.m_20148_();
                  Deque<BlockPos> playerLocations = CommandMek.tpStack.computeIfAbsent(uuid, u -> new ArrayDeque<>());
                  if (playerLocations.isEmpty()) {
                     throw TPOP_EMPTY.create();
                  } else {
                     BlockPos lastPos = playerLocations.pop();
                     player.f_8906_.m_9774_(lastPos.m_123341_(), lastPos.m_123342_(), lastPos.m_123343_(), player.m_146908_(), player.m_146909_());
                     source.m_288197_(
                        () -> MekanismLang.COMMAND_TPOP
                           .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, getPosition(lastPos), EnumColor.INDIGO, playerLocations.size()}),
                        true
                     );
                     return 0;
                  }
               }
            );
      }

      private static Component getPosition(BlockPos pos) {
         return MekanismLang.GENERIC_BLOCK_POS.translate(new Object[]{pos.m_123341_(), pos.m_123342_(), pos.m_123343_()});
      }
   }
}
