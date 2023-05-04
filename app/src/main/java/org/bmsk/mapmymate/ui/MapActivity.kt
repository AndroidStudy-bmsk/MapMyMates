package org.bmsk.mapmymate.ui

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.bmsk.mapmymate.R
import org.bmsk.mapmymate.data.Person
import org.bmsk.mapmymate.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val markerMap = hashMapOf<String, Marker>()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(ACCESS_FINE_LOCATION, false) -> {
                // fine location 권한이 있는 경우
                getCurrentLocation()
            }

            permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                // coarse location 권한이 있는 경우
                getCurrentLocation()
            }

            else -> {
                // TODO 설정으로 보내기 or 교육용 팝업을 띄워서 다시 권한 요청
            }
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // 새로 요청된 위치 정보
            for (location in locationResult.locations) {

                Log.e(
                    "MapActivity",
                    "onLocationResult : ${location.latitude}, ${location.longitude}"
                )

                val uid = Firebase.auth.currentUser?.uid.orEmpty()

                val locationMap = mutableMapOf<String, Any>()
                locationMap["latitude"] = location.latitude
                locationMap["longitude"] = location.longitude
                // TODO Firebase에 내 위치 업로드
                // Person이라는 DB를 만들었으므로 uid라는 객체에 updateChildren() 함수를 호출
                Firebase.database.reference.child(getString(R.string.db_path_person)).child(uid)
                    .updateChildren(locationMap)
                // 지도 마커 움직임
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermission()
        setupFirebaseDatabase()
    }

    override fun onResume() {
        super.onResume()

        getCurrentLocation()
    }

    override fun onPause() {
        super.onPause()

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap.setMaxZoomPreference(20.0f)
        googleMap.setMinZoomPreference(10.0f)

    }

    private fun getCurrentLocation() {
        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 5000)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        // 권한이 있는 상태
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        fusedLocationClient.lastLocation.addOnSuccessListener {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.0f)
            )
        }
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        )
    }

    private fun setupFirebaseDatabase() {
        Firebase.database.reference.child(getString(R.string.db_path_person))
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val person = snapshot.getValue(Person::class.java) ?: return
                    val uid = person.uid ?: return

                    if (markerMap[uid] == null) {
                        markerMap[uid] = makeNewMarker(person, uid) ?: return
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val person = snapshot.getValue(Person::class.java) ?: return
                    val uid = person.uid ?: return

                    if (markerMap[uid] == null) {
                        markerMap[uid] = makeNewMarker(person, uid) ?: return
                    } else {
                        markerMap[uid]?.position =
                            LatLng(person.latitude ?: 0.0, person.longitude ?: 0.0)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun makeNewMarker(person: Person, uid: String): Marker? {
        val marker = googleMap.addMarker(
            MarkerOptions().position(
                LatLng(
                    person.latitude ?: 0.0,
                    person.longitude ?: 0.0
                )
            ).title(person.name.orEmpty())
        ) ?: return null

        return marker
    }
}