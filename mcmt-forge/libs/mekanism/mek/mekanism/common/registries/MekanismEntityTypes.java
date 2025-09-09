package mekanism.common.registries;

import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType.Builder;

public class MekanismEntityTypes {
   public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister("mekanism");
   public static final EntityTypeRegistryObject<EntityFlame> FLAME = ENTITY_TYPES.register(
      "flame", Builder.m_20704_(EntityFlame::new, MobCategory.MISC).m_20699_(0.5F, 0.5F).m_20719_()
   );
   public static final EntityTypeRegistryObject<EntityRobit> ROBIT = ENTITY_TYPES.register(
      "robit", Builder.m_20704_(EntityRobit::new, MobCategory.MISC).m_20699_(0.6F, 0.65F).m_20719_().m_20698_(), EntityRobit::getDefaultAttributes
   );

   private MekanismEntityTypes() {
   }
}
