package cn.entertech.affective.data.provider.pipeline

fun interface WindowPolicy<Point> {
    fun apply(points: List<Point>): List<Point>
}
