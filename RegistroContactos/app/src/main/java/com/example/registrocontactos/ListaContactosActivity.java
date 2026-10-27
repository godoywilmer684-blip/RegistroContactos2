package com.example.registrocontactos;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListaContactosActivity extends AppCompatActivity {
    private EditText etBuscar;
    private ListView lvContactos;
    private TextView tvSeleccionado;
    private DatabaseHelper db;
    private ContactoAdapter adapter;
    private Contacto seleccionado;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);
        db = new DatabaseHelper(this);
        etBuscar = findViewById(R.id.etBuscar);
        lvContactos = findViewById(R.id.lvContactos);
        tvSeleccionado = findViewById(R.id.tvSeleccionado);
        cargarContactos("");
        lvContactos.setOnItemClickListener((p, v, pos, id) -> {
            seleccionado = adapter.getItem(pos);
            tvSeleccionado.setText("Seleccionado: " + seleccionado.getNombre() + "\nTeléfono: " + seleccionado.getTelefono());
        });
        etBuscar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int count, int after) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) {
                seleccionado = null; tvSeleccionado.setText("Seleccione un contacto"); cargarContactos(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
        findViewById(R.id.btnActualizar).setOnClickListener(v -> {
            if (!verificarSeleccion()) return;
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("idContacto", seleccionado.getId()); startActivity(i);
        });
        findViewById(R.id.btnEliminar).setOnClickListener(v -> {
            if (!verificarSeleccion()) return;
            new AlertDialog.Builder(this).setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + seleccionado.getNombre() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (d, w) -> {
                    db.eliminarContacto(seleccionado.getId()); seleccionado = null;
                    tvSeleccionado.setText("Seleccione un contacto"); cargarContactos(etBuscar.getText().toString());
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                }).show();
        });
        findViewById(R.id.btnCompartir).setOnClickListener(v -> {
            if (!verificarSeleccion()) return;
            String texto = "Contacto: " + seleccionado.getNombre() + "\nPaís: " + seleccionado.getPais()
                    + "\nTeléfono: " + seleccionado.getTelefono() + "\nNota: " + seleccionado.getNota();
            Intent i = new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT, texto);
            startActivity(Intent.createChooser(i, "Compartir contacto"));
        });
        findViewById(R.id.btnVerImagen).setOnClickListener(v -> {
            if (!verificarSeleccion()) return;
            byte[] foto = seleccionado.getImagen();
            if (foto == null || foto.length == 0) {
                Toast.makeText(this, "Este contacto no tiene fotografía", Toast.LENGTH_SHORT).show(); return;
            }
            ImageView img = new ImageView(this);
            img.setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.length)); img.setAdjustViewBounds(true);
            new AlertDialog.Builder(this).setTitle(seleccionado.getNombre()).setView(img).setPositiveButton("Cerrar", null).show();
        });
        findViewById(R.id.btnLlamar).setOnClickListener(v -> {
            if (!verificarSeleccion()) return;
            Intent i = new Intent(this, LlamarActivity.class); i.putExtra("idContacto", seleccionado.getId()); startActivity(i);
        });
    }

    private void cargarContactos(String busqueda) {
        ArrayList<Contacto> lista = db.listarContactos(busqueda);
        adapter = new ContactoAdapter(this, lista); lvContactos.setAdapter(adapter);
    }
    private boolean verificarSeleccion() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto de la lista", Toast.LENGTH_SHORT).show(); return false;
        }
        return true;
    }
    @Override protected void onResume() {
        super.onResume();
        if (db != null && etBuscar != null) {
            seleccionado = null; tvSeleccionado.setText("Seleccione un contacto");
            cargarContactos(etBuscar.getText().toString());
        }
    }
}
