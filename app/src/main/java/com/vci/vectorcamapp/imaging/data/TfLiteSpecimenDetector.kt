package com.vci.vectorcamapp.imaging.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.vci.vectorcamapp.core.domain.model.InferenceResult
import com.vci.vectorcamapp.imaging.domain.SpecimenDetector
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.MatOfRect2d
import org.opencv.core.Rect
import org.opencv.core.Rect2d
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.dnn.Dnn
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max
import kotlin.math.roundToInt

class TfLiteSpecimenDetector(
    private val context: Context
) : SpecimenDetector {

    private var detector: Interpreter? = null

    private val detectorLock = Any()
    private var isClosed = false

    private val handlerThread = HandlerThread("TFLiteSpecimenDetectorThread").apply { start() }
    private val handler = Handler(handlerThread.looper)

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

//                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
//                    try {
//                        options.addDelegate(GpuDelegateManager.getDelegate())
//                        Log.d(TAG, "GPU delegate initialized")
//                    } catch (e: Exception) {
//                        Log.w(TAG, "GPU delegate failed: ${e.message}. Falling back to CPU.")
//                    }
//                }

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

    override suspend fun detect(bitmap: Bitmap): List<InferenceResult> {
        if (!isReady()) return emptyList()

        return suspendCoroutine { continuation ->
            handler.post {
                try {
                    val inputMatrix = prepareInputMatrix(bitmap)

                    val preprocessedMatrix = preprocessMatrix(inputMatrix)
                    val preprocessedMatrixHeight = preprocessedMatrix.height()
                    val preprocessedMatrixWidth = preprocessedMatrix.width()
                    val preprocessedMatrixChannels = preprocessedMatrix.channels()

                    val inputTensor = TensorBuffer.createFixedSize(
                        intArrayOf(
                            1,
                            preprocessedMatrixHeight,
                            preprocessedMatrixWidth,
                            preprocessedMatrixChannels
                        ), DataType.FLOAT32
                    )
                    val inputFloatBuffer =
                        FloatArray(preprocessedMatrixHeight * preprocessedMatrixWidth * preprocessedMatrixChannels)
                    preprocessedMatrix.get(0, 0, inputFloatBuffer)
                    inputTensor.loadArray(inputFloatBuffer)

                    val outputTensor = TensorBuffer.createFixedSize(
                        intArrayOf(1, outputNumChannels, outputNumElements), DataType.FLOAT32
                    )

                    val result = synchronized(detectorLock) {
                        if (!isReady()) return@post continuation.resume(emptyList())
                        detector?.run(inputTensor.buffer, outputTensor.buffer)
                        getDetectedResults(outputTensor.floatArray)
                    }

                    continuation.resume(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Inference failed: ${e.message}")
                    continuation.resume(emptyList())
                }
            }
        }
    }

    private fun isReady(): Boolean = synchronized(detectorLock) {
        !isClosed && detector != null
    }

    private fun prepareInputMatrix(bitmap: Bitmap): Mat {
        val inputMatrix = Mat()
        Utils.bitmapToMat(bitmap, inputMatrix)
        Imgproc.cvtColor(inputMatrix, inputMatrix, Imgproc.COLOR_RGBA2BGR)
        return inputMatrix
    }

    private fun preprocessMatrix(inputMatrix: Mat): Mat {
        val resizedMatrix = Mat()
        val resizedMatrixWidth = (inputTensorWidth / ASPECT_RATIO).toInt()
        val resizedMatrixHeight = inputTensorHeight
        Imgproc.resize(
            inputMatrix,
            resizedMatrix,
            Size(resizedMatrixWidth.toDouble(), resizedMatrixHeight.toDouble())
        )

        val paddedSideLength = max(resizedMatrixWidth, resizedMatrixHeight)
        val paddedMatrix =
            Mat(paddedSideLength, paddedSideLength, resizedMatrix.type(), Scalar(0.0, 0.0, 0.0))

        val xOffset = (paddedSideLength - resizedMatrixWidth) / 2
        val yOffset = (paddedSideLength - resizedMatrixHeight) / 2
        val regionOfIntersection = paddedMatrix.submat(
            Rect(xOffset, yOffset, resizedMatrixWidth, resizedMatrixHeight)
        )
        resizedMatrix.copyTo(regionOfIntersection)
        paddedMatrix.convertTo(paddedMatrix, CvType.CV_32F, PIXEL_NORMALIZATION_SCALE.toDouble())
        return paddedMatrix
    }

    private fun getDetectedResults(boxes: FloatArray): List<InferenceResult> {
        val predictions = mutableListOf<FloatArray>()

        for (i in 0 until outputNumChannels) {
            val prediction = FloatArray(outputNumElements)
            for (j in 0 until outputNumElements) {
                prediction[j] = boxes[i * outputNumElements + j]
            }
            if (prediction[4] >= CONFIDENCE_THRESHOLD) {
                predictions.add(prediction)
            }
        }

        if (predictions.isEmpty()) return emptyList()

        val boxesForNms = mutableListOf<Rect2d>()
        val confidenceScoresForNms = mutableListOf<Float>()

        for (prediction in predictions) {
            val centerXNormalized = prediction[0]
            val centerYNormalized = prediction[1]
            val widthNormalized = prediction[2]
            val heightNormalized = prediction[3]
            val confidence = prediction[4]

            val topLeftXAbsolute = (centerXNormalized - (widthNormalized / 2f)) * inputTensorWidth
            val topLeftYAbsolute = (centerYNormalized - (heightNormalized / 2f)) * inputTensorHeight
            val widthAbsolute = widthNormalized * inputTensorWidth
            val heightAbsolute = heightNormalized * inputTensorHeight

            boxesForNms.add(
                Rect2d(
                    topLeftXAbsolute.toDouble(),
                    topLeftYAbsolute.toDouble(),
                    widthAbsolute.toDouble(),
                    heightAbsolute.toDouble()
                )
            )
            confidenceScoresForNms.add(confidence)
        }

        val boxesMatrix = MatOfRect2d(*boxesForNms.toTypedArray())
        val confidenceScoresMatrix = MatOfFloat(*confidenceScoresForNms.toFloatArray())
        val indicesMatrix = MatOfInt()
        Dnn.NMSBoxes(
            boxesMatrix, confidenceScoresMatrix, CONFIDENCE_THRESHOLD, IOU_THRESHOLD, indicesMatrix
        )

        if (indicesMatrix.empty()) return emptyList()

        val selectedIndices = indicesMatrix.toArray()
        val finalInferenceResults = mutableListOf<InferenceResult>()

        for (index in selectedIndices) {
            val prediction = predictions[index]

            var centerX = prediction[0]
            val centerY = prediction[1]
            var width = prediction[2]
            val height = prediction[3]
            val confidence = prediction[4]
            val classId = prediction[5].roundToInt()

            val offset =
                ((inputTensorWidth - (inputTensorWidth / ASPECT_RATIO)) / 2f) / inputTensorWidth
            centerX -= offset
            centerX *= ASPECT_RATIO
            width *= ASPECT_RATIO

            val topLeftX = (centerX - width / 2f).coerceAtLeast(0f)
            val topLeftY = (centerY - height / 2f).coerceAtLeast(0f)

            val inferenceResult = InferenceResult(
                bboxTopLeftX = topLeftX,
                bboxTopLeftY = topLeftY,
                bboxWidth = width,
                bboxHeight = height,
                bboxConfidence = confidence,
                bboxClassId = classId,
                speciesLogits = null,
                sexLogits = null,
                abdomenStatusLogits = null,
            )
            Log.d("InferenceResult", "($topLeftX, $topLeftY) -> ($width, $height)")

            finalInferenceResults.add(inferenceResult)
        }

        return finalInferenceResults
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

    companion object {
        private const val TAG = "TfLiteSpecimenDetector"
        private const val DEFAULT_TENSOR_HEIGHT = 640
        private const val DEFAULT_TENSOR_WIDTH = 640
        private const val DEFAULT_NUM_CHANNELS = 25200
        private const val DEFAULT_NUM_ELEMENTS = 6
        private const val CONFIDENCE_THRESHOLD = 0.8f
        private const val IOU_THRESHOLD = 0.5f

        private const val PIXEL_NORMALIZATION_SCALE = 1f / 255f
        private const val ASPECT_RATIO = 4f / 3f
    }
}
