package mekanism.common.item.interfaces;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.api.IIncrementalEnum;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.CommonPlayerTickHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface IJetpackItem {
   boolean canUseJetpack(ItemStack stack);

   IJetpackItem.JetpackMode getJetpackMode(ItemStack stack);

   void useJetpackFuel(ItemStack stack);

   @NotNull
   static ItemStack getActiveJetpack(LivingEntity entity) {
      return getJetpack(entity, stack -> stack.m_41720_() instanceof IJetpackItem jetpackItem && jetpackItem.canUseJetpack(stack));
   }

   @NotNull
   static ItemStack getPrimaryJetpack(LivingEntity entity) {
      return getJetpack(entity, stack -> stack.m_41720_() instanceof IJetpackItem);
   }

   private static ItemStack getJetpack(LivingEntity entity, Predicate<ItemStack> matcher) {
      ItemStack chest = entity.m_6844_(EquipmentSlot.CHEST);
      if (matcher.test(chest)) {
         return chest;
      } else {
         return Mekanism.hooks.CuriosLoaded ? CuriosIntegration.findFirstCurio(entity, matcher) : ItemStack.f_41583_;
      }
   }

   static boolean handleJetpackMotion(Player player, IJetpackItem.JetpackMode mode, BooleanSupplier ascendingSupplier) {
      Vec3 motion = player.m_20184_();
      if (mode == IJetpackItem.JetpackMode.NORMAL) {
         if (player.m_21255_()) {
            Vec3 forward = player.m_20154_();
            Vec3 delta = forward.m_82559_(forward.m_82490_(0.15)).m_82549_(forward.m_82490_(1.5).m_82546_(motion).m_82490_(0.5));
            player.m_20256_(motion.m_82549_(delta));
            return false;
         }

         player.m_20334_(motion.m_7096_(), Math.min(motion.m_7098_() + 0.15, 0.5), motion.m_7094_());
      } else if (mode == IJetpackItem.JetpackMode.HOVER) {
         boolean ascending = ascendingSupplier.getAsBoolean();
         boolean descending = player.m_20164_();
         if (ascending == descending) {
            if (motion.m_7098_() > 0.0) {
               player.m_20334_(motion.m_7096_(), Math.max(motion.m_7098_() - 0.15, 0.0), motion.m_7094_());
            } else if (motion.m_7098_() < 0.0 && !CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
               player.m_20334_(motion.m_7096_(), Math.min(motion.m_7098_() + 0.15, 0.0), motion.m_7094_());
            }
         } else if (ascending) {
            player.m_20334_(motion.m_7096_(), Math.min(motion.m_7098_() + 0.15, 0.2), motion.m_7094_());
         } else if (!CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
            player.m_20334_(motion.m_7096_(), Math.max(motion.m_7098_() - 0.15, -0.2), motion.m_7094_());
         }
      }

      return true;
   }

   static IJetpackItem.JetpackMode getPlayerJetpackMode(Player player, IJetpackItem.JetpackMode mode, BooleanSupplier ascendingSupplier) {
      if (!player.m_5833_() && mode != IJetpackItem.JetpackMode.DISABLED) {
         boolean ascending = ascendingSupplier.getAsBoolean();
         if (mode == IJetpackItem.JetpackMode.HOVER) {
            if (ascending && !player.m_20164_() || !CommonPlayerTickHandler.isOnGroundOrSleeping(player)) {
               return mode;
            }
         } else if (mode == IJetpackItem.JetpackMode.NORMAL && ascending) {
            return mode;
         }
      }

      return IJetpackItem.JetpackMode.DISABLED;
   }

   @NothingNullByDefault
   public static enum JetpackMode implements IIncrementalEnum<IJetpackItem.JetpackMode>, IHasTextComponent {
      NORMAL(MekanismLang.JETPACK_NORMAL, EnumColor.DARK_GREEN, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "jetpack_normal.png")),
      HOVER(MekanismLang.JETPACK_HOVER, EnumColor.DARK_AQUA, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "jetpack_hover.png")),
      DISABLED(MekanismLang.JETPACK_DISABLED, EnumColor.DARK_RED, MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "jetpack_off.png"));

      private static final IJetpackItem.JetpackMode[] MODES = values();
      private final ILangEntry langEntry;
      private final EnumColor color;
      private final ResourceLocation hudIcon;

      private JetpackMode(ILangEntry langEntry, EnumColor color, ResourceLocation hudIcon) {
         this.langEntry = langEntry;
         this.color = color;
         this.hudIcon = hudIcon;
      }

      @Override
      public Component getTextComponent() {
         return this.langEntry.translateColored(this.color);
      }

      public IJetpackItem.JetpackMode byIndex(int index) {
         return byIndexStatic(index);
      }

      public ResourceLocation getHUDIcon() {
         return this.hudIcon;
      }

      public static IJetpackItem.JetpackMode byIndexStatic(int index) {
         return MathUtils.getByIndexMod(MODES, index);
      }
   }
}
