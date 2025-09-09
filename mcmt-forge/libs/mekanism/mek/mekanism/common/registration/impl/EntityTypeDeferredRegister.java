package mekanism.common.registration.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import mekanism.common.Mekanism;
import mekanism.common.registration.WrappedDeferredRegister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeDeferredRegister extends WrappedDeferredRegister<EntityType<?>> {
   private Map<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder>> livingEntityAttributes = new HashMap<>();

   public EntityTypeDeferredRegister(String modid) {
      super(modid, ForgeRegistries.ENTITY_TYPES);
   }

   public <ENTITY extends LivingEntity> EntityTypeRegistryObject<ENTITY> register(
      String name, Builder<ENTITY> builder, Supplier<net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder> attributes
   ) {
      EntityTypeRegistryObject<ENTITY> entityTypeRO = this.register(name, builder);
      this.livingEntityAttributes.put(entityTypeRO, attributes);
      return entityTypeRO;
   }

   public <ENTITY extends Entity> EntityTypeRegistryObject<ENTITY> register(String name, Builder<ENTITY> builder) {
      return this.register(name, () -> builder.m_20712_(name), EntityTypeRegistryObject::new);
   }

   @Override
   public void register(IEventBus bus) {
      super.register(bus);
      bus.addListener(this::registerEntityAttributes);
   }

   private void registerEntityAttributes(EntityAttributeCreationEvent event) {
      if (this.livingEntityAttributes == null) {
         Mekanism.logger.error("GlobalEntityTypeAttributes have already been set. This should not happen.");
      } else {
         for (Entry<EntityTypeRegistryObject<? extends LivingEntity>, Supplier<net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder>> entry : this.livingEntityAttributes
            .entrySet()) {
            event.put((EntityType)entry.getKey().get(), entry.getValue().get().m_22265_());
         }

         this.livingEntityAttributes = null;
      }
   }
}
