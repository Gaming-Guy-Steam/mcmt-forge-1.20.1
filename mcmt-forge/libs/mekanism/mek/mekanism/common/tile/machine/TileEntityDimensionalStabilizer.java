package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.FixedUsageEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader, ISustainedData, IHasVisualization {
   public static final int MAX_LOAD_RADIUS = 2;
   public static final int MAX_LOAD_DIAMETER = 5;
   private static final String COMPUTER_RANGE_STR = "Range: [-2, 2]";
   private static final String COMPUTER_RANGE_RAD = "Range: [1, 2]";
   private static final BiFunction<FloatingLong, TileEntityDimensionalStabilizer, FloatingLong> BASE_ENERGY_CALCULATOR = (base, tile) -> base.multiply(
      (long)tile.chunksLoaded
   );
   private final TileEntityDimensionalStabilizer.ChunkLoader chunkLoaderComponent;
   private final boolean[][] loadingChunks;
   @SyntheticComputerMethod(
      getter = "getChunksLoaded",
      getterDescription = "Get the number of chunks being loaded."
   )
   private int chunksLoaded = 1;
   private boolean clientRendering;
   private FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> energyContainer;
   @WrappingComputerMethod(
      wrapper = SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper.class,
      methodNames = {"getEnergyItem"},
      docPlaceholder = "energy slot"
   )
   EnergyInventorySlot energySlot;

   public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
      super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));
      this.chunkLoaderComponent = new TileEntityDimensionalStabilizer.ChunkLoader(this);
      this.loadingChunks = new boolean[5][5];
      this.loadingChunks[2][2] = true;
   }

   @NotNull
   @Override
   protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
      EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
      builder.addContainer(this.energyContainer = FixedUsageEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, listener));
      return builder.build();
   }

   @NotNull
   @Override
   protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
      InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
      builder.addSlot(this.energySlot = EnergyInventorySlot.fillOrConvert(this.energyContainer, this::m_58904_, listener, 143, 35), RelativeSide.BACK);
      return builder.build();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      this.energySlot.fillContainerOrConvert();
      if (MekanismConfig.general.allowChunkloading.get() && MekanismUtils.canFunction(this)) {
         FloatingLong energyPerTick = this.energyContainer.getEnergyPerTick();
         if (this.energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
            this.energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
            this.setActive(true);
         } else {
            this.setActive(false);
         }
      } else {
         this.setActive(false);
      }
   }

   public boolean isChunkLoadingAt(int x, int z) {
      return this.loadingChunks[x][z];
   }

   public void toggleChunkLoadingAt(int x, int z) {
      if (x >= 0 && x < 5 && z >= 0 && z < 5 && this.setChunkLoadingAt(x, z, !this.isChunkLoadingAt(x, z))) {
         this.setChanged(false);
         this.energyContainer.updateEnergyPerTick();
         this.getChunkLoader().refreshChunkTickets();
      }
   }

   public void adjustChunkLoadingRadius(int radius, boolean load) {
      if (radius > 0 && radius <= 2) {
         boolean changed = false;

         for (int x = -radius; x <= radius; x++) {
            boolean skipInner = x > -radius && x < radius;
            int actualX = x + 2;

            for (int z = -radius; z <= radius; z += skipInner ? 2 * radius : 1) {
               if (this.setChunkLoadingAt(actualX, z + 2, load)) {
                  changed = true;
               }
            }
         }

         if (changed) {
            this.setChanged(false);
            this.energyContainer.updateEnergyPerTick();
            this.getChunkLoader().refreshChunkTickets();
         }
      }
   }

   private boolean setChunkLoadingAt(int x, int z, boolean load) {
      if (x == 2 && z == 2) {
         return false;
      } else if (this.isChunkLoadingAt(x, z) != load) {
         this.loadingChunks[x][z] = load;
         if (load) {
            this.chunksLoaded++;
         } else {
            this.chunksLoaded--;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public TileComponentChunkLoader<TileEntityDimensionalStabilizer> getChunkLoader() {
      return this.chunkLoaderComponent;
   }

   @Override
   public Set<ChunkPos> getChunkSet() {
      Set<ChunkPos> chunkSet = new HashSet<>();
      int chunkX = SectionPos.m_123171_(this.f_58858_.m_123341_());
      int chunkZ = SectionPos.m_123171_(this.f_58858_.m_123343_());

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            if (this.isChunkLoadingAt(x + 2, z + 2)) {
               chunkSet.add(new ChunkPos(chunkX + x, chunkZ + z));
            }
         }
      }

      return chunkSet;
   }

   @Override
   public int getRedstoneLevel() {
      return this.getActive() ? 15 : 0;
   }

   @Override
   protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
      return false;
   }

   @Override
   public int getCurrentRedstoneLevel() {
      return this.getRedstoneLevel();
   }

   @NotNull
   public AABB getRenderBoundingBox() {
      if (this.isClientRendering() && this.canDisplayVisuals() && this.f_58857_ != null) {
         int chunkX = SectionPos.m_123171_(this.f_58858_.m_123341_());
         int chunkZ = SectionPos.m_123171_(this.f_58858_.m_123343_());
         ChunkPos minChunk = new ChunkPos(chunkX - 2, chunkZ - 2);
         ChunkPos maxChunk = new ChunkPos(chunkX + 2, chunkZ + 2);
         return new AABB(
            minChunk.m_45604_(), this.f_58857_.m_141937_(), minChunk.m_45605_(), maxChunk.m_45608_() + 1, this.f_58857_.m_151558_(), maxChunk.m_45609_() + 1
         );
      } else {
         return super.getRenderBoundingBox();
      }
   }

   @Override
   public boolean isClientRendering() {
      return this.clientRendering;
   }

   @Override
   public void toggleClientRendering() {
      this.clientRendering = !this.clientRendering;
   }

   @Override
   public void addContainerTrackers(MekanismContainer container) {
      super.addContainerTrackers(container);
      container.trackArray(this.loadingChunks);
   }

   @Override
   public void writeSustainedData(CompoundTag dataMap) {
      byte[] chunksToLoad = new byte[25];

      for (int x = 0; x < 5; x++) {
         for (int z = 0; z < 5; z++) {
            chunksToLoad[x * 5 + z] = (byte)(this.isChunkLoadingAt(x, z) ? 1 : 0);
         }
      }

      dataMap.m_128382_("stabilizerChunksToLoad", chunksToLoad);
   }

   @Override
   public void readSustainedData(CompoundTag dataMap) {
      boolean changed = false;
      int lastChunksLoaded = this.chunksLoaded;
      byte[] chunksToLoad = dataMap.m_128463_("stabilizerChunksToLoad");
      if (chunksToLoad.length != 25) {
         chunksToLoad = new byte[25];
      }

      for (int x = 0; x < 5; x++) {
         for (int z = 0; z < 5; z++) {
            changed |= this.setChunkLoadingAt(x, z, chunksToLoad[x * 5 + z] == 1);
         }
      }

      if (changed) {
         if (this.chunksLoaded != lastChunksLoaded) {
            this.energyContainer.updateEnergyPerTick();
         }

         if (this.m_58898_()) {
            this.getChunkLoader().refreshChunkTickets();
         }
      }
   }

   @Override
   public Map<String, String> getTileDataRemap() {
      Map<String, String> remap = new Object2ObjectOpenHashMap();
      remap.put("stabilizerChunksToLoad", "stabilizerChunksToLoad");
      return remap;
   }

   @Override
   public void configurationDataSet() {
      super.configurationDataSet();
      this.getChunkLoader().refreshChunkTickets();
   }

   public FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> getEnergyContainer() {
      return this.energyContainer;
   }

   private int validateDimension(int val, boolean x) throws ComputerException {
      if (val >= -2 && val <= 2) {
         return val + 2;
      } else {
         throw new ComputerException("%s offset '%d' is not in range, must be between %d and %d inclusive.", x ? "X" : "Z", val, -2, 2);
      }
   }

   @ComputerMethod(
      nameOverride = "isChunkLoadingAt",
      methodDescription = "Check if the Dimensional Stabilizer is configured to load a the specified relative chunk position at x,y (Stabilizer is at 0,0). Range: [-2, 2]"
   )
   boolean computerIsChunkloadingAt(int x, int z) throws ComputerException {
      return this.isChunkLoadingAt(this.validateDimension(x, true), this.validateDimension(z, false));
   }

   @ComputerMethod(
      nameOverride = "toggleChunkLoadingAt",
      requiresPublicSecurity = true,
      methodDescription = "Toggle loading the specified relative chunk at the relative x,y position (Stabilizer is at 0,0). Just like clicking the button in the GUI. Range: [-2, 2]"
   )
   void computerToggleChunkLoadingAt(int x, int z) throws ComputerException {
      this.validateSecurityIsPublic();
      this.toggleChunkLoadingAt(this.validateDimension(x, true), this.validateDimension(z, false));
   }

   @ComputerMethod(
      nameOverride = "setChunkLoadingAt",
      requiresPublicSecurity = true,
      methodDescription = "Set if the Dimensional Stabilizer is configured to load a the specified relative position (Stabilizer is at 0,0). True = load the chunk, false = don't load the chunk. Range: [-2, 2]"
   )
   void computerSetChunkLoadingAt(int x, int z, boolean load) throws ComputerException {
      this.validateSecurityIsPublic();
      if (this.setChunkLoadingAt(this.validateDimension(x, true), this.validateDimension(z, false), load)) {
         this.setChanged(false);
         this.energyContainer.updateEnergyPerTick();
         this.getChunkLoader().refreshChunkTickets();
      }
   }

   private void validateRadius(int radius) throws ComputerException {
      if (radius <= 0 || radius > 2) {
         throw new ComputerException("Radius '%d' is not in range, must be between 1 and %d inclusive.", radius, 2);
      }
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Sets the chunks in the specified radius to be loaded. The chunk the Stabilizer is in is always loaded. Range: [1, 2]"
   )
   void enableChunkLoadingFor(int radius) throws ComputerException {
      this.validateSecurityIsPublic();
      this.validateRadius(radius);
      this.adjustChunkLoadingRadius(radius, true);
   }

   @ComputerMethod(
      requiresPublicSecurity = true,
      methodDescription = "Sets the chunks in the specified radius to not be kept loaded. The chunk the Stabilizer is in is always loaded. Range: [1, 2]"
   )
   void disableChunkLoadingFor(int radius) throws ComputerException {
      this.validateSecurityIsPublic();
      this.validateRadius(radius);
      this.adjustChunkLoadingRadius(radius, false);
   }

   private class ChunkLoader extends TileComponentChunkLoader<TileEntityDimensionalStabilizer> {
      public ChunkLoader(TileEntityDimensionalStabilizer tile) {
         super(tile, true);
      }

      @Override
      public boolean canOperate() {
         return MekanismConfig.general.allowChunkloading.get() && TileEntityDimensionalStabilizer.this.getActive();
      }
   }
}
