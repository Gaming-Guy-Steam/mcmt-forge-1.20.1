package mekanism.common.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import mekanism.api.Coord4D;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.base.MekanismPermissions;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.lib.radiation.RadiationManager;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RadiationCommand {
   static ArgumentBuilder<CommandSourceStack, ?> register() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_(
                              "radiation"
                           )
                           .requires(MekanismPermissions.COMMAND_RADIATION))
                        .then(subCommandAdd()))
                     .then(subCommandAddEntity()))
                  .then(subCommandGet()))
               .then(subCommandHeal()))
            .then(subCommandReduce()))
         .then(
            ((LiteralArgumentBuilder)Commands.m_82127_("removeAll").requires(MekanismPermissions.COMMAND_RADIATION_REMOVE_ALL))
               .executes(
                  ctx -> {
                     RadiationManager.get().clearSources();
                     ((CommandSourceStack)ctx.getSource())
                        .m_288197_(() -> MekanismLang.COMMAND_RADIATION_REMOVE_ALL.translateColored(EnumColor.GRAY, new Object[0]), true);
                     return 0;
                  }
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> subCommandAdd() {
      return ((LiteralArgumentBuilder)Commands.m_82127_("add").requires(MekanismPermissions.COMMAND_RADIATION_ADD))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10000.0)).executes(ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  return addRadiation(source, source.m_81371_(), source.m_81372_(), DoubleArgumentType.getDouble(ctx, "magnitude"));
               }))
               .then(
                  ((RequiredArgumentBuilder)Commands.m_82129_("location", Vec3Argument.m_120841_()).executes(ctx -> {
                        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                        return addRadiation(source, Vec3Argument.m_120849_(ctx, "location"), source.m_81372_(), DoubleArgumentType.getDouble(ctx, "magnitude"));
                     }))
                     .then(
                        Commands.m_82129_("dimension", DimensionArgument.m_88805_())
                           .executes(
                              ctx -> {
                                 CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                                 return addRadiation(
                                    source,
                                    Vec3Argument.m_120849_(ctx, "location"),
                                    DimensionArgument.m_88808_(ctx, "dimension"),
                                    DoubleArgumentType.getDouble(ctx, "magnitude")
                                 );
                              }
                           )
                     )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> subCommandAddEntity() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("addEntity").requires(MekanismPermissions.COMMAND_RADIATION_ADD_ENTITY))
            .then(
               Commands.m_82129_("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10000.0))
                  .executes(
                     ctx -> {
                        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                        double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                        source.m_81375_()
                           .getCapability(Capabilities.RADIATION_ENTITY)
                           .ifPresent(
                              c -> {
                                 c.radiate(magnitude);
                                 source.m_288197_(
                                    () -> MekanismLang.COMMAND_RADIATION_ADD_ENTITY
                                       .translateColored(
                                          EnumColor.GRAY,
                                          new Object[]{
                                             RadiationManager.RadiationScale.getSeverityColor(magnitude),
                                             UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SVH, 3)
                                          }
                                       ),
                                    true
                                 );
                              }
                           );
                        return 0;
                     }
                  )
            ))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("targets", EntityArgument.m_91460_()).requires(MekanismPermissions.COMMAND_RADIATION_ADD_ENTITY_OTHERS))
               .then(
                  Commands.m_82129_("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10000.0))
                     .executes(
                        ctx -> {
                           CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                           double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                           int addedTo = 0;

                           for (Entity entity : EntityArgument.m_91461_(ctx, "targets")) {
                              if (entity instanceof LivingEntity) {
                                 entity.getCapability(Capabilities.RADIATION_ENTITY)
                                    .ifPresent(
                                       c -> {
                                          c.radiate(magnitude);
                                          source.m_288197_(
                                             () -> MekanismLang.COMMAND_RADIATION_ADD_ENTITY_TARGET
                                                .translateColored(
                                                   EnumColor.GRAY,
                                                   new Object[]{
                                                      RadiationManager.RadiationScale.getSeverityColor(magnitude),
                                                      UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SVH, 3),
                                                      EnumColor.INDIGO,
                                                      entity.m_5446_()
                                                   }
                                                ),
                                             true
                                          );
                                       }
                                    );
                                 addedTo++;
                              }
                           }

                           return addedTo;
                        }
                     )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> subCommandGet() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("get").requires(MekanismPermissions.COMMAND_RADIATION_GET)).executes(ctx -> {
            CommandSourceStack source = (CommandSourceStack)ctx.getSource();
            return getRadiationLevel(source, source.m_81371_(), source.m_81372_());
         }))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("location", Vec3Argument.m_120841_()).executes(ctx -> {
                  CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                  return getRadiationLevel(source, Vec3Argument.m_120849_(ctx, "location"), source.m_81372_());
               }))
               .then(
                  Commands.m_82129_("dimension", DimensionArgument.m_88805_())
                     .executes(
                        ctx -> getRadiationLevel(
                           (CommandSourceStack)ctx.getSource(), Vec3Argument.m_120849_(ctx, "location"), DimensionArgument.m_88808_(ctx, "dimension")
                        )
                     )
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> subCommandHeal() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("heal").requires(MekanismPermissions.COMMAND_RADIATION_HEAL))
            .executes(ctx -> {
               CommandSourceStack source = (CommandSourceStack)ctx.getSource();
               source.m_81375_().getCapability(Capabilities.RADIATION_ENTITY).ifPresent(c -> {
                  c.set(1.0E-7);
                  source.m_288197_(() -> MekanismLang.COMMAND_RADIATION_CLEAR.translateColored(EnumColor.GRAY, new Object[0]), true);
               });
               return 0;
            }))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("targets", EntityArgument.m_91460_()).requires(MekanismPermissions.COMMAND_RADIATION_HEAL_OTHERS))
               .executes(
                  ctx -> {
                     CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                     int healed = 0;

                     for (Entity entity : EntityArgument.m_91461_(ctx, "targets")) {
                        if (entity instanceof LivingEntity) {
                           entity.getCapability(Capabilities.RADIATION_ENTITY)
                              .ifPresent(
                                 c -> {
                                    c.set(1.0E-7);
                                    source.m_288197_(
                                       () -> MekanismLang.COMMAND_RADIATION_CLEAR_ENTITY
                                          .translateColored(EnumColor.GRAY, new Object[]{EnumColor.INDIGO, entity.m_5446_()}),
                                       true
                                    );
                                 }
                              );
                           healed++;
                        }
                     }

                     return healed;
                  }
               )
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> subCommandReduce() {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("reduce").requires(MekanismPermissions.COMMAND_RADIATION_REDUCE))
            .then(
               Commands.m_82129_("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10000.0))
                  .executes(
                     ctx -> {
                        CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                        double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                        source.m_81375_()
                           .getCapability(Capabilities.RADIATION_ENTITY)
                           .ifPresent(
                              c -> {
                                 double newValue = Math.max(1.0E-7, c.getRadiation() - magnitude);
                                 double reduced = c.getRadiation() - newValue;
                                 c.set(newValue);
                                 source.m_288197_(
                                    () -> MekanismLang.COMMAND_RADIATION_REDUCE
                                       .translateColored(
                                          EnumColor.GRAY,
                                          new Object[]{
                                             RadiationManager.RadiationScale.getSeverityColor(reduced),
                                             UnitDisplayUtils.getDisplayShort(reduced, UnitDisplayUtils.RadiationUnit.SVH, 3)
                                          }
                                       ),
                                    true
                                 );
                              }
                           );
                        return 0;
                     }
                  )
            ))
         .then(
            ((RequiredArgumentBuilder)Commands.m_82129_("targets", EntityArgument.m_91460_()).requires(MekanismPermissions.COMMAND_RADIATION_REDUCE_OTHERS))
               .then(
                  Commands.m_82129_("magnitude", DoubleArgumentType.doubleArg(Double.MIN_VALUE, 10000.0))
                     .executes(
                        ctx -> {
                           CommandSourceStack source = (CommandSourceStack)ctx.getSource();
                           double magnitude = DoubleArgumentType.getDouble(ctx, "magnitude");
                           int reducedFrom = 0;

                           for (Entity entity : EntityArgument.m_91461_(ctx, "targets")) {
                              if (entity instanceof LivingEntity) {
                                 entity.getCapability(Capabilities.RADIATION_ENTITY)
                                    .ifPresent(
                                       c -> {
                                          double newValue = Math.max(1.0E-7, c.getRadiation() - magnitude);
                                          double reduced = c.getRadiation() - newValue;
                                          c.set(newValue);
                                          source.m_288197_(
                                             () -> MekanismLang.COMMAND_RADIATION_REDUCE_TARGET
                                                .translateColored(
                                                   EnumColor.GRAY,
                                                   new Object[]{
                                                      EnumColor.INDIGO,
                                                      entity.m_5446_(),
                                                      RadiationManager.RadiationScale.getSeverityColor(reduced),
                                                      UnitDisplayUtils.getDisplayShort(reduced, UnitDisplayUtils.RadiationUnit.SVH, 3)
                                                   }
                                                ),
                                             true
                                          );
                                       }
                                    );
                                 reducedFrom++;
                              }
                           }

                           return reducedFrom;
                        }
                     )
               )
         );
   }

   private static int addRadiation(CommandSourceStack source, Coordinates location, Level world, double magnitude) {
      return addRadiation(source, location.m_6955_(source), world, magnitude);
   }

   private static int addRadiation(CommandSourceStack source, Vec3 pos, Level world, double magnitude) {
      Coord4D location = new Coord4D(pos.f_82479_, pos.f_82480_, pos.f_82481_, world.m_46472_());
      IRadiationManager.INSTANCE.radiate(location, magnitude);
      source.m_288197_(
         () -> MekanismLang.COMMAND_RADIATION_ADD
            .translateColored(
               EnumColor.GRAY,
               new Object[]{
                  RadiationManager.RadiationScale.getSeverityColor(magnitude),
                  UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SVH, 3),
                  EnumColor.INDIGO,
                  getPosition(location.getPos()),
                  EnumColor.INDIGO,
                  location.dimension.m_135782_()
               }
            ),
         true
      );
      return 0;
   }

   private static int getRadiationLevel(CommandSourceStack source, Coordinates location, Level world) {
      return getRadiationLevel(source, location.m_6955_(source), world);
   }

   private static int getRadiationLevel(CommandSourceStack source, Vec3 pos, Level world) {
      Coord4D location = new Coord4D(pos.f_82479_, pos.f_82480_, pos.f_82481_, world.m_46472_());
      double magnitude = IRadiationManager.INSTANCE.getRadiationLevel(location);
      source.m_288197_(
         () -> MekanismLang.COMMAND_RADIATION_GET
            .translateColored(
               EnumColor.GRAY,
               new Object[]{
                  EnumColor.INDIGO,
                  getPosition(location.getPos()),
                  EnumColor.INDIGO,
                  location.dimension.m_135782_(),
                  RadiationManager.RadiationScale.getSeverityColor(magnitude),
                  UnitDisplayUtils.getDisplayShort(magnitude, UnitDisplayUtils.RadiationUnit.SVH, 3)
               }
            ),
         true
      );
      return 0;
   }

   private static Component getPosition(BlockPos pos) {
      return MekanismLang.GENERIC_BLOCK_POS.translate(new Object[]{pos.m_123341_(), pos.m_123342_(), pos.m_123343_()});
   }
}
