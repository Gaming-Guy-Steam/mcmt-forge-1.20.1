package mekanism.api;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import net.minecraft.core.Direction;

@NothingNullByDefault
public enum RelativeSide implements IHasTranslationKey {
   FRONT(APILang.FRONT),
   LEFT(APILang.LEFT),
   RIGHT(APILang.RIGHT),
   BACK(APILang.BACK),
   TOP(APILang.TOP),
   BOTTOM(APILang.BOTTOM);

   private static final RelativeSide[] SIDES = values();
   private final ILangEntry langEntry;

   public static RelativeSide byIndex(int index) {
      return MathUtils.getByIndexMod(SIDES, index);
   }

   private RelativeSide(ILangEntry langEntry) {
      this.langEntry = langEntry;
   }

   @Override
   public String getTranslationKey() {
      return this.langEntry.getTranslationKey();
   }

   public Direction getDirection(Direction facing) {
      return switch (this) {
         case FRONT -> facing;
         case BACK -> facing.m_122424_();
         case LEFT -> facing != Direction.DOWN && facing != Direction.UP ? facing.m_122427_() : Direction.EAST;
         case RIGHT -> facing != Direction.DOWN && facing != Direction.UP ? facing.m_122428_() : Direction.WEST;
         case TOP -> {
            switch (facing) {
               case DOWN:
                  yield Direction.NORTH;
               case UP:
                  yield Direction.SOUTH;
               default:
                  yield Direction.UP;
            }
         }
         case BOTTOM -> {
            switch (facing) {
               case DOWN:
                  yield Direction.SOUTH;
               case UP:
                  yield Direction.NORTH;
               default:
                  yield Direction.DOWN;
            }
         }
      };
   }

   public static RelativeSide fromDirections(Direction facing, Direction side) {
      if (side == facing) {
         return FRONT;
      } else if (side == facing.m_122424_()) {
         return BACK;
      } else if (facing == Direction.DOWN || facing == Direction.UP) {
         return switch (side) {
            case NORTH -> facing == Direction.DOWN ? TOP : BOTTOM;
            case SOUTH -> facing == Direction.DOWN ? BOTTOM : TOP;
            case WEST -> RIGHT;
            case EAST -> LEFT;
            default -> throw new IllegalStateException("Case should have been caught earlier.");
         };
      } else if (side == Direction.DOWN) {
         return BOTTOM;
      } else if (side == Direction.UP) {
         return TOP;
      } else if (side == facing.m_122428_()) {
         return RIGHT;
      } else {
         return side == facing.m_122427_() ? LEFT : FRONT;
      }
   }
}
