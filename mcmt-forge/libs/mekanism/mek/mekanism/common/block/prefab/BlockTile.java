package mekanism.common.block.prefab;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockTile<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockBase<TYPE> implements IHasTileEntity<TILE> {
   public BlockTile(TYPE type, UnaryOperator<Properties> propertiesModifier) {
      this(type, propertiesModifier.apply(Properties.m_284310_().m_60913_(3.5F, 16.0F).m_60999_()));
   }

   public BlockTile(TYPE type, Properties properties) {
      super(type, properties);
   }

   @Override
   public TileEntityTypeRegistryObject<TILE> getTileType() {
      return this.type.getTileType();
   }

   @Deprecated
   @NotNull
   @Override
   public InteractionResult m_6227_(
      @NotNull BlockState state,
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
      if (tile == null) {
         return InteractionResult.PASS;
      } else if (world.f_46443_) {
         return this.genericClientActivated(player, hand);
      } else if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
         return InteractionResult.SUCCESS;
      } else {
         return this.type.has(AttributeGui.class) ? tile.openGui(player) : InteractionResult.PASS;
      }
   }

   @Override
   protected float getDestroyProgress(
      @NotNull BlockState state, @NotNull Player player, @NotNull BlockGetter world, @NotNull BlockPos pos, @Nullable BlockEntity tile
   ) {
      return ISecurityUtils.INSTANCE.canAccess(player, tile) ? super.getDestroyProgress(state, player, world, pos, tile) : 0.0F;
   }

   @Override
   public void m_214162_(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
      super.m_214162_(state, world, pos, random);
      if (MekanismConfig.client.machineEffects.get()) {
         AttributeParticleFX particleFX = this.type.get(AttributeParticleFX.class);
         if (particleFX != null && Attribute.isActive(state)) {
            Direction facing = Attribute.getFacing(state);

            for (Function<RandomSource, AttributeParticleFX.Particle> particleFunction : particleFX.getParticleFunctions()) {
               AttributeParticleFX.Particle particle = particleFunction.apply(random);
               Vec3 particlePos = particle.getPos();
               if (facing == Direction.WEST) {
                  particlePos = particlePos.m_82524_(90.0F);
               } else if (facing == Direction.EAST) {
                  particlePos = particlePos.m_82524_(270.0F);
               } else if (facing == Direction.NORTH) {
                  particlePos = particlePos.m_82524_(180.0F);
               }

               particlePos = particlePos.m_82520_(pos.m_123341_() + 0.5, pos.m_123342_() + 0.5, pos.m_123343_() + 0.5);
               world.m_7106_(particle.getType(), particlePos.f_82479_, particlePos.f_82480_, particlePos.f_82481_, 0.0, 0.0, 0.0);
            }
         }
      }
   }

   @Deprecated
   public void m_6861_(
      @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean isMoving
   ) {
      if (!world.f_46443_) {
         TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
         if (tile != null) {
            tile.onNeighborChange(neighborBlock, neighborPos);
         }
      }
   }

   @Deprecated
   public boolean m_7899_(@NotNull BlockState state) {
      return this.type.has(Attributes.AttributeRedstoneEmitter.class);
   }

   public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, Direction side) {
      return this.type.has(Attributes.AttributeRedstoneEmitter.class) || super.canConnectRedstone(state, world, pos, side);
   }

   @Deprecated
   public int m_6378_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
      Attributes.AttributeRedstoneEmitter<TileEntityMekanism> redstoneEmitter = this.type.get(Attributes.AttributeRedstoneEmitter.class);
      if (redstoneEmitter != null) {
         TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
         if (tile != null) {
            return redstoneEmitter.getRedstoneLevel(tile, side.m_122424_());
         }
      }

      return super.m_6378_(state, world, pos, side);
   }

   public static class BlockTileModel<TILE extends TileEntityMekanism, BLOCK extends BlockTypeTile<TILE>>
      extends BlockTile<TILE, BLOCK>
      implements IStateFluidLoggable {
      public BlockTileModel(BLOCK type, UnaryOperator<Properties> propertiesModifier) {
         super(type, propertiesModifier);
      }

      public BlockTileModel(BLOCK type, Properties properties) {
         super(type, properties);
      }
   }
}
