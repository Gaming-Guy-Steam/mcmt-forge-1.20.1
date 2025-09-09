package mekanism.api.gear;

import java.util.Arrays;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModuleData<MODULE extends ICustomModule<MODULE>> implements IModuleDataProvider<MODULE> {
   private final NonNullSupplier<MODULE> supplier;
   private final IItemProvider itemProvider;
   private final int maxStackSize;
   private final Rarity rarity;
   private final int exclusive;
   private final boolean handlesModeChange;
   private final boolean modeChangeDisabledByDefault;
   private final boolean rendersHUD;
   private final boolean noDisable;
   private final boolean disabledByDefault;
   @Nullable
   private String translationKey;
   @Nullable
   private String descriptionTranslationKey;

   public ModuleData(ModuleData.ModuleDataBuilder<MODULE> builder) {
      this.supplier = builder.supplier;
      this.itemProvider = builder.itemProvider;
      this.rarity = builder.rarity;
      this.maxStackSize = builder.maxStackSize;
      this.exclusive = builder.exclusive;
      this.handlesModeChange = builder.handlesModeChange;
      this.modeChangeDisabledByDefault = builder.modeChangeDisabledByDefault;
      this.rendersHUD = builder.rendersHUD;
      this.noDisable = builder.noDisable;
      this.disabledByDefault = builder.disabledByDefault;
   }

   @NotNull
   @Override
   public final ModuleData<MODULE> getModuleData() {
      return this;
   }

   @NotNull
   public final IItemProvider getItemProvider() {
      return this.itemProvider;
   }

   @NotNull
   public final MODULE get() {
      return (MODULE)this.supplier.get();
   }

   public final Rarity getRarity() {
      return this.rarity;
   }

   public final int getMaxStackSize() {
      return this.maxStackSize;
   }

   public final boolean isExclusive(int mask) {
      return (this.exclusive & mask) != 0;
   }

   public final int getExclusiveFlags() {
      return this.exclusive;
   }

   public final boolean handlesModeChange() {
      return this.handlesModeChange;
   }

   public final boolean isModeChangeDisabledByDefault() {
      return this.modeChangeDisabledByDefault;
   }

   public final boolean rendersHUD() {
      return this.rendersHUD;
   }

   public final boolean isNoDisable() {
      return this.noDisable;
   }

   public final boolean isDisabledByDefault() {
      return this.disabledByDefault;
   }

   @Override
   public String getTranslationKey() {
      if (this.translationKey == null) {
         this.translationKey = Util.m_137492_("module", this.getRegistryName());
      }

      return this.translationKey;
   }

   public String getDescriptionTranslationKey() {
      if (this.descriptionTranslationKey == null) {
         this.descriptionTranslationKey = Util.m_137492_("description", this.getRegistryName());
      }

      return this.descriptionTranslationKey;
   }

   @Override
   public final ResourceLocation getRegistryName() {
      IForgeRegistry<ModuleData<?>> registry = MekanismAPI.moduleRegistry();
      return registry == null ? null : registry.getKey(this);
   }

   public static enum ExclusiveFlag {
      INTERACT_EMPTY,
      INTERACT_ENTITY,
      INTERACT_BLOCK,
      OVERRIDE_JUMP,
      OVERRIDE_DROPS;

      public static final int NONE = 0;
      public static final int ANY = -1;
      public static final int INTERACT_ANY = getCompoundMask(INTERACT_EMPTY, INTERACT_ENTITY, INTERACT_BLOCK);

      public int getMask() {
         return 1 << this.ordinal();
      }

      public static int getCompoundMask(ModuleData.ExclusiveFlag... flags) {
         return Arrays.stream(flags).mapToInt(ModuleData.ExclusiveFlag::getMask).reduce(0, (result, mask) -> result | mask);
      }
   }

   public static class ModuleDataBuilder<MODULE extends ICustomModule<MODULE>> {
      private static final ICustomModule<?> MARKER_MODULE = new ICustomModule() {};
      private static final NonNullSupplier<ICustomModule<?>> MARKER_MODULE_SUPPLIER = () -> MARKER_MODULE;
      private final NonNullSupplier<MODULE> supplier;
      private final IItemProvider itemProvider;
      private Rarity rarity = Rarity.COMMON;
      private int maxStackSize = 1;
      private int exclusive;
      private boolean handlesModeChange;
      private boolean modeChangeDisabledByDefault;
      private boolean rendersHUD;
      private boolean noDisable;
      private boolean disabledByDefault;

      public static ModuleData.ModuleDataBuilder<?> marker(IItemProvider itemProvider) {
         return new ModuleData.ModuleDataBuilder(MARKER_MODULE_SUPPLIER, itemProvider);
      }

      public static <MODULE extends ICustomModule<MODULE>> ModuleData.ModuleDataBuilder<MODULE> custom(
         NonNullSupplier<MODULE> customModule, IItemProvider itemProvider
      ) {
         return new ModuleData.ModuleDataBuilder<>(customModule, itemProvider);
      }

      private ModuleDataBuilder(NonNullSupplier<MODULE> supplier, IItemProvider itemProvider) {
         this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null.");
         this.itemProvider = Objects.requireNonNull(itemProvider, "Item provider cannot be null.");
      }

      public ModuleData.ModuleDataBuilder<MODULE> rarity(Rarity rarity) {
         this.rarity = Objects.requireNonNull(rarity, "Rarity cannot be null.");
         return this;
      }

      public ModuleData.ModuleDataBuilder<MODULE> maxStackSize(int maxStackSize) {
         if (maxStackSize <= 0) {
            throw new IllegalArgumentException("Max stack size must be at least one.");
         } else {
            this.maxStackSize = maxStackSize;
            return this;
         }
      }

      public ModuleData.ModuleDataBuilder<MODULE> exclusive(int mask) {
         this.exclusive = mask;
         return this;
      }

      public ModuleData.ModuleDataBuilder<MODULE> exclusive(ModuleData.ExclusiveFlag... flags) {
         return this.exclusive(flags.length == 0 ? -1 : ModuleData.ExclusiveFlag.getCompoundMask(flags));
      }

      public ModuleData.ModuleDataBuilder<MODULE> handlesModeChange() {
         this.handlesModeChange = true;
         return this;
      }

      public ModuleData.ModuleDataBuilder<MODULE> modeChangeDisabledByDefault() {
         if (!this.handlesModeChange) {
            throw new IllegalStateException("Cannot have a module type that has mode change disabled by default but doesn't support changing modes.");
         } else {
            this.modeChangeDisabledByDefault = true;
            return this;
         }
      }

      public ModuleData.ModuleDataBuilder<MODULE> rendersHUD() {
         this.rendersHUD = true;
         return this;
      }

      public ModuleData.ModuleDataBuilder<MODULE> noDisable() {
         if (this.disabledByDefault) {
            throw new IllegalStateException("Cannot have a module type that is unable to be disabled and also disabled by default.");
         } else {
            this.noDisable = true;
            return this;
         }
      }

      public ModuleData.ModuleDataBuilder<MODULE> disabledByDefault() {
         if (this.noDisable) {
            throw new IllegalStateException("Cannot have a module type that is unable to be disabled and also disabled by default.");
         } else {
            this.disabledByDefault = true;
            return this;
         }
      }
   }
}
