package Modelo;

public class Cliente {
    private long id;
    private String cedula;
    private String nombre;
    private String correo;
    private String telefono;

    public Cliente() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    @Override
    public String toString() { return nombre; }
}
