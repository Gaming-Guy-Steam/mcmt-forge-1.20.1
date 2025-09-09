package mekanism.common.integration.gender;

import java.util.function.Consumer;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;

public class GenderCapabilityHelper {
   public static void addGenderCapability(ArmorItem item, Consumer<ItemCapabilityWrapper.ItemCapability> addCapability) {
      if (Mekanism.hooks.WildfireGenderModLoaded && item.m_266204_() == Type.CHESTPLATE) {
         MekanismGenderArmor genderArmor = null;
         if (item == MekanismItems.HAZMAT_GOWN.m_5456_()) {
            genderArmor = MekanismGenderArmor.HAZMAT;
         } else if (item == MekanismItems.JETPACK.m_5456_() || item == MekanismItems.SCUBA_TANK.m_5456_()) {
            genderArmor = MekanismGenderArmor.OPEN_FRONT;
         } else if (item == MekanismItems.ARMORED_JETPACK.m_5456_() || item == MekanismItems.MEKASUIT_BODYARMOR.m_5456_()) {
            genderArmor = MekanismGenderArmor.HIDES_BREASTS;
         }

         if (genderArmor != null) {
            addCapability.accept(genderArmor);
         }
      }
   }
}
