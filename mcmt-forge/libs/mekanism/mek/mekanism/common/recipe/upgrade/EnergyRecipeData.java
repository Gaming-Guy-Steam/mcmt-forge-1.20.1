package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.DataHandlerUtils;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class EnergyRecipeData implements RecipeUpgradeData<EnergyRecipeData> {
   private final List<IEnergyContainer> energyContainers;

   EnergyRecipeData(ListTag containers) {
      int count = DataHandlerUtils.getMaxId(containers, "Container");
      this.energyContainers = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         this.energyContainers.add(BasicEnergyContainer.create(FloatingLong.MAX_VALUE, null));
      }

      DataHandlerUtils.readContainers(this.energyContainers, containers);
   }

   private EnergyRecipeData(List<IEnergyContainer> energyContainers) {
      this.energyContainers = energyContainers;
   }

   @Nullable
   public EnergyRecipeData merge(EnergyRecipeData other) {
      List<IEnergyContainer> allContainers = new ArrayList<>(this.energyContainers);
      allContainers.addAll(other.energyContainers);
      return new EnergyRecipeData(allContainers);
   }

   @Override
   public boolean applyToStack(ItemStack stack) {
      if (this.energyContainers.isEmpty()) {
         return true;
      } else {
         Item item = stack.m_41720_();
         Optional<IStrictEnergyHandler> capability = stack.getCapability(Capabilities.STRICT_ENERGY).resolve();
         final List<IEnergyContainer> energyContainers = new ArrayList<>();
         if (capability.isPresent()) {
            IStrictEnergyHandler energyHandler = capability.get();

            for (int container = 0; container < energyHandler.getEnergyContainerCount(); container++) {
               energyContainers.add(BasicEnergyContainer.create(energyHandler.getMaxEnergy(container), null));
            }
         } else {
            if (!(item instanceof BlockItem blockItem)) {
               return false;
            }

            TileEntityMekanism tile = this.getTileFromBlock(blockItem.m_40614_());
            if (tile == null || !tile.handles(SubstanceType.ENERGY)) {
               return false;
            }

            for (int container = 0; container < tile.getEnergyContainerCount(); container++) {
               energyContainers.add(BasicEnergyContainer.create(tile.getMaxEnergy(container), null));
            }
         }

         if (energyContainers.isEmpty()) {
            return true;
         } else {
            IMekanismStrictEnergyHandler outputHandler = new IMekanismStrictEnergyHandler() {
               @NotNull
               @Override
               public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
                  return energyContainers;
               }

               @Override
               public void onContentsChanged() {
               }
            };
            boolean hasData = false;

            for (IEnergyContainer energyContainer : this.energyContainers) {
               if (!energyContainer.isEmpty()) {
                  hasData = true;
                  if (!outputHandler.insertEnergy(energyContainer.getEnergy(), Action.EXECUTE).isZero()) {
                     break;
                  }
               }
            }

            if (hasData) {
               ItemDataUtils.writeContainers(stack, "EnergyContainers", energyContainers);
            }

            return true;
         }
      }
   }
}
