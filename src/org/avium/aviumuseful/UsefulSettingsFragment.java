/*
 * Copyright (C) 2025-2026 The AviumUI Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.avium.aviumuseful;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;

import com.android.settingslib.PrimarySwitchPreference;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import org.avium.aviumuseful.service.UsefulService;

import java.util.ArrayList;
import java.util.List;

public class UsefulSettingsFragment extends SettingsBasePreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "UsefulSettingsFragment";

    private static final String PREFS_NAME = "app_settings";

    private static final String KEY_MUSIC_SWITCH = "music_suggestion_switch";
    private static final String KEY_MUSIC_PACKAGE = "music_app_package_name";
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private PrimarySwitchPreference mMusicPref;
    private CharSequence[] mAppEntries;
    private CharSequence[] mAppEntryValues;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.useful_settings);

        initMusicAppPreference();
        checkPermissionsAndStartService();
    }

    private void initMusicAppPreference() {
        mMusicPref = findPreference(KEY_MUSIC_SWITCH);
        if (mMusicPref == null) return;
        loadMusicApps();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        boolean isEnabled = prefs.getBoolean(KEY_MUSIC_SWITCH, false);
        mMusicPref.setChecked(isEnabled);
        if (isEnabled) {
            updateMusicAppSummary();
        } else {
            mMusicPref.setSummary(R.string.switch_music_suggestion_summary_off);
        }
        mMusicPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean enabled = (Boolean) newValue;
            if (enabled) {
                updateMusicAppSummary();
            } else {
                mMusicPref.setSummary(R.string.switch_music_suggestion_summary_off);
            }
            return true;
        });
        mMusicPref.setOnPreferenceClickListener(preference -> {
            showAppSelectionDialog();
            /* Sometimes user select app, but the switch is not on,
               Let's assume that the user want to use this function,
               So set the switch to on.
             */
            return true;
        });
    }

    private void loadMusicApps() {
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entryValues = new ArrayList<>();

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                entries.add(app.loadLabel(pm));
                entryValues.add(app.packageName);
            }
        }

        mAppEntries = entries.toArray(new CharSequence[0]);
        mAppEntryValues = entryValues.toArray(new CharSequence[0]);
    }

    private void showAppSelectionDialog() {
        if (mAppEntries == null || mAppEntries.length == 0) return;
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String currentPackage = prefs.getString(KEY_MUSIC_PACKAGE, getString(R.string.default_music_app));
        int selectedIndex = -1;
        for (int i = 0; i < mAppEntryValues.length; i++) {
            if (TextUtils.equals(currentPackage, mAppEntryValues[i])) {
                selectedIndex = i;
                break;
            }
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.switch_music_suggestion_title)
                .setSingleChoiceItems(mAppEntries, selectedIndex, (dialog, which) -> {
                    String selectedPackage = mAppEntryValues[which].toString();
                    prefs.edit()
                            .putString(KEY_MUSIC_PACKAGE, selectedPackage)
                            .putBoolean(KEY_MUSIC_SWITCH, true)
                            .apply();
                    updateMusicAppSummary();
                    mMusicPref.setChecked(true);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateMusicAppSummary() {
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String packageName = prefs.getString(KEY_MUSIC_PACKAGE, getString(R.string.default_music_app));
        try {
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            String label = info.loadLabel(pm).toString();
            mMusicPref.setSummary(getString(R.string.switch_music_suggestion_summary_on, label));
        } catch (PackageManager.NameNotFoundException e) {
            mMusicPref.setSummary(packageName);
        }
    }

    // Just allow to switch
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    // TODO: Remove check permissions, these should be granted through AndroidManifest.xml.
    private void checkPermissionsAndStartService() {
        Context context = getContext();
        if (context == null) return;

        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        } else {
            startUsefulService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            startUsefulService();
        }
    }

    private void startUsefulService() {
        Context context = getContext();
        if (context != null) {
            Intent serviceIntent = new Intent(context, UsefulService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }
}
