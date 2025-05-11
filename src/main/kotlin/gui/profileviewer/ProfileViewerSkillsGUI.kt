package moe.nea.firmament.gui.profileviewer

import moe.nea.firmament.util.titleCase
import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import java.util.UUID
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.apis.Skill
import moe.nea.firmament.repo.RepoManager

class ProfileViewerSkillsGUI(val profile: Profile, val member: Member) {
	data class GUISkill(
		@get:Bind("skillName") val name: String,
		@get:Bind("skillExp") val exp: Double,
		@get:Bind("skillLevel") val level: Int,
		@get:Bind("skillMaxLevel") val maxLevel: Int,
	) {
		@Bind("skillLevelString")
		fun getSkillLevelString(): String = level.toString()
	}

	@Bind("Skills")
	fun getSkills(): ObservableList<GUISkill> {
		val parsedSkills = ObservableList<GUISkill>(mutableListOf())
		for ((i, skill) in Skill.entries.withIndex()) parsedSkills.add(parseSkill(skill))
		return parsedSkills
	}

	fun parseSkill(skill: Skill): GUISkill {
		val leveling = RepoManager.neuRepo.constants.leveling
		val exp = skill.accessor(member)
		val maxLevel = skill.getMaximumLevel(leveling)
		val level = skill.getLadder(leveling)
			.runningFold(0.0) { a, b -> a + b }
			.filter { it <= exp }
			.size
			.coerceAtMost(maxLevel)

		return GUISkill(
			name = titleCase(skill.name),
			exp = exp,
			level = level,
			maxLevel = maxLevel
		)
	}

}
