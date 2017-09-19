package com.logicshore.apkupdate;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;


public class MainActivity extends AppCompatActivity {
    ProgressDialog pd;
    TextView update;
    int totalSize = 0;
    int downloadedSize = 0;
    ProgressBar progress;
    TextView cur_val;
    //ftp details
    String server = "203.217.147.83";
    String username = "administrator";
    String password = "uiGHnm34Fd" ;
    Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        update=(TextView)findViewById(R.id.version);
       Button updatebutton=(Button)findViewById(R.id.buttonupdate);
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), 0);
            int version = info.versionCode;
            update.setText("VER:"+version);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

        }

        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            new UpdateApk().execute();
            }
        });



    }

    private class UpdateApk extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            FTPClient ftpClient = new FTPClient();

            try {
                ftpClient.connect(server,21);//21 is the port
                ftpClient.login(username, password);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setBufferSize(1024*1024*2);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                //create a directory
                File folder = new File(Environment.getExternalStorageDirectory()+ File.separator + "update APK");
                boolean path_flg = true;
                if (!folder.exists()) {
                    path_flg = folder.mkdirs();
                }else{
                    folder.delete();
                    path_flg = folder.mkdirs();
                }
                if (path_flg) {
                    Log.i("New Path ::", "Created");
                } else {
                    Log.i("Old Path ::", "Existing");
                }
                //file path from FTP
                File downloadFile1 = new File("/sdcard/update APK/APKUpdate.apk");
                String remoteFile1 = "APKUpdate.apk";

                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile1));
                boolean success = ftpClient.retrieveFile(remoteFile1, outputStream);

                FileOutputStream fileOutput = new FileOutputStream(downloadFile1);
                InputStream inputStream = ftpClient.retrieveFileStream(remoteFile1);

                if (inputStream == null || ftpClient.getReplyCode() == 550) {
                    fileOutput.close();
                    outputStream.close();
                   // showToast("Your App is Upto Date, no need to Update...!");

                    Toast.makeText(getApplicationContext(),"Your App is Upto Date, no need to Update...!",Toast.LENGTH_SHORT).show();
                }else{

                    totalSize = remoteFile1.length();

                    Log.d("total size",totalSize+"");

                    runOnUiThread(new Runnable() {
                        public void run() {

                            showProgress();
                            progress.setMax(totalSize);
                        }
                    });

                    //create a buffer...
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;

                    while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        // update the progressbar //
                        runOnUiThread(new Runnable() {
                            public void run() {
                                progress.setProgress(downloadedSize);
                                float per = ((float)downloadedSize/totalSize) * 100;

                                // cur_val.setText("Downloaded " + downloadedSize/1024*1024 + "MB / " + totalSize + "MB (" + ((int)per /1024*1024)/100 + "%)" );
                                cur_val.setText((int)per/1000000 + "%");
                            }
                        });
                    }
                    fileOutput.close();
                    outputStream.close();


                    if (success) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        System.out.println("File #1 has been downloaded successfully.");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/update APK/" + "APKUpdate.apk")), "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        protected void publishProgress(String... progress) {

        }
    }

    private void showProgress() {
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.myprogressdialog);
        dialog.setTitle("Download Progress");
        dialog.setCancelable(false);


        TextView text = (TextView) dialog.findViewById(R.id.tv1);
        text.setText("Downloading file ... ");
        cur_val = (TextView) dialog.findViewById(R.id.cur_pg_tv);
        cur_val.setText("It may Take Few Minutes.....");
        dialog.show();

        progress = (ProgressBar)dialog.findViewById(R.id.progress_bar);
        progress.setProgress(0);//initially progress is 0
        progress.setMax(totalSize);
        progress.setIndeterminate(true);

    }

}
