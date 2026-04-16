package com.example.mad;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRecordsActivity extends AppCompatActivity {
    private RecyclerView rvReports;
    private ReportAdapter adapter;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_records);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        SharedPreferences sharedPref = getSharedPreferences("MAD_PREFS", MODE_PRIVATE);
        currentUsername = sharedPref.getString("USERNAME", "Unknown");

        rvReports = findViewById(R.id.rvReports);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new ReportAdapter(new ArrayList<>());
        rvReports.setAdapter(adapter);

        loadReports();
    }

    private void loadReports() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<AssessmentReport> reports = AppDatabase.getDatabase(this)
                    .assessmentReportDao().getReportsByUserId(currentUsername);
            runOnUiThread(() -> {
                adapter.updateList(reports);
            });
        });
    }

    private class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
        private List<AssessmentReport> reports;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public ReportAdapter(List<AssessmentReport> reports) {
            this.reports = reports;
        }

        public void updateList(List<AssessmentReport> newList) {
            this.reports = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            AssessmentReport report = reports.get(position);
            holder.tvType.setText(report.testType + " Report");
            holder.tvDate.setText(dateFormat.format(new Date(report.timestamp)));
            holder.tvScore.setText("Score: " + report.score);
            holder.tvFeedback.setText(report.feedback);

            holder.btnSend.setOnClickListener(v -> {
                // Navigate to Doctor selection to share this report
                Intent intent = new Intent(MyRecordsActivity.this, DoctorListActivity.class);
                intent.putExtra("USERNAME", currentUsername);
                intent.putExtra("REPORT_TO_SEND", "REPORT_DATA:" + report.testType + "|" + report.score + "|" + report.feedback);
                startActivity(intent);
                Toast.makeText(MyRecordsActivity.this, "Select a doctor to share this report", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }

        class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvDate, tvScore, tvFeedback;
            MaterialButton btnSend;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvTestType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvScore = itemView.findViewById(R.id.tvScore);
                tvFeedback = itemView.findViewById(R.id.tvFeedback);
                btnSend = itemView.findViewById(R.id.btnSendToDoctor);
            }
        }
    }
}