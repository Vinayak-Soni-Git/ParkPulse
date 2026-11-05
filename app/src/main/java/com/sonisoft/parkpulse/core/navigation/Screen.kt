package com.sonisoft.parkpulse.core.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Map : Screen("map")
    data object CreatePark : Screen("create_park")

    data object Survey : Screen("survey/{parkId}") {
        fun createRoute(parkId: String) = "survey/$parkId"
    }

    data object Camera : Screen("camera/{parkId}") {
        fun createRoute(parkId: String) = "camera/$parkId"
    }
}
