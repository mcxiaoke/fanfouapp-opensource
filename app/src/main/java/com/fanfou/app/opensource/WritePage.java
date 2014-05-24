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
import java.io.IOException;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fanfou.app.opensource.adapter.AtTokenizer;
import com.fanfou.app.opensource.adapter.AutoCompleteCursorAdapter;
import com.fanfou.app.opensource.api.bean.Draft;
import com.fanfou.app.opensource.db.Contents.BasicColumns;
import com.fanfou.app.opensource.db.Contents.DraftInfo;
import com.fanfou.app.opensource.db.Contents.UserInfo;
import com.fanfou.app.opensource.dialog.ConfirmDialog;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.service.PostStatusService;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.ActionBar.AbstractAction;
import com.fanfou.app.opensource.ui.ActionManager;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.ui.widget.MyAutoCompleteTextView;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IOHelper;
import com.fanfou.app.opensource.util.ImageHelper;
import com.fanfou.app.opensource.util.OptionHelper;
import com.fanfou.app.opensource.util.StringHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.20
 * @version 2.0 2011.10.24
 * @version 3.0 2011.10.27
 * @version 4.0 2011.11.08
 * @version 4.7 2011.12.26
 * 
 */
public class WritePage extends BaseActivity {

    private class LocationMonitor implements LocationListener {

        @Override
        public void onLocationChanged(final Location location) {
            if (location != null) {
                updateLocationString(location);
            }
        }

        @Override
        public void onProviderDisabled(final String provider) {
        }

        @Override
        public void onProviderEnabled(final String provider) {
        }

        @Override
        public void onStatusChanged(final String provider, final int status,
                final Bundle extras) {
        }

    }

    private class SendAction extends AbstractAction {

        public SendAction() {
            super(R.drawable.ic_send);
        }

        @Override
        public void performAction(final View view) {
            doSend();
        }

    }

    private static final String TAG = WritePage.class.getSimpleName();
    private static final int REQUEST_PHOTO_CAPTURE = 0;
    private static final int REQUEST_PHOTO_LIBRARY = 1;
    private static final int REQUEST_LOCATION_ADD = 2;

    private static final int REQUEST_USERNAME_ADD = 3;

    private static final int REQUEST_PHOTO_EFFECTS = 4;
    private ActionBar mActionBar;

    private MyAutoCompleteTextView mAutoCompleteTextView;
    private View mPictureView;
    private ImageView iPicturePrieview;
    private ImageView iPictureRemove;

    private TextView tWordsCount;
    private ImageView iAtIcon;
    private ImageView iDraftIcon;
    private ImageView iLocationIcon;
    private ImageView iGalleryIcon;

    private ImageView iCameraIcon;
    private Uri photoUri;
    private File photo;
    private String contentOriginal;
    private String content;

    private int wordsCount;

    private String mLocationString;
    private LocationManager mLocationManager;
    private String mLocationProvider;

    private LocationMonitor mLocationMonitor;

    private boolean enableLocation;
    private String inReplyToStatusId;
    private String text;
    private int type;

    private int size;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_REPLY = 1;
    public static final int TYPE_REPOST = 2;
    public static final int TYPE_GALLERY = 3;

    public static final int TYPE_CAMERA = 4;

    private void checkSave() {

        final ConfirmDialog dialog = new ConfirmDialog(this, "保存草稿",
                "要保存未发送内容为草稿吗？");
        dialog.setButton1Text("保存");
        dialog.setButton2Text("放弃");
        dialog.setClickListener(new ConfirmDialog.ClickHandler() {

            @Override
            public void onButton1Click() {
                doSaveDrafts();
                finish();
            }

            @Override
            public void onButton2Click() {
                finish();
            }
        });
        dialog.show();
    }

    private void deleteDraft(final int id) {
        if (id >= 0) {
            getContentResolver().delete(
                    ContentUris.withAppendedId(DraftInfo.CONTENT_URI, id),
                    null, null);
        }
    }

    private void doSaveDrafts() {
        final Draft d = new Draft();
        d.type = this.type;
        d.text = this.content;
        d.filePath = this.photo == null ? "" : this.photo.toString();
        d.replyTo = this.inReplyToStatusId;
        getContentResolver().insert(DraftInfo.CONTENT_URI, d.toContentValues());
    }

    private void doSend() {
        if (this.wordsCount < 1) {
            CommonHelper.notify(this, "消息内容不能为空");
            return;
        }
        CommonHelper.hideKeyboard(this, this.mAutoCompleteTextView);
        startSendService();
        finish();
    }

    private void hidePreview() {
        this.mPictureView.setVisibility(View.GONE);
    }

