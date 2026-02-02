package com.simats.dealhive.ui.screens.admin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.simats.dealhive.LocalIsDarkMode
import com.simats.dealhive.data.api.RetrofitClient
import com.simats.dealhive.data.model.CategoryDto
import com.simats.dealhive.ui.components.DealHeader
import com.simats.dealhive.ui.theme.Primary
import com.simats.dealhive.ui.theme.Spacing
import com.simats.dealhive.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun AdminCategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddCategory: () -> Unit,
    onNavigateToEditCategory: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var categoryToDelete by remember { mutableStateOf<CategoryDto?>(null) }
    
    fun loadCategories() {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.phpApiService.getCategories()
                categories = response.categories
            } catch (e: Exception) {
                Log.e("AdminCategories", "Error loading categories", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    fun deleteCategory(category: CategoryDto) {
        coroutineScope.launch {
            try {
                RetrofitClient.phpApiService.deleteCategory(category.id)
                Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                loadCategories()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadCategories()
    }
    
    // Delete Confirmation Dialog
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = surfaceColor,
            title = { Text("Delete Category?", color = textColor) },
            text = { 
                Text(
                    "Are you sure you want to delete \"${categoryToDelete?.name}\"? This action cannot be undone.",
                    color = textSecondaryColor
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    categoryToDelete?.let { deleteCategory(it) }
                    categoryToDelete = null
                }) {
                    Text("Delete", color = errorColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel", color = textSecondaryColor)
                }
            }
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DealHeader(
                title = "Manage Categories",
                showBack = true,
                onBackClick = onNavigateBack
            )
            
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                
                categories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = textMutedColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.Companion.height(Spacing.md))
                            Text("No categories yet", color = textMutedColor)
                            Text("Tap + to add your first category", color = textSecondaryColor, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(categories) { category ->
                            CategoryListItem(
                                category = category,
                                onEdit = { onNavigateToEditCategory(category.id) },
                                onDelete = { categoryToDelete = category }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = onNavigateToAddCategory,
            containerColor = Primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Spacing.xl, bottom = 100.dp)
        ) {
            Icon(Icons.Default.Add, "Add Category", tint = White)
        }
    }
}

@Composable
private fun CategoryListItem(
    category: CategoryDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurface
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMutedColor = MaterialTheme.colorScheme.outline
    val errorColor = MaterialTheme.colorScheme.error

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(Spacing.md)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(surfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                if (category.image.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    AsyncImage(
                        model = category.image,
                        contentDescription = category.name,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            
            Spacer(modifier = Modifier.Companion.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "/${category.slug}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textMutedColor
                )
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = Primary)
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = errorColor)
            }
        }
    }
}
