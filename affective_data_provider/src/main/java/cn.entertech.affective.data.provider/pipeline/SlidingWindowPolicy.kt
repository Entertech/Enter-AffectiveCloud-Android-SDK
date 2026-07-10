package cn.entertech.affective.data.provider.pipeline

class SlidingWindowPolicy<Point>(
    private val maxSize: Int,
) : WindowPolicy<Point> {
    init {
        require(maxSize > 0) { "maxSize must be greater than zero" }
    }

    override fun apply(points: List<Point>): List<Point> =
        if (points.size <= maxSize) points.toList() else points.takeLast(maxSize)
}
