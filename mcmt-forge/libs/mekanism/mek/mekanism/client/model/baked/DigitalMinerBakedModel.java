package mekanism.client.model.baked;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DigitalMinerBakedModel extends ExtensionBakedModel<Void> {
   @Nullable
   private static TextureAtlasSprite AFD_SAD;
   @Nullable
   private static TextureAtlasSprite AFD_TEXT;
   @Nullable
   private static TextureAtlasSprite MAY_4TH;
   private final Lazy<QuadTransformation> APRIL_FOOLS_TRANSFORM = Lazy.of(
      () -> QuadTransformation.list(
         QuadTransformation.TextureFilteredTransformation.of(
            QuadTransformation.texture(AFD_SAD), s -> s.m_135815_().contains("screen_hello") || s.m_135815_().contains("screen_cmd")
         ),
         QuadTransformation.TextureFilteredTransformation.of(QuadTransformation.texture(AFD_TEXT), s -> s.m_135815_().contains("screen_blank"))
      )
   );
   private final Lazy<QuadTransformation> MAY_4TH_TRANSFORM = Lazy.of(
      () -> QuadTransformation.TextureFilteredTransformation.of(QuadTransformation.texture(MAY_4TH), s -> s.m_135815_().contains("screen_hello"))
   );

   public static void onStitch(Post event) {
      TextureAtlas atlas = event.getAtlas();
      AFD_SAD = atlas.m_118316_(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
      AFD_TEXT = atlas.m_118316_(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
      MAY_4TH = atlas.m_118316_(Mekanism.rl("block/models/digital_miner_screen_may4th"));
   }

   public DigitalMinerBakedModel(BakedModel original) {
      super(original);
   }

   @Nullable
   @Override
   protected ExtensionBakedModel.QuadsKey<Void> createKey(ExtensionBakedModel.QuadsKey<Void> key, ModelData data) {
      if (MekanismConfig.client.holidays.get()) {
         if (HolidayManager.MAY_4.isToday()) {
            return key.transform(this.MAY_4TH_TRANSFORM);
         }

         if (HolidayManager.APRIL_FOOLS.isToday()) {
            return key.transform(this.APRIL_FOOLS_TRANSFORM);
         }
      }

      return null;
   }

   protected DigitalMinerBakedModel wrapModel(BakedModel model) {
      return new DigitalMinerBakedModel(model);
   }
}
