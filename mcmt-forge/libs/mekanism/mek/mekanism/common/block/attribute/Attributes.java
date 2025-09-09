package mekanism.common.block.attribute;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.ToIntBiFunction;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.lib.multiblock.IInternalMultiblock;
import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.common.lib.multiblock.IStructuralMultiblock;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockBehaviour.StateArgumentPredicate;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction.Builder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Attributes {
   public static final Attribute ACTIVE = new AttributeStateActive(0);
   public static final Attribute ACTIVE_LIGHT = new AttributeStateActive(8);
   public static final Attribute ACTIVE_MELT_LIGHT = new AttributeStateActive(13);
   public static final Attribute ACTIVE_FULL_LIGHT = new AttributeStateActive(15);
   public static final Attribute COMPARATOR = new Attributes.AttributeComparator();
   public static final Attribute INVENTORY = new Attributes.AttributeInventory();
   public static final Attribute REDSTONE = new Attributes.AttributeRedstone();
   public static final Attribute SECURITY = new Attributes.AttributeSecurity();

   private Attributes() {
   }

   public static class AttributeComparator implements Attribute {
      private AttributeComparator() {
      }
   }

   public record AttributeComputerIntegration(String name) implements Attribute {
   }

   public record AttributeCustomResistance(float resistance) implements Attribute {
   }

   public static class AttributeInventory<DelayedLootItemBuilder extends ConditionUserBuilder<DelayedLootItemBuilder> & FunctionUserBuilder<DelayedLootItemBuilder>>
      implements Attribute {
      @Nullable
      private final BiFunction<DelayedLootItemBuilder, Builder, Boolean> customLootBuilder;

      public AttributeInventory(@Nullable BiFunction<DelayedLootItemBuilder, Builder, Boolean> customLootBuilder) {
         this.customLootBuilder = customLootBuilder;
      }

      private AttributeInventory() {
         this(null);
      }

      public boolean hasCustomLoot() {
         return this.customLootBuilder != null;
      }

      public boolean applyLoot(DelayedLootItemBuilder builder, Builder nbtBuilder) {
         return this.customLootBuilder != null ? this.customLootBuilder.apply(builder, nbtBuilder) : false;
      }
   }

   public static class AttributeLight implements Attribute {
      private final int light;

      public AttributeLight(int light) {
         this.light = light;
      }

      @Override
      public void adjustProperties(Properties props) {
         BlockStateHelper.applyLightLevelAdjustments(props, state -> this.light);
      }
   }

   public static class AttributeMobSpawn implements Attribute {
      public static final StateArgumentPredicate<EntityType<?>> NEVER_PREDICATE = (state, reader, pos, entityType) -> false;
      public static final Attributes.AttributeMobSpawn NEVER = new Attributes.AttributeMobSpawn(NEVER_PREDICATE);
      public static final Attributes.AttributeMobSpawn WHEN_NOT_FORMED = new Attributes.AttributeMobSpawn((state, reader, pos, entityType) -> {
         BlockEntity tile = WorldUtils.getTileEntity(reader, pos);
         if (tile instanceof IMultiblock<?> multiblockTile) {
            if (reader instanceof LevelReader levelReader && levelReader.m_5776_()) {
               if (multiblockTile.getMultiblock().isFormed()) {
                  return false;
               }
            } else if (multiblockTile.getMultiblock().isPositionInsideBounds(multiblockTile.getStructure(), pos.m_7494_())) {
               return false;
            }
         } else if (tile instanceof IStructuralMultiblock structuralMultiblock && structuralMultiblock.hasFormedMultiblock()) {
            if (reader instanceof LevelReader levelReaderx && levelReaderx.m_5776_()) {
               return false;
            }

            BlockPos above = pos.m_7494_();
            Iterator var10 = structuralMultiblock.getStructureMap().values().iterator();

            Structure structure;
            MultiblockData data;
            do {
               if (!var10.hasNext()) {
                  return state.m_60783_(reader, pos, Direction.UP) && state.getLightEmission(reader, pos) < 14;
               }

               structure = (Structure)var10.next();
               data = structure.getMultiblockData();
            } while (data == null || !data.isFormed() || !data.isPositionInsideBounds(structure, above));

            return false;
         } else if (tile instanceof IInternalMultiblock internalMultiblock && internalMultiblock.hasFormedMultiblock()) {
            return false;
         }

         return state.m_60783_(reader, pos, Direction.UP) && state.getLightEmission(reader, pos) < 14;
      });
      private final StateArgumentPredicate<EntityType<?>> spawningPredicate;

      public AttributeMobSpawn(StateArgumentPredicate<EntityType<?>> spawningPredicate) {
         this.spawningPredicate = spawningPredicate;
      }

      @Override
      public void adjustProperties(Properties props) {
         props.m_60922_(this.spawningPredicate);
      }
   }

   public static class AttributeRedstone implements Attribute {
      private AttributeRedstone() {
      }
   }

   public static class AttributeRedstoneEmitter<TILE extends TileEntityMekanism> implements Attribute.TileAttribute<TILE> {
      private final ToIntBiFunction<TILE, Direction> redstoneFunction;

      public AttributeRedstoneEmitter(ToIntBiFunction<TILE, Direction> redstoneFunction) {
         this.redstoneFunction = redstoneFunction;
      }

      public int getRedstoneLevel(TILE tile, @NotNull Direction side) {
         return this.redstoneFunction.applyAsInt(tile, side);
      }
   }

   public static class AttributeSecurity implements Attribute {
      private AttributeSecurity() {
      }
   }
}
