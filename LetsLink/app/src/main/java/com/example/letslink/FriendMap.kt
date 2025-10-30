package com.example.letslink.fragments

import android.content.res.Resources
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.letslink.Compass
import com.example.letslink.R
import com.example.letslink.nav.HorizontalCoordinator
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random
import android.content.Intent

class FriendMapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var friendCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_friend_map, container, false)

        // Handle system insets for full-screen experience
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Only apply top inset to back button and zoom buttons, let map fill entire screen
            view.findViewById<ImageButton>(R.id.back_button).setPadding(12, systemBars.top + 12, 12, 12)
            view.findViewById<View>(R.id.zoom_buttons).setPadding(12, systemBars.top + 12, 12, 12)
            insets
        }

        // Back button - properly navigate back to home and update nav bar
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            // Get reference to the activity and call the navigation method
            (activity as? HorizontalCoordinator)?.navigateToHome()
        }

        // Initialize friend card
        friendCard = view.findViewById(R.id.friend_list_card)

        // Apply blur and transparency safely
        val friendBg = view.findViewById<View>(R.id.friend_list_bg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            friendBg.setRenderEffect(
                RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
            )
        }
        friendCard.setCardBackgroundColor(Color.parseColor("#88FFFFFF"))

        // Map fragment (childFragmentManager because this is inside a fragment)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // Zoom buttons
        view.findViewById<ImageButton>(R.id.zoom_in).setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        view.findViewById<ImageButton>(R.id.zoom_out).setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }
        view.findViewById<ImageButton>(R.id.zoom_normal).setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }

        // Open compass
        view.findViewById<Button>(R.id.btn_compass).setOnClickListener {
            startActivity(Intent(requireContext(), Compass::class.java))
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )

            if (!success) {
                Log.e("FriendMap", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("FriendMap", "Can't find style. Error: ", e)
        }

        // Example user location (Johannesburg)
        val userLocation = LatLng(-26.2041, 28.0473)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        // Blue circle for user
        mMap?.addCircle(
            CircleOptions()
                .center(userLocation)
                .radius(10.0)
                .fillColor(Color.BLUE)
                .strokeColor(Color.BLUE)
        )

        // Random friends
        val friends = List(4) {
            LatLng(
                userLocation.latitude + Random.nextDouble(-0.01, 0.01),
                userLocation.longitude + Random.nextDouble(-0.01, 0.01)
            )
        }

        friends.forEach { friend ->
            mMap?.addMarker(
                MarkerOptions()
                    .position(friend)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )
        }
    }
}