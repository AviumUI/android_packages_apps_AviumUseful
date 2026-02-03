package org.exthm.exthmuseful;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;
import org.exthm.exthmuseful.service.UsefulService;

import java.util.ArrayList;
import java.util.List;

public class UsefulSettingsFragment extends SettingsBasePreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "UsefulSettingsFragment";
    
    private static final String PREFS_NAME = "app_settings";
    
    private static final String KEY_MUSIC_APP = "music_app_suggestion";
    private static final int REQUEST_CODE_PERMISSIONS = 1001;

    private ListPreference mMusicAppPref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.useful_settings);

        initMusicAppPreference();
        checkPermissionsAndStartService();
    }

    private void initMusicAppPreference() {
        mMusicAppPref = findPreference(KEY_MUSIC_APP);
        if (mMusicAppPref != null) {
            loadMusicApps();
            mMusicAppPref.setOnPreferenceChangeListener(this);
            updateMusicAppSummary(mMusicAppPref.getValue());
        }
    }

    private void loadMusicApps() {
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entryValues = new ArrayList<>();

        entries.add(getString(R.string.switch_music_suggestion_summary_off));
        entryValues.add("");

        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                entries.add(app.loadLabel(pm));
                entryValues.add(app.packageName);
            }
        }

        mMusicAppPref.setEntries(entries.toArray(new CharSequence[0]));
        mMusicAppPref.setEntryValues(entryValues.toArray(new CharSequence[0]));
    }

    private void updateMusicAppSummary(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            mMusicAppPref.setSummary(R.string.switch_music_suggestion_summary_off);
        } else {
            try {
                PackageManager pm = getContext().getPackageManager();
                ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
                String label = info.loadLabel(pm).toString();
                mMusicAppPref.setSummary(getString(R.string.switch_music_suggestion_summary_on, label));
            } catch (PackageManager.NameNotFoundException e) {
                mMusicAppPref.setSummary(packageName);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMusicAppPref) {
            updateMusicAppSummary((String) newValue);
            return true;
        }
        return true;
    }

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
