package com.accessibility.testapp.ui.fragmnet;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.widget.figureView.FigureView;

/**
 * @author Aleksandr Brazhkin
 */
public class FiguresFragment extends Fragment {

    public static FiguresFragment newInstance() {
        return new FiguresFragment();
    }

    //region Views
    private FigureView figureView;
    //endregion

    Vibrator vibrator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_figures, container, false);
        figureView = view.findViewById(R.id.figureView);
        figureView.setTouchListener(touchListener);

        RadioGroup figureTypes = view.findViewById(R.id.figureTypes);
        figureTypes.setOnCheckedChangeListener((group, checkedId) -> onFigureTypeSelected(checkedId));

        SeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onSeekBarProgressChanged(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return view;
    }

    private void onFigureTypeSelected(int checkedId) {
        if (checkedId == R.id.figureTypeCircle) {
            figureView.setFigureType(FigureView.TYPE_CIRCLE);
        } else if (checkedId == R.id.figureTypeTriangle) {
            figureView.setFigureType(FigureView.TYPE_TRIANGLE);
        } else if (checkedId == R.id.figureTypeSquare) {
            figureView.setFigureType(FigureView.TYPE_SQUARE);
        }
    }

    private void onSeekBarProgressChanged(int progress) {
        figureView.setScale(progress / 100f);
    }

    private final FigureView.TouchListener touchListener = new FigureView.TouchListener() {

        private final long[] vibratePattern = new long[]{0, 10};

        @Override
        public void onFigureTouchStateChanged(boolean inTouch) {
            figureView.setBackgroundColor(inTouch ? Color.BLUE : Color.MAGENTA);
            if (!vibrator.hasVibrator()) {
                return;
            }
            if (inTouch) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
                } else {
                    //deprecated in API 26
                    vibrator.vibrate(vibratePattern, 0);
                }
            } else {
                vibrator.cancel();
            }
        }
    };
}
