package mekanism.client.jei.interfaces;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IJEIGhostTarget {
   @Nullable
   IJEIGhostTarget.IGhostIngredientConsumer getGhostHandler();

   default int borderSize() {
      return 0;
   }

   public interface IGhostBlockItemConsumer extends IJEIGhostTarget.IGhostItemConsumer {
      @Override
      default boolean supportsIngredient(Object ingredient) {
         return IJEIGhostTarget.IGhostItemConsumer.super.supportsIngredient(ingredient) && ((ItemStack)ingredient).m_41720_() instanceof BlockItem;
      }
   }

   public interface IGhostIngredientConsumer extends Consumer<Object> {
      boolean supportsIngredient(Object ingredient);
   }

   public interface IGhostItemConsumer extends IJEIGhostTarget.IGhostIngredientConsumer {
      @Override
      default boolean supportsIngredient(Object ingredient) {
         return ingredient instanceof ItemStack stack && !stack.m_41619_();
      }
   }
}
