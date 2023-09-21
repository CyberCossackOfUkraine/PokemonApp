package com.example.glidepicture.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glidepicture.R;
import com.example.glidepicture.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;


public class LoginFragment extends Fragment implements View.OnClickListener {

    private TextView tvRegister, tvForgotPassword;
    private EditText etEmail, etPassword;
    private Button bLoginSubmit;
    private CheckBox cbRememberMe;
    private Boolean isRemembered;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private RegisterFragment registerFragment;
    private ResetPasswordFragment resetPasswordFragment;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReenterTransition((new MaterialSharedAxis(MaterialSharedAxis.X, false)));

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition((new MaterialSharedAxis(MaterialSharedAxis.X, true)));

        handleOnBackButton();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        tvRegister.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        bLoginSubmit.setOnClickListener(this);

        registerFragment = new RegisterFragment();
        resetPasswordFragment = new ResetPasswordFragment();

        mAuth = FirebaseAuth.getInstance();

        loginPreferences = this.getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        isRemembered = loginPreferences.getBoolean("isRemembered", false);
        if (isRemembered == true) {
            etEmail.setText(loginPreferences.getString("username", ""));
            etPassword.setText(loginPreferences.getString("password", ""));
            cbRememberMe.setChecked(true);
        }


    }

    private void findViews(View view) {
        tvRegister = view.findViewById(R.id.tv_register);
        tvForgotPassword = view.findViewById(R.id.tv_password_forgot);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        bLoginSubmit = view.findViewById(R.id.b_login_submit);
        cbRememberMe = view.findViewById(R.id.cb_remember_me);
        progressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_register:
                openFragment(registerFragment);
                break;

            case R.id.tv_password_forgot:
                openFragment(resetPasswordFragment);
                break;

            case R.id.b_login_submit:
                Toast.makeText(requireActivity(), "1", Toast.LENGTH_SHORT);
                userLogin();
                break;

        }
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, fragment);
        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName());
        fragmentTransaction.commit();
    }

    private void userLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email!");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password is too short!");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {

                    if (cbRememberMe.isChecked()) {
                        loginPrefsEditor.putBoolean("isRemembered", true);
                        loginPrefsEditor.putString("username", email);
                        loginPrefsEditor.putString("password", password);
                        loginPrefsEditor.commit();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.commit();
                    }

                    if (Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid().equals("XFD5L7PD8bNLirnr2ljplhXJab93")) {
                        Toast.makeText(requireActivity(), "Love you.", Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(requireActivity(), "Failed! Please check your credentials or Internet connection!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void handleOnBackButton() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }
}