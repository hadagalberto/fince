package com.hadagalberto.fince;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private SwipeRefreshLayout container;
    private ListView listaContas;
    private List<ParseObject> objetos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        validaLogin();
        container = findViewById(R.id.swipeView);
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        listaContas = findViewById(R.id.listaConta);
        listaContas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParseObject contaSelecionada = objetos.get(i);
                Intent intent = new Intent(MainActivity.this, Conta.class);
                intent.putExtra("objeto", contaSelecionada.getObjectId());
                startActivity(intent);
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Conta.class));
            }
        });
        refreshOffline();
        refresh();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        validaLogin();
    }


    private void validaLogin(){
        if (ParseUser.getCurrentUser() == null)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void refresh(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Contas");
        query.whereEqualTo("Usuario", ParseUser.getCurrentUser());
        query.setLimit(999999999);
        query.orderByAscending("Vencimento");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                container.setRefreshing(false);
                if (e==null){
                    ParseObject.unpinAllInBackground("Contas", objects, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            ParseObject.pinAllInBackground("Contas", objects);
                        }
                    });
                    objetos = objects;
                    List<String> contas = new ArrayList<>();
                    for (ParseObject conta : objects){
                        String tipo = (conta.getBoolean("Tipo") ? "Receber" : "Pagar");
                        contas.add(conta.getString("Descricao") + " - R$ " + conta.getDouble("Valor") + " - " + tipo);
                    }
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, contas){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            // Get the current item from ListView
                            View view = super.getView(position,convertView,parent);
                            ParseObject obj = objetos.get(position);
                            String cor;
                            if(obj.getBoolean("Tipo")){
                                
                            }
                            view.setBackgroundColor(Color.parseColor("#FFB6B546"));
                            return view;
                        }
                    };
                    listaContas.setAdapter(adapter);
                } else {
                    if (e.getCode() == 100){
                        refreshOffline();
                        Toast.makeText(getApplicationContext(), "Sem conexão, trabalhando offline!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Erro ao buscar suas contas! Codigo do erro: " + e.getCode(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void refreshOffline(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Contas");
        query.whereEqualTo("Usuario", ParseUser.getCurrentUser());
        query.setLimit(999999999);
        query.fromLocalDatastore();
        query.orderByAscending("Vencimento");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                container.setRefreshing(false);
                if (e==null){
                    objetos = objects;
                    List<String> contas = new ArrayList<>();
                    for (ParseObject conta : objects){
                        String tipo = (conta.getBoolean("Tipo") ? "Receber" : "Pagar");
                        contas.add(conta.getString("Descricao") + " - R$ " + conta.getDouble("Valor") + " - " + tipo);
                    }
                    ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, contas);
                    listaContas.setAdapter(adapter);
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao buscar suas contas! Codigo do erro: " + e.getCode(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
