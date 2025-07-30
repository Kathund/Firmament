

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.events.HandledScreenClickEvent;
import moe.nea.firmament.events.HandledScreenForegroundEvent;
import moe.nea.firmament.events.HandledScreenKeyPressedEvent;
import moe.nea.firmament.events.IsSlotProtectedEvent;
import moe.nea.firmament.events.SlotRenderEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HandledScreen.class, priority = 990)
public abstract class MixinHandledScreen<T extends ScreenHandler> {

	@Shadow
	@Final
	protected T handler;

	@Shadow
	public abstract T getScreenHandler();

	@Shadow
	protected int y;
	@Shadow
	protected int x;
	@Unique
	PlayerInventory playerInventory;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void savePlayerInventory(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
		this.playerInventory = inventory;
	}

	@Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z", shift = At.Shift.BEFORE), cancellable = true)
	public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent((HandledScreen<?>) (Object) this, keyCode, scanCode, modifiers)).getCancelled()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (HandledScreenClickEvent.Companion.publish(new HandledScreenClickEvent((HandledScreen<?>) (Object) this, mouseX, mouseY, button)).getCancelled()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "renderMain", at = @At("HEAD"))
	public void onAfterRenderForeground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		HandledScreenForegroundEvent.Companion.publish(new HandledScreenForegroundEvent((HandledScreen<?>) (Object) this, context, mouseX, mouseY, delta));
	}

	@Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
	public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
		if (slotId == -999 && getScreenHandler() != null && actionType == SlotActionType.PICKUP) { // -999 is code for "clicked outside the main window"
			ItemStack cursorStack = getScreenHandler().getCursorStack();
			if (cursorStack != null && IsSlotProtectedEvent.shouldBlockInteraction(slot, SlotActionType.THROW, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE, cursorStack)) {
				ci.cancel();
				return;
			}
		}
		if (IsSlotProtectedEvent.shouldBlockInteraction(slot, actionType, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE)) {
			ci.cancel();
			return;
		}
		if (actionType == SlotActionType.SWAP && 0 <= button && button < 9) {
			if (IsSlotProtectedEvent.shouldBlockInteraction(new Slot(playerInventory, button, 0, 0), actionType, IsSlotProtectedEvent.MoveOrigin.INVENTORY_MOVE)) {
				ci.cancel();
			}
		}
	}


	@WrapOperation(method = "drawSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
	public void onDrawSlots(HandledScreen instance, DrawContext context, Slot slot, Operation<Void> original) {
		var before = new SlotRenderEvents.Before(context, slot);
		SlotRenderEvents.Before.Companion.publish(before);
		original.call(instance, context, slot);
		var after = new SlotRenderEvents.After(context, slot);
		SlotRenderEvents.After.Companion.publish(after);
	}
}
