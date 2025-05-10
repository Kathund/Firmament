@file:UseSerializers(DashlessUUIDSerializer::class, InstantAsLongSerializer::class)

package moe.nea.firmament.apis

import io.github.moulberry.repo.constants.Leveling
import io.github.moulberry.repo.data.Rarity
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.assertNotNullOr
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.json.InstantAsLongSerializer
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import java.util.*

@Serializable
data class CollectionSkillData(
    val items: Map<CollectionType, CollectionInfo>
)

@Serializable
data class CollectionResponse(
    val success: Boolean,
    val collections: Map<String, CollectionSkillData>
)

@Serializable
data class CollectionInfo(
    val name: String,
    val maxTiers: Int,
    val tiers: List<CollectionTier>
)

@Serializable
data class CollectionTier(
    val tier: Int,
    val amountRequired: Long,
    val unlocks: List<String>,
)


@Serializable
data class Profiles(
    val success: Boolean,
    val profiles: List<Profile>?
)

@Serializable
data class Profile(
    @SerialName("profile_id")
    val profileId: UUID,
    @SerialName("cute_name")
    val cuteName: String,
    val selected: Boolean = false,
    val members: Map<UUID, Member>,
)

enum class Skill(val accessor: (Member) -> Double, val color: DyeColor, val icon: SkyblockId) {
	FARMING({ it.playerData.skills.farming }, DyeColor.YELLOW, SkyblockId("ROOKIE_HOE")),
	FORAGING({ it.playerData.skills.foraging }, DyeColor.BROWN, SkyblockId("TREECAPITATOR_AXE")),
	MINING({ it.playerData.skills.mining }, DyeColor.LIGHT_GRAY, SkyblockId("DIAMOND_PICKAXE")),
	ALCHEMY({ it.playerData.skills.alchemy }, DyeColor.PURPLE, SkyblockId("BREWING_STAND")),
	TAMING({ it.playerData.skills.taming }, DyeColor.GREEN, SkyblockId("SUPER_EGG")),
	FISHING({ it.playerData.skills.fishing }, DyeColor.BLUE, SkyblockId("FARMER_ROD")),
	RUNECRAFTING({ it.playerData.skills.runecrafting }, DyeColor.PINK, SkyblockId("MUSIC_RUNE;1")),
	CARPENTRY({ it.playerData.skills.carpentry }, DyeColor.ORANGE, SkyblockId("WORKBENCH")),
	COMBAT({ it.playerData.skills.combat }, DyeColor.RED, SkyblockId("UNDEAD_SWORD")),
	SOCIAL({ it.playerData.skills.social }, DyeColor.WHITE, SkyblockId("EGG_HUNT")),
	ENCHANTING({ it.playerData.skills.enchanting }, DyeColor.MAGENTA, SkyblockId("ENCHANTMENT_TABLE")),
	;

    fun getMaximumLevel(leveling: Leveling) = assertNotNullOr(leveling.maximumLevels[name.lowercase()]) { 50 }

    fun getLadder(leveling: Leveling): List<Int> {
        if (this == SOCIAL) return leveling.socialExperienceRequiredPerLevel
        if (this == RUNECRAFTING) return leveling.runecraftingExperienceRequiredPerLevel
        return leveling.skillExperienceRequiredPerLevel
    }
}

enum class CollectionCategory(val skill: Skill?, val color: DyeColor, val icon: SkyblockId) {
    FARMING(Skill.FARMING, DyeColor.YELLOW, SkyblockId("ROOKIE_HOE")),
    FORAGING(Skill.FORAGING, DyeColor.BROWN, SkyblockId("TREECAPITATOR_AXE")),
    MINING(Skill.MINING, DyeColor.LIGHT_GRAY, SkyblockId("DIAMOND_PICKAXE")),
    FISHING(Skill.FISHING, DyeColor.BLUE, SkyblockId("FARMER_ROD")),
    COMBAT(Skill.COMBAT, DyeColor.RED, SkyblockId("UNDEAD_SWORD")),
    RIFT(null, DyeColor.PURPLE, SkyblockId("SKYBLOCK_MOTE")),
}

@Serializable
@JvmInline
value class CollectionType(val string: String) {
    val skyblockId get() = SkyblockId(string.replace(":", "-").replace("MUSHROOM_COLLECTION", "HUGE_MUSHROOM_2"))
}

@Serializable
data class MemberPlayerDataSkills (
	@SerialName("SKILL_FISHING") val fishing: Double = 0.0,
	@SerialName("SKILL_ALCHEMY") val alchemy: Double = 0.0,
	@SerialName("SKILL_RUNECRAFTING") val runecrafting: Double = 0.0,
	@SerialName("SKILL_MINING") val mining: Double = 0.0,
	@SerialName("SKILL_FARMING") val farming: Double = 0.0,
	@SerialName("SKILL_ENCHANTING") val enchanting: Double = 0.0,
	@SerialName("SKILL_TAMING") val taming: Double = 0.0,
	@SerialName("SKILL_FORAGING") val foraging: Double = 0.0,
	@SerialName("SKILL_SOCIAL") val social: Double = 0.0,
	@SerialName("SKILL_CARPENTRY") val carpentry: Double = 0.0,
	@SerialName("SKILL_COMBAT") val combat: Double = 0.0,
)

