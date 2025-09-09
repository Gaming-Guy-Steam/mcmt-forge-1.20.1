package mekanism.common.command.builders;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.RegistryUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildCommand {
   private static final SimpleCommandExceptionType MISS = new SimpleCommandExceptionType(MekanismLang.COMMAND_ERROR_BUILD_MISS.translate(new Object[0]));
   public static final ArgumentBuilder<CommandSourceStack, ?> COMMAND = ((LiteralArgumentBuilder)Commands.m_82127_("build")
         .requires(MekanismPermissions.COMMAND_BUILD.and(cs -> cs.m_81373_() instanceof ServerPlayer)))
      .then(((LiteralArgumentBuilder)Commands.m_82127_("remove").requires(MekanismPermissions.COMMAND_BUILD_REMOVE)).executes(ctx -> {
         CommandSourceStack source = (CommandSourceStack)ctx.getSource();
         destroy(source.m_81372_(), rayTracePos(source));
         source.m_288197_(() -> MekanismLang.COMMAND_BUILD_REMOVED.translateColored(EnumColor.GRAY, new Object[0]), true);
         return 0;
      }));

   private BuildCommand() {
   }

   public static void register(String name, ILangEntry localizedName, StructureBuilder builder) {
      COMMAND.then(
         registerSub(Commands.m_82127_(name).then(registerSub(Commands.m_82127_("empty"), localizedName, builder, true)), localizedName, builder, false)
      );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> registerSub(
      ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, ILangEntry localizedName, StructureBuilder builder, boolean empty
   ) {
      return argumentBuilder.executes(ctx -> {
            CommandSourceStack source = (CommandSourceStack)ctx.getSource();
            BlockPos pos = rayTracePos(source).m_121945_(Direction.UP);
            return build(ctx, localizedName, builder, pos, empty);
         })
         .then(
            Commands.m_82129_("start", BlockPosArgument.m_118239_())
               .executes(ctx -> build(ctx, localizedName, builder, BlockPosArgument.m_118242_(ctx, "start"), empty))
         );
   }

   private static BlockPos rayTracePos(CommandSourceStack source) throws CommandSyntaxException {
      BlockHitResult result = MekanismUtils.rayTrace(source.m_81375_(), 100.0);
      if (result.m_6662_() == Type.MISS) {
         throw MISS.create();
      } else {
         return result.m_82425_();
      }
   }

   private static int build(CommandContext<CommandSourceStack> ctx, ILangEntry localizedName, StructureBuilder builder, BlockPos start, boolean empty) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      builder.build(source.m_81372_(), start, empty);
      source.m_288197_(() -> {
         ILangEntry builtEntry = empty ? MekanismLang.COMMAND_BUILD_BUILT_EMPTY : MekanismLang.COMMAND_BUILD_BUILT;
         return builtEntry.translateColored(EnumColor.GRAY, EnumColor.INDIGO, localizedName);
      }, true);
      return 0;
   }

   private static void destroy(Level world, BlockPos pos) throws CommandSyntaxException {
      Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap();
      if (!isMekanismBlock(world, chunkMap, pos)) {
         throw MISS.create();
      } else {
         Set<BlockPos> traversed = new HashSet<>();
         Queue<BlockPos> openSet = new ArrayDeque<>();
         openSet.add(pos);
         traversed.add(pos);

         while (!openSet.isEmpty()) {
            BlockPos ptr = openSet.poll();
            if (isMekanismBlock(world, chunkMap, ptr)) {
               Clearable.m_18908_(WorldUtils.getTileEntity(world, chunkMap, ptr));
               world.m_7471_(ptr, false);

               for (Direction side : EnumUtils.DIRECTIONS) {
                  BlockPos offset = ptr.m_121945_(side);
                  if (traversed.add(offset)) {
                     openSet.add(offset);
                  }
               }
            }
         }
      }
   }

   private static boolean isMekanismBlock(@Nullable LevelAccessor world, @NotNull Long2ObjectMap<ChunkAccess> chunkMap, @NotNull BlockPos pos) {
      Optional<BlockState> state = WorldUtils.getBlockState(world, chunkMap, pos);
      return state.isPresent() && RegistryUtils.getNamespace(state.get().m_60734_()).startsWith("mekanism");
   }
}
