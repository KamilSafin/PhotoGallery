package com.example.kamil.photogallery;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private RecyclerView mPhotosRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoAdapter.PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Handler responseHander = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader(responseHander);
        mThumbnailDownloader.setOnThumbnailDownloadedListener(new ThumbnailDownloader.OnThumbnailDownloadedListener<PhotoAdapter.PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoAdapter.PhotoHolder obj, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                obj.bindGalleryItem(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotosRecyclerView = (RecyclerView) view.findViewById(R.id.photos_recycler_view);
        mPhotosRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        new FetchItemsTask().execute("");

        setupAdapter();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setQuery(getContext(), query);
                new FetchItemsTask().execute(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = QueryPreferences.getQuery(getContext());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotosRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemsTask extends AsyncTask<String, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(String... query) {

            if (query == null || query[0].equals("")) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(query[0]);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

        private List<GalleryItem> items;

        public PhotoAdapter(List<GalleryItem> items) {
            this.items = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            Drawable placeholder = getResources().getDrawable(R.mipmap.ic_launcher);
            holder.bindGalleryItem(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, mItems, position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class PhotoHolder extends RecyclerView.ViewHolder {

            private ImageView mImageView;

            public PhotoHolder(View itemView) {
                super(itemView);

                mImageView = (ImageView) itemView.findViewById(R.id.gallery_image_view);
            }

            public void bindGalleryItem(Drawable drawable) {
                mImageView.setImageDrawable(drawable);
            }
        }
    }
}