@Serializable
data class MemberPlayerData (
	@SerialName("experience") val skills: MemberPlayerDataSkills = MemberPlayerDataSkills(),
	@SerialName("death_count") val deaths: Double = 0.0
)

@Serializable
data class MemberCurrenciesEssenceItem (
	@SerialName("current") val current: Double = 0.0
)

@Serializable
data class MemberCurrenciesEssence (
	@SerialName("DRAGON") val dragon: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("DIAMOND") val diamond: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("SPIDER") val spider: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("GOLD") val gold: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("UNDEAD") val undead: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("WITHER") val wither: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem(),
	@SerialName("ICE") val ice: MemberCurrenciesEssenceItem = MemberCurrenciesEssenceItem()
)

@Serializable
data class MemberCurrencies (
	@SerialName("coin_purse") val coinPurse: Double = 0.0,
	@SerialName("motes_purse") val motesPurse: Double = 0.0,
	@SerialName("essence") val essence: MemberCurrenciesEssence = MemberCurrenciesEssence()
)

@Serializable
data class MemberProfile (
	@SerialName("first_join") val firstJoin: Double = 0.0,
	@SerialName("bank_account") val bankAccount: Double = 0.0,
	@SerialName("cookie_buff_active") val cookie: Boolean = false
)

@Serializable
data class MemberFairySouls (
	@SerialName("fairy_exchanges") val exchanges: Double = 0.0,
	@SerialName("total_collected") val collected: Double = 0.0,
	@SerialName("unspent_souls") val unspent: Double = 0.0
)

@Serializable
data class MemberPlayerStatsPetsMilestone (
	@SerialName("ores_mined") val oresMined: Double = 0.0,
	@SerialName("sea_creatures_killed") val seaCreaturesKilled: Double = 0.0
)

@Serializable
data class MemberPlayerStatsPets (
	@SerialName("milestone") val milestone: MemberPlayerStatsPetsMilestone = MemberPlayerStatsPetsMilestone()
)

@Serializable
data class MemberPlayerStatsAuctions (
	@SerialName("bids") val bids: Double = 0.0,
	@SerialName("highest_bid") val highestBid: Double = 0.0,
	@SerialName("created") val auctionsCreated: Double = 0.0,
	@SerialName("won") val auctionsWon: Double = 0.0,
	@SerialName("gold_spent") val goldSpent: Double = 0.0,
	@SerialName("gold_earned") val goldEarned: Double = 0.0,
)

@Serializable
data class MemberPlayerStats (
	@SerialName("highest_critical_damage") val highestCriticalDamage: Double = 0.0,
	@SerialName("highest_damage") val highestDamage: Double = 0.0,
	@SerialName("sea_creature_kills") val seaCreatureKills: Double = 0.0,
	@SerialName("pets") val pets: MemberPlayerStatsPets = MemberPlayerStatsPets(),
	@SerialName("auctions") val auctions: MemberPlayerStatsAuctions = MemberPlayerStatsAuctions()
)

@Serializable
data class Member(
	@SerialName("player_id") val UUID: String,
	@SerialName("player_data") val playerData: MemberPlayerData = MemberPlayerData(),
	@SerialName("currencies") val currencies: MemberCurrencies = MemberCurrencies(),
	@SerialName("profile") val profile: MemberProfile = MemberProfile(),
	@SerialName("fairy_soul") val fairySoul: MemberFairySouls = MemberFairySouls(),
	@SerialName("player_stats") val playerStats: MemberPlayerStats = MemberPlayerStats(),
)

@Serializable
data class CoopInvitation(
    val timestamp: Instant,
    @SerialName("invited_by")
    val invitedBy: UUID? = null,
    val confirmed: Boolean,
)

@JvmInline
@Serializable
value class PetType(val name: String)

@Serializable
data class Pet(
    val uuid: UUID? = null,
    val type: PetType,
    val exp: Double = 0.0,
    val active: Boolean = false,
    val tier: Rarity,
    val candyUsed: Int = 0,
    val heldItem: String? = null,
    val skin: String? = null,
) {
    val itemId get() = SkyblockId("${type.name};${tier.ordinal}")
}

@Serializable
data class PlayerResponse(
    val success: Boolean,
    val player: PlayerData,
)

@Serializable
data class PlayerData(
    val uuid: UUID,
    val firstLogin: Instant,
    val lastLogin: Instant? = null,
    @SerialName("playername")
    val playerName: String,
    val achievementsOneTime: List<String> = listOf(),
    @SerialName("newPackageRank")
    val packageRank: String? = null,
    val monthlyPackageRank: String? = null,
    val rankPlusColor: String = "GOLD"
) {
    val rankPlusDyeColor = LegacyFormattingCode.values().find { it.name == rankPlusColor } ?: LegacyFormattingCode.GOLD
    val rankData get() = RepoManager.neuRepo.constants.misc.ranks[if (monthlyPackageRank == "NONE" || monthlyPackageRank == null) packageRank else monthlyPackageRank]
    fun getDisplayName(name: String = playerName) = rankData?.let {
        ("§${it.color}[${it.tag}${rankPlusDyeColor.modern}" +
                "${it.plus ?: ""}§${it.color}] $name")
    } ?: "${Formatting.GRAY}$name"


}

@Serializable
data class MowojangNameLookup(
    val name: String,
    val id: UUID,
)
