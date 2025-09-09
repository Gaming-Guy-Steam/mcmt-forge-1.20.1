package mekanism.common.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.config.MekanismConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceRetrogenCommand {
   private static final SimpleCommandExceptionType RETROGEN_NOT_ENABLED = new SimpleCommandExceptionType(
      MekanismLang.COMMAND_ERROR_RETROGEN_DISABLED.translate(new Object[0])
   );
   private static final SimpleCommandExceptionType NO_CHUNKS_QUEUED = new SimpleCommandExceptionType(
      MekanismLang.COMMAND_ERROR_RETROGEN_FAILURE.translate(new Object[0])
   );

   static ArgumentBuilder<CommandSourceStack, ?> register() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("retrogen").requires(MekanismPermissions.COMMAND_FORCE_RETROGEN))
            .executes(ctx -> {
               BlockPos blockPos = BlockPos.m_274446_(((CommandSourceStack)ctx.getSource()).m_81371_());
               ColumnPos pos = new ColumnPos(blockPos.m_123341_(), blockPos.m_123343_());
               return addChunksToRegen((CommandSourceStack)ctx.getSource(), pos, pos);
            }))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("from", ColumnPosArgument.m_118989_()).executes(ctx -> {
                  ColumnPos from = ColumnPosArgument.m_118992_(ctx, "from");
                  return addChunksToRegen((CommandSourceStack)ctx.getSource(), from, from);
               }))
               .then(
                  Commands.m_82129_("to", ColumnPosArgument.m_118989_())
                     .executes(
                        ctx -> addChunksToRegen(
                           (CommandSourceStack)ctx.getSource(), ColumnPosArgument.m_118992_(ctx, "from"), ColumnPosArgument.m_118992_(ctx, "to")
                        )
                     )
               )
         );
   }

   private static int addChunksToRegen(CommandSourceStack source, ColumnPos start, ColumnPos end) throws CommandSyntaxException {
      if (!MekanismConfig.world.enableRegeneration.get()) {
         throw RETROGEN_NOT_ENABLED.create();
      } else {
         int xStart = Math.min(start.f_140723_(), end.f_140723_());
         int xEnd = Math.max(start.f_140723_(), end.f_140723_());
         int zStart = Math.min(start.f_140724_(), end.f_140724_());
         int zEnd = Math.max(start.f_140724_(), end.f_140724_());
         if (xStart >= -30000000 && zStart >= -30000000 && xEnd < 30000000 && zEnd < 30000000) {
            int chunkXStart = SectionPos.m_123171_(xStart);
            int chunkXEnd = SectionPos.m_123171_(xEnd);
            int chunkZStart = SectionPos.m_123171_(zStart);
            int chunkZEnd = SectionPos.m_123171_(zEnd);
            ServerLevel world = source.m_81372_();
            ResourceKey<Level> registryKey = world.m_46472_();
            boolean hasChunks = false;

            for (int chunkX = chunkXStart; chunkX <= chunkXEnd; chunkX++) {
               for (int chunkZ = chunkZStart; chunkZ <= chunkZEnd; chunkZ++) {
                  if (world.m_7232_(chunkX, chunkZ)) {
                     Mekanism.worldTickHandler.addRegenChunk(registryKey, new ChunkPos(chunkX, chunkZ));
                     int finalChunkX = chunkX;
                     int finalChunkZ = chunkZ;
                     source.m_288197_(
                        () -> MekanismLang.COMMAND_RETROGEN_CHUNK_QUEUED
                           .translateColored(
                              EnumColor.GRAY,
                              new Object[]{
                                 EnumColor.INDIGO,
                                 MekanismLang.GENERIC_WITH_COMMA.translate(new Object[]{finalChunkX, finalChunkZ}),
                                 EnumColor.INDIGO,
                                 registryKey.m_135782_()
                              }
                           ),
                        true
                     );
                     hasChunks = true;
                  }
               }
            }

            if (!hasChunks) {
               throw NO_CHUNKS_QUEUED.create();
            } else {
               return 0;
            }
         } else {
            throw BlockPosArgument.f_118235_.create();
         }
      }
   }
}
