

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockEntityRenderer.class)
public class CustomSkullTexturePatch {
	@Inject(
		method = "getRenderLayer",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onGetRenderLayer(SkullBlock.SkullType type, ProfileComponent profile, CallbackInfoReturnable<RenderLayer> cir) {
		CustomSkyBlockTextures.INSTANCE.modifySkullTexture(type, profile, cir);
	}
}
