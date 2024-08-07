@file:OptIn(kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, ObsoleteWorkersApi::class)

import kt42172.*
import kotlin.native.concurrent.*
import kotlinx.cinterop.*

fun main() {
    val worker = Worker.start()
    worker.execute(TransferMode.SAFE, {}) {
        val withFinalizer = WithFinalizer()
        val finalizer: Finalizer = staticCFunction { ptr: COpaquePointer? ->
            ptr?.asStableRef<Any>()?.dispose()
            println("Executed finalizer")
        }
        val arg = StableRef.create(Any()).asCPointer()
        withFinalizer.setFinalizer(finalizer, arg)
    }.result
    worker.requestTermination().result
    waitWorkerTermination(worker)

    // Enforce GC on program exit:
    kotlin.native.runtime.Debugging.forceCheckedShutdown = true
}
