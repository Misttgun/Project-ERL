package com.maurelsagbo.project_erl.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.maurelsagbo.project_erl.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected static final String TAG = "MainActivity";
    private Button login, offline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initUI();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    /**
     * Method for UI initialization. It help get Ui components and set listeners on them.
     */
    private void initUI(){
        // Get buttons
        login = (Button) findViewById(R.id.btn_login);
        offline = (Button) findViewById(R.id.btn_offline);

        // Set on click listener
        login.setOnClickListener(this);
        offline.setOnClickListener(this);
    }

    /**
     * Method that show the login view when the login button is clicked.
     */
    private void showLogin(){
        LinearLayout loginView = (LinearLayout) getLayoutInflater().inflate(R.layout.login_pop_up, null);
        EditText username = (EditText) findViewById(R.id.username_tf);
        EditText password = (EditText) findViewById(R.id.password_tf);
        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(loginView)
                .setPositiveButton("Connexion",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login :
                showLogin();
                break;
            case R.id.btn_offline :
                // Create an intent and set flags so that the back button can exit the application
                Intent intent = new Intent(this, FlightPActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                break;
        }
    }
}
