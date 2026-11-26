package foo.starred.ionic.mixin.mixins.event.packet.singular;

import foo.starred.ionic.event.impl.PacketEvent;
import foo.starred.ionic.event.impl.TickEvent;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Connection.class, priority = Integer.MIN_VALUE)
public class ConnectionMixin {
    @Inject(method = "channelRead0*", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"), cancellable = true)
    private void ionic$channelRead(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundPingPacket packet1 && packet1.getId() != 0) {
            TickEvent.Server.INSTANCE.post();
        }

        if (new PacketEvent.Receive(packet).post()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("HEAD"), cancellable = true)
    private void ionic$sendPacket(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        if (new PacketEvent.Send(packet).post()) ci.cancel();
    }
}
