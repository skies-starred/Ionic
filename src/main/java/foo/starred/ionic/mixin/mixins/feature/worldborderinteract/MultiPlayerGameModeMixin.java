package foo.starred.ionic.mixin.mixins.feature.worldborderinteract;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import foo.starred.ionic.module.impl.general.WorldBorderInteract;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @ModifyExpressionValue(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean ionic$useItemOn(boolean original) {
        return original || WorldBorderInteract.INSTANCE.getEnabled();
    }
}
