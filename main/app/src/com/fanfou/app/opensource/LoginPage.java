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

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Selection;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fanfou.app.opensource.api.bean.User;
import com.fanfou.app.opensource.auth.OAuthConfig;
import com.fanfou.app.opensource.auth.OAuthToken;
import com.fanfou.app.opensource.auth.XAuthService;
import com.fanfou.app.opensource.service.Constants;
import com.fanfou.app.opensource.task.AsyncTaskResult;
import com.fanfou.app.opensource.ui.ActionBar;
import com.fanfou.app.opensource.ui.TextChangeListener;
import com.fanfou.app.opensource.util.AlarmHelper;
import com.fanfou.app.opensource.util.CommonHelper;
import com.fanfou.app.opensource.util.IntentHelper;

/**
 * @author mcxiaoke
 * @version 1.0 2011.06.10
 * @version 2.0 2011.10.17
 * @version 3.0 2011.12.01
 * @version 3.1 2011.12.06
 * @version 3.2 2011.12.13
 * @version 3.3 2011.12.14
 * 
 */
public final class LoginPage extends Activity implements OnClickListener {

    private class LoginTask extends AsyncTask<Void, Integer, AsyncTaskResult> {

        static final int LOGIN_IO_ERROR = 0; // 网络错误
        static final int LOGIN_AUTH_FAILED = 1; // 验证失败
        static final int LOGIN_AUTH_SUCCESS = 2; // 首次验证成功
        static final int LOGIN_CANCELLED_BY_USER = 3;

        private ProgressDialog progressDialog;
        private boolean isCancelled;

        @Override
        protected AsyncTaskResult doInBackground(final Void... params) {
            try {
                final XAuthService xauth = new XAuthService(new OAuthConfig());
                final OAuthToken token = xauth.requestOAuthAccessToken(
                        LoginPage.this.username, LoginPage.this.password);
                if (AppContext.DEBUG) {
                    log("xauth token=" + token);
                }

                if (this.isCancelled) {
                    if (AppContext.DEBUG) {
                        log("login cancelled after xauth process.");
                    }
                    return new AsyncTaskResult(
                            LoginTask.LOGIN_CANCELLED_BY_USER,
                            "user cancel login process.");
                }

                if (token != null) {
                    publishProgress(1);
                    AppContext.setOAuthToken(token);
                    final User u = AppContext.getApiClient().verifyAccount(
                            Constants.MODE);

                    if (this.isCancelled) {
                        if (AppContext.DEBUG) {
                            log("login cancelled after verifyAccount process.");
                        }
                        return new AsyncTaskResult(
                                LoginTask.LOGIN_CANCELLED_BY_USER,
                                "user cancel login process.");
                    }

                    if ((u != null) && !u.isNull()) {
                        AppContext.updateAccountInfo(LoginPage.this.mContext,
                                u, token);
                        if (AppContext.DEBUG) {
                            log("xauth successful! ");
                        }
                        return new AsyncTaskResult(LoginTask.LOGIN_AUTH_SUCCESS);
                    } else {
                        if (AppContext.DEBUG) {
                            log("xauth failed.");
                        }
                        return new AsyncTaskResult(LoginTask.LOGIN_AUTH_FAILED,
                                "XAuth successful, but verifyAccount failed. ");
                    }
                } else {
                    return new AsyncTaskResult(LoginTask.LOGIN_AUTH_FAILED,
                            "username or password is incorrect, XAuth failed.");
                }

            } catch (final IOException e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
                return new AsyncTaskResult(LoginTask.LOGIN_IO_ERROR,
                        getString(R.string.msg_connection_error));
            } catch (final Exception e) {
                if (AppContext.DEBUG) {
                    e.printStackTrace();
                }
                return new AsyncTaskResult(LoginTask.LOGIN_IO_ERROR,
                        e.getMessage());
            } finally {
            }
        }

        @Override
        protected void onPostExecute(final AsyncTaskResult result) {
            if ((this.progressDialog != null) && !LoginPage.this.destroyed) {
                this.progressDialog.dismiss();
            }
            switch (result.code) {
            case LOGIN_IO_ERROR:
            case LOGIN_AUTH_FAILED:
                CommonHelper.notify(LoginPage.this.mContext, result.message);
                break;
            case LOGIN_CANCELLED_BY_USER:
                break;
            case LOGIN_AUTH_SUCCESS:
                AlarmHelper.setScheduledTasks(LoginPage.this.mContext);
                IntentHelper.goHomePage(LoginPage.this.mContext,
                        LoginPage.this.page);
                finish();
                break;
            default:
                break;
            }
        }

