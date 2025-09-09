package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FluidRecipeData implements RecipeUpgradeData<FluidRecipeData> {
   private final List<IExtendedFluidTank> fluidTanks;

   FluidRecipeData(ListTag tanks) {
      int count = DataHandlerUtils.getMaxId(tanks, "Tank");
      this.fluidTanks = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         this.fluidTanks.add(BasicFluidTank.create(Integer.MAX_VALUE, null));
      }

      DataHandlerUtils.readContainers(this.fluidTanks, tanks);
   }

   private FluidRecipeData(List<IExtendedFluidTank> fluidTanks) {
      this.fluidTanks = fluidTanks;
   }

   @Nullable
   public FluidRecipeData merge(FluidRecipeData other) {
      List<IExtendedFluidTank> allTanks = new ArrayList<>(this.fluidTanks);
      allTanks.addAll(other.fluidTanks);
      return new FluidRecipeData(allTanks);
   }

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.fluidTanks.isEmpty()) {
         return true;
      } else {
         Item item = stack.m_41720_();
         Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
         final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
         if (capability.isPresent()) {
            IFluidHandlerItem fluidHandler = capability.get();

            for (int i = 0; i < fluidHandler.getTanks(); i++) {
               fluidTanks.add(BasicFluidTank.create(fluidHandler.getTankCapacity(i), fluid -> fluidHandler.isFluidValid(i, fluid), null));
            }
         } else {
            if (!(item instanceof BlockItem blockItem)) {
               return false;
            }

            TileEntityMekanism tile = this.getTileFromBlock(blockItem.m_40614_());
            if (tile == null || !tile.handles(SubstanceType.FLUID)) {
               return false;
            }

            for (int i = 0; i < tile.getTanks(); i++) {
               int tank = i;
               fluidTanks.add(BasicFluidTank.create(tile.getTankCapacity(tank), fluid -> tile.isFluidValid(tank, fluid), null));
            }
         }

         if (fluidTanks.isEmpty()) {
            return true;
         } else {
            IMekanismFluidHandler outputHandler = new IMekanismFluidHandler() {
               @NotNull
               @Override
               public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
                  return fluidTanks;
               }

               @Override
               public void onContentsChanged() {
               }
            };
            boolean hasData = false;

            for (IExtendedFluidTank fluidTank : this.fluidTanks) {
               if (!fluidTank.isEmpty()) {
                  if (!outputHandler.insertFluid(fluidTank.getFluid(), Action.EXECUTE).isEmpty()) {
                     return false;
                  }

                  hasData = true;
               }
            }

            if (hasData) {
               ItemDataUtils.writeContainers(stack, "FluidTanks", fluidTanks);
            }

            return true;
         }
      }
   }
}
