/*
 *
 * Copyright (C) 2025 The AviumUI Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.exthm.exthmuseful.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.exthm.exthmuseful.R
import org.exthm.exthmuseful.ui.screen.SettingItem

data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?
)

private const val PREF_MUSIC_SUGGESTION_ENABLED = "music_suggestion"
private const val PREF_MUSIC_APP_PACKAGE = "music_app_suggestion"

@Composable
fun MusicAppPreferenceItem(
    modifier: Modifier = Modifier,
    sharedPreferences: SharedPreferences,
    title: String,
    initialEnabledState: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isEnabled by remember { mutableStateOf(initialEnabledState) }
    var selectedMusicAppPackage by remember {
        mutableStateOf(sharedPreferences.getString(PREF_MUSIC_APP_PACKAGE, null))
    }

    var showAppSelectionDialog by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(false) }

    val selectedMusicAppName = remember(selectedMusicAppPackage, installedApps) {
        selectedMusicAppPackage?.let { packageName ->
            installedApps.find { it.packageName == packageName }?.appName
                ?: try {
                    val pm = context.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    sharedPreferences.edit().remove(PREF_MUSIC_APP_PACKAGE).apply()
                    selectedMusicAppPackage = null
                    null
                }
        }
    }

    (selectedMusicAppName
        ?: stringResource(R.string.switch_music_suggestion_desc_placeholder)).apply {

        SettingItem(
            modifier = modifier,
            title = title,
            description = this,
            isChecked = isEnabled,
            onCheckedChange = { newCheckedState ->
                isEnabled = newCheckedState
                sharedPreferences.edit().putBoolean(PREF_MUSIC_SUGGESTION_ENABLED, newCheckedState).apply()
            },
            onClick = {
                if (!isLoadingApps) {
                    isLoadingApps = true
                    coroutineScope.launch {
                        val apps = getInstalledAppsForMusic(context)
                        installedApps = apps
                        showAppSelectionDialog = true
                        isLoadingApps = false
                    }
                }
            }
        )
    }

    if (showAppSelectionDialog) {
        AppSelectionDialogInternal(
            installedApps = installedApps,
            currentSelectedPackage = selectedMusicAppPackage,
            onAppSelected = { appInfo ->
                selectedMusicAppPackage = appInfo.packageName
                sharedPreferences.edit().putString(PREF_MUSIC_APP_PACKAGE, appInfo.packageName).apply()
                showAppSelectionDialog = false
            },
            onDismissRequest = {
                showAppSelectionDialog = false
            }
        )
    }

    if (isLoadingApps) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = false, onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

private suspend fun getInstalledAppsForMusic(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
    val appsList = mutableListOf<AppInfo>()
    for (app in packages) {
        if (pm.getLaunchIntentForPackage(app.packageName) != null) {
            appsList.add(
                AppInfo(
                    appName = pm.getApplicationLabel(app).toString(),
                    packageName = app.packageName,
                    icon = try { pm.getApplicationIcon(app.packageName) } catch (e: PackageManager.NameNotFoundException) { null }
                )
            )
        }
    }
    return@withContext appsList.sortedBy { it.appName.lowercase() }
}

@Composable
private fun AppSelectionDialogInternal(
    installedApps: List<AppInfo>,
    currentSelectedPackage: String?,
    onAppSelected: (AppInfo) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.dialog_select_music_app_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (installedApps.isEmpty()) {
                    Text(stringResource(R.string.dialog_no_apps_found))
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(installedApps) { app ->
                            AppListItemInternal(
                                appInfo = app,
                                isSelected = app.packageName == currentSelectedPackage,
                                onClick = { onAppSelected(app) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        }
    }
}

@Composable
private fun AppListItemInternal(appInfo: AppInfo, isSelected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        appInfo.icon?.let { iconDrawable ->
            val bitmap = remember(iconDrawable) {
                iconDrawable.toBitmap(width = dpToPxInternal(40, context), height = dpToPxInternal(40, context)).asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = appInfo.appName,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        } ?: Spacer(modifier = Modifier.size(40.dp))

        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = appInfo.appName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(R.string.desc_selected_app),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun dpToPxInternal(dp: Int, context: Context): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}

