package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.MekanismLang;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class MekKeyBindingBuilder {
   @Nullable
   private String description;
   private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;
   private KeyModifier keyModifier = KeyModifier.NONE;
   @Nullable
   private Key key;
   private String category = MekanismLang.MEKANISM.getTranslationKey();
   @Nullable
   private BiConsumer<KeyMapping, Boolean> onKeyDown;
   @Nullable
   private Consumer<KeyMapping> onKeyUp;
   @Nullable
   private BooleanSupplier toggleable;
   private boolean repeating;

   public MekKeyBindingBuilder description(IHasTranslationKey description) {
      return this.description(Objects.requireNonNull(description, "Description cannot be null.").getTranslationKey());
   }

   public MekKeyBindingBuilder description(String description) {
      this.description = Objects.requireNonNull(description, "Description cannot be null.");
      return this;
   }

   public MekKeyBindingBuilder conflictInGame() {
      return this.conflictContext(KeyConflictContext.IN_GAME);
   }

   public MekKeyBindingBuilder conflictInGui() {
      return this.conflictContext(KeyConflictContext.GUI);
   }

   public MekKeyBindingBuilder conflictContext(IKeyConflictContext keyConflictContext) {
      this.keyConflictContext = Objects.requireNonNull(keyConflictContext, "Key conflict context cannot be null.");
      return this;
   }

   public MekKeyBindingBuilder modifier(KeyModifier keyModifier) {
      this.keyModifier = Objects.requireNonNull(keyModifier, "Key modifier cannot be null.");
      return this;
   }

   public MekKeyBindingBuilder keyCode(int keyCode) {
      return this.keyCode(Type.KEYSYM, keyCode);
   }

   public MekKeyBindingBuilder keyCode(Type keyType, int keyCode) {
      Objects.requireNonNull(keyType, "Key type cannot be null.");
      return this.keyCode(keyType.m_84895_(keyCode));
   }

   public MekKeyBindingBuilder keyCode(Key key) {
      this.key = Objects.requireNonNull(key, "Key cannot be null.");
      return this;
   }

   public MekKeyBindingBuilder category(IHasTranslationKey category) {
      return this.category(Objects.requireNonNull(category, "Category cannot be null.").getTranslationKey());
   }

   public MekKeyBindingBuilder category(String category) {
      this.category = Objects.requireNonNull(category, "Category cannot be null.");
      return this;
   }

   public MekKeyBindingBuilder onKeyDown(BiConsumer<KeyMapping, Boolean> onKeyDown) {
      this.onKeyDown = Objects.requireNonNull(onKeyDown, "On key down cannot be null when manually specified.");
      return this;
   }

   public MekKeyBindingBuilder onKeyUp(Consumer<KeyMapping> onKeyUp) {
      this.onKeyUp = Objects.requireNonNull(onKeyUp, "On key up cannot be null when manually specified.");
      return this;
   }

   public MekKeyBindingBuilder toggleable() {
      return this.toggleable(() -> true);
   }

   public MekKeyBindingBuilder toggleable(BooleanSupplier toggleable) {
      this.toggleable = Objects.requireNonNull(toggleable, "Toggleable supplier cannot be null when manually specified.");
      return this;
   }

   public MekKeyBindingBuilder repeating() {
      this.repeating = true;
      return this;
   }

   public KeyMapping build() {
      return new MekKeyBinding(
         Objects.requireNonNull(this.description, "Description has not been set."),
         this.keyConflictContext,
         this.keyModifier,
         Objects.requireNonNull(this.key, "Key has not been set"),
         this.category,
         this.onKeyDown,
         this.onKeyUp,
         this.toggleable,
         this.repeating
      );
   }
}
