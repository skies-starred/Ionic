package foo.starred.ionic.mixin.mixins.event.packet.bundle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import foo.starred.ionic.event.impl.PacketEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientPacketListener.class, priority = Integer.MIN_VALUE)
public class ClientPacketListenerMixin {
    @WrapOperation(method = "handleBundlePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"))
    private void ionic$handleBundlePacket(Packet<?> packet, PacketListener listener, Operation<Void> original) {
        if (new PacketEvent.Process.Pre(packet).post()) return;

        original.call(packet, listener);
        new PacketEvent.Process.Post(packet).post();
    }
}
