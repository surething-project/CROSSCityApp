package pt.ulisboa.tecnico.cross.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.concurrent.atomic.AtomicBoolean;

import pt.ulisboa.tecnico.cross.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

  private LoginViewModel loginViewModel;
  private FragmentLoginBinding binding;
  private final AtomicBoolean touchedUsername = new AtomicBoolean(false);
  private final AtomicBoolean touchedPassword = new AtomicBoolean(false);

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentLoginBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), this::handleLoginFormState);

    binding.username.addTextChangedListener(
        new AfterTextChangedListener() {
          @Override
          public void afterTextChanged(Editable s) {
            loginViewModel.usernameChanged(binding.username.getText().toString().trim());
          }
        });
    binding.password.addTextChangedListener(
        new AfterTextChangedListener() {
          @Override
          public void afterTextChanged(Editable s) {
            loginViewModel.passwordChanged(binding.password.getText().toString().trim());
          }
        });

    binding.username.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (hasFocus && !touchedUsername.get()) touchedUsername.set(true);
          if (!hasFocus) handleLoginFormState(loginViewModel.getLoginFormState().getValue());
        });
    binding.password.setOnFocusChangeListener(
        (v, hasFocus) -> {
          if (hasFocus && !touchedPassword.get()) touchedPassword.set(true);
          if (!hasFocus) handleLoginFormState(loginViewModel.getLoginFormState().getValue());
        });

    binding.password.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) binding.password.clearFocus();
          return false;
        });
    view.setOnClickListener(
        (v) -> {
          getContext()
              .getSystemService(InputMethodManager.class)
              .hideSoftInputFromWindow(v.getWindowToken(), 0);
          binding.username.clearFocus();
          binding.password.clearFocus();
        });

    binding.signin.setOnClickListener(v -> login(v, false));
    binding.signup.setOnClickListener(v -> login(v, true));
  }

  private void handleLoginFormState(LoginFormState loginFormState) {
    if (loginFormState == null) return;
    binding.signin.setEnabled(loginFormState.isDataValid());
    binding.signup.setEnabled(loginFormState.isDataValid());
    if (loginFormState.getUsernameError() != null
        && touchedUsername.get()
        && !binding.username.hasFocus()) {
      binding.username.setError(getString(loginFormState.getUsernameError()));
    }
    if (loginFormState.getPasswordError() != null
        && touchedPassword.get()
        && !binding.password.hasFocus()) {
      binding.password.setError(getString(loginFormState.getPasswordError()));
    }
  }

  private void login(View view, boolean isNewAccount) {
    binding.loading.setVisibility(View.VISIBLE);
    new Thread(
            () -> {
              boolean loginSuccessful =
                  loginViewModel.login(
                      binding.username.getText().toString(),
                      binding.password.getText().toString(),
                      isNewAccount);
              requireActivity()
                  .runOnUiThread(
                      () -> {
                        binding.loading.setVisibility(View.GONE);
                        if (loginSuccessful) Navigation.findNavController(view).popBackStack();
                      });
            })
        .start();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  public abstract static class AfterTextChangedListener implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
  }
}
