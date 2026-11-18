package foo.starred.ionic.mixin.mixins.event.entity.player.attack;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foo.starred.ionic.event.impl.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique
    private boolean ionic$cancel;

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void ionic$startAttack$head(CallbackInfoReturnable<Boolean> cir) {
        ionic$cancel = false;
    }

    @WrapWithCondition(method = "startAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"))
    private boolean ionic$startAttack$attack(MultiPlayerGameMode instance, Player player, Entity entity) {
        boolean any = PlayerEvent.Attack.Any.INSTANCE.post();
        boolean specific = new PlayerEvent.Attack.Entity(entity).post();

        return !(ionic$cancel = any || specific);
    }

    @WrapOperation(method = "startAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean ionic$startAttack$block(MultiPlayerGameMode instance, BlockPos pos, Direction direction, Operation<Boolean> original) {
        boolean any = PlayerEvent.Attack.Any.INSTANCE.post();
        boolean specific = new PlayerEvent.Attack.Block(pos).post();

        ionic$cancel = any || specific;
        if (ionic$cancel) return false;

        return original.call(instance, pos, direction);
    }

    @WrapWithCondition(method = "startAttack()Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V", ordinal = 1))
    private boolean ionic$startAttack$swing(LocalPlayer instance, InteractionHand hand) {
        return !ionic$cancel;
    }
}
