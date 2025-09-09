package mekanism.common.capabilities.security.item;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class ItemStackSecurityObject extends ItemStackOwnerObject implements ISecurityObject {
   @Override
   public SecurityMode getSecurityMode() {
      ItemStack stack = this.getStack();
      return stack.m_41619_() ? SecurityMode.PUBLIC : SecurityMode.byIndexStatic(ItemDataUtils.getInt(stack, "securityMode"));
   }

   @Override
   public void setSecurityMode(SecurityMode mode) {
      ItemStack stack = this.getStack();
      if (!stack.m_41619_()) {
         SecurityMode current = this.getSecurityMode();
         if (current != mode) {
            ItemDataUtils.setInt(stack, "securityMode", mode.ordinal());
            this.onSecurityChanged(current, mode);
         }
      }
   }

   @Override
   public void onSecurityChanged(@NotNull SecurityMode old, @NotNull SecurityMode mode) {
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.security(this));
   }
}
