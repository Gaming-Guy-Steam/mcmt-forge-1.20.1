package mekanism.common.recipe.upgrade.chemical;

import java.util.List;
import java.util.function.Predicate;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class SlurryRecipeData extends ChemicalRecipeData<Slurry, SlurryStack, ISlurryTank, ISlurryHandler> {
   public SlurryRecipeData(ListTag tanks) {
      super(tanks);
   }

   private SlurryRecipeData(List<ISlurryTank> tanks) {
      super(tanks);
   }

   protected SlurryRecipeData create(List<ISlurryTank> tanks) {
      return new SlurryRecipeData(tanks);
   }

   @Override
   protected SubstanceType getSubstanceType() {
      return SubstanceType.SLURRY;
   }

   @Override
   protected ChemicalTankBuilder<Slurry, SlurryStack, ISlurryTank> getTankBuilder() {
      return ChemicalTankBuilder.SLURRY;
   }

   protected ISlurryHandler getOutputHandler(List<ISlurryTank> tanks) {
      return new ISlurryHandler.IMekanismSlurryHandler() {
         @NotNull
         @Override
         public List<ISlurryTank> getChemicalTanks(@Nullable Direction side) {
            return tanks;
         }

         @Override
         public void onContentsChanged() {
         }
      };
   }

   @Override
   protected Capability<ISlurryHandler> getCapability() {
      return Capabilities.SLURRY_HANDLER;
   }

   protected Predicate<Slurry> cloneValidator(ISlurryHandler handler, int tank) {
      return type -> handler.isValid(tank, new SlurryStack(type, 1L));
   }

   protected ISlurryHandler getHandlerFromTile(TileEntityMekanism tile) {
      return (ISlurryHandler)tile.getSlurryManager().getInternal();
   }
}
