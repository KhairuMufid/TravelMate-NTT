package com.example.travelmatentt.Maps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.Maps.DirectionsApiService
import com.example.travelmatentt.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.example.travelmatentt.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnGasKeTujuan.setOnClickListener {
            activateMode("Gas Ke Tujuan")
        }
        binding.btnOffline.setOnClickListener {
            activateMode("Offline")
        }
        binding.btnAturNanti.setOnClickListener {
            showAturNantiDialog()
        }
        binding.btnMyLocation.setOnClickListener {
            if (isLocationPermissionGranted()) {
                getLastKnownLocation()
            } else {
                requestLocationPermission()
            }
        }
        binding.btnZoomIn.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        val defaultLocation = LatLng(-6.4025, 106.7942) // Example: Depok, Margonda
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
        map.addMarker(MarkerOptions().position(defaultLocation).title("Default Location"))
    }

    private fun activateMode(mode: String) {
        when (mode) {
            "Gas Ke Tujuan" -> {
                binding.modeStatus.text = "Mode: Gas Ke Tujuan"
                binding.modeStatus.setBackgroundColor(getColor(R.color.green))


                Toast.makeText(this, "Navigasi dimulai menuju tujuan.", Toast.LENGTH_SHORT).show()
                searchLocation()
            }

            "Offline" -> {
                binding.modeStatus.text = "Mode: Offline"
                binding.modeStatus.setBackgroundColor(getColor(R.color.green))


                Toast.makeText(
                    this,
                    "Sinyal ilang nih, navigasi offline.",
                    Toast.LENGTH_SHORT
                ).show()
                showOfflineMessage()
            }
        }
    }

    private fun showAturNantiDialog() {

        Toast.makeText(this, "Feature Atur Nanti belum diimplementasikan.", Toast.LENGTH_SHORT)
            .show()
    }

    private fun searchLocation() {
        val location = binding.searchInput.text.toString()
        if (location.isNotEmpty()) {
            val geocoder = Geocoder(this)
            try {
                val addressList = geocoder.getFromLocationName(location, 1)
                if (addressList != null && addressList.isNotEmpty()) {
                    val address = addressList[0]
                    val destinationLatLng = LatLng(address.latitude, address.longitude)


                    map.addMarker(MarkerOptions().position(destinationLatLng).title(location))
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15f))


                    drawRouteToLocation(destinationLatLng)
                } else {
                    Toast.makeText(this, "Lokasi tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Kesalahan saat mencari lokasi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Masukkan nama lokasi terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawRouteToLocation(destination: LatLng) {
        if (isLocationPermissionGranted()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val origin = LatLng(location.latitude, location.longitude)


                        val originString = "${location.latitude},${location.longitude}"
                        val destinationString = "${destination.latitude},${destination.longitude}"


                        lifecycleScope.launch {
                            try {
                                val retrofit = Retrofit.Builder()
                                    .baseUrl("https://maps.googleapis.com/maps/api/")
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

                                val service = retrofit.create(DirectionsApiService::class.java)


                                val response = service.getRoute(
                                    originString,
                                    destinationString,
                                    "AIzaSyBxL3ph_3uJoPcDwQ9YWepG2etQh5U1mkk"
                                )


                                if (response.isSuccessful && response.body()?.routes?.isNotEmpty() == true) {
                                    val route = response.body()?.routes?.get(0)
                                    val polylineOptions = PolylineOptions().width(12f).color(android.graphics.Color.BLUE)


                                    route?.legs?.get(0)?.steps?.forEach { step ->
                                        val polyline = decodePolyline(step.polyline.points)
                                        polylineOptions.addAll(polyline)
                                    }

                                    map.addPolyline(polylineOptions)


                                    val bounds = LatLngBounds.builder()
                                        .include(origin)
                                        .include(destination)
                                        .build()
                                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                                } else {
                                    Toast.makeText(
                                        this@MapsActivity,
                                        "",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                            } catch (e: Exception) {

                                Toast.makeText(this@MapsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Tidak dapat mengambil lokasi saat ini.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Kesalahan mendapatkan lokasi: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Kesalahan keamanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Izin lokasi tidak diberikan. Tidak dapat menggambar rute.", Toast.LENGTH_SHORT).show()
            requestLocationPermission()
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val polyline = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dLat = if (result and 1 != 0) {
                (result shr 1)
            } else {
                result shr 1
            }
            lat += dLat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dLng = if (result and 1 != 0) {
                (result shr 1)
            } else {
                result shr 1
            }
            lng += dLng

            val position = LatLng(lat / 1E5, lng / 1E5)
            polyline.add(position)
        }

        return polyline
    }

    private fun getLastKnownLocation() {
        if (isLocationPermissionGranted()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    } else {
                        Toast.makeText(this, "Unable to retrieve current location.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: SecurityException) {
                Toast.makeText(this, "Security exception: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun showOfflineMessage() {
        Toast.makeText(
            this,
            "Navigasi offline ",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLastKnownLocation()
            } else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    Toast.makeText(
                        this,
                        "Location permission is required for this feature. Please allow access.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {

                    Toast.makeText(
                        this,
                        "Permission denied permanently. You can enable it in app settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
