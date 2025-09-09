package mekanism.common.item;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ILangEntry;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tags.TagUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemDictionary extends Item {
   public ItemDictionary(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.UNCOMMON));
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.descriptionKey)) {
         tooltip.add(MekanismLang.DESCRIPTION_DICTIONARY.translate(new Object[0]));
      } else {
         tooltip.add(
            MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, new Object[]{EnumColor.AQUA, MekanismKeyHandler.descriptionKey.m_90863_()})
         );
      }
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player != null) {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         BlockEntity tile = WorldUtils.getTileEntity(world, pos);
         if (tile != null || !player.m_6144_()) {
            if (!world.f_46443_) {
               BlockState blockState = world.m_8055_(pos);
               FluidState fluidState = blockState.m_60819_();
               Set<ResourceLocation> blockTags = TagUtils.tagNames(blockState.m_204343_());
               Set<ResourceLocation> fluidTags = fluidState.m_76178_() ? Collections.emptySet() : TagUtils.tagNames(fluidState.m_205075_());
               Set<ResourceLocation> tileTags = tile == null ? Collections.emptySet() : TagUtils.tagNames(ForgeRegistries.BLOCK_ENTITY_TYPES, tile.m_58903_());
               if (blockTags.isEmpty() && fluidTags.isEmpty() && tileTags.isEmpty()) {
                  player.m_213846_(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY));
               } else {
                  this.sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_TAGS_FOUND, blockTags);
                  this.sendTagsToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, fluidTags);
                  this.sendTagsToPlayer(player, MekanismLang.DICTIONARY_BLOCK_ENTITY_TYPE_TAGS_FOUND, tileTags);
               }
            }

            return InteractionResult.m_19078_(world.f_46443_);
         }
      }

      return InteractionResult.PASS;
   }

   @NotNull
   public InteractionResult m_6880_(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
      if (!player.m_6144_()) {
         if (!player.m_9236_().f_46443_) {
            this.sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_ENTITY_TYPE_TAGS_FOUND, entity.m_6095_().getTags());
         }

         return InteractionResult.m_19078_(player.m_9236_().f_46443_);
      } else {
         return InteractionResult.PASS;
      }
   }

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (player.m_6144_()) {
         if (!world.m_5776_()) {
            MekanismContainerTypes.DICTIONARY.tryOpenGui((ServerPlayer)player, hand, stack);
         }

         return InteractionResultHolder.m_19092_(stack, world.f_46443_);
      } else {
         BlockHitResult result = MekanismUtils.rayTrace(player, Fluid.ANY);
         if (result.m_6662_() != Type.MISS) {
            FluidState fluidState = world.m_6425_(result.m_82425_());
            if (!fluidState.m_76178_()) {
               if (!world.m_5776_()) {
                  this.sendTagsOrEmptyToPlayer(player, MekanismLang.DICTIONARY_FLUID_TAGS_FOUND, fluidState.m_205075_());
               }

               return InteractionResultHolder.m_19092_(stack, world.f_46443_);
            }
         }

         return InteractionResultHolder.m_19098_(stack);
      }
   }

   private void sendTagsOrEmptyToPlayer(Player player, ILangEntry tagsFoundEntry, Stream<? extends TagKey<?>> tags) {
      this.sendTagsOrEmptyToPlayer(player, tagsFoundEntry, TagUtils.tagNames(tags));
   }

   private void sendTagsOrEmptyToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
      if (tags.isEmpty()) {
         player.m_213846_(MekanismUtils.logFormat(MekanismLang.DICTIONARY_NO_KEY));
      } else {
         this.sendTagsToPlayer(player, tagsFoundEntry, tags);
      }
   }

   private void sendTagsToPlayer(Player player, ILangEntry tagsFoundEntry, Set<ResourceLocation> tags) {
      if (!tags.isEmpty()) {
         player.m_213846_(MekanismUtils.logFormat(tagsFoundEntry));

         for (ResourceLocation tag : tags) {
            player.m_213846_(MekanismLang.DICTIONARY_KEY.translateColored(EnumColor.DARK_GREEN, new Object[]{tag}));
         }
      }
   }
}
