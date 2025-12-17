package com.tngocnhat.shortformcontents;

import android.os.Environment;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoManager {
    private static VideoManager instance;
    private List<Video> playlist;
    private int currentIndex;

    private VideoManager() {
        playlist = new ArrayList<>();
        currentIndex = -1;
    }

    public static synchronized VideoManager getInstance() {
        if (instance == null) {
            instance = new VideoManager();
        }
        return instance;
    }

    public void loadVideos(android.content.Context context) {
        playlist.clear();
        boolean foundExternal = false;

        // 1. Try External Storage
        File videoDir = new File(Environment.getExternalStorageDirectory(), "Videos");
        if (videoDir.exists() && videoDir.isDirectory()) {
            File[] files = videoDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp4") ||
                    name.toLowerCase().endsWith(".mkv") ||
                    name.toLowerCase().endsWith(".3gp"));

            if (files != null && files.length > 0) {
                foundExternal = true;
                for (File file : files) {
                    if (file.length() < 50 * 1024 * 1024) { // < 50MB
                        playlist.add(new Video(file.getAbsolutePath(), file.getName()));
                    }
                }
            }
        }

        // 2. If no external videos, load from res/raw
        // 2. If no external videos, load from res/raw
        if (!foundExternal) {
            try {
                java.lang.reflect.Field[] fields = R.raw.class.getFields();

                for (java.lang.reflect.Field field : fields) {
                    String name = field.getName();
                    int resId = field.getInt(null);

                    // Copy raw resource to cache file for reliable playback
                    File cacheFile = copyResourceToCache(context, resId, name + ".mp4");
                    if (cacheFile != null) {
                        playlist.add(new Video(cacheFile.getAbsolutePath(), "Demo: " + name));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Shuffle for random playback
        Collections.shuffle(playlist);
        currentIndex = 0;
    }

    public Video getCurrentVideo() {
        if (playlist.isEmpty())
            return null;
        if (currentIndex < 0 || currentIndex >= playlist.size())
            currentIndex = 0;
        return playlist.get(currentIndex);
    }

    public Video getNextVideo() {
        if (playlist.isEmpty())
            return null;
        currentIndex++;
        if (currentIndex >= playlist.size()) {
            currentIndex = 0; // Loop back to start
        }
        return playlist.get(currentIndex);
    }

    public Video getPreviousVideo() {
        if (playlist.isEmpty())
            return null;
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = playlist.size() - 1; // Loop to end
        }
        return playlist.get(currentIndex);
    }

    public boolean hasVideos() {
        return !playlist.isEmpty();
    }

    public int getPlaylistSize() {
        return playlist.size();
    }

    private File copyResourceToCache(android.content.Context context, int resId, String filename) {
        try {
            java.io.InputStream in = context.getResources().openRawResource(resId);
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null)
                cacheDir = context.getCacheDir();

            File outFile = new File(cacheDir, filename);
            java.io.FileOutputStream out = new java.io.FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
