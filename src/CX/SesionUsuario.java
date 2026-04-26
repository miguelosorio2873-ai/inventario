package CX;

public class SesionUsuario {
    private static SesionUsuario instancia;

    private Long usuarioId;
    private String nombreUsuario;
    private String rol;
    private String permisos;

    private SesionUsuario() {}

    public static SesionUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SesionUsuario();
        }
        return instancia;
    }

    public void iniciarSesion(long id, String nombre, String rol, String permisos) {
        this.usuarioId = id;
        this.nombreUsuario = nombre;
        this.rol = rol;
        this.permisos = permisos;
    }

    public void cerrarSesion() {
        this.usuarioId = null;
        this.nombreUsuario = null;
        this.rol = null;
        this.permisos = null;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public String getPermisos() {
        return permisos;
    }

    public boolean tienePermiso(String modulo) {
        if ("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) return true;
        if (permisos == null) return false;
        return permisos.contains(modulo.toUpperCase());
    }
}
