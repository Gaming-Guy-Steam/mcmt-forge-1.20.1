package mekanism.client.gui.element.tab;

import java.util.function.Supplier;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.security.SecurityData;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketSecurityMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiSecurityTab extends GuiInsetElement<Supplier<ICapabilityProvider>> {
   private static final ResourceLocation PUBLIC = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "public.png");
   private static final ResourceLocation PRIVATE = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "private.png");
   private static final ResourceLocation PROTECTED = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI, "protected.png");
   @Nullable
   private final InteractionHand currentHand;

   public GuiSecurityTab(IGuiWrapper gui, ICapabilityProvider provider) {
      this(gui, provider, 34);
   }

   public GuiSecurityTab(IGuiWrapper gui, ICapabilityProvider provider, int y) {
      this(gui, () -> provider, y, null);
   }

   public GuiSecurityTab(IGuiWrapper gui, @NotNull InteractionHand hand) {
      this(gui, () -> minecraft.f_91074_.m_21120_(hand), 34, hand);
   }

   private GuiSecurityTab(IGuiWrapper gui, Supplier<ICapabilityProvider> provider, int y, @Nullable InteractionHand hand) {
      super(PUBLIC, gui, provider, gui.getWidth(), y, 26, 18, false);
      this.currentHand = hand;
   }

   @Override
   protected void colorTab(GuiGraphics guiGraphics) {
      MekanismRenderer.color(guiGraphics, SpecialColors.TAB_SECURITY);
   }

   @Override
   protected ResourceLocation getOverlay() {
      return switch (ISecurityUtils.INSTANCE.getSecurityMode(this.dataSource.get(), true)) {
         case PUBLIC -> super.getOverlay();
         case PRIVATE -> PRIVATE;
         case TRUSTED -> PROTECTED;
      };
   }

   @Override
   public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderToolTip(guiGraphics, mouseX, mouseY);
      ICapabilityProvider provider = this.dataSource.get();
      if (provider != null) {
         provider.getCapability(Capabilities.SECURITY_OBJECT)
            .ifPresent(
               security -> {
                  SecurityData data = SecurityUtils.get().getFinalData(security, true);
                  Component securityComponent = MekanismLang.SECURITY.translateColored(EnumColor.GRAY, new Object[]{data.mode()});
                  Component ownerComponent = OwnerDisplay.of(minecraft.f_91074_, security.getOwnerUUID(), security.getOwnerName()).getTextComponent();
                  if (data.override()) {
                     this.displayTooltips(
                        guiGraphics,
                        mouseX,
                        mouseY,
                        new Component[]{securityComponent, ownerComponent, MekanismLang.SECURITY_OVERRIDDEN.translateColored(EnumColor.RED, new Object[0])}
                     );
                  } else {
                     this.displayTooltips(guiGraphics, mouseX, mouseY, new Component[]{securityComponent, ownerComponent});
                  }
               }
            );
      }
   }

   @Override
   public void onClick(double mouseX, double mouseY, int button) {
      ICapabilityProvider provider = this.dataSource.get();
      if (provider != null) {
         provider.getCapability(Capabilities.SECURITY_OBJECT)
            .ifPresent(
               security -> {
                  if (security.ownerMatches(minecraft.f_91074_)) {
                     if (this.currentHand != null) {
                        Mekanism.packetHandler().sendToServer(new PacketSecurityMode(this.currentHand, button == 0));
                     } else if (provider instanceof BlockEntity tile) {
                        Mekanism.packetHandler()
                           .sendToServer(
                              new PacketGuiInteract(
                                 button == 0 ? PacketGuiInteract.GuiInteraction.NEXT_SECURITY_MODE : PacketGuiInteract.GuiInteraction.PREVIOUS_SECURITY_MODE,
                                 tile
                              )
                           );
                     } else if (provider instanceof Entity entity) {
                        Mekanism.packetHandler()
                           .sendToServer(
                              new PacketGuiInteract(
                                 button == 0
                                    ? PacketGuiInteract.GuiInteractionEntity.NEXT_SECURITY_MODE
                                    : PacketGuiInteract.GuiInteractionEntity.PREVIOUS_SECURITY_MODE,
                                 entity
                              )
                           );
                     }
                  }
               }
            );
      }
   }

   public boolean m_7972_(int button) {
      return button == 0 || button == 1;
   }
}
