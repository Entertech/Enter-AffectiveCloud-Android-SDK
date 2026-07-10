package cn.entertech.affective.sdk.manager.internal

import cn.entertech.affective.sdk.api.IAffectiveDataAnalysisService
import java.util.concurrent.ConcurrentHashMap

internal class AlgorithmRegistry<Key : Any> {
    private val slots = ConcurrentHashMap<Key, AlgorithmSlot>()

    fun get(key: Key): AlgorithmSlot? = slots[key]

    fun getOrCreate(
        key: Key,
        serviceFactory: () -> IAffectiveDataAnalysisService?,
    ): AlgorithmSlot? {
        slots[key]?.let { return it }
        val service = serviceFactory() ?: return null
        return slots.computeIfAbsent(key) { AlgorithmSlot(service) }
    }

    fun register(key: Key, service: IAffectiveDataAnalysisService): AlgorithmSlot {
        val replacement = AlgorithmSlot(service)
        slots.put(key, replacement)?.clearListeners()
        return replacement
    }

    fun remove(key: Key): AlgorithmSlot? = slots.remove(key)?.also(AlgorithmSlot::clearListeners)

    fun entries(): List<Map.Entry<Key, AlgorithmSlot>> = slots.entries.toList()

    fun values(): List<AlgorithmSlot> = slots.values.toList()

    fun clear() {
        slots.values.forEach(AlgorithmSlot::clearListeners)
        slots.clear()
    }
}
