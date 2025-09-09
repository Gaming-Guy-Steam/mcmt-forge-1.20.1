package mekanism.common.recipe.upgrade.chemical;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.recipe.upgrade.RecipeUpgradeData;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class ChemicalRecipeData<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>, HANDLER extends IChemicalHandler<CHEMICAL, STACK>>
   implements RecipeUpgradeData<ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER>> {
   protected final List<TANK> tanks;

   protected ChemicalRecipeData(ListTag tanks) {
      int count = DataHandlerUtils.getMaxId(tanks, "Tank");
      this.tanks = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         this.tanks.add(this.getTankBuilder().createDummy(Long.MAX_VALUE));
      }

      DataHandlerUtils.readContainers(this.tanks, tanks);
   }

   protected ChemicalRecipeData(List<TANK> tanks) {
      this.tanks = tanks;
   }

   @Nullable
   public ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> merge(ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> other) {
      List<TANK> allTanks = new ArrayList<>(this.tanks);
      allTanks.addAll(other.tanks);
      return this.create(allTanks);
   }

   protected abstract ChemicalRecipeData<CHEMICAL, STACK, TANK, HANDLER> create(List<TANK> tanks);

   protected abstract SubstanceType getSubstanceType();

   protected abstract ChemicalTankBuilder<CHEMICAL, STACK, TANK> getTankBuilder();

   protected abstract HANDLER getOutputHandler(List<TANK> tanks);

   protected abstract Capability<HANDLER> getCapability();

   protected abstract Predicate<CHEMICAL> cloneValidator(HANDLER handler, int tank);

   protected abstract HANDLER getHandlerFromTile(TileEntityMekanism tile);

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.tanks.isEmpty()) {
         return true;
      } else {
         Optional<HANDLER> capability = stack.getCapability(this.getCapability()).resolve();
         HANDLER handler;
         if (capability.isPresent()) {
            handler = capability.get();
         } else {
            if (!(stack.m_41720_() instanceof BlockItem blockItem)) {
               return false;
            }

            TileEntityMekanism tile = null;
            if (blockItem.m_40614_() instanceof IHasTileEntity<?> hasTileEntity) {
               BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
               if (tileEntity instanceof TileEntityMekanism) {
                  tile = (TileEntityMekanism)tileEntity;
               }
            }

            if (tile == null || !tile.handles(this.getSubstanceType())) {
               return false;
            }

            handler = this.getHandlerFromTile(tile);
         }

         int tankCount = handler.getTanks();
         if (tankCount == 0) {
            return true;
         } else {
            List<TANK> tanks = new ArrayList<>();

            for (int tank = 0; tank < tankCount; tank++) {
               tanks.add(this.getTankBuilder().create(handler.getTankCapacity(tank), this.cloneValidator(handler, tank), null));
            }

            HANDLER outputHandler = this.getOutputHandler(tanks);
            boolean hasData = false;

            for (TANK tank : this.tanks) {
               if (!tank.isEmpty()) {
                  if (!outputHandler.insertChemical(tank.getStack(), Action.EXECUTE).isEmpty()) {
                     return false;
                  }

                  hasData = true;
               }
            }

            if (hasData) {
               ItemDataUtils.writeContainers(stack, this.getSubstanceType().getContainerTag(), tanks);
            }

            return true;
         }
      }
   }
}
