package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IEntityTypeProvider extends IBaseProvider {
   EntityType<?> getEntityType();

   @Override
   default ResourceLocation getRegistryName() {
      return ForgeRegistries.ENTITY_TYPES.getKey(this.getEntityType());
   }

   @Override
   default Component getTextComponent() {
      return this.getEntityType().m_20676_();
   }

   @Override
   default String getTranslationKey() {
      return this.getEntityType().m_20675_();
   }
}
