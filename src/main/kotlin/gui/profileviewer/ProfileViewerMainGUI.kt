package moe.nea.firmament.gui.profileviewer

import io.github.notenoughupdates.moulconfig.xml.Bind
import java.util.UUID
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.PlayerData
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil

class ProfileViewerMainGUI(val UUID: UUID, val username: String, val profile: Profile, val accountData: PlayerData?) {
	val member: Member = profile.members[UUID] ?: error("Primary player not in profile")

	@Bind
	fun profileStats() {
		ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen(
			"profileviewer/profile_stats",
			ProfileViewerProfileStatsGUI(profile, member),
			null))
	}

}
