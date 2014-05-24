/*******************************************************************************
 * Copyright 2011, 2012, 2013 fanfou.com, Xiaoke, Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.fanfou.app.opensource;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fanfou.app.opensource.adapter.SearchAdapter;
import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.Search;
import com.fanfou.app.opensource.http.ResponseCode;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.util.CommonHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.08.02
 * @version 2.0 2011.10.21
 * 
 */
public class SearchPage extends BaseActivity implements OnItemClickListener {

    private class TrendsTask extends AsyncTask<Void, Void, ApiException> {

        @Override
        protected ApiException doInBackground(final Void... params) {
            final ApiClient api = AppContext.getApiClient();
            try {
                final List<Search> savedSearches = api.savedSearchesList();
                if ((savedSearches != null) && (savedSearches.size() > 0)) {
                    SearchPage.this.mHotwords.addAll(savedSearches);
                }

                final List<Search> trends = api.trends();

                if ((trends != null) && (trends.size() > 0)) {
                    SearchPage.this.mHotwords.addAll(trends);
                }

                return null;

            } catch (final ApiException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(final ApiException e) {
            showHotwords();
            if (e != null) {
                final int errorCode = e.statusCode;
                final String errorMessage = e.errorMessage;
                if (errorCode != ResponseCode.HTTP_OK) {
                    CommonHelper.checkErrorCode(SearchPage.this.mContext,
                            errorCode, errorMessage);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }

    }

    private ActionBar mActionBar;
    private ListView mListView;
    private View mEmptyView;
    private BaseAdapter mAdapter;
    private Activity mContext;

    private ArrayList<Search> mHotwords;

    private void fetchHotwords() {
        new TrendsTask().execute();
    }

    private void goSearch(final Context context, final String query) {
        final Intent intent = new Intent(context, SearchResultsPage.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        startActivity(intent);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        this.mHotwords = new ArrayList<Search>(20);
        parseIntent();
        setLayout();
        fetchHotwords();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        final Search s = (Search) parent.getAdapter().getItem(position);
        if (s != null) {
            goSearch(this.mContext, s.query);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void parseIntent() {
    }

    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("搜索");
        this.mActionBar.setRightAction(new ActionBar.SearchAction(this));
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));
    }

    private void setLayout() {
        setContentView(R.layout.search);

        setActionBar();
        this.mEmptyView = findViewById(R.id.empty);
        final TextView tv = (TextView) findViewById(R.id.empty_text);
        tv.setText("热词载入中...");

        this.mListView = (ListView) findViewById(R.id.list);
        this.mListView.setOnItemClickListener(this);
    }

    private void showHotwords() {
        this.mEmptyView.setVisibility(View.GONE);
        this.mListView.setVisibility(View.VISIBLE);

        this.mAdapter = new SearchAdapter(this, this.mHotwords);
        this.mListView.setAdapter(this.mAdapter);
        this.mAdapter.notifyDataSetChanged();
    }

}
