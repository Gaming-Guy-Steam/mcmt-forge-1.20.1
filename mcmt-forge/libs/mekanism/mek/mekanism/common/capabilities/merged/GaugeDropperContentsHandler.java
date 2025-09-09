package mekanism.common.capabilities.merged;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.DynamicHandler;
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank;
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class GaugeDropperContentsHandler extends MergedTankContentsHandler<MergedTank> implements IMekanismFluidHandler, IFluidHandlerItem {
   private static final int CAPACITY = 16000;
   private static final int TRANSFER_RATE = 256;
   protected final List<IExtendedFluidTank> fluidTanks;

   public static GaugeDropperContentsHandler create() {
      return new GaugeDropperContentsHandler();
   }

   private GaugeDropperContentsHandler() {
      this.mergedTank = MergedTank.create(
         new RateLimitFluidHandler.RateLimitFluidTank(() -> 256, () -> 16000, this),
         new RateLimitChemicalTank.RateLimitGasTank(
            () -> 256L,
            () -> 16000L,
            ChemicalTankBuilder.GAS.alwaysTrueBi,
            ChemicalTankBuilder.GAS.alwaysTrueBi,
            ChemicalTankBuilder.GAS.alwaysTrue,
            null,
            this.gasHandler = new DynamicChemicalHandler.DynamicGasHandler(
               side -> this.gasTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("GasTanks", this.gasTanks)
            )
         ),
         new RateLimitChemicalTank.RateLimitInfusionTank(
            () -> 256L,
            () -> 16000L,
            ChemicalTankBuilder.INFUSION.alwaysTrueBi,
            ChemicalTankBuilder.INFUSION.alwaysTrueBi,
            ChemicalTankBuilder.INFUSION.alwaysTrue,
            this.infusionHandler = new DynamicChemicalHandler.DynamicInfusionHandler(
               side -> this.infusionTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("InfusionTanks", this.infusionTanks)
            )
         ),
         new RateLimitChemicalTank.RateLimitPigmentTank(
            () -> 256L,
            () -> 16000L,
            ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
            ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
            ChemicalTankBuilder.PIGMENT.alwaysTrue,
            this.pigmentHandler = new DynamicChemicalHandler.DynamicPigmentHandler(
               side -> this.pigmentTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("PigmentTanks", this.pigmentTanks)
            )
         ),
         new RateLimitChemicalTank.RateLimitSlurryTank(
            () -> 256L,
            () -> 16000L,
            ChemicalTankBuilder.SLURRY.alwaysTrueBi,
            ChemicalTankBuilder.SLURRY.alwaysTrueBi,
            ChemicalTankBuilder.SLURRY.alwaysTrue,
            this.slurryHandler = new DynamicChemicalHandler.DynamicSlurryHandler(
               side -> this.slurryTanks,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               DynamicHandler.InteractPredicate.ALWAYS_TRUE,
               () -> this.onContentsChanged("SlurryTanks", this.slurryTanks)
            )
         )
      );
      this.fluidTanks = Collections.singletonList(this.mergedTank.getFluidTank());
   }

   @Override
   protected void load() {
      super.load();
      ItemDataUtils.readContainers(this.getStack(), "FluidTanks", this.getFluidTanks(null));
   }

   @NotNull
   @Override
   public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
      return this.fluidTanks;
   }

   @Override
   public void onContentsChanged() {
      this.onContentsChanged("FluidTanks", this.fluidTanks);
   }

   @NotNull
   public ItemStack getContainer() {
      return this.getStack();
   }

   @Override
   protected void gatherCapabilityResolvers(Consumer<ICapabilityResolver> consumer) {
      super.gatherCapabilityResolvers(consumer);
      consumer.accept(BasicCapabilityResolver.constant(ForgeCapabilities.FLUID_HANDLER_ITEM, this));
   }
}
