package com.accessibility.testapp.ui.fragmnet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.fragmnet.grid.GridFragment;

/**
 * @author Aleksandr Brazhkin
 */
public class StartFragment extends Fragment {

    /**
     * Delay to block opening multiple screens at once.
     */
    private static final long BTN_CLICK_DELAY = 100;

    public static StartFragment newInstance() {
        return new StartFragment();
    }

    private boolean navigationBlocked;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        Button speechScreenBtn = view.findViewById(R.id.speechScreenBtn);
        speechScreenBtn.setOnClickListener(v -> onSpeechScreenBtnClick());
        Button figuresScreenBtn = view.findViewById(R.id.figuresScreenBtn);
        figuresScreenBtn.setOnClickListener(v -> onFiguresScreenBtnClick());
        Button gridScreenBtn = view.findViewById(R.id.gridScreenBtn);
        gridScreenBtn.setOnClickListener(v -> onGridScreenBtnClick());

        return view;
    }

    private void onSpeechScreenBtnClick() {
        navigateToFragment(SpeechFragment.newInstance());
    }

    private void onFiguresScreenBtnClick() {
        navigateToFragment(FiguresFragment.newInstance());
    }

    private void onGridScreenBtnClick() {
        navigateToFragment(GridFragment.newInstance());
    }

    private void navigateToFragment(Fragment fragment) {
        if (navigationBlocked) {
            return;
        }
        View view = getView();
        if (view == null) {
            return;
        }
        if (getFragmentManager().findFragmentById(R.id.fragmentContainer) != this) {
            return;
        }
        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, fragment)
                .commit();
        navigationBlocked = true;
        view.postDelayed(() -> navigationBlocked = false, BTN_CLICK_DELAY);
    }
}
