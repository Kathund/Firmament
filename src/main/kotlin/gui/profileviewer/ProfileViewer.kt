package moe.nea.firmament.gui.profileviewer

import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.Routes
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil
import net.minecraft.text.Text
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

class ProfileViewer(playerUsername: String) {
	companion object {
		suspend fun onCommand(source: FabricClientCommandSource, name: String) {
			source.sendFeedback(Text.stringifiedTranslatable("firmament.pv.lookingup", name))
			try {
				val uuid = Routes.getUUIDForPlayerName(name)
				if (uuid == null) {
					source.sendError(Text.stringifiedTranslatable("firmament.pv.noplayer", name))
					return
				}
				val name = Routes.getPlayerNameForUUID(uuid) ?: name
				val names = mapOf(uuid to (name))

				val accountData = Routes.getAccountData(uuid)
				if (accountData == null) {
					source.sendError(Text.stringifiedTranslatable("firmament.pv.neverjoined", name))
					return
				}

				val profiles = Routes.getProfiles(uuid)
				if (profiles == null) {
					source.sendFeedback(Text.stringifiedTranslatable("firmament.pv.noprofile", name))
					return
				}

				val profile = profiles.profiles?.find { it.selected }
				if (profile == null) {
					source.sendFeedback(Text.stringifiedTranslatable("firmament.pv.noprofile", name))
					return
				}

				ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen(
					"profileviewer/main",
					ProfileViewerMainGUI(uuid, name, profile, accountData),
					null))
			} catch (e: Exception) {
				Firmament.logger.error("Error loading profile data for $name", e)
				source.sendError(Text.stringifiedTranslatable("firmament.pv.badprofile", name, e.message))
			}
		}
	}
}
