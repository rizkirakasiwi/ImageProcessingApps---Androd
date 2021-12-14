package id.rizki.imgprocessingapps

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.squareup.picasso.Picasso
import id.rizki.imgprocessingapps.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pickImage: ActivityResultLauncher<String>
    private lateinit var takePictures: ActivityResultLauncher<Uri>
    private lateinit var cropimage : ActivityResultLauncher<CropImageContractOptions>

    private var mUri : Uri? = null
    private val permissionCode = 0

    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //request permission
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), permissionCode
        )


        //pick image from gallery launcher
        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.setImageUri(it)
            }
        }

        //take pickture from camera launcher
        takePictures = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                viewModel.setImageUri(mUri)
            }
        }

        //crop image launcher
        cropimage = registerForActivityResult(CropImageContract()){result ->
            if (result.isSuccessful) {
                 viewModel.setImageUri(result.uriContent)
            } else {
                val exception = result.error
                Log.i("MainActivity", "error $exception")
            }
        }

        //initialize click listener
        binding.pickFromGaleryBtn.setOnClickListener(this)
        binding.openCameraBtn.setOnClickListener(this)
        binding.cropBtn.setOnClickListener(this)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if ((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity", "Permission Granted")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        //manage visibility
        viewModel.imageUri.observe(this, { imageUri ->
            Log.i("MainActivity", "image $imageUri")
            if (imageUri != null) {
                binding.imageview.visibility = View.VISIBLE
                binding.cropBtn.visibility = View.VISIBLE
                binding.noImageTxt.visibility = View.GONE
                Picasso.get().load(imageUri).into(binding.imageview)
            } else {
                binding.imageview.visibility = View.GONE
                binding.cropBtn.visibility = View.GONE
                binding.noImageTxt.visibility = View.VISIBLE
            }
        })


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.pick_from_galery_btn -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    pickImage.launch("image/*")
                }
            }
            R.id.open_camera_btn -> {
                imageUri()
                takePictures.launch(mUri)
            }

            R.id.crop_btn -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    cropimage.launch(
                        options(uri = viewModel.imageUri.value) {
                            setGuidelines(CropImageView.Guidelines.ON)
                            setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        }
                    )
                }
            }
        }
    }


    //setting up uri file storage for the camera image
    private fun imageUri() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            val name = "img_" + System.currentTimeMillis() + ".jpg"
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "DCIM/" + "Camera"
            )

            val uri = resolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            mUri = uri
        }else{
            val root = File(
                Environment.getExternalStorageDirectory(),
                BuildConfig.APPLICATION_ID + File.separator
            )

            root.mkdirs()
            val fname = "img_" + System.currentTimeMillis() + ".jpg"
            val sdImageMainDirectory = File(root, fname)
            val uri = FileProvider.getUriForFile(
                this,
                this.applicationContext?.packageName + ".provider",
                sdImageMainDirectory
            )

            mUri = uri
        }
    }

}