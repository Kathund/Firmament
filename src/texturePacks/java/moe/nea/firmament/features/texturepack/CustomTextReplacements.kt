package moe.nea.firmament.features.texturepack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.text.Text
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.util.ErrorUtil.intoCatch
import moe.nea.firmament.util.formattedString
import moe.nea.firmament.util.unformattedString

object CustomTextReplacements : SinglePreparationResourceReloader<List<CustomTextReplacements.CustomTextReplacement>>() {

	@Serializable
	data class CustomTextReplacement(
		val predicates: StringMatcher,
		val textTitle: TextReplacer? = null
	)

	@Serializable
	data class TextReplacer(
		val replace: String? = null
	)


	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloader(CustomTextReplacements)
	}

	override fun prepare(
		manager: ResourceManager,
		profiler: Profiler
	): List<CustomTextReplacement> {
		val allScreenLayouts = manager.findResources(
			"overrides/text_replacement",
			{ it.path.endsWith(".json") && it.namespace == "firmskyblock" })
		val allParsedLayouts = allScreenLayouts.mapNotNull { (path, stream) ->
			Firmament.tryDecodeJsonFromStream<CustomTextReplacement>(stream.inputStream)
				.intoCatch("Could not read custom text replacement from $path").orNull()
		}
		return allParsedLayouts
	}

	var customTextReplacements = listOf<CustomTextReplacement>()

	override fun apply(
		prepared: List<CustomTextReplacement>,
		manager: ResourceManager?,
		profiler: Profiler?
	) {
		this.customTextReplacements = prepared
	}

	@Subscribe
	fun displayLore(event: ItemTooltipEvent) {
		for (i in event.lines.indices) {
			customTextReplacements.find {
				(it.predicates as StringMatcher.Pattern).patternWithColorCodes.toRegex()
					.containsMatchIn(event.lines[i].unformattedString)
			}?.let {
				if (it.textTitle?.replace != null) {
					event.lines[i] = Text.literal(
						event.lines[i].formattedString().replace(
							(it.predicates as StringMatcher.Pattern).patternWithColorCodes,
							it.textTitle.replace
						)
					)
				}
			}
		}
	}
}

