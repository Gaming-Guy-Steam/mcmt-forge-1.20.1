package mekanism.client.render.hud;

import mekanism.api.radiation.IRadiationManager;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class RadiationOverlay implements IGuiOverlay {
   public static final RadiationOverlay INSTANCE = new RadiationOverlay();
   private double prevRadiation = 0.0;
   private long lastTick;

   private RadiationOverlay() {
   }

   public void resetRadiation() {
      this.prevRadiation = 0.0;
   }

   public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
      Player player = gui.getMinecraft().f_91074_;
      if (player != null && IRadiationManager.INSTANCE.isRadiationEnabled() && MekanismUtils.isPlayingMode(player)) {
         player.getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> {
            double radiation = c.getRadiation();
            double severity = RadiationManager.RadiationScale.getScaledDoseSeverity(radiation) * 0.8;
            if (this.lastTick != player.m_9236_().m_46467_()) {
               this.lastTick = player.m_9236_().m_46467_();
               if (this.prevRadiation < severity) {
                  this.prevRadiation = Math.min(severity, this.prevRadiation + 0.01);
               }

               if (this.prevRadiation > severity) {
                  this.prevRadiation = Math.max(severity, this.prevRadiation - 0.01);
               }
            }

            if (severity > 1.0E-7) {
               int effect = (int)(this.prevRadiation * 255.0);
               int color = 1881021952 + effect;
               MekanismRenderer.renderColorOverlay(guiGraphics, 0, 0, color);
            }
         });
      }
   }
}
