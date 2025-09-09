package mekanism.common.content.sps;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumSet;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.common.registries.MekanismBlockTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SPSValidator extends CuboidStructureValidator<SPSMultiblockData> {
   private static final VoxelCuboid BOUNDS = new VoxelCuboid(7, 7, 7);
   private static final byte[][] ALLOWED_GRID = new byte[][]{
      {0, 0, 1, 1, 1, 0, 0},
      {0, 1, 2, 2, 2, 1, 0},
      {1, 2, 2, 2, 2, 2, 1},
      {1, 2, 2, 2, 2, 2, 1},
      {1, 2, 2, 2, 2, 2, 1},
      {0, 1, 2, 2, 2, 1, 0},
      {0, 0, 1, 1, 1, 0, 0}
   };

   @Override
   protected FormationProtocol.StructureRequirement getStructureRequirement(BlockPos pos) {
      VoxelCuboid.WallRelative relative = this.cuboid.getWallRelative(pos);
      if (relative.isWall()) {
         Structure.Axis axis = Structure.Axis.get(this.cuboid.getSide(pos));
         Structure.Axis h = axis.horizontal();
         Structure.Axis v = axis.vertical();
         pos = pos.m_121996_(this.cuboid.getMinPos());
         return FormationProtocol.StructureRequirement.REQUIREMENTS[ALLOWED_GRID[h.getCoord(pos)][v.getCoord(pos)]];
      } else {
         return super.getStructureRequirement(pos);
      }
   }

   @Override
   protected FormationProtocol.CasingType getCasingType(BlockState state) {
      Block block = state.m_60734_();
      if (BlockType.is(block, MekanismBlockTypes.SPS_CASING)) {
         return FormationProtocol.CasingType.FRAME;
      } else {
         return BlockType.is(block, MekanismBlockTypes.SPS_PORT) ? FormationProtocol.CasingType.VALVE : FormationProtocol.CasingType.INVALID;
      }
   }

   @Override
   protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
      return super.validateInner(state, chunkMap, pos) ? true : BlockType.is(state.m_60734_(), MekanismBlockTypes.SUPERCHARGED_COIL);
   }

   @Override
   public boolean precheck() {
      this.cuboid = StructureHelper.fetchCuboid(this.structure, BOUNDS, BOUNDS, EnumSet.allOf(VoxelCuboid.CuboidSide.class), 72);
      return this.cuboid != null;
   }

   public FormationProtocol.FormationResult postcheck(SPSMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
      Set<BlockPos> validCoils = new ObjectOpenHashSet();

      for (IValveHandler.ValveData valve : structure.valves) {
         BlockPos pos = valve.location.m_121945_(valve.side.m_122424_());
         if (structure.internalLocations.contains(pos)) {
            structure.addCoil(valve.location, valve.side.m_122424_());
            validCoils.add(pos);
         }
      }

      return structure.internalLocations.size() != validCoils.size()
         ? FormationProtocol.FormationResult.fail(MekanismLang.SPS_INVALID_DISCONNECTED_COIL)
         : FormationProtocol.FormationResult.SUCCESS;
   }
}
