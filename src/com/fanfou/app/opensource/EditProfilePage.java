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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.api.ApiClient;
import com.fanfou.app.opensource.api.ApiException;
import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.cache.ImageLoader;
import com.fanfou.app.opensource.db.FanFouProvider;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.task.AsyncTaskResult;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.ImageHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.11.07
 * @version 1.5 2011.11.08
 * @version 1.6 2011.11.09
 * @version 1.7 2011.11.18
 * 
 */
public class EditProfilePage extends BaseActivity {

    /**
     * 更新个人头像后台任务
     */
    private static class UpdateProfileImageTask extends
            AsyncTask<File, Integer, AsyncTaskResult> {

        private final EditProfilePage mEditProfilePage;
        private ProgressDialog pd = null;
        private ApiClient api;
        private boolean isCancelled;

        public UpdateProfileImageTask(final EditProfilePage context) {
            super();
            this.mEditProfilePage = context;
        }

        @Override
        protected AsyncTaskResult doInBackground(final File... params) {
            if ((params == null) || (params.length == 0)) {
                return new AsyncTaskResult(AsyncTaskResult.CODE_ERROR, "参数不能为空");
            }
            final File srcFile = params[0];
            try {
                final File file = ImageHelper.prepareProfileImage(
                        this.mEditProfilePage, srcFile);
                final User user = this.api.updateProfileImage(file,
                        Constants.MODE);
                if (this.isCancelled) {
                    return new AsyncTaskResult(AsyncTaskResult.CODE_CANCELED,
                            "用户取消");
                }
                if ((user == null) || user.isNull()) {
                    return new AsyncTaskResult(AsyncTaskResult.CODE_FAILED,
                            "更新个人头像失败");
                } else {

                    FanFouProvider.updateUserInfo(this.mEditProfilePage, user);
                    FanFouProvider.updateStatusProfileImageUrl(
                            this.mEditProfilePage, user);

                    return new AsyncTaskResult(AsyncTaskResult.CODE_SUCCESS,
                            "更新个人头像成功", user);
                }
            } catch (final ApiException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
                return new AsyncTaskResult(AsyncTaskResult.CODE_ERROR,
                        e.getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(final AsyncTaskResult result) {
            super.onPostExecute(result);
            this.pd.dismiss();
            final int code = result.code;
            switch (code) {
            case AsyncTaskResult.CODE_SUCCESS:
                CommonHelper.notify(this.mEditProfilePage, result.message);
                onSuccess(result);
                break;
            case AsyncTaskResult.CODE_FAILED:
                CommonHelper.notify(this.mEditProfilePage, result.message);
                break;
            case AsyncTaskResult.CODE_ERROR:
                CommonHelper.notify(this.mEditProfilePage, result.message);
                break;
            case AsyncTaskResult.CODE_CANCELED:
                break;
            default:
                break;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.api = AppContext.getApiClient();
            this.pd = new ProgressDialog(this.mEditProfilePage);
            this.pd.setMessage("正在更新头像...");
            this.pd.setIndeterminate(true);
            this.pd.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(final DialogInterface dialog) {
                    UpdateProfileImageTask.this.isCancelled = true;
                    cancel(true);
                }
            });
            this.pd.show();
        }

        private void onSuccess(final AsyncTaskResult result) {
            final User user = (User) result.content;
            if (user != null) {
                FanFouProvider.updateUserInfo(this.mEditProfilePage, user);
                final Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_DATA, user);
                this.mEditProfilePage.setResult(Activity.RESULT_OK, intent);
                this.mEditProfilePage.user = user;
                this.mEditProfilePage.updateProfileImagePreview();
                // mEditProfilePage.finish();
            }
        }

    }

    /**
     * 更新个人资料后台任务
     */
    private static class UpdateProfileTask extends
            AsyncTask<HashMap<String, String>, Integer, AsyncTaskResult> {

        private final Activity mContext;
        private ProgressDialog pd = null;
        private ApiClient api;
        private boolean isCancelled;

        public UpdateProfileTask(final Activity context) {
            super();
            this.mContext = context;
        }

        @Override
        protected AsyncTaskResult doInBackground(
                final HashMap<String, String>... params) {
            if ((params == null) || (params.length == 0)) {
                return new AsyncTaskResult(AsyncTaskResult.CODE_ERROR, "参数不能为空");
            }
            final Map<String, String> map = params[0];
            final String description = map.get("description");
            final String name = map.get("name");
            final String location = map.get("location");
            final String url = map.get("url");
            try {
                final User user = this.api.updateProfile(description, name,
                        location, url, Constants.MODE);
                if (this.isCancelled) {
                    return new AsyncTaskResult(AsyncTaskResult.CODE_CANCELED,
                            "用户取消");
                }
                if ((user == null) || user.isNull()) {
                    return new AsyncTaskResult(AsyncTaskResult.CODE_FAILED,
                            "更新个人资料失败");
                } else {
                    FanFouProvider.updateUserInfo(this.mContext, user);

                    return new AsyncTaskResult(AsyncTaskResult.CODE_SUCCESS,
                            "更新个人资料成功", user);
                }
            } catch (final ApiException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
                return new AsyncTaskResult(AsyncTaskResult.CODE_ERROR,
                        e.getMessage());

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(final AsyncTaskResult result) {
            super.onPostExecute(result);
            this.pd.dismiss();
            final int code = result.code;
            switch (code) {
            case AsyncTaskResult.CODE_SUCCESS:
                CommonHelper.notify(this.mContext, result.message);
                onSuccess(result);
                break;
            case AsyncTaskResult.CODE_FAILED:
                CommonHelper.notify(this.mContext, result.message);
                break;
            case AsyncTaskResult.CODE_ERROR:
                CommonHelper.notify(this.mContext, result.message);
                break;
            case AsyncTaskResult.CODE_CANCELED:
                break;
            default:
                break;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.api = AppContext.getApiClient();
            this.pd = new ProgressDialog(this.mContext);
            this.pd.setMessage("正在更新个人资料...");
            this.pd.setIndeterminate(true);
            this.pd.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(final DialogInterface dialog) {
                    UpdateProfileTask.this.isCancelled = true;
                    cancel(true);
                }
            });
            this.pd.show();
        }

        private void onSuccess(final AsyncTaskResult result) {
            final User user = (User) result.content;
            if (user != null) {
                final Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_DATA, user);
                this.mContext.setResult(Activity.RESULT_OK, intent);
                this.mContext.finish();
            }
        }

    }

    private static final String TAG = EditProfilePage.class.getSimpleName();

    private ActionBar mActionBar;
    private ImageView mHeadView;

    private ImageView mHeadEdit;
    private Button mButtonOK;

    private Button mButtonCancel;
    private EditText mNameEdit;
    private EditText mDescriptionEdit;
    private EditText mUrlEdit;

    private EditText mLocationEdit;
    private TextView mNameLabel;
    private TextView mDescriptionLabel;
    private TextView mUrlLabel;

    private TextView mLocationLabel;
    private String mName;
    private String mDescription;
    private String mUrl;

    private String mLocation;

    private User user;

    private ImageLoader mLoader;

    private static final int REQUEST_CODE_SELECT_IMAGE = 0;

    @SuppressWarnings("unchecked")
    private void doUpdateProfile() {
        final HashMap<String, String> map = new HashMap<String, String>();
        if (!StringHelper.isEmpty(this.mDescription)
                && !this.mDescription.equals(this.user.description)) {
            map.put("description", this.mDescription);
        }
        if (!StringHelper.isEmpty(this.mName)
                && !this.mName.equals(this.user.screenName)) {
            map.put("name", this.mName);
        }
        if (!StringHelper.isEmpty(this.mUrl)
                && !this.mUrl.equals(this.user.url)) {
            map.put("url", this.mUrl);
        }
        if (!StringHelper.isEmpty(this.mLocation)
                && !this.mLocation.equals(this.user.location)) {
            map.put("location", this.mLocation);
        }
        if (map.size() > 0) {
            new UpdateProfileTask(this).execute(map);
        } else {
            // Utils.notify(this, "无任何修改");
            finish();
        }

    }

    private void doUpdateProfileImage(final File file) {
        new UpdateProfileImageTask(this).execute(file);
    }

    private File getPhotoFilePath(final Uri uri) {
        if (uri != null) {
            if (AppContext.DEBUG) {
                log("from gallery uri=" + uri);
            }
            String path;
            if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                path = IOHelper.getRealPathFromURI(this, uri);
            } else {
                path = uri.getPath();
            }
            final File file = new File(path);
            if (AppContext.DEBUG) {
                log("from gallery file=" + path);
            }
            return file;
        }
        return null;
    }

