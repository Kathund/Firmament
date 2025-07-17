package moe.nea.firmament.features.diana

import kotlin.time.Duration.Companion.seconds
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.SoundReceiveEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.WarpUtil
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems

object AncestralSpadeSolver : SubscriptionOwner {
	var lastDing = TimeMark.farPast()
		private set
	private val pitches = mutableListOf<Float>()
	val particlePositions = mutableListOf<Vec3d>()
	var nextGuess: Vec3d? = null
		private set

	private var lastTeleportAttempt = TimeMark.farPast()

	fun isEnabled() =
		DianaWaypoints.TConfig.ancestralSpadeSolver
			&& SBData.skyblockLocation == SkyBlockIsland.HUB
			&& MC.player?.inventory?.containsAny { it.skyBlockId == SkyBlockItems.ANCESTRAL_SPADE } == true // TODO: add a reactive property here

	@Subscribe
	fun onKeyBind(event: WorldKeyboardEvent) {
		if (!isEnabled()) return
		if (!event.matches(DianaWaypoints.TConfig.ancestralSpadeTeleport)) return

		if (lastTeleportAttempt.passedTime() < 3.seconds) return
		WarpUtil.teleportToNearestWarp(SkyBlockIsland.HUB, nextGuess ?: return)
		lastTeleportAttempt = TimeMark.now()
	}

	@Subscribe
	fun onParticleSpawn(event: ParticleSpawnEvent) {
		if (!isEnabled()) return
		if (event.particleEffect != ParticleTypes.DRIPPING_LAVA) return
		if (event.offset.x != 0.0F || event.offset.y != 0F || event.offset.z != 0F)
			return
		particlePositions.add(event.position)
		if (particlePositions.size > 20) {
			particlePositions.removeFirst()
		}
	}

	@Subscribe
	fun onPlaySound(event: SoundReceiveEvent) {
		if (!isEnabled()) return
		if (!SoundEvents.BLOCK_NOTE_BLOCK_HARP.matchesId(event.sound.value().id)) return

		if (lastDing.passedTime() > 1.seconds) {
			particlePositions.clear()
			pitches.clear()
		}
		lastDing = TimeMark.now()

		pitches.add(event.pitch)
		if (pitches.size > 20) {
			pitches.removeFirst()
		}

		if (particlePositions.size < 3) {
			return
		}

		val averagePitchDelta =
			if (pitches.isEmpty()) return
			else pitches
				.zipWithNext { a, b -> b - a }
				.average()

		val soundDistanceEstimate = (Math.E / averagePitchDelta) - particlePositions.first().distanceTo(event.position)

		if (soundDistanceEstimate > 1000) {
			return
		}

		val lastParticleDirection = particlePositions
			.takeLast(3)
			.let { (a, _, b) -> b.subtract(a) }
			.normalize()

		nextGuess = event.position.add(lastParticleDirection.multiply(soundDistanceEstimate))
	}

	@Subscribe
	fun onWorldRender(event: WorldRenderLastEvent) {
		if (!isEnabled()) return
		RenderInWorldContext.renderInWorld(event) {
			nextGuess?.let {
				tinyBlock(it, 1f, 0x80FFFFFF.toInt())
				// TODO: replace this
				tracer(it, lineWidth = 3f, color = 0x80FFFFFF.toInt())
			}
			if (particlePositions.size > 2 && lastDing.passedTime() < 10.seconds && nextGuess != null) {
				// TODO: replace this // TODO: add toggle
				line(particlePositions, color = 0x80FFFFFF.toInt())
			}
		}
	}

	@Subscribe
	fun onSwapWorld(event: WorldReadyEvent) {
		nextGuess = null
		particlePositions.clear()
		pitches.clear()
		lastDing = TimeMark.farPast()
	}

	override val delegateFeature: FirmamentFeature
		get() = DianaWaypoints

}
