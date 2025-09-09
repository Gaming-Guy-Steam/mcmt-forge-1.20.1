package mekanism.common.capabilities.chemical.item;

import java.util.List;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsReturnNonnullByDefault
@ParametersAreNotNullByDefault
public abstract class ItemStackMekanismChemicalHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>>
   extends ItemCapabilityWrapper.ItemCapability
   implements IMekanismChemicalHandler<CHEMICAL, STACK, TANK> {
   protected List<TANK> tanks;

   @Override
   protected void init() {
      super.init();
      this.tanks = this.getInitialTanks();
   }

   @Override
   protected void load() {
      super.load();
      ItemDataUtils.readContainers(this.getStack(), this.getNbtKey(), this.getChemicalTanks(null));
   }

   @Override
   public void onContentsChanged() {
      ItemDataUtils.writeContainers(this.getStack(), this.getNbtKey(), this.getChemicalTanks(null));
   }

   @Override
   public List<TANK> getChemicalTanks(@Nullable Direction side) {
      return this.tanks;
   }

   protected abstract List<TANK> getInitialTanks();

   protected abstract String getNbtKey();
}
