package mekanism.common.content.gear;

import mekanism.api.gear.ICustomModule;
import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MekaSuitDispenseBehavior extends ModuleDispenseBehavior {
   @Override
   protected ICustomModule.ModuleDispenseResult performBuiltin(@NotNull BlockSource source, @NotNull ItemStack stack) {
      return ArmorItem.m_40398_(source, stack) ? ICustomModule.ModuleDispenseResult.HANDLED : super.performBuiltin(source, stack);
   }
}
