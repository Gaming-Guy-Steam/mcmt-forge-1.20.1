package mekanism.common.integration.computer;

import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.integration.computer.annotation.MethodFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@MethodFactory(
   target = ComputerFilterHelper.class
)
public class ComputerFilterHelper$ComputerHandler extends ComputerMethodFactory<ComputerFilterHelper> {
   private final String[] NAMES_item = new String[]{"item"};
   private final String[] NAMES_filterTag = new String[]{"filterTag"};
   private final String[] NAMES_tag = new String[]{"tag"};
   private final String[] NAMES_filterTag_selectedOutput = new String[]{"filterTag", "selectedOutput"};
   private final String[] NAMES_modId = new String[]{"modId"};
   private final Class[] TYPES_dbbe1d5d = new Class[]{ResourceLocation.class};
   private final Class[] TYPES_b987be9f = new Class[]{Item.class};
   private final Class[] TYPES_473e3684 = new Class[]{String.class};
   private final Class[] TYPES_558d4cc3 = new Class[]{ResourceLocation.class, Item.class};

   public ComputerFilterHelper$ComputerHandler() {
      this.register(
         MethodData.builder("createSorterItemFilter", ComputerFilterHelper$ComputerHandler::createSorterItemFilter_1)
            .returnType(SorterItemStackFilter.class)
            .methodDescription("Create a Logistical Sorter Item Filter structure from an Item name")
            .arguments(this.NAMES_item, this.TYPES_b987be9f)
      );
      this.register(
         MethodData.builder("createSorterModIdFilter", ComputerFilterHelper$ComputerHandler::createSorterModIdFilter_1)
            .returnType(SorterModIDFilter.class)
            .methodDescription("Create a Logistical Sorter Mod Id Filter structure from a mod id")
            .arguments(this.NAMES_modId, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createSorterTagFilter", ComputerFilterHelper$ComputerHandler::createSorterTagFilter_1)
            .returnType(SorterTagFilter.class)
            .methodDescription("Create a Logistical Sorter Tag Filter from a tag")
            .arguments(this.NAMES_tag, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createMinerItemFilter", ComputerFilterHelper$ComputerHandler::createMinerItemFilter_1)
            .returnType(MinerItemStackFilter.class)
            .methodDescription("Create a Digital Miner Item Filter from an Item name")
            .arguments(this.NAMES_item, this.TYPES_b987be9f)
      );
      this.register(
         MethodData.builder("createMinerModIdFilter", ComputerFilterHelper$ComputerHandler::createMinerModIdFilter_1)
            .returnType(MinerModIDFilter.class)
            .methodDescription("Create a Digital Miner Mod Id Filter from a mod id")
            .arguments(this.NAMES_modId, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createMinerTagFilter", ComputerFilterHelper$ComputerHandler::createMinerTagFilter_1)
            .returnType(MinerTagFilter.class)
            .methodDescription("Create a Digital Miner Tag Filter from a Tag name")
            .arguments(this.NAMES_tag, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createOredictionificatorItemFilter", ComputerFilterHelper$ComputerHandler::createOredictionificatorItemFilter_1)
            .returnType(OredictionificatorItemFilter.class)
            .methodDescription("Create an Oredictionificator filter from a tag, without specifying an output item")
            .arguments(this.NAMES_filterTag, this.TYPES_dbbe1d5d)
      );
      this.register(
         MethodData.builder("createOredictionificatorItemFilter", ComputerFilterHelper$ComputerHandler::createOredictionificatorItemFilter_2)
            .returnType(OredictionificatorItemFilter.class)
            .methodDescription("Create an Oredictionificator filter from a tag and a selected output. The output is not validated.")
            .arguments(this.NAMES_filterTag_selectedOutput, this.TYPES_558d4cc3)
      );
      this.register(
         MethodData.builder("createQIOItemFilter", ComputerFilterHelper$ComputerHandler::createQIOItemFilter_1)
            .returnType(QIOItemStackFilter.class)
            .methodDescription("Create a QIO Item Filter structure from an Item name")
            .arguments(this.NAMES_item, this.TYPES_b987be9f)
      );
      this.register(
         MethodData.builder("createQIOModIdFilter", ComputerFilterHelper$ComputerHandler::createQIOModIdFilter_1)
            .returnType(QIOModIDFilter.class)
            .methodDescription("Create a QIO Mod Id Filter from a mod id")
            .arguments(this.NAMES_modId, this.TYPES_473e3684)
      );
      this.register(
         MethodData.builder("createQIOTagFilter", ComputerFilterHelper$ComputerHandler::createQIOTagFilter_1)
            .returnType(QIOTagFilter.class)
            .methodDescription("Create a QIO Tag Filter from a Tag name")
            .arguments(this.NAMES_tag, this.TYPES_473e3684)
      );
   }

   public static Object createSorterItemFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createSorterItemFilter(helper.getItem(0)));
   }

   public static Object createSorterModIdFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createSorterModIdFilter(helper.getString(0)));
   }

   public static Object createSorterTagFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createSorterTagFilter(helper.getString(0)));
   }

   public static Object createMinerItemFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createMinerItemFilter(helper.getItem(0)));
   }

   public static Object createMinerModIdFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createMinerModIdFilter(helper.getString(0)));
   }

   public static Object createMinerTagFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createMinerTagFilter(helper.getString(0)));
   }

   public static Object createOredictionificatorItemFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createOredictionificatorItemFilter(helper.getResourceLocation(0)));
   }

   public static Object createOredictionificatorItemFilter_2(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createOredictionificatorItemFilter(helper.getResourceLocation(0), helper.getItem(1)));
   }

   public static Object createQIOItemFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createQIOItemFilter(helper.getItem(0)));
   }

   public static Object createQIOModIdFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createQIOModIdFilter(helper.getString(0)));
   }

   public static Object createQIOTagFilter_1(ComputerFilterHelper subject, BaseComputerHelper helper) throws ComputerException {
      return helper.convert(ComputerFilterHelper.createQIOTagFilter(helper.getString(0)));
   }
}
