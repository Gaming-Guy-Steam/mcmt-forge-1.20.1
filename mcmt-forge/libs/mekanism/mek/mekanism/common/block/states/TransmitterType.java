package mekanism.common.block.states;

import mekanism.api.math.MathUtils;

public enum TransmitterType {
   UNIVERSAL_CABLE(TransmitterType.Size.SMALL),
   MECHANICAL_PIPE(TransmitterType.Size.LARGE),
   PRESSURIZED_TUBE(TransmitterType.Size.SMALL),
   LOGISTICAL_TRANSPORTER(TransmitterType.Size.LARGE),
   RESTRICTIVE_TRANSPORTER(TransmitterType.Size.LARGE),
   DIVERSION_TRANSPORTER(TransmitterType.Size.LARGE),
   THERMODYNAMIC_CONDUCTOR(TransmitterType.Size.SMALL);

   private static final TransmitterType[] TYPES = values();
   private final TransmitterType.Size size;

   private TransmitterType(TransmitterType.Size size) {
      this.size = size;
   }

   public TransmitterType.Size getSize() {
      return this.size;
   }

   public static TransmitterType byIndexStatic(int index) {
      return MathUtils.getByIndexMod(TYPES, index);
   }

   public static enum Size {
      SMALL(6),
      LARGE(8);

      public final int centerSize;

      private Size(int size) {
         this.centerSize = size;
      }
   }
}
