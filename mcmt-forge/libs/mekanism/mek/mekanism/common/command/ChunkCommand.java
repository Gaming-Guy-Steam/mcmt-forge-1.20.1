package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ChunkEvent.Load;
import net.minecraftforge.event.level.ChunkEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkCommand {
   private static final LongSet chunkWatchers = new LongOpenHashSet();

   private ChunkCommand() {
   }

   static ArgumentBuilder<CommandSourceStack, ?> register() {
      MinecraftForge.EVENT_BUS.register(ChunkCommand.class);
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("chunk")
                     .requires(MekanismPermissions.COMMAND_CHUNK))
                  .then(ChunkCommand.WatchCommand.register()))
               .then(ChunkCommand.UnwatchCommand.register()))
            .then(ChunkCommand.ClearCommand.register()))
         .then(ChunkCommand.FlushCommand.register());
   }

   @SubscribeEvent
   public static void onChunkLoad(Load event) {
      handleChunkEvent(event, MekanismLang.COMMAND_CHUNK_LOADED);
   }

   @SubscribeEvent
   public static void onChunkUnload(Unload event) {
      handleChunkEvent(event, MekanismLang.COMMAND_CHUNK_UNLOADED);
   }

   private static void handleChunkEvent(ChunkEvent event, ILangEntry direction) {
      if (event.getLevel() != null && !event.getLevel().m_5776_()) {
         ChunkPos pos = event.getChunk().m_7697_();
         if (chunkWatchers.contains(pos.m_45588_())) {
            Component message = direction.translateColored(EnumColor.GRAY, EnumColor.INDIGO, getPosition(pos));
            event.getLevel().m_6907_().forEach(player -> player.m_213846_(message));
         }
      }
   }

   private static Component getPosition(ChunkPos pos) {
      return MekanismLang.GENERIC_WITH_COMMA.translate(new Object[]{pos.f_45578_, pos.f_45579_});
   }

   private static class ClearCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("clear").requires(MekanismPermissions.COMMAND_CHUNK_CLEAR))
            .executes(
               ctx -> {
                  int count = ChunkCommand.chunkWatchers.size();
                  ChunkCommand.chunkWatchers.clear();
                  ((CommandSourceStack)ctx.getSource())
                     .m_288197_(() -> MekanismLang.COMMAND_CHUNK_CLEAR.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, count}), true);
                  return 0;
               }
            );
      }
   }

   private static class FlushCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)Commands.m_82127_("flush").requires(MekanismPermissions.COMMAND_CHUNK_FLUSH))
            .executes(
               ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  ServerChunkCache sp = source.m_81372_().m_7726_();
                  int startCount = sp.m_8482_();
                  sp.m_201698_(() -> false, false);
                  source.m_288197_(
                     () -> MekanismLang.COMMAND_CHUNK_FLUSH.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, startCount - sp.m_8482_()}), true
                  );
                  return 0;
               }
            );
      }
   }

   private static class UnwatchCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("unwatch").requires(MekanismPermissions.COMMAND_CHUNK_UNWATCH))
               .executes(ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  return unwatch(source, new ChunkPos(BlockPos.m_274446_(source.m_81371_())));
               }))
            .then(Commands.m_82129_("pos", ColumnPosArgument.m_118989_()).executes(ctx -> {
               ColumnPos column = ColumnPosArgument.m_118992_(ctx, "pos");
               return unwatch((CommandSourceStack)ctx.getSource(), column.m_143196_());
            }));
      }

      private static int unwatch(CommandSourceStack source, ChunkPos chunkPos) {
         ChunkCommand.chunkWatchers.remove(ChunkPos.m_45589_(chunkPos.f_45578_, chunkPos.f_45579_));
         source.m_288197_(
            () -> MekanismLang.COMMAND_CHUNK_UNWATCH.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, ChunkCommand.getPosition(chunkPos)}), true
         );
         return 0;
      }
   }

   private static class WatchCommand {
      static ArgumentBuilder<CommandSourceStack, ?> register() {
         return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("watch").requires(MekanismPermissions.COMMAND_CHUNK_WATCH))
               .executes(ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  return watch(source, new ChunkPos(BlockPos.m_274446_(source.m_81371_())));
               }))
            .then(Commands.m_82129_("pos", ColumnPosArgument.m_118989_()).executes(ctx -> {
               ColumnPos column = ColumnPosArgument.m_118992_(ctx, "pos");
               return watch((CommandSourceStack)ctx.getSource(), column.m_143196_());
            }));
      }

      private static int watch(CommandSourceStack source, ChunkPos chunkPos) {
         ChunkCommand.chunkWatchers.add(ChunkPos.m_45589_(chunkPos.f_45578_, chunkPos.f_45579_));
         source.m_288197_(
            () -> MekanismLang.COMMAND_CHUNK_WATCH.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, ChunkCommand.getPosition(chunkPos)}), true
         );
         return 0;
      }
   }
}
