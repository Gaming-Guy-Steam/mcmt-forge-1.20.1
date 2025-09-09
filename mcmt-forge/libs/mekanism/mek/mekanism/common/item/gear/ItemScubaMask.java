package mekanism.common.item.gear;

import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.RenderPropertiesProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemScubaMask extends ItemSpecialArmor {
   private static final ItemScubaMask.ScubaMaskMaterial SCUBA_MASK_MATERIAL = new ItemScubaMask.ScubaMaskMaterial();

   public ItemScubaMask(Properties properties) {
      super(SCUBA_MASK_MATERIAL, Type.HELMET, properties.m_41497_(Rarity.RARE).setNoRepair());
   }

   public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
      consumer.accept(RenderPropertiesProvider.scubaMask());
   }

   public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
      return super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.m_41809_();
   }

   @NothingNullByDefault
   protected static class ScubaMaskMaterial extends BaseSpecialArmorMaterial {
      public String m_6082_() {
         return "mekanism:scuba_mask";
      }
   }
}
