package moe.nea.firmament.features.chat

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.CaseInsensitiveLiteralCommandNode
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.PartyMessageReceivedEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.useMatch
import kotlin.math.round
import kotlin.random.Random

object PartyCommands {

	val messageInChannel = "(?<channel>Party|Guild) >([^:]+?)? (?<name>[^: ]+): (?<message>.+)".toPattern()

	@Subscribe
	fun onChat(event: ProcessChatEvent) {
		messageInChannel.useMatch(event.unformattedString) {
			val channel = group("channel")
			val message = group("message")
			val name = group("name")
			if (channel == "Party") {
				PartyMessageReceivedEvent.publish(PartyMessageReceivedEvent(
					event, message, name
				))
			}
		}
	}

	val commandPrefixes = "!-?$.&#+~€\"@°_;:³²`'´ß\\,|".toSet()

	data class PartyCommandContext(
		val name: String
	)

	val dispatch = CommandDispatcher<PartyCommandContext>().also { dispatch ->
		fun register(
			name: String,
			vararg alias: String,
			block: CaseInsensitiveLiteralCommandNode.Builder<PartyCommandContext>.() -> Unit = {},
		): LiteralCommandNode<PartyCommandContext> {
			val node =
				dispatch.register(CaseInsensitiveLiteralCommandNode.Builder<PartyCommandContext>(name).also(block))
			alias.forEach { register(it) { redirect(node) } }
			return node
		}

		register("warp", "pw", "pwarp", "partywarp") {
			executes {
				if (!TConfig.warpCommand) return@executes 0
				// TODO: add check if you are the party leader
				MC.sendCommand("p warp")
				0
			}
		}

		register("transfer", "pt", "ptme") {
			executes {
				if (!TConfig.transferCommand) return@executes 0
				MC.sendCommand("p transfer ${it.source.name}")
				0
			}
		}

		register("allinvite", "allinv") {
			executes {
				if (!TConfig.allinviteCommand) return@executes 0
				MC.sendCommand("p settings allinvite")
				0
			}
		}

		register("coords") {
			executes {
				if (!TConfig.coordsCommand) return@executes 0
				val p = MC.player?.blockPos ?: BlockPos.ORIGIN
				MC.sendCommand("pc x: ${p.x}, y: ${p.y}, z: ${p.z}")
				0
			}
		}

		register("ping") {
			executes {
				if (!TConfig.pingCommand) return@executes 0
				val ping = MC.player?.let {
					val entry: PlayerListEntry? = MC.networkHandler?.getPlayerListEntry(it.uuid)
					entry?.latency ?: -1
				} ?: -1
				MC.sendCommand(
					"pc ${
						String.format(
							tr(
								"firmament.config.hud.ping-count-hud.display", "Ping: %s ms"
							).string, ping
						)
					}"
				)
				0
			}
		}

		register("fps") {
			executes {
				if (!TConfig.fpsCommand) return@executes 0
				MC.sendCommand(
					"pc ${
						String.format(
							tr("firmament.config.hud.fps-count-hud.display", "FPS: %s").string, MC.instance.currentFps
						)
					}"
				)
				0
			}
		}

		register("gay") {
			executes {
				if (!TConfig.gayCommand) return@executes 0
				MC.sendCommand("pc ${it.source.name} is ${if (it.source.name == "lrg89") "100.0" else calcGay()}% gay!")
				0
			}
		}

		register("racism", "racist") {
			executes {
				if (!TConfig.racismCommand) return@executes 0
				MC.sendCommand("pc ${it.source.name} is ${if (it.source.name == "lrg89") "100.0" else calcGay()}% racist!")
				0
			}
		}

		// TODO: downtime tracker (display message again at end of dungeon)
		// instance ends: kuudra, dungeons, bacte
		// TODO: at TPS command
	}

	object TConfig : ManagedConfig("party-commands", Category.CHAT) {
		val enable by toggle("enable") { false }
		val cooldown by duration("cooldown", 0.seconds, 20.seconds) { 2.seconds }
		val ignoreOwnCommands by toggle("ignore-own") { false }
		val warpCommand by toggle("warp") { true }
		val transferCommand by toggle("transfer") { true }
		val allinviteCommand by toggle("allinvite") { true }
		val coordsCommand by toggle("coords") { true }
		val pingCommand by toggle("ping") { true }
		val fpsCommand by toggle("fps") { true }
		val gayCommand by toggle("gay") { true }
		val racismCommand by toggle("racism") { true }
	}

	var lastCommand = TimeMark.farPast()

	@Subscribe
	fun listPartyCommands(event: CommandEvent.SubCommand) {
		event.subcommand("partycommands") {
			thenExecute {
				// TODO: Better help, including descriptions and redirect detection
				MC.sendChat(tr("firmament.partycommands.help", "Available party commands: ${dispatch.root.children.map { it.name }}. Available prefixes: $commandPrefixes"))
			}
		}
	}

	@Subscribe
	fun onPartyMessage(event: PartyMessageReceivedEvent) {
		if (!TConfig.enable) return
		if (event.message.firstOrNull() !in commandPrefixes) return
		if (event.name == MC.playerName && TConfig.ignoreOwnCommands) return
		if (lastCommand.passedTime() < TConfig.cooldown) {
			MC.sendChat(tr("firmament.partycommands.cooldown", "Skipping party command. Cooldown not passed."))
			return
		}
		// TODO: add trust levels
		val commandLine = event.message.substring(1)
		try {
			dispatch.execute(StringReader(commandLine), PartyCommandContext(event.name))
		} catch (ex: Exception) {
			if (ex is CommandSyntaxException) {
				MC.sendChat(tr("firmament.partycommands.unknowncommand", "Unknown party command."))
				return
			} else {
				MC.sendChat(tr("firmament.partycommands.unknownerror", "Unknown error during command execution."))
				ErrorUtil.softError("Unknown error during command execution.", ex)
			}
		}
		lastCommand = TimeMark.now()
	}

	fun calcGay(): Double {
		val raw = Random.nextDouble(0.0, 100.0)
		return round(raw * 100) / 100
	}
}
