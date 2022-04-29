package com.example.babin.pantry_app;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * A login screen that offers login via email/password.
 */
public class LoginPage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    // UI references.
    private EditText email;
    private EditText password;
    private LoginButton fb_login;
    private SignInButton google_login;


    //firebase authentication
    private FirebaseAuth mAuth;

    //google login related
    private GoogleApiClient googleApiClient;
    private final int GOOGLE_REQUEST = 1;

    //facebook login related
    private CallbackManager facebookManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //element assignment
        email = findViewById(R.id.email_input);
        password = findViewById(R.id.password);
        fb_login = findViewById(R.id.btn_fb_login);
        google_login = findViewById(R.id.btn_google_login);

        google_login.setOnClickListener(this);
        fb_login.setOnClickListener(this);

        //required for firebase authentication
        mAuth = FirebaseAuth.getInstance();

        //required for google account firebase authentication
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(LoginPage.this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //facebook
        facebookManager = CallbackManager.Factory.create();
        fb_login.setReadPermissions("email", "public_profile");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        logIn(currentUser);
    }

    public void logIn(FirebaseUser currentUser) {
        if(currentUser != null)

        if(currentUser != null) {
            startActivity(new Intent(LoginPage.this, MainActivity.class));
            this.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onClick(View V) {
        switch(V.getId()) {
            case R.id.btn_login:
                attemptUserSignIn();
                break;
            case R.id.btn_fb_login:
                facebookSignIn();
                break;
            case R.id.btn_google_login:

                googleSignIn();
                break;
            case R.id.btn_create_account:
                startActivity(new Intent(LoginPage.this, SignUpPage.class));
                break;
            default:
                break;
        }
    }

    //This method is for Pantry App custom account log in
    public void attemptUserSignIn(){
        if(isEmailValid(email.getText().toString()) && isPasswordValid(password.getText().toString())) {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                logIn(mAuth.getCurrentUser());
                            } else {
                                Log.w("signInWithEmail:failure", task.getException());
                                Toast errorToast = Toast.makeText(LoginPage.this, "Could Not Sign In", Toast.LENGTH_SHORT);
                                errorToast.setGravity(Gravity.CENTER, 0, 0);
                                errorToast.show();
                            }
                        }
                    });
        } else {
            Toast toast = Toast.makeText(LoginPage.this,"Email or Password is not formatted correctly", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    private void googleSignIn() {

        Intent signIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);

        startActivityForResult(signIntent, GOOGLE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GOOGLE_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                googleAuthentication(account);
            }
        } else {
            facebookManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void googleAuthentication(GoogleSignInAccount account){
        AuthCredential cred = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(cred)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            logIn(mAuth.getCurrentUser());
                        }
                        else {
                            Toast errorToast = Toast.makeText(LoginPage.this,"Could Not Sign in with Google.", Toast.LENGTH_SHORT);
                            errorToast.setGravity(Gravity.CENTER, 0, 0);
                            errorToast.show();
                        }
                    }
                });
    }

    public void facebookSignIn() {
        fb_login.registerCallback(facebookManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                validateFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                // ...
            }
        });
    }

    public void validateFacebookToken(AccessToken accessToken){
        AuthCredential cred = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(cred).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.w("LOGIN SUCCESSFUL", "LOGIN WORKED");
                    FirebaseUser user = mAuth.getCurrentUser();
                    logIn(user);
                } else {
                    //TODO: Error message
                }
            }
        });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

