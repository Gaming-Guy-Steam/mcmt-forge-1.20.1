package dev.mcmt.forge.runtime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.mcmt.forge.integration.ForgeSideEffectContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod(MCMTForgeMod.MOD_ID)
public final class MCMTForgeMod {

    public static final String MOD_ID = "mcmt";
    private static final int DEFAULT_THREAD_RESERVE = 2;

    private MinecraftServer server;
    private final ExecutorService workers;
    private TickOrchestrator orchestrator;

    public MCMTForgeMod() {
        this.workers = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            r -> {
                Thread t = new Thread(r, "MCMT-Worker");
                t.setDaemon(true);
                return t;
            }
        );

        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            MCMTConfig config = MCMTConfig.load();
            if (config.maxThreads <= 0) {
                int cores = Runtime.getRuntime().availableProcessors();
                int suggested = Math.max(1, cores - DEFAULT_THREAD_RESERVE);
                config.maxThreads = suggested;
                MCMTConfig.save(config);
                System.out.println("[MCMT] First run detected. Setting maxThreads to "
                        + suggested + " (cores: " + cores + ", reserved: " + DEFAULT_THREAD_RESERVE + ")");
            }
        });
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("mcmt")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("summary")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() ->
                        Component.literal("MCMT Summary: Threads=" + MCMTConfig.load().maxThreads), false);
                    return 1;
                }))
            .then(Commands.literal("threads")
                .then(Commands.literal("current")
                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("Set current session threads to " + count), true);
                            return 1;
                        })))
                .then(Commands.literal("always")
                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            int count = IntegerArgumentType.getInteger(ctx, "count");
                            MCMTConfig config = MCMTConfig.load();
                            config.maxThreads = count;
                            MCMTConfig.save(config);
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("Set max threads permanently to " + count), true);
                            return 1;
                        }))))
            .then(Commands.literal("logger")
                .then(Commands.literal("start")
                    .executes(ctx -> {
                        MCMTLogger.start();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal("MCMT logger started."), true);
                        return 1;
                    }))
                .then(Commands.literal("stop")
                    .executes(ctx -> {
                        MCMTLogger.stop();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal("MCMT logger stopped."), true);
                        return 1;
                    })))
            .then(Commands.literal("create")
                .then(Commands.literal("csv")
                    .executes(ctx -> {
                        MCMTLogger.exportCSV();
                        ctx.getSource().sendSuccess(() ->
                            Component.literal("CSV log created."), true);
                        return 1;
                    })))
        );
    }

    private void onServerStarted(ServerStartedEvent e) {
        this.server = e.getServer();
        ForgeSideEffectContext ctx = new ForgeSideEffectContext(dimId -> {
            ResourceLocation rl = ResourceLocation.tryParse(dimId);
            if (rl == null) return null;
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl);
            return server.getLevel(key);
        });

        this.orchestrator = new TickOrchestrator(
            workers,
            () -> listLevelsSnapshot(server),
            (levelView, proxy, submit) -> {
                // Parallel werk hier
            },
            ctx
        );
    }

    private static List<TickOrchestrator.LevelView> listLevelsSnapshot(MinecraftServer server) {
        List<TickOrchestrator.LevelView> out = new ArrayList<>();
        for (ServerLevel lvl : server.getAllLevels()) {
            String id = lvl.dimension().location().toString();
            out.add(new TickOrchestrator.LevelView(id, lvl));
        }
        return out;
    }

    private void onServerStopping(ServerStoppingEvent e) {
        orchestrator = null;
        server = null;
        workers.shutdownNow();
    }

    private void onServerTick(TickEvent.ServerTickEvent e) {
        if (orchestrator == null) return;
        if (e.phase == TickEvent.Phase.START) {
            orchestrator.beginTick();
            MCMTLogger.log("Tick start");
        } else {
            orchestrator.endTick();
            MCMTLogger.log("Tick end");
        }
    }
}
