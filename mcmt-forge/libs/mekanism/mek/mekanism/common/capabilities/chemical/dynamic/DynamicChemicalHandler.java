package mekanism.common.capabilities.chemical.dynamic;

import java.util.List;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.DynamicHandler;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class DynamicChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
   extends DynamicHandler<TANK>
   implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {
   protected DynamicChemicalHandler(
      Function<Direction, List<TANK>> tankSupplier,
      DynamicHandler.InteractPredicate canExtract,
      DynamicHandler.InteractPredicate canInsert,
      @Nullable IContentsListener listener
   ) {
      super(tankSupplier, canExtract, canInsert, listener);
   }

   @Override
   public List<TANK> getChemicalTanks(@Nullable Direction side) {
      return this.containerSupplier.apply(side);
   }

   @Override
   public STACK insertChemical(int tank, STACK stack, @Nullable Direction side, Action action) {
      return this.canInsert.test(tank, side) ? IMekanismChemicalHandler.super.insertChemical(tank, stack, side, action) : stack;
   }

   @Override
   public STACK extractChemical(int tank, long amount, @Nullable Direction side, Action action) {
      return this.canExtract.test(tank, side) ? IMekanismChemicalHandler.super.extractChemical(tank, amount, side, action) : this.getEmptyStack();
   }

   public static class DynamicGasHandler extends DynamicChemicalHandler<Gas, GasStack, IGasTank> implements IGasHandler.IMekanismGasHandler {
      public DynamicGasHandler(
         Function<Direction, List<IGasTank>> tankSupplier,
         DynamicHandler.InteractPredicate canExtract,
         DynamicHandler.InteractPredicate canInsert,
         @Nullable IContentsListener listener
      ) {
         super(tankSupplier, canExtract, canInsert, listener);
      }
   }

   public static class DynamicInfusionHandler
      extends DynamicChemicalHandler<InfuseType, InfusionStack, IInfusionTank>
      implements IInfusionHandler.IMekanismInfusionHandler {
      public DynamicInfusionHandler(
         Function<Direction, List<IInfusionTank>> tankSupplier,
         DynamicHandler.InteractPredicate canExtract,
         DynamicHandler.InteractPredicate canInsert,
         @Nullable IContentsListener listener
      ) {
         super(tankSupplier, canExtract, canInsert, listener);
      }
   }

   public static class DynamicPigmentHandler
      extends DynamicChemicalHandler<Pigment, PigmentStack, IPigmentTank>
      implements IPigmentHandler.IMekanismPigmentHandler {
      public DynamicPigmentHandler(
         Function<Direction, List<IPigmentTank>> tankSupplier,
         DynamicHandler.InteractPredicate canExtract,
         DynamicHandler.InteractPredicate canInsert,
         @Nullable IContentsListener listener
      ) {
         super(tankSupplier, canExtract, canInsert, listener);
      }
   }

   public static class DynamicSlurryHandler extends DynamicChemicalHandler<Slurry, SlurryStack, ISlurryTank> implements ISlurryHandler.IMekanismSlurryHandler {
      public DynamicSlurryHandler(
         Function<Direction, List<ISlurryTank>> tankSupplier,
         DynamicHandler.InteractPredicate canExtract,
         DynamicHandler.InteractPredicate canInsert,
         @Nullable IContentsListener listener
      ) {
         super(tankSupplier, canExtract, canInsert, listener);
      }
   }
}
