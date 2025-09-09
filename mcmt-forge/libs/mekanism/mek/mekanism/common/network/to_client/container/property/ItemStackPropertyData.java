package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackPropertyData extends PropertyData {
   @NotNull
   private final ItemStack value;

   public ItemStackPropertyData(short property, @NotNull ItemStack value) {
      super(PropertyType.ITEM_STACK, property);
      this.value = value;
   }

   @Override
   public void handleWindowProperty(MekanismContainer container) {
      container.handleWindowProperty(this.getProperty(), this.value);
   }

   @Override
   public void writeToPacket(FriendlyByteBuf buffer) {
      super.writeToPacket(buffer);
      buffer.m_130055_(this.value);
   }
}
