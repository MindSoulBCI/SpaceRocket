package com.example.spacerocket.p9d574e9f.ui.scene
import androidx.compose.ui.unit.IntSize
import com.pico.spatial.core.container.SpatialViewContent
import com.pico.spatial.core.ecs.BoundingBox
import com.pico.spatial.core.ecs.ViewCoordinateSpace
import com.pico.spatial.core.math.Vector3
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class BoundedSpatialViewRegions(
    val spatialView: BoundingBox,
    val windowContainer: BoundingBox,
)

fun SpatialViewContent.measureBoundedRegions(
    windowContentSizePx: IntSize,
): BoundedSpatialViewRegions? {
    if (windowContentSizePx.width <= 0 || windowContentSizePx.height <= 0) {
        return null
    }

    val localSpace = localSpatialCoordinateSpace
    val spatialViewTopLeftBack =
        convertPosition(Vector3.ZERO, ViewCoordinateSpace.Local, localSpace)

    val spatialViewHalfExtent =
        Vector3(
            abs(spatialViewTopLeftBack.x),
            abs(spatialViewTopLeftBack.y),
            abs(spatialViewTopLeftBack.z),
        )
    if (
        spatialViewHalfExtent.x <= 0f ||
            spatialViewHalfExtent.y <= 0f ||
            spatialViewHalfExtent.z <= 0f
    ) {
        return null
    }

    val windowTopLeftBack =
        convertPosition(Vector3.ZERO, ViewCoordinateSpace.Global, localSpace)
    val windowBottomRightBack =
        convertPosition(
            Vector3(
                windowContentSizePx.width.toFloat(),
                windowContentSizePx.height.toFloat(),
                0f,
            ),
            ViewCoordinateSpace.Global,
            localSpace,
        )

    return BoundedSpatialViewRegions(
        spatialView =
            BoundingBox(
                center = Vector3.ZERO,
                halfExtent = spatialViewHalfExtent,
            ),
        windowContainer =
            windowContainerBounds(
                windowTopLeftBack,
                windowBottomRightBack,
                spatialViewHalfExtent.z,
            ),
    )
}

private fun windowContainerBounds(
    topLeftBack: Vector3,
    bottomRightBack: Vector3,
    depthHalfExtent: Float,
): BoundingBox {
    val center =
        Vector3(
            (topLeftBack.x + bottomRightBack.x) * 0.5f,
            (topLeftBack.y + bottomRightBack.y) * 0.5f,
            0f,
        )
    val halfExtent =
        Vector3(
            abs(bottomRightBack.x - topLeftBack.x) * 0.5f,
            abs(bottomRightBack.y - topLeftBack.y) * 0.5f,
            depthHalfExtent,
        )
    return BoundingBox(
        center = center,
        halfExtent = halfExtent,
    )
}
