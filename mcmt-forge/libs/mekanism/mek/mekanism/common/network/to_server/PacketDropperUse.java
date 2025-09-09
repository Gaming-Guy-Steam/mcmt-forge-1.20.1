package mekanism.common.network.to_server;

import java.util.List;
import java.util.Optional;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.Coord4D;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.tier.BaseTier;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.advancements.triggers.UseGaugeDropperTrigger;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.capabilities.chemical.dynamic.IGasTracker;
import mekanism.common.capabilities.chemical.dynamic.IInfusionTracker;
import mekanism.common.capabilities.chemical.dynamic.IPigmentTracker;
import mekanism.common.capabilities.chemical.dynamic.ISlurryTracker;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.network.NetworkEvent.Context;

public class PacketDropperUse implements IMekanismPacket {
   private final BlockPos pos;
   private final PacketDropperUse.DropperAction action;
   private final PacketDropperUse.TankType tankType;
   private final int tankId;

   public PacketDropperUse(BlockPos pos, PacketDropperUse.DropperAction action, PacketDropperUse.TankType tankType, int tankId) {
      this.pos = pos;
      this.action = action;
      this.tankType = tankType;
      this.tankId = tankId;
   }

   @Override
   public void handle(Context context) {
      ServerPlayer player = context.getSender();
      if (player != null && this.tankId >= 0) {
         ItemStack stack = player.f_36096_.m_142621_();
         if (!stack.m_41619_() && stack.m_41720_() instanceof ItemGaugeDropper) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, player.m_9236_(), this.pos);
            if (tile != null) {
               if (tile instanceof TileEntityMultiblock<?> multiblock) {
                  MultiblockData structure = multiblock.getMultiblock();
                  if (structure.isFormed()) {
                     this.handleTankType(structure, player, stack, new Coord4D(structure.getBounds().getCenter(), player.m_9236_()));
                  }
               } else {
                  if (this.action == PacketDropperUse.DropperAction.DUMP_TANK
                     && !player.m_7500_()
                     && Attribute.getBaseTier(tile.getBlockType()) == BaseTier.CREATIVE) {
                     return;
                  }

                  this.handleTankType(tile, player, stack, tile.getTileCoord());
               }
            }
         }
      }
   }

   private <HANDLER extends IMekanismFluidHandler & IGasTracker & IInfusionTracker & IPigmentTracker & ISlurryTracker> void handleTankType(
      HANDLER handler, ServerPlayer player, ItemStack stack, Coord4D coord
   ) {
      if (this.tankType == PacketDropperUse.TankType.FLUID_TANK) {
         IExtendedFluidTank fluidTank = handler.getFluidTank(this.tankId, null);
         if (fluidTank != null) {
            this.handleFluidTank(player, stack, fluidTank);
         }
      } else if (this.tankType == PacketDropperUse.TankType.GAS_TANK) {
         this.handleChemicalTanks(player, stack, handler.getGasTanks(null), coord);
      } else if (this.tankType == PacketDropperUse.TankType.INFUSION_TANK) {
         this.handleChemicalTanks(player, stack, handler.getInfusionTanks(null), coord);
      } else if (this.tankType == PacketDropperUse.TankType.PIGMENT_TANK) {
         this.handleChemicalTanks(player, stack, handler.getPigmentTanks(null), coord);
      } else if (this.tankType == PacketDropperUse.TankType.SLURRY_TANK) {
         this.handleChemicalTanks(player, stack, handler.getSlurryTanks(null), coord);
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void handleChemicalTanks(
      ServerPlayer player, ItemStack stack, List<TANK> tanks, Coord4D coord
   ) {
      if (this.tankId < tanks.size()) {
         this.handleChemicalTank(player, stack, tanks.get(this.tankId), coord);
      }
   }

   private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void handleChemicalTank(
      ServerPlayer player, ItemStack stack, IChemicalTank<CHEMICAL, STACK> tank, Coord4D coord
   ) {
      if (this.action == PacketDropperUse.DropperAction.DUMP_TANK) {
         if (!tank.isEmpty()) {
            if (tank instanceof IGasTank gasTank) {
               IRadiationManager.INSTANCE.dumpRadiation(coord, gasTank.getStack());
            }

            tank.setEmpty();
            MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.DUMP);
         }
      } else {
         Optional<IChemicalHandler<CHEMICAL, STACK>> cap = stack.getCapability(ChemicalUtil.getCapabilityForChemical(tank)).resolve();
         if (cap.isPresent()) {
            IChemicalHandler<CHEMICAL, STACK> handler = cap.get();
            if (handler instanceof IMekanismChemicalHandler<CHEMICAL, STACK, ?> chemicalHandler) {
               IChemicalTank<CHEMICAL, STACK> itemTank = chemicalHandler.getChemicalTank(0, null);
               if (itemTank != null) {
                  if (this.action == PacketDropperUse.DropperAction.FILL_DROPPER) {
                     transferBetweenTanks(tank, itemTank, player);
                     MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.FILL);
                  } else if (this.action == PacketDropperUse.DropperAction.DRAIN_DROPPER) {
                     transferBetweenTanks(itemTank, tank, player);
                     MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.DRAIN);
                  }
               }
            }
         }
      }
   }

   private void handleFluidTank(ServerPlayer player, ItemStack stack, IExtendedFluidTank fluidTank) {
      if (this.action == PacketDropperUse.DropperAction.DUMP_TANK) {
         fluidTank.setEmpty();
         MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.DUMP);
      } else {
         Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(stack).resolve();
         if (capability.isPresent()) {
            IFluidHandlerItem fluidHandlerItem = capability.get();
            if (fluidHandlerItem instanceof IMekanismFluidHandler fluidHandler) {
               IExtendedFluidTank itemFluidTank = fluidHandler.getFluidTank(0, null);
               if (itemFluidTank != null) {
                  if (this.action == PacketDropperUse.DropperAction.FILL_DROPPER) {
                     transferBetweenTanks(fluidTank, itemFluidTank, player);
                     MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.FILL);
                  } else if (this.action == PacketDropperUse.DropperAction.DRAIN_DROPPER) {
                     transferBetweenTanks(itemFluidTank, fluidTank, player);
                     MekanismCriteriaTriggers.USE_GAUGE_DROPPER.trigger(player, UseGaugeDropperTrigger.UseDropperAction.DRAIN);
                  }
               }
            }
         }
      }
   }

   private static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> void transferBetweenTanks(
      IChemicalTank<CHEMICAL, STACK> drainTank, IChemicalTank<CHEMICAL, STACK> fillTank, Player player
   ) {
      if (!drainTank.isEmpty() && fillTank.getNeeded() > 0L) {
         STACK chemicalInDrainTank = drainTank.getStack();
         STACK simulatedRemainder = fillTank.insert(chemicalInDrainTank, Action.SIMULATE, AutomationType.MANUAL);
         long remainder = simulatedRemainder.getAmount();
         long amount = chemicalInDrainTank.getAmount();
         if (remainder < amount) {
            STACK extractedChemical = drainTank.extract(amount - remainder, Action.EXECUTE, AutomationType.MANUAL);
            if (!extractedChemical.isEmpty()) {
               MekanismUtils.logMismatchedStackSize(fillTank.insert(extractedChemical, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0L);
               player.f_36096_.m_150445_();
            }
         }
      }
   }

   private static void transferBetweenTanks(IExtendedFluidTank drainTank, IExtendedFluidTank fillTank, Player player) {
      if (!drainTank.isEmpty() && fillTank.getNeeded() > 0) {
         FluidStack fluidInDrainTank = drainTank.getFluid();
         FluidStack simulatedRemainder = fillTank.insert(fluidInDrainTank, Action.SIMULATE, AutomationType.MANUAL);
         int remainder = simulatedRemainder.getAmount();
         int amount = fluidInDrainTank.getAmount();
         if (remainder < amount) {
            FluidStack extractedFluid = drainTank.extract(amount - remainder, Action.EXECUTE, AutomationType.MANUAL);
            if (!extractedFluid.isEmpty()) {
               MekanismUtils.logMismatchedStackSize(fillTank.insert(extractedFluid, Action.EXECUTE, AutomationType.MANUAL).getAmount(), 0L);
               player.f_36096_.m_150445_();
            }
         }
      }
   }

   @Override
   public void encode(FriendlyByteBuf buffer) {
      buffer.m_130064_(this.pos);
      buffer.m_130068_(this.action);
      buffer.m_130068_(this.tankType);
      buffer.m_130130_(this.tankId);
   }

   public static PacketDropperUse decode(FriendlyByteBuf buffer) {
      return new PacketDropperUse(
         buffer.m_130135_(),
         (PacketDropperUse.DropperAction)buffer.m_130066_(PacketDropperUse.DropperAction.class),
         (PacketDropperUse.TankType)buffer.m_130066_(PacketDropperUse.TankType.class),
         buffer.m_130242_()
      );
   }

   public static enum DropperAction {
      FILL_DROPPER,
      DRAIN_DROPPER,
      DUMP_TANK;
   }

   public static enum TankType {
      GAS_TANK,
      FLUID_TANK,
      INFUSION_TANK,
      PIGMENT_TANK,
      SLURRY_TANK;
   }
}
