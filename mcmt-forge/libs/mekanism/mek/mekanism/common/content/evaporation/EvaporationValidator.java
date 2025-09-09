package mekanism.common.content.evaporation;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.EnumSet;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.StructureHelper;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.multiblock.TileEntityThermalEvaporationController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class EvaporationValidator extends CuboidStructureValidator<EvaporationMultiblockData> {
   private static final VoxelCuboid MIN_CUBOID = new VoxelCuboid(4, 3, 4);
   private static final VoxelCuboid MAX_CUBOID = new VoxelCuboid(4, 18, 4);
   private boolean foundController = false;

   @Override
   protected FormationProtocol.FormationResult validateFrame(
      FormationProtocol<EvaporationMultiblockData> ctx, BlockPos pos, BlockState state, FormationProtocol.CasingType type, boolean needsFrame
   ) {
      boolean controller = this.structure.getTile(pos) instanceof TileEntityThermalEvaporationController;
      if (this.foundController && controller) {
         return FormationProtocol.FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_CONTROLLER_CONFLICT, pos, true);
      } else {
         this.foundController |= controller;
         return super.validateFrame(ctx, pos, state, type, needsFrame);
      }
   }

   @Override
   protected FormationProtocol.StructureRequirement getStructureRequirement(BlockPos pos) {
      VoxelCuboid.WallRelative relative = this.cuboid.getWallRelative(pos);
      if (pos.m_123342_() == this.cuboid.getMaxPos().m_123342_()) {
         if (relative.isOnCorner()) {
            return FormationProtocol.StructureRequirement.IGNORED;
         } else {
            return !relative.isOnEdge() ? FormationProtocol.StructureRequirement.INNER : FormationProtocol.StructureRequirement.OTHER;
         }
      } else {
         return super.getStructureRequirement(pos);
      }
   }

   @Override
   protected FormationProtocol.CasingType getCasingType(BlockState state) {
      Block block = state.m_60734_();
      if (BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_BLOCK)) {
         return FormationProtocol.CasingType.FRAME;
      } else if (BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_VALVE)) {
         return FormationProtocol.CasingType.VALVE;
      } else {
         return BlockType.is(block, MekanismBlockTypes.THERMAL_EVAPORATION_CONTROLLER)
            ? FormationProtocol.CasingType.OTHER
            : FormationProtocol.CasingType.INVALID;
      }
   }

   @Override
   public boolean precheck() {
      this.cuboid = StructureHelper.fetchCuboid(this.structure, MIN_CUBOID, MAX_CUBOID, EnumSet.complementOf(EnumSet.of(VoxelCuboid.CuboidSide.TOP)), 8);
      return this.cuboid != null;
   }

   public FormationProtocol.FormationResult postcheck(EvaporationMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
      return !this.foundController
         ? FormationProtocol.FormationResult.fail(MekanismLang.MULTIBLOCK_INVALID_NO_CONTROLLER)
         : FormationProtocol.FormationResult.SUCCESS;
   }
}
