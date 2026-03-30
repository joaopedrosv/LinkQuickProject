package com.example.youtubedownloader.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.youtubedownloader.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextInputEditText urlET;
    LinearLayout containerQualidades, cardVideo;
    ImageView thumbnail;
    TextView titleTV, titleApp;
    ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlET = findViewById(R.id.urlET);
        containerQualidades = findViewById(R.id.containerQualidades);
        thumbnail = findViewById(R.id.thumbnail);
        titleTV = findViewById(R.id.titleTV);
        loading = findViewById(R.id.loading);
        cardVideo = findViewById(R.id.cardVideo);
        titleApp = findViewById(R.id.titleApp);

        animarEntrada();

        MaterialButton btn = findViewById(R.id.download);

        btn.setOnClickListener(v -> {

            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() ->
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                    );

            String url = urlET.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(this, "Cole uma URL", Toast.LENGTH_SHORT).show();
                return;
            }

            carregarEstado();
            buscarVideo(url);
        });
    }

    private void animarEntrada() {
        titleApp.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void carregarEstado() {
        loading.setVisibility(View.VISIBLE);
        cardVideo.setVisibility(View.GONE);
        containerQualidades.removeAllViews();
    }

    private void buscarVideo(String url) {

        new Thread(() -> {
            try {

                String apiUrl = "http://192.168.15.169:5000/info?url=" + url;

                java.net.HttpURLConnection conn =
                        (java.net.HttpURLConnection) new java.net.URL(apiUrl).openConnection();

                java.io.BufferedReader reader =
                        new java.io.BufferedReader(
                                new java.io.InputStreamReader(conn.getInputStream())
                        );

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                runOnUiThread(() -> sucesso(response.toString()));

            } catch (Exception e) {
                runOnUiThread(this::erroEstado);
            }
        }).start();
    }

    private void sucesso(String json) {

        loading.setVisibility(View.GONE);
        cardVideo.setVisibility(View.VISIBLE);

        try {
            JSONObject obj = new JSONObject(json);

            String thumb = obj.getString("thumbnail");
            String title = obj.getString("title");
            JSONArray formats = obj.getJSONArray("formats");

            titleTV.setText(title);
            Glide.with(this).load(thumb).into(thumbnail);

            cardVideo.setAlpha(0f);
            cardVideo.animate().alpha(1f).setDuration(400).start();

            HashMap<Integer, String> map = new HashMap<>();

            for (int i = 0; i < formats.length(); i++) {
                JSONObject f = formats.getJSONObject(i);

                int q = f.getInt("quality");
                String u = f.getString("url");

                if (q >= 360 && !map.containsKey(q)) {
                    map.put(q, u);
                }
            }

            List<Integer> lista = new ArrayList<>(map.keySet());
            Collections.sort(lista, Collections.reverseOrder());

            for (Integer q : lista) {

                String videoUrl = map.get(q);

                MaterialButton b = new MaterialButton(this);
                b.setText("Baixar " + q + "p");

                b.setTextColor(getResources().getColor(R.color.textPrimary));
                b.setCornerRadius(20);
                b.setStrokeWidth(1);
                b.setStrokeColorResource(R.color.accent);
                b.setBackgroundTintList(getResources().getColorStateList(R.color.card));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 20, 0, 0);
                b.setLayoutParams(params);

                b.setOnClickListener(v -> baixar(videoUrl));

                // animação estilo Nubank
                b.setAlpha(0f);
                b.setTranslationY(40);
                b.animate()
                        .alpha(1f)
                        .translationY(0)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .start();

                containerQualidades.addView(b);
            }

        } catch (Exception e) {
            erroEstado();
        }
    }

    private void erroEstado() {
        loading.setVisibility(View.GONE);
        Toast.makeText(this, "Erro ao processar vídeo", Toast.LENGTH_LONG).show();
    }

    private void baixar(String url) {

        Uri uri = Uri.parse(url);

        String nome = "video_" + System.currentTimeMillis() + ".mp4";

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Baixando vídeo");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                nome
        );

        DownloadManager manager =
                (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        manager.enqueue(request);

        Toast.makeText(this, "Download iniciado", Toast.LENGTH_SHORT).show();
    }
}