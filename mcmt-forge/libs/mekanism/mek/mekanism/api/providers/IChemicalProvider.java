package mekanism.api.providers;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IChemicalProvider<CHEMICAL extends Chemical<CHEMICAL>> extends IBaseProvider {
   CHEMICAL getChemical();

   ChemicalStack<CHEMICAL> getStack(long var1);

   @Override
   default ResourceLocation getRegistryName() {
      return this.getChemical().getRegistryName();
   }

   @Override
   default Component getTextComponent() {
      return this.getChemical().getTextComponent();
   }

   @Override
   default String getTranslationKey() {
      return this.getChemical().getTranslationKey();
   }
}
