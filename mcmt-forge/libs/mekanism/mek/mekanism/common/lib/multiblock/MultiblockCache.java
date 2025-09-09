package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.DataHandlerUtils;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.inventory.slot.BasicInventorySlot;
import mekanism.common.util.StackUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockCache<T extends MultiblockData>
   implements IMekanismInventory,
   IMekanismFluidHandler,
   IMekanismStrictEnergyHandler,
   IMekanismHeatHandler,
   IGasTracker,
   IInfusionTracker,
   IPigmentTracker,
   ISlurryTracker {
   private final List<IInventorySlot> inventorySlots = new ArrayList<>();
   private final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
   private final List<IGasTank> gasTanks = new ArrayList<>();
   private final List<IInfusionTank> infusionTanks = new ArrayList<>();
   private final List<IPigmentTank> pigmentTanks = new ArrayList<>();
   private final List<ISlurryTank> slurryTanks = new ArrayList<>();
   private final List<IEnergyContainer> energyContainers = new ArrayList<>();
   private final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

   public void apply(T data) {
      for (MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>> type : MultiblockCache.CacheSubstance.VALUES) {
         List<? extends INBTSerializable<CompoundTag>> containers = type.getContainerList(data);
         if (containers != null) {
            List<? extends INBTSerializable<CompoundTag>> cacheContainers = type.getContainerList(this);

            for (int i = 0; i < cacheContainers.size(); i++) {
               if (i < containers.size()) {
                  containers.get(i).deserializeNBT((CompoundTag)cacheContainers.get(i).serializeNBT());
               }
            }
         }
      }
   }

   public void sync(T data) {
      for (MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>> type : MultiblockCache.CacheSubstance.VALUES) {
         List<? extends INBTSerializable<CompoundTag>> containersToCopy = type.getContainerList(data);
         if (containersToCopy != null) {
            List<? extends INBTSerializable<CompoundTag>> cacheContainers = type.getContainerList(this);
            if (cacheContainers.isEmpty()) {
               type.prefab(this, containersToCopy.size());
            }

            for (int i = 0; i < containersToCopy.size(); i++) {
               type.sync((INBTSerializable<CompoundTag>)cacheContainers.get(i), (INBTSerializable<CompoundTag>)containersToCopy.get(i));
            }
         }
      }
   }

   public void load(CompoundTag nbtTags) {
      for (MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>> type : MultiblockCache.CacheSubstance.VALUES) {
         int stored = nbtTags.m_128451_(type.getTagKey() + "_stored");
         if (stored > 0) {
            type.prefab(this, stored);
            DataHandlerUtils.readContainers(type.getContainerList(this), nbtTags.m_128437_(type.getTagKey(), 10));
         }
      }
   }

   public void save(CompoundTag nbtTags) {
      for (MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>> type : MultiblockCache.CacheSubstance.VALUES) {
         List<INBTSerializable<CompoundTag>> containers = type.getContainerList(this);
         if (!containers.isEmpty()) {
            nbtTags.m_128405_(type.getTagKey() + "_stored", containers.size());
            nbtTags.m_128365_(type.getTagKey(), DataHandlerUtils.writeContainers(containers));
         }
      }
   }

   public void merge(MultiblockCache<T> mergeCache, MultiblockCache.RejectContents rejectContents) {
      for (MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>> type : MultiblockCache.CacheSubstance.VALUES) {
         type.preHandleMerge(this, mergeCache);
      }

      StackUtils.merge(this.getInventorySlots(null), mergeCache.getInventorySlots(null), rejectContents.rejectedItems);
      StorageUtils.mergeFluidTanks(this.getFluidTanks(null), mergeCache.getFluidTanks(null), rejectContents.rejectedFluids);
      StorageUtils.mergeTanks(this.getGasTanks(null), mergeCache.getGasTanks(null), rejectContents.rejectedGases);
      StorageUtils.mergeTanks(this.getInfusionTanks(null), mergeCache.getInfusionTanks(null), rejectContents.rejectedInfuseTypes);
      StorageUtils.mergeTanks(this.getPigmentTanks(null), mergeCache.getPigmentTanks(null), rejectContents.rejectedPigments);
      StorageUtils.mergeTanks(this.getSlurryTanks(null), mergeCache.getSlurryTanks(null), rejectContents.rejectedSlurries);
      StorageUtils.mergeEnergyContainers(this.getEnergyContainers(null), mergeCache.getEnergyContainers(null));
      StorageUtils.mergeHeatCapacitors(this.getHeatCapacitors(null), mergeCache.getHeatCapacitors(null));
   }

   @Override
   public void onContentsChanged() {
   }

   @NotNull
   @Override
   public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
      return this.inventorySlots;
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidTanks;
   }

   @NotNull
   @Override
   public List<IGasTank> getGasTanks(@Nullable Direction side) {
      return this.gasTanks;
   }

   @NotNull
   @Override
   public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
      return this.infusionTanks;
   }

   @NotNull
   @Override
   public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
      return this.pigmentTanks;
   }

   @NotNull
   @Override
   public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
      return this.slurryTanks;
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyContainers;
   }

   @NotNull
   @Override
   public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
      return this.heatCapacitors;
   }

   public abstract static class CacheSubstance<HANDLER, ELEMENT> {
      public static final MultiblockCache.CacheSubstance<IMekanismInventory, IInventorySlot> ITEMS = new MultiblockCache.CacheSubstance<IMekanismInventory, IInventorySlot>(
         "Items"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.inventorySlots.add(BasicInventorySlot.at(cache, 0, 0));
         }

         protected List<IInventorySlot> containerList(IMekanismInventory inventory) {
            return inventory.getInventorySlots(null);
         }

         public void sync(IInventorySlot cache, IInventorySlot data) {
            cache.setStack(data.getStack());
         }
      };
      public static final MultiblockCache.CacheSubstance<IMekanismFluidHandler, IExtendedFluidTank> FLUID = new MultiblockCache.CacheSubstance<IMekanismFluidHandler, IExtendedFluidTank>(
         "FluidTanks"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, cache));
         }

         protected List<IExtendedFluidTank> containerList(IMekanismFluidHandler fluidHandler) {
            return fluidHandler.getFluidTanks(null);
         }

         public void sync(IExtendedFluidTank cache, IExtendedFluidTank data) {
            cache.setStack(data.getFluid());
         }
      };
      public static final MultiblockCache.CacheSubstance<IGasTracker, IGasTank> GAS = new MultiblockCache.CacheSubstance<IGasTracker, IGasTank>("GasTanks") {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.gasTanks.add((IGasTank)ChemicalTankBuilder.GAS.createAllValid(Long.MAX_VALUE, cache));
         }

         protected List<IGasTank> containerList(IGasTracker tracker) {
            return tracker.getGasTanks(null);
         }

         public void sync(IGasTank cache, IGasTank data) {
            cache.setStack(data.getStack());
         }
      };
      public static final MultiblockCache.CacheSubstance<IInfusionTracker, IInfusionTank> INFUSION = new MultiblockCache.CacheSubstance<IInfusionTracker, IInfusionTank>(
         "InfusionTanks"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.infusionTanks.add((IInfusionTank)ChemicalTankBuilder.INFUSION.createAllValid(Long.MAX_VALUE, cache));
         }

         protected List<IInfusionTank> containerList(IInfusionTracker tracker) {
            return tracker.getInfusionTanks(null);
         }

         public void sync(IInfusionTank cache, IInfusionTank data) {
            cache.setStack(data.getStack());
         }
      };
      public static final MultiblockCache.CacheSubstance<IPigmentTracker, IPigmentTank> PIGMENT = new MultiblockCache.CacheSubstance<IPigmentTracker, IPigmentTank>(
         "PigmentTanks"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.pigmentTanks.add((IPigmentTank)ChemicalTankBuilder.PIGMENT.createAllValid(Long.MAX_VALUE, cache));
         }

         protected List<IPigmentTank> containerList(IPigmentTracker tracker) {
            return tracker.getPigmentTanks(null);
         }

         public void sync(IPigmentTank cache, IPigmentTank data) {
            cache.setStack(data.getStack());
         }
      };
      public static final MultiblockCache.CacheSubstance<ISlurryTracker, ISlurryTank> SLURRY = new MultiblockCache.CacheSubstance<ISlurryTracker, ISlurryTank>(
         "SlurryTanks"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.slurryTanks.add((ISlurryTank)ChemicalTankBuilder.SLURRY.createAllValid(Long.MAX_VALUE, cache));
         }

         protected List<ISlurryTank> containerList(ISlurryTracker tracker) {
            return tracker.getSlurryTanks(null);
         }

         public void sync(ISlurryTank cache, ISlurryTank data) {
            cache.setStack(data.getStack());
         }
      };
      public static final MultiblockCache.CacheSubstance<IMekanismStrictEnergyHandler, IEnergyContainer> ENERGY = new MultiblockCache.CacheSubstance<IMekanismStrictEnergyHandler, IEnergyContainer>(
         "EnergyContainers"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, cache));
         }

         protected List<IEnergyContainer> containerList(IMekanismStrictEnergyHandler handler) {
            return handler.getEnergyContainers(null);
         }

         public void sync(IEnergyContainer cache, IEnergyContainer data) {
            cache.setEnergy(data.getEnergy());
         }
      };
      public static final MultiblockCache.CacheSubstance<IMekanismHeatHandler, IHeatCapacitor> HEAT = new MultiblockCache.CacheSubstance<IMekanismHeatHandler, IHeatCapacitor>(
         "HeatCapacitors"
      ) {
         @Override
         protected void defaultPrefab(MultiblockCache<?> cache) {
            cache.heatCapacitors.add(BasicHeatCapacitor.create(1.0, null, cache));
         }

         protected List<IHeatCapacitor> containerList(IMekanismHeatHandler handler) {
            return handler.getHeatCapacitors(null);
         }

         public void sync(IHeatCapacitor cache, IHeatCapacitor data) {
            cache.setHeat(data.getHeat());
            if (cache instanceof BasicHeatCapacitor heatCapacitor) {
               heatCapacitor.setHeatCapacity(data.getHeatCapacity(), false);
            }
         }
      };
      public static final MultiblockCache.CacheSubstance<?, INBTSerializable<CompoundTag>>[] VALUES = new MultiblockCache.CacheSubstance[]{
         ITEMS, FLUID, GAS, INFUSION, PIGMENT, SLURRY, ENERGY, HEAT
      };
      private final String tagKey;

      public CacheSubstance(String tagKey) {
         this.tagKey = tagKey;
      }

      protected abstract void defaultPrefab(MultiblockCache<?> cache);

      protected abstract List<ELEMENT> containerList(HANDLER handler);

      private void prefab(MultiblockCache<?> cache, int count) {
         for (int i = 0; i < count; i++) {
            this.defaultPrefab(cache);
         }
      }

      public List<ELEMENT> getContainerList(Object holder) {
         return this.containerList((HANDLER)holder);
      }

      public abstract void sync(ELEMENT cache, ELEMENT data);

      public void preHandleMerge(MultiblockCache<?> cache, MultiblockCache<?> merge) {
         int diff = this.getContainerList(merge).size() - this.getContainerList(cache).size();
         if (diff > 0) {
            this.prefab(cache, diff);
         }
      }

      public String getTagKey() {
         return this.tagKey;
      }
   }

   public static class RejectContents {
      public final List<ItemStack> rejectedItems = new ArrayList<>();
      public final List<FluidStack> rejectedFluids = new ArrayList<>();
      public final List<GasStack> rejectedGases = new ArrayList<>();
      public final List<InfusionStack> rejectedInfuseTypes = new ArrayList<>();
      public final List<PigmentStack> rejectedPigments = new ArrayList<>();
      public final List<SlurryStack> rejectedSlurries = new ArrayList<>();
   }
}
