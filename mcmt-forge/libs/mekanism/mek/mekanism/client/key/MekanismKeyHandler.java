package mekanism.client.key;

import mekanism.client.ClientRegistrationUtil;
import mekanism.client.MekanismClient;
import mekanism.client.sound.SoundHandler;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.curios.CuriosIntegration;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.item.interfaces.IGasItem;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.network.to_server.PacketModeChange;
import mekanism.common.network.to_server.PacketModeChangeCurios;
import mekanism.common.network.to_server.PacketOpenGui;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyModifier;
import top.theillusivec4.curios.api.SlotContext;

public class MekanismKeyHandler {
   public static final KeyMapping handModeSwitchKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_HAND_MODE)
      .conflictInGame()
      .keyCode(78)
      .onKeyDown((kb, isRepeat) -> {
         Player player = Minecraft.m_91087_().f_91074_;
         if (player != null) {
            if (IModeItem.isModeItem(player, EquipmentSlot.MAINHAND, false)) {
               Mekanism.packetHandler().sendToServer(new PacketModeChange(EquipmentSlot.MAINHAND, player.m_6144_()));
            } else if (!IModeItem.isModeItem(player, EquipmentSlot.MAINHAND) && IModeItem.isModeItem(player, EquipmentSlot.OFFHAND, false)) {
               Mekanism.packetHandler().sendToServer(new PacketModeChange(EquipmentSlot.OFFHAND, player.m_6144_()));
            }
         }
      })
      .build();
   public static final KeyMapping headModeSwitchKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_HEAD_MODE)
      .conflictInGame()
      .keyCode(86)
      .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlot.HEAD))
      .build();
   public static final KeyMapping chestModeSwitchKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_CHEST_MODE)
      .conflictInGame()
      .keyCode(71)
      .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlot.CHEST))
      .build();
   public static final KeyMapping legsModeSwitchKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_LEGS_MODE)
      .conflictInGame()
      .keyCode(74)
      .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlot.LEGS))
      .build();
   public static final KeyMapping feetModeSwitchKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_FEET_MODE)
      .conflictInGame()
      .keyCode(66)
      .onKeyDown((kb, isRepeat) -> handlePotentialModeItem(EquipmentSlot.FEET))
      .build();
   public static final KeyMapping detailsKey = new MekKeyBindingBuilder().description(MekanismLang.KEY_DETAILS_MODE).conflictInGui().keyCode(340).build();
   public static final KeyMapping descriptionKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_DESCRIPTION_MODE)
      .conflictInGui()
      .modifier(KeyModifier.SHIFT)
      .keyCode(78)
      .build();
   public static final KeyMapping moduleTweakerKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_MODULE_TWEAKER)
      .conflictInGame()
      .keyCode(92)
      .onKeyDown((kb, isRepeat) -> {
         Player player = Minecraft.m_91087_().f_91074_;
         if (player != null && ModuleTweakerContainer.hasTweakableItem(player)) {
            Mekanism.packetHandler().sendToServer(new PacketOpenGui(PacketOpenGui.GuiType.MODULE_TWEAKER));
         }
      })
      .build();
   public static final KeyMapping boostKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_BOOST)
      .conflictInGame()
      .keyCode(341)
      .onKeyDown((kb, isRepeat) -> MekanismClient.updateKey(kb, 1))
      .onKeyUp(kb -> MekanismClient.updateKey(kb, 1))
      .build();
   public static final KeyMapping hudKey = new MekKeyBindingBuilder()
      .description(MekanismLang.KEY_HUD)
      .conflictInGame()
      .keyCode(72)
      .onKeyDown((kb, isRepeat) -> {
         MekanismConfig.client.enableHUD.set(!MekanismConfig.client.enableHUD.get());
         MekanismConfig.client.save();
      })
      .build();

   public static void registerKeybindings(RegisterKeyMappingsEvent event) {
      ClientRegistrationUtil.registerKeyBindings(
         event,
         handModeSwitchKey,
         headModeSwitchKey,
         chestModeSwitchKey,
         legsModeSwitchKey,
         feetModeSwitchKey,
         detailsKey,
         descriptionKey,
         moduleTweakerKey,
         boostKey,
         hudKey
      );
   }

   private static void handlePotentialModeItem(EquipmentSlot slot) {
      Player player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         if (IModeItem.isModeItem(player, slot)) {
            Mekanism.packetHandler().sendToServer(new PacketModeChange(slot, player.m_6144_()));
            SoundHandler.playSound(MekanismSounds.HYDRAULIC);
         } else if (Mekanism.hooks.CuriosLoaded) {
            CuriosIntegration.findFirstCurioAsResult(
                  player,
                  stack -> stack.canEquip(slot, player) && IModeItem.isModeItem(stack, slot)
                     ? !(stack.m_41720_() instanceof IGasItem item && !item.hasGas(stack))
                     : false
               )
               .ifPresent(result -> {
                  SlotContext slotContext = result.slotContext();
                  Mekanism.packetHandler().sendToServer(new PacketModeChangeCurios(slotContext.identifier(), slotContext.index(), player.m_6144_()));
                  SoundHandler.playSound(MekanismSounds.HYDRAULIC);
               });
         }
      }
   }
}
