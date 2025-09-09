package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public abstract class MergedTankContentsHandler<MERGED extends MergedChemicalTank> extends ItemCapabilityWrapper.ItemCapability {
   protected MERGED mergedTank;
   protected DynamicChemicalHandler.DynamicGasHandler gasHandler;
   protected DynamicChemicalHandler.DynamicInfusionHandler infusionHandler;
   protected DynamicChemicalHandler.DynamicPigmentHandler pigmentHandler;
   protected DynamicChemicalHandler.DynamicSlurryHandler slurryHandler;
   protected List<ISlurryTank> slurryTanks;
   protected List<IPigmentTank> pigmentTanks;
   protected List<IInfusionTank> infusionTanks;
   protected List<IGasTank> gasTanks;

   @Override
   protected void init() {
      super.init();
      this.gasTanks = Collections.singletonList(this.mergedTank.getGasTank());
      this.infusionTanks = Collections.singletonList(this.mergedTank.getInfusionTank());
      this.pigmentTanks = Collections.singletonList(this.mergedTank.getPigmentTank());
      this.slurryTanks = Collections.singletonList(this.mergedTank.getSlurryTank());
   }

   @Override
   protected void load() {
      super.load();
      ItemStack stack = this.getStack();
      if (!stack.m_41619_()) {
         ItemDataUtils.readContainers(stack, "GasTanks", this.gasTanks);
         ItemDataUtils.readContainers(stack, "InfusionTanks", this.infusionTanks);
         ItemDataUtils.readContainers(stack, "PigmentTanks", this.pigmentTanks);
         ItemDataUtils.readContainers(stack, "SlurryTanks", this.slurryTanks);
      }
   }

   protected void onContentsChanged(String key, List<? extends INBTSerializable<CompoundTag>> containers) {
      ItemDataUtils.writeContainers(this.getStack(), key, containers);
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.GAS_HANDLER, this.gasHandler));
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.INFUSION_HANDLER, this.infusionHandler));
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.PIGMENT_HANDLER, this.pigmentHandler));
      consumer.accept(BasicCapabilityResolver.constant(Capabilities.SLURRY_HANDLER, this.slurryHandler));
   }
}
