package com.vci.vectorcamapp.imaging.data.repository

import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.vci.vectorcamapp.imaging.data.GpuDelegateManager
import com.vci.vectorcamapp.imaging.di.AbdomenStatusClassifier
import com.vci.vectorcamapp.imaging.di.Detector
import com.vci.vectorcamapp.imaging.di.SexClassifier
import com.vci.vectorcamapp.imaging.di.SpeciesClassifier
import com.vci.vectorcamapp.imaging.di.SpecimenIdRecognizer
import com.vci.vectorcamapp.imaging.domain.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.BoundingBox
import com.vci.vectorcamapp.imaging.domain.SexLabel
import com.vci.vectorcamapp.imaging.domain.SpeciesLabel
import com.vci.vectorcamapp.imaging.domain.SpecimenClassifier
import com.vci.vectorcamapp.imaging.domain.SpecimenDetector
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
import com.vci.vectorcamapp.imaging.presentation.extensions.cropToBoundingBoxAndPad
import com.vci.vectorcamapp.imaging.presentation.extensions.resizeTo
import com.vci.vectorcamapp.imaging.presentation.model.BoundingBoxUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InferenceRepositoryImplementation @Inject constructor(
    @SpecimenIdRecognizer private val specimenIdRecognizer: TextRecognizer,
    @Detector private val specimenDetector: SpecimenDetector,
    @SpeciesClassifier private val speciesClassifier: SpecimenClassifier,
    @SexClassifier private val sexClassifier: SpecimenClassifier,
    @AbdomenStatusClassifier private val abdomenStatusClassifier: SpecimenClassifier,
) : InferenceRepository {
    override suspend fun readSpecimenId(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            suspendCoroutine { continuation ->
                specimenIdRecognizer.process(inputImage).addOnSuccessListener { visionText ->
                    val id = visionText.text.lineSequence().firstOrNull()?.trim().orEmpty()
                    continuation.resume(id)
                }.addOnFailureListener { exception ->
                    Log.e("Repository", "Text recognition failed: ${exception.message}")
                    continuation.resume("")
                }
            }
        } catch (e: Exception) {
            Log.e("Repository", "Specimen ID analysis exception: ${e.message}", e)
            ""
        }
    }

    override suspend fun detectSpecimen(bitmap: Bitmap): Pair<Bitmap, BoundingBox?> =
        withContext(Dispatchers.Default) {
            val (tensorHeight, tensorWidth) = specimenDetector.getInputTensorShape()

            val resized = bitmap.resizeTo(tensorWidth, tensorHeight)
            Pair(resized, specimenDetector.detect(resized))
        }

    override suspend fun classifySpecimen(bitmap: Bitmap): Triple<SpeciesLabel?, SexLabel?, AbdomenStatusLabel?> =
        withContext(Dispatchers.Default) {
            val (resized, boundingBox) = detectSpecimen(bitmap)

            val croppedAndPadded = boundingBox?.let {
                resized.cropToBoundingBoxAndPad(it)
            }

            croppedAndPadded?.let {
                val speciesPromise = async { getClassification(it, speciesClassifier) }
                val sexPromise = async { getClassification(it, sexClassifier) }
                val abdomenStatusPromise = async { getClassification(it, abdomenStatusClassifier) }

                val species =
                    speciesPromise.await()?.let { index -> SpeciesLabel.entries.getOrNull(index) }
                var sex = sexPromise.await()?.let { index -> SexLabel.entries.getOrNull(index) }
                var abdomenStatus = abdomenStatusPromise.await()
                    ?.let { index -> AbdomenStatusLabel.entries.getOrNull(index) }

                if (species == SpeciesLabel.NON_MOSQUITO || species == null) {
                    sex = null
                }
                if (sex == SexLabel.MALE || sex == null) {
                    abdomenStatus = null
                }

                Triple(species, sex, abdomenStatus)
            } ?: Triple(null, null, null)
        }

    override fun convertToBoundingBoxUi(
        boundingBox: BoundingBox?, imageWidth: Int, imageHeight: Int
    ): BoundingBoxUi? {
        if (boundingBox == null) return null

        val (tensorHeight, tensorWidth) = specimenDetector.getInputTensorShape()

        val screenHeight = Resources.getSystem().displayMetrics.heightPixels.toFloat()
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels.toFloat()

        val previewHeight = screenHeight
        val previewWidth = (imageWidth.toFloat() / imageHeight.toFloat()) * previewHeight

        val scaleX = previewWidth / tensorWidth.toFloat()
        val scaleY = previewHeight / tensorHeight.toFloat()

        val xOffset = (previewWidth - screenWidth) / 2

        val scaledX = (boundingBox.topLeftX * tensorWidth * scaleX) - xOffset
        val scaledY = boundingBox.topLeftY * tensorHeight * scaleY
        val scaledWidth = boundingBox.width * tensorWidth * scaleX
        val scaledHeight = boundingBox.height * tensorHeight * scaleY

        return BoundingBoxUi(
            topLeftX = scaledX,
            topLeftY = scaledY,
            width = scaledWidth,
            height = scaledHeight,
            confidence = boundingBox.confidence,
            classId = boundingBox.classId,
        )
    }

    override fun closeResources() {
        specimenIdRecognizer.close()
        specimenDetector.close()
        speciesClassifier.close()
        sexClassifier.close()
        abdomenStatusClassifier.close()
        GpuDelegateManager.close()
    }

    private suspend fun getClassification(bitmap: Bitmap, classifier: SpecimenClassifier): Int? {
        val (classifierTensorHeight, classifierTensorWidth) = classifier.getInputTensorShape()

        return classifier.classify(bitmap.resizeTo(classifierTensorWidth, classifierTensorHeight))
    }
}