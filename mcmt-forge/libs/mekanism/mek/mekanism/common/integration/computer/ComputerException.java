package mekanism.common.integration.computer;

import java.util.Locale;

public class ComputerException extends Exception {
   public ComputerException(String message) {
      super(message);
   }

   public ComputerException(String messageFormat, Object... args) {
      this(String.format(Locale.ROOT, messageFormat, args));
   }

   public ComputerException(Exception e) {
      super(e);
   }

   @Override
   public synchronized Throwable fillInStackTrace() {
      return this;
   }
}
