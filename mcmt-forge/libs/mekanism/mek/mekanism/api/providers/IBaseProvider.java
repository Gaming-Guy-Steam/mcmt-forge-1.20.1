package mekanism.api.providers;

import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public interface IBaseProvider extends IHasTextComponent, IHasTranslationKey {
   ResourceLocation getRegistryName();

   default String getName() {
      return this.getRegistryName().m_135815_();
   }

   @Override
   default Component getTextComponent() {
      return TextComponentUtil.translate(this.getTranslationKey());
   }
}
