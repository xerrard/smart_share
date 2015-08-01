package com.huaqin.wirelessfiletransfer.activity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.huaqin.wirelessfiletransfer.MainApplication;
import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.model.Const;

public class WirelessTransferHistory extends Activity implements
        OnCreateContextMenuListener, OnItemClickListener {

    private ListView mListView;
    private WirelessOppTransferAdapter mTransferAdapter;
    private List<WirelessTransferHistoryItem> mList;
    MainApplication app;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    private int mContextMenuPosition;
    private static ClearHistory clearHistory;
    private int dir;
    NotificationManager mNotificationManager;

    class HistoryUpdateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.ACTION_UPDATE_HISTORY)) {
                mTransferAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MainApplication) getApplication();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        setContentView(R.layout.wireless_transfers_page);
        Intent in = getIntent();
        dir = in.getIntExtra("direction", 0);
        if (dir == Const.DIRECTION_OUTBOUND) {
            setTitle(getText(R.string.outbound_history_title));
            mList = app.mFileTransferSendHistorylist;
        }
        else {
            setTitle(getText(R.string.inbound_history_title));
            mList = app.mFileTransferReceiveHistorylist;
        }
        mListView = (ListView) findViewById(R.id.list);
        mListView.setEmptyView(findViewById(R.id.empty));

        // Create a list "controller" for the data
        mTransferAdapter = new WirelessOppTransferAdapter(this,
                R.layout.wireless_transfer_item, mList);
        mListView.setAdapter(mTransferAdapter);
        mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);

        receiver = new HistoryUpdateBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_UPDATE_HISTORY);
        registerReceiver(receiver, intentFilter);

        if (clearHistory != null
                && clearHistory.getStatus() != AsyncTask.Status.FINISHED) {
            clearHistory.showProgress(this);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        if (clearHistory != null) {
            clearHistory.dismissProgress();
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long arg3) {

        mTransferAdapter.notifyDataSetChanged();
        String content = getString(R.string.noti_caption, mList.size());
        if (dir == Const.DIRECTION_OUTBOUND) {
            Notification notification = app.mNotifacionList.get(0)
                    .setContentText(content).build();
            mNotificationManager.notify(Const.NOTIFICATION_SEND, notification);
        }
        else {
            openCompleteTransfer(position);
            Notification notification = app.mNotifacionList.get(1)
                    .setContentText(content).build();
            mNotificationManager.notify(Const.NOTIFICATION_RECEIVE,
                    notification);
        }
        //mList.remove(position);

    }

    /**
     * Open the selected finished transfer. mDownloadCursor must be moved to
     * appropriate position before calling this function
     */
    private void openCompleteTransfer(int position) {
        WirelessTransferHistoryItem item = mList.get(position);
        File f = item.getFile();

        if (!f.exists()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.not_exist_file))
                    .setMessage(R.string.not_exist_file_desc)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                }
                            }).create();

            mTransferAdapter.notifyDataSetChanged();
        }
        else {
            Uri uri = Uri.fromFile(f);
            String mimetype = null;
            try {
//                mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
//                        MimeTypeMap.getFileExtensionFromUrl(f
//                                .getCanonicalPath())); 
            	String suffix = "";
                String name = f.getName();
                int idx = name.lastIndexOf(".");
                if (idx > 0) {
                    suffix = name.substring(idx + 1);
                }
            	mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(suffix);
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (isRecognizedFileType(this, uri, mimetype)) {
                Intent activityIntent = new Intent(Intent.ACTION_VIEW);
                activityIntent.setDataAndTypeAndNormalize(uri, mimetype);

                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(activityIntent);
                }
                catch (ActivityNotFoundException ex) {
                }
            }
            else {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.unknown_file))
                        .setMessage(R.string.unknown_file_desc)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                    }
                                }).create().show();
                ;
            }
        }

    }

    /**
     * To judge if the file type supported (can be handled by some app) by phone
     * system.
     */
    public static boolean isRecognizedFileType(Context context, Uri fileUri,
            String mimetype) {
        boolean ret = true;

        Intent mimetypeIntent = new Intent(Intent.ACTION_VIEW);
        mimetypeIntent.setDataAndTypeAndNormalize(fileUri, mimetype);
        List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(mimetypeIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);

        if (list.size() == 0) {
            ret = false;
        }
        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.transferhistory, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showClear = mList.size() > 0;
        menu.findItem(R.id.transfer_menu_clear_all).setEnabled(showClear);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transfer_menu_clear_all:
                promptClearList();
                return true;
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transfer_menu_open:
                openCompleteTransfer(mContextMenuPosition);
                mTransferAdapter.notifyDataSetChanged();
                return true;

            case R.id.transfer_menu_clear:
                mList.remove(mContextMenuPosition);
                mTransferAdapter.notifyDataSetChanged();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        mContextMenuPosition = info.position;

        String fileName = mList.get(mContextMenuPosition).getFilename();

        menu.setHeaderTitle(fileName);

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.transferhistorycontextfinished, menu);

        if (dir == Const.DIRECTION_OUTBOUND) {
            menu.findItem(R.id.transfer_menu_open).setVisible(false);
        }

    }

    /**
     * Prompt the user if they would like to clear the transfer history
     */
    private void promptClearList() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.transfer_clear_dlg_title)
                .setMessage(R.string.transfer_clear_dlg_msg)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {

                                if (clearHistory == null) {
                                    clearHistory = new ClearHistory();
                                    clearHistory.execute(0);
                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public class WirelessOppTransferAdapter extends BaseAdapter {
        Context context;

        public WirelessOppTransferAdapter(Context context,
                int wirelessTransferItem,
                List<WirelessTransferHistoryItem> mList) {
            this.context = context;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.wireless_transfer_item, null);
                convertView.setTag(convertView);
            }
            else {
                convertView = (View) convertView.getTag();
            }

            WirelessTransferHistoryItem item = mList.get(position);
            ImageView transfer_icon = (ImageView) convertView
                    .findViewById(R.id.transfer_icon);
            TextView transfer_title = (TextView) convertView
                    .findViewById(R.id.transfer_title);
            TextView targetdevice = (TextView) convertView
                    .findViewById(R.id.targetdevice);
            TextView complete_date = (TextView) convertView
                    .findViewById(R.id.complete_date);
            TextView complete_text = (TextView) convertView
                    .findViewById(R.id.complete_text);

            transfer_title.setText(item.getFilename());
            targetdevice.setText(item.getDeviceName());
            complete_date.setText(item.getCurrentTime());
            if (dir == Const.DIRECTION_INBOUND) {
                transfer_icon
                        .setImageResource(android.R.drawable.stat_sys_download_done);
                complete_text.setText(item.getFilesize() + "  "
                        + context.getString(R.string.download_success));
            }
            else {
                transfer_icon
                        .setImageResource(android.R.drawable.stat_sys_upload_done);
                complete_text.setText(item.getFilesize() + "  "
                        + context.getString(R.string.upload_success));
            }

            return convertView;
        }

    }

    class ClearHistory extends AsyncTask<Integer, Integer, Boolean> {

        private ProgressDialog mDialog;

        public void showProgress(Activity activity) {
            mDialog = new ProgressDialog(activity);
            mDialog.setMessage(getString(R.string.transfer_menu_clear_all)
                    + "...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.show();

        }

        public void dismissProgress() {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            mDialog = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(WirelessTransferHistory.this);
        }

        @Override
        protected Boolean doInBackground(Integer... arg0) {
            // TODO Auto-generated method stub
            mList.clear();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dismissProgress();
            clearHistory = null;
            mTransferAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }

    public static class WirelessTransferHistoryItem {

        private File file = null;
        private String deviceName = null;
        private int roleIconId = 0;
        private String currentTime = null;
        private String filesizeString = null;
        private String filename = null;

        public WirelessTransferHistoryItem(File file, String deviceName,
                int roleIconId, String currentTime, String filesizeString) {
            super();
            this.file = file;
            this.deviceName = deviceName;
            this.roleIconId = roleIconId;
            this.currentTime = currentTime;
            this.filesizeString = filesizeString;
        }

        public WirelessTransferHistoryItem(String filename, String deviceName,
                int roleIconId, String currentTime, String filesizeString) {
            super();
            this.filename = filename;
            this.deviceName = deviceName;
            this.roleIconId = roleIconId;
            this.currentTime = currentTime;
            this.filesizeString = filesizeString;
        }

        public String getFilename() {
            if (filename != null) {
                return filename;
            }
            else if (file != null) {
                return file.getName();
            }
            else {
                return null;
            }
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFilesize() {
            return filesizeString;
        }

        public void setFilesize(String filesize) {
            this.filesizeString = filesize;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public int getRoleIconId() {
            return roleIconId;
        }

        public void setRoleIconId(int roleIconId) {
            this.roleIconId = roleIconId;
        }

        public String getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(String currentTime) {
            this.currentTime = currentTime;
        }

    }

}

