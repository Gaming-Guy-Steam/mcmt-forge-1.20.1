package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants.Key;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class MekKeyBinding extends KeyMapping {
   @Nullable
   private final BiConsumer<KeyMapping, Boolean> onKeyDown;
   @Nullable
   private final Consumer<KeyMapping> onKeyUp;
   @Nullable
   private final BooleanSupplier toggleable;
   private final boolean repeating;
   private boolean lastState;

   MekKeyBinding(
      String description,
      IKeyConflictContext keyConflictContext,
      KeyModifier keyModifier,
      Key key,
      String category,
      @Nullable BiConsumer<KeyMapping, Boolean> onKeyDown,
      @Nullable Consumer<KeyMapping> onKeyUp,
      @Nullable BooleanSupplier toggleable,
      boolean repeating
   ) {
      super(description, keyConflictContext, keyModifier, key, category);
      this.onKeyDown = onKeyDown;
      this.onKeyUp = onKeyUp;
      this.toggleable = toggleable;
      this.repeating = repeating;
   }

   private boolean isToggleable() {
      return this.toggleable != null && this.toggleable.getAsBoolean();
   }

   public void m_7249_(boolean value) {
      if (this.isToggleable()) {
         if (value && this.isConflictContextAndModifierActive()) {
            super.m_7249_(!this.m_90857_());
         }
      } else {
         super.m_7249_(value);
      }

      boolean state = this.m_90857_();
      if (state != this.lastState || state && this.repeating) {
         if (state) {
            if (this.onKeyDown != null) {
               this.onKeyDown.accept(this, this.lastState);
            }
         } else if (this.onKeyUp != null) {
            this.onKeyUp.accept(this);
         }

         this.lastState = state;
      }
   }

   public boolean m_90857_() {
      return this.f_90817_ && (this.isConflictContextAndModifierActive() || this.isToggleable());
   }
}
