package mekanism.common.capabilities.security.item;

import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IOwnerObject;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.lib.frequency.IFrequencyItem;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.OwnerDisplay;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemStackOwnerObject extends ItemCapabilityWrapper.ItemCapability implements IOwnerObject {
   @Nullable
   @Override
   public UUID getOwnerUUID() {
      ItemStack stack = this.getStack();
      return stack.m_41619_() ? null : ItemDataUtils.getUniqueID(stack, "owner");
   }

   @Nullable
   @Override
   public String getOwnerName() {
      UUID owner = this.getOwnerUUID();
      return owner != null ? OwnerDisplay.getOwnerName(MekanismUtils.tryGetClientPlayer(), owner, null) : null;
   }

   @Override
   public void setOwnerUUID(@Nullable UUID owner) {
      ItemStack stack = this.getStack();
      if (!stack.m_41619_()) {
         if (stack.m_41720_() instanceof IFrequencyItem frequencyItem) {
            frequencyItem.setFrequency(stack, null);
         }

         ItemDataUtils.setUUID(stack, "owner", owner);
      }
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.OWNER_OBJECT, this));
   }
}
