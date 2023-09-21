package com.example.glidepicture.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.glidepicture.R;
import com.example.glidepicture.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RegisterFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button bRegisterSubmit;
    private TextView tvBack;
    private EditText etEmail, etNickname, etPassword, etPasswordConfirm;
    private ProgressBar progressBar;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.X, true));
        setReenterTransition((new MaterialSharedAxis(MaterialSharedAxis.X, false)));

        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.X, false));
        setReturnTransition((new MaterialSharedAxis(MaterialSharedAxis.X, true)));

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        bRegisterSubmit.setOnClickListener(this);
        tvBack.setOnClickListener(this);

    }

    private void findViews(View view) {

        bRegisterSubmit = view.findViewById(R.id.b_register_submit);
        etEmail = view.findViewById(R.id.et_email);
        etNickname = view.findViewById(R.id.et_nickname);
        etPassword = view.findViewById(R.id.et_password);
        etPasswordConfirm = view.findViewById(R.id.et_password_confirm);
        progressBar = view.findViewById(R.id.progressBar);
        tvBack = view.findViewById(R.id.tv_back);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                getParentFragmentManager().popBackStack();

                break;
            case R.id.b_register_submit:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String password_confirm = etPasswordConfirm.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required!");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please provide valid email!");
            etEmail.requestFocus();
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("Nicknames/" + nickname);

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(getContext(), "exist", Toast.LENGTH_SHORT);
                    etNickname.setError("This nickname already exist!");
                    etNickname.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (etNickname.getError() != null) {
            if (etNickname.getError().toString().contains("exist")) {
                return;
            }
        }

        if (nickname.isEmpty()) {
            etNickname.setError("Nickname is required!");
            etNickname.requestFocus();
            return;
        }
        if (nickname.length() < 3) {
            etNickname.setError("Nickname is too short!");
            etNickname.requestFocus();
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
        if (password_confirm.isEmpty()) {
            etPasswordConfirm.setError("Password confirm is required!");
            etPasswordConfirm.requestFocus();
            return;
        }
        if (!password.matches(password_confirm)) {
            etPasswordConfirm.setError("Passwords do not match!");
            etPasswordConfirm.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            UserModel userModel = new UserModel(email, nickname);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(userModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseDatabase.getInstance().getReference("Nicknames")
                                                        .child(nickname)
                                                        .setValue(email);
                                                Toast.makeText(getContext(), "You have been registered!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                                getParentFragmentManager().popBackStack();
                                            } else {
                                                Toast.makeText(getContext(), "Registration failed! Try again!", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                etEmail.setError(e.getMessage());
                etEmail.requestFocus();
                progressBar.setVisibility(View.GONE);
            }
        });

    }
}