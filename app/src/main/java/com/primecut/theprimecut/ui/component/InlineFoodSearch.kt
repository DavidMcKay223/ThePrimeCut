package com.primecut.theprimecut.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InlineFoodSearch(
    filteredFoodItems: List<com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel.HistoricalFoodItem>,
    recentEntries: List<MealEntry>,
    brands: List<String>,
    groups: List<String>,
    nameQuery: String,
    onNameQueryChanged: (String) -> Unit,
    brandQuery: String,
    onBrandQueryChanged: (String) -> Unit,
    groupQuery: String,
    onGroupQueryChanged: (String) -> Unit,
    selectedFilters: Set<String>,
    onToggleFilter: (String) -> Unit,
    onClearAllFilters: () -> Unit,
    onFoodSelected: (com.primecut.theprimecut.ui.viewmodels.FoodItemViewModel.HistoricalFoodItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    val macroFilters = listOf("High Protein", "Low Carb", "Keto", "Balanced", "Low Fiber")
    val hasActiveFilters = nameQuery.isNotEmpty() || groupQuery.isNotEmpty() || brandQuery.isNotEmpty() || selectedFilters.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 1. Search Bar Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Search, 
                    null, 
                    modifier = Modifier.padding(start = 12.dp).size(20.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
                
                TextField(
                    value = nameQuery,
                    onValueChange = onNameQueryChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search food database...", style = MaterialTheme.typography.bodyMedium) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    trailingIcon = {
                        if (nameQuery.isNotEmpty()) {
                            IconButton(onClick = { onNameQueryChanged("") }) {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, "Dismiss", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            // 2. Filter Controls
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactFilterChip(
                        label = if (groupQuery.isEmpty()) "Categories" else groupQuery,
                        options = groups,
                        onSelected = onGroupQueryChanged,
                        active = groupQuery.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    CompactFilterChip(
                        label = if (brandQuery.isEmpty()) "Brands" else brandQuery,
                        options = brands,
                        onSelected = onBrandQueryChanged,
                        active = brandQuery.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(macroFilters) { filter ->
                            val isSelected = selectedFilters.contains(filter)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onToggleFilter(filter) },
                                label = { Text(filter, fontSize = 10.sp) },
                                shape = CircleShape,
                                modifier = Modifier.height(28.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (hasActiveFilters) {
                        TextButton(
                            onClick = onClearAllFilters,
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.FilterListOff, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Results List
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredFoodItems.isEmpty()) {
                        item { EmptySearchState() }
                    } else {
                        items(filteredFoodItems.take(50)) { historical ->
                            CompactFoodRow(
                                food = historical.item,
                                isHistorical = historical.isHistorical,
                                onClick = { onFoodSelected(historical) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactFoodRow(food: FoodItem, isHistorical: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isHistorical) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp).padding(end = 4.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        food.recipeName, 
                        style = MaterialTheme.typography.bodyMedium, 
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    "${food.brandType} • ${food.totalCalories.toInt()} kcal", 
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroBadge(label = "P", value = food.protein.toInt(), color = macroPink)
                MacroBadge(label = "C", value = food.carbs.toInt(), color = macroBlue)
                MacroBadge(label = "F", value = food.fats.toInt(), color = macroAmber)
                MacroBadge(label = "FB", value = food.fiber.toInt(), color = macroGreen)
            }
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: Int, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun CompactFilterChip(
    label: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    active: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            )
        )
        DropdownMenu(
            expanded = expanded, 
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            DropdownMenuItem(
                text = { Text("All", style = MaterialTheme.typography.bodySmall) }, 
                onClick = { onSelected(""); expanded = false }
            )
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt, style = MaterialTheme.typography.bodySmall) }, 
                    onClick = { onSelected(opt); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.SearchOff, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text("No foods match your filters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}
