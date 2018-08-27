package com.example.tornado.upgradeversion;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btnUpgrade;
    String Version="1.0";
    String VersionUrl = "http://android.gazrapor.ir/nikandroid/version.txt";

    //address file aok ke ghara ast upgrade shavad
    String file_url="http://android.gazrapor.ir/nikandroid/Upgrade.apk";

    ProgressDialog prgDialog;
    boolean permission=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnUpgrade=findViewById(R.id.btn_upgrade);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                 new CheckVer().execute(VersionUrl);
            }
        });
    }



    protected Dialog onCreateDialog(int id)
    {
        prgDialog=new ProgressDialog(this);
        prgDialog.setMessage("لطفا کمی صبر کنید  \n درحال بروز رسانی نرم افزار");
        prgDialog.setMax(100);
        prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prgDialog.show();
       return prgDialog;
    }

    private class UpdateApp extends AsyncTask<String,String,Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(0);
        }

        @Override
        protected Void doInBackground(String... params) {
           //niaz darim file apk ra download konim
            try {
                //ebteda check mikonim url vojud darad ya na
                URL url=new URL(params[0]);

                // agar url vojud dasht anra open mikonim
                HttpURLConnection c = (HttpURLConnection) url.openConnection();

                c.setRequestMethod("GET");
                c.connect();
                //ta i ja ba server ertebat ra bargharar kardim

                String PATH ="/mnt/sdcard/download";
                File file = new File(PATH);
                file.mkdir();

                String AppName = getString(R.string.app_name) + ".apk";
                File outputFile= new File(file,AppName);

                if(outputFile.exists())
                    outputFile.delete();//apk version ghadimi ra delete kon

                FileOutputStream fos= new FileOutputStream(outputFile);

                InputStream is=c.getInputStream();
                int lenght=c.getContentLength();
                byte[] buffer=new byte[1024];
                int len =0;

                int total=0;

                // -1 yani hameye info az internet khande shode yani download kamel shode ast
                while ((len = is.read(buffer)) !=-1){

                    //downloading...

                    //darsad bandi progress dialog horizintal
                    total +=len;
                     fos.write(buffer,0,len);

                     publishProgress(""+(int)((total*100)/lenght));
                }
                 fos.flush();;
                 fos.close();
                 is.close();

                 //ta inja tavanestim barnamye khod ra danload konim
                // hal bayad barname ra ejra konim

                Intent intent=new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile
                        (new File(Environment.getExternalStorageDirectory()+"/download/" + AppName)
                        ),"application/vnd.android.package-archive" );

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  null;
        }

        protected void onProgressUpdate(String... values)
        {
            prgDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            dismissDialog(0);
        }
    }




    private class CheckVer extends AsyncTask<String,Void,String>{

        private ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog=new ProgressDialog(MainActivity.this);
            dialog.setMessage("در حال ارتباط با سرور...");
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
                                     // params = address url
            try {
                String url=params[0];  //khaneye 0 havie VersionUrl maast

                //4 line zir check mikonad aya texti tahte onvan version.text(line 14) tuye server vojud darad ya no
                //va hamchenin ba in 4 dastur check mikardim aya hamchin urli(VersionUrl) line 28 vojud darad ya na

                HttpClient httpClient= new DefaultHttpClient();
                HttpContext localContext= new BasicHttpContext();
                HttpGet httpGet=new HttpGet(url);
                HttpResponse httpResponse=httpClient.execute(httpGet,localContext);

                //ta inja mohtaviat ra gereftim , agar mohtaviati vojud dasht dakhele buffer reader beriz

                            //reader havie text ruye server maast (version barname masalan 1.0)
                BufferedReader reader=new BufferedReader
                        (new InputStreamReader(httpResponse.getEntity().getContent()));

                //reader ra aval be char sepas be string tabdim mikonim,chun line 26 "1.0" string ast
                //pas reader ra niz bayad be string tabdil kinim ta betavanim 2 ta string ra ba ham compare konim

                char line[];
                line=reader.readLine().toCharArray();

                     // chun version ma shamele 3 character asa(1.0) 3 bar append kardim
                 String a = new StringBuilder().append(line[0]).append(line[1]).append(line[2]).toString();
                 //ta alan mohtaviti ke az server gereftim (shamele ye text ke havie version ast) ra darune String a rikhtim

                //hal bayad in String a ra ba line 26 (  String Version="1.0";) moghayese konim

                if(Version.equals(a)){
                    //agar line 26 mosavi bud ba String a ke az server gereftim leza nabayad upgrade surat pazirad

                    permission=false;
                }else{
                    //current version barname ba version server yeki nist va bayad upgrade surat pazirad
                    permission=true;
                }




            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();

            if(permission==true)
            {
                //current version barname ba version server yeki nist va bayad upgrade surat pazirad
                Toast.makeText(MainActivity.this, "برنامه به روز نیست.", Toast.LENGTH_SHORT).show();
                // dar line zire miguim amaliat upgrade ra anjam bede
                new UpdateApp().execute(file_url);
            }

            else
                Toast.makeText(MainActivity.this, "برنامه به روز است.", Toast.LENGTH_SHORT).show();

        }
    }
}