    private void log(final String message) {
        Log.d(EditProfilePage.TAG, message);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EditProfilePage.REQUEST_CODE_SELECT_IMAGE) {
                final File file = getPhotoFilePath(data.getData());
                doUpdateProfileImage(file);
            }
        }
    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.profile_image:
        case R.id.profile_image_edit:
            startEditProfileImage();
            break;
        case R.id.button_ok:
            doUpdateProfile();
            break;
        case R.id.button_cancel:
            finish();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        this.mLoader = AppContext.getImageLoader();
        setLayout();
        updateUI();
    }

    private void parseIntent() {
        this.user = (User) getIntent().getParcelableExtra(Constants.EXTRA_DATA);
    }

    private void setFakedBold(final TextView tv) {
        final TextPaint tp = tv.getPaint();
        tp.setFakeBoldText(true);
    }

    private void setLayout() {
        setContentView(R.layout.edit_profile);

        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));

        this.mButtonOK = (Button) findViewById(R.id.button_ok);
        this.mButtonOK.setOnClickListener(this);

        this.mButtonCancel = (Button) findViewById(R.id.button_cancel);
        this.mButtonCancel.setOnClickListener(this);

        this.mHeadView = (ImageView) findViewById(R.id.profile_image);
        this.mHeadView.setOnClickListener(this);

        this.mHeadEdit = (ImageView) findViewById(R.id.profile_image_edit);
        this.mHeadEdit.setOnClickListener(this);

        this.mNameEdit = (EditText) findViewById(R.id.profile_name_edit);
        this.mDescriptionEdit = (EditText) findViewById(R.id.profile_description_edit);
        this.mUrlEdit = (EditText) findViewById(R.id.profile_url_edit);
        this.mLocationEdit = (EditText) findViewById(R.id.profile_location_edit);

        this.mNameLabel = (TextView) findViewById(R.id.profile_name);
        this.mDescriptionLabel = (TextView) findViewById(R.id.profile_description);
        this.mUrlLabel = (TextView) findViewById(R.id.profile_url);
        this.mLocationLabel = (TextView) findViewById(R.id.profile_location);

        setFakedBold(this.mNameLabel);
        setFakedBold(this.mDescriptionLabel);
        setFakedBold(this.mUrlLabel);
        setFakedBold(this.mLocationLabel);

        setTextChangeListener();

    }

    private void setTextChangeListener() {
        this.mNameEdit.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                EditProfilePage.this.mName = s.toString();
            }
        });

        this.mDescriptionEdit.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                EditProfilePage.this.mDescription = s.toString();
            }
        });

        this.mUrlEdit.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                EditProfilePage.this.mUrl = s.toString();
            }
        });

        this.mLocationEdit.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                EditProfilePage.this.mLocation = s.toString();
            }
        });

    }

    private void startEditProfileImage() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择头像"),
                EditProfilePage.REQUEST_CODE_SELECT_IMAGE);
    }

    private void updateProfileImagePreview() {
        if (AppContext.DEBUG) {
            log("updateProfileImagePreview() url=" + this.user.profileImageUrl);
        }
        this.mHeadView.setImageResource(R.drawable.default_head);
        this.mHeadView.invalidate();
        this.mHeadView.setTag(this.user.profileImageUrl);
        this.mLoader.displayImage(this.user.profileImageUrl, this.mHeadView,
                R.drawable.default_head);

    }

    private void updateUI() {
        this.mActionBar.setTitle("编辑个人资料");
        this.mNameEdit.setText(this.user.screenName);
        this.mDescriptionEdit.setText(this.user.description);
        this.mUrlEdit.setText(this.user.url);
        this.mLocationEdit.setText(this.user.location);

        updateProfileImagePreview();

    }

}
