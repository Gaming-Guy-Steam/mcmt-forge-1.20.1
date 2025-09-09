package mekanism.common.integration.crafttweaker.bracket;

public interface IBracketSupport {
   String getBracketName();

   public interface IGasBracketSupport extends IBracketSupport {
      @Override
      default String getBracketName() {
         return "gas";
      }
   }

   public interface IInfuseTypeBracketSupport extends IBracketSupport {
      @Override
      default String getBracketName() {
         return "infuse_type";
      }
   }

   public interface IPigmentBracketSupport extends IBracketSupport {
      @Override
      default String getBracketName() {
         return "pigment";
      }
   }

   public interface ISlurryBracketSupport extends IBracketSupport {
      @Override
      default String getBracketName() {
         return "slurry";
      }
   }
}
