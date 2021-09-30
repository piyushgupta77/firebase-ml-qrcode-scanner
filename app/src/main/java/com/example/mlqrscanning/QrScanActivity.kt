package com.example.mlqrscanning

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import com.example.mlqrscanning.model.AppDatabase
import com.example.mlqrscanning.model.QrEntity
import com.example.mlqrscanning.model.QrScanDao
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QrScanActivity : AppCompatActivity() {
    private lateinit var rescanBtn: Button
    private lateinit var cameraProvider: ProcessCameraProvider
    private var startTime: Long = 0
    private var endTime: Long = 0
    private lateinit var previewUseCase: Preview
    private lateinit var previewView: PreviewView
    private lateinit var cameraSelector: CameraSelector
    private lateinit var analysisUseCase: ImageAnalysis
    private lateinit var qrDao: QrScanDao
    private lateinit var scannedQrStatus: TextView
    private lateinit var qrResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scan)
        previewView = findViewById(R.id.preview_view)

        qrDao = AppDatabase.getInstance(applicationContext)?.qrDao()!!

        if (isCameraPermissionGranted()) {
            // startCamera
            setupCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_CAMERA_REQUEST
            )
        }

        scannedQrStatus = findViewById<Button>(R.id.scannedQrStatus)
        qrResult = findViewById<Button>(R.id.qrResult)

        rescanBtn = findViewById<Button>(R.id.rescan_button)
        rescanBtn.setOnClickListener {
            Log.d(TAG, "QrScan start")
            enableCameraView(true)
        }
    }

    private fun setupCamera() {
        var lensFacing = CameraSelector.LENS_FACING_BACK

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        ViewModelProvider(
            this, AndroidViewModelFactory.getInstance(application)
        ).get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this, Observer { provider: ProcessCameraProvider? ->
                if (isCameraPermissionGranted()) {
                    cameraProvider = provider!!
                    startTime = System.currentTimeMillis()
                    bindPreviewUseCase()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSION_CAMERA_REQUEST
                    )
                }
            }
            )
    }

    private fun bindPreviewUseCase() {
        previewUseCase = Preview.Builder()
            .setTargetRotation(previewView.display.rotation)
            .build()
        previewUseCase.setSurfaceProvider(previewView.createSurfaceProvider())

        try {
            bindAnalyseUseCase()

            cameraProvider.bindToLifecycle(this, cameraSelector, analysisUseCase, previewUseCase)

        } catch (illegalStateException: IllegalStateException) {
            illegalStateException.message?.let { Log.e(TAG, it) }
        } catch (illegalArgumentException: IllegalArgumentException) {
            illegalArgumentException.message?.let { Log.e(TAG, it) }
        }
    }

    private fun bindAnalyseUseCase() {
        analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setTargetRotation(previewView.display.rotation)
            .setTargetResolution(
                Size(
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels
                )
            )
            .build()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_ALL_FORMATS
            )
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)

        analysisUseCase.setAnalyzer(
            ContextCompat.getMainExecutor(applicationContext), { imageProxy ->
                processImageProxy(barcodeScanner, imageProxy)
            })
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.size > 0) {
                    enableCameraView(false)
                    Log.d(TAG, "QrScan size ${barcodes.size}")
                    endTime = System.currentTimeMillis()
                    // we get same barcode fro continuous frames hence just pick 1st barcode
                    barcodes[0].rawValue.let {
                        val qr = QrEntity(
                            System.currentTimeMillis(),
                            it,
                            startTime,
                            endTime,
                            (endTime - startTime)
                        )
                        qrDao.insert(qr)
                        Log.d(TAG, "QrScan success $qr")
                        scannedQrStatus.text = getString(R.string.scan_done)
                        qrResult.text =
                            it + "\n\nScanned in " + (endTime - startTime) + " ms"

                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.message!!)
            }.addOnCompleteListener {
                // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the camera
                // may stall.
                imageProxy.close()
            }
    }

    private fun enableCameraView(isEnabled: Boolean) {
        if (isEnabled) {
            previewView.visibility = View.VISIBLE
            setupCamera()
            scannedQrStatus.visibility = View.GONE
            qrResult.visibility = View.GONE
            startTime = System.currentTimeMillis()
            rescanBtn.visibility = View.GONE
        } else {
            cameraProvider.unbindAll()
            analysisUseCase.clearAnalyzer()
            previewView.visibility = View.GONE
            rescanBtn.visibility = View.VISIBLE
            scannedQrStatus.visibility = View.VISIBLE
            qrResult.visibility = View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                // start camera
                setupCamera()
            } else {
                Log.e(TAG, "no camera permission")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = QrScanActivity::class.java.simpleName
        private const val PERMISSION_CAMERA_REQUEST = 1
    }
}