package foo.starred.ionic.mixin.mixins.event.packet.singular;

import foo.starred.ionic.event.impl.PacketEvent;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.PacketProcessor$ListenerAndPacket", priority = Integer.MIN_VALUE)
public class PacketProcessorMixin {
    @Shadow
    @Final
    private Packet<?> packet;

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private void ionic$handle$pre(CallbackInfo ci) {
        if (new PacketEvent.Process.Pre(packet).post()) ci.cancel();
    }

    @Inject(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V", shift = At.Shift.AFTER), cancellable = true)
    private void ionic$handle$post(CallbackInfo ci) {
        if (new PacketEvent.Process.Post(packet).post()) ci.cancel();
    }
}
