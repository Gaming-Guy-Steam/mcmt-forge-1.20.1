package mekanism.common.capabilities.merged;

import java.util.Arrays;
import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.merged.MergedChemicalTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.capabilities.fluid.FluidTankWrapper;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

@NothingNullByDefault
public class MergedTank extends MergedChemicalTank {
   private final IExtendedFluidTank fluidTank;

   public static MergedTank create(IExtendedFluidTank fluidTank, IGasTank gasTank, IInfusionTank infusionTank, IPigmentTank pigmentTank, ISlurryTank slurryTank) {
      Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
      Objects.requireNonNull(gasTank, "Gas tank cannot be null");
      Objects.requireNonNull(infusionTank, "Infusion tank cannot be null");
      Objects.requireNonNull(pigmentTank, "Pigment tank cannot be null");
      Objects.requireNonNull(slurryTank, "Slurry tank cannot be null");
      return new MergedTank(fluidTank, gasTank, infusionTank, pigmentTank, slurryTank);
   }

   private MergedTank(IExtendedFluidTank fluidTank, IChemicalTank<?, ?>... chemicalTanks) {
      super(fluidTank::isEmpty, chemicalTanks);
      this.fluidTank = new FluidTankWrapper(this, fluidTank, () -> Arrays.stream(chemicalTanks).allMatch(IChemicalTank::isEmpty));
   }

   public MergedTank.CurrentType getCurrentType() {
      if (!this.getFluidTank().isEmpty()) {
         return MergedTank.CurrentType.FLUID;
      } else {
         return switch (this.getCurrent()) {
            case EMPTY -> MergedTank.CurrentType.EMPTY;
            case GAS -> MergedTank.CurrentType.GAS;
            case INFUSION -> MergedTank.CurrentType.INFUSION;
            case PIGMENT -> MergedTank.CurrentType.PIGMENT;
            case SLURRY -> MergedTank.CurrentType.SLURRY;
         };
      }
   }

   public final IExtendedFluidTank getFluidTank() {
      return this.fluidTank;
   }

   public void addToUpdateTag(CompoundTag updateTag) {
      updateTag.m_128365_("fluid", this.getFluidTank().getFluid().writeToNBT(new CompoundTag()));
      updateTag.m_128365_("gas", this.getGasTank().getStack().write(new CompoundTag()));
      updateTag.m_128365_("infuseTypeName", this.getInfusionTank().getStack().write(new CompoundTag()));
      updateTag.m_128365_("pigment", this.getPigmentTank().getStack().write(new CompoundTag()));
      updateTag.m_128365_("slurry", this.getSlurryTank().getStack().write(new CompoundTag()));
   }

   public void readFromUpdateTag(CompoundTag tag) {
      NBTUtils.setFluidStackIfPresent(tag, "fluid", value -> this.getFluidTank().setStack(value));
      NBTUtils.setGasStackIfPresent(tag, "gas", value -> this.getGasTank().setStack(value));
      NBTUtils.setInfusionStackIfPresent(tag, "infuseTypeName", value -> this.getInfusionTank().setStack(value));
      NBTUtils.setPigmentStackIfPresent(tag, "pigment", value -> this.getPigmentTank().setStack(value));
      NBTUtils.setSlurryStackIfPresent(tag, "slurry", value -> this.getSlurryTank().setStack(value));
   }

   public static enum CurrentType {
      EMPTY,
      FLUID,
      GAS,
      INFUSION,
      PIGMENT,
      SLURRY;
   }
}
