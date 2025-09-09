package mekanism.common.content.gear;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.ILangEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModuleConfigItem<TYPE> implements IModuleConfigItem<TYPE> {
   private final Module<?> module;
   private final String name;
   private final ILangEntry description;
   private final ModuleConfigData<TYPE> data;

   public ModuleConfigItem(Module<?> module, String name, ILangEntry description, ModuleConfigData<TYPE> data) {
      this.module = module;
      this.name = name;
      this.description = description;
      this.data = data;
   }

   public Component getDescription() {
      return this.description.translate();
   }

   public ModuleConfigData<TYPE> getData() {
      return this.data;
   }

   @NotNull
   @Override
   public TYPE get() {
      return this.data.get();
   }

   @Override
   public void set(@NotNull TYPE val) {
      this.set(val, null);
   }

   public void set(@NotNull TYPE val, @Nullable Runnable callback) {
      Objects.requireNonNull(val, "Value cannot be null.");
      this.data.set(val);
      this.checkValidity(val, callback);
      this.module.save(callback);
   }

   protected void checkValidity(@NotNull TYPE val, @Nullable Runnable callback) {
   }

   public boolean matches(IModuleDataProvider<?> moduleType, String name) {
      return this.module.getData() == moduleType.getModuleData() && this.getName().equals(name);
   }

   public void read(CompoundTag tag) {
      if (tag.m_128441_(this.name)) {
         this.data.read(this.name, tag);
      }
   }

   public void write(CompoundTag tag) {
      this.data.write(this.name, tag);
   }

   @NotNull
   @Override
   public String getName() {
      return this.name;
   }

   public static class DisableableModuleConfigItem extends ModuleConfigItem<Boolean> {
      private final BooleanSupplier isConfigEnabled;

      public DisableableModuleConfigItem(Module<?> module, String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
         super(module, name, description, new ModuleBooleanData(def));
         this.isConfigEnabled = isConfigEnabled;
      }

      @NotNull
      public Boolean get() {
         return this.isConfigEnabled() && (Boolean)super.get();
      }

      public boolean isConfigEnabled() {
         return this.isConfigEnabled.getAsBoolean();
      }
   }
}
