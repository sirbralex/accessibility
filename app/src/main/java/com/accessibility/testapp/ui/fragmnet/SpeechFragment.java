package com.accessibility.testapp.ui.fragmnet;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.accessibility.testapp.R;

import java.util.Locale;

/**
 * @author Aleksandr Brazhkin
 */
public class SpeechFragment extends Fragment {

    public static SpeechFragment newInstance() {
        return new SpeechFragment();
    }

    //region Views
    private EditText editText;
    private Spinner languageSpinner;
    private Button speakBtn;
    //endregion
    private TextToSpeech textToSpeech;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new TextToSpeech(getContext(), onInitListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speech, container, false);

        editText = view.findViewById(R.id.editText);
        speakBtn = view.findViewById(R.id.speakBtn);
        speakBtn.setOnClickListener(v -> onSpeechBtnClick());
        languageSpinner = view.findViewById(R.id.languageSpinner);
        ArrayAdapter<AdapterItem> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        adapter.addAll(
                new AdapterItem("en", R.string.speech_lang_english),
                new AdapterItem("fr", R.string.speech_lang_french),
                new AdapterItem("de", R.string.speech_lang_german),
                new AdapterItem("it", R.string.speech_lang_italian),
                new AdapterItem("ru", R.string.speech_lang_russian)
        );
        languageSpinner.setAdapter(adapter);

        return view;
    }

    private void onSpeechBtnClick() {
        AdapterItem adapterItem = (AdapterItem) languageSpinner.getSelectedItem();
        Locale locale = new Locale(adapterItem.locale);
        int result = textToSpeech.setLanguage(locale);

        if (result == TextToSpeech.LANG_MISSING_DATA
                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(getContext(), "Language is not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = editText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onDestroy() {
        textToSpeech.stop();
        textToSpeech.shutdown();
        super.onDestroy();
    }

    private final TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                speakBtn.setEnabled(true);
            } else {
                Toast.makeText(getContext(), "TextToSpeech init error", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private class AdapterItem {
        private final String locale;
        private final int displayValueRes;

        private AdapterItem(String locale, @StringRes int displayValueRes) {
            this.locale = locale;
            this.displayValueRes = displayValueRes;
        }

        @Override
        public String toString() {
            return getString(displayValueRes);
        }
    }
}
