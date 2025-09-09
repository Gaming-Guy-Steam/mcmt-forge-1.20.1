package mekanism.client.model.baked;

import com.google.common.collect.ImmutableList;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemOverrides.BakedOverride;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ExtensionOverrideBakedModel<T> extends ExtensionBakedModel<T> {
   private final ItemOverrides overrides;

   public ExtensionOverrideBakedModel(BakedModel original, UnaryOperator<ItemOverrides> wrapper) {
      super(original);
      this.overrides = wrapper.apply(super.m_7343_());
   }

   public ItemOverrides m_7343_() {
      return this.overrides;
   }

   public abstract static class ExtendedItemOverrides extends ItemOverrides {
      protected final ItemOverrides original;

      protected ExtendedItemOverrides(ItemOverrides original) {
         this.original = original;
      }

      @Nullable
      public abstract BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed);

      protected BakedModel wrap(
         BakedModel model,
         ItemStack stack,
         @Nullable ClientLevel world,
         @Nullable LivingEntity entity,
         int seed,
         ModelData modelData,
         BiFunction<BakedModel, ModelData, ModelDataBakedModel> wrapper
      ) {
         BakedModel resolved = this.original.m_173464_(model, stack, world, entity, seed);
         if (resolved == null) {
            resolved = model;
         }

         return (BakedModel)wrapper.apply(resolved, modelData);
      }

      public ImmutableList<BakedOverride> getOverrides() {
         return this.original.getOverrides();
      }
   }
}
