package dev.mcmt.forge.runtime;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.mcmt.core.scheduling.AdapterRegistry;
import dev.mcmt.core.tick.TickOrchestrator;
import dev.mcmt.forge.integration.ForgeSideEffectContext;
import dev.mcmt.forge.integration.mekanism.MekFactoryAdapter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.storage.LevelResource;
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
    private final MekFactoryAdapter mekAdapter = new MekFactoryAdapter();
    private final List<BlockEntity> mekFactories = new ArrayList<>();

    public MCMTForgeMod() {
        this.workers = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            r -> {
                Thread t = new Thread(r, "MCMT-Worker");
                t.setDaemon(true);
                return t;
            }
        );

        MinecraftForge.EVENT_BUS.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
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
            AdapterRegistry.register(mekAdapter);
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
        );
    }

    @SubscribeEvent
    private void onServerStarted(ServerStartedEvent e) {
        this.server = e.getServer();

        ForgeSideEffectContext ctx = new ForgeSideEffectContext(dimId -> {
            ResourceLocation rl = ResourceLocation.tryParse(dimId);
            if (rl == null) return null;
            ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, rl);
            return server.getLevel(key);
        });

        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() - DEFAULT_THREAD_RESERVE);
        this.orchestrator = new TickOrchestrator(
            threads,
            new dev.mcmt.core.blacklist.BlacklistManager(
                server.getWorldPath(LevelResource.ROOT).resolve("mcmt_blacklist.json"),
                msg -> System.out.println("[MCMT-Blacklist] " + msg)
            ),
            msg -> System.out.println("[MCMT] " + msg)
        );

        mekFactories.clear();
        for (ServerLevel level : server.getAllLevels()) {
            for (var ticker : level.blockEntityTickers) { // via AT public gemaakt
                if (ticker == null || ticker.getBlockEntity() == null) continue;
                BlockEntity be = ticker.getBlockEntity();
                if (be instanceof mekanism.common.tile.factory.TileEntityFactory<?>) {
                    mekFactories.add(be);
                }
            }
        }
        System.out.println("[MCMT] Vooraf gedetecteerde Mekanism factories: " + mekFactories.size());

        MinecraftForge.EVENT_BUS.addListener((TickEvent.LevelTickEvent ev) -> {
            if (ev.phase != TickEvent.Phase.END) return;
            if (!(ev.level instanceof ServerLevel sl)) return;

            TickOrchestrator.TickFrame frame = orchestrator.beginTick();
            int adapterCount = 0;

            for (var ticker : sl.blockEntityTickers) { // via AT public gemaakt
                if (ticker == null || ticker.getBlockEntity() == null) continue;
                BlockEntity be = ticker.getBlockEntity();
                if (!(be instanceof mekanism.common.tile.factory.TileEntityFactory<?>)) continue;

                String beKey = be.getType().toString() + "@"
                    + be.getBlockPos().getX() + ","
                    + be.getBlockPos().getY() + ","
                    + be.getBlockPos().getZ();

                orchestrator.submitWithAdapter(frame, beKey, be, () -> {
                    // Adapter doet het werk
                });
                adapterCount++;
            }

            orchestrator.endTick(frame, ctx);

            if (adapterCount > 0) {
                System.out.println("[MCMT] " + adapterCount +
                    " Mekanism factories via adapters getickt in " +
                    sl.dimension().location());
            }
        });
    }

    @SubscribeEvent
    private void onServerStopping(ServerStoppingEvent e) {
        try {
            orchestrator = null;
            server = null;
        } finally {
            workers.shutdownNow();
        }
    }

    @SubscribeEvent
    private void onServerTick(TickEvent.ServerTickEvent e) {
        // Eventueel globale server-taken
    }
}
