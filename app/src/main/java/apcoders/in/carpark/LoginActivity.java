package apcoders.in.carpark;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;
//
//import com.github.ybq.android.spinkit.sprite.Sprite;
//import com.github.ybq.android.spinkit.style.Wave;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;

//import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {
    EditText edEmail, edPassowrd;
    TextView tv;
    Button btn;
    String UserType;
    FirebaseAuth firebaseAuth;
    RadioGroup radioGroup;
    FirebaseFirestore firebaseFirestore;
    CollectionReference UserscollectionReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build());
        UserscollectionReference = firebaseFirestore.collection("Users");

        edEmail = findViewById(R.id.editTextLoginUsername);
        edPassowrd = findViewById(R.id.editTextLoginPassword);
        tv = findViewById(R.id.textViewNewUser);
        btn = findViewById(R.id.buttonLogin);
        radioGroup = findViewById(R.id.radioGroup);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.spin_kit);
        Sprite doubleBounce = new Wave();
        progressBar.setIndeterminateDrawable(doubleBounce);
        SharedPreferences sharedPreferences = getSharedPreferences("share_prefs", MODE_PRIVATE);
        UserType = sharedPreferences.getString("UserType", "User"); // Default: Normal User
        setRadioButtons();
        isLogin();
        sharedPreferences = getSharedPreferences("share_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        try {
            if (firebaseAuth.getCurrentUser().getUid() != null) {
                // If already logged in, skip login and go to HomeActivity
                navigateToHome();

            }
        } catch (Exception exception) {

        }


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                btn.setEnabled(false);
                String email = edEmail.getText().toString();
                String password = edPassowrd.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    progressBar.setVisibility(View.GONE);
                    btn.setEnabled(true);
                    Toasty.error(LoginActivity.this, "Fill All The Filed", Toasty.LENGTH_LONG).show();
                } else {
                    UserscollectionReference.whereEqualTo("email", email.toLowerCase()).whereEqualTo("userRole", UserType).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getDocuments().size() > 0) {
                                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                progressBar.setVisibility(View.GONE);
                                                btn.setEnabled(true);
                                                Toasty.success(LoginActivity.this, "LoginActivity Successful", Toasty.LENGTH_LONG).show();

                                                SharedPreferences sharedPreferences = getSharedPreferences("share_prefs", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putBoolean("isLoggedIn", true);
                                                editor.putString("UserType", UserType);
                                                editor.apply();
                                                editor.commit();
                                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                i.putExtra("UserType", UserType);
                                                editor.putString("UserType", UserType);
                                                startActivity(i);
                                                finish();

                                                navigateToHome(); // Navigate based on updated UserType

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            btn.setEnabled(true);
                                            Toasty.error(LoginActivity.this, "Invalid Credentials", Toasty.LENGTH_LONG).show();
                                        }
                                    });
                                }else {
                                    progressBar.setVisibility(View.GONE);
                                    btn.setEnabled(true);
                                    Toasty.error(LoginActivity.this, "Check Your Operating Role", Toasty.LENGTH_LONG).show();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            btn.setEnabled(true);
                            Toasty.error(LoginActivity.this, "Check Your Operating Role", Toasty.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void navigateToHome() {
        SharedPreferences sharedPreferences = getSharedPreferences("share_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        editor.putString("UserType", UserType);
        i.putExtra("UserType", UserType);
        startActivity(i);
        finish();

    }


    private void setRadioButtons() {

        if (UserType.equals("User")) {
            radioGroup.check(R.id.radioBtnNormalBtn);
//            EdAuthorityLevel.setVisibility(View.GONE);
        } else {
            radioGroup.check(R.id.radioBtnAuthorityBtn);
//            EdAuthorityLevel.setVisibility(View.VISIBLE);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtnNormalBtn) {
                    // Handle "Normal User" selection
//                    EdAuthorityLevel.setVisibility(View.GONE);
                    UserType = "User";
                    Toast.makeText(getApplicationContext(), "Normal User selected", Toast.LENGTH_SHORT).show();

                } else if (checkedId == R.id.radioBtnAuthorityBtn) {
                    // Handle "Authorities" selection
//                    EdAuthorityLevel.setVisibility(View.VISIBLE);
                    UserType = "ParkingOwner";
                    Toast.makeText(getApplicationContext(), "Authorities selected", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void isLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("share_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            if (firebaseAuth.getCurrentUser().getUid() != null) {

                editor.putString("UserType", UserType);
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("UserType", UserType);
                startActivity(i);
                finish();

            }
        } catch (Exception exception) {

        }
    }

}