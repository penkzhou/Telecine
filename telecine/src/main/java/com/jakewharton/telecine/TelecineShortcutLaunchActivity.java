package com.jakewharton.telecine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.gms.analytics.HitBuilders;
import dagger.android.AndroidInjection;

import javax.inject.Inject;

public final class TelecineShortcutLaunchActivity extends Activity {
    private static final String KEY_ACTION = "launch-action";

    static Intent createQuickTileIntent(Context context) {
        Intent intent = new Intent(context, TelecineShortcutLaunchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(KEY_ACTION, Analytics.ACTION_QUICK_TILE_LAUNCHED);
        return intent;
    }

    @Inject
    Analytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        }

        String launchAction = getIntent().getStringExtra(KEY_ACTION);
        if (launchAction == null) {
            launchAction = Analytics.ACTION_SHORTCUT_LAUNCHED;
        }

        analytics.send(new HitBuilders.EventBuilder() //
                .setCategory(Analytics.CATEGORY_SHORTCUT)
                .setAction(launchAction)
                .build());

        CaptureHelper.fireScreenCaptureIntent(this, analytics);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!CaptureHelper.handleActivityResult(this, requestCode, resultCode, data, analytics)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        finish();
    }

    @Override
    protected void onStop() {
        if (!isFinishing()) {
            finish();
        }
        super.onStop();
    }
}
