package mekanism.common.item.gear;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;

@NothingNullByDefault
public abstract class BaseSpecialArmorMaterial implements ArmorMaterial {
   public int m_266425_(Type armorType) {
      return 0;
   }

   public int m_6646_() {
      return 0;
   }

   public SoundEvent m_7344_() {
      return SoundEvents.f_11675_;
   }

   public Ingredient m_6230_() {
      return Ingredient.f_43901_;
   }

   public int m_7366_(Type armorType) {
      return 0;
   }

   public float m_6651_() {
      return 0.0F;
   }

   public float m_6649_() {
      return 0.0F;
   }
}
