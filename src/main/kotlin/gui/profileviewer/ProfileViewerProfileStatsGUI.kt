package moe.nea.firmament.gui.profileviewer

import io.github.notenoughupdates.moulconfig.xml.Bind
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.util.FirmFormatters.formatAbsoluteTimespan
import moe.nea.firmament.util.FirmFormatters.shortFormat
import moe.nea.firmament.util.FirmFormatters.formatCommas
import moe.nea.firmament.util.FirmFormatters.formatUnixTimestamp
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import net.minecraft.text.Text

class ProfileViewerProfileStatsGUI(UUID: UUID, username: String, profile: Profile) {

	val member: Member = profile.members[UUID] ?: error("Primary player not in profile")

	@Bind("purse") fun getPurse(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.purse", shortFormat(member.currencies.coinPurse)).string
	@Bind("purseToolTip") fun getPurseToolTip(): List<String> = listOf(formatCommas(member.currencies.coinPurse.toInt()))
	@Bind("bankPurse") fun getBankPurse(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.bank", shortFormat(member.profile.bankAccount)).string
	@Bind("bankPurseToolTip") fun getBankPurseToolTip(): List<String> = listOf(formatCommas(member.profile.bankAccount.toInt()))
	@Bind("timeJoined") fun getTimeJoined(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.joined", formatAbsoluteTimespan((System.currentTimeMillis() - member.profile.firstJoin).milliseconds, true)).string
	@Bind("timeJoinedToolTip") fun getTimeJoinedTooltip(): List<String> = listOf(formatUnixTimestamp(member.profile.firstJoin.toLong()))
	@Bind("cookie") fun getCookie(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.cookie", if (member.profile.cookie) "active" else "inactive").string

	@Bind("dragonEssence") fun getDragonEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.dragonEssence", shortFormat(member.currencies.essence.dragon.current)).string
	@Bind("dragonEssenceToolTip") fun getDragonEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.dragon.current.toInt()))
	@Bind("diamondEssence") fun getDiamondEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.diamondEssence", shortFormat(member.currencies.essence.diamond.current)).string
	@Bind("diamondEssenceToolTip") fun getDiamondEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.diamond.current.toInt()))
	@Bind("spiderEssence") fun getSpiderEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.spiderEssence", shortFormat(member.currencies.essence.spider.current)).string
	@Bind("spiderEssenceToolTip") fun getSpiderEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.spider.current.toInt()))
	@Bind("goldEssence") fun getGoldEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.goldEssence", shortFormat(member.currencies.essence.gold.current)).string
	@Bind("goldEssenceToolTip") fun getGoldEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.gold.current.toInt()))
	@Bind("undeadEssence") fun getUndeadEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.undeadEssence", shortFormat(member.currencies.essence.undead.current)).string
	@Bind("undeadEssenceToolTip") fun getUndeadEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.undead.current.toInt()))
	@Bind("witherEssence") fun getWitherEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.witherEssence", shortFormat(member.currencies.essence.wither.current)).string
	@Bind("witherEssenceToolTip") fun getWitherEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.wither.current.toInt()))
	@Bind("iceEssence") fun getIceEssence(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.iceEssence", shortFormat(member.currencies.essence.ice.current)).string
	@Bind("iceEssenceToolTip") fun getIceEssenceToolTip(): List<String> = listOf(formatCommas(member.currencies.essence.ice.current.toInt()))

	@Bind("auctionBids") fun getAuctionBids(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.auctionBids", shortFormat(member.playerStats.auctions.bids)).string
	@Bind("auctionBidsToolTip") fun getAuctionBidsToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.bids.toInt()))
	@Bind("highestBid") fun getHighestBid(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.highestBid", shortFormat(member.playerStats.auctions.highestBid)).string
	@Bind("highestBidToolTip") fun getHighestBidToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.highestBid.toInt()))
	@Bind("auctionsWon") fun getAuctionsWon(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.auctionsWon", shortFormat(member.playerStats.auctions.auctionsWon)).string
	@Bind("auctionsWonToolTip") fun getAuctionsWonToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.auctionsWon.toInt()))
	@Bind("auctionsCreated") fun getAuctionsCreated(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.auctionsCreated", shortFormat(member.playerStats.auctions.auctionsCreated)).string
	@Bind("auctionsCreatedToolTip") fun getAuctionsCreatedToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.auctionsCreated.toInt()))
	@Bind("goldSpent") fun getGoldSpent(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.goldSpent", shortFormat(member.playerStats.auctions.goldSpent)).string
	@Bind("goldSpentToolTip") fun getGoldSpentToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.goldSpent.toInt()))
	@Bind("goldEarned") fun getGoldEarned(): String = Text.stringifiedTranslatable("firmament.pv.profilestats.goldEarned", shortFormat(member.playerStats.auctions.goldEarned)).string
	@Bind("goldEarnedToolTip") fun getGoldEarnedToolTip(): List<String> = listOf(formatCommas(member.playerStats.auctions.goldEarned.toInt()))

}
