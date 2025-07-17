package moe.nea.firmament.gametest

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext
import org.junit.jupiter.api.Assertions
import org.spongepowered.asm.mixin.MixinEnvironment
import moe.nea.firmament.init.MixinPlugin
import moe.nea.firmament.test.FirmTestBootstrap

class GameTest : FabricClientGameTest {
	override fun runTest(ctx: ClientGameTestContext) {
		FirmTestBootstrap.bootstrapMinecraft()
		MixinEnvironment.getCurrentEnvironment().audit()
		val mp = MixinPlugin.instances.single()
		Assertions.assertEquals(
			mp.expectedFullPathMixins,
			mp.appliedFullPathMixins,
		)
		Assertions.assertNotEquals(
			0,
			mp.mixins.size
		)
	}
}
