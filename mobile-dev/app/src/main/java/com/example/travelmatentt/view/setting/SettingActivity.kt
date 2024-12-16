package com.example.travelmatentt.view.setting

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.travelmatentt.R
import com.example.travelmatentt.data.response.UserResponse
import com.example.travelmatentt.data.retrofit.ApiService
import com.example.travelmatentt.databinding.ActivitySettingBinding
import com.example.travelmatentt.view.infoaccount.InfoAccountActivity
import com.example.travelmatentt.view.login.LoginActivity
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var settingViewModel: SettingViewModel
    private val apiService: ApiService by lazy { createApiService() }

    private val cropImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    openCropper(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        settingViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)

        if (!token.isNullOrEmpty()) {
            fetchUserInfo(token)
        } else {
            binding.tvUsernameProfile.text = getString(R.string.username_not_found)
        }

        displayProfileImage()
        loadThemePreference()
        setupClickListener()
    }



    private fun fetchUserInfo(token: String) {
        showLoading(true)
        apiService.getUser("Bearer $token").enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        val greeting = getString(R.string.hi_rizki, user.username)
                        binding.tvUsernameProfile.text = greeting
                    } else {
                        binding.tvUsernameProfile.text = getString(R.string.username_not_found)
                    }
                } else {
                    showError("Gagal memuat informasi pengguna: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                showLoading(false)
                showError("Error: ${t.localizedMessage}")
            }
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun createApiService(): ApiService {
        val okHttpClient = OkHttpClient.Builder().build()
        return Retrofit.Builder()
            .baseUrl("https://travelmate-ntt-1096623490059.asia-southeast2.run.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun setupClickListener() {
        binding.tvEditProfile.setOnClickListener {
            openImagePicker()
        }

        binding.btnInfoAccount.setOnClickListener {
            val intent = Intent(this, InfoAccountActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }

        binding.btnToggleDarkMode.setOnClickListener {
            toggleDarkMode()
        }
        updateDarkModeToggleState()
    }

    private fun toggleDarkMode() {
        // Check current theme mode
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val newMode = if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Switch to light mode
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            // Switch to dark mode
            AppCompatDelegate.MODE_NIGHT_YES
        }
        // Set the new theme mode
        AppCompatDelegate.setDefaultNightMode(newMode)
        // Save the theme preference to SharedPreferences
        settingViewModel.saveThemePreference(newMode)
        // Recreate activity to apply the theme changes immediately
        recreate()
        // Update toggle state
        updateDarkModeToggleState()
    }
    private fun updateDarkModeToggleState() {
        // Check current mode and update the toggle button state
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        binding.btnToggleDarkMode.isChecked = currentMode == AppCompatDelegate.MODE_NIGHT_YES
    }
    private fun loadThemePreference() {
        // Read the theme preference from ViewModel
        val themeMode = settingViewModel.getThemePreference()
        // Apply the saved theme mode
        AppCompatDelegate.setDefaultNightMode(themeMode)
        // Update toggle state based on saved theme preference
        updateDarkModeToggleState()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        cropImageLauncher.launch(intent)
    }

    private fun openCropper(uri: Uri) {
        // Start the cropping activity using UCrop
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f) // You can change aspect ratio
            .withMaxResultSize(500, 500) // Set max image size
            .start(this)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                showConfirmationDialog(it)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showConfirmationDialog(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Yakin ingin mengubah gambar profil?")
            .setPositiveButton("Ya") { _, _ ->
                // Tampilkan progress bar selama proses update
                binding.progressBar.visibility = View.VISIBLE

                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        // Simulasikan proses update dengan delay
                        delay(2000)

                        // Set gambar baru ke ImageView (real-time update)
                        binding.profileImageView.setImageURI(uri)

                        // Simpan URI gambar baru ke SharedPreferences
                        saveProfileImageUri(uri)

                    } finally {
                        // Sembunyikan progress bar setelah gambar diperbarui
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveProfileImageUri(uri: Uri) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("profile_image_uri", uri.toString())
        editor.apply()

        displayProfileImage()
    }

    private fun displayProfileImage() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        uriString?.let {
            val uri = Uri.parse(it)
            binding.profileImageView.setImageURI(uri)
        }
    }

    private fun logoutUser() {
        settingViewModel.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}