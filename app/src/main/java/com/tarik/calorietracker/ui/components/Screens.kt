package com.tarik.calorietracker.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tarik.calorietracker.ui.ProfileViewModel
import com.tarik.calorietracker.ui.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackerScreen(
    state: TrackerViewModel.TrackerUiState,
    dispatch: (TrackerViewModel.Action) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dispatch(TrackerViewModel.Action.SetDialogVisibility(true))
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Meal")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome, ${state.userProfile?.name ?: "User"}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Daily Goal: ${state.dailyGoal} kcal")

                    Spacer(modifier = Modifier.height(16.dp))

                    val remaining = state.dailyGoal - state.todaysCalories
                    Text(
                        text = "${state.todaysCalories} / ${state.dailyGoal}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$remaining kcal remaining",
                        color = if (remaining < 0) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                "Today's Meals",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val today = System.currentTimeMillis()
            val formatter = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.meals.filter {
                    formatter.format(Date(it.timestamp)) == formatter.format(Date(today))
                }) { meal ->
                    MealItemRow(meal.name, meal.calories, meal.timestamp)
                }
            }
        }
    }

    if (state.isAddDialogVisible) {
        AddMealDialog(state, dispatch)
    }
}

@Composable
fun HistoryScreen(
    state: TrackerViewModel.TrackerUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Meal History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (state.meals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No meals recorded yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.meals) { meal ->
                    MealItemRow(meal.name, meal.calories, meal.timestamp)
                }
            }
        }
    }
}

@Composable
fun MealItemRow(name: String, calories: Int, timestamp: Long) {
    val dateString = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(dateString, style = MaterialTheme.typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "$calories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    state: ProfileViewModel.ProfileUiState,
    dispatch: (ProfileViewModel.Action) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(bottom = 64.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { dispatch(ProfileViewModel.Action.UpdateName(it)) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.age,
            onValueChange = { dispatch(ProfileViewModel.Action.UpdateAge(it)) },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.weight,
            onValueChange = { dispatch(ProfileViewModel.Action.UpdateWeight(it)) },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.height,
            onValueChange = { dispatch(ProfileViewModel.Action.UpdateHeight(it)) },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Gender")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = state.isMale,
                onClick = { dispatch(ProfileViewModel.Action.UpdateGender(true)) })
            Text("Male")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = !state.isMale,
                onClick = { dispatch(ProfileViewModel.Action.UpdateGender(false)) })
            Text("Female")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Dark Theme")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = state.isDarkTheme,
                onCheckedChange = { dispatch(ProfileViewModel.Action.ToggleTheme(it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { dispatch(ProfileViewModel.Action.SaveProfile) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

@Composable
fun AddMealDialog(
    state: TrackerViewModel.TrackerUiState,
    dispatch: (TrackerViewModel.Action) -> Unit
) {
    Dialog(
        onDismissRequest = { dispatch(TrackerViewModel.Action.SetDialogVisibility(false)) },
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (state.selectedFood == null) "Add Meal" else "Adjust Portion",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = {
                        dispatch(
                            TrackerViewModel.Action.SetDialogVisibility(
                                false
                            )
                        )
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (state.selectedFood == null) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { dispatch(TrackerViewModel.Action.UpdateSearchQuery(it)) },
                        label = { Text("Search Food (e.g. 'Apple')") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (state.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.searchResults) { food ->
                            SearchResultItem(food) {
                                dispatch(TrackerViewModel.Action.SelectFood(food))
                            }
                        }
                    }
                } else {
                    PortionCalculator(
                        food = state.selectedFood,
                        onBack = { dispatch(TrackerViewModel.Action.SelectFood(null)) },
                        onAdd = { portion ->
                            dispatch(
                                TrackerViewModel.Action.AddMeal(
                                    state.selectedFood.name,
                                    state.selectedFood.calories,
                                    portion
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    food: TrackerViewModel.FoodSearchResult,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge)
                Text(food.source, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("${food.calories} kcal", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PortionCalculator(
    food: TrackerViewModel.FoodSearchResult,
    onBack: () -> Unit,
    onAdd: (Float) -> Unit
) {
    var portionText by remember { mutableStateOf("1.0") }
    val portion = portionText.toFloatOrNull() ?: 0f
    val totalCalories = (food.calories * portion).toInt()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(food.name, style = MaterialTheme.typography.titleLarge)
        Text(
            "Base: ${food.calories} kcal / serving (or 100g)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$totalCalories",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                " kcal",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = portionText,
            onValueChange = { portionText = it },
            label = { Text("Portion Size (e.g. 1.5)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Enter 1 for a single serving or 100g",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = { onAdd(portion) },
                modifier = Modifier.weight(1f),
                enabled = portion > 0
            ) {
                Text("Add Meal")
            }
        }
    }
}

@Composable
fun ContactScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Contact Us", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:1111111")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Call Us")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val query = Uri.encode("Softwarepark Hagenberg, Linz")
                val uri = Uri.parse("geo:0,0?q=$query")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val webIntent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(webIntent)
                }
            },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.Place, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Find Us on Maps")
        }
    }
}