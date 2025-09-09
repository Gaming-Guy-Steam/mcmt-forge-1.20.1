package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.common.CommonWorldTickHandler;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedValue;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.content.oredictionificator.OredictionificatorFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableBoolean;
import mekanism.common.inventory.slot.InputInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tags.TagUtils;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.tile.prefab.TileEntityConfigurableMachine;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;

public class TileEntityOredictionificator extends TileEntityConfigurableMachine implements ISustainedData, ITileFilterHolder<OredictionificatorItemFilter> {
   private final FilterManager<OredictionificatorItemFilter> filterManager = new FilterManager<>(OredictionificatorItemFilter.class, this::markForSave);
   public boolean didProcess;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getInputItem"},
      docPlaceholder = "input slot"
   )
   InputInventorySlot inputSlot;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getOutputItem"},
      docPlaceholder = "output slot"
   )
   OutputInventorySlot outputSlot;
   private final CachedValue.IConfigValueInvalidationListener validFiltersListener = new TileEntityOredictionificator.ODConfigValueInvalidationListener();

   public TileEntityOredictionificator(BlockPos pos, BlockState state) {
      super(MekanismBlocks.OREDICTIONIFICATOR, pos, state);
      this.configComponent = new TileComponentConfig(this, TransmissionType.ITEM);
      this.configComponent.setupIOConfig(TransmissionType.ITEM, this.inputSlot, this.outputSlot, RelativeSide.RIGHT);
      this.ejectorComponent = new TileComponentEjector(this);
      this.ejectorComponent.setOutputData(this.configComponent, TransmissionType.ITEM);
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSideWithConfig(this::getDirection, this::getConfig);
      builder.addSlot(this.inputSlot = InputInventorySlot.at(item -> !this.getResult(item).m_41619_(), this::hasFilterableTags, listener, 26, 115));
      builder.addSlot(this.outputSlot = OutputInventorySlot.at(listener, 134, 115));
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (CommonWorldTickHandler.flushTagAndRecipeCaches) {
         for (OredictionificatorFilter<?, ?, ?> filter : this.filterManager.getFilters()) {
            filter.flushCachedTag();
         }
      }

      this.didProcess = false;
      if (MekanismUtils.canFunction(this) && !this.inputSlot.isEmpty()) {
         ItemStack result = this.getResult(this.inputSlot.getStack());
         if (!result.m_41619_()) {
            ItemStack outputStack = this.outputSlot.getStack();
            if (outputStack.m_41619_()) {
               this.inputSlot.shrinkStack(1, Action.EXECUTE);
               this.outputSlot.setStack(result);
               this.didProcess = true;
            } else if (ItemHandlerHelper.canItemStacksStack(outputStack, result) && outputStack.m_41613_() < this.outputSlot.getLimit(outputStack)) {
               this.inputSlot.shrinkStack(1, Action.EXECUTE);
               this.outputSlot.growStack(1, Action.EXECUTE);
               this.didProcess = true;
            }
         }
      }
   }

   public void onLoad() {
      super.onLoad();
      MekanismConfig.general.validOredictionificatorFilters.addInvalidationListener(this.validFiltersListener);
   }

   @Override
   public void m_7651_() {
      super.m_7651_();
      MekanismConfig.general.validOredictionificatorFilters.removeInvalidationListener(this.validFiltersListener);
   }

   private List<ResourceLocation> getFilterableTags(ItemStack stack) {
      Set<ResourceLocation> tags = TagUtils.tagNames(stack.m_204131_());
      if (tags.isEmpty()) {
         return Collections.emptyList();
      } else {
         Map<String, List<String>> possibleFilters = (Map<String, List<String>>)MekanismConfig.general.validOredictionificatorFilters.get();
         List<ResourceLocation> filterableTags = new ArrayList<>();

         for (ResourceLocation resource : tags) {
            if (possibleFilters.getOrDefault(resource.m_135827_(), Collections.emptyList()).stream().anyMatch(pre -> resource.m_135815_().startsWith(pre))) {
               filterableTags.add(resource);
            }
         }

         return filterableTags;
      }
   }

   private boolean hasFilterableTags(ItemStack stack) {
      Set<ResourceLocation> tags = TagUtils.tagNames(stack.m_204131_());
      if (!tags.isEmpty()) {
         Map<String, List<String>> possibleFilters = (Map<String, List<String>>)MekanismConfig.general.validOredictionificatorFilters.get();

         for (ResourceLocation resource : tags) {
            if (possibleFilters.getOrDefault(resource.m_135827_(), Collections.emptyList()).stream().anyMatch(pre -> resource.m_135815_().startsWith(pre))) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isValidTarget(ResourceLocation tag) {
      ITagManager<Item> manager = TagUtils.manager(ForgeRegistries.ITEMS);
      if (manager.isKnownTagName(manager.createTagKey(tag))) {
         for (String filter : ((Map)MekanismConfig.general.validOredictionificatorFilters.get()).getOrDefault(tag.m_135827_(), Collections.emptyList())) {
            if (tag.m_135815_().startsWith(filter)) {
               return true;
            }
         }
      }

      return false;
   }

   private ItemStack getResult(ItemStack stack) {
      List<OredictionificatorItemFilter> enabledFilters = this.filterManager.getEnabledFilters();
      if (!enabledFilters.isEmpty()) {
         for (ResourceLocation filterableTag : this.getFilterableTags(stack)) {
            for (OredictionificatorItemFilter filter : enabledFilters) {
               if (filter.filterMatches(filterableTag)) {
                  ItemStack result = filter.getResult();
                  if (!result.m_41619_()) {
                     return result;
                  }
               }
            }
         }
      }

      return ItemStack.f_41583_;
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      this.filterManager.writeToNBT(dataMap);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      this.filterManager.readFromNBT(dataMap);
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("filters", "filters");
      return remap;
   }

   @Override
   public boolean canPulse() {
      return true;
   }

   @Override
   public FilterManager<OredictionificatorItemFilter> getFilterManager() {
      return this.filterManager;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.track(SyncableBoolean.create(() -> this.didProcess, value -> this.didProcess = value));
      this.filterManager.addContainerTrackers(container);
   }

   @ComputerMethod
   List<OredictionificatorItemFilter> getFilters() {
      return this.filterManager.getFilters();
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean addFilter(OredictionificatorItemFilter filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.addFilter(filter);
   }

   @ComputerMethod(
      requiresPublicSecurity = true
   )
   boolean removeFilter(OredictionificatorItemFilter filter) throws ComputerException {
      this.validateSecurityIsPublic();
      return this.filterManager.removeFilter(filter);
   }

   public class ODConfigValueInvalidationListener implements CachedValue.IConfigValueInvalidationListener {
      @Override
      public void run() {
         for (OredictionificatorItemFilter filter : TileEntityOredictionificator.this.filterManager.getFilters()) {
            filter.checkValidity();
         }
      }

      public boolean isIn(Level level) {
         return TileEntityOredictionificator.this.m_58904_() == level;
      }
   }
}
