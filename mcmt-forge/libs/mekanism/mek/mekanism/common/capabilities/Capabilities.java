package mekanism.common.capabilities;

import mekanism.api.IAlloyInteraction;
import mekanism.api.IConfigCardAccess;
import mekanism.api.IConfigurable;
import mekanism.api.IEvaporationSolar;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.infuse.IInfusionHandler;
import mekanism.api.chemical.pigment.IPigmentHandler;
import mekanism.api.chemical.slurry.ISlurryHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.heat.IHeatHandler;
import mekanism.api.lasers.ILaserDissipation;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.radiation.capability.IRadiationEntity;
import mekanism.api.radiation.capability.IRadiationShielding;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {
   public static final Capability<IGasHandler> GAS_HANDLER = CapabilityManager.get(new CapabilityToken<IGasHandler>() {});
   public static final Capability<IInfusionHandler> INFUSION_HANDLER = CapabilityManager.get(new CapabilityToken<IInfusionHandler>() {});
   public static final Capability<IPigmentHandler> PIGMENT_HANDLER = CapabilityManager.get(new CapabilityToken<IPigmentHandler>() {});
   public static final Capability<ISlurryHandler> SLURRY_HANDLER = CapabilityManager.get(new CapabilityToken<ISlurryHandler>() {});
   public static final Capability<IHeatHandler> HEAT_HANDLER = CapabilityManager.get(new CapabilityToken<IHeatHandler>() {});
   public static final Capability<IStrictEnergyHandler> STRICT_ENERGY = CapabilityManager.get(new CapabilityToken<IStrictEnergyHandler>() {});
   public static final Capability<IConfigurable> CONFIGURABLE = CapabilityManager.get(new CapabilityToken<IConfigurable>() {});
   public static final Capability<IAlloyInteraction> ALLOY_INTERACTION = CapabilityManager.get(new CapabilityToken<IAlloyInteraction>() {});
   public static final Capability<IConfigCardAccess> CONFIG_CARD = CapabilityManager.get(new CapabilityToken<IConfigCardAccess>() {});
   public static final Capability<IEvaporationSolar> EVAPORATION_SOLAR = CapabilityManager.get(new CapabilityToken<IEvaporationSolar>() {});
   public static final Capability<ILaserReceptor> LASER_RECEPTOR = CapabilityManager.get(new CapabilityToken<ILaserReceptor>() {});
   public static final Capability<ILaserDissipation> LASER_DISSIPATION = CapabilityManager.get(new CapabilityToken<ILaserDissipation>() {});
   public static final Capability<IRadiationShielding> RADIATION_SHIELDING = CapabilityManager.get(new CapabilityToken<IRadiationShielding>() {});
   public static final Capability<IRadiationEntity> RADIATION_ENTITY = CapabilityManager.get(new CapabilityToken<IRadiationEntity>() {});
   public static final Capability<IOwnerObject> OWNER_OBJECT = CapabilityManager.get(new CapabilityToken<IOwnerObject>() {});
   public static final Capability<ISecurityObject> SECURITY_OBJECT = CapabilityManager.get(new CapabilityToken<ISecurityObject>() {});

   private Capabilities() {
   }
}
