package mekanism.common.network;

import mekanism.common.Mekanism;
import mekanism.common.network.to_client.PacketLaserHitBlock;
import mekanism.common.network.to_client.PacketLightningRender;
import mekanism.common.network.to_client.PacketPlayerData;
import mekanism.common.network.to_client.PacketPortalFX;
import mekanism.common.network.to_client.PacketQIOItemViewerGuiSync;
import mekanism.common.network.to_client.PacketRadiationData;
import mekanism.common.network.to_client.PacketResetPlayerClient;
import mekanism.common.network.to_client.PacketSecurityUpdate;
import mekanism.common.network.to_client.PacketShowModeChange;
import mekanism.common.network.to_client.PacketTransmitterUpdate;
import mekanism.common.network.to_client.PacketTransporterUpdate;
import mekanism.common.network.to_client.PacketUpdateTile;
import mekanism.common.network.to_client.container.PacketUpdateContainer;
import mekanism.common.network.to_server.PacketAddTrusted;
import mekanism.common.network.to_server.PacketConfigurationUpdate;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketEditFilter;
import mekanism.common.network.to_server.PacketGearStateUpdate;
import mekanism.common.network.to_server.PacketGuiButtonPress;
import mekanism.common.network.to_server.PacketGuiInteract;
import mekanism.common.network.to_server.PacketGuiItemDataRequest;
import mekanism.common.network.to_server.PacketGuiSetEnergy;
import mekanism.common.network.to_server.PacketGuiSetFrequency;
import mekanism.common.network.to_server.PacketGuiSetFrequencyColor;
import mekanism.common.network.to_server.PacketKey;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketModeChangeCurios;
import mekanism.common.network.to_server.PacketNewFilter;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.network.to_server.PacketPortableTeleporterTeleport;
import mekanism.common.network.to_server.PacketQIOClearCraftingWindow;
import mekanism.common.network.to_server.PacketQIOFillCraftingWindow;
import mekanism.common.network.to_server.PacketQIOItemViewerSlotInteract;
import mekanism.common.network.to_server.PacketRadialModeChange;
import mekanism.common.network.to_server.PacketRemoveModule;
import mekanism.common.network.to_server.PacketRobit;
import mekanism.common.network.to_server.PacketSecurityMode;
import mekanism.common.network.to_server.PacketUpdateModuleSettings;
import mekanism.common.network.to_server.PacketWindowSelect;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler extends BasePacketHandler {
   private final SimpleChannel netHandler = createChannel(Mekanism.rl("mekanism"), Mekanism.instance.versionNumber);

   @Override
   protected SimpleChannel getChannel() {
      return this.netHandler;
   }

   @Override
   public void initialize() {
      this.registerClientToServer(PacketAddTrusted.class, PacketAddTrusted::decode);
      this.registerClientToServer(PacketConfigurationUpdate.class, PacketConfigurationUpdate::decode);
      this.registerClientToServer(PacketDropperUse.class, PacketDropperUse::decode);
      this.registerClientToServer(PacketEditFilter.class, PacketEditFilter::decode);
      this.registerClientToServer(PacketGearStateUpdate.class, PacketGearStateUpdate::decode);
      this.registerClientToServer(PacketGuiButtonPress.class, PacketGuiButtonPress::decode);
      this.registerClientToServer(PacketGuiInteract.class, PacketGuiInteract::decode);
      this.registerClientToServer(PacketGuiItemDataRequest.class, PacketGuiItemDataRequest::decode);
      this.registerClientToServer(PacketGuiSetEnergy.class, PacketGuiSetEnergy::decode);
      this.registerClientToServer(PacketGuiSetFrequency.class, PacketGuiSetFrequency::decode);
      this.registerClientToServer(PacketGuiSetFrequencyColor.class, PacketGuiSetFrequencyColor::decode);
      this.registerClientToServer(PacketKey.class, PacketKey::decode);
      this.registerClientToServer(PacketModeChange.class, PacketModeChange::decode);
      this.registerClientToServer(PacketModeChangeCurios.class, PacketModeChangeCurios::decode);
      this.registerClientToServer(PacketNewFilter.class, PacketNewFilter::decode);
      this.registerClientToServer(PacketOpenGui.class, PacketOpenGui::decode);
      this.registerClientToServer(PacketPortableTeleporterTeleport.class, PacketPortableTeleporterTeleport::decode);
      this.registerClientToServer(PacketQIOClearCraftingWindow.class, PacketQIOClearCraftingWindow::decode);
      this.registerClientToServer(PacketQIOFillCraftingWindow.class, PacketQIOFillCraftingWindow::decode);
      this.registerClientToServer(PacketQIOItemViewerSlotInteract.class, PacketQIOItemViewerSlotInteract::decode);
      this.registerClientToServer(PacketRadialModeChange.class, PacketRadialModeChange::decode);
      this.registerClientToServer(PacketRemoveModule.class, PacketRemoveModule::decode);
      this.registerClientToServer(PacketRobit.class, PacketRobit::decode);
      this.registerClientToServer(PacketSecurityMode.class, PacketSecurityMode::decode);
      this.registerClientToServer(PacketUpdateModuleSettings.class, PacketUpdateModuleSettings::decode);
      this.registerClientToServer(PacketWindowSelect.class, PacketWindowSelect::decode);
      this.registerServerToClient(PacketLaserHitBlock.class, PacketLaserHitBlock::decode);
      this.registerServerToClient(PacketLightningRender.class, PacketLightningRender::decode);
      this.registerServerToClient(PacketPlayerData.class, PacketPlayerData::decode);
      this.registerServerToClient(PacketPortalFX.class, PacketPortalFX::decode);
      this.registerServerToClient(PacketQIOItemViewerGuiSync.class, PacketQIOItemViewerGuiSync::decode);
      this.registerServerToClient(PacketRadiationData.class, PacketRadiationData::decode);
      this.registerServerToClient(PacketResetPlayerClient.class, PacketResetPlayerClient::decode);
      this.registerServerToClient(PacketSecurityUpdate.class, PacketSecurityUpdate::decode);
      this.registerServerToClient(PacketShowModeChange.class, PacketShowModeChange::decode);
      this.registerServerToClient(PacketTransmitterUpdate.class, PacketTransmitterUpdate::decode);
      this.registerServerToClient(PacketTransporterUpdate.class, PacketTransporterUpdate::decode);
      this.registerServerToClient(PacketUpdateContainer.class, PacketUpdateContainer::decode);
      this.registerServerToClient(PacketUpdateTile.class, PacketUpdateTile::decode);
   }
}
