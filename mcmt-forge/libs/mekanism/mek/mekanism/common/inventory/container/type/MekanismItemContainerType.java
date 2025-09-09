package mekanism.common.inventory.container.type;

import mekanism.common.util.RegistryUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.IContainerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismItemContainerType<ITEM extends Item, CONTAINER extends AbstractContainerMenu>
   extends BaseMekanismContainerType<ITEM, CONTAINER, MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER>> {
   public static <ITEM extends Item, CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<ITEM, CONTAINER> item(
      Class<ITEM> type, MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER> constructor
   ) {
      return new MekanismItemContainerType<>(
         type, constructor, (id, inv, buf) -> constructor.create(id, inv, (InteractionHand)buf.m_130066_(InteractionHand.class), getStackFromBuffer(buf, type))
      );
   }

   public static <ITEM extends Item, CONTAINER extends AbstractContainerMenu> MekanismItemContainerType<ITEM, CONTAINER> item(
      Class<ITEM> type, MekanismItemContainerType.IMekanismSidedItemContainerFactory<ITEM, CONTAINER> constructor
   ) {
      return new MekanismItemContainerType<>(
         type,
         constructor,
         (id, inv, buf) -> constructor.create(id, inv, (InteractionHand)buf.m_130066_(InteractionHand.class), getStackFromBuffer(buf, type), true)
      );
   }

   protected MekanismItemContainerType(
      Class<ITEM> type, MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER> mekanismConstructor, IContainerFactory<CONTAINER> constructor
   ) {
      super(type, mekanismConstructor, constructor);
   }

   @Nullable
   public CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
      return !stack.m_41619_() && this.type.isInstance(stack.m_41720_()) ? this.mekanismConstructor.create(id, inv, hand, stack) : null;
   }

   @Nullable
   public MenuConstructor create(InteractionHand hand, ItemStack stack) {
      return !stack.m_41619_() && this.type.isInstance(stack.m_41720_()) ? (id, inv, player) -> this.mekanismConstructor.create(id, inv, hand, stack) : null;
   }

   @NotNull
   private static <ITEM extends Item> ItemStack getStackFromBuffer(FriendlyByteBuf buf, Class<ITEM> type) {
      if (buf == null) {
         throw new IllegalArgumentException("Null packet buffer");
      } else {
         ItemStack stack = buf.m_130267_();
         if (type.isInstance(stack.m_41720_())) {
            return stack;
         } else {
            throw new IllegalStateException("Client received invalid stack (" + RegistryUtils.getName(stack.m_41720_()) + ") for item container.");
         }
      }
   }

   @FunctionalInterface
   public interface IMekanismItemContainerFactory<ITEM extends Item, CONTAINER extends AbstractContainerMenu> {
      CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack);
   }

   @FunctionalInterface
   public interface IMekanismSidedItemContainerFactory<ITEM extends Item, CONTAINER extends AbstractContainerMenu>
      extends MekanismItemContainerType.IMekanismItemContainerFactory<ITEM, CONTAINER> {
      CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack, boolean remote);

      @Override
      default CONTAINER create(int id, Inventory inv, InteractionHand hand, ItemStack stack) {
         return this.create(id, inv, hand, stack, false);
      }
   }
}
