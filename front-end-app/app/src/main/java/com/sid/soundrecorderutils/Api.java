package com.sid.soundrecorderutils;

import android.graphics.Bitmap;
import android.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.sid.soundrecorderutils.Util.*;

public class Api {
    public static int saveTime(String time){
        Map<String, String> map = new HashMap<>();
        map.put("clock",time);
        return send(map2json(map),"/clock").second;
    }

    public static Bitmap getPicture() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final Bitmap[] pic = {null};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    pic[0] = sended(map2json(map), "/pic").first;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pic[0];
    }

    public static String sendPDF(File file){
        Pair<String, Integer> pdf=null;

        try {
            pdf = sendFile("file", file, "/pdf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return pdf.first;
    }

    public static String sendMP4(File file){
        Pair<String, Integer> mp4=null;

        try {
            mp4 = sendFile("file", file, "/mp4");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return mp4.first;
    }

    public static String sendFileToBaidu(String path){
        String file=null;
        file = store(path);
        return file;
    }


    public static String sendTestFile(File file){
        Pair<String, Integer> test=null;

        try {
            test = sendFile("file", file, "/test");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return test.first;
    }
}