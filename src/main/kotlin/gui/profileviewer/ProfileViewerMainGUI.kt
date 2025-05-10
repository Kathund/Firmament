package moe.nea.firmament.gui.profileviewer

import io.github.notenoughupdates.moulconfig.xml.Bind
import java.util.UUID
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil

class ProfileViewerMainGUI(val UUID: UUID, val username: String, val profile: Profile) {
	@Bind
	fun profileStats() {
		ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen(
			"profileviewer/profile_stats",
			ProfileViewerProfileStatsGUI(UUID, username, profile),
			null))
	}

}
