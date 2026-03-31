package com.example.youtubedownloader.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    TextInputEditText urlET;
    LinearLayout containerQualidades, cardVideo;
    ImageView thumbnail, userIcon;
    TextView titleTV, titleApp;
    ProgressBar loading;

    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;

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
        userIcon = findViewById(R.id.userIcon);

        auth = FirebaseAuth.getInstance();

        // 🔥 CONFIG GOOGLE LOGIN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("541150820222-3a2up7bctt5r84im0logl5jv7n70m4ai.apps.googleusercontent.com")
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        animarEntrada();
        atualizarUsuario();

        userIcon.setOnClickListener(v -> mostrarModalUsuario());

        MaterialButton btn = findViewById(R.id.download);

        btn.setOnClickListener(v -> {

            String url = urlET.getText().toString().trim();

            if (url.isEmpty()) {
                Toast.makeText(this, "Cole uma URL", Toast.LENGTH_SHORT).show();
                return;
            }

            carregarEstado();
            buscarVideo(url);
        });
    }

    // 🔥 MODAL
    private void mostrarModalUsuario() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_user, null);
        dialog.setContentView(view);

        ImageView photo = view.findViewById(R.id.userPhoto);
        TextView name = view.findViewById(R.id.userName);
        TextView email = view.findViewById(R.id.userEmail);
        MaterialButton loginBtn = view.findViewById(R.id.loginBtn);
        MaterialButton logoutBtn = view.findViewById(R.id.logoutBtn);

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            name.setText(user.getDisplayName());
            email.setText(user.getEmail());

            loginBtn.setVisibility(View.GONE);
            logoutBtn.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .into(photo);

            logoutBtn.setOnClickListener(v -> {
                auth.signOut();
                googleSignInClient.signOut();
                dialog.dismiss();
                atualizarUsuario();
            });

        } else {
            name.setText("Não logado");
            email.setText("");

            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);

            loginBtn.setOnClickListener(v -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, 1001);
            });
        }

        dialog.show();
    }

    // 🔥 RESULTADO LOGIN
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            try {
                GoogleSignInAccount account = GoogleSignIn
                        .getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);

                if (account != null) {
                    Toast.makeText(this, "Logado: " + account.getEmail(), Toast.LENGTH_SHORT).show();
                    atualizarUsuario();
                }

            } catch (Exception e) {
                Toast.makeText(this, "Erro no login", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void atualizarUsuario() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .into(userIcon);
        } else {
            userIcon.setImageResource(R.drawable.user);
        }
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

                String apiUrl = "http://192.xxx.xx.xxx:5000/info?url=" + url;

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
            if (thumb != null && !thumb.isEmpty()) {
                Glide.with(this)
                        .load(thumb)
                        .placeholder(R.drawable.user)
                        .error(R.drawable.user)
                        .into(thumbnail);
            } else {
                thumbnail.setImageResource(R.drawable.user);
            }

            containerQualidades.removeAllViews();

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

                b.setOnClickListener(v -> baixar(videoUrl));

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
