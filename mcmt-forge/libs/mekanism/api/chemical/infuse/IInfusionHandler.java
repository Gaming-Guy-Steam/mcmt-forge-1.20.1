package mekanism.api.chemical.infuse;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IInfusionHandler extends IChemicalHandler<InfuseType, InfusionStack>, IEmptyInfusionProvider {
   public interface IMekanismInfusionHandler extends IMekanismChemicalHandler<InfuseType, InfusionStack, IInfusionTank>, IInfusionHandler.ISidedInfusionHandler {
   }

   public interface ISidedInfusionHandler extends ISidedChemicalHandler<InfuseType, InfusionStack>, IInfusionHandler {
   }
}