        @Override
        protected void onPreExecute() {
            this.progressDialog = new ProgressDialog(LoginPage.this.mContext);
            this.progressDialog.setMessage("正在进行登录认证...");
            this.progressDialog.setIndeterminate(true);
            this.progressDialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(final DialogInterface dialog) {
                            LoginTask.this.isCancelled = true;
                            cancel(true);
                        }
                    });
            this.progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            if (values.length > 0) {
                final int value = values[0];
                if (value == 1) {
                    this.progressDialog.setMessage("正在验证帐号信息...");
                }
            }
        }

    }

    private static final int REQUEST_CODE_REGISTER = 0;
    public static final String TAG = LoginPage.class.getSimpleName();

    private LoginPage mContext;

    private boolean destroyed;

    private int page;
    private static final String USERNAME = "username";

    private static final String PASSWORD = "password";
    private EditText editUsername;

    private EditText editPassword;
    private Button mButtonSignin;

    private ActionBar mActionBar;
    private String username;

    private String password;

    private void doLogin() {
        if (TextUtils.isEmpty(this.username)
                || TextUtils.isEmpty(this.password)) {
            CommonHelper.notify(this.mContext, "密码和帐号不能为空");
        } else {
            CommonHelper.hideKeyboard(this, this.editPassword);
            new LoginTask().execute();
        }
    }

    // private static class RegisterAction extends AbstractAction {
    // Activity mContext;
    //
    // public RegisterAction(Activity context) {
    // super(R.drawable.ic_register);
    // mContext = context;
    // }
    //
    // @Override
    // public void performAction(View view) {
    // goRegisterPage(mContext);
    // }
    // }

    // private static void goRegisterPage(Activity context) {
    // Intent intent = new Intent(context, RegisterPage.class);
    // context.startActivityForResult(intent, REQUEST_CODE_REGISTER);
    // }

    private void init() {
        this.mContext = this;
        CommonHelper.initScreenConfig(this);
    }

    public void log(final String message) {
        Log.i(LoginPage.TAG, message);
    }

    @Override
    protected void onActivityResult(final int requestCode,
            final int resultCode, final Intent data) {
        if ((resultCode == Activity.RESULT_OK)
                && (requestCode == LoginPage.REQUEST_CODE_REGISTER)) {
            this.editUsername.setText(data.getStringExtra("email"));
            this.editPassword.setText(data.getStringExtra("password"));
            this.page = data.getIntExtra(Constants.EXTRA_PAGE, 0);
            new LoginTask().execute();
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
        // case R.id.button_register:
        // goRegisterPage(mContext);
        // break;
        case R.id.button_signin:
            doLogin();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        setLayout();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.destroyed = true;
    }

    @Override
    protected void onPause() {
        AppContext.active = false;
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle state) {
        super.onRestoreInstanceState(state);
        this.editUsername.setText(state.getString(LoginPage.USERNAME));
        Selection.setSelection(this.editUsername.getText(), this.editUsername
                .getText().length());
        this.editPassword.setText(state.getString(LoginPage.PASSWORD));
        Selection.setSelection(this.editPassword.getText(), this.editPassword
                .getText().length());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppContext.active = true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(LoginPage.USERNAME, this.username);
        state.putString(LoginPage.PASSWORD, this.password);
    }

    private void setLayout() {
        setContentView(R.layout.login);

        this.mActionBar = (ActionBar) findViewById(R.id.actionbar);
        this.mActionBar.setTitle(getString(R.string.app_name));
        this.editUsername = (EditText) findViewById(R.id.login_username);
        this.editUsername.addTextChangedListener(new TextChangeListener() {
            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                LoginPage.this.username = s.toString();
            }
        });
        this.editPassword = (EditText) findViewById(R.id.login_password);
        this.editPassword.addTextChangedListener(new TextChangeListener() {

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                LoginPage.this.password = s.toString();
            }
        });
        this.editPassword
                .setOnEditorActionListener(new OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(final TextView v,
                            final int actionId, final KeyEvent event) {
                        if (AppContext.DEBUG) {
                            Log.d(LoginPage.TAG, "actionId=" + actionId
                                    + " KeyEvent=" + event);
                        }
                        if (actionId == EditorInfo.IME_ACTION_SEND) {
                            doLogin();
                            return true;
                        }
                        return false;
                    }
                });

        // mButtonRegister = (Button) findViewById(R.id.button_register);
        // mButtonRegister.setOnClickListener(this);

        this.mButtonSignin = (Button) findViewById(R.id.button_signin);
        this.mButtonSignin.setOnClickListener(this);

    }

}
