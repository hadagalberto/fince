package com.hadagalberto.fince;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Date;

public class RelatorioActivity extends AppCompatActivity {

    private Date dataInicio, dataFim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relatorio);
    }
}
