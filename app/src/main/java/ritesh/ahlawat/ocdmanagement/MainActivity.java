package ritesh.ahlawat.ocdmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 123;
    public static final String TAG = "tagz";
    private final String DEFAULT_PHOTO_URL = "https://www.cogta.gov.za/cgta_2016/wp-content/uploads/2018/05/placeholder-img.jpg";

    private List<AuthUI.IdpConfig> providers;
    public static FirebaseAuth auth = FirebaseAuth.getInstance();
    private static FirebaseUser user;
//    Button buttonSignOut;
    private Toolbar mToolBar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TextView nameNavHeaderTV;
    private ImageView profilePhotoNavHeaderIV;
    private View headerView;
    private Bundle savedInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstance = savedInstanceState;
        setContentView(R.layout.activity_main);

        mToolBar = findViewById(R.id.main_toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mToolBar.setTitle("");
//        mToolBar.setSubtitle("Hello");
        setSupportActionBar(mToolBar);

        initializeToolBarMenuItemSelectedListener();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                mToolBar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.home_menu_item);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        if (auth.getCurrentUser() == null) {
            // Not signed in
            showSignInOptions();
//            buttonSignOut.setEnabled(false);
        } else {
            initializeOnceAuthorized(savedInstanceState);
        }



    }
    private void initializeOnceAuthorized(Bundle savedInstanceState) {
        // Layout created at this point
        user = auth.getCurrentUser();
        headerView = navigationView.getHeaderView(0);
        nameNavHeaderTV = headerView.findViewById(R.id.nav_header_name);
        nameNavHeaderTV.setText(user.getDisplayName());
        Uri photoURI = user.getPhotoUrl();
        String photoURL;
        if (photoURI == null) photoURL = DEFAULT_PHOTO_URL;
        else photoURL = photoURI.toString().substring(0, user.getPhotoUrl().toString().length()-4) + "256";

        Log.d(TAG, photoURL);
        profilePhotoNavHeaderIV = headerView.findViewById(R.id.nav_header_image);
        Glide.with(this).load(photoURL).into(profilePhotoNavHeaderIV);
//        buttonSignOut = findViewById(R.id.btn_sign_out);
//        setOnClickListeners();

        // Initialize fragment
        // If we're being restored from a previous state, we don't need to do anything
        // Or else we can end up with overlapping fragments.
        if (savedInstanceState != null) return;
        HomeFragment homeFragment = new HomeFragment();
        // In case this activity was started with special instructions from an Intent, pass the extra's to fragment as arguments
        homeFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, homeFragment).commit();
    }

    private void initializeToolBarMenuItemSelectedListener() {
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.top_bar_help_button:
                        Log.d(TAG, "Help");
                        break;
                    case R.id.top_bar_logout_button:
                        Log.d(TAG, "Logout");
                        logout();
                        break;
                }
                return false;
            }
        });
    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(MainActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showSignInOptions();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyTheme)
                .setLogo(R.drawable.ic_logo)
                .setIsSmartLockEnabled(false)
                .setTosAndPrivacyPolicyUrls(
                        "https://example.com/terms.html",
                        "https://example.com/privacy.html")
                .build(), RC_SIGN_IN
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
                Toast.makeText(this, "" + user.getDisplayName() + " Signed In", Toast.LENGTH_SHORT).show();
                initializeOnceAuthorized(savedInstance);
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using the back button.
                // Otherwise check response.getError().getErrorCode() and handle the error.
                if (response == null) {
                    Toast.makeText(this, "Must Sign In", Toast.LENGTH_SHORT).show();
                    showSignInOptions();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    showSignInOptions();
                    return;
                }
                Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
                showSignInOptions();
                Log.d(TAG, "Sign-in error: " + response.getError());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.equals(navigationView.getCheckedItem())) {
            drawerLayout.closeDrawer(GravityCompat.START);

            return false;
        }
        navigationView.setCheckedItem(item);
        Fragment newFragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (item.getItemId()) {
            case R.id.home_menu_item:
                newFragment = new HomeFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
            case R.id.forums_menu_item:
                newFragment = new ForumsFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
            case R.id.social_media_check_menu_item:
                newFragment = new SMCFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
            case R.id.video_check_menu_item:
                newFragment = new VCFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
            case R.id.user_settings_menu_item:
                newFragment = new UserSettingsFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
            case R.id.app_settings_menu_item:
                newFragment = new AppSettingsFragment();
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}