package com.example.letslink.nav

import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.View
import com.example.letslink.*
import com.example.letslink.fragments.AccountFragment
import com.example.letslink.fragments.EventsFragment
import com.example.letslink.fragments.FriendMapFragment
import com.example.letslink.fragments.GroupsFragment
import com.example.letslink.fragments.HomeFragment
import com.example.letslink.fragments.SettingsFragment
import com.example.letslink.fragments.CreateGroupFragment
import com.example.letslink.CreateCustomEventFragment // ⭐️ NEW IMPORT FOR CUSTOM EVENT

class HorizontalCoordinator : AppCompatActivity() {

    private lateinit var navButtons: List<ImageButton>
    private lateinit var bottomNavBar: View
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horizontal_coordinator)

        bottomNavBar = findViewById(R.id.bottomNavBar)

        // Make sure navbar is always visible from the start
        bottomNavBar.visibility = View.VISIBLE

        // Collect nav buttons (6 total, Home added at index 0)
        navButtons = listOf(
            findViewById(R.id.nav_home),      // 0 - Home
            findViewById(R.id.nav_groups),    // 1 - Groups ⭐️ TARGET FOR DOUBLE-TAP LOGIC
            findViewById(R.id.nav_map),       // 2 - Map
            findViewById(R.id.nav_events),    // 3 - Events ⭐️ TARGET FOR DOUBLE-TAP LOGIC
            findViewById(R.id.nav_account),   // 4 - Account
            findViewById(R.id.nav_settings)   // 5 - Settings
        )

        // Determine which fragment to load (default = home)
        val fragmentToShow = intent.getStringExtra("showFragment") ?: "home"
        currentIndex = when (fragmentToShow) {
            "map" -> 2
            "groups" -> 1
            "events" -> 3
            "account" -> 4
            "settings" -> 5
            else -> 0 // home
        }

        val fragment = when (fragmentToShow) {
            "map" -> FriendMapFragment()
            "groups" -> GroupsFragment() // Ensure initial load is Groups list
            "events" -> EventsFragment()
            "account" -> AccountFragment()
            "settings" -> SettingsFragment()
            else -> HomeFragment()
        }

        // Initialize groups fragment load if we start on it
        val loadGroupsWithBackStack = if (fragmentToShow == "groups") false else false // Don't add initial load to back stack

        loadFragment(fragment, loadGroupsWithBackStack)


        // Set initial nav bar state (shrunk if on map, normal otherwise)
        setNavBarState(isMapVisible = currentIndex == 2)

        // Initialize all buttons to unselected state first
        navButtons.forEachIndexed { index, button ->
            setButtonUnselected(button, index)
        }

        // Then set the current one as selected
        setButtonSelected(navButtons[currentIndex], currentIndex)

        // Click listeners
        navButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                when (index) {
                    0 -> {
                        loadFragment(HomeFragment())
                        updateNavSelection(0)
                    }
                    1 -> {
                        // ⭐️ GROUPS DOUBLE-TAP LOGIC ⭐️
                        if (currentIndex == 1) {
                            // If currently on the Groups tab (index 1), the second press opens Create Group
                            loadFragment(CreateGroupFragment(), true) // Add to back stack for back navigation
                            // Do NOT call updateNavSelection(1). The index remains 1, keeping the '+ Group' icon.
                        } else {
                            // First press (or switching from another tab) opens the main Groups list
                            loadFragment(GroupsFragment())
                            updateNavSelection(1)
                        }
                    }
                    2 -> {
                        loadFragment(FriendMapFragment())
                        updateNavSelection(2)
                    }
                    3 -> {
                        // ⭐️ EVENTS DOUBLE-TAP LOGIC ⭐️
                        if (currentIndex == 3) {
                            // If currently on the Events tab (index 3), the second press opens Create Custom Event
                            loadFragment(CreateCustomEventFragment(), true) // Add to back stack for back navigation
                            // Do NOT call updateNavSelection(3). The index remains 3, keeping the '+ Event' icon.
                        } else {
                            // First press (or switching from another tab) opens the main Events list
                            loadFragment(EventsFragment())
                            updateNavSelection(3)
                        }
                    }
                    4 -> {
                        loadFragment(AccountFragment())
                        updateNavSelection(4)
                    }
                    5 -> {
                        loadFragment(SettingsFragment())
                        updateNavSelection(5)
                    }
                }
            }
        }
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment, addToBackStack: Boolean = false) { // ⭐️ UPDATED SIGNATURE
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null) // Allows the back arrow in the fragment to pop this transaction
        }

        transaction.commit()
    }

    // Public method for fragments to call when navigating back
    fun navigateToHome() {
        loadFragment(HomeFragment())

        // Deselect current button (map button)
        if (currentIndex < navButtons.size) {
            setButtonUnselected(navButtons[currentIndex], currentIndex)
        }

        // Set home as selected
        setButtonSelected(navButtons[0], 0)

        // Update current index
        currentIndex = 0

        // Animate nav bar back to full size and opacity
        setNavBarState(isMapVisible = false)
    }

    private fun updateNavSelection(newIndex: Int) {
        // Only update if it's actually different
        if (currentIndex != newIndex) {
            // Deselect old button
            setButtonUnselected(navButtons[currentIndex], currentIndex)

            // Select new button
            setButtonSelected(navButtons[newIndex], newIndex)

            // Update current index
            currentIndex = newIndex
        }

        // Animate nav bar size and transparency based on selection
        setNavBarState(isMapVisible = currentIndex == 2)
    }

    private fun setNavBarState(isMapVisible: Boolean) {
        // NEVER hide the navbar - always keep it visible
        bottomNavBar.visibility = View.VISIBLE

        if (isMapVisible) {
            // Shrink navbar to half size, move to very bottom (no margin), and keep fully opaque
            bottomNavBar.animate()
                .scaleY(0.5f)
                .translationY(35f)  // Move down by half its height to stick to bottom edge
                .alpha(1.0f)  // Keep fully opaque as per user preference
                .setDuration(300)
                .start()

            // Scale icons back up to maintain their aspect ratio
            navButtons.forEach { button ->
                button.animate()
                    .scaleY(2.0f)  // Compensate for the navbar's 0.5f scale
                    .alpha(1.0f)   // Keep icons fully opaque
                    .setDuration(300)
                    .start()
            }
        } else {
            // Full size, normal position, and opaque for other fragments
            bottomNavBar.animate()
                .scaleY(1.0f)
                .translationY(0f)  // Return to normal position
                .alpha(1.0f)  // Keep fully opaque
                .setDuration(300)
                .start()

            // Return icons to normal scale and full opacity
            navButtons.forEach { button ->
                button.animate()
                    .scaleY(1.0f)
                    .alpha(1.0f)  // Keep icons fully opaque
                    .setDuration(300)
                    .start()
            }
        }
    }

    private fun setButtonSelected(button: ImageButton, index: Int) {
        // Set orange background
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
        // Set midnight (black) icon color
        button.setColorFilter(ContextCompat.getColor(this, R.color.midnight))

        // Set appropriate icon for selected state
        when (index) {
            1 -> button.setImageResource(R.drawable.add_group)
            3 -> button.setImageResource(R.drawable.add_event)
            // Other buttons keep their default icons
        }

        // Scale animation for selection - preserve Y scale if we're on map
        val targetScaleY = if (currentIndex == 2) 2.2f else 1.1f  // Slightly larger for selection

        button.animate()
            .scaleX(1.1f)
            .scaleY(targetScaleY)
            .alpha(1.0f)  // Always full opacity
            .setDuration(200)
            .start()
    }

    private fun setButtonUnselected(button: ImageButton, index: Int) {
        // Set transparent background (or midnight if you prefer)
        button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        // Set orange icon color
        button.setColorFilter(ContextCompat.getColor(this, R.color.orange))

        // Set appropriate icon for unselected state
        when (index) {
            1 -> button.setImageResource(R.drawable.ic_groups)
            3 -> button.setImageResource(R.drawable.eventsicon)
            // Other buttons keep their default icons
        }

        // Scale back to normal - preserve Y scale if we're on map
        val targetScaleY = if (currentIndex == 2) 2.0f else 1.0f

        button.animate()
            .scaleX(1.0f)
            .scaleY(targetScaleY)
            .alpha(1.0f)  // Always full opacity
            .setDuration(200)
            .start()
    }

    // Method to get current fragment type (useful for fragments to know their state)
    fun getCurrentFragmentIndex(): Int = currentIndex
}
