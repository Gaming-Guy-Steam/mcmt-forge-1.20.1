package mekanism.client.render.hud;

import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.element.bar.GuiBar;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class MekaSuitEnergyLevel implements IGuiOverlay {
   public static final MekaSuitEnergyLevel INSTANCE = new MekaSuitEnergyLevel();
   private static final ResourceLocation POWER_BAR = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_BAR, "horizontal_power_long.png");

   private MekaSuitEnergyLevel() {
   }

   public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
      if (!gui.getMinecraft().f_91066_.f_92062_ && gui.shouldDrawSurvivalElements()) {
         gui.setupOverlayRenderState(true, false);
         FloatingLong capacity = FloatingLong.ZERO;
         FloatingLong stored = FloatingLong.ZERO;

         for (ItemStack stack : gui.getMinecraft().f_91074_.m_6168_()) {
            if (stack.m_41720_() instanceof ItemMekaSuitArmor) {
               IEnergyContainer container = StorageUtils.getEnergyContainer(stack, 0);
               if (container != null) {
                  capacity = capacity.plusEqual(container.getMaxEnergy());
                  stored = stored.plusEqual(container.getEnergy());
               }
            }
         }

         if (!capacity.isZero()) {
            int x = screenWidth / 2 - 91;
            int y = screenHeight - gui.leftHeight + 2;
            int length = (int)Math.round(stored.divide(capacity).doubleValue() * 79.0);
            GuiUtils.renderExtendedTexture(guiGraphics, GuiBar.BAR, 2, 2, x, y, 81, 6);
            guiGraphics.m_280411_(POWER_BAR, x + 1, y + 1, length, 4, 0.0F, 0.0F, length, 4, 79, 4);
            gui.leftHeight += 8;
         }
      }
   }
}
