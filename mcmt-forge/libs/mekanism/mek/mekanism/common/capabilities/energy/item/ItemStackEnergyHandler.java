package mekanism.common.capabilities.energy.item;

import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.resolver.EnergyCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public abstract class ItemStackEnergyHandler extends ItemCapabilityWrapper.ItemCapability implements IMekanismStrictEnergyHandler {
   protected List<IEnergyContainer> energyContainers;

   protected abstract List<IEnergyContainer> getInitialContainers();

   @Override
   protected void init() {
      super.init();
      this.energyContainers = this.getInitialContainers();
   }

   @Override
   protected void load() {
      super.load();
      ItemDataUtils.readContainers(this.getStack(), "EnergyContainers", this.getEnergyContainers(null));
   }

   @NotNull
   @Override
   public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
      return this.energyContainers;
   }

   @Override
   public void onContentsChanged() {
      ItemDataUtils.writeContainers(this.getStack(), "EnergyContainers", this.getEnergyContainers(null));
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(new EnergyCapabilityResolver(this));
   }
}
