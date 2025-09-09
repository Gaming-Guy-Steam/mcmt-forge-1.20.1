package mekanism.common.content.gear;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.gear.config.IModuleConfigItem;
import mekanism.api.gear.config.ModuleBooleanData;
import mekanism.api.gear.config.ModuleConfigData;
import mekanism.api.gear.config.ModuleConfigItemCreator;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import mekanism.api.radial.RadialData;
import mekanism.api.radial.mode.IRadialMode;
import mekanism.api.radial.mode.NestedRadialMode;
import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public final class Module<MODULE extends ICustomModule<MODULE>> implements IModule<MODULE> {
   public static final String ENABLED_KEY = "enabled";
   private final List<ModuleConfigItem<?>> configItems = new ArrayList<>();
   private final ModuleData<MODULE> data;
   private final ItemStack container;
   private final MODULE customModule;
   private ModuleConfigItem<Boolean> enabled;
   private ModuleConfigItem<Boolean> handleModeChange;
   private ModuleConfigItem<Boolean> renderHUD;
   private int installed = 1;

   public Module(ModuleData<MODULE> data, ItemStack container) {
      this.data = data;
      this.container = container;
      this.customModule = data.get();
   }

   @Override
   public MODULE getCustomInstance() {
      return this.customModule;
   }

   public void init() {
      this.enabled = this.addConfigItem(
         new ModuleConfigItem<Boolean>(this, "enabled", MekanismLang.MODULE_ENABLED, new ModuleBooleanData(!this.data.isDisabledByDefault())) {
            public void set(@NotNull Boolean val, @Nullable Runnable callback) {
               boolean wasEnabled = this.get();
               super.set(val, callback);
               if (callback == null && wasEnabled != this.get()) {
                  Module.this.customModule.onEnabledStateChange(Module.this);
               }
            }

            protected void checkValidity(@NotNull Boolean value, @Nullable Runnable callback) {
               if (value && (Module.this.handlesModeChange() || Module.this.data.getExclusiveFlags() != 0)) {
                  for (Module<?> m : ModuleHelper.get().loadAll(Module.this.getContainer())) {
                     if (Module.this.data != m.getData()) {
                        if (m.getData().isExclusive(Module.this.data.getExclusiveFlags())) {
                           m.setDisabledForce(callback != null);
                        }

                        if (Module.this.handlesModeChange() && m.handlesModeChange()) {
                           m.setModeHandlingDisabledForce();
                        }
                     }
                  }
               }
            }
         }
      );
      if (this.data.handlesModeChange()) {
         this.handleModeChange = this.addConfigItem(
            new ModuleConfigItem<Boolean>(
               this, "handleModeChange", MekanismLang.MODULE_HANDLE_MODE_CHANGE, new ModuleBooleanData(!this.data.isModeChangeDisabledByDefault())
            ) {
               protected void checkValidity(@NotNull Boolean value, @Nullable Runnable callback) {
                  if (value && Module.this.handlesModeChange()) {
                     for (Module<?> m : ModuleHelper.get().loadAll(Module.this.getContainer())) {
                        if (Module.this.data != m.getData() && m.handlesModeChange()) {
                           m.setModeHandlingDisabledForce();
                        }
                     }
                  }
               }
            }
         );
      }

      if (this.data.rendersHUD()) {
         this.renderHUD = this.addConfigItem(new ModuleConfigItem<>(this, "renderHUD", MekanismLang.MODULE_RENDER_HUD, new ModuleBooleanData()));
      }

      this.customModule.init(this, new ModuleConfigItemCreator() {
         @Override
         public <TYPE> IModuleConfigItem<TYPE> createConfigItem(String name, ILangEntry description, ModuleConfigData<TYPE> data) {
            return Module.this.addConfigItem(new ModuleConfigItem<>(Module.this, name, description, data));
         }

         @Override
         public IModuleConfigItem<Boolean> createDisableableConfigItem(String name, ILangEntry description, boolean def, BooleanSupplier isConfigEnabled) {
            return Module.this.addConfigItem(new ModuleConfigItem.DisableableModuleConfigItem(Module.this, name, description, def, isConfigEnabled));
         }
      });
   }

   private <T> ModuleConfigItem<T> addConfigItem(ModuleConfigItem<T> item) {
      this.configItems.add(item);
      return item;
   }

   public void tick(Player player) {
      if (this.isEnabled()) {
         if (player.m_9236_().m_5776_()) {
            this.customModule.tickClient(this, player);
         } else {
            this.customModule.tickServer(this, player);
         }
      }
   }

   @Nullable
   @Override
   public IEnergyContainer getEnergyContainer() {
      return StorageUtils.getEnergyContainer(this.getContainer(), 0);
   }

   @Override
   public FloatingLong getContainerEnergy() {
      IEnergyContainer energyContainer = this.getEnergyContainer();
      return energyContainer == null ? FloatingLong.ZERO : energyContainer.getEnergy();
   }

   @Override
   public boolean hasEnoughEnergy(FloatingLongSupplier energySupplier) {
      return this.hasEnoughEnergy(energySupplier.get());
   }

   @Override
   public boolean hasEnoughEnergy(FloatingLong cost) {
      return cost.isZero() || this.getContainerEnergy().greaterOrEqual(cost);
   }

   @Override
   public boolean canUseEnergy(LivingEntity wearer, FloatingLong energy) {
      return this.canUseEnergy(wearer, energy, false);
   }

   @Override
   public boolean canUseEnergy(LivingEntity wearer, FloatingLong energy, boolean ignoreCreative) {
      return this.canUseEnergy(wearer, this.getEnergyContainer(), energy, ignoreCreative);
   }

   @Override
   public boolean canUseEnergy(LivingEntity wearer, @Nullable IEnergyContainer energyContainer, FloatingLong energy, boolean ignoreCreative) {
      return energyContainer == null || wearer.m_5833_() || ignoreCreative && wearer instanceof Player player && player.m_7500_()
         ? false
         : energyContainer.extract(energy, Action.SIMULATE, AutomationType.MANUAL).equals(energy);
   }

   @Override
   public FloatingLong useEnergy(LivingEntity wearer, FloatingLong energy) {
      return this.useEnergy(wearer, energy, true);
   }

   @Override
   public FloatingLong useEnergy(LivingEntity wearer, FloatingLong energy, boolean freeCreative) {
      return this.useEnergy(wearer, this.getEnergyContainer(), energy, freeCreative);
   }

   @Override
   public FloatingLong useEnergy(LivingEntity wearer, @Nullable IEnergyContainer energyContainer, FloatingLong energy, boolean freeCreative) {
      return energyContainer == null || freeCreative && wearer instanceof Player player && !MekanismUtils.isPlayingMode(player)
         ? FloatingLong.ZERO
         : energyContainer.extract(energy, Action.EXECUTE, AutomationType.MANUAL);
   }

   public void read(CompoundTag nbt) {
      if (nbt.m_128425_("amount", 3)) {
         this.installed = nbt.m_128451_("amount");
      }

      this.init();

      for (ModuleConfigItem<?> item : this.configItems) {
         item.read(nbt);
      }
   }

   public void save(@Nullable Runnable callback) {
      CompoundTag modulesTag = ItemDataUtils.getOrAddCompound(this.container, "modules");
      String registryName = this.data.getRegistryName().toString();
      CompoundTag nbt = modulesTag.m_128469_(registryName);
      nbt.m_128405_("amount", this.installed);

      for (ModuleConfigItem<?> item : this.configItems) {
         item.write(nbt);
      }

      modulesTag.m_128365_(registryName, nbt);
      if (callback != null) {
         callback.run();
      }
   }

   @Override
   public ModuleData<MODULE> getData() {
      return this.data;
   }

   @Override
   public int getInstalledCount() {
      return this.installed;
   }

   public void setInstalledCount(int installed) {
      this.installed = installed;
   }

   @Override
   public boolean isEnabled() {
      return this.enabled.get();
   }

   public void setDisabledForce(boolean hasCallback) {
      if (this.isEnabled()) {
         this.enabled.getData().set(false);
         this.save(null);
         if (!hasCallback) {
            this.customModule.onEnabledStateChange(this);
         }
      }
   }

   @Override
   public ItemStack getContainer() {
      return this.container;
   }

   public List<ModuleConfigItem<?>> getConfigItems() {
      return this.configItems;
   }

   public void addHUDStrings(Player player, List<Component> list) {
      this.customModule.addHUDStrings(this, player, list::add);
   }

   public void addHUDElements(Player player, List<IHUDElement> list) {
      this.customModule.addHUDElements(this, player, list::add);
   }

   public void addRadialModes(@NotNull ItemStack stack, Consumer<NestedRadialMode> adder) {
      this.customModule.addRadialModes(this, stack, adder);
   }

   @Nullable
   public <M extends IRadialMode> M getMode(@NotNull ItemStack stack, RadialData<M> radialData) {
      return this.customModule.getMode(this, stack, radialData);
   }

   public <M extends IRadialMode> boolean setMode(@NotNull Player player, @NotNull ItemStack stack, RadialData<M> radialData, M mode) {
      return this.customModule.setMode(this, player, stack, radialData, mode);
   }

   @Nullable
   public Component getModeScrollComponent(ItemStack stack) {
      return this.customModule.getModeScrollComponent(this, stack);
   }

   public void changeMode(@NotNull Player player, @NotNull ItemStack stack, int shift, IModeItem.DisplayChange displayChange) {
      this.customModule.changeMode(this, player, stack, shift, displayChange != IModeItem.DisplayChange.NONE);
   }

   @Override
   public boolean handlesModeChange() {
      return this.data.handlesModeChange() && this.handleModeChange.get() && (this.isEnabled() || this.customModule.canChangeModeWhenDisabled(this));
   }

   @Override
   public boolean handlesRadialModeChange() {
      return this.data.handlesModeChange() && (this.isEnabled() || this.customModule.canChangeRadialModeWhenDisabled(this));
   }

   public boolean handlesAnyModeChange() {
      return !this.data.handlesModeChange()
         ? false
         : this.isEnabled()
            || this.handleModeChange.get() && this.customModule.canChangeModeWhenDisabled(this)
            || this.customModule.canChangeRadialModeWhenDisabled(this);
   }

   public void setModeHandlingDisabledForce() {
      if (this.data.handlesModeChange()) {
         this.handleModeChange.getData().set(false);
         this.save(null);
      }
   }

   @Override
   public boolean renderHUD() {
      return this.data.rendersHUD() && this.renderHUD.get();
   }

   public void onAdded(boolean first) {
      for (Module<?> module : ModuleHelper.get().loadAll(this.getContainer())) {
         if (module.getData() != this.getData()) {
            if (this.getData().isExclusive(module.getData().getExclusiveFlags())) {
               module.setDisabledForce(false);
            }

            if (this.handlesModeChange() && module.handlesModeChange()) {
               module.setModeHandlingDisabledForce();
            }
         }
      }

      this.customModule.onAdded(this, first);
   }

   public void onRemoved(boolean last) {
      this.customModule.onRemoved(this, last);
   }

   @Override
   public void displayModeChange(Player player, Component modeName, IHasTextComponent mode) {
      player.m_213846_(MekanismUtils.logFormat(MekanismLang.MODULE_MODE_CHANGE.translate(new Object[]{modeName, EnumColor.INDIGO, mode})));
   }

   @Override
   public void toggleEnabled(Player player, Component modeName) {
      this.enabled.set(!this.isEnabled());
      Component message;
      if (this.isEnabled()) {
         message = MekanismLang.GENERIC_STORED.translate(new Object[]{modeName, EnumColor.BRIGHT_GREEN, MekanismLang.MODULE_ENABLED_LOWER});
      } else {
         message = MekanismLang.GENERIC_STORED.translate(new Object[]{modeName, EnumColor.DARK_RED, MekanismLang.MODULE_DISABLED_LOWER});
      }

      player.m_213846_(MekanismUtils.logFormat(message));
   }
}
