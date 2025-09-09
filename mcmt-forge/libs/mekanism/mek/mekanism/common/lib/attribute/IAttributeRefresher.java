package mekanism.common.lib.attribute;

import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface IAttributeRefresher {
   void addToBuilder(Builder<Attribute, AttributeModifier> builder);
}
