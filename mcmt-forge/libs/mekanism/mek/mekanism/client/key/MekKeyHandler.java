package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

public class MekKeyHandler {
   private MekKeyHandler() {
   }

   public static boolean isKeyPressed(KeyMapping keyBinding) {
      if (keyBinding.m_90857_()) {
         return true;
      } else {
         return keyBinding.getKeyConflictContext().isActive() && keyBinding.getKeyModifier().isActive(keyBinding.getKeyConflictContext())
            ? isKeyDown(keyBinding)
            : KeyModifier.isKeyCodeModifier(keyBinding.getKey()) && isKeyDown(keyBinding);
      }
   }

   private static boolean isKeyDown(KeyMapping keyBinding) {
      Key key = keyBinding.getKey();
      int keyCode = key.m_84873_();
      if (keyCode != InputConstants.f_84822_.m_84873_()) {
         long windowHandle = Minecraft.m_91087_().m_91268_().m_85439_();

         try {
            if (key.m_84868_() == Type.KEYSYM) {
               return InputConstants.m_84830_(windowHandle, keyCode);
            }

            if (key.m_84868_() == Type.MOUSE) {
               return GLFW.glfwGetMouseButton(windowHandle, keyCode) == 1;
            }
         } catch (Exception var6) {
         }
      }

      return false;
   }

   public static boolean isRadialPressed() {
      KeyMapping keyBinding = MekanismKeyHandler.handModeSwitchKey;
      if (keyBinding.m_90857_()) {
         return true;
      } else {
         IKeyConflictContext conflictContext = keyBinding.getKeyConflictContext();
         if (!conflictContext.isActive()) {
            conflictContext = KeyConflictContext.GUI;
         }

         return !conflictContext.isActive() || keyBinding.getKeyModifier() != KeyModifier.NONE && !keyBinding.getKeyModifier().isActive(conflictContext)
            ? KeyModifier.isKeyCodeModifier(keyBinding.getKey()) && isKeyDown(keyBinding)
            : isKeyDown(keyBinding);
      }
   }
}
