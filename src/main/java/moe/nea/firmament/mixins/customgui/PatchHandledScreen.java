
package moe.nea.firmament.mixins.customgui;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent;
import moe.nea.firmament.util.customgui.CoordRememberingSlot;
import moe.nea.firmament.util.customgui.CustomGui;
import moe.nea.firmament.util.customgui.HasCustomGui;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public class PatchHandledScreen<T extends ScreenHandler> extends Screen implements HasCustomGui {
	@Shadow
	@Final
	protected T handler;
	@Shadow
	protected int x;
	@Shadow
	protected int y;
	@Shadow
	protected int backgroundHeight;
	@Shadow
	protected int backgroundWidth;
	@Unique
	public CustomGui override;
	@Unique
	public boolean hasRememberedSlots = false;
	@Unique
	private int originalBackgroundWidth;
	@Unique
	private int originalBackgroundHeight;

	protected PatchHandledScreen(Text title) {
		super(title);
	}

	@Nullable
	@Override
	public CustomGui getCustomGui_Firmament() {
		return override;
	}

	@Override
	public void setCustomGui_Firmament(@Nullable CustomGui gui) {
		if (this.override != null) {
			backgroundHeight = originalBackgroundHeight;
			backgroundWidth = originalBackgroundWidth;
		}
		if (gui != null) {
			originalBackgroundHeight = backgroundHeight;
			originalBackgroundWidth = backgroundWidth;
		}
		this.override = gui;
	}

	public boolean mouseScrolled_firmament(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return override != null && override.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	public boolean keyReleased_firmament(int keyCode, int scanCode, int modifiers) {
		if (HandledScreenKeyReleasedEvent.Companion.publish(new HandledScreenKeyReleasedEvent((HandledScreen<?>) (Object) this, keyCode, scanCode, modifiers)).getCancelled())
			return true;
		return override != null && override.keyReleased(keyCode, scanCode, modifiers);
	}

	public boolean charTyped_firmament(char chr, int modifiers) {
		return override != null && override.charTyped(chr, modifiers);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void onInit(CallbackInfo ci) {
		if (override != null) {
			override.onInit();
		}
	}

	@Inject(method = "drawForeground", at = @At("HEAD"), cancellable = true)
	private void onDrawForeground(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
		if (override != null && !override.shouldDrawForeground())
			ci.cancel();
	}


	@WrapOperation(
		method = "drawSlots",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
	private void beforeSlotRender(HandledScreen instance, DrawContext context, Slot slot, Operation<Void> original) {
		if (override != null) {
			override.beforeSlotRender(context, slot);
		}
		original.call(instance, context, slot);
		if (override != null) {
			override.afterSlotRender(context, slot);
		}
	}

	@Inject(method = "isClickOutsideBounds", at = @At("HEAD"), cancellable = true)
	public void onIsClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isClickOutsideBounds(mouseX, mouseY));
		}
	}

	@Inject(method = "isPointWithinBounds", at = @At("HEAD"), cancellable = true)
	public void onIsPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointWithinBounds(x + this.x, y + this.y, width, height, pointX, pointY));
		}
	}

	@Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
	public void onIsPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			cir.setReturnValue(override.isPointOverSlot(slot, this.x, this.y, pointX, pointY));
		}
	}

	@Inject(method = "renderBackground", at = @At("HEAD"))
	public void moveSlots(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (override != null) {
			for (Slot slot : handler.slots) {
				if (!hasRememberedSlots) {
					((CoordRememberingSlot) slot).rememberCoords_firmament();
				}
				override.moveSlot(slot);
			}
			hasRememberedSlots = true;
		} else {
			if (hasRememberedSlots) {
				for (Slot slot : handler.slots) {
					((CoordRememberingSlot) slot).restoreCoords_firmament();
				}
				hasRememberedSlots = false;
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "close", cancellable = true)
	private void onVoluntaryExit(CallbackInfo ci) {
		if (override != null) {
			if (!override.onVoluntaryExit())
				ci.cancel();
		}
	}

	@WrapWithCondition(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V"))
	public boolean preventDrawingBackground(HandledScreen instance, DrawContext drawContext, float delta, int mouseX, int mouseY) {
		if (override != null) {
			override.render(drawContext, delta, mouseX, mouseY);
		}
		return override == null;
	}

	@WrapOperation(
		method = "mouseClicked",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
	public boolean overrideMouseClicks(HandledScreen instance, double mouseX, double mouseY, int button,
	                                   Operation<Boolean> original) {
		if (override != null) {
			if (override.mouseClick(mouseX, mouseY, button))
				return true;
		}
		return original.call(instance, mouseX, mouseY, button);
	}

	@Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
	public void overrideMouseDrags(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseDragged(mouseX, mouseY, button, deltaX, deltaY))
				cir.setReturnValue(true);
		}
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void overrideKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.keyPressed(keyCode, scanCode, modifiers)) {
				cir.setReturnValue(true);
			}
		}
	}


	@Inject(
		method = "mouseReleased",
		at = @At("HEAD"), cancellable = true)
	public void overrideMouseReleases(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (override != null) {
			if (override.mouseReleased(mouseX, mouseY, button))
				cir.setReturnValue(true);
		}
	}
}
