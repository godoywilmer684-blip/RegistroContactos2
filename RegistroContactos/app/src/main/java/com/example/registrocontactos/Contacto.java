package com.example.registrocontactos;

public class Contacto {
    private int id;
    private String pais, nombre, telefono, nota;
    private byte[] imagen;

    public Contacto(int id, String pais, String nombre, String telefono, String nota, byte[] imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public int getId() { return id; }
    public String getPais() { return pais; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getNota() { return nota; }
    public byte[] getImagen() { return imagen; }
    public void setId(int id) { this.id = id; }
    public void setPais(String pais) { this.pais = pais; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setNota(String nota) { this.nota = nota; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }
}
