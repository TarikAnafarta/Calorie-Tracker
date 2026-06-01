package com.tarik.calorietracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

interface Routable {
    val route: String
    val label: String
    val icon: ImageVector
}

object Tracker : Routable {
    override val route: String = "main/tracker"
    override val label: String = "Tracker"
    override val icon: ImageVector = Icons.Default.Home
}

object History : Routable {
    override val route: String = "main/history"
    override val label: String = "History"
    override val icon: ImageVector = Icons.Default.History
}

object Profile : Routable {
    override val route: String = "main/profile"
    override val label: String = "Profile"
    override val icon: ImageVector = Icons.Default.Person
}

object ContactUs : Routable {
    override val route: String = "main/contact"
    override val label: String = "Contact"
    override val icon: ImageVector = Icons.Default.Call
}