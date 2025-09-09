package mekanism.client.gui.element.window.filter.miner;

import java.util.function.UnaryOperator;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.button.MekanismImageButton;
import mekanism.client.gui.element.slot.GuiSlot;
import mekanism.client.gui.element.slot.SlotType;
import mekanism.client.gui.element.window.filter.GuiFilter;
import mekanism.client.gui.element.window.filter.GuiFilterHelper;
import mekanism.client.jei.interfaces.IJEIGhostTarget;
import mekanism.common.MekanismLang;
import mekanism.common.content.miner.MinerFilter;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public interface GuiMinerFilterHelper extends GuiFilterHelper<TileEntityDigitalMiner> {
   default void addMinerDefaults(IGuiWrapper gui, MinerFilter<?> filter, int slotOffset, UnaryOperator<GuiElement> childAdder) {
      childAdder.apply(
         new GuiSlot(SlotType.NORMAL, gui, this.getRelativeX() + 148, this.getRelativeY() + slotOffset)
            .setRenderHover(true)
            .stored(() -> new ItemStack(filter.replaceTarget))
            .click(GuiFilter.getHandleClickSlot(gui, GuiFilter.NOT_EMPTY_BLOCK, stack -> filter.replaceTarget = stack.m_41720_()))
            .setGhostHandler((IJEIGhostTarget.IGhostBlockItemConsumer)ingredient -> {
               filter.replaceTarget = ((ItemStack)ingredient).m_41720_();
               Minecraft.m_91087_().m_91106_().m_120367_(SimpleSoundInstance.m_263171_(SoundEvents.f_12490_, 1.0F));
            })
      );
      childAdder.apply(
         new MekanismImageButton(
            gui,
            this.getRelativeX() + 148,
            this.getRelativeY() + 45,
            14,
            16,
            MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BUTTON, "exclamation.png"),
            () -> filter.requiresReplacement = !filter.requiresReplacement,
            (onHover, guiGraphics, mouseX, mouseY) -> gui.displayTooltips(
               guiGraphics,
               mouseX,
               mouseY,
               MekanismLang.MINER_REQUIRE_REPLACE.translate(new Object[]{BooleanStateDisplay.YesNo.of(filter.requiresReplacement)})
            )
         )
      );
   }

   default GuiMinerFilerSelect getFilterSelect(IGuiWrapper gui, TileEntityDigitalMiner tile) {
      return new GuiMinerFilerSelect(gui, tile);
   }
}
