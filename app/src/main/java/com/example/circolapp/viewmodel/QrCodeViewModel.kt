package com.example.circolapp.viewmodel // o il tuo package per i ViewModel

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

    private val _qrCodeBitmap = MutableLiveData<Bitmap?>()
    val qrCodeBitmap: LiveData<Bitmap?> get() = _qrCodeBitmap

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val auth = FirebaseAuth.getInstance()

    fun generateUserQrCode() {
        _isLoading.value = true
        _errorMessage.value = null
        _qrCodeBitmap.value = null // Resetta il bitmap precedente

        val userUid = auth.currentUser?.uid

        if (userUid.isNullOrBlank()) {
            _errorMessage.value = "Utente non autenticato. Impossibile generare il QR code."
            _isLoading.value = false
            return
        }

        viewModelScope.launch(Dispatchers.Default) { // Esegui su un thread in background
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