package mekanism.common.content.qio;

import mekanism.api.IContentsListener;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface IQIOCraftingWindowHolder extends IContentsListener {
   byte MAX_CRAFTING_WINDOWS = 3;

   @Nullable
   Level getHolderWorld();

   QIOCraftingWindow[] getCraftingWindows();

   @Nullable
   QIOFrequency getFrequency();
}
