package ru.samrzhevsky.astronetics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import ru.samrzhevsky.astronetics.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration appBarConfig;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        appBarConfig = new AppBarConfiguration.Builder(
                R.id.nav_theoretical_materials,
                R.id.nav_tests_list,
                R.id.nav_rating,
                R.id.nav_settings)
                .setOpenableLayout(binding.drawerLayout)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfig);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfig) || super.onSupportNavigateUp();
    }

    @SuppressLint("UnsafeIntentLaunch")
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri intentData = intent.getData();
        if (intentData == null) {
            System.out.println("intent data is null");
            return;
        }

        String payload = intentData.getQueryParameter("payload");
        if (payload == null) {
            System.out.println("payload is null");
            return;
        }

        try {
            JSONObject payloadJson = new JSONObject(payload);
            JSONObject oauth = payloadJson.getJSONObject("oauth");

            if (!VkAuth.validateState(oauth.getString("state"))) {
                Toast toast = Toast.makeText(context, "Error: validating state", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            new Thread(() -> {
                try {
                    String exchangeResult = VkAuth.exchangeCode(context, oauth.getString("code"));
                    if (exchangeResult == null) {
                        Intent curIntent = getIntent();
                        finish();
                        startActivity(curIntent);
                    } else {
                        runOnUiThread(() -> new AlertDialog.Builder(context)
                                .setTitle(R.string.error)
                                .setMessage(exchangeResult)
                                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                                .show());
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> new AlertDialog.Builder(context)
                            .setTitle(R.string.error)
                            .setMessage(R.string.error_fatal)
                            .setPositiveButton(R.string.ok, (dialog, id) -> dialog.cancel())
                            .show());
                }
            }).start();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}