package ch.and.pokemonpastropgo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import ch.and.pokemonpastropgo.databinding.ActivityQrcodeBinding
import ch.and.pokemonpastropgo.db.PPTGDatabaseApp
import ch.and.pokemonpastropgo.viewModels.PokemonToHuntViewModel
import ch.and.pokemonpastropgo.viewModels.ViewModelFactory
import java.io.IOException
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.Detector.Detections

// Inspired from: https://harshitabambure.medium.com/barcode-scanner-and-qr-code-scanner-android-kotlin-b911b1299f65
class QRCodeActivity : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var qrCodeActivityBinding: ActivityQrcodeBinding

    // View Model
    private val toHuntVm: PokemonToHuntViewModel by viewModels {
        ViewModelFactory((application as PPTGDatabaseApp).pokemonToHuntRepository)
    }

    private var zoneId = -1L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        qrCodeActivityBinding = ActivityQrcodeBinding.inflate(layoutInflater)
        val view = qrCodeActivityBinding.root
        setContentView(view)
        if (ContextCompat.checkSelfPermission(
                this@QRCodeActivity, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupControls()
        }

        val aniSlide: Animation = AnimationUtils.loadAnimation(this@QRCodeActivity, R.anim.scanner_animation)
        qrCodeActivityBinding.barcodeLine.startAnimation(aniSlide)

        zoneId = intent.getLongExtra("zoneId", -1)
    }

    private fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        qrCodeActivityBinding.cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    Log.d("QRCodeActivity", "surfaceCreated")
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d("QRCodeActivity", "surfaceChanged")
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("QRCodeActivity", "surfaceDestroyed")
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        cameraSource.stop()

                        // When QR-code has been scanned, it shows the value in a toast and sends it back to the map activity through an intent
                        Toast.makeText(this@QRCodeActivity, "value- $scannedValue", Toast.LENGTH_SHORT).show()
                        if (zoneId == scannedValue.substringAfter("_").toLong()) {
                            val res = Intent()
                            res.putExtra(SCAN_QR_RESULT_KEY, scannedValue)
                            setResult(RESULT_OK, res)
                            toHuntVm.foundPokemon(scannedValue)
                        } else {
                            Log.d("ERROR", "You are not in the required zone")
                        }
                        finish()
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource.stop()
    }

    companion object {
        const val SCAN_QR_RESULT_KEY = "QR_KEY"
    }
}
