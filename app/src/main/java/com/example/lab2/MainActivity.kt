package com.example.lab2

// Импорты нужных Android-классов
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    // Объявление элементов интерфейса и переменных
    private lateinit var imageViewPhoto: ImageView   // Поле для отображения фото
    private lateinit var btnTakePhoto: Button        // Кнопка "Сделать фото"
    private lateinit var btnSendEmail: Button        // Кнопка "Отправить письмо"

    private var takenPhotoBitmap: Bitmap? = null     // Хранит сделанное фото
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Void?> // Запуск камеры

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Подключаем разметку

        // Привязываем UI элементы к переменным
        imageViewPhoto = findViewById(R.id.imageViewPhoto)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnSendEmail = findViewById(R.id.btnSendEmail)

        // Регистрируем обработчик камеры
        takePhotoLauncher = registerForActivityResult(TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                takenPhotoBitmap = bitmap // Сохраняем фото
                imageViewPhoto.setImageBitmap(bitmap) // Показываем фото в приложении
            } else {
                Log.e("MainActivity", "Фото не получено!") // В лог, если фото нет
            }
        }

        // При нажатии на кнопку — запускаем камеру
        btnTakePhoto.setOnClickListener {
            takePhotoLauncher.launch(null)
        }

        // При нажатии на кнопку — отправляем email, если фото есть
        btnSendEmail.setOnClickListener {
            if (takenPhotoBitmap != null) {
                sendEmailWithGmail(takenPhotoBitmap!!)
            } else {
                Log.e("MainActivity", "Нет фото для отправки!")
            }
        }
    }

    /**
     * Метод отправляет email с вложением изображения через Gmail
     */
    private fun sendEmailWithGmail(bitmap: Bitmap) {
        val imageUri: Uri? = getImageUri(bitmap) // Преобразуем фото в Uri

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg" // Тип вложения
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hodovychenko@op.edu.ua")) // Кому
            putExtra(Intent.EXTRA_SUBJECT, "ANDROID Kara  Nikita AI-221")     // Тема
            putExtra(
                Intent.EXTRA_TEXT,
                "Вітаю!\n\nУ вкладенні моє селфі.\n\nПосилання на репозиторій проекту: "
            ) // Текст письма
            if (imageUri != null) {
                putExtra(Intent.EXTRA_STREAM, imageUri) // Вложение
            }
            `package` = "com.google.android.gm" // Открыть письмо через Gmail
        }

        try {
            startActivity(emailIntent) // Открываем Gmail
        } catch (e: ActivityNotFoundException) {
            // Если Gmail не установлен — сообщаем пользователю
            Toast.makeText(this, "Gmail не встановлено", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Преобразует фото (Bitmap) в Uri — чтобы можно было приложить его к письму
     */
    private fun getImageUri(bitmap: Bitmap): Uri? {
        return try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes) // Сжатие фото
            val path = MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "selfie_" + System.currentTimeMillis(), // Имя изображения
                null
            )
            Uri.parse(path) // Получаем Uri для вложения
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
