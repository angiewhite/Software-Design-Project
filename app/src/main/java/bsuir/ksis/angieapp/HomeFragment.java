package bsuir.ksis.angieapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import bsuir.ksis.angieapp.services.rss.FeedItem;
import bsuir.ksis.angieapp.services.rss.FeedsAdapter;
import bsuir.ksis.angieapp.services.rss.RssReader;
import bsuir.ksis.angieapp.storage.CacheRepository;
import bsuir.ksis.angieapp.interfaces.OnProgressListener;

import java.net.MalformedURLException;
import java.util.ArrayList;


public class HomeFragment extends Fragment implements RssReader.OnFeedItemLoadedListener, RssReader.OnItemsLoadedListener, OnProgressListener {

    private RecyclerView recyclerView;
    private FeedsAdapter feedsAdapter;
    private RssReader rssReader;
    private ProgressDialog progressDialog;
    private boolean loadedFromCache = false;
    private static boolean loaded = false;
    private FeedsAdapter.OnItemClickListener onItemClickListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.news_recyclerView);
        int orientation = getContext().getResources().getConfiguration().orientation;

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), orientation));

        onItemClickListener = new FeedsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FeedItem item) {
                if (checkInternetConnection()) {
                    Intent intent = new Intent(getContext(), RssWebView.class);
                    intent.putExtra("URL", item.getLink());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
                }
            }
        };
        SharedPreferences preferences = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        int lastUserId = preferences.getInt(getString(R.string.preference_user_id), -1);
        int currentUserId = preferences.getInt(getString(R.string.current_user), -1);
        String rssUrl = preferences.getString(getString(R.string.preference_rss_url), null);
        if (lastUserId == -1){
            askToInputNewUrl(getString(R.string.welcome_to_rss));
        } else if (lastUserId != currentUserId) {
            CacheRepository.getInstance().removeCacheForUser(getContext(), String.valueOf(lastUserId));
            askToInputNewUrl(getString(R.string.welcome_to_rss));
        } else {
            doRss(rssUrl);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_news_source:
                showRssSourceInputDialog();
                return true;
            case R.id.refresh:
                String address = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getString(getString(R.string.preference_rss_url), null);
                if (address == null) return true;
                loadRssFromTheInternet(address);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doRss(String address) {
        feedsAdapter = new FeedsAdapter(getContext(), new ArrayList<FeedItem>(), onItemClickListener);
        recyclerView.setAdapter(feedsAdapter);

        boolean isConnected = checkInternetConnection();

        if (isConnected && !loaded) {
            loadRssFromTheInternet(address);
        } else {
            loadRssFromCache();
        }
    }

    private void loadRssFromTheInternet(String address){
        rssReader = new RssReader(getContext(), address);
        rssReader.addOnFeedItemLoadedListener(this);
        rssReader.addOnExecutedListener(this);
        rssReader.addOnProgressListener(this);
        rssReader.execute();
        loaded = true;
    }

    private void loadRssFromCache() {
        loadedFromCache = true;
        ArrayList<FeedItem> items = CacheRepository.getInstance().readRssCache(getContext(),
                String.valueOf(getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getInt(getString(R.string.current_user), -1)));
        feedsAdapter.setFeedItems(items);
        Toast.makeText(getContext(), R.string.feed_loaded_from_cache, Toast.LENGTH_SHORT).show();
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    private void askToInputNewUrl(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setCancelable(false)
                .setMessage(R.string.rss_correct_url_request)
                .setTitle(title)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                showRssSourceInputDialog();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        builder.create().show();

    }

    private void showRssSourceInputDialog() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View dialogView = li.inflate(R.layout.rss_source_input_dialog, null);
        final EditText sourceInput = dialogView
                .findViewById(R.id.rssSourceEditText);
        sourceInput.setText(getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getString(getString(R.string.preference_rss_url), ""));

        AlertDialog.Builder builder = new AlertDialog.Builder(
                getContext());

        builder.setView(dialogView);

        builder
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String url = sourceInput.getText().toString();
                                setRssUrlPreference(url);
                                loadRssFromTheInternet(url);
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onFeedItemLoaded(final FeedItem item) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                feedsAdapter.addItem(item);
            }
        });

    }

    @Override
    public void onFeedItemLoadFailed(Exception e) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.feed_item_loading_failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setLastUserUidPreference(int id) {
        SharedPreferences preferences = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.preference_user_id), id);
        editor.apply();
    }

    private void setRssUrlPreference(String url) {
        SharedPreferences preferences = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.preference_rss_url), url);
        editor.apply();
    }

    @Override
    public void onItemsLoaded() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.feed_loaded, Toast.LENGTH_LONG).show();
                int id = getActivity().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).getInt(getString(R.string.current_user), -1);
                if (id == -1) return;
                setLastUserUidPreference(id);
                CacheRepository.getInstance().writeRssToCache(getContext(), feedsAdapter.getFeedItems(), String.valueOf(id));
            }
        });

    }

    @Override
    public void onItemsLoadFailed(Exception e) {
        if (e instanceof MalformedURLException) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    askToInputNewUrl(getString(R.string.incorrect_rss_url));
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), getString(R.string.loading_failed), Toast.LENGTH_LONG).show();
                    loadRssFromCache();
                }
            });
        }
    }

    @Override
    public void onProgressStarted() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
        }
        progressDialog.show();
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
    }

    @Override
    public void onProgressEnded() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home_fragment, menu);
    }

}
