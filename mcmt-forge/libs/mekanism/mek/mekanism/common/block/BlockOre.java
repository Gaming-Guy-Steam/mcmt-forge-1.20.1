package mekanism.common.block;

import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.resource.ore.OreType;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

public class BlockOre extends Block implements IHasDescription {
   private final OreType ore;
   private String descriptionTranslationKey;

   public BlockOre(OreType ore) {
      this(
         ore,
         BlockStateHelper.applyLightLevelAdjustments(
            Properties.m_284310_().m_60913_(3.0F, 3.0F).m_60999_().m_284180_(MapColor.f_283947_).m_280658_(NoteBlockInstrument.BASEDRUM)
         )
      );
   }

   public BlockOre(OreType ore, Properties properties) {
      super(properties);
      this.ore = ore;
   }

   @NotNull
   public String getDescriptionTranslationKey() {
      if (this.descriptionTranslationKey == null) {
         this.descriptionTranslationKey = Util.m_137492_("description", Mekanism.rl(this.ore.getResource().getRegistrySuffix() + "_ore"));
      }

      return this.descriptionTranslationKey;
   }

   @NotNull
   @Override
   public ILangEntry getDescription() {
      return this::getDescriptionTranslationKey;
   }

   public int getExpDrop(BlockState state, LevelReader reader, RandomSource random, BlockPos pos, int fortune, int silkTouch) {
      return this.ore.getMaxExp() > 0 && silkTouch == 0
         ? Mth.m_216271_(random, this.ore.getMinExp(), this.ore.getMaxExp())
         : super.getExpDrop(state, reader, random, pos, fortune, silkTouch);
   }
}
