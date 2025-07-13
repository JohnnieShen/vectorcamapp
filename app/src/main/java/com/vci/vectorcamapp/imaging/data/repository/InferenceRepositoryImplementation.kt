package com.vci.vectorcamapp.imaging.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import com.vci.vectorcamapp.core.domain.model.BoundingBox
import com.vci.vectorcamapp.imaging.data.GpuDelegateManager
import com.vci.vectorcamapp.imaging.di.AbdomenStatusClassifier
import com.vci.vectorcamapp.imaging.di.Detector
import com.vci.vectorcamapp.imaging.di.SexClassifier
import com.vci.vectorcamapp.imaging.di.SpeciesClassifier
import com.vci.vectorcamapp.imaging.di.SpecimenIdRecognizer
import com.vci.vectorcamapp.imaging.domain.SpecimenClassifier
import com.vci.vectorcamapp.imaging.domain.SpecimenDetector
import com.vci.vectorcamapp.imaging.domain.enums.AbdomenStatusLabel
import com.vci.vectorcamapp.imaging.domain.enums.SexLabel
import com.vci.vectorcamapp.imaging.domain.enums.SpeciesLabel
import com.vci.vectorcamapp.imaging.domain.repository.InferenceRepository
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

    override suspend fun detectSpecimen(bitmap: Bitmap): List<BoundingBox> =
        withContext(Dispatchers.Default) {
            specimenDetector.detect(bitmap)
        }

    override suspend fun classifySpecimen(croppedBitmap: Bitmap): Triple<SpeciesLabel?, SexLabel?, AbdomenStatusLabel?> =
        withContext(Dispatchers.Default) {
            val speciesPromise = async { getClassification(croppedBitmap, speciesClassifier) }
            val sexPromise = async { getClassification(croppedBitmap, sexClassifier) }
            val abdomenStatusPromise =
                async { getClassification(croppedBitmap, abdomenStatusClassifier) }

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
        }

    private suspend fun getClassification(
        croppedBitmap: Bitmap, classifier: SpecimenClassifier
    ): Int? = classifier.classify(croppedBitmap)

    override fun closeResources() {
        specimenIdRecognizer.close()
        specimenDetector.close()
        speciesClassifier.close()
        sexClassifier.close()
        abdomenStatusClassifier.close()
        GpuDelegateManager.close()
    }
}