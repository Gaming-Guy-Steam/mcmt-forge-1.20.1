package mekanism.common.lib;

import java.io.File;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismSavedData extends SavedData {
   public abstract void load(@NotNull CompoundTag nbt);

   public void m_77757_(@NotNull File file) {
      if (this.m_77764_()) {
         File tempFile = file.toPath().getParent().resolve(file.getName() + ".tmp").toFile();
         super.m_77757_(tempFile);
         if (file.exists() && !file.delete()) {
            Mekanism.logger.error("Failed to delete " + file.getName());
         }

         if (!tempFile.renameTo(file)) {
            Mekanism.logger.error("Failed to rename " + tempFile.getName());
         }
      }
   }

   public static <DATA extends MekanismSavedData> DATA createSavedData(Supplier<DATA> createFunction, String name) {
      MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
      if (currentServer == null) {
         throw new IllegalStateException("Current server is null");
      } else {
         DimensionDataStorage dataStorage = currentServer.m_129783_().m_8895_();
         return createSavedData(dataStorage, createFunction, name);
      }
   }

   public static <DATA extends MekanismSavedData> DATA createSavedData(DimensionDataStorage dataStorage, Supplier<DATA> createFunction, String name) {
      return (DATA)dataStorage.m_164861_(tag -> {
         DATA handler = createFunction.get();
         handler.load(tag);
         return handler;
      }, createFunction, "mekanism_" + name);
   }
}
