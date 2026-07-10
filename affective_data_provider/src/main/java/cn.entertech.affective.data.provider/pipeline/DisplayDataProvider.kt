package cn.entertech.affective.data.provider.pipeline

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DisplayDataProvider<Input, Point>(
    initial: List<Point> = emptyList(),
    private val selector: DataSelector<Input, Point>,
    private val filter: DataFilter<Point> = DataFilter { true },
    private val windowPolicy: WindowPolicy<Point>,
) {
    private val lock = Any()
    private val initialSnapshot = windowPolicy.apply(initial).toList()
    private val mutableData = MutableStateFlow(initialSnapshot)

    val data: StateFlow<List<Point>> = mutableData.asStateFlow()

    fun accept(input: Input) {
        val point = selector.select(input)
        if (!filter.accept(point)) return
        synchronized(lock) {
            mutableData.value = windowPolicy.apply(mutableData.value + point).toList()
        }
    }

    fun clear() {
        synchronized(lock) {
            mutableData.value = initialSnapshot
        }
    }
}
