package com.sg.moviesindex.utils;

import android.content.Context;
import android.database.MatrixCursor;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sg.moviesindex.BuildConfig;
import com.sg.moviesindex.R;
import com.sg.moviesindex.adapter.SearchAdapter;
import com.sg.moviesindex.fragments.Movies;
import com.sg.moviesindex.model.tmdb.Discover;
import com.sg.moviesindex.model.tmdb.DiscoverDBResponse;
import com.sg.moviesindex.service.FetchFirstTimeDataService;
import com.sg.moviesindex.service.network.MovieDataService;
import com.sg.moviesindex.service.network.RetrofitInstance;
import com.sg.moviesindex.view.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class SearchUtil {
    private Observable<DiscoverDBResponse> observableDB;
    private CompositeDisposable compositeDisposable;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Context context;
    private ProgressBar progressBar;
    private MatrixCursor cursor;


    public SearchUtil(CompositeDisposable compositeDisposable, FragmentManager fragmentManager, Context context, ProgressBar progressBar) {
        this.compositeDisposable = compositeDisposable;
        this.fragmentManager = fragmentManager;
        this.fragmentTransaction = fragmentManager.beginTransaction();
        this.context = context;
        this.progressBar = progressBar;
    }

    public void search(final SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                for (int i = 0; i <= fragmentManager.getBackStackEntryCount(); i++) {
                    fragmentManager.popBackStack();
                }
                if (!query.isEmpty()) {
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    searchView.setIconified(true);
                    final MovieDataService movieDataService = RetrofitInstance.getTMDbService();
                    final String ApiKey = BuildConfig.ApiKey;
                    MainActivity.queryM = query;
                    MainActivity.drawer = 3;
                    observableDB = movieDataService.search(ApiKey, false, query, 1);
                    compositeDisposable.add(observableDB
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<DiscoverDBResponse>() {
                                @Override
                                public void onNext(DiscoverDBResponse discoverDBResponse) {
                                    if (discoverDBResponse != null && discoverDBResponse.getResults() != null) {
                                        MainActivity.discovers = (ArrayList<Discover>) discoverDBResponse.getResults();
                                        MainActivity.totalPages = discoverDBResponse.getTotalPages();
                                        DiscoverToMovie discoverToMovie = new DiscoverToMovie(MainActivity.discovers);
                                        MainActivity.movieList = discoverToMovie.getMovies();
                                        if (progressBar != null) {
                                            progressBar.setIndeterminate(false);
                                        }
                                        fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.add(R.id.frame_layout, new Movies(new FetchFirstTimeDataService(progressBar, compositeDisposable, fragmentManager), SearchUtil.this)).commitAllowingStateLoss();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(context, "Error! " + e.getLocalizedMessage().trim(), Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onComplete() {

                                }
                            }));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final MovieDataService movieDataService = RetrofitInstance.getTMDbService();
                final String ApiKey = BuildConfig.ApiKey;
                MainActivity.queryM = newText;
                MainActivity.drawer = 3;
                observableDB = movieDataService.search(ApiKey, false, MainActivity.queryM, 1);
                compositeDisposable.add(observableDB
                        .debounce(400, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableObserver<DiscoverDBResponse>() {
                            @Override
                            public void onNext(DiscoverDBResponse discoverDBResponse) {
                                if (discoverDBResponse != null && discoverDBResponse.getResults() != null) {
                                    MainActivity.search = (ArrayList<Discover>) discoverDBResponse.getResults();
                                    DiscoverToMovie discoverToMovie = new DiscoverToMovie(MainActivity.search);
                                    MainActivity.moviesearch = discoverToMovie.getMovies();
                                    String a[] = new String[MainActivity.moviesearch.size()];
                                    for (int i = 0; i < a.length; i++) {
                                        a[i] = MainActivity.moviesearch.get(i).getTitle();
                                    }
                                    ArrayAdapter<String> Adapter = new ArrayAdapter<String>(context, R.layout.search_list, a);
                                    String[] columnNames = {"_id", "text"};
                                    cursor = new MatrixCursor(columnNames);
                                    String[] temp = new String[2];
                                    int id = 0;
                                    for (String item : a) {
                                        temp[0] = Integer.toString(id++);
                                        temp[1] = item;
                                        cursor.addRow(temp);

                                    }
                                    SearchAdapter searchAdapter = new SearchAdapter(context, cursor, true, searchView, MainActivity.moviesearch);
                                    searchView.setSuggestionsAdapter(searchAdapter);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        }));
                return true;
            }
        });
    }

    public void loadMoreSearches(int pageIndex, String query) {
        final MovieDataService movieDataService = RetrofitInstance.getTMDbService();
        final String ApiKey = BuildConfig.ApiKey;
        observableDB = movieDataService.search(ApiKey, false, query, pageIndex);
        compositeDisposable.add(observableDB
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<DiscoverDBResponse>() {
                    @Override
                    public void onNext(DiscoverDBResponse discoverDBResponse) {
                        if (discoverDBResponse != null && discoverDBResponse.getResults() != null) {
                            MainActivity.discovers = (ArrayList<Discover>) discoverDBResponse.getResults();
                            MainActivity.totalPages = discoverDBResponse.getTotalPages();
                            DiscoverToMovie discoverToMovie = new DiscoverToMovie(MainActivity.discovers);
                            MainActivity.movieList.addAll(discoverToMovie.getMovies());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "Error! " + e.getLocalizedMessage().trim(), Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }
}
