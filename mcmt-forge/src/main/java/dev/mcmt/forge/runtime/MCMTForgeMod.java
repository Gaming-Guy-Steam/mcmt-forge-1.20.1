// mcmt-forge/src/main/java/dev/mcmt/forge/runtime/MCMTForgeMod.java
package dev.mcmt.forge.runtime;

import dev.mcmt.core.effects.SideEffectBuffer;
import dev.mcmt.core.world.LevelProxy;
import dev.mcmt.forge.integration.ForgeSideEffectContext;
import dev.mcmt.forge.runtime.TickOrchestrator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@Mod(MCMTForgeMod.MOD_ID)
public final class MCMTForgeMod {

    public static final String MOD_ID = "mcmt";

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
                // TODO: replace with your real parallel compute
                // Example smoke test: set a block at 0,64,0 every tick in each level
                // submit.accept(() -> {
                //     BlockState state = Blocks.STONE.defaultBlockState();
                //     proxy.setBlock(0, 64, 0, new ForgeSideEffectContext.FBlockState(state), 3);
                // });
            },
            ctx
        );
    }

    private static List<TickOrchestrator.LevelView> listLevelsSnapshot(MinecraftServer server) {
        List<TickOrchestrator.LevelView> out = new ArrayList<>();
        for (ServerLevel lvl : server.getAllLevels()) {
            String id = lvl.dimension().location().toString(); // e.g., "minecraft:overworld"
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
        } else {
            orchestrator.endTick();
        }
    }
}
