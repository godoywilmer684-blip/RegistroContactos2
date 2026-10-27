package com.example.registrocontactos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ContactosDB.db";
    private static final int DB_VERSION = 1;
    private static final String TABLA = "contactos";

    public DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, pais TEXT NOT NULL, " +
                "nombre TEXT NOT NULL, telefono TEXT NOT NULL, nota TEXT, imagen BLOB)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    public long insertarContacto(Contacto c) {
        ContentValues v = valores(c);
        return getWritableDatabase().insert(TABLA, null, v);
    }

    public int actualizarContacto(Contacto c) {
        return getWritableDatabase().update(TABLA, valores(c), "id=?",
                new String[]{String.valueOf(c.getId())});
    }

    private ContentValues valores(Contacto c) {
        ContentValues v = new ContentValues();
        v.put("pais", c.getPais());
        v.put("nombre", c.getNombre());
        v.put("telefono", c.getTelefono());
        v.put("nota", c.getNota());
        if (c.getImagen() == null) v.putNull("imagen");
        else v.put("imagen", c.getImagen());
        return v;
    }

    public int eliminarContacto(int id) {
        return getWritableDatabase().delete(TABLA, "id=?", new String[]{String.valueOf(id)});
    }

    public Contacto obtenerContacto(int id) {
        try (Cursor c = getReadableDatabase().query(TABLA, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return c.moveToFirst() ? leerContacto(c) : null;
        }
    }

    public ArrayList<Contacto> listarContactos(String busqueda) {
        ArrayList<Contacto> lista = new ArrayList<>();
        String filtro = "%" + busqueda + "%";
        try (Cursor c = getReadableDatabase().query(TABLA, null,
                "nombre LIKE ? OR telefono LIKE ?", new String[]{filtro, filtro},
                null, null, "nombre COLLATE NOCASE ASC")) {
            while (c.moveToNext()) lista.add(leerContacto(c));
        }
        return lista;
    }

    private Contacto leerContacto(Cursor c) {
        return new Contacto(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("pais")),
                c.getString(c.getColumnIndexOrThrow("nombre")),
                c.getString(c.getColumnIndexOrThrow("telefono")),
                c.getString(c.getColumnIndexOrThrow("nota")),
                c.getBlob(c.getColumnIndexOrThrow("imagen")));
    }
}
