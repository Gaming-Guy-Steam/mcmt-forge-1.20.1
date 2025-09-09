package mekanism.common.item;

import java.util.List;
import java.util.Set;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.key.MekKeyHandler;
import mekanism.client.key.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemModule extends Item implements IModuleItem {
   private final IModuleDataProvider<?> moduleData;

   public ItemModule(IModuleDataProvider<?> moduleData, Properties properties) {
      super(properties);
      this.moduleData = moduleData;
   }

   public int getMaxStackSize(ItemStack stack) {
      return this.getModuleData().getMaxStackSize();
   }

   @Override
   public ModuleData<?> getModuleData() {
      return this.moduleData.getModuleData();
   }

   @NotNull
   public Rarity m_41460_(@NotNull ItemStack stack) {
      return this.getModuleData().getRarity();
   }

   public void m_7373_(@NotNull ItemStack stack, Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
      if (MekKeyHandler.isKeyPressed(MekanismKeyHandler.detailsKey)) {
         tooltip.add(MekanismLang.MODULE_SUPPORTED.translateColored(EnumColor.BRIGHT_GREEN, new Object[0]));
         IModuleHelper moduleHelper = IModuleHelper.INSTANCE;

         for (Item item : moduleHelper.getSupported(this.getModuleData())) {
            tooltip.add(MekanismLang.GENERIC_LIST.translate(new Object[]{item.m_7626_(new ItemStack(item))}));
         }

         Set<ModuleData<?>> conflicting = moduleHelper.getConflicting(this.getModuleData());
         if (!conflicting.isEmpty()) {
            tooltip.add(MekanismLang.MODULE_CONFLICTING.translateColored(EnumColor.RED, new Object[0]));

            for (ModuleData<?> module : conflicting) {
               tooltip.add(MekanismLang.GENERIC_LIST.translate(new Object[]{module}));
            }
         }
      } else {
         ModuleData<?> moduleData = this.getModuleData();
         tooltip.add(TextComponentUtil.translate(moduleData.getDescriptionTranslationKey()));
         tooltip.add(MekanismLang.MODULE_STACKABLE.translateColored(EnumColor.GRAY, new Object[]{EnumColor.AQUA, moduleData.getMaxStackSize()}));
         tooltip.add(
            MekanismLang.HOLD_FOR_SUPPORTED_ITEMS.translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, MekanismKeyHandler.detailsKey.m_90863_()})
         );
      }
   }

   @NotNull
   public String m_5524_() {
      return this.getModuleData().getTranslationKey();
   }
}
