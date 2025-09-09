package mekanism.common.integration.computer;

import java.util.function.Predicate;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tile.interfaces.IComparatorSupport;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.tile.interfaces.ITileRedstone;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import org.jetbrains.annotations.Nullable;

public enum MethodRestriction implements Predicate<Object> {
   NONE(ConstantPredicates.alwaysTrue()),
   DIRECTIONAL(handler -> handler instanceof ITileDirectional directional && directional.isDirectional()),
   ENERGY(handler -> handler instanceof IMekanismStrictEnergyHandler energyHandler && energyHandler.canHandleEnergy()),
   MULTIBLOCK(handler -> handler instanceof TileEntityMultiblock<?> multiblock && multiblock.exposesMultiblockToComputer()),
   REDSTONE_CONTROL(handler -> handler instanceof ITileRedstone redstone && redstone.supportsRedstone()),
   COMPARATOR(handler -> handler instanceof IComparatorSupport comparatorSupport && comparatorSupport.supportsComparator());

   private final Predicate<Object> validator;

   private MethodRestriction(Predicate<Object> validator) {
      this.validator = validator;
   }

   @Override
   public boolean test(@Nullable Object handler) {
      return this.validator.test(handler);
   }
}
