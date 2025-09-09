package mekanism.common.content.gear.mekasuit;

import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils;
import mekanism.common.util.text.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

@ParametersAreNotNullByDefault
public class ModuleDosimeterUnit implements ICustomModule<ModuleDosimeterUnit> {
   private static final ResourceLocation icon = MekanismUtils.getResource(MekanismUtils.ResourceType.GUI_HUD, "dosimeter.png");

   @Override
   public void addHUDElements(IModule<ModuleDosimeterUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
      if (module.isEnabled()) {
         player.getCapability(Capabilities.RADIATION_ENTITY)
            .ifPresent(
               capability -> {
                  double radiation = IRadiationManager.INSTANCE.isRadiationEnabled() ? capability.getRadiation() : 0.0;
                  Component text = UnitDisplayUtils.getDisplayShort(radiation, UnitDisplayUtils.RadiationUnit.SV, 2);
                  if (MekanismConfig.common.enableDecayTimers.get() && radiation > 1.0E-5) {
                     text = MekanismLang.GENERIC_WITH_PARENTHESIS
                        .translate(new Object[]{text, TextUtils.getHoursMinutes(RadiationManager.get().getDecayTime(radiation, false))});
                  }

                  IHUDElement.HUDColor color;
                  if (radiation < 1.0E-5) {
                     color = IHUDElement.HUDColor.REGULAR;
                  } else {
                     color = radiation < 0.1 ? IHUDElement.HUDColor.WARNING : IHUDElement.HUDColor.DANGER;
                  }

                  hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(icon, text, color));
               }
            );
      }
   }
}
