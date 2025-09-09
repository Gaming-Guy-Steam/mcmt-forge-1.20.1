package mekanism.common.capabilities.resolver.manager;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CapabilityHandlerManager<HOLDER extends IHolder, CONTAINER, HANDLER, SIDED_HANDLER extends HANDLER>
   extends BasicSidedCapabilityResolver<HANDLER, SIDED_HANDLER>
   implements ICapabilityHandlerManager<CONTAINER> {
   private final BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter;
   private final boolean canHandle;
   @Nullable
   protected final HOLDER holder;

   protected CapabilityHandlerManager(
      @Nullable HOLDER holder,
      SIDED_HANDLER baseHandler,
      Capability<HANDLER> supportedCapability,
      BasicSidedCapabilityResolver.ProxyCreator<HANDLER, SIDED_HANDLER> proxyCreator,
      BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter
   ) {
      super(baseHandler, supportedCapability, proxyCreator, holder != null);
      this.holder = holder;
      this.canHandle = this.holder != null;
      this.containerGetter = containerGetter;
   }

   @Override
   public boolean canHandle() {
      return this.canHandle;
   }

   @Override
   public List<CONTAINER> getContainers(@Nullable Direction side) {
      return this.canHandle() ? this.containerGetter.apply(this.holder, side) : Collections.emptyList();
   }

   @Nullable
   @Override
   protected IHolder getHolder() {
      return this.holder;
   }

   @Override
   public <T> LazyOptional<T> resolve(Capability<T> capability, @Nullable Direction side) {
      return this.getContainers(side).isEmpty() ? LazyOptional.empty() : super.resolve(capability, side);
   }
}
