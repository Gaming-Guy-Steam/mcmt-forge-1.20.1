package mekanism.common.capabilities;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCapabilityWrapper implements ICapabilityProvider {
   private final Map<Capability<?>, ItemCapabilityWrapper.ItemCapability> capabilities = new IdentityHashMap<>();
   private final CapabilityCache capabilityCache = new CapabilityCache();
   protected final ItemStack itemStack;

   public ItemCapabilityWrapper(ItemStack stack, ItemCapabilityWrapper.ItemCapability... caps) {
      this.itemStack = stack;
      this.add(caps);
   }

   public void add(ItemCapabilityWrapper.ItemCapability... caps) {
      for (ItemCapabilityWrapper.ItemCapability c : caps) {
         c.wrapper = this;
         c.init();
         c.gatherCapabilityResolvers(resolver -> {
            this.capabilityCache.addCapabilityResolver(resolver);

            for (Capability<?> supportedCapability : resolver.getSupportedCapabilities()) {
               this.capabilities.put(supportedCapability, c);
            }
         });
      }
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
      if (!this.itemStack.m_41619_()
         && capability.isRegistered()
         && !this.capabilityCache.isCapabilityDisabled(capability, null)
         && this.capabilityCache.canResolve(capability)) {
         ItemCapabilityWrapper.ItemCapability cap = this.capabilities.get(capability);
         if (cap != null) {
            cap.load();
         }

         return this.capabilityCache.getCapabilityUnchecked(capability, null);
      } else {
         return LazyOptional.empty();
      }
   }

   public abstract static class ItemCapability {
      private ItemCapabilityWrapper wrapper;

      protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      }

      protected void init() {
      }

      protected void load() {
      }

      public ItemStack getStack() {
         return this.wrapper.itemStack;
      }
   }
}
