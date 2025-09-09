package mekanism.client.gui.element.window.filter.miner;

import java.util.Collections;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiModIDFilter;
import mekanism.common.base.TagCache;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiMinerModIDFilter extends GuiModIDFilter<MinerModIDFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {
   public static GuiMinerModIDFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
      return new GuiMinerModIDFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
   }

   public static GuiMinerModIDFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerModIDFilter filter) {
      return new GuiMinerModIDFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
   }

   private GuiMinerModIDFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, @Nullable MinerModIDFilter origFilter) {
      super(gui, x, y, 173, 90, tile, origFilter);
   }

   @Override
   protected void init() {
      super.init();
      this.addMinerDefaults(this.gui(), this.filter, this.getSlotOffset(), x$0 -> this.addChild(x$0));
   }

   protected MinerModIDFilter createNewFilter() {
      return new MinerModIDFilter();
   }

   @NotNull
   @Override
   protected List<ItemStack> getRenderStacks() {
      return this.filter.hasFilter() ? TagCache.getBlockModIDStacks(this.filter.getModID()).stacks() : Collections.emptyList();
   }

   @Override
   protected boolean hasMatchingTargets(String name) {
      return TagCache.getBlockModIDStacks(name).hasMatch();
   }
}
