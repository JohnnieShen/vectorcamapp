package com.vci.vectorcamapp.imaging.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.domain.SpecimenDetector
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TfLiteSpecimenDetector(
    private val context: Context
) : SpecimenDetector {

    private var detector: Interpreter? = null

    private val detectorLock = Any()
    private var isClosed = false

    private val handlerThread = HandlerThread("TFLiteSpecimenDetectorThread").apply { start() }
    private val handler = Handler(handlerThread.looper)

    private val imageProcessor by lazy {
        ImageProcessor.Builder().add(CastOp(DataType.FLOAT32)).add(NormalizeOp(0f, 255f)).build()
    }

    private var inputTensorHeight = DEFAULT_TENSOR_HEIGHT
    private var inputTensorWidth = DEFAULT_TENSOR_WIDTH

    private var outputNumChannels = DEFAULT_NUM_CHANNELS
    private var outputNumElements = DEFAULT_NUM_ELEMENTS

    init {
        handler.post { initializeInterpreter() }
    }

    private fun initializeInterpreter() {
        synchronized(detectorLock) {
            if (detector != null || isClosed) return

            try {
                val model = FileUtil.loadMappedFile(context, "detect.tflite")
                val options = Interpreter.Options()

                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    try {
                        options.addDelegate(GpuDelegateManager.getDelegate())
                        Log.d(TAG, "GPU delegate initialized")
                    } catch (e: Exception) {
                        Log.w(TAG, "GPU delegate failed: ${e.message}. Falling back to CPU.")
                    }
                }

                options.setNumThreads(Runtime.getRuntime().availableProcessors())
                detector = Interpreter(model, options)

                detector?.let {
                    inputTensorHeight = it.getInputTensor(0).shape()[1]
                    inputTensorWidth = it.getInputTensor(0).shape()[2]

                    outputNumChannels = it.getOutputTensor(0).shape()[1]
                    outputNumElements = it.getOutputTensor(0).shape()[2]
                }

                Log.d(TAG, "TFLite interpreter initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize TFLite interpreter: ${e.message}")
            }
        }
    }

    override fun getInputTensorShape(): Pair<Int, Int> = inputTensorHeight to inputTensorWidth

    override fun getOutputTensorShape(): Pair<Int, Int> = outputNumChannels to outputNumElements

    override suspend fun detect(bitmap: Bitmap): List<BoundingBox> {
        if (!isReady()) return emptyList()

        return suspendCoroutine { continuation ->
            handler.post {
                try {
                    val tensorImage = TensorImage(DataType.FLOAT32).apply {
                        load(bitmap.copy(Bitmap.Config.ARGB_8888, false))
                    }
                    val input = imageProcessor.process(tensorImage)

                    val output = TensorBuffer.createFixedSize(
                        intArrayOf(1, outputNumChannels, outputNumElements), DataType.FLOAT32
                    )

                    val result = synchronized(detectorLock) {
                        if (!isReady()) return@post continuation.resume(emptyList())
                        detector?.run(input.buffer, output.buffer)
                        getDetectedBoxes(output.floatArray)
                    }

                    continuation.resume(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Inference failed: ${e.message}")
                    continuation.resume(emptyList())
                }
            }
        }
    }

    override fun close() {
        synchronized(detectorLock) {
            if (isClosed) return
            isClosed = true

            handler.post {
                try {
                    detector?.close()
                    detector = null
                    handlerThread.quitSafely()
                    Log.d(TAG, "Detector closed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during detector close: ${e.message}")
                }
            }
        }
    }

    private fun isReady(): Boolean = synchronized(detectorLock) {
        !isClosed && detector != null
    }

    private fun getDetectedBoxes(boxes: FloatArray): List<BoundingBox> {
        val boundingBoxes = mutableListOf<BoundingBox>()

        for (i in 0 until outputNumChannels) {
            val baseIndex = i * outputNumElements
            val confidence = boxes[baseIndex + 4]

            if (confidence < CONFIDENCE_THRESHOLD) continue

            var maxClassScore = -1f
            var bestClassIndex = -1
            for (j in 5 until outputNumElements) {
                val score = boxes[baseIndex + j]
                if (score > maxClassScore) {
                    maxClassScore = score
                    bestClassIndex = j - 5
                }
            }

            val centerX = boxes[baseIndex]
            val centerY = boxes[baseIndex + 1]
            val width = boxes[baseIndex + 2]
            val height = boxes[baseIndex + 3]

            val topLeftX = centerX - (width / 2f)
            val topLeftY = centerY - (height / 2f)
            val bottomRightX = centerX + (width / 2f)
            val bottomRightY = centerY + (height / 2f)

            if (topLeftX < 0f || topLeftX > 1f || topLeftY < 0f || topLeftY > 1f || bottomRightX < 0f || bottomRightX > 1f || bottomRightY < 0f || bottomRightY > 1f) continue

            boundingBoxes.add(
                BoundingBox(
                    topLeftX = topLeftX,
                    topLeftY = topLeftY,
                    width = width,
                    height = height,
                    confidence = confidence,
                    classId = bestClassIndex
                )
            )
        }

        return applyNMS(boundingBoxes)
    }

    private fun applyNMS(boxes: List<BoundingBox>): MutableList<BoundingBox> {
        val sorted = boxes.sortedByDescending { it.confidence }.toMutableList()
        val selected = mutableListOf<BoundingBox>()

        while (sorted.isNotEmpty()) {
            val best = sorted.removeAt(0)
            selected.add(best)

            val iterator = sorted.iterator()
            while (iterator.hasNext()) {
                val other = iterator.next()
                if (calculateIoU(best, other) >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selected
    }

    private fun calculateIoU(a: BoundingBox, b: BoundingBox): Float {
        val x1 = maxOf(a.topLeftX, b.topLeftX)
        val y1 = maxOf(a.topLeftY, b.topLeftY)
        val x2 = minOf(a.topLeftX + a.width, b.topLeftX + b.width)
        val y2 = minOf(a.topLeftY + a.height, b.topLeftY + b.height)

        val intersection = maxOf(0f, x2 - x1) * maxOf(0f, y2 - y1)
        val areaA = a.width * a.height
        val areaB = b.width * b.height

        return if (areaA + areaB - intersection > 0) {
            intersection / (areaA + areaB - intersection)
        } else 0f
    }

    companion object {
        private const val TAG = "TfLiteSpecimenDetector"
        private const val DEFAULT_TENSOR_HEIGHT = 640
        private const val DEFAULT_TENSOR_WIDTH = 640
        private const val DEFAULT_NUM_CHANNELS = 25200
        private const val DEFAULT_NUM_ELEMENTS = 6
        private const val CONFIDENCE_THRESHOLD = 0.4f
        private const val IOU_THRESHOLD = 0.5f
    }
}
