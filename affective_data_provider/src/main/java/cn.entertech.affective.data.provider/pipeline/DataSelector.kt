package cn.entertech.affective.data.provider.pipeline

fun interface DataSelector<Input, Point> {
    fun select(input: Input): Point
}
