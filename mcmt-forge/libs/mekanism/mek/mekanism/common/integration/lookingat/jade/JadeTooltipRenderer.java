package mekanism.common.integration.lookingat.jade;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;

public class JadeTooltipRenderer implements IBlockComponentProvider, IEntityComponentProvider {
   static final JadeTooltipRenderer INSTANCE = new JadeTooltipRenderer();

   public ResourceLocation getUid() {
      return JadeConstants.TOOLTIP_RENDERER;
   }

   public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
      this.append(tooltip, accessor, config);
   }

   public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
      this.append(tooltip, accessor, config);
   }

   private void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
      CompoundTag data = accessor.getServerData();
      if (data.m_128425_("mekData", 9)) {
         Component lastText = null;
         ListTag list = data.m_128437_("mekData", 10);

         for (int i = 0; i < list.size(); i++) {
            CompoundTag elementData = list.m_128728_(i);
            if (elementData.m_128425_("text", 8)) {
               Component text = Serializer.m_130701_(elementData.m_128461_("text"));
               if (text != null) {
                  if (lastText != null) {
                     tooltip.add(lastText);
                  }

                  lastText = text;
               }
            } else {
               LookingAtElement element;
               ResourceLocation name;
               if (elementData.m_128425_("energy", 8)) {
                  element = new EnergyElement(
                     FloatingLong.parseFloatingLong(elementData.m_128461_("energy"), true), FloatingLong.parseFloatingLong(elementData.m_128461_("max"), true)
                  );
                  name = LookingAtUtils.ENERGY;
               } else if (elementData.m_128425_("fluid", 10)) {
                  element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.m_128469_("fluid")), elementData.m_128451_("max"));
                  name = LookingAtUtils.FLUID;
               } else {
                  if (!elementData.m_128425_("chemical", 10)) {
                     continue;
                  }

                  CompoundTag chemicalData = elementData.m_128469_("chemical");
                  ChemicalStack<?> chemicalStack;
                  if (chemicalData.m_128425_("gasName", 8)) {
                     chemicalStack = GasStack.readFromNBT(chemicalData);
                     name = LookingAtUtils.GAS;
                  } else if (chemicalData.m_128425_("infuseTypeName", 8)) {
                     chemicalStack = InfusionStack.readFromNBT(chemicalData);
                     name = LookingAtUtils.INFUSE_TYPE;
                  } else if (chemicalData.m_128425_("pigmentName", 8)) {
                     chemicalStack = PigmentStack.readFromNBT(chemicalData);
                     name = LookingAtUtils.PIGMENT;
                  } else {
                     if (!chemicalData.m_128425_("slurryName", 8)) {
                        continue;
                     }

                     chemicalStack = SlurryStack.readFromNBT(chemicalData);
                     name = LookingAtUtils.SLURRY;
                  }

                  element = new ChemicalElement(chemicalStack, elementData.m_128454_("max"));
               }

               if (config.get(name)) {
                  tooltip.add(new JadeTooltipRenderer.MekElement(lastText, element).tag(name));
               }

               lastText = null;
            }
         }

         if (lastText != null) {
            tooltip.add(lastText);
         }
      }
   }

   private static class MekElement extends Element {
      @Nullable
      private final Component text;
      private final LookingAtElement element;

      public MekElement(@Nullable Component text, LookingAtElement element) {
         this.element = element;
         this.text = text;
      }

      public Vec2 getSize() {
         int width = this.element.getWidth();
         int height = this.element.getHeight() + 2;
         if (this.text != null) {
            width = Math.max(width, 96);
            height += 14;
         }

         return new Vec2(width, height);
      }

      public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
         if (this.text != null) {
            LookingAtElement.renderScaledText(Minecraft.m_91087_(), guiGraphics, x + 4.0F, y + 3.0F, 16777215, 92.0F, this.text);
            y += 13.0F;
         }

         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_(x, y, 0.0F);
         this.element.render(guiGraphics, 0, 1);
         pose.m_85849_();
      }
   }
}
