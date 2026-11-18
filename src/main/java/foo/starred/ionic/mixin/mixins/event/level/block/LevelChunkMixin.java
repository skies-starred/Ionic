package foo.starred.ionic.mixin.mixins.event.level.block;

import foo.starred.ionic.event.impl.BlockUpdateEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
    @Final
    @Shadow
    private Level level;

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void ionic$setBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        BlockState old = this.level.getBlockState(pos);
        if (old == state) return;

        new BlockUpdateEvent(old, state, pos).post();
    }
}
