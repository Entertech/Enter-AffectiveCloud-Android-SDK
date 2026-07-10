package cn.entertech.affective.data.provider.pipeline

fun interface DataFilter<Point> {
    fun accept(point: Point): Boolean
}
