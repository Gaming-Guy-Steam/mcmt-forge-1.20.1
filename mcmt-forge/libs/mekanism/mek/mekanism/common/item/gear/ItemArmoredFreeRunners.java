package mekanism.common.item.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemArmoredFreeRunners extends ItemFreeRunners implements IAttributeRefresher {
   private static final ItemArmoredFreeRunners.ArmoredFreeRunnerMaterial ARMORED_FREE_RUNNER_MATERIAL = new ItemArmoredFreeRunners.ArmoredFreeRunnerMaterial();
   private final AttributeCache attributeCache = new AttributeCache(
      this,
      MekanismConfig.gear.armoredFreeRunnerArmor,
      MekanismConfig.gear.armoredFreeRunnerToughness,
      MekanismConfig.gear.armoredFreeRunnerKnockbackResistance
   );

   public ItemArmoredFreeRunners(Properties properties) {
      super(ARMORED_FREE_RUNNER_MATERIAL, properties);
   }

   @Override
   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.armoredFreeRunners());
   }

   public int m_40404_() {
      return this.m_40401_().m_7366_(this.m_266204_());
   }

   public float m_40405_() {
      return this.m_40401_().m_6651_();
   }

   @NotNull
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
      return (Multimap<Attribute, AttributeModifier>)(slot == this.m_40402_() ? this.attributeCache.get() : ImmutableMultimap.of());
   }

   @Override
   public void addToBuilder(Builder<Attribute, AttributeModifier> builder) {
      UUID modifier = (UUID)f_265987_.get(this.m_266204_());
      builder.put(Attributes.f_22284_, new AttributeModifier(modifier, "Armor modifier", this.m_40404_(), Operation.ADDITION));
      builder.put(Attributes.f_22285_, new AttributeModifier(modifier, "Armor toughness", this.m_40405_(), Operation.ADDITION));
      builder.put(Attributes.f_22278_, new AttributeModifier(modifier, "Armor knockback resistance", this.m_40401_().m_6649_(), Operation.ADDITION));
   }

   @NothingNullByDefault
   private static class ArmoredFreeRunnerMaterial extends ItemFreeRunners.FreeRunnerMaterial {
      @Override
      public int m_7366_(Type armorType) {
         return armorType == Type.BOOTS ? MekanismConfig.gear.armoredFreeRunnerArmor.getOrDefault() : 0;
      }

      @Override
      public String m_6082_() {
         return "mekanism:free_runners_armored";
      }

      @Override
      public float m_6651_() {
         return MekanismConfig.gear.armoredFreeRunnerToughness.getOrDefault();
      }

      @Override
      public float m_6649_() {
         return MekanismConfig.gear.armoredFreeRunnerKnockbackResistance.getOrDefault();
      }
   }
}