    private void initialize() {
        this.enableLocation = OptionHelper.readBoolean(this.mContext,
                R.string.option_location_enable, true);
        this.mLocationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        this.mLocationMonitor = new LocationMonitor();
        this.size = Float.valueOf(
                getResources().getDimension(R.dimen.photo_preview_width))
                .intValue();
        for (final String provider : this.mLocationManager.getProviders(true)) {
            if (LocationManager.NETWORK_PROVIDER.equals(provider)
                    || LocationManager.GPS_PROVIDER.equals(provider)) {
                this.mLocationProvider = provider;
                break;
            }
        }

        if (this.mDisplayMetrics.heightPixels < 600) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        }
    }

    private void insertNames(final Intent intent) {
        final String names = intent.getStringExtra(Constants.EXTRA_TEXT);
        if (AppContext.DEBUG) {
            log("doAddUserNames: " + names);
        }
        if (!StringHelper.isEmpty(names)) {
            final Editable editable = this.mAutoCompleteTextView
                    .getEditableText();
            editable.append(names);
            Selection.setSelection(editable, editable.length());
        }

    }

    private void log(final String message) {
        Log.d(WritePage.TAG, message);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
            case REQUEST_LOCATION_ADD:
                break;
            case REQUEST_PHOTO_LIBRARY:
                if (AppContext.DEBUG) {
                    log("onActivityResult requestCode=REQUEST_PHOTO_LIBRARY");
                }
                if (data != null) {
                    final Uri uri = data.getData();
                    onPhotoFromLibrary(uri);
                }
                break;
            case REQUEST_PHOTO_CAPTURE:
                if (AppContext.DEBUG) {
                    log("onActivityResult requestCode=REQUEST_PHOTO_CAPTURE");
                }
                if (data != null) {
                    onPhotoFromCamera(data);
                }
                break;
            case REQUEST_PHOTO_EFFECTS:
                if (data != null) {
                    log("onActivityResult requestCode=REQUEST_PHOTO_EFFECTS uri="
                            + data.getData());
                    onPhotoFromFiltered(data);
                }
                break;
            case REQUEST_USERNAME_ADD:
                if (AppContext.DEBUG) {
                    log("onActivityResult requestCode=REQUEST_USERNAME_ADD");
                }
                if (data != null) {
                    insertNames(data);
                }
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (AppContext.DEBUG) {
            log("onBackPressed content=" + this.content + " contentOriginal="
                    + this.contentOriginal);
        }
        if (StringHelper.isEmpty(this.content)
                || this.content.equals(this.contentOriginal)) {
            super.onBackPressed();
        } else {
            checkSave();
        }

    }

    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
        case R.id.write_action_at:
            startAddUsername();
            break;
        case R.id.write_action_draft:
            ActionManager.doShowDrafts(this);
            break;
        case R.id.write_action_location:
            switchLocation();
            break;
        case R.id.write_action_gallery:
            startAddPicture();
            break;
        case R.id.write_action_camera:
            startCameraShot();
            break;
        case R.id.write_picture_remove:
            removePicture();
            break;
        default:
            break;
        }

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
        setLayout();
        parseIntent();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.enableLocation) {
            this.mLocationManager.removeUpdates(this.mLocationMonitor);
        }
    }

    private void onPhotoFromCamera(final Intent data) {
        try {
            if (AppContext.DEBUG) {
                log("from camera uri=" + this.photoUri);
                log("from camera filename=" + this.photo.getCanonicalPath());
                log("file.size=" + this.photo.length());
            }
            showPreview();
            // applyEffects(photoUri);
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void onPhotoFromFiltered(final Intent data) {
        final Uri uri = data.getData();

        String path;
        if (uri.getScheme().equals("content")) {
            path = IOHelper.getRealPathFromURI(this, uri);
        } else {
            path = uri.getPath();
        }
        this.photo = new File(path);
        if (this.photo.exists()) {
            this.photoUri = uri;
        }
        if (AppContext.DEBUG) {
            log("onFilteredComplete uri=" + this.photoUri);
            try {
                log("onFilteredComplete filename="
                        + this.photo.getCanonicalPath());
            } catch (final IOException e) {
            }
            log("onFilteredComplete file.size=" + this.photo.length());
        }
        showPreview();
    }

    private void onPhotoFromLibrary(final Uri uri) {
        if (uri != null) {

            if (AppContext.DEBUG) {
                log("from gallery uri=" + this.photoUri);
            }

            String path;
            if (uri.getScheme().equals("content")) {
                path = IOHelper.getRealPathFromURI(this, uri);
            } else {
                path = uri.getPath();
            }
            this.photo = new File(path);
            if (this.photo.exists()) {
                this.photoUri = uri;
            } else {
                this.photoUri = null;
            }
            if (AppContext.DEBUG) {
                log("from gallery file=" + path);
            }
            showPreview();
            // applyEffects(photoUri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.enableLocation && (this.mLocationProvider != null)) {
            this.mLocationManager.requestLocationUpdates(
                    this.mLocationProvider, 0, 0, this.mLocationMonitor);
        }
    }

    private void parseIntent() {
        this.type = WritePage.TYPE_NORMAL;
        final Intent intent = getIntent();
        if (intent != null) {
            final String action = intent.getAction();
            if (action == null) {
                this.type = intent.getIntExtra(Constants.EXTRA_TYPE,
                        WritePage.TYPE_NORMAL);
                this.text = intent.getStringExtra(Constants.EXTRA_TEXT);
                this.inReplyToStatusId = intent
                        .getStringExtra(Constants.EXTRA_IN_REPLY_TO_ID);
                final File file = (File) intent
                        .getSerializableExtra(Constants.EXTRA_DATA);
                final int draftId = intent.getIntExtra(Constants.EXTRA_ID, -1);
                parsePhoto(file);
                updateUI();
                deleteDraft(draftId);
            } else if (action.equals(Intent.ACTION_SEND)
                    || action.equals(Constants.ACTION_SEND)) {
                final Bundle extras = intent.getExtras();
                if (extras != null) {
                    this.text = extras.getString(Intent.EXTRA_TEXT);
                    final Uri uri = extras.getParcelable(Intent.EXTRA_STREAM);
                    onPhotoFromLibrary(uri);
                    updateUI();
                }
            } else if (action.equals(Constants.ACTION_SEND_FROM_GALLERY)) {
                this.type = WritePage.TYPE_GALLERY;
                startAddPicture();
            } else if (action.equals(Constants.ACTION_SEND_FROM_CAMERA)) {
                this.type = WritePage.TYPE_CAMERA;
                startCameraShot();
            }

            this.contentOriginal = this.text == null ? null : this.text.trim();
            if (AppContext.DEBUG) {
                log("intent type=" + this.type);
                log("intent text=" + this.text);
            }
        }
    }

    private void parsePhoto(final File file) {
        if ((file != null) && file.exists()) {
            this.photo = file;
            this.photoUri = Uri.fromFile(file);
            if (AppContext.DEBUG) {
                log("from file=" + file);
            }
        }
    }

    private void removePicture() {
        hidePreview();
        this.photo = null;
        this.photoUri = null;
    }

    /**
     * 初始化和设置ActionBar
     */
    private void setActionBar() {
        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle("写消息");
        this.mActionBar.setRightAction(new SendAction());
        this.mActionBar.setLeftAction(new ActionBar.BackAction(this));

    }

    private void setAutoComplete() {
        this.mAutoCompleteTextView = (MyAutoCompleteTextView) findViewById(R.id.write_text);
        this.mAutoCompleteTextView
                .addTextChangedListener(new TextChangeListener() {

                    @Override
                    public void onTextChanged(final CharSequence s,
                            final int start, final int before, final int count) {
                        WritePage.this.content = s.toString().trim();
                        WritePage.this.wordsCount = WritePage.this.content
                                .length();
                        showCount(WritePage.this.wordsCount);
                    }
                });

        this.mAutoCompleteTextView.setTokenizer(new AtTokenizer());
        this.mAutoCompleteTextView.setBackgroundResource(R.drawable.input_bg);
        final String[] projection = new String[] { BaseColumns._ID,
                BasicColumns.ID, UserInfo.SCREEN_NAME, BasicColumns.TYPE,
                BasicColumns.OWNER_ID };
        final String where = BasicColumns.OWNER_ID + " = '"
                + AppContext.getUserId() + "' AND " + BasicColumns.TYPE
                + " = '" + Constants.TYPE_USERS_FRIENDS + "'";
        // Cursor cursor = managedQuery(UserInfo.CONTENT_URI, projection, where,
        // null,
        // null);
        final Cursor cursor = getContentResolver().query(UserInfo.CONTENT_URI,
                projection, where, null, null);
        this.mAutoCompleteTextView.setAdapter(new AutoCompleteCursorAdapter(
                this, cursor));
    }

    private void setLayout() {

        setContentView(R.layout.write);

        setActionBar();
        setAutoComplete();

        this.mPictureView = findViewById(R.id.write_picture);
        this.iPicturePrieview = (ImageView) findViewById(R.id.write_picture_prieview);
        this.iPictureRemove = (ImageView) findViewById(R.id.write_picture_remove);

        this.tWordsCount = (TextView) findViewById(R.id.write_extra_words);

        this.iAtIcon = (ImageView) findViewById(R.id.write_action_at);
        this.iDraftIcon = (ImageView) findViewById(R.id.write_action_draft);
        this.iLocationIcon = (ImageView) findViewById(R.id.write_action_location);
        this.iGalleryIcon = (ImageView) findViewById(R.id.write_action_gallery);
        this.iCameraIcon = (ImageView) findViewById(R.id.write_action_camera);

        this.iAtIcon.setOnClickListener(this);
        this.iDraftIcon.setOnClickListener(this);
        this.iLocationIcon.setOnClickListener(this);
        this.iGalleryIcon.setOnClickListener(this);
        this.iCameraIcon.setOnClickListener(this);

        this.iPictureRemove.setOnClickListener(this);

        this.iLocationIcon
                .setImageResource(this.enableLocation ? R.drawable.ic_bar_geoon
                        : R.drawable.ic_bar_geooff);

    }

    private void showCount(final int count) {
        if (count > 140) {
            this.tWordsCount.setTextColor(getResources().getColorStateList(
                    R.color.write_count_alert_text));
            this.tWordsCount.setText("字数超标：" + (count - 140));
        } else {

            this.tWordsCount.setTextColor(getResources().getColorStateList(
                    R.color.write_count_text));
            this.tWordsCount.setText("剩余字数：" + (140 - count));
        }

    }

    private void showPreview() {
        this.mPictureView.setVisibility(View.VISIBLE);
        try {
            this.iPicturePrieview
                    .setImageBitmap(ImageHelper.getRoundedCornerBitmap(
                            ImageHelper.resampleImage(this.photo, this.size), 6));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void startAddPicture() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        // startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
        startActivityForResult(Intent.createChooser(intent, "选择照片"),
                WritePage.REQUEST_PHOTO_LIBRARY);
    }

    private void startAddUsername() {
        final Intent intent = new Intent(this, UserChoosePage.class);
        startActivityForResult(intent, WritePage.REQUEST_USERNAME_ADD);
    }

    private void startCameraShot() {
        this.photo = IOHelper.getPhotoFilePath(this);
        this.photoUri = Uri.fromFile(this.photo);
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.photoUri);
        startActivityForResult(Intent.createChooser(intent, "拍摄照片"),
                WritePage.REQUEST_PHOTO_CAPTURE);

    }

    private void startSendService() {
        final Intent i = new Intent(this.mContext, PostStatusService.class);
        i.putExtra(Constants.EXTRA_TYPE, this.type);
        i.putExtra(Constants.EXTRA_TEXT, this.content);
        i.putExtra(Constants.EXTRA_DATA, this.photo);
        i.putExtra(Constants.EXTRA_LOCATION, this.mLocationString);
        i.putExtra(Constants.EXTRA_IN_REPLY_TO_ID, this.inReplyToStatusId);
        if (AppContext.DEBUG) {
            log("intent=" + i);
        }
        startService(i);
    }

    private void switchLocation() {
        this.enableLocation = !this.enableLocation;
        OptionHelper.saveBoolean(this.mContext,
                R.string.option_location_enable, this.enableLocation);
        if (AppContext.DEBUG) {
            log("location enable status=" + this.enableLocation);
        }
        if (this.enableLocation) {
            this.iLocationIcon.setImageResource(R.drawable.ic_bar_geoon);
            if (this.mLocationProvider != null) {
                this.mLocationManager.requestLocationUpdates(
                        this.mLocationProvider, 0, 0, this.mLocationMonitor);
            }
        } else {
            this.iLocationIcon.setImageResource(R.drawable.ic_bar_geooff);
            this.mLocationManager.removeUpdates(this.mLocationMonitor);
        }
    }

    private void updateLocationString(final Location loc) {
        if (loc != null) {
            this.mLocationString = String.format("%1$.5f,%2$.5f",
                    loc.getLatitude(), loc.getLongitude());
            if (AppContext.DEBUG) {
                log("Location Info: " + this.mLocationString);
            }
        }
    }

    private void updateUI() {
        if (!StringHelper.isEmpty(this.text)) {
            this.mAutoCompleteTextView.setText(this.text);
            if (this.type != WritePage.TYPE_REPOST) {
                Selection.setSelection(this.mAutoCompleteTextView.getText(),
                        this.mAutoCompleteTextView.getText().length());
            }
        }
        if (this.photoUri != null) {
            showPreview();
        }
    }

}
