package moe.nea.firmament.util.render

import org.joml.Matrix3x2f
import org.joml.Vector3f
import org.joml.Vector4f
import net.minecraft.client.gui.DrawContext

fun DrawContext.enableScissorWithTranslation(x1: Float, y1: Float, x2: Float, y2: Float) {
	enableScissor(x1.toInt(), y1.toInt(), x2.toInt(), y2.toInt())
}

fun DrawContext.enableScissorWithoutTranslation(x1: Float, y1: Float, x2: Float, y2: Float) {
	val pMat = Matrix3x2f(matrices).invert()
	var target = Vector3f()

	target.set(x1, y1, 1F)
	target.mul(pMat)
	val scissorX1 = target.x
	val scissorY1 = target.y

	target.set(x2, y2, 1F)
	target.mul(pMat)
	val scissorX2 = target.x
	val scissorY2 = target.y

	enableScissor(scissorX1.toInt(), scissorY1.toInt(), scissorX2.toInt(), scissorY2.toInt())
}
