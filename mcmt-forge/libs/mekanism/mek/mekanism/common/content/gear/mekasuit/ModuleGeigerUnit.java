package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@ParametersAreNotNullByDefault
public class ModuleGeigerUnit implements ICustomModule<ModuleGeigerUnit> {
   private static final ResourceLocation icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "geiger_counter.png");

   @Override
   public void addHUDElements(IModule<ModuleGeigerUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
      if (module.isEnabled()) {
         double magnitude = RadiationManager.get().getClientEnvironmentalRadiation();
         Component text = UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SV, 2);
         if (MekanismConfig.common.enableDecayTimers.get() && magnitude > 1.0E-7) {
            double maxMagnitude = RadiationManager.get().getClientMaxMagnitude();
            text = MekanismLang.GENERIC_WITH_PARENTHESIS
               .translate(new Object[]{text, TextUtils.getHoursMinutes(RadiationManager.get().getDecayTime(maxMagnitude, true))});
         }

         IHUDElement.HUDColor color;
         if (magnitude <= 1.0E-7) {
            color = IHUDElement.HUDColor.REGULAR;
         } else {
            color = magnitude < 0.1 ? IHUDElement.HUDColor.WARNING : IHUDElement.HUDColor.DANGER;
         }

         hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(icon, text, color));
      }
   }
}
