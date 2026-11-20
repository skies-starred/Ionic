package foo.starred.ionic.mixin.mixins.event.render.level;

import com.mojang.blaze3d.vertex.PoseStack;
import foo.starred.ionic.event.impl.WorldRenderEvent;
import foo.starred.kontext.api.entity.duck.EntityRenderStateDuck;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void ionic$submit$pre(EntityRenderState state, CameraRenderState camera, double x, double y, double z, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
        if (new WorldRenderEvent.Entity(state, EntityRenderStateDuck.Companion.get(state)).post()) ci.cancel();
    }
}
