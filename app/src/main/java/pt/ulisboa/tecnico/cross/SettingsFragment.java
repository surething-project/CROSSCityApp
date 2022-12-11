package pt.ulisboa.tecnico.cross;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    setPreferencesFromResource(R.xml.settings, rootKey);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_settings)
        .setVisible(false);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_settings)
        .setVisible(true);
  }
}
