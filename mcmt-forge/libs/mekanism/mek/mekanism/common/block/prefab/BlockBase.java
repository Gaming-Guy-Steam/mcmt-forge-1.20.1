package mekanism.common.block.prefab;

import java.util.function.UnaryOperator;
import mekanism.api.text.ILangEntry;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.content.blocktype.BlockType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockBase<TYPE extends BlockType> extends BlockMekanism implements IHasDescription, ITypeBlock {
   protected final TYPE type;
   private static BlockType cacheType;

   public BlockBase(TYPE type, UnaryOperator<Properties> propertyModifier) {
      this(type, propertyModifier.apply(Properties.m_284310_().m_60999_()));
   }

   public BlockBase(TYPE type, Properties properties) {
      super(hack(type, properties));
      this.type = type;
   }

   private static <TYPE extends BlockType> Properties hack(TYPE type, Properties props) {
      cacheType = type;
      type.getAll().forEach(a -> a.adjustProperties(props));
      return props;
   }

   @Override
   public BlockType getType() {
      return this.type == null ? cacheType : this.type;
   }

   @NotNull
   @Override
   public ILangEntry getDescription() {
      return this.type.getDescription();
   }

   @NotNull
   public MutableComponent m_49954_() {
      BaseTier baseTier = Attribute.getBaseTier(this);
      return baseTier == null ? super.m_49954_() : TextComponentUtil.build(baseTier.getColor(), super.m_49954_());
   }

   public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
      Attributes.AttributeCustomResistance customResistance = this.type.get(Attributes.AttributeCustomResistance.class);
      return customResistance == null ? super.getExplosionResistance(state, world, pos, explosion) : customResistance.resistance();
   }

   @Deprecated
   public boolean m_7357_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull PathComputationType pathType) {
      return !this.type.has(AttributeCustomShape.class) && super.m_7357_(state, world, pos, pathType);
   }

   @Deprecated
   @NotNull
   public VoxelShape m_5940_(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
      AttributeCustomShape customShape = this.type.get(AttributeCustomShape.class);
      if (customShape != null) {
         VoxelShape[] bounds = customShape.bounds();
         if (bounds.length == 1) {
            return bounds[0];
         } else {
            AttributeStateFacing attr = this.type.get(AttributeStateFacing.class);
            int index = attr == null ? 0 : attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateProperties.f_61372_ ? 0 : 2);
            return bounds[index];
         }
      } else {
         return super.m_5940_(state, world, pos, context);
      }
   }

   @Deprecated
   @NotNull
   public InteractionResult m_6227_(
      @NotNull BlockState state,
      @NotNull Level world,
      @NotNull BlockPos pos,
      @NotNull Player player,
      @NotNull InteractionHand hand,
      @NotNull BlockHitResult hit
   ) {
      if (player.m_6144_() && MekanismUtils.canUseAsWrench(player.m_21120_(hand))) {
         if (!world.f_46443_) {
            WorldUtils.dismantleBlock(state, world, pos);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static class BlockBaseModel<BLOCK extends BlockType> extends BlockBase<BLOCK> implements IStateFluidLoggable {
      public BlockBaseModel(BLOCK blockType, UnaryOperator<Properties> propertyModifier) {
         super(blockType, propertyModifier);
      }

      public BlockBaseModel(BLOCK blockType, Properties properties) {
         super(blockType, properties);
      }
   }
}
