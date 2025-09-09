package mekanism.common.block;

import java.util.function.UnaryOperator;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.util.WorldUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class BlockRadioactiveWasteBarrel extends BlockTile.BlockTileModel<TileEntityRadioactiveWasteBarrel, BlockTypeTile<TileEntityRadioactiveWasteBarrel>> {
   public BlockRadioactiveWasteBarrel() {
      super(MekanismBlockTypes.RADIOACTIVE_WASTE_BARREL, (UnaryOperator<Properties>)(properties -> properties.m_284180_(MapColor.f_283927_)));
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
      if (!player.m_6144_()) {
         return InteractionResult.PASS;
      } else {
         TileEntityRadioactiveWasteBarrel tile = WorldUtils.getTileEntity(TileEntityRadioactiveWasteBarrel.class, world, pos);
         if (tile == null) {
            return InteractionResult.PASS;
         } else {
            if (!world.m_5776_()) {
               GasStack stored = tile.getGas();
               Component text;
               if (stored.isEmpty()) {
                  text = MekanismLang.NO_GAS.translateColored(EnumColor.GRAY, new Object[0]);
               } else {
                  text = MekanismLang.STORED_MB_PERCENTAGE
                     .translateColored(
                        EnumColor.ORANGE,
                        new Object[]{EnumColor.ORANGE, stored, EnumColor.GRAY, TextUtils.format(stored.getAmount()), TextUtils.getPercent(tile.getGasScale())}
                     );
               }

               player.m_213846_(text);
            }

            return InteractionResult.m_19078_(world.f_46443_);
         }
      }
   }
}
