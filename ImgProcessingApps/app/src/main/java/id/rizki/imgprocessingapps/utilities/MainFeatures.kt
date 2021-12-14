package id.rizki.imgprocessingapps.utilities

import android.app.Activity
import android.content.Intent

interface MainFeatures {
    fun onPickImage():Intent
    fun onTakePictures()
    fun cropImage()
    fun analizeImage()
}

class MainFeaturesImpl:MainFeatures{
    override fun onPickImage():Intent {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        return intent
    }

    override fun onTakePictures() {
    }

    override fun cropImage() {
    }

    override fun analizeImage() {
        TODO("Not yet implemented")
    }

}