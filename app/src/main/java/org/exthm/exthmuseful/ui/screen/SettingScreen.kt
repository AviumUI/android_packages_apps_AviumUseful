package org.exthm.exthmuseful.ui.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.exthm.exthmuseful.R
import org.exthm.exthmuseful.ui.dialog.MusicAppPreferenceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPref = remember {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    var isScreenOn by remember { mutableStateOf(sharedPref.getBoolean("screen_always_on", false)) }
    var isTorchSuggestion by remember { mutableStateOf(sharedPref.getBoolean("torch_suggestion", false)) }

    var isurlSuggestion by remember { mutableStateOf(sharedPref.getBoolean("url_suggestion", false)) }
    var isurlShareSuggestion by remember { mutableStateOf(sharedPref.getBoolean("url_share_suggestion", false)) }
    var isDeliverySuggestion by remember { mutableStateOf(sharedPref.getBoolean("delivery_suggestion", false)) }


    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_name),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(id = R.drawable.screen_page),
                contentDescription = null,
                modifier = Modifier
                    .size(320.dp)
                    .padding(16.dp)
            )
            Text(
                text = stringResource(R.string.main_hint),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = stringResource(R.string.main_ps),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            SettingItem(
                title = stringResource(R.string.switch_screen_always_on_title),
                description = stringResource(R.string.switch_screen_always_on_desc),
                isChecked = isScreenOn,
                onCheckedChange = { newState ->
                    isScreenOn = newState
                    sharedPref.edit().putBoolean("screen_always_on", newState).apply()
                },
                onClick = {
                    val newCheckedState = !isScreenOn
                    isScreenOn = newCheckedState
                    sharedPref.edit().putBoolean("screen_always_on", newCheckedState).apply()
                }
            )

            SettingItem(
                title = stringResource(R.string.switch_torch_suggestion_title),
                description = stringResource(R.string.switch_torch_suggestion_desc),
                isChecked = isTorchSuggestion,
                onCheckedChange = { newState ->
                    isTorchSuggestion = newState
                    sharedPref.edit().putBoolean("torch_suggestion", newState).apply()
                },
                onClick = {
                    val newCheckedState = !isTorchSuggestion
                    isTorchSuggestion = newCheckedState
                    sharedPref.edit().putBoolean("torch_suggestion", newCheckedState).apply()
                }
            )

            MusicAppPreferenceItem(
                sharedPreferences = sharedPref,
                title = stringResource(R.string.switch_music_suggestion_title),
                initialEnabledState = sharedPref.getBoolean("music_suggestion", false),
            )

            SettingItem(
                title = stringResource(R.string.switch_url_suggestion_title),
                description = stringResource(R.string.switch_url_suggestion_desc),
                isChecked = isurlSuggestion,
                onCheckedChange = { newState ->
                    isurlSuggestion = newState
                    sharedPref.edit().putBoolean("url_suggestion", newState).apply()
                },
                onClick = {
                    val newCheckedState = !isurlSuggestion
                    isurlSuggestion = newCheckedState
                    sharedPref.edit().putBoolean("url_suggestion", newCheckedState).apply()
                }
            )

            SettingItem(
                title = stringResource(R.string.switch_url_share_title),
                description = stringResource(R.string.switch_url_share_desc),
                isChecked = isurlShareSuggestion,
                onCheckedChange = { newState ->
                    isurlShareSuggestion = newState
                    sharedPref.edit().putBoolean("url_share_suggestion", newState).apply()
                },
                onClick = {
                    val newCheckedState = !isurlShareSuggestion
                    isurlShareSuggestion = newCheckedState
                    sharedPref.edit().putBoolean("url_share_suggestion", newCheckedState).apply()
                }
            )

            SettingItem(
                title = stringResource(R.string.switch_delivery_title),
                description = stringResource(R.string.switch_delivery_desc),
                isChecked = isDeliverySuggestion,
                onCheckedChange = { newState ->
                    isDeliverySuggestion = newState
                    sharedPref.edit().putBoolean("delivery_suggestion", newState).apply()
                },
                onClick = {
                    val newCheckedState = !isDeliverySuggestion
                    isDeliverySuggestion = newCheckedState
                    sharedPref.edit().putBoolean("delivery_suggestion", newCheckedState).apply()
                }
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}