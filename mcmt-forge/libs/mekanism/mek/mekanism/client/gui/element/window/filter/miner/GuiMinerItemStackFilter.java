package mekanism.client.gui.element.window.filter.miner;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.filter.GuiItemStackFilter;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GuiMinerItemStackFilter extends GuiItemStackFilter<MinerItemStackFilter, TileEntityDigitalMiner> implements GuiMinerFilterHelper {
   public static GuiMinerItemStackFilter create(IGuiWrapper gui, TileEntityDigitalMiner tile) {
      return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, null);
   }

   public static GuiMinerItemStackFilter edit(IGuiWrapper gui, TileEntityDigitalMiner tile, MinerItemStackFilter filter) {
      return new GuiMinerItemStackFilter(gui, (gui.getWidth() - 173) / 2, 30, tile, filter);
   }

   private GuiMinerItemStackFilter(IGuiWrapper gui, int x, int y, TileEntityDigitalMiner tile, @Nullable MinerItemStackFilter origFilter) {
      super(gui, x, y, 173, 90, tile, origFilter);
   }

   @Override
   protected void init() {
      super.init();
      this.addMinerDefaults(this.gui(), this.filter, this.getSlotOffset(), x$0 -> this.addChild(x$0));
   }

   protected MinerItemStackFilter createNewFilter() {
      return new MinerItemStackFilter();
   }

   @Nullable
   @Override
   protected IJEIGhostTarget.IGhostItemConsumer getGhostHandler() {
      return new IJEIGhostTarget.IGhostItemConsumer() {
         @Override
         public boolean supportsIngredient(Object ingredient) {
            return MekanismConfig.general.easyMinerFilters.get() && IJEIGhostTarget.IGhostItemConsumer.super.supportsIngredient(ingredient);
         }

         @Override
         public void accept(Object ingredient) {
            GuiMinerItemStackFilter.this.setFilterStackWithSound(((ItemStack)ingredient).m_255036_(1));
         }
      };
   }
}
