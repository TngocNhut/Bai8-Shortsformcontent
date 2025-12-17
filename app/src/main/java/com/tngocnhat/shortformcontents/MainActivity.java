package com.tngocnhat.shortformcontents;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private VideoView videoView;
    private TextView tvLikeCount, tvDislikeCount, tvVideoDescription;
    private FrameLayout btnLike, btnDislike;
    private GestureDetector gestureDetector;
    private VideoManager videoManager;
    private Video currentVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar for immersive experience
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        initializeViews();
        setupGestureDetector();
        checkPermissionsAndLoad();
    }

    private void initializeViews() {
        videoView = findViewById(R.id.videoView);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvDislikeCount = findViewById(R.id.tvDislikeCount);
        tvVideoDescription = findViewById(R.id.tvVideoDescription);
        btnLike = findViewById(R.id.btnLike);
        btnDislike = findViewById(R.id.btnDislike);
        View btnProfile = findViewById(R.id.btnProfileTop);

        videoManager = VideoManager.getInstance();

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnLike.setOnClickListener(v -> handleLike());
        btnDislike.setOnClickListener(v -> handleDislike());
    }

    private void checkPermissionsAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            // For Android 13+ (Tiramisu), use READ_MEDIA_VIDEO
            if (Build.VERSION.SDK_INT >= 33) {
                permission = "android.permission.READ_MEDIA_VIDEO";
            }

            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { permission }, PERMISSION_REQUEST_CODE);
            } else {
                loadVideos();
            }
        } else {
            loadVideos();
        }
    }

    private void loadVideos() {
        videoManager.loadVideos(this);
        if (videoManager.hasVideos()) {
            Toast.makeText(this, "Loaded " + videoManager.getInstance().getPlaylistSize() + " videos.",
                    Toast.LENGTH_SHORT).show();
            playVideo(videoManager.getCurrentVideo());
        } else {
            Toast.makeText(this, "No videos found in /Videos folder or res/raw", Toast.LENGTH_LONG).show();
        }
    }

    private void playVideo(Video video) {
        if (video == null)
            return;
        currentVideo = video;
        updateUI();

        // Check if path is a URI (starts with android.resource) or a file path
        if (video.getPath().startsWith("android.resource://")) {
            videoView.setVideoURI(Uri.parse(video.getPath()));
        } else {
            videoView.setVideoPath(video.getPath());
        }

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = videoView.getWidth() / (float) videoView.getHeight();
            float scale = videoRatio / screenRatio;
            if (scale >= 1f) {
                videoView.setScaleX(scale);
            } else {
                videoView.setScaleY(1f / scale);
            }
            videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void updateUI() {
        if (currentVideo == null)
            return;

        tvVideoDescription.setText(currentVideo.getTitle());
        tvLikeCount.setText(String.valueOf(currentVideo.getLikeCount()));
        tvDislikeCount.setText(String.valueOf(currentVideo.getDislikeCount()));

        // Update Like Button Visuals
        if (currentVideo.isLiked()) {
            btnLike.setBackgroundResource(R.drawable.bg_button_like);
        } else {
            btnLike.setBackgroundResource(R.drawable.bg_button_neutral);
        }

        // Update Dislike Button Visuals
        if (currentVideo.isDisliked()) {
            btnDislike.setBackgroundResource(R.drawable.bg_button_dislike);
        } else {
            btnDislike.setBackgroundResource(R.drawable.bg_button_neutral);
        }
    }

    private void handleLike() {
        if (currentVideo == null)
            return;

        if (currentVideo.isLiked()) {
            // Un-like
            currentVideo.setLiked(false);
            currentVideo.setLikeCount(currentVideo.getLikeCount() - 1);
        } else {
            // Like
            currentVideo.setLiked(true);
            currentVideo.setLikeCount(currentVideo.getLikeCount() + 1);

            // If was disliked, un-dislike
            if (currentVideo.isDisliked()) {
                currentVideo.setDisliked(false);
                currentVideo.setDislikeCount(currentVideo.getDislikeCount() - 1);
            }
        }
        updateUI();
    }

    private void handleDislike() {
        if (currentVideo == null)
            return;

        if (currentVideo.isDisliked()) {
            // Un-dislike
            currentVideo.setDisliked(false);
            currentVideo.setDislikeCount(currentVideo.getDislikeCount() - 1);
        } else {
            // Dislike
            currentVideo.setDisliked(true);
            currentVideo.setDislikeCount(currentVideo.getDislikeCount() + 1);

            // If was liked, un-like
            if (currentVideo.isLiked()) {
                currentVideo.setLiked(false);
                currentVideo.setLikeCount(currentVideo.getLikeCount() - 1);
            }
        }
        updateUI();
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null)
                    return false;

                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaY) > 100 && Math.abs(velocityY) > 100) {
                    if (deltaY < 0) {
                        // Swipe Up -> Next Video
                        playVideo(videoManager.getNextVideo());
                    } else {
                        // Swipe Down -> Previous Video
                        playVideo(videoManager.getPreviousVideo());
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });

        videoView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                Toast.makeText(this, "Permission denied. Only checking for built-in app videos.", Toast.LENGTH_SHORT)
                        .show();
                loadVideos(); // Still try to load internal videos
            }
        }
    }
}
