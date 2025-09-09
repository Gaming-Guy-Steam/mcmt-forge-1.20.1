package mekanism.common.content.boiler;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Optional;
import java.util.Set;
import mekanism.common.MekanismLang;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityPressureDisperser;
import mekanism.common.tile.multiblock.TileEntitySuperheatingElement;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class BoilerValidator extends CuboidStructureValidator<BoilerMultiblockData> {
   @Override
   protected FormationProtocol.CasingType getCasingType(BlockState state) {
      Block block = state.m_60734_();
      if (BlockType.is(block, MekanismBlockTypes.BOILER_CASING)) {
         return FormationProtocol.CasingType.FRAME;
      } else {
         return BlockType.is(block, MekanismBlockTypes.BOILER_VALVE) ? FormationProtocol.CasingType.VALVE : FormationProtocol.CasingType.INVALID;
      }
   }

   @Override
   protected boolean validateInner(BlockState state, Long2ObjectMap<ChunkAccess> chunkMap, BlockPos pos) {
      return super.validateInner(state, chunkMap, pos)
         ? true
         : BlockType.is(state.m_60734_(), MekanismBlockTypes.PRESSURE_DISPERSER, MekanismBlockTypes.SUPERHEATING_ELEMENT);
   }

   public FormationProtocol.FormationResult postcheck(BoilerMultiblockData structure, Long2ObjectMap<ChunkAccess> chunkMap) {
      Set<BlockPos> dispersers = new ObjectOpenHashSet();
      Set<BlockPos> elements = new ObjectOpenHashSet();

      for (BlockPos pos : structure.internalLocations) {
         BlockEntity tile = WorldUtils.getTileEntity(this.world, chunkMap, pos);
         if (tile instanceof TileEntityPressureDisperser) {
            dispersers.add(pos);
         } else if (tile instanceof TileEntitySuperheatingElement) {
            elements.add(pos);
         }
      }

      if (dispersers.isEmpty()) {
         return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_NO_DISPERSER);
      } else if (elements.isEmpty()) {
         return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
      } else {
         BlockPos initDisperser = dispersers.iterator().next();
         MutableBlockPos mutablePos = new MutableBlockPos();

         for (int x = 1; x < structure.length() - 1; x++) {
            for (int z = 1; z < structure.width() - 1; z++) {
               mutablePos.m_122178_(structure.renderLocation.m_123341_() + x, initDisperser.m_123342_(), structure.renderLocation.m_123343_() + z);
               TileEntityPressureDisperser tile = WorldUtils.getTileEntity(TileEntityPressureDisperser.class, this.world, chunkMap, mutablePos);
               if (tile == null) {
                  return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_MISSING_DISPERSER, mutablePos);
               }

               dispersers.remove(mutablePos);
            }
         }

         if (!dispersers.isEmpty()) {
            return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_EXTRA_DISPERSER);
         } else {
            structure.superheatingElements = FormationProtocol.explore(
               elements.iterator().next(),
               coord -> coord.m_123342_() < initDisperser.m_123342_()
                  && WorldUtils.getTileEntity(TileEntitySuperheatingElement.class, this.world, chunkMap, coord) != null
            );
            if (elements.size() > structure.superheatingElements) {
               return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_SUPERHEATING);
            } else {
               BlockPos initAir = null;
               MutableBlockPos mutableAir = new MutableBlockPos();
               int totalAir = 0;

               for (int x = structure.renderLocation.m_123341_(); x < structure.renderLocation.m_123341_() + structure.length(); x++) {
                  for (int y = structure.renderLocation.m_123342_(); y < initDisperser.m_123342_(); y++) {
                     for (int z = structure.renderLocation.m_123343_(); z < structure.renderLocation.m_123343_() + structure.width(); z++) {
                        mutableAir.m_122178_(x, y, z);
                        if (this.isAirOrFrame(chunkMap, mutableAir)) {
                           initAir = mutableAir.m_7949_();
                           totalAir++;
                        }
                     }
                  }
               }

               BlockPos renderLocation = structure.renderLocation;
               int volLength = structure.length();
               int volWidth = structure.width();
               structure.setWaterVolume(
                  FormationProtocol.explore(
                     initAir,
                     coord -> coord.m_123342_() >= renderLocation.m_123342_() - 1
                        && coord.m_123342_() < initDisperser.m_123342_()
                        && coord.m_123341_() >= renderLocation.m_123341_()
                        && coord.m_123341_() < renderLocation.m_123341_() + volLength
                        && coord.m_123343_() >= renderLocation.m_123343_()
                        && coord.m_123343_() < renderLocation.m_123343_() + volWidth
                        && this.isAirOrFrame(chunkMap, coord)
                  )
               );
               if (totalAir > structure.getWaterVolume()) {
                  return FormationProtocol.FormationResult.fail(MekanismLang.BOILER_INVALID_AIR_POCKETS);
               } else {
                  int steamHeight = structure.renderLocation.m_123342_() + structure.height() - 2 - initDisperser.m_123342_();
                  structure.setSteamVolume(structure.width() * structure.length() * steamHeight);
                  structure.upperRenderLocation = new BlockPos(
                     structure.renderLocation.m_123341_(), initDisperser.m_123342_() + 1, structure.renderLocation.m_123343_()
                  );
                  return FormationProtocol.FormationResult.SUCCESS;
               }
            }
         }
      }
   }

   private boolean isAirOrFrame(Long2ObjectMap<ChunkAccess> chunkMap, BlockPos airPos) {
      Optional<BlockState> stateOptional = WorldUtils.getBlockState(this.world, chunkMap, airPos);
      return stateOptional.isPresent() && stateOptional.get().m_60795_() || this.isFrameCompatible(WorldUtils.getTileEntity(this.world, chunkMap, airPos));
   }
}
