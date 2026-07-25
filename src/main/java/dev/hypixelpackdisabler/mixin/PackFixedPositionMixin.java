package dev.hypixelpackdisabler.mixin;

import dev.hypixelpackdisabler.ServerPackControl;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pack.class)
public abstract class PackFixedPositionMixin {

	@Inject(method = "isFixedPosition", at = @At("HEAD"), cancellable = true)
	private void hypixelpackdisabler$unpinServerPack(CallbackInfoReturnable<Boolean> cir) {
		Pack self = (Pack) (Object) this;
		if (self.getPackSource() == PackSource.SERVER && ServerPackControl.INSTANCE.isReordering()) {
			cir.setReturnValue(false);
		}
	}
}
