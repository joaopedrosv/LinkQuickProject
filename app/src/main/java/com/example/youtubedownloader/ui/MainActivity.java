package com.example.youtubedownloader.ui;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.youtubedownloader.R;
import com.example.youtubedownloader.data.AppDatabase;
import com.example.youtubedownloader.data.DownloadHistory;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText urlET;
    private LinearLayout containerQualidades, cardVideo;
    private ImageView thumbnail, userIcon;
    private TextView titleTV, titleApp;
    private ProgressBar loading;
    private View splashScreen;
    private TextView splashText;

    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private AppDatabase db;

    // IP atualizado com base no seu ipconfig
    private static final String SERVER_IP = "10.41.196.207";
    // TOKEN DO GOOGLE SERVICES ATUALIZADO
    private static final String WEB_CLIENT_ID = "232606653175-tqddteg18o9tn7mg17ouea5tssnd946v.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialização de Views
        urlET = findViewById(R.id.urlET);
        containerQualidades = findViewById(R.id.containerQualidades);
        thumbnail = findViewById(R.id.thumbnail);
        titleTV = findViewById(R.id.titleTV);
        loading = findViewById(R.id.loading);
        cardVideo = findViewById(R.id.cardVideo);
        titleApp = findViewById(R.id.titleApp);
        userIcon = findViewById(R.id.userIcon);
        splashScreen = findViewById(R.id.splashScreen);
        splashText = findViewById(R.id.splashText);

        auth = FirebaseAuth.getInstance();
        db = AppDatabase.getInstance(this);

        configurarGoogleSignIn();
        atualizarUsuario();
        executarSplash(); // Inicia a intro

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

        // Botão de Histórico
        MaterialButton historyBtn = new MaterialButton(this);
        historyBtn.setText("Ver Histórico");
        historyBtn.setOnClickListener(v -> showHistory());
        ViewGroup parent = (ViewGroup) btn.getParent();
        if (parent != null) {
            parent.addView(historyBtn, parent.indexOfChild(btn) + 1);
        }
    }

    private void executarSplash() {
        // Animação da Intro "LinkQuick"
        splashText.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    // Após 1.5 segundos, some com a splash
                    splashScreen.postDelayed(() -> {
                        splashScreen.animate()
                                .alpha(0f)
                                .setDuration(500)
                                .withEndAction(() -> {
                                    splashScreen.setVisibility(View.GONE);
                                    animarEntradaApp(); // Anima o conteúdo principal
                                }).start();
                    }, 1500);
                }).start();
    }

    private void animarEntradaApp() {
        titleApp.setAlpha(0f);
        titleApp.setTranslationY(-30f);
        titleApp.animate().alpha(1f).translationY(0).setDuration(500).start();
    }

    private void configurarGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(WEB_CLIENT_ID)
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            googleSignInClient = null;
        }
    }

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
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(photo);
            }

            logoutBtn.setOnClickListener(v -> {
                auth.signOut();
                if (googleSignInClient != null) {
                    googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                        dialog.dismiss();
                        atualizarUsuario();
                    });
                } else {
                    dialog.dismiss();
                    atualizarUsuario();
                }
            });
        } else {
            name.setText("Não logado");
            loginBtn.setVisibility(View.VISIBLE);
            logoutBtn.setVisibility(View.GONE);
            loginBtn.setOnClickListener(v -> {
                if (googleSignInClient != null) {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, 1001);
                }
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Erro no login: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        atualizarUsuario();
                        Toast.makeText(this, "Sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Erro ao autenticar com Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void atualizarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(userIcon);
        } else {
            userIcon.setImageResource(R.drawable.user);
        }
    }

    private void carregarEstado() {
        loading.setVisibility(View.VISIBLE);
        cardVideo.setVisibility(View.GONE);
        containerQualidades.removeAllViews();
    }

    private void buscarVideo(String url) {
        new Thread(() -> {
            try {
                String apiUrl = "http://" + SERVER_IP + ":5000/info?url=" + url;
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(apiUrl).openConnection();
                conn.setConnectTimeout(10000);
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                runOnUiThread(() -> sucesso(response.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(this, "Erro de conexão com o servidor", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void sucesso(String json) {
        loading.setVisibility(View.GONE);
        cardVideo.setVisibility(View.VISIBLE);
        cardVideo.setAlpha(1f);
        try {
            JSONObject obj = new JSONObject(json);
            String thumb = obj.optString("thumbnail", "");
            String title = obj.optString("title", "Vídeo sem título");
            JSONArray formats = obj.optJSONArray("formats");

            titleTV.setText(title);
            if (!thumb.isEmpty()) {
                Glide.with(this).load(thumb).placeholder(R.drawable.user).into(thumbnail);
            }

            HashMap<Integer, String> map = new HashMap<>();
            if (formats != null) {
                for (int i = 0; i < formats.length(); i++) {
                    JSONObject f = formats.getJSONObject(i);
                    int q = f.optInt("quality", 0);
                    String u = f.optString("url", "");
                    if (!u.isEmpty()) {
                        if (q == 0) q = 720;
                        if (!map.containsKey(q)) map.put(q, u);
                    }
                }
            }

            List<Integer> lista = new ArrayList<>(map.keySet());
            Collections.sort(lista, Collections.reverseOrder());

            for (Integer q : lista) {
                MaterialButton b = new MaterialButton(this);
                b.setText("Baixar " + (q > 0 ? q + "p" : "Vídeo"));
                b.setOnClickListener(v -> baixar(map.get(q), title, thumb));
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

    private void showHistory() {
        new Thread(() -> {
            List<DownloadHistory> histories = db.downloadDao().getAll();
            runOnUiThread(() -> {
                if (histories.isEmpty()) {
                    Toast.makeText(this, "Nenhum download no histórico", Toast.LENGTH_SHORT).show();
                    return;
                }
                BottomSheetDialog dialog = new BottomSheetDialog(this);
                View view = getLayoutInflater().inflate(R.layout.bottom_sheet_history, null);
                dialog.setContentView(view);

                RecyclerView recyclerView = view.findViewById(R.id.historyRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                
                HistoryAdapter adapter = new HistoryAdapter(histories, new HistoryAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(DownloadHistory history) {
                        showLinkDialog(history);
                    }

                    @Override
                    public void onDeleteClick(DownloadHistory history, int position) {
                        new Thread(() -> {
                            db.downloadDao().delete(history);
                            runOnUiThread(() -> {
                                histories.remove(position);
                                recyclerView.getAdapter().notifyItemRemoved(position);
                            });
                        }).start();
                    }
                });
                
                recyclerView.setAdapter(adapter);
                dialog.show();
            });
        }).start();
    }

    private void showLinkDialog(DownloadHistory history) {
        new AlertDialog.Builder(this)
                .setTitle("Link do Vídeo")
                .setMessage(history.url)
                .setPositiveButton("Copiar", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("URL", history.url);
                    if (clipboard != null) clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Link copiado!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("OK", null).show();
    }

    private void baixar(String url, String title, String thumb) {
        String nome = "video_" + System.currentTimeMillis() + ".mp4";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Baixando: " + title);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nome);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) manager.enqueue(request);

        new Thread(() -> {
            DownloadHistory history = new DownloadHistory();
            history.url = urlET.getText().toString().trim();
            history.title = title;
            history.thumbnail = thumb;
            history.filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + nome;
            history.date = System.currentTimeMillis();
            db.downloadDao().insert(history);
            runOnUiThread(() -> Toast.makeText(this, "Download iniciado!", Toast.LENGTH_SHORT).show());
        }).start();
    }
}

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<DownloadHistory> histories;
    private OnItemClickListener listener;

    public interface OnItemClickListener { 
        void onItemClick(DownloadHistory history);
        void onDeleteClick(DownloadHistory history, int position);
    }

    public HistoryAdapter(List<DownloadHistory> histories, OnItemClickListener listener) {
        this.histories = histories;
        this.listener = listener;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        DownloadHistory history = histories.get(position);
        holder.titleTV.setText(history.title != null ? history.title : "Sem título");
        Glide.with(holder.itemView.getContext()).load(history.thumbnail).placeholder(R.drawable.user).into(holder.thumbnailIV);

        long diff = System.currentTimeMillis() - history.date;
        long min = diff / 60000;
        holder.dateTV.setText(min < 60 ? min + "m atrás" : (min / 60) + "h atrás");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(history));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(history, position));
    }

    @Override
    public int getItemCount() { return histories.size(); }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailIV;
        TextView titleTV, dateTV;
        ImageButton btnDelete;
        public HistoryViewHolder(View v) {
            super(v);
            thumbnailIV = v.findViewById(R.id.historyThumbnail);
            titleTV = v.findViewById(R.id.historyTitle);
            dateTV = v.findViewById(R.id.historyDate);
            btnDelete = v.findViewById(R.id.btnDeleteHistory);
        }
    }
}