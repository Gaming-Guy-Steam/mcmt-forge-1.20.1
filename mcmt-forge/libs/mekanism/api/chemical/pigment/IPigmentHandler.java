package mekanism.api.chemical.pigment;

import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.ISidedChemicalHandler;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IPigmentHandler extends IChemicalHandler<Pigment, PigmentStack>, IEmptyPigmentProvider {
   public interface IMekanismPigmentHandler extends IMekanismChemicalHandler<Pigment, PigmentStack, IPigmentTank>, IPigmentHandler.ISidedPigmentHandler {
   }

   public interface ISidedPigmentHandler extends ISidedChemicalHandler<Pigment, PigmentStack>, IPigmentHandler {
   }
}
