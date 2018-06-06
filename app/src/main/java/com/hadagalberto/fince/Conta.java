package com.hadagalberto.fince;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Conta extends AppCompatActivity{

    EditText desc, valor;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Date dataVencimento;
    Button vencimento, salvar;
    EditText data;
    Switch tipo;
    private ParseObject contaObjeto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conta);
        setTitle("Fince - Conta");
        Intent intent = getIntent();

        //Referencia as views
        desc = findViewById(R.id.txtDesc);
        valor = findViewById(R.id.txtValor);
        vencimento = findViewById(R.id.btnVencimento);
        data = findViewById(R.id.txtVencimento);
        tipo = findViewById(R.id.switch1);
        salvar = findViewById(R.id.btnSalvar);
        tipo.setTextOn("Receber");
        tipo.setTextOff("Pagar");
        data.setEnabled(false);

        //Verifica se foi passado por intent
        try {
            String objId = intent.getStringExtra("objeto");
            if (objId != null)
                carrega(objId);
            else
                contaObjeto = new ParseObject("Contas");
        } catch (Exception e){
            contaObjeto = new ParseObject("Contas");
        }

        //Cria listener para salvar
        salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                salvarConta();
            }
        });

        //Cria listener para o botao do calendario
        vencimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        Conta.this,
                        android.R.style.Theme_Material_Light_Dialog_MinWidth,
                        dateSetListener,
                        year, month, day
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            }
        });

        //Listener para o calendario
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.YEAR, year);
                now.set(Calendar.DAY_OF_MONTH, day);
                now.set(Calendar.MONTH, month);
                dataVencimento = now.getTime();
                data.setText(day + "/" + (month+1) + "/" + year );
            }
        };

    }


    //Método para carregar dados de objeto existente
    private void carrega(String id){
        //Cria uma query para fazer a busca do objeto
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Contas");
        //Busca pelo id
        query.whereEqualTo("objectId", id);
        //Busca localmente
        query.fromLocalDatastore();
        //Inicia a busca
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                //Se estiver sem erros
                if(e==null){
                    //Pega o objeto e modifica os valores dos campos
                    contaObjeto = objects.get(0);
                    valor.setText(String.valueOf(contaObjeto.getDouble("Valor")));
                    dataVencimento = contaObjeto.getDate("Vencimento");
                    //Cria um calendario para converter para data
                    Calendar now = Calendar.getInstance();
                    now.setTime(dataVencimento);
                    String dataString = now.get(Calendar.DAY_OF_MONTH) + "/" + (now.get(Calendar.MONTH)+1) + "/" + now.get(Calendar.YEAR);
                    data.setText(dataString);
                    desc.setText(contaObjeto.getString("Descricao"));
                    tipo.setChecked(contaObjeto.getBoolean("Tipo"));
                } else {
                    //Se der erro mostra o erro e volta pra home
                    Toast.makeText(getApplicationContext(), "Erro ao buscar sua conta! Codigo do erro: " + e.getCode(), Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Conta.this, MainActivity.class));
                }
            }
        });
    }

    private void salvarConta(){
        //Desabilita campos
        valor.setEnabled(false);
        desc.setEnabled(false);
        vencimento.setEnabled(false);
        tipo.setEnabled(false);
        int erro = 0;

        //Verifica se algum está null
        if(valor.getText().toString().trim().equals(""))
            erro++;
        if(data.getText().toString().trim().equals(""))
            erro++;
        if(desc.getText().toString().trim().equals(""))
            erro++;

        //Caso esteja, reabilita todos os campos e mostra um toast
        if (erro > 0){
            valor.setEnabled(true);
            desc.setEnabled(true);
            vencimento.setEnabled(true);
            tipo.setEnabled(true);
            Toast.makeText(getApplicationContext(), "Complete todos os campos!", Toast.LENGTH_LONG).show();
            return;
        }

        //Pega os valores
        double valorDouble = Double.parseDouble(valor.getText().toString().trim());
        String descString = desc.getText().toString().trim();

        //Cria o objeto do Parse
        contaObjeto.put("Valor", valorDouble);
        contaObjeto.put("Vencimento", dataVencimento);
        boolean tipoO = tipo.isChecked();
        contaObjeto.put("Tipo", tipoO);
        contaObjeto.put("Descricao", descString);

        //Pega o usuario logado
        contaObjeto.put("Usuario", ParseUser.getCurrentUser());
        //Salva objeto offline e online se estiver conectado
        contaObjeto.saveEventually();
        Toast.makeText(getApplicationContext(), "Conta salva com sucesso!", Toast.LENGTH_LONG).show();
        startActivity(new Intent(Conta.this, MainActivity.class));
    }

}