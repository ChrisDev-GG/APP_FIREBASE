package com.example.app_firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnregistrar;
    Button btnbuscar;
    Button btneliminar;
    Button btnmodificar;
    ListView lvDatos;
    EditText txtid;
    EditText txtnombre;
    EditText txttelefono;
    EditText txtcorreo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnregistrar = (Button) findViewById(R.id.btnRegistrar);
        btnmodificar = (Button) findViewById(R.id.btnModificar);
        btneliminar = (Button) findViewById(R.id.btnEliminar);
        btnbuscar = (Button) findViewById(R.id.btnBuscar);
        txtid = (EditText) findViewById(R.id.txtId);
        txtnombre = (EditText) findViewById(R.id.txtNombre);
        txttelefono = (EditText) findViewById(R.id.txtTelefono);
        txtcorreo = (EditText) findViewById(R.id.txtCorreo);
        lvDatos = (ListView) findViewById(R.id.lvDatos);



        btnregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtid.getText().toString().isEmpty()
                        || txtnombre.getText().toString().isEmpty()
                        || txttelefono.getText().toString().isEmpty()
                        || txtcorreo.getText().toString().isEmpty())
                {
                    ocultarTeclado();
                    Toast.makeText(MainActivity.this, "Complete los campos faltantes!!", Toast.LENGTH_SHORT).show();
                }else{
                    int id = Integer.parseInt(txtid.getText().toString());
                    String nombre = txtnombre.getText().toString();
                    String telefono = txttelefono.getText().toString();
                    String correo = txtcorreo.getText().toString();

                    FirebaseDatabase db = FirebaseDatabase.getInstance(); // conexion a la base de datos
                    DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName()); // referencia a la base de datos agenda

                    // evento de firebase que genera la tarea de insercion
                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean exist = false;
                            String aux = Integer.toString(id);
                            String iddb;
                            for (DataSnapshot x : snapshot.getChildren()) {
                                iddb = String.valueOf(x.child("id").getValue());
                                if(iddb == aux){
                                    System.out.println("hola");
                                    exist = true;
                                    Toast.makeText(MainActivity.this, "El id ya existe!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            if(!exist){
                                Agenda agenda = new Agenda(id, nombre, telefono, correo);
                                dbref.push().setValue(agenda);
                                ocultarTeclado();
                                Toast.makeText(MainActivity.this, "Contacto registrado correctamente!!", Toast.LENGTH_SHORT).show();
                                limpiar();
                                listarContactos();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } // Cierra el if/else inicial

            }
        });
        //-----------------------------------------------------------------------------
        //-----------------------------------------------------------------------------
        btnmodificar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(txtid.getText().toString().trim().isEmpty()
                        || txtnombre.getText().toString().trim().isEmpty()
                        || txttelefono.getText().toString().trim().isEmpty()
                        || txtcorreo.getText().toString().trim().isEmpty()
                )
                {
                    ocultarTeclado();
                    Toast.makeText(MainActivity.this, "Complete los campos faltantes para actualizar!!", Toast.LENGTH_SHORT).show();
                }else{

                    int id = Integer.parseInt(txtid.getText().toString());
                    String nombre = txtnombre.getText().toString();
                    String telefono = txttelefono.getText().toString();
                    String correo = txtcorreo.getText().toString();

                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName());

                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String aux = Integer.toString(id);
                                boolean res = false;
                                for(DataSnapshot x : snapshot.getChildren()){
                                    if(x.child("id").getValue() != null) {
                                        x.getRef().child("nombre").setValue(nombre);
                                        x.getRef().child("telefono").setValue(telefono);
                                        x.getRef().child("correo").setValue(correo);
                                        limpiar();
                                        ocultarTeclado();
                                        listarContactos();
                                        Toast.makeText(MainActivity.this, "Contacto modificado!!", Toast.LENGTH_SHORT).show();
                                    }else{

                                        listarContactos();
                                    }
                                }


                            }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } // Cierra el if/else inicial.

            }
        });
        //-----------------------------------------------------------------------------
        //-----------------------------------------------------------------------------
        btneliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtid.getText().toString().trim().isEmpty()){
                    ocultarTeclado();
                    Toast.makeText(MainActivity.this, "Digite el ID del contacto a eliminar!!", Toast.LENGTH_SHORT).show();
                }else{
                    int id = Integer.parseInt(txtid.getText().toString());
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName());
                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String aux = Integer.toString(id);
                            final boolean[] res = {false};
                            for(DataSnapshot x : snapshot.getChildren()){
                                if(aux.equalsIgnoreCase(x.child("id").getValue().toString())){
                                    AlertDialog.Builder a = new AlertDialog.Builder(MainActivity.this);
                                    a.setCancelable(false);
                                    a.setTitle("Pregunta");
                                    a.setMessage("¿Está seguro(a) de querer eliminar el registro?");
                                    a.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });

                                    a.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            res[0] = true;
                                            ocultarTeclado();
                                            x.getRef().removeValue();
                                            listarContactos();
                                        }
                                    });
                                    a.show();
                                    break;
                                }
                            }

                            if(res[0] == false){
                                ocultarTeclado();
                                Toast.makeText(MainActivity.this, "ID ("+aux+") No encontrado.\nimposible eliminar!!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } // Cierra el if/else inicial.

            }
        });// Cierra el método botonEliminar.
        //-----------------------------------------------------------------------------
        //-----------------------------------------------------------------------------
        btnbuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtid.getText().toString().trim().isEmpty()){
                    ocultarTeclado();
                    Toast.makeText(MainActivity.this, "Digite el ID del contacto a buscar!!", Toast.LENGTH_SHORT).show();
                }else{
                    int id = Integer.parseInt(txtid.getText().toString());
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName());
                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String aux = Integer.toString(id);
                            boolean res = false;
                            for(DataSnapshot x : snapshot.getChildren()){
                                if(aux.equalsIgnoreCase(x.child("id").getValue().toString())){
                                    res = true;
                                    ocultarTeclado();
                                    txtnombre.setText(x.child("nombre").getValue().toString());
                                    txttelefono.setText(x.child("telefono").getValue().toString());
                                    txtcorreo.setText(x.child("correo").getValue().toString());
                                    break;
                                }
                            }
                            if(res == false){
                                ocultarTeclado();
                                Toast.makeText(MainActivity.this, "ID ("+aux+") No encontrado!!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } // Cierra el if/else inicial.

            }
        });// Cierra el método botonBuscar.

        listarContactos();

    }
    //------------------other

    //-------------------------------other

    private void ocultarTeclado(){
        View view = this.getCurrentFocus();
        if(view != null){
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    private void limpiar(){
        txtid.setText("");
        txtnombre.setText("");
        txttelefono.setText("");
        txtcorreo.setText("");
    }

    private void listarContactos(){
        FirebaseDatabase db = FirebaseDatabase.getInstance(); // conexion a la base de datos
        DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName());
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList data = new ArrayList<String>();
                for (DataSnapshot x : snapshot.getChildren()) {
                    if (x.child("nombre").getValue() != null) {
                        data.add(x.child("id").getValue() +": "+
                                x.child("nombre").getValue() + "/" +
                                x.child("telefono").getValue() + "/" +
                                x.child("correo").getValue());

                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
                lvDatos.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbref = db.getReference(Agenda.class.getSimpleName());

        ArrayList<Agenda> listaagenda = new ArrayList<Agenda>();
        ArrayAdapter<Agenda> adapter = new ArrayAdapter <Agenda> (MainActivity.this, android.R.layout.simple_list_item_1, listaagenda);
        lvDatos.setAdapter(adapter);


        dbref.addChildEventListener(new ChildEventListener() {
            @Override
            //ocurre cuando se agregar un nuevo registro
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Agenda agenda = snapshot.getValue(Agenda.class);
                listaagenda.add(agenda);
                adapter.notifyDataSetChanged();
            }

            @Override
            // ocurre cuando se modifica o elimina un registro
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        lvDatos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Agenda agenda = listaagenda.get(i);
                AlertDialog.Builder a = new AlertDialog.Builder(MainActivity.this);
                a.setCancelable(true);
                a.setTitle("Contacto Seleccionado");
                String msg = "ID : " + agenda.getId() +"\n\n";
                msg += "NOMBRE : " + agenda.getNombre();
                a.setMessage(msg);
                a.show();
            }
        });*/

    } // Cierra el método

}