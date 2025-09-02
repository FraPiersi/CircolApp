package com.example.circolapp.viewmodel

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrCodeViewModel : ViewModel() {

    private
            try {
                val writer = QRCodeWriter()
                val bitMatrix: BitMatrix = writer.encode(userUid, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                withContext(Dispatchers.Main) {
                    _qrCodeBitmap.value = bmp
                }
            } catch (e: WriterException) {
                Log.e("QrCodeViewModel", "Errore durante la generazione del QR Code", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Errore nella creazione del QR code."
                }
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }
}