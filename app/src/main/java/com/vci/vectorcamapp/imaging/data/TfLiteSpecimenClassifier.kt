package com.vci.vectorcamapp.imaging.data

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.vci.vectorcamapp.imaging.domain.SpecimenClassifier
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max

class TfLiteSpecimenClassifier(
    private val context: Context,
    private val filePath: String,
    threadName: String,
) : SpecimenClassifier {

    private var classifier: Interpreter? = null

    private val classifierLock = Any()
    private var isClosed = false

    private val handlerThread = HandlerThread(threadName).apply { start() }
    private val handler = Handler(handlerThread.looper)

    private var inputTensorHeight = DEFAULT_TENSOR_HEIGHT
    private var inputTensorWidth = DEFAULT_TENSOR_WIDTH

    private var outputNumClasses = DEFAULT_NUM_CLASSES

    init {
        handler.post { initializeInterpreter() }
    }

    private fun initializeInterpreter() {
        synchronized(classifierLock) {
            if (classifier != null || isClosed) return

            try {
                val model = FileUtil.loadMappedFile(context, filePath)
                val options = Interpreter.Options()

                options.setNumThreads(Runtime.getRuntime().availableProcessors())
                classifier = Interpreter(model, options)

                classifier?.let {
                    inputTensorHeight = it.getInputTensor(0).shape()[2]
                    inputTensorWidth = it.getInputTensor(0).shape()[3]

                    outputNumClasses = it.getOutputTensor(0).shape()[1]
                }

                Log.d(TAG, "TFLite interpreter initialized")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize TFLite interpreter: ${e.message}")
            }
        }
    }

    private fun isReady(): Boolean = synchronized(classifierLock) {
        !isClosed && classifier != null
    }

    override fun getInputTensorShape(): Pair<Int, Int> = inputTensorHeight to inputTensorWidth

    override fun getOutputTensorShape(): Int = outputNumClasses

    override suspend fun classify(croppedBitmap: Bitmap): List<Float>? {
        if (!isReady()) return null

        return suspendCoroutine { continuation ->
            handler.post {
                try {
                    val inputMatrix = prepareInputMatrix(croppedBitmap)

                    val preprocessedMatrix = preprocessMatrix(inputMatrix)
                    val preprocessedMatrixHeight = preprocessedMatrix.height()
                    val preprocessedMatrixWidth = preprocessedMatrix.width()
                    val preprocessedMatrixChannels = preprocessedMatrix.channels()

                    val inputTensor = TensorBuffer.createFixedSize(
                        intArrayOf(
                            1,
                            preprocessedMatrixChannels,
                            preprocessedMatrixHeight,
                            preprocessedMatrixWidth
                        ), DataType.FLOAT32
                    )
                    val inputFloatBuffer =
                        FloatArray(preprocessedMatrixHeight * preprocessedMatrixWidth * preprocessedMatrixChannels)
                    preprocessedMatrix.get(0, 0, inputFloatBuffer)

                    val chwArray =
                        FloatArray(preprocessedMatrixChannels * preprocessedMatrixHeight * preprocessedMatrixWidth)
                    for (channel in 0 until preprocessedMatrixChannels) {
                        for (i in 0 until preprocessedMatrixHeight * preprocessedMatrixWidth) {
                            chwArray[channel * preprocessedMatrixHeight * preprocessedMatrixWidth + i] =
                                inputFloatBuffer[i * preprocessedMatrixChannels + channel]
                        }
                    }
                    inputTensor.loadArray(chwArray)

                    val outputTensor = TensorBuffer.createFixedSize(
                        intArrayOf(1, outputNumClasses), DataType.FLOAT32
                    )

                    val result = synchronized(classifierLock) {
                        if (!isReady()) return@post continuation.resume(null)
                        classifier?.run(inputTensor.buffer, outputTensor.buffer)
                        outputTensor.floatArray.toList()
                    }

                    Log.d(TAG, "Inference result: $result")
                    continuation.resume(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Inference failed: ${e.message}")
                    continuation.resume(null)
                }
            }
        }
    }

    private fun prepareInputMatrix(croppedBitmap: Bitmap): Mat {
        val inputMatrix = Mat()
        Utils.bitmapToMat(croppedBitmap, inputMatrix)
        Imgproc.cvtColor(inputMatrix, inputMatrix, Imgproc.COLOR_RGBA2RGB)
        return inputMatrix
    }

    private fun preprocessMatrix(inputMatrix: Mat): Mat {
        val inputMatrixWidth = inputMatrix.width()
        val inputMatrixHeight = inputMatrix.height()
        val paddedSideLength = max(inputMatrixWidth, inputMatrixHeight)
        val paddedMatrix = Mat.zeros(paddedSideLength, paddedSideLength, inputMatrix.type())

        val rowStart = (paddedSideLength - inputMatrixHeight) / 2
        val rowEnd = rowStart + inputMatrixHeight
        val colStart = (paddedSideLength - inputMatrixWidth) / 2
        val colEnd = colStart + inputMatrixWidth
        val regionOfIntersection = paddedMatrix.submat(rowStart, rowEnd, colStart, colEnd)
        inputMatrix.copyTo(regionOfIntersection)

        val resizedMatrix = Mat()
        Imgproc.resize(
            paddedMatrix,
            resizedMatrix,
            Size(inputTensorWidth.toDouble(), inputTensorHeight.toDouble())
        )
        resizedMatrix.convertTo(resizedMatrix, CvType.CV_32F, PIXEL_NORMALIZATION_SCALE.toDouble())

        val meanMatrix = Mat(resizedMatrix.size(), CvType.CV_32FC3, NORMALIZE_MEAN)
        val stdDevMatrix = Mat(resizedMatrix.size(), CvType.CV_32FC3, NORMALIZE_STDDEV)
        Core.subtract(resizedMatrix, meanMatrix, resizedMatrix)
        Core.divide(resizedMatrix, stdDevMatrix, resizedMatrix)

        return resizedMatrix
    }

    override fun close() {
        synchronized(classifierLock) {
            if (isClosed) return
            isClosed = true

            handler.post {
                try {
                    classifier?.close()
                    classifier = null
                    handlerThread.quitSafely()
                    Log.d(TAG, "Classifier closed")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during classifier close: ${e.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "TfLiteSpeciesClassifier"
        private const val DEFAULT_TENSOR_HEIGHT = 300
        private const val DEFAULT_TENSOR_WIDTH = 300
        private const val DEFAULT_NUM_CLASSES = 1

        private const val PIXEL_NORMALIZATION_SCALE = 1f / 255f
        private val NORMALIZE_MEAN = Scalar(0.485, 0.456, 0.406)
        private val NORMALIZE_STDDEV = Scalar(0.229, 0.224, 0.225)
    }
}