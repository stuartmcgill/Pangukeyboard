package org.cicipu.pangukeyboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;

import java.util.List;

interface IRefreshView {
    void refreshDefaultStatus();
}

class InputMethodObserver extends ContentObserver {
    private IRefreshView refreshView;

    public InputMethodObserver(Handler handler, IRefreshView refreshView) {
        super(handler);
        this.refreshView = refreshView;
        Log.v("Pangu", "InputMethodObserver constructor");
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.v("Pangu", "onChange");
        super.onChange(selfChange);
        this.refreshView.refreshDefaultStatus();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.v("Pangu", "onChange2");
        onChange(selfChange);
    }
}

public class MainActivity extends AppCompatActivity implements IRefreshView {

    ContentObserver contentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // returns the applications main looper (which runs on the application's main UI thread)
        Looper looper = Looper.getMainLooper();

        // creates the handler using the passed looper
        Handler handler = new Handler(looper);
        this.contentObserver = new InputMethodObserver(handler, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshEnabledStatus();
        refreshDefaultStatus();

        ContentResolver resolver = this.getApplicationContext().getContentResolver();

        Uri setting = Settings.Secure.CONTENT_URI;
        resolver.registerContentObserver(setting, true, this.contentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.getApplicationContext().getContentResolver().unregisterContentObserver(this.contentObserver);
    }

    public void onEnable(View view) {
        Intent enableIntent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(enableIntent);

        refreshEnabledStatus();
    }

    public void onDefault(View view) {
        InputMethodManager imeManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imeManager != null) {
            imeManager.showInputMethodPicker();
        }

        refreshDefaultStatus();
    }

    private void refreshEnabledStatus() {
        View view = findViewById(R.id.checkBoxEnabled);
        ((CheckBox) view).setChecked(isPanguEnabled());
    }

    @Override
    public void refreshDefaultStatus() {
        View view = findViewById(R.id.checkBoxDefault);
        ((CheckBox) view).setChecked(isPanguDefault());
    }

    private boolean isPanguEnabled()
    {
        InputMethodManager imeManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledMethods = imeManager.getEnabledInputMethodList();

        boolean enabled = false;
        for (InputMethodInfo method : enabledMethods)
        {
            String name = method.getPackageName();
            if (name.contains("pangukeyboard")) {
                enabled = true;
                break;
            }
        }

        return enabled;
    }

    private boolean isPanguDefault()
    {
        boolean isDefault = false;

        InputMethodManager imeManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> enabledMethods = imeManager.getEnabledInputMethodList();

        String defaultId = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        for (InputMethodInfo method : enabledMethods)
        {
            String name = method.getPackageName();
            if (name.contains("pangukeyboard")) {
                if (method.getId().equals(defaultId)) {
                    isDefault = true;
                }
                break;
            }
        }

        return isDefault;
    }
}
