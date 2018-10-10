package com.accessibility.testapp.ui.fragmnet.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.accessibility.testapp.R;
import com.accessibility.testapp.ui.helper.imageloader.ImageLoader;
import com.accessibility.testapp.ui.helper.imageloader.ImageRequest;

import java.util.Collections;
import java.util.List;

/**
 * @author Aleksandr Brazhkin
 */
public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.ViewHolder> {

    private final ImageLoader imageLoader;

    private Context context;
    private LayoutInflater layoutInflater;

    private List<String> pictures = Collections.emptyList();

    PicturesAdapter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    void setPictures(@NonNull List<String> pictures) {
        this.pictures = pictures;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        context = recyclerView.getContext();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        context = null;
        layoutInflater = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = layoutInflater.inflate(R.layout.item_view_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        vh.setImagePath(pictures.get(position));
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private String imagePath;
        private ImageRequest pendingRequest;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        void setImagePath(@Nullable String imagePath) {
            this.imagePath = imagePath;
            onImageShouldBeReloaded();
        }

        private void onImageShouldBeReloaded() {
            if (pendingRequest != null) {
                imageLoader.cancelRequest(imageView);
                pendingRequest = null;
            }
            imageView.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
            imageView.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
        }

        private void onImageViewSizeDetermined() {
            if (imagePath != null) {
                pendingRequest = imageLoader
                        .load(imagePath)
                        .resize(imageView.getWidth(), imageView.getHeight())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
        }

        private final ViewTreeObserver.OnPreDrawListener onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (imageView.getWidth() > 0 && imageView.getHeight() > 0) {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    onImageViewSizeDetermined();
                }
                return true;
            }
        };
    }
}
