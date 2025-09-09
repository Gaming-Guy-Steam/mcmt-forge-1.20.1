package mekanism.common.integration.lookingat.wthit;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IDataReader;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class WTHITTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {
   static final WTHITTooltipRenderer INSTANCE = new WTHITTooltipRenderer();

   public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
      this.append(tooltip, accessor.getData(), config);
   }

   public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
      this.append(tooltip, accessor.getData(), config);
   }

   private void append(ITooltip tooltip, IDataReader dataReader, IPluginConfig config) {
      WTHITLookingAtHelper helper = (WTHITLookingAtHelper)dataReader.get(WTHITLookingAtHelper.class);
      if (helper != null) {
         tooltip.setLine(MekanismWTHITPlugin.MEK_DATA);
         Component lastText = null;

         for (Object element : helper.elements) {
            if (element instanceof Component component) {
               if (lastText != null) {
                  tooltip.addLine(lastText);
               }

               lastText = component;
            } else {
               ResourceLocation name;
               if (element instanceof EnergyElement) {
                  name = LookingAtUtils.ENERGY;
               } else if (element instanceof FluidElement) {
                  name = LookingAtUtils.FLUID;
               } else {
                  if (!(element instanceof ChemicalElement chemicalElement)) {
                     continue;
                  }
                  name = switch (chemicalElement.getChemicalType()) {
                     case GAS -> LookingAtUtils.GAS;
                     case INFUSION -> LookingAtUtils.INFUSE_TYPE;
                     case PIGMENT -> LookingAtUtils.PIGMENT;
                     case SLURRY -> LookingAtUtils.SLURRY;
                  };
               }

               if (config.getBoolean(name)) {
                  tooltip.addLine(new WTHITTooltipRenderer.MekElement(lastText, (LookingAtElement)element));
               }

               lastText = null;
            }
         }

         if (lastText != null) {
            tooltip.addLine(lastText);
         }
      }
   }

   private record MekElement(@Nullable Component text, LookingAtElement element) implements ITooltipComponent {
      public int getWidth() {
         return this.text == null ? this.element.getWidth() : Math.max(this.element.getWidth(), 96);
      }

      public int getHeight() {
         return this.text == null ? this.element.getHeight() + 2 : this.element.getHeight() + 16;
      }

      public void render(GuiGraphics guiGraphics, int x, int y, float delta) {
         if (this.text != null) {
            LookingAtElement.renderScaledText(Minecraft.m_91087_(), guiGraphics, x + 4, y + 3, 16777215, 92.0F, this.text);
            y += 13;
         }

         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_(x, y, 0.0F);
         this.element.render(guiGraphics, 0, 1);
         pose.m_85849_();
      }
   }
}
