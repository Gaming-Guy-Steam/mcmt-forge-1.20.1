package mekanism.api.chemical;

import org.jetbrains.annotations.NotNull;

public interface IEmptyStackProvider<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> {
   @NotNull
   STACK getEmptyStack();
}
