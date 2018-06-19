package com.hadagalberto.fince;

import android.content.Intent;
import android.graphics.Color;
import com.github.clans.fab.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabNovo, fabRelatorio;
    private SwipeRefreshLayout container;
    private ListView listaContas;
    private List<ParseObject> objetos, objetosLista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        validaLogin();
        container = findViewById(R.id.swipeView);
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh(false);
            }
        });

        listaContas = findViewById(R.id.listaConta);
        listaContas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ParseObject contaSelecionada = objetosLista.get(i);
                Intent intent = new Intent(MainActivity.this, ContaActivity.class);
                intent.putExtra("objeto", contaSelecionada.getObjectId());
                startActivity(intent);
            }
        });
        fabRelatorio = findViewById(R.id.fabRelatorio);
        fabRelatorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Texto", Toast.LENGTH_LONG).show();
            }
        });
        fabNovo = findViewById(R.id.fabNovo);
        fabNovo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ContaActivity.class));
            }
        });
        refresh(true);
        refresh(false);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        validaLogin();
    }


    private void validaLogin(){
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        if (ParseUser.getCurrentUser() == null)
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }

    private void refresh(boolean offline){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Contas");
        query.whereEqualTo("Usuario", ParseUser.getCurrentUser());
        query.setLimit(999999999);
        if(offline)
            query.fromLocalDatastore();
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
                    objetosLista = new ArrayList<>();
                    for (ParseObject conta : objects){
                        if (conta.getDouble("Valor") == conta.getDouble("JaPago")){
                            continue;
                        }
                        objetosLista.add(conta);
                        String tipo = (conta.getBoolean("Tipo") ? "Receber" : "Pagar");
                        String tipoPassado = (conta.getBoolean("Tipo") ? "Recebido " : "Pago ");
                        Calendar now = Calendar.getInstance();
                        now.setTime(conta.getDate("Vencimento"));
                        String dataString = now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH)+1) + "/" + now.get(Calendar.YEAR);
                        contas.add(conta.getString("Descricao")
                                + "\nValor R$ " + conta.getDouble("Valor") + " - " + tipoPassado + conta.getDouble("JaPago") + " - " + "Restam R$ " + String.valueOf(conta.getDouble("Valor") - conta.getDouble("JaPago"))
                                + "\nVencimento em " + dataString
                                + "\nConta para " + tipo);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, contas){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            // Get the current item from ListView
                            View view = super.getView(position,convertView,parent);
                            ParseObject obj = objetos.get(position);
                            String cor;
                            if(obj.getBoolean("Tipo")){
                                if (obj.getDouble("JaPago") == 0.0)
                                    cor = "#8BE8BB";
                                else if (obj.getDouble("JaPago") < obj.getDouble("Valor"))
                                    cor = "#87CBFF";
                                else
                                    cor = "#88FF65";
                            } else{
                                if (obj.getDouble("JaPago") == 0.0)
                                    cor = "#FF8671";
                                else if (obj.getDouble("JaPago") < obj.getDouble("Valor"))
                                    cor = "#FFC60D";
                                else
                                    cor = "#88FF65";
                            }
                            //vermelho #FF0000
                            //amarelho #FFC60D
                            //azul #87CBFF
                            view.setBackgroundColor(Color.parseColor(cor));
                            return view;
                        }
                    };
                    listaContas.setAdapter(adapter);
                } else {
                    if (e.getCode() == 100){
                        refresh(true);
                        Toast.makeText(getApplicationContext(), "Sem conexão, trabalhando offline!", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Erro ao buscar suas contas! Codigo do erro: " + e.getCode(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
