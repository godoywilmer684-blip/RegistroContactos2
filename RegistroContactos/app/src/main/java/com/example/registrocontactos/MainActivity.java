package com.example.registrocontactos;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private EditText etNombre, etTelefono, etNota;
    private Spinner spPais;
    private ImageView imgContacto;
    private Button btnGuardar;
    private DatabaseHelper db;
    private byte[] imagenBytes;
    private int idContacto = -1;
    private static final int CODIGO_GALERIA = 100, CODIGO_CAMARA = 101, PERMISO_CAMARA = 102;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        spPais = findViewById(R.id.spPais);
        imgContacto = findViewById(R.id.imgContacto);
        btnGuardar = findViewById(R.id.btnGuardar);
        findViewById(R.id.btnImagen).setOnClickListener(v -> mostrarOpcionesImagen());
        btnGuardar.setOnClickListener(v -> guardarContacto());
        findViewById(R.id.btnVerContactos).setOnClickListener(v ->
                startActivity(new Intent(this, ListaContactosActivity.class)));
        if (getIntent().hasExtra("idContacto")) cargarContacto(getIntent().getIntExtra("idContacto", -1));
    }

    private void guardarContacto() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();
        String pais = spPais.getSelectedItem().toString();
        if (nombre.isEmpty() || !nombre.matches("[\\p{L} ]+")) {
            etNombre.setError("Ingrese un nombre válido, solo letras"); etNombre.requestFocus(); return;
        }
        if (telefono.isEmpty() || !telefono.matches("[0-9]+")) {
            etTelefono.setError("Ingrese un teléfono con solo dígitos"); etTelefono.requestFocus(); return;
        }
        Contacto c = new Contacto(idContacto, pais, nombre, telefono, nota, imagenBytes);
        if (idContacto == -1) {
            if (db.insertarContacto(c) != -1) {
                Toast.makeText(this, "Contacto guardado correctamente", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
            } else Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        } else {
            if (db.actualizarContacto(c) > 0) {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                limpiarFormulario();
                finish();
            } else Toast.makeText(this, "No se pudo actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarContacto(int id) {
        Contacto c = db.obtenerContacto(id);
        if (c == null) { Toast.makeText(this, "Contacto no encontrado", Toast.LENGTH_SHORT).show(); finish(); return; }
        idContacto = c.getId();
        etNombre.setText(c.getNombre()); etTelefono.setText(c.getTelefono()); etNota.setText(c.getNota());
        for (int i = 0; i < spPais.getCount(); i++)
            if (spPais.getItemAtPosition(i).toString().equals(c.getPais())) { spPais.setSelection(i); break; }
        imagenBytes = c.getImagen();
        if (imagenBytes != null && imagenBytes.length > 0)
            imgContacto.setImageBitmap(BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length));
        btnGuardar.setText("Actualizar contacto");
    }

    private void limpiarFormulario() {
        etNombre.setText(""); etTelefono.setText(""); etNota.setText(""); spPais.setSelection(0);
        imgContacto.setImageResource(android.R.drawable.ic_menu_camera);
        imagenBytes = null; idContacto = -1; btnGuardar.setText("Guardar contacto");
    }

    private void mostrarOpcionesImagen() {
        new AlertDialog.Builder(this).setTitle("Fotografía del contacto")
            .setItems(new String[]{"Tomar fotografía", "Seleccionar de galería"}, (d, which) -> {
                if (which == 0) abrirCamara(); else abrirGaleria();
            }).show();
    }

    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA); return;
        }
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) startActivityForResult(i, CODIGO_CAMARA);
        else Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
    }

    private void abrirGaleria() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Seleccionar fotografía"), CODIGO_GALERIA);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISO_CAMARA && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) abrirCamara();
        else if (requestCode == PERMISO_CAMARA) Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;
        try {
            Bitmap bitmap = null;
            if (requestCode == CODIGO_CAMARA) {
                Object obj = data.getExtras() != null ? data.getExtras().get("data") : null;
                if (obj instanceof Bitmap) bitmap = (Bitmap) obj;
            } else if (requestCode == CODIGO_GALERIA && data.getData() != null) {
                Uri uri = data.getData();
                try (InputStream in = getContentResolver().openInputStream(uri)) { bitmap = BitmapFactory.decodeStream(in); }
            }
            if (bitmap != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                imagenBytes = out.toByteArray(); imgContacto.setImageBitmap(bitmap);
            } else Toast.makeText(this, "No se pudo cargar la imagen", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error con la imagen", Toast.LENGTH_LONG).show();
        }
    }
}
