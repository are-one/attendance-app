package com.one.attendanceapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.one.attendanceapp.databinding.ActivityMainBinding
import java.lang.Math.pow
import java.lang.Math.toRadians
import java.util.*
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    companion object {
        const val ID_LOCATION_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkPermissionLocation()
        onClick()
    }

    private fun checkPermissionLocation() {
        if(checkPermission()){
            if (!isLocationEnable()){
                Toast.makeText(this, "Please turn on your location.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }else{
            requestPermission()
        }
    }

    private fun checkPermission() : Boolean{
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
            ){
            return true
        }

        return false
    }

    private fun isLocationEnable() : Boolean{
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            return true
        }

        return false
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION),
            ID_LOCATION_PERMISSION
            )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == ID_LOCATION_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Berhasil diizinkan.", Toast.LENGTH_SHORT).show()

                if(!isLocationEnable()){
                    Toast.makeText(this,"Please turn on your location.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }else{
                Toast.makeText(this,"Gagal diizinkan.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onClick() {
        binding.fabCheckIn.setOnClickListener {
            loadScanLocation()
            Handler().postDelayed({
                getLastLocation()
            },10000)
        }
    }

    private fun loadScanLocation(){
        binding.rippleBackground.startRippleAnimation()
        binding.tvScanning.visibility = View.VISIBLE
    }

    private fun stopScanLocation(){
        binding.rippleBackground.stopRippleAnimation()
        binding.tvScanning.visibility = View.GONE
    }

    private fun getLastLocation(){
        if(checkPermission()){

            if(isLocationEnable()){
//                Untuk mendapatkan lokasi terakhir
                LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
                    val currentLatitude = location.latitude
                    val currentLongitude = location.longitude

                    val distance: Double = calculateDistance(
                        currentLatitude,
                        currentLongitude,
                        getAddresses()[0].latitude,
                        getAddresses()[0].longitude)

//                    Konversi Km ke meter
                    val jarak : Double = distance * 1000

                    binding.tvCheckInSuccess.visibility = View.VISIBLE
                    binding.tvCheckInSuccess.text = """
                        Lokasi : lat: $currentLatitude, long: $currentLongitude
                        Tujuan : lat: ${getAddresses()[0].latitude}, long: ${getAddresses()[0].longitude}
                        Jarak : $jarak meter
                        """.trimMargin()


                    stopScanLocation()
                }
            }else{
                Toast.makeText(this, "Please turn on your location.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

        }else{
            requestPermission()
        }
    }

    private fun getAddresses() : List<Address>{
//        val destination = "adam cell kambu"
        val destination = "Fakultas Matematika dan Ilmu Pengetahuan Alam (F-MIPA) UHO"
        val geocode = Geocoder(this, Locale.getDefault())

        return geocode.getFromLocationName(destination, 100)

    }

//    Menghitung jarak dalam lingkaran
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6372.8 // in kilometers

        val radianLat1 = toRadians(lat1)
        val radianLat2 = toRadians(lat2)
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        return 2 * r * asin(sqrt(sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(radianLat1) * cos(radianLat2)))
    }
}