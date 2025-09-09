package mekanism.api.chemical.gas;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IGasHandler extends IChemicalHandler<Gas, GasStack>, IEmptyGasProvider {
   public interface IMekanismGasHandler extends IMekanismChemicalHandler<Gas, GasStack, IGasTank>, IGasHandler.ISidedGasHandler {
   }

   public interface ISidedGasHandler extends ISidedChemicalHandler<Gas, GasStack>, IGasHandler {
   }
}
