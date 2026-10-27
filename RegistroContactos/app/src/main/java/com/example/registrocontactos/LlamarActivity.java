package com.example.registrocontactos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LlamarActivity extends AppCompatActivity {
    private static final int PERMISO_LLAMADA = 200;
    private DatabaseHelper db;
    private Contacto contacto;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);
        db = new DatabaseHelper(this);
        int id = getIntent().getIntExtra("idContacto", -1);
        contacto = db.obtenerContacto(id);
        if (contacto == null) {
            Toast.makeText(this, "Contacto no encontrado", Toast.LENGTH_SHORT).show(); finish(); return;
        }
        ((TextView)findViewById(R.id.tvNombreLlamar)).setText(contacto.getNombre());
        ((TextView)findViewById(R.id.tvTelefonoLlamar)).setText(contacto.getPais() + "\n" + contacto.getTelefono());
        byte[] foto = contacto.getImagen();
        if (foto != null && foto.length > 0)
            ((ImageView)findViewById(R.id.imgLlamar)).setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.length));
        findViewById(R.id.btnLlamarAhora).setOnClickListener(v -> confirmarLlamada());
        findViewById(R.id.btnCancelarLlamada).setOnClickListener(v -> finish());
    }

    private String numeroCompleto() {
        String pais = contacto.getPais(), prefijo = "+504";
        if (pais.startsWith("México")) prefijo = "+52";
        else if (pais.startsWith("Estados Unidos")) prefijo = "+1";
        else if (pais.startsWith("Guatemala")) prefijo = "+502";
        else if (pais.startsWith("El Salvador")) prefijo = "+503";
        else if (pais.startsWith("Nicaragua")) prefijo = "+505";
        else if (pais.startsWith("Costa Rica")) prefijo = "+506";
        else if (pais.startsWith("Panamá")) prefijo = "+507";
        String tel = contacto.getTelefono();
        return tel.startsWith("+") ? tel : prefijo + tel;
    }

    private void confirmarLlamada() {
        new AlertDialog.Builder(this).setTitle("Confirmar llamada")
            .setMessage("¿Desea llamar a " + contacto.getNombre() + "?\n" + numeroCompleto())
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Llamar", (d, w) -> realizarLlamada()).show();
    }

    private void realizarLlamada() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISO_LLAMADA); return;
        }
        ejecutarLlamada();
    }
    private void ejecutarLlamada() {
        try {
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + numeroCompleto()));
            startActivity(i);
        } catch (SecurityException e) {
            Toast.makeText(this, "No se concedió permiso para llamar", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo iniciar la llamada", Toast.LENGTH_SHORT).show();
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISO_LLAMADA && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) ejecutarLlamada();
        else if (requestCode == PERMISO_LLAMADA) Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_LONG).show();
    }
}
