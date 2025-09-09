package mekanism.client.render.armor;

import net.minecraft.world.item.ArmorItem.Type;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public interface ISpecialGear extends IClientItemExtensions {
   @NotNull
   ICustomArmor getGearModel(Type type);
}
