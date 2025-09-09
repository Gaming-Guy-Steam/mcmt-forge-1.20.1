package mekanism.common.content.gear;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemModule;
import mekanism.common.registries.MekanismItems;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.BooleanStateDisplay;
import mekanism.common.util.text.TextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ModuleHelper implements IModuleHelper {
   private final Map<Item, Set<ModuleData<?>>> supportedModules = new Reference2ObjectArrayMap(5);
   private final Map<ModuleData<?>, Set<Item>> supportedContainers = new IdentityHashMap<>();
   private final Map<ModuleData<?>, Set<ModuleData<?>>> conflictingModules = new IdentityHashMap<>();

   public static ModuleHelper get() {
      return (ModuleHelper)INSTANCE;
   }

   public void processIMC(InterModProcessEvent event) {
      Map<ModuleData<?>, Builder<Item>> supportedContainersBuilderMap = new IdentityHashMap<>();
      this.mapSupportedModules(event, "add_meka_tool_modules", MekanismItems.MEKA_TOOL, supportedContainersBuilderMap);
      this.mapSupportedModules(event, "add_meka_suit_helmet_modules", MekanismItems.MEKASUIT_HELMET, supportedContainersBuilderMap);
      this.mapSupportedModules(event, "add_meka_suit_bodyarmor_modules", MekanismItems.MEKASUIT_BODYARMOR, supportedContainersBuilderMap);
      this.mapSupportedModules(event, "add_meka_suit_pants_modules", MekanismItems.MEKASUIT_PANTS, supportedContainersBuilderMap);
      this.mapSupportedModules(event, "add_meka_suit_boots_modules", MekanismItems.MEKASUIT_BOOTS, supportedContainersBuilderMap);

      for (Entry<ModuleData<?>, Builder<Item>> entry : supportedContainersBuilderMap.entrySet()) {
         this.supportedContainers.put(entry.getKey(), entry.getValue().build());
      }
   }

   private void mapSupportedModules(
      InterModProcessEvent event, String imcMethod, IItemProvider moduleContainer, Map<ModuleData<?>, Builder<Item>> supportedContainersBuilderMap
   ) {
      Builder<ModuleData<?>> supportedModulesBuilder = ImmutableSet.builder();
      event.getIMCStream(imcMethod::equals).forEach(message -> {
         Object body = message.messageSupplier().get();
         if (body instanceof IModuleDataProvider<?> moduleDataProvider) {
            supportedModulesBuilder.add(moduleDataProvider.getModuleData());
            this.logDebugReceivedIMC(imcMethod, message.senderModId(), moduleDataProvider);
         } else if (body instanceof IModuleDataProvider<?>[] providers) {
            for (IModuleDataProvider<?> moduleDataProvider : providers) {
               supportedModulesBuilder.add(moduleDataProvider.getModuleData());
               this.logDebugReceivedIMC(imcMethod, message.senderModId(), moduleDataProvider);
            }
         } else {
            Mekanism.logger.warn("Received IMC message for '{}' from mod '{}' with an invalid body.", imcMethod, message.senderModId());
         }
      });
      Set<ModuleData<?>> supported = supportedModulesBuilder.build();
      if (!supported.isEmpty()) {
         Item item = moduleContainer.m_5456_();
         this.supportedModules.put(item, supported);

         for (ModuleData<?> data : supported) {
            supportedContainersBuilderMap.computeIfAbsent(data, d -> ImmutableSet.builder()).add(item);
         }
      }
   }

   private void logDebugReceivedIMC(String imcMethod, String senderModId, IModuleDataProvider<?> moduleDataProvider) {
      Mekanism.logger.debug("Received IMC message '{}' from '{}' for module '{}'.", new Object[]{imcMethod, senderModId, moduleDataProvider.getRegistryName()});
   }

   public ItemModule createModuleItem(IModuleDataProvider<?> moduleDataProvider, Properties properties) {
      return new ItemModule(moduleDataProvider, properties);
   }

   @Override
   public Set<ModuleData<?>> getSupported(ItemStack container) {
      return this.getSupported(container.m_41720_());
   }

   private Set<ModuleData<?>> getSupported(Item item) {
      return this.supportedModules.getOrDefault(item, Collections.emptySet());
   }

   @Override
   public Set<Item> getSupported(IModuleDataProvider<?> typeProvider) {
      return this.supportedContainers.getOrDefault(typeProvider.getModuleData(), Collections.emptySet());
   }

   @Override
   public Set<ModuleData<?>> getConflicting(IModuleDataProvider<?> typeProvider) {
      return this.conflictingModules.computeIfAbsent(typeProvider.getModuleData(), moduleType -> {
         Set<ModuleData<?>> conflicting = new ReferenceOpenHashSet();

         for (Item item : this.getSupported(moduleType)) {
            for (ModuleData<?> other : this.getSupported(item)) {
               if (moduleType != other && moduleType.isExclusive(other.getExclusiveFlags())) {
                  conflicting.add(other);
               }
            }
         }

         return conflicting;
      });
   }

   @Override
   public boolean isEnabled(ItemStack container, IModuleDataProvider<?> typeProvider) {
      IModule<?> m = this.load(container, typeProvider);
      return m != null && m.isEnabled();
   }

   @Nullable
   public <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(ItemStack container, IModuleDataProvider<MODULE> typeProvider) {
      if (container.m_41720_() instanceof IModuleContainerItem) {
         CompoundTag modulesTag = ItemDataUtils.getCompound(container, "modules");
         return this.load(container, typeProvider.getModuleData(), modulesTag, null);
      } else {
         return null;
      }
   }

   @Override
   public List<Module<?>> loadAll(ItemStack container) {
      if (container.m_41720_() instanceof IModuleContainerItem) {
         List<Module<?>> modules = new ArrayList<>();
         CompoundTag modulesTag = ItemDataUtils.getCompound(container, "modules");

         for (ModuleData<?> moduleType : this.loadAllTypes(modulesTag)) {
            Module<?> module = this.load(container, moduleType, modulesTag, null);
            if (module != null) {
               modules.add(module);
            }
         }

         return modules;
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public <MODULE extends ICustomModule<?>> List<Module<? extends MODULE>> loadAll(ItemStack container, Class<MODULE> moduleClass) {
      if (container.m_41720_() instanceof IModuleContainerItem) {
         List<Module<? extends MODULE>> modules = new ArrayList<>();
         CompoundTag modulesTag = ItemDataUtils.getCompound(container, "modules");

         for (ModuleData<?> moduleType : this.loadAllTypes(modulesTag)) {
            Module<?> module = this.load(container, moduleType, modulesTag, moduleClass);
            if (module != null) {
               modules.add((Module<? extends MODULE>)module);
            }
         }

         return modules;
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public List<ModuleData<?>> loadAllTypes(ItemStack container) {
      return container.m_41720_() instanceof IModuleContainerItem
         ? this.loadAllTypes(ItemDataUtils.getCompound(container, "modules"))
         : Collections.emptyList();
   }

   private List<ModuleData<?>> loadAllTypes(CompoundTag modulesTag) {
      List<ModuleData<?>> moduleTypes = new ArrayList<>();

      for (String name : modulesTag.m_128431_()) {
         ModuleData<?> moduleType = this.getModuleTypeFromName(name);
         if (moduleType != null) {
            moduleTypes.add(moduleType);
         }
      }

      return moduleTypes;
   }

   public Reference2IntMap<ModuleData<?>> loadAllCounts(ItemStack container) {
      return container.m_41720_() instanceof IModuleContainerItem
         ? this.loadAllCounts(ItemDataUtils.getCompound(container, "modules"))
         : Reference2IntMaps.emptyMap();
   }

   private Reference2IntMap<ModuleData<?>> loadAllCounts(CompoundTag modulesTag) {
      Reference2IntMap<ModuleData<?>> counts = new Reference2IntOpenHashMap();

      for (String name : modulesTag.m_128431_()) {
         ModuleData<?> moduleType = this.getModuleTypeFromName(name);
         if (moduleType != null) {
            int count = 1;
            CompoundTag moduleData = modulesTag.m_128469_(name);
            if (moduleData.m_128425_("amount", 3)) {
               count = moduleData.m_128451_("amount");
            }

            counts.put(moduleType, count);
         }
      }

      return counts;
   }

   @Nullable
   private ModuleData<?> getModuleTypeFromName(String name) {
      ResourceLocation registryName = ResourceLocation.m_135820_(name);
      return registryName == null ? null : (ModuleData)MekanismAPI.moduleRegistry().getValue(registryName);
   }

   @Nullable
   private <MODULE extends ICustomModule<MODULE>> Module<MODULE> load(
      ItemStack container, ModuleData<MODULE> type, CompoundTag modulesTag, @Nullable Class<? extends ICustomModule<?>> typeFilter
   ) {
      String registryName = type.getRegistryName().toString();
      if (modulesTag.m_128425_(registryName, 10)) {
         Module<MODULE> module = new Module<>(type, container);
         if (typeFilter == null || typeFilter.isInstance(module.getCustomInstance())) {
            module.read(modulesTag.m_128469_(registryName));
            return module;
         }
      }

      return null;
   }

   @Override
   public IHUDElement hudElementEnabled(ResourceLocation icon, boolean enabled) {
      return this.hudElement(
         icon, BooleanStateDisplay.OnOff.caps(enabled, false).getTextComponent(), enabled ? IHUDElement.HUDColor.REGULAR : IHUDElement.HUDColor.FADED
      );
   }

   @Override
   public IHUDElement hudElementPercent(ResourceLocation icon, double ratio) {
      return this.hudElement(
         icon,
         TextUtils.getPercent(ratio),
         ratio > 0.2 ? IHUDElement.HUDColor.REGULAR : (ratio > 0.1 ? IHUDElement.HUDColor.WARNING : IHUDElement.HUDColor.DANGER)
      );
   }

   @Override
   public IHUDElement hudElement(ResourceLocation icon, Component text, IHUDElement.HUDColor color) {
      return HUDElement.of(icon, text, HUDElement.HUDColor.from(color));
   }

   @Override
   public synchronized void addMekaSuitModuleModels(ResourceLocation location) {
      MekanismModelCache.INSTANCE.registerMekaSuitModuleModel(location);
   }

   @Override
   public synchronized void addMekaSuitModuleModelSpec(
      String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlot slotType, Predicate<LivingEntity> isActive
   ) {
      MekaSuitArmor.registerModule(name, moduleDataProvider, slotType, isActive);
   }
}
