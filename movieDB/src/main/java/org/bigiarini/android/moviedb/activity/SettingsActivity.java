package org.bigiarini.android.moviedb.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.bigiarini.android.moviedb.R;
import org.bigiarini.android.moviedb.model.ServerModel;
import org.bigiarini.android.moviedb.service.TestConnectionService;

/**
 * Created by emanuele on 26/10/13.
 */
public class SettingsActivity extends Activity {

    private static final String TAG_LOG = SettingsActivity.class.getName();

    public static final String SETTINGS_ACTION = "org.bigiarini.android.moviedb.actions.SETTINGS_ACTION";
    public static final String SERVER_DATA_EXTRA = "org.bigiarini.android.moviedb.extra.SERVER_DATA_EXTRA";

    private EditText mServerEditText;
    private EditText mPortEditText;
    private TextView mErrorTextView;

    private TestServerAsyncTask mTestServerAsyncTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mServerEditText = (EditText) findViewById(R.id.server_edittext);
        mErrorTextView = (TextView) findViewById(R.id.error_textview);
        mPortEditText = (EditText) findViewById(R.id.port_edittext);
        ServerModel servermodel = ServerModel.load(this);
        if (servermodel != null) {
            mServerEditText.setText(servermodel.getAddress());
            mPortEditText.setText(Integer.toString(servermodel.getPort()));
        }
        //ActionBar actionBar = getSupport
    }

    public void saveSettings(View savesettingsButton) {

        mErrorTextView.setVisibility(View.INVISIBLE);

        final Editable serverEdit = mServerEditText.getText();
        final Editable portEdit = mPortEditText.getText();
        if (TextUtils.isEmpty(serverEdit)) {
            final String serverMandatoryString = getResources().getString(R.string.serverMandatory);
            Log.w(TAG_LOG,serverMandatoryString);
            mErrorTextView.setText(serverMandatoryString);
            mErrorTextView.setVisibility(View.VISIBLE);
            return;
        }
        if (TextUtils.isEmpty(portEdit) || !isInteger(portEdit)) {
            final String portMandatoryString = getResources().getString(R.string.portMandatory);
            Log.w(TAG_LOG,portMandatoryString);
            mErrorTextView.setText(portMandatoryString);
            mErrorTextView.setVisibility(View.VISIBLE);
            return;
        }
        final String serverAddress = serverEdit.toString();
        final int portNumber = Integer.parseInt(portEdit.toString());

        mTestServerAsyncTask = new TestServerAsyncTask();
        mTestServerAsyncTask.execute(ServerModel.create(serverAddress).withPort(portNumber));
    }

    private boolean isInteger(Editable edit) {
        boolean result;
        String text = edit.toString();
        try {
            int number = Integer.parseInt(text);
            result = true;
        }
        catch (NumberFormatException e) {
            result = false;
        }
        return  result;
    }

    private class TestServerAsyncTask extends AsyncTask<ServerModel, Void, ServerModel> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG_LOG,"Testing connection to the server.");
        }

        @Override
        protected ServerModel doInBackground(ServerModel... params) {
            final ServerModel returnModel = TestConnectionService.get().testServer(getApplicationContext(), params[0].getAddress(), params[0].getPort());
            return returnModel;
        }

        @Override
        protected void onPostExecute(ServerModel serverModel) {
            super.onPostExecute(serverModel);
            if (serverModel != null) {
                if (serverModel.state().isGood()) {
                    Intent intentResult = new Intent();
                    intentResult.putExtra(SERVER_DATA_EXTRA, serverModel);
                    setResult(RESULT_OK, intentResult);
                    finish();
                }
                else  {
                    mErrorTextView.setText(serverModel.state().errorMessage());
                    mErrorTextView.setVisibility(View.VISIBLE);
                }
            }
            else {
                mErrorTextView.setText("Error!");
                mErrorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

}