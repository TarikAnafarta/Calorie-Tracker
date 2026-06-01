package com.tarik.calorietracker.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tarik.calorietracker.App
import com.tarik.calorietracker.data.UserPreferences
import com.tarik.calorietracker.data.UserProfile
import com.tarik.calorietracker.database.MealEntity
import com.tarik.calorietracker.network.LocalFoodItem
import com.tarik.calorietracker.network.OpenFoodFactsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import java.util.Calendar

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.userData.collect { profile ->
                _uiState.update {
                    it.copy(
                        name = profile.name,
                        age = profile.age.toString(),
                        weight = profile.weight.toString(),
                        height = profile.height.toString(),
                        isMale = profile.isMale,
                        isDarkTheme = profile.isDarkTheme
                    )
                }
            }
        }
    }

    data class ProfileUiState(
        val name: String = "",
        val age: String = "",
        val weight: String = "",
        val height: String = "",
        val isMale: Boolean = true,
        val isDarkTheme: Boolean = false
    )

    sealed interface Action {
        data class UpdateName(val value: String) : Action
        data class UpdateAge(val value: String) : Action
        data class UpdateWeight(val value: String) : Action
        data class UpdateHeight(val value: String) : Action
        data class UpdateGender(val isMale: Boolean) : Action
        data class ToggleTheme(val isDark: Boolean) : Action
        object SaveProfile : Action
    }

    fun dispatch(action: Action) {
        when (action) {
            is Action.UpdateName -> _uiState.update { it.copy(name = action.value) }
            is Action.UpdateAge -> _uiState.update { it.copy(age = action.value) }
            is Action.UpdateWeight -> _uiState.update { it.copy(weight = action.value) }
            is Action.UpdateHeight -> _uiState.update { it.copy(height = action.value) }
            is Action.UpdateGender -> _uiState.update { it.copy(isMale = action.isMale) }
            is Action.ToggleTheme -> viewModelScope.launch { userPreferences.setDarkTheme(action.isDark) }
            Action.SaveProfile -> saveToPrefs()
        }
    }

    private fun saveToPrefs() {
        viewModelScope.launch {
            val s = _uiState.value
            val profile = UserProfile(
                name = s.name,
                age = s.age.toIntOrNull() ?: 25,
                weight = s.weight.toFloatOrNull() ?: 70f,
                height = s.height.toIntOrNull() ?: 175,
                isMale = s.isMale,
                isDarkTheme = s.isDarkTheme
            )
            userPreferences.saveProfile(profile)
            Toast.makeText(getApplication(), "Profile Saved", Toast.LENGTH_SHORT).show()
        }
    }
}

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = (application as App).database
    private val userPreferences = UserPreferences(application)
    private val client = OkHttpClient()
    private val jsonMapper = Json { ignoreUnknownKeys = true }

    // List to hold the loaded JSON data
    private var commonFoods: List<LocalFoodItem> = emptyList()

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadMeals()
        loadCommonFoods() // Load JSON on init
        viewModelScope.launch {
            userPreferences.userData.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
                calculateDailyGoal(profile)
            }
        }
    }

    // Unified model for search results (API + Local)
    data class FoodSearchResult(
        val name: String,
        val calories: Int, // per serving or per 100g
        val source: String // "Local" or "API"
    )

    data class TrackerUiState(
        val meals: List<MealEntity> = emptyList(),
        val todaysCalories: Int = 0,
        val dailyGoal: Int = 2000,
        val userProfile: UserProfile? = null,
        val isAddDialogVisible: Boolean = false,
        val searchQuery: String = "",
        val searchResults: List<FoodSearchResult> = emptyList(),
        val isLoading: Boolean = false,
        val selectedFood: FoodSearchResult? = null // For the portion dialog
    )

    sealed interface Action {
        object LoadMeals : Action
        data class SearchFood(val query: String) : Action
        data class SelectFood(val food: FoodSearchResult?) : Action
        data class AddMeal(val name: String, val baseCalories: Int, val portion: Float) : Action
        data class SetDialogVisibility(val isVisible: Boolean) : Action
        data class UpdateSearchQuery(val query: String) : Action
    }

    fun dispatch(action: Action) {
        when (action) {
            Action.LoadMeals -> loadMeals()
            is Action.SearchFood -> performSearch(action.query)
            is Action.SelectFood -> _uiState.update { it.copy(selectedFood = action.food) }
            is Action.AddMeal -> addMeal(action.name, action.baseCalories, action.portion)
            is Action.SetDialogVisibility -> _uiState.update {
                it.copy(
                    isAddDialogVisible = action.isVisible,
                    searchQuery = "",
                    searchResults = emptyList(),
                    selectedFood = null
                )
            }

            is Action.UpdateSearchQuery -> {
                _uiState.update { it.copy(searchQuery = action.query) }
                performSearch(action.query) // Auto-search on type
            }
        }
    }

    private fun loadCommonFoods() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Read from assets
                val jsonString = getApplication<Application>().assets
                    .open("Common_Food_Items.json")
                    .bufferedReader()
                    .use { it.readText() }

                commonFoods = jsonMapper.decodeFromString(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(500) // Debounce for typing

            // 1. Search Local Data
            val localMatches = withContext(Dispatchers.Default) {
                commonFoods.filter { food ->
                    food.name.contains(query, ignoreCase = true) ||
                            food.aliases.any { it.contains(query, ignoreCase = true) }
                }.map {
                    FoodSearchResult(it.name, it.calories, "Common Food")
                }
            }

            _uiState.update { it.copy(searchResults = localMatches) }

            // 2. Search API (OpenFoodFacts)
            try {
                val url =
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=$query&search_simple=1&action=process&json=1&page_size=5"
                val request = Request.Builder().url(url).build()

                val apiResults = withContext(Dispatchers.IO) {
                    client.newCall(request).executeAsync().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body.string()
                            val offResponse =
                                jsonMapper.decodeFromString<OpenFoodFactsResponse>(body)
                            offResponse.products.filter { !it.productName.isNullOrBlank() }
                                .map { item ->
                                    FoodSearchResult(
                                        name = item.productName ?: "Unknown",
                                        calories = item.nutriments?.energyKcal100g?.toInt() ?: 0,
                                        source = "Database (100g)"
                                    )
                                }
                        } else emptyList()
                    }
                }

                // Combine results
                _uiState.update {
                    it.copy(
                        searchResults = localMatches + apiResults,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateDailyGoal(profile: UserProfile) {
        val bmr = if (profile.isMale) {
            (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) + 5
        } else {
            (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) - 161
        }
        val goal = (bmr * 1.2).toInt()
        _uiState.update { it.copy(dailyGoal = goal) }
    }

    private fun loadMeals() {
        viewModelScope.launch(Dispatchers.IO) {
            val allMeals = database.getMealDao().getAllMeals()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            val todaysMeals = allMeals.filter { it.timestamp >= startOfDay }
            val totalCal = todaysMeals.sumOf { it.calories }

            _uiState.update { it.copy(meals = allMeals, todaysCalories = totalCal) }
        }
    }

    private fun addMeal(name: String, baseCalories: Int, portion: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val finalCalories = (baseCalories * portion).toInt()
            database.getMealDao().insertMeal(
                MealEntity(
                    name = name,
                    calories = finalCalories,
                    timestamp = System.currentTimeMillis()
                )
            )
            loadMeals()
            _uiState.update {
                it.copy(
                    isAddDialogVisible = false,
                    selectedFood = null,
                    searchQuery = ""
                )
            }
        }
    }
}