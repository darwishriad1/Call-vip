package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.ui.viewmodel.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactScreen(
    viewModel: ContactsViewModel,
    contactId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isEditMode by remember { mutableStateOf(false) }

    // Form fields
    var realName by remember { mutableStateOf("") }
    var realNumber by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var displayNumber by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("عام") }
    var selectedColor by remember { mutableStateOf(0xFF6200EE.toInt()) }
    var selectedEmoji by remember { mutableStateOf("👤") }
    var isIdentityActive by remember { mutableStateOf(true) }
    var note by remember { mutableStateOf("") }

    // Color list presets (beautiful Material palette)
    val colorPresets = listOf(
        0xFFE91E63.toInt(), // Pink
        0xFF9C27B0.toInt(), // Purple
        0xFF3F51B5.toInt(), // Indigo
        0xFF2196F3.toInt(), // Blue
        0xFF009688.toInt(), // Teal
        0xFF4CAF50.toInt(), // Green
        0xFFFF9800.toInt(), // Orange
        0xFF795548.toInt(), // Brown
        0xFF607D8B.toInt()  // Slate Grey
    )

    // Emoji presets
    val emojiPresets = listOf(
        "👤", "👨", "👩", "👴", "👵", "🧑‍💼", "👨‍💻", "👩‍⚕️",
        "🦊", "🦁", "🐼", "🐻", "🚀", "💡", "🛡️", "🔑", "⭐"
    )

    // Categories list
    val categories = listOf("عام", "عائلة", "أصدقاء", "عمل", "خاص")

    // If edit mode, load contact data
    LaunchedEffect(contactId) {
        if (contactId != null && contactId > 0) {
            isEditMode = true
            val contact = viewModel.contactsState.value.find { it.id == contactId }
            if (contact != null) {
                realName = contact.realName
                realNumber = contact.realNumber
                displayName = contact.displayName
                displayNumber = contact.displayNumber
                selectedCategory = contact.category
                selectedColor = contact.avatarColor
                selectedEmoji = contact.avatarEmoji
                isIdentityActive = contact.isIdentityActive
                note = contact.note ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "تعديل هوية جهة الاتصال" else "هوية بديلة جديدة",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Interactive Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "معاينة هوية العرض البديلة",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = selectedEmoji, fontSize = 38.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (displayName.isEmpty()) "اسم العرض البديل" else displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (displayNumber.isEmpty()) "077000000" else displayNumber,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Real Data Inputs
            Text(
                text = "البيانات الحقيقية (المخفية المشفرة)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = realName,
                onValueChange = { realName = it },
                label = { Text("الاسم الحقيقي بالكامل") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("real_name_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = realNumber,
                onValueChange = { realNumber = it },
                label = { Text("الرقم الحقيقي") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("real_number_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Alternate Display Inputs
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بيانات الهوية البديلة (التي تظهر للجميع)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("اسم العرض البديل (مثال: أحمد)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("display_name_input"),
                singleLine = true
            )

            OutlinedTextField(
                value = displayNumber,
                onValueChange = { displayNumber = it },
                label = { Text("رقم العرض البديل (مثال: 0777123456)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("display_number_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Avatar Creator Details
            Text(
                text = "تخصيص صورة العرض (الألوان والرموز)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Color Preset List
            Text(text = "اختر اللون الأساسي للأيقونة:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(colorPresets) { colorInt ->
                    val isSelected = selectedColor == colorInt
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(colorInt))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = colorInt }
                    )
                }
            }

            // Emoji Preset List
            Text(text = "اختر الرمز التعبيري للوجه:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(emojiPresets) { emoji ->
                    val isSelected = selectedEmoji == emoji
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 22.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Classification / Category Chips
            Text(
                text = "تصنيف جهة الاتصال",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Extra Note input and general toggle
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("ملاحظات إضافية خاصة") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input"),
                maxLines = 3
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "تفعيل الهوية البديلة فوراً",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "إذا تم الإيقاف، ستظهر البيانات الحقيقية فقط.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isIdentityActive,
                    onCheckedChange = { isIdentityActive = it }
                )
            }

            // Save actions
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء")
                }

                Button(
                    onClick = {
                        if (realName.isBlank() || realNumber.isBlank() || displayName.isBlank() || displayNumber.isBlank()) {
                            Toast.makeText(context, "الرجاء تعبئة جميع الحقول الأساسية قبل الحفظ", Toast.LENGTH_SHORT).show()
                        } else {
                            if (isEditMode && contactId != null) {
                                // Update existing
                                val oldContact = viewModel.contactsState.value.find { it.id == contactId }
                                if (oldContact != null) {
                                    val updated = ContactEntity.create(
                                        id = contactId,
                                        realName = realName,
                                        realNumber = realNumber,
                                        displayName = displayName,
                                        displayNumber = displayNumber,
                                        avatarColor = selectedColor,
                                        avatarEmoji = selectedEmoji,
                                        isIdentityActive = isIdentityActive,
                                        category = selectedCategory,
                                        note = note,
                                        createdAt = oldContact.createdAt
                                    )
                                    viewModel.updateContact(updated)
                                    Toast.makeText(context, "تم حفظ التعديلات بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Add new
                                viewModel.addContact(
                                    realName = realName,
                                    realNumber = realNumber,
                                    displayName = displayName,
                                    displayNumber = displayNumber,
                                    avatarColor = selectedColor,
                                    avatarEmoji = selectedEmoji,
                                    isIdentityActive = isIdentityActive,
                                    category = selectedCategory,
                                    note = note
                                )
                                Toast.makeText(context, "تمت إضافة جهة الاتصال بنجاح", Toast.LENGTH_SHORT).show()
                            }
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("save_contact_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditMode) "حفظ التعديلات" else "إضافة جهة اتصال")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
