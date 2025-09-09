package mekanism.common.tile.component;

import java.util.UUID;
import mekanism.api.security.SecurityMode;
import mekanism.common.integration.computer.BaseComputerHelper;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.ComputerMethodFactory;
import mekanism.common.integration.computer.MethodData;
import mekanism.common.integration.computer.annotation.MethodFactory;

@MethodFactory(
   target = TileComponentSecurity.class
)
public class TileComponentSecurity$ComputerHandler extends ComputerMethodFactory<TileComponentSecurity> {
   public TileComponentSecurity$ComputerHandler() {
      this.register(MethodData.builder("getOwnerUUID", TileComponentSecurity$ComputerHandler::getOwnerUUID_0).returnType(UUID.class));
      this.register(MethodData.builder("getOwnerName", TileComponentSecurity$ComputerHandler::getOwnerName_0).returnType(String.class));
      this.register(MethodData.builder("getSecurityMode", TileComponentSecurity$ComputerHandler::getSecurityMode_0).returnType(SecurityMode.class));
   }

   public static Object getOwnerUUID_0(TileComponentSecurity subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOwnerUUID());
   }

   public static Object getOwnerName_0(TileComponentSecurity subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getOwnerName());
   }

   public static Object getSecurityMode_0(TileComponentSecurity subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(subject.getComputerSecurityMode());
   }
}
