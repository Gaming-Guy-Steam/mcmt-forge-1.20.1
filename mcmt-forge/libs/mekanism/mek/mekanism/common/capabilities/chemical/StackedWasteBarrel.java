package mekanism.common.capabilities.chemical;

import java.util.Objects;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.attribute.GasAttributes;
import mekanism.common.capabilities.chemical.variable.VariableCapacityChemicalTank;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.TileEntityRadioactiveWasteBarrel;
import mekanism.common.util.WorldUtils;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class StackedWasteBarrel extends VariableCapacityChemicalTank<Gas, GasStack> implements IGasHandler, IGasTank {
   private static final ChemicalAttributeValidator ATTRIBUTE_VALIDATOR = ChemicalAttributeValidator.createStrict(GasAttributes.Radiation.class);
   private final TileEntityRadioactiveWasteBarrel tile;

   public static StackedWasteBarrel create(TileEntityRadioactiveWasteBarrel tile, @Nullable IContentsListener listener) {
      Objects.requireNonNull(tile, "Radioactive Waste Barrel tile entity cannot be null");
      return new StackedWasteBarrel(tile, listener);
   }

   protected StackedWasteBarrel(TileEntityRadioactiveWasteBarrel tile, @Nullable IContentsListener listener) {
      super(
         MekanismConfig.general.radioactiveWasteBarrelMaxGas,
         ChemicalTankBuilder.GAS.alwaysTrueBi,
         ChemicalTankBuilder.GAS.alwaysTrueBi,
         ChemicalTankBuilder.GAS.alwaysTrue,
         ATTRIBUTE_VALIDATOR,
         listener
      );
      this.tile = tile;
   }

   public GasStack insert(GasStack stack, Action action, AutomationType automationType) {
      GasStack remainder = super.insert(stack, action, automationType);
      if (!remainder.isEmpty()) {
         TileEntityRadioactiveWasteBarrel tileAbove = WorldUtils.getTileEntity(
            TileEntityRadioactiveWasteBarrel.class, this.tile.m_58904_(), this.tile.m_58899_().m_7494_()
         );
         if (tileAbove != null) {
            remainder = tileAbove.getGasTank().insert(remainder, action, AutomationType.EXTERNAL);
         }
      }

      return remainder;
   }

   @Override
   public long growStack(long amount, Action action) {
      long grownAmount = super.growStack(amount, action);
      if (amount > 0L && grownAmount < amount && !this.tile.getActive()) {
         TileEntityRadioactiveWasteBarrel tileAbove = WorldUtils.getTileEntity(
            TileEntityRadioactiveWasteBarrel.class, this.tile.m_58904_(), this.tile.m_58899_().m_7494_()
         );
         if (tileAbove != null) {
            long leftOverToInsert = amount - grownAmount;
            GasStack remainder = tileAbove.getGasTank().insert(new GasStack(this.stored, leftOverToInsert), action, AutomationType.EXTERNAL);
            grownAmount += leftOverToInsert - remainder.getAmount();
         }
      }

      return grownAmount;
   }
}
