package foo.starred.ionic.mixin.mixins.api.player.rotation;

import foo.starred.ionic.api.player.rotation.RotationPlayerAPI;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "update", at = @At("HEAD"))
    //~ if >= 26.3 'DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci' -> 'DeltaTracker deltaTracker, CallbackInfo ci'
    private void ionic$update(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        RotationPlayerAPI.INSTANCE.frame();
    }
}
