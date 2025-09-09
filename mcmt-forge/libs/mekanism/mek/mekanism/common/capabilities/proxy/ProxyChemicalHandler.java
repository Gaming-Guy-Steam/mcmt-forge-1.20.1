package mekanism.common.capabilities.proxy;

import java.util.Collections;
import java.util.List;
import mekanism.api.Action;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ProxyChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, SIDED_HANDLER extends ISidedChemicalHandler<CHEMICAL, STACK>>
   extends ProxyHandler
   implements IChemicalHandler<CHEMICAL, STACK> {
   private final SIDED_HANDLER sidedHandler;

   public ProxyChemicalHandler(SIDED_HANDLER sidedHandler, @Nullable Direction side, @Nullable IHolder holder) {
      super(side, holder);
      this.sidedHandler = sidedHandler;
   }

   public <TANK extends IChemicalTank<CHEMICAL, STACK>> List<TANK> getTanksIfMekanism() {
      return this.sidedHandler instanceof IMekanismChemicalHandler
         ? ((IMekanismChemicalHandler)this.sidedHandler).getChemicalTanks(null)
         : Collections.emptyList();
   }

   @Override
   public int getTanks() {
      return this.sidedHandler.getTanks(this.side);
   }

   @Override
   public STACK getChemicalInTank(int tank) {
      return this.sidedHandler.getChemicalInTank(tank, this.side);
   }

   @Override
   public void setChemicalInTank(int tank, STACK stack) {
      if (!this.readOnly) {
         this.sidedHandler.setChemicalInTank(tank, stack, this.side);
      }
   }

   @Override
   public long getTankCapacity(int tank) {
      return this.sidedHandler.getTankCapacity(tank, this.side);
   }

   @Override
   public boolean isValid(int tank, STACK stack) {
      return !this.readOnly || this.sidedHandler.isValid(tank, stack, this.side);
   }

   @Override
   public STACK insertChemical(int tank, STACK stack, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.sidedHandler.insertChemical(tank, stack, this.side, action) : stack;
   }

   @Override
   public STACK extractChemical(int tank, long amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.sidedHandler.extractChemical(tank, amount, this.side, action) : this.getEmptyStack();
   }

   @Override
   public STACK insertChemical(STACK stack, Action action) {
      return !this.readOnly && !this.readOnlyInsert.getAsBoolean() ? this.sidedHandler.insertChemical(stack, this.side, action) : stack;
   }

   @Override
   public STACK extractChemical(long amount, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.sidedHandler.extractChemical(amount, this.side, action) : this.getEmptyStack();
   }

   @Override
   public STACK extractChemical(STACK stack, Action action) {
      return !this.readOnly && !this.readOnlyExtract.getAsBoolean() ? this.sidedHandler.extractChemical(stack, this.side, action) : this.getEmptyStack();
   }

   public static class ProxyGasHandler extends ProxyChemicalHandler<Gas, GasStack, IGasHandler.ISidedGasHandler> implements IGasHandler {
      public ProxyGasHandler(@NotNull IGasHandler.ISidedGasHandler gasHandler, @Nullable Direction side, @Nullable IHolder holder) {
         super(gasHandler, side, holder);
      }
   }

   public static class ProxyInfusionHandler
      extends ProxyChemicalHandler<InfuseType, InfusionStack, IInfusionHandler.ISidedInfusionHandler>
      implements IInfusionHandler {
      public ProxyInfusionHandler(@NotNull IInfusionHandler.ISidedInfusionHandler infusionHandler, @Nullable Direction side, @Nullable IHolder holder) {
         super(infusionHandler, side, holder);
      }
   }

   public static class ProxyPigmentHandler extends ProxyChemicalHandler<Pigment, PigmentStack, IPigmentHandler.ISidedPigmentHandler> implements IPigmentHandler {
      public ProxyPigmentHandler(@NotNull IPigmentHandler.ISidedPigmentHandler pigmentHandler, @Nullable Direction side, @Nullable IHolder holder) {
         super(pigmentHandler, side, holder);
      }
   }

   public static class ProxySlurryHandler extends ProxyChemicalHandler<Slurry, SlurryStack, ISlurryHandler.ISidedSlurryHandler> implements ISlurryHandler {
      public ProxySlurryHandler(@NotNull ISlurryHandler.ISidedSlurryHandler slurryHandler, @Nullable Direction side, @Nullable IHolder holder) {
         super(slurryHandler, side, holder);
      }
   }
}
