package mekanism.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;

public class TransmitterTypeDecorator implements IItemDecorator {
   private final ResourceLocation texture;

   public static void registerDecorators(RegisterItemDecorationsEvent event, IBlockProvider... blocks) {
      for (IBlockProvider block : blocks) {
         event.register(block, new TransmitterTypeDecorator(block));
      }
   }

   private TransmitterTypeDecorator(IBlockProvider block) {
      this.texture = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_ICONS, block.getRegistryName().m_135815_() + ".png");
   }

   public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
      if (stack.m_41619_()) {
         return false;
      } else {
         PoseStack pose = guiGraphics.m_280168_();
         pose.m_85836_();
         pose.m_252880_(0.0F, 0.0F, 200.0F);
         guiGraphics.m_280163_(this.texture, xOffset, yOffset, 0.0F, 0.0F, 16, 16, 16, 16);
         pose.m_85849_();
         return true;
      }
   }
}
