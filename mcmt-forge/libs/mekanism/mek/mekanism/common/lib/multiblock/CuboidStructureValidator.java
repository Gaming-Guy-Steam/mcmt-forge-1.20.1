package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Optional;
import java.util.UUID;
import mekanism.common.MekanismLang;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class CuboidStructureValidator<T extends MultiblockData> implements IStructureValidator<T> {
   private final VoxelCuboid minBounds;
   private final VoxelCuboid maxBounds;
   protected VoxelCuboid cuboid;
   protected Structure structure;
   protected Level world;
   protected MultiblockManager<T> manager;

   public CuboidStructureValidator() {
      this(new VoxelCuboid(3, 3, 3), new VoxelCuboid(18, 18, 18));
   }

   public CuboidStructureValidator(VoxelCuboid minBounds, VoxelCuboid maxBounds) {
      this.minBounds = minBounds;
      this.maxBounds = maxBounds;
   }

   @Override
   public void init(Level world, MultiblockManager<T> manager, Structure structure) {
      this.world = world;
      this.manager = manager;
      this.structure = structure;
   }

   @Override
   public FormationProtocol.FormationResult validate(FormationProtocol<T> ctx, Long2ObjectMap<ChunkAccess> chunkMap) {
      BlockPos min = this.cuboid.getMinPos();
      BlockPos max = this.cuboid.getMaxPos();
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = min.m_123341_(); x <= max.m_123341_(); x++) {
         for (int y = min.m_123342_(); y <= max.m_123342_(); y++) {
            for (int z = min.m_123343_(); z <= max.m_123343_(); z++) {
               mutablePos.m_122178_(x, y, z);
               FormationProtocol.FormationResult ret = this.validateNode(ctx, chunkMap, mutablePos);
               if (!ret.isFormed()) {
                  return ret;
               }
            }
         }
      }

      return FormationProtocol.FormationResult.SUCCESS;
   }

   protected FormationProtocol.FormationResult validateNode(FormationProtocol<T> ctx, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
      Optional<BlockState> optionalState = WorldUtils.getBlockState(this.world, chunkMap, pos);
      if (optionalState.isEmpty()) {
         return FormationProtocol.FormationResult.FAIL;
      } else {
         BlockState state = optionalState.get();
         FormationProtocol.StructureRequirement requirement = this.getStructureRequirement(pos);
         if (requirement.isCasing()) {
            FormationProtocol.CasingType type = this.getCasingType(state);
            FormationProtocol.FormationResult ret = this.validateFrame(ctx, pos, state, type, requirement.needsFrame());
            if ((requirement != FormationProtocol.StructureRequirement.IGNORED || ret.isNoIgnore()) && !ret.isFormed()) {
               return ret;
            }
         } else {
            if (!this.validateInner(state, chunkMap, pos)) {
               return FormationProtocol.FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_INNER, pos);
            }

            if (!state.m_60795_()) {
               ctx.internalLocations.add(pos.m_7949_());
            }
         }

         return FormationProtocol.FormationResult.SUCCESS;
      }
   }

   protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
      return state.m_60795_();
   }

   protected abstract FormationProtocol.CasingType getCasingType(BlockState state);

   protected boolean isFrameCompatible(BlockEntity tile) {
      return tile instanceof IStructuralMultiblock multiblock && multiblock.canInterface(this.manager) ? true : this.manager.isCompatible(tile);
   }

   protected FormationProtocol.FormationResult validateFrame(
      FormationProtocol<T> ctx, BlockPos pos, BlockState state, FormationProtocol.CasingType type, boolean needsFrame
   ) {
      IMultiblockBase tile = this.structure.getTile(pos);
      if (this.isFrameCompatible((BlockEntity)tile) && (!needsFrame || type.isFrame())) {
         if (tile instanceof IMultiblock<?> multiblockTile) {
            UUID uuid = multiblockTile.getCacheID();
            if (uuid != null && multiblockTile.getManager() == this.manager) {
               MultiblockCache<T> cache = this.manager.getCache(uuid);
               if (cache == null) {
                  multiblockTile.resetCache();
               } else {
                  ctx.idsFound.put(uuid, cache);
               }
            }
         }

         pos = pos.m_7949_();
         ctx.locations.add(pos);
         if (type.isValve()) {
            IValveHandler.ValveData data = new IValveHandler.ValveData(pos, this.getSide(pos));
            ctx.valves.add(data);
         }

         return FormationProtocol.FormationResult.SUCCESS;
      } else {
         return FormationProtocol.FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_FRAME, pos);
      }
   }

   @Override
   public FormationProtocol.FormationResult postcheck(T structure, Long2ObjectMap<ChunkAccess> chunkMap) {
      return FormationProtocol.FormationResult.SUCCESS;
   }

   protected FormationProtocol.StructureRequirement getStructureRequirement(BlockPos pos) {
      VoxelCuboid.WallRelative relative = this.cuboid.getWallRelative(pos);
      if (relative.isOnEdge()) {
         return FormationProtocol.StructureRequirement.FRAME;
      } else {
         return relative.isWall() ? FormationProtocol.StructureRequirement.OTHER : FormationProtocol.StructureRequirement.INNER;
      }
   }

   protected Direction getSide(BlockPos pos) {
      return this.cuboid.getSide(pos);
   }

   @Override
   public IShape getShape() {
      return this.cuboid;
   }

   @Override
   public boolean precheck() {
      this.cuboid = StructureHelper.fetchCuboid(this.structure, this.minBounds, this.maxBounds);
      return this.cuboid != null;
   }

   public void loadCuboid(VoxelCuboid cuboid) {
      this.cuboid = cuboid;
   }
}
