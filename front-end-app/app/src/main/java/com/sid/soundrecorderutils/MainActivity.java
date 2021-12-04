package com.sid.soundrecorderutils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Bundle;

import com.android.king.fileselector.FileSelector;
import com.android.king.fileselector.FileSelectorActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int count=0;

    private Button mBtnRecordAudio;
    private Button mBtnPlayAudio;
    private Button mBtnPDF;
    private Intent intent;
    private Thread thread;
    private boolean settings;
    private ImageView pdfView;
    ArrayList<Bitmap> bitmaps;
    private int page;
    private int capacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        intent = new Intent(this, RecordingService.class);
        mBtnRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (settings) {
                            count++;
                            count%=2;
                            start();
                            try {

                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            stop();
                            String s = Api.sendMP4(new File("storage/emulated/0/SoundRecorder/test"+((count+1)%2)+".mp4"));
                            Log.d("====================",s);
                            if (s.equals("next page")){
                                page=(page+1)==capacity?page:(page+1);
                                pdfView.setImageBitmap(bitmaps.get(page));
                            }
                        }
                    }
                });
                settings=true;
                thread.start();
            }
        });

        mBtnPlayAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings=false;
            }
        });
        final FileSelector.Builder builder = new FileSelector.Builder(this);
        mBtnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = builder.setFileRoot("")//初始路径  init file root
                        .setIsMultiple(true)//是否多选模式 whether is multiple select
                        .setMaxCount(1)//限定文件选择数 max file count
//                        .setFilters(filters)//筛选文件类型  file filter
                        .getIntent();
                startActivityForResult(intent, 100);
            }
        });
    }

    private ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
                Bitmap bitmap;
                final int pageCount = renderer.getPageCount();
                Log.e("test_sign", "图片de 张数： " +pageCount);
                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = renderer.openPage(i);
                    int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                    int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    //todo 以下三行处理图片存储到本地出现黑屏的问题，这个涉及到背景问题
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    Rect r = new Rect(0, 0, width, height);
                    page.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    bitmaps.add(bitmap);
                    // close the page
                    page.close();
                }
                // close the renderer
                renderer.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bitmaps;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100 && data != null) {
            ArrayList<String> pathList = data.getStringArrayListExtra(FileSelectorActivity.ACTIVITY_KEY_RESULT_PATHLIST);
            for (final String path : pathList) {
                Log.d("-------------", path);
                bitmaps = pdfToBitmap(new File(path));
                page=0;
                pdfView.setImageBitmap(bitmaps.get(page));
//                pdfView.fromFile(new File(path))
//                        .pages(0, 1) // all pages are displayed by default
//                        .enableSwipe(true) // allows to block changing pages using swipe
//                        .swipeHorizontal(false)
//                        .enableDoubletap(true)
//                        .defaultPage(0)
//                        .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
//                        .password(null)
//                        .scrollHandle(null)
//                        .enableAntialiasing(true) // improve rendering a little bit on low-res screens
//                        // spacing between pages in dp. To define spacing color, set view background
//                        .spacing(0)
//                        .autoSpacing(false) // add dynamic spacing to fit each page on its own on the screen
//                        .fitEachPage(false) // fit each page to the view, else smaller pages are scaled relative to largest page.
//                        .pageSnap(false) // snap pages to screen boundaries
//                        .pageFling(false) // make a fling change only a single page like ViewPager
//                        .nightMode(false) // toggle night mode
//                        .load();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String s = Api.sendPDF(new File(path));
                        Log.d("++++++++++++++", s);
                        capacity=Integer.parseInt(s);
                    }
                }).start();
            }
        }
    }

    private void start(){
//        Toast.makeText(this, "开始录音...", Toast.LENGTH_SHORT).show();
        File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
        if (!folder.exists()) {
            //folder /SoundRecorder doesn't exist, create the folder
            folder.mkdir();
        }
        this.startService(intent);
    }

    private void stop(){
//        Toast.makeText(this, "录音结束...", Toast.LENGTH_SHORT).show();

        this.stopService(intent);
    }

    private void initView() {
        mBtnRecordAudio = (Button)findViewById(R.id.main_btn_record_sound);
        mBtnPlayAudio = (Button) findViewById(R.id.main_btn_play_sound);
        mBtnPDF = (Button) findViewById(R.id.pdf_button);
        pdfView = findViewById(R.id.pdf_view);
    }
}
