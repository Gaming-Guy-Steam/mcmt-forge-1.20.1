package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.client.gui.GuiUtils;
import mekanism.client.render.HUDRenderer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.item.interfaces.IItemHUDProvider;
import mekanism.common.tags.MekanismTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.items.IItemHandler;

public class MekanismHUD implements IGuiOverlay {
   public static final MekanismHUD INSTANCE = new MekanismHUD();
   private static final EquipmentSlot[] EQUIPMENT_ORDER = new EquipmentSlot[]{
      EquipmentSlot.OFFHAND, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private final HUDRenderer hudRenderer = new HUDRenderer();

   private MekanismHUD() {
   }

   public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight) {
      Minecraft minecraft = gui.getMinecraft();
      Player player = minecraft.f_91074_;
      if (!minecraft.f_91066_.f_92062_ && player != null && !player.m_5833_() && MekanismConfig.client.enableHUD.get()) {
         int count = 0;
         List<List<Component>> renderStrings = new ArrayList<>();

         for (EquipmentSlot slotType : EQUIPMENT_ORDER) {
            ItemStack stack = player.m_6844_(slotType);
            if (stack.m_41720_() instanceof IItemHUDProvider hudProvider) {
               count += this.makeComponent(list -> hudProvider.addHUDStrings(list, player, stack, slotType), renderStrings);
            }
         }

         if (Mekanism.hooks.CuriosLoaded) {
            Optional<? extends IItemHandler> invOptional = CuriosIntegration.getCuriosInventory(player);
            if (invOptional.isPresent()) {
               IItemHandler inv = invOptional.get();
               int i = 0;

               for (int slots = inv.getSlots(); i < slots; i++) {
                  ItemStack stack = inv.getStackInSlot(i);
                  if (stack.m_41720_() instanceof IItemHUDProvider hudProvider) {
                     count += this.makeComponent(list -> hudProvider.addCurioHUDStrings(list, player, stack), renderStrings);
                  }
               }
            }
         }

         Font font = gui.m_93082_();
         boolean reverseHud = MekanismConfig.client.reverseHUD.get();
         int maxTextHeight = screenHeight;
         if (count > 0) {
            float hudScale = MekanismConfig.client.hudScale.get();
            int xScale = (int)(screenWidth / hudScale);
            int yScale = (int)(screenHeight / hudScale);
            int start = renderStrings.size() * 2 + count * 9;
            int y = yScale - start;
            maxTextHeight = (int)(y * hudScale);
            PoseStack pose = guiGraphics.m_280168_();
            pose.m_85836_();
            pose.m_85841_(hudScale, hudScale, hudScale);
            int backgroundColor = minecraft.f_91066_.m_92170_(0.0F);
            if (backgroundColor != 0) {
               int maxTextWidth = 0;

               for (List<Component> group : renderStrings) {
                  for (Component text : group) {
                     int textWidth = font.m_92852_(text);
                     if (textWidth > maxTextWidth) {
                        maxTextWidth = textWidth;
                     }
                  }
               }

               int x = reverseHud ? xScale - maxTextWidth - 2 : 2;
               GuiUtils.drawBackdrop(guiGraphics, Minecraft.m_91087_(), x, y, maxTextWidth, maxTextHeight, -1);
            }

            for (List<Component> group : renderStrings) {
               for (Component textx : group) {
                  int textWidth = font.m_92852_(textx);
                  int x = reverseHud ? xScale - textWidth - 2 : 2;
                  guiGraphics.m_280430_(font, textx, x, y, -3618616);
                  y += 9;
               }

               y += 2;
            }

            pose.m_85849_();
         }

         if (player.m_6844_(EquipmentSlot.HEAD).m_204117_(MekanismTags.Items.MEKASUIT_HUD_RENDERER)) {
            this.hudRenderer.renderHUD(minecraft, guiGraphics, font, partialTicks, screenWidth, screenHeight, maxTextHeight, reverseHud);
         }
      }
   }

   private int makeComponent(Consumer<List<Component>> adder, List<List<Component>> initial) {
      List<Component> list = new ArrayList<>();
      adder.accept(list);
      int size = list.size();
      if (size > 0) {
         initial.add(list);
      }

      return size;
   }
}
