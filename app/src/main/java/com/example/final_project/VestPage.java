package com.example.final_project;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.final_project.databinding.ActivityMainBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class VestPage extends AppCompatActivity {

    private ActivityMainBinding mainBinding;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private ImageView imageView;
    private File imageFile;
    private File fotoVest;
    private static final int MY_PERMISSIONS_REQUEST_WRITE = 223;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.vest_page);

        String fotoHelmPath = getIntent().getStringExtra("fotoHelmPath");

        Button next = findViewById(R.id.btnLanjutkan);
        Button capture = findViewById(R.id.bCapturePhoto);
        Button retake = findViewById(R.id.retake);
        imageView = findViewById(R.id.capturedImageView);

        next.setOnClickListener(view -> {
            Intent intent = new Intent(VestPage.this, BootsPage.class);
            intent.putExtra("fotoHelmPath", fotoHelmPath);
            intent.putExtra("fotoVestPath", fotoVest.getAbsolutePath());
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        capture.setOnClickListener(view -> capturePhoto());

        retake.setOnClickListener(view -> retakePhoto());

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                startCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        try {
            imageFile = createImageFile();
            fotoVest = imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Uri uriSavedImage = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(imageFile).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                displayCapturedImage(uriSavedImage);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
                Toast.makeText(VestPage.this, "Gagal mengambil gambar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void displayCapturedImage(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

            Button capture = findViewById(R.id.bCapturePhoto);
            capture.setVisibility(View.GONE);

            Button retake = findViewById(R.id.retake);
            retake.setVisibility(View.VISIBLE);

            androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
            previewView.setVisibility(View.GONE);

            Button next = findViewById(R.id.btnLanjutkan);
            next.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCamera(ProcessCameraProvider cameraProvider) {
//        if (imageFile != null && imageFile.exists()) {
//            boolean deleted = imageFile.delete();
//            if (deleted) {
//                Toast.makeText(this, "Foto sebelumnya dihapus", Toast.LENGTH_SHORT).show();
//            }
//        }

        askWritePermission();

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        Preview preview = new Preview.Builder().build();
        androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void retakePhoto() {
        startCamera(cameraProvider);

        imageView.setVisibility(View.GONE);

        Button capture = findViewById(R.id.bCapturePhoto);
        capture.setVisibility(View.VISIBLE);

        Button retake = findViewById(R.id.retake);
        retake.setVisibility(View.GONE);

        androidx.camera.view.PreviewView previewView = findViewById(R.id.pvPreview);
        previewView.setVisibility(View.VISIBLE);

        Button next = findViewById(R.id.btnLanjutkan);
        next.setVisibility(View.GONE);

    }

    private void askWritePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int cameraPermission = this.checkSelfPermission(Manifest.permission.CAMERA);
            int writePermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_WRITE);
            }

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin diberikan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Izin diperlukan untuk menyimpan foto", Toast.LENGTH_SHORT).show();
            }
        }
    }
}