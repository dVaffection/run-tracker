package com.dvlab.runtracker.ui;

import android.support.v4.app.Fragment;

public class RunActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new RunFragment();
    }
}
