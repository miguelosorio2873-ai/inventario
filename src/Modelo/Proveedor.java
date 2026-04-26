package Modelo;

public class Proveedor {
    private long id;
    private String nombreEmpresa;
    private String nitCedula;
    private String telefono;
    private String direccion;
    private String correo;
    private String nombreContacto;

    public Proveedor() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getNitCedula() { return nitCedula; }
    public void setNitCedula(String nitCedula) { this.nitCedula = nitCedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }

    @Override
    public String toString() { return nombreEmpresa; }
}
