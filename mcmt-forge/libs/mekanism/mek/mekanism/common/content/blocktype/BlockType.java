package mekanism.common.content.blocktype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream.Builder;
import mekanism.api.functions.TriConsumer;
import mekanism.api.text.ILangEntry;
import mekanism.api.tier.ITier;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeMultiblock;
import mekanism.common.block.attribute.Attributes;
import mekanism.common.block.interfaces.ITypeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockType {
   private final ILangEntry description;
   private final Map<Class<? extends Attribute>, Attribute> attributeMap = new HashMap<>();

   public BlockType(ILangEntry description) {
      this.description = description;
   }

   public boolean has(Class<? extends Attribute> type) {
      return this.attributeMap.containsKey(type);
   }

   @Nullable
   public <ATTRIBUTE extends Attribute> ATTRIBUTE get(Class<ATTRIBUTE> type) {
      return (ATTRIBUTE)this.attributeMap.get(type);
   }

   @SafeVarargs
   protected final void setFrom(BlockTypeTile<?> tile, Class<? extends Attribute>... types) {
      for (Class<? extends Attribute> type : types) {
         this.attributeMap.put(type, tile.get(type));
      }
   }

   public void add(Attribute... attrs) {
      for (Attribute attr : attrs) {
         this.attributeMap.put((Class<? extends Attribute>)attr.getClass(), attr);
      }
   }

   @SafeVarargs
   public final void remove(Class<? extends Attribute>... attrs) {
      for (Class<? extends Attribute> attr : attrs) {
         this.attributeMap.remove(attr);
      }
   }

   public Collection<Attribute> getAll() {
      return this.attributeMap.values();
   }

   @NotNull
   public ILangEntry getDescription() {
      return this.description;
   }

   public static boolean is(Block block, BlockType... types) {
      if (block instanceof ITypeBlock typeBlock) {
         for (BlockType type : types) {
            if (typeBlock.getType() == type) {
               return true;
            }
         }
      }

      return false;
   }

   public static BlockType get(Block block) {
      return block instanceof ITypeBlock typeBlock ? typeBlock.getType() : null;
   }

   public static class BlockTypeBuilder<BLOCK extends BlockType, T extends BlockType.BlockTypeBuilder<BLOCK, T>> {
      protected final BLOCK holder;

      protected BlockTypeBuilder(BLOCK holder) {
         this.holder = holder;
      }

      public static BlockType.BlockTypeBuilder<BlockType, ?> createBlock(ILangEntry description) {
         return new BlockType.BlockTypeBuilder<>(new BlockType(description));
      }

      public T self() {
         return (T)this;
      }

      public final T replace(Attribute... attrs) {
         return this.with(attrs);
      }

      public final T with(Attribute... attrs) {
         this.holder.add(attrs);
         return this.self();
      }

      public final T withBounding(TriConsumer<BlockPos, BlockState, Builder<BlockPos>> boundingPositions) {
         return this.with(new AttributeHasBounding(boundingPositions));
      }

      @SafeVarargs
      public final T without(Class<? extends Attribute>... attrs) {
         this.holder.remove(attrs);
         return this.self();
      }

      public T withCustomShape(VoxelShape[] shape) {
         return this.with(new AttributeCustomShape(shape));
      }

      public T withLight(int light) {
         return this.with(new Attributes.AttributeLight(light));
      }

      public T withComputerSupport(String name) {
         return this.with(new Attributes.AttributeComputerIntegration(name));
      }

      public T withComputerSupport(ITier tier, String name) {
         return this.withComputerSupport(tier.getBaseTier().getLowerName() + name);
      }

      public final T externalMultiblock() {
         return this.with(AttributeMultiblock.EXTERNAL, Attributes.AttributeMobSpawn.WHEN_NOT_FORMED);
      }

      public final T internalMultiblock() {
         return this.with(AttributeMultiblock.INTERNAL, Attributes.AttributeMobSpawn.WHEN_NOT_FORMED);
      }

      public BLOCK build() {
         return this.holder;
      }
   }
}
