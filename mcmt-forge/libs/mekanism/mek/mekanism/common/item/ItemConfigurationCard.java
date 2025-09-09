package mekanism.common.item;

import java.util.List;
import java.util.Optional;
import mekanism.api.IConfigCardAccess;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemConfigurationCard extends Item {
   public ItemConfigurationCard(Properties properties) {
      super(properties.m_41487_(1).m_41497_(Rarity.UNCOMMON));
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, List<Component> tooltip, @NotNull TooltipFlag flag) {
      tooltip.add(
         MekanismLang.CONFIG_CARD_HAS_DATA.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, this.getConfigCardName(this.getData(stack))})
      );
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext context) {
      Player player = context.m_43723_();
      if (player == null) {
         return InteractionResult.PASS;
      } else {
         Level world = context.m_43725_();
         BlockPos pos = context.m_8083_();
         Direction side = context.m_43719_();
         BlockEntity tile = WorldUtils.getTileEntity(world, pos);
         Optional<IConfigCardAccess> configCardSupport = CapabilityUtils.getCapability(tile, Capabilities.CONFIG_CARD, side).resolve();
         if (configCardSupport.isPresent()) {
            if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
               return InteractionResult.FAIL;
            } else {
               ItemStack stack = context.m_43722_();
               if (player.m_6144_()) {
                  if (!world.f_46443_) {
                     IConfigCardAccess configCardAccess = configCardSupport.get();
                     String translationKey = configCardAccess.getConfigCardName();
                     CompoundTag data = configCardAccess.getConfigurationData(player);
                     data.m_128359_("dataName", translationKey);
                     NBTUtils.writeRegistryEntry(data, "dataType", ForgeRegistries.BLOCK_ENTITY_TYPES, configCardAccess.getConfigurationDataType());
                     ItemDataUtils.setCompound(stack, "data", data);
                     player.m_213846_(
                        MekanismUtils.logFormat(
                           MekanismLang.CONFIG_CARD_GOT.translate(new Object[]{EnumColor.INDIGO, TextComponentUtil.translate(translationKey)})
                        )
                     );
                     MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger((ServerPlayer)player, true);
                  }
               } else {
                  CompoundTag data = this.getData(stack);
                  BlockEntityType<?> storedType = this.getStoredTileType(data);
                  if (storedType == null) {
                     return InteractionResult.PASS;
                  }

                  if (!world.f_46443_) {
                     IConfigCardAccess configCardAccess = configCardSupport.get();
                     if (configCardAccess.isConfigurationDataCompatible(storedType)) {
                        configCardAccess.setConfigurationData(player, data);
                        configCardAccess.configurationDataSet();
                        player.m_213846_(
                           MekanismUtils.logFormat(
                              EnumColor.DARK_GREEN, MekanismLang.CONFIG_CARD_SET.translate(new Object[]{EnumColor.INDIGO, this.getConfigCardName(data)})
                           )
                        );
                        MekanismCriteriaTriggers.CONFIGURATION_CARD.trigger((ServerPlayer)player, false);
                     } else {
                        player.m_213846_(MekanismUtils.logFormat(EnumColor.RED, MekanismLang.CONFIG_CARD_UNEQUAL));
                     }
                  }
               }

               return InteractionResult.m_19078_(world.f_46443_);
            }
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   private CompoundTag getData(ItemStack stack) {
      CompoundTag data = ItemDataUtils.getCompound(stack, "data");
      return data.m_128456_() ? null : data;
   }

   @Nullable
   @Contract("null -> null")
   private BlockEntityType<?> getStoredTileType(@Nullable CompoundTag data) {
      if (data != null && data.m_128425_("dataType", 8)) {
         ResourceLocation tileRegistryName = ResourceLocation.m_135820_(data.m_128461_("dataType"));
         return tileRegistryName == null ? null : (BlockEntityType)ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(tileRegistryName);
      } else {
         return null;
      }
   }

   private Component getConfigCardName(@Nullable CompoundTag data) {
      return data != null && data.m_128425_("dataName", 8)
         ? TextComponentUtil.translate(data.m_128461_("dataName"))
         : MekanismLang.NONE.translate(new Object[0]);
   }

   public boolean hasData(ItemStack stack) {
      CompoundTag data = this.getData(stack);
      return data != null && data.m_128425_("dataName", 8);
   }
}
