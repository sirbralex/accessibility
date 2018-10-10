package com.accessibility.testapp.ui.fragmnet.grid;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.activity.MainActivity;
import com.accessibility.testapp.ui.helper.imageloader.Cache;
import com.accessibility.testapp.ui.helper.imageloader.ImageDownloader;
import com.accessibility.testapp.ui.helper.imageloader.ImageLoader;

import java.util.Arrays;
import java.util.List;

/**
 * @author Aleksandr Brazhkin
 */
public class GridFragment extends Fragment {

    public static GridFragment newInstance() {
        return new GridFragment();
    }

    //region Views
    private RecyclerView recyclerView;
    private TextView picturesCount;
    private ImageButton incrementBtn;
    private ImageButton decrementBtn;
    //endregion
    private ImageLoader imageLoader;
    private RtPermissionsDelegate rtPermissionsDelegate;
    private PicturesAdapter picturesAdapter;
    private GridLayoutManager layoutManager;
    private List<Pair<Integer, String>> supportedSizes;
    private int currentSizePos = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cache cache = new Cache(getActivity());
        ImageDownloader imageDownloader = new ImageDownloader(cache);
        Handler uiThread = new Handler();
        imageLoader = new ImageLoader(imageDownloader, uiThread);
        picturesAdapter = new PicturesAdapter(imageLoader);
        supportedSizes = Arrays.asList(
                new Pair<>(3, getString(R.string.grid_size_9)),
                new Pair<>(4, getString(R.string.grid_size_16))
        );
        rtPermissionsDelegate = ((MainActivity) getActivity()).getRtPermissionsDelegate();
        rtPermissionsDelegate.setCallback(rtPermissionsDelegateCallback);
        rtPermissionsDelegate.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(picturesAdapter);

        picturesCount = view.findViewById(R.id.picturesCount);
        incrementBtn = view.findViewById(R.id.incrementBtn);
        incrementBtn.setOnClickListener(v -> onIncrementBtnClicked());
        decrementBtn = view.findViewById(R.id.decrementBtn);
        decrementBtn.setOnClickListener(v -> onDecrementBtnClicked());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        rtPermissionsDelegate.onResume();
    }

    @Override
    public void onPause() {
        rtPermissionsDelegate.onPause();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        rtPermissionsDelegate.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        rtPermissionsDelegate.setCallback(null);
        imageLoader.cancel();
        super.onDestroy();
    }


    private void onIncrementBtnClicked() {
        if (currentSizePos < supportedSizes.size() - 1) {
            currentSizePos++;
        }
        updateState();
    }


    private void onDecrementBtnClicked() {
        if (currentSizePos > 0) {
            currentSizePos--;
        }
        updateState();
    }

    private void updateState() {
        int size = supportedSizes.get(currentSizePos).first;
        List<String> pictures = getPictures(size * size);
        picturesAdapter.setPictures(pictures);
        layoutManager.setSpanCount(size);
        incrementBtn.setEnabled(currentSizePos < supportedSizes.size() - 1);
        decrementBtn.setEnabled(currentSizePos > 0);
        picturesCount.setText(supportedSizes.get(currentSizePos).second);
    }

    private List<String> getPictures(int count) {
        return Arrays.asList(
                "https://images.aif.ru/013/972/426446b72e46adcb12070889b05c5552.jpg",
                "http://1001goroskop.ru/img/gadanie/derevo_zh/_derevo.jpg",
                "https://bugaga.ru/uploads/posts/2017-03/1488547866_kartinko-5.jpg",
                "http://vestikavkaza.ru/upload/2018-05-22/15269863175b03f64d5f1709.85218357.jpg",
                "https://hi-news.ru/wp-content/uploads/2017/05/space-03-650x433.jpg",
                "https://content.nebo.by/photos/steklo/small/fotopechat-1825.jpg",
                "https://bipbap.ru/wp-content/uploads/2017/05/VOLKI-krasivye-i-ochen-umnye-zhivotnye.jpg",
                "https://static.euronews.com/articles/407077/1000x563_407077.jpg",
                "https://i.mycdn.me/i?r=ATFH4yR_3Vo7iU_-nD1bYWeeCGRm3fig_cX1pJkGgx7jKyCCDx6H11LRhpoEs4OYJOQ",
                "https://habrastorage.org/web/791/423/156/791423156f0e47ae8fe5adbfe2f265a9.jpg",
                "https://freelance.today/uploads/images/00/07/43/2017/08/25/b1e49d.jpg",
                "http://watermark.algid.net/ru/images/article-resize-image-02.jpg",
                "https://www.securitylab.ru/upload/iblock/4a5/4a5f43ea0f6e8c5a4e82c6c5d67b93c1.jpg",
                "https://ecotechnica.com.ua/images/foto9/bfr.jpg",
                "https://henddecor.ru/wp-content/uploads/2013/12/12.jpg",
                "http://spacegid.com/wp-content/uploads/2013/01/Solntse-so-sputnika-SOHO1.jpg"

        ).subList(0, count);
    }

    private final RtPermissionsDelegate.Callback rtPermissionsDelegateCallback = granted -> {
        updateState();
    };
}
