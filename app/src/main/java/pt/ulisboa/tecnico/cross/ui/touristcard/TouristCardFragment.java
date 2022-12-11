package pt.ulisboa.tecnico.cross.ui.touristcard;

import static com.google.zxing.BarcodeFormat.QR_CODE;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Base64;

import pt.ulisboa.tecnico.cross.CROSSCityApp;
import pt.ulisboa.tecnico.cross.MainActivity;
import pt.ulisboa.tecnico.cross.R;
import pt.ulisboa.tecnico.cross.account.CryptoManager;
import pt.ulisboa.tecnico.cross.api.APIManager;
import pt.ulisboa.tecnico.cross.databinding.FragmentTouristCardBinding;
import pt.ulisboa.tecnico.cross.ui.CROSSViewModel;
import timber.log.Timber;

public class TouristCardFragment extends Fragment {

  private FragmentTouristCardBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentTouristCardBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CROSSViewModel viewModel = new ViewModelProvider(requireActivity()).get(CROSSViewModel.class);
    viewModel
        .getGems()
        .observe(getViewLifecycleOwner(), gems -> binding.gems.setText(String.valueOf(gems)));
    binding.gemsTip.setOnClickListener(View::performLongClick);
    renderQRCode();
  }

  private void renderQRCode() {
    byte[] encryptedJwt = CryptoManager.get().encrypt(APIManager.get().getJwt().getBytes(UTF_8));
    if (encryptedJwt != null) {
      try {
        binding.qrCode.setImageBitmap(
            new BarcodeEncoder()
                .encodeBitmap(Base64.getEncoder().encodeToString(encryptedJwt), QR_CODE, 400, 400));
        return;
      } catch (WriterException e) {
        Timber.e(e);
      }
    }
    CROSSCityApp.get().showToast("Error generating the QR code.");
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_information_gems)
        .setVisible(true);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ((MainActivity) requireActivity())
        .getOptionsMenu()
        .findItem(R.id.action_information_gems)
        .setVisible(false);
    binding = null;
  }
}
