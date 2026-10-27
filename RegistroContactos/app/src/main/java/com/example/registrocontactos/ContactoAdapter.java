package com.example.registrocontactos;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class ContactoAdapter extends BaseAdapter {
    private final Context context;
    private final ArrayList<Contacto> contactos;

    public ContactoAdapter(Context context, ArrayList<Contacto> contactos) {
        this.context = context; this.contactos = contactos;
    }
    @Override public int getCount() { return contactos.size(); }
    @Override public Contacto getItem(int position) { return contactos.get(position); }
    @Override public long getItemId(int position) { return contactos.get(position).getId(); }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) v = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        Contacto c = getItem(position);
        ImageView img = v.findViewById(R.id.imgItemContacto);
        ((TextView)v.findViewById(R.id.tvNombre)).setText(c.getNombre());
        ((TextView)v.findViewById(R.id.tvTelefono)).setText(c.getTelefono());
        ((TextView)v.findViewById(R.id.tvPais)).setText(c.getPais());
        byte[] foto = c.getImagen();
        if (foto != null && foto.length > 0) img.setImageBitmap(BitmapFactory.decodeByteArray(foto, 0, foto.length));
        else img.setImageResource(android.R.drawable.ic_menu_myplaces);
        return v;
    }
}
