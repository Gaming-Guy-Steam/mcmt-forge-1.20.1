package mekanism.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismRobitSkins;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismModelCache extends BaseModelCache {
   public static final MekanismModelCache INSTANCE = new MekanismModelCache();
   private final Set<Runnable> callbacks = new HashSet<>();
   public final BaseModelCache.OBJModelData MEKASUIT = this.registerOBJ("models/entity/mekasuit.obj");
   public final BaseModelCache.OBJModelData MEKATOOL_LEFT_HAND = this.registerOBJ("models/entity/mekatool_left.obj");
   public final BaseModelCache.OBJModelData MEKATOOL_RIGHT_HAND = this.registerOBJ("models/entity/mekatool_right.obj");
   private final Set<MekaSuitArmor.ModuleOBJModelData> mekaSuitModules = new HashSet<>();
   public final Set<MekaSuitArmor.ModuleOBJModelData> MEKASUIT_MODULES = Collections.unmodifiableSet(this.mekaSuitModules);
   public final BaseModelCache.OBJModelData TRANSMITTER_CONTENTS = this.register(
      RenderTransmitterBase.MODEL_LOCATION, rl -> new BaseModelCache.OBJModelData(rl) {
         @Override
         protected boolean useDiffuseLighting() {
            return false;
         }
      }
   );
   public final BaseModelCache.JSONModelData LIQUIFIER_BLADE = this.registerJSON("block/liquifier_blade");
   public final BaseModelCache.JSONModelData VIBRATOR_SHAFT = this.registerJSON("block/vibrator_shaft");
   public final BaseModelCache.JSONModelData PIGMENT_MIXER_SHAFT = this.registerJSON("block/pigment_mixer_shaft");
   public final BaseModelCache.JSONModelData[] QIO_DRIVES = new BaseModelCache.JSONModelData[TileEntityQIODriveArray.DriveStatus.STATUSES.length];
   private final Map<ResourceLocation, BaseModelCache.JSONModelData> CUSTOM_ROBIT_MODELS = new HashMap<>();
   private final Map<ResourceLocation, BaseModelCache.JSONModelData> ROBIT_SKINS = new HashMap<>();
   private BakedModel BASE_ROBIT;

   private MekanismModelCache() {
      super("mekanism");

      for (TileEntityQIODriveArray.DriveStatus status : TileEntityQIODriveArray.DriveStatus.STATUSES) {
         if (status != TileEntityQIODriveArray.DriveStatus.NONE) {
            this.QIO_DRIVES[status.ordinal()] = this.registerJSON(status.getModel());
         }
      }
   }

   @Override
   public void onBake(BakingCompleted evt) {
      super.onBake(evt);
      this.callbacks.forEach(Runnable::run);
      this.BASE_ROBIT = getBakedModel(evt, new ModelResourceLocation(Mekanism.rl("robit"), "inventory"));
      this.ROBIT_SKINS.clear();
   }

   public void reloadCallback(Runnable callback) {
      this.callbacks.add(callback);
   }

   @Nullable
   public BakedModel getRobitSkin(@NotNull MekanismRobitSkins.SkinLookup skinLookup) {
      BaseModelCache.JSONModelData data = this.ROBIT_SKINS.computeIfAbsent(skinLookup.location(), skinName -> {
         ResourceLocation customModel = skinLookup.skin().customModel();
         return customModel != null ? this.CUSTOM_ROBIT_MODELS.computeIfAbsent(customModel, this::registerJSONAndBake) : null;
      });
      return data == null ? this.BASE_ROBIT : data.getBakedModel();
   }

   public MekaSuitArmor.ModuleOBJModelData registerMekaSuitModuleModel(ResourceLocation rl) {
      MekaSuitArmor.ModuleOBJModelData data = this.register(rl, MekaSuitArmor.ModuleOBJModelData::new);
      this.mekaSuitModules.add(data);
      return data;
   }
}
