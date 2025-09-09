package mekanism.common.tile.prefab;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import mekanism.api.IConfigurable;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public abstract class TileEntityStructuralMultiblock extends TileEntityMekanism implements IStructuralMultiblock, IConfigurable {
   private final Map<MultiblockManager<?>, Structure> structures = new HashMap<>();
   private final Structure invalidStructure = Structure.INVALID;
   private final MultiblockData defaultMultiblock = new MultiblockData(this);
   private String clientActiveMultiblock = null;

   public TileEntityStructuralMultiblock(IBlockProvider provider, BlockPos pos, BlockState state) {
      super(provider, pos, state);
      this.addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIGURABLE, this));
   }

   @Override
   public MultiblockData getDefaultData() {
      return this.defaultMultiblock;
   }

   @Override
   public void setStructure(MultiblockManager<?> manager, Structure structure) {
      this.structures.put(manager, structure);
   }

   @Override
   public Structure getStructure(MultiblockManager<?> manager) {
      return this.structures.getOrDefault(manager, this.invalidStructure);
   }

   @Override
   public boolean hasStructure(Structure structure) {
      return this.structures.get(structure.getManager()) == structure;
   }

   @Override
   public boolean hasFormedMultiblock() {
      return this.clientActiveMultiblock != null;
   }

   @Override
   public boolean structuralGuiAccessAllowed() {
      return this.hasFormedMultiblock() && this.structuralGuiAccessAllowed(this.clientActiveMultiblock);
   }

   protected boolean structuralGuiAccessAllowed(@NotNull String multiblock) {
      return !multiblock.contains("fusion") && !multiblock.contains("evaporation");
   }

   @Override
   public Map<MultiblockManager<?>, Structure> getStructureMap() {
      return this.structures;
   }

   private MultiblockData getMultiblockData(Structure structure) {
      MultiblockData data = structure.getMultiblockData();
      return data != null && data.isFormed() ? data : this.getDefaultData();
   }

   @Override
   protected void onUpdateServer() {
      super.onUpdateServer();
      if (this.ticker % 10 == 0) {
         String activeMultiblock = null;
         if (!this.structures.isEmpty()) {
            Iterator<Entry<MultiblockManager<?>, Structure>> iterator = this.structures.entrySet().iterator();

            while (iterator.hasNext()) {
               Entry<MultiblockManager<?>, Structure> entry = iterator.next();
               Structure structure = entry.getValue();
               if (structure.isValid()) {
                  if (activeMultiblock == null && structure.getController() != null && this.getMultiblockData(structure).isFormed()) {
                     activeMultiblock = entry.getKey().getNameLower();
                  }
               } else {
                  iterator.remove();
               }
            }
         }

         if (this.ticker >= 3 && this.structures.isEmpty()) {
            this.invalidStructure.tick(this, true);

            for (Entry<MultiblockManager<?>, Structure> entry : this.structures.entrySet()) {
               Structure structure = entry.getValue();
               if (structure.getController() != null && this.getMultiblockData(structure).isFormed()) {
                  activeMultiblock = entry.getKey().getNameLower();
                  break;
               }
            }
         }

         if (!Objects.equals(activeMultiblock, this.clientActiveMultiblock)) {
            this.clientActiveMultiblock = activeMultiblock;
            this.sendUpdatePacket();
         }
      }
   }

   @Override
   public InteractionResult onActivate(Player player, InteractionHand hand, ItemStack stack) {
      for (Entry<MultiblockManager<?>, Structure> entry : this.structures.entrySet()) {
         Structure structure = entry.getValue();
         IMultiblock<?> master = structure.getController();
         if (master != null) {
            MultiblockData data = this.getMultiblockData(structure);
            if (data.isFormed()
               && this.structuralGuiAccessAllowed(entry.getKey().getNameLower())
               && data.getBounds().getRelativeLocation(this.m_58899_()).isWall()) {
               return master.onActivate(player, hand, stack);
            }
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public void onNeighborChange(Block block, BlockPos neighborPos) {
      super.onNeighborChange(block, neighborPos);
      if (!this.isRemote()) {
         for (Structure s : this.structures.values()) {
            if (s.getController() != null) {
               MultiblockData multiblockData = this.getMultiblockData(s);
               if (multiblockData.isPositionInsideBounds(s, neighborPos)
                  && (this.f_58857_.m_46859_(neighborPos) || !multiblockData.internalLocations.contains(neighborPos))) {
                  s.markForUpdate(this.f_58857_, true);
               }
            }
         }
      }
   }

   @Override
   public InteractionResult onRightClick(Player player) {
      if (!this.isRemote()) {
         for (Structure s : this.structures.values()) {
            if (s.getController() != null && !this.getMultiblockData(s).isFormed()) {
               FormationProtocol.FormationResult result = s.runUpdate(this);
               if (!result.isFormed() && result.getResultText() != null) {
                  player.m_213846_(result.getResultText());
                  return InteractionResult.m_19078_(this.isRemote());
               }
            }
         }
      }

      return InteractionResult.PASS;
   }

   @Override
   public InteractionResult onSneakRightClick(Player player) {
      return InteractionResult.PASS;
   }

   @NotNull
   @Override
   public CompoundTag getReducedUpdateTag() {
      CompoundTag updateTag = super.getReducedUpdateTag();
      if (this.clientActiveMultiblock != null) {
         updateTag.m_128359_("activeState", this.clientActiveMultiblock);
      }

      return updateTag;
   }

   @Override
   public void handleUpdateTag(@NotNull CompoundTag tag) {
      super.handleUpdateTag(tag);
      this.clientActiveMultiblock = tag.m_128425_("activeState", 8) ? tag.m_128461_("activeState") : null;
   }

   @Override
   public void m_7651_() {
      super.m_7651_();
      if (!this.isRemote()) {
         this.structures.values().forEach(s -> s.invalidate(this.f_58857_));
      }
   }

   @Override
   public boolean shouldDumpRadiation() {
      return false;
   }
}
