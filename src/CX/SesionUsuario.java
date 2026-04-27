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

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return s.toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replaceAll("[^A-Z0-9:,]", ""); // Eliminar caracteres extraños o acentos no mapeados
    }

    public boolean tienePermiso(String modulo) {
        String p = normalizar(permisos);
        String m = normalizar(modulo);
        
        int idx = p.indexOf(m + ":");
        if (idx != -1) {
            int endIdx = p.indexOf(",", idx);
            String sub = (endIdx == -1) ? p.substring(idx) : p.substring(idx, endIdx);
            String actionsPart = sub.substring(sub.indexOf(":") + 1);
            return !actionsPart.isEmpty(); // Si tiene cualquier letra (C, E, D, X), tiene permiso de entrar
        }
        
        if ("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) return true;
        return p.contains(m);
    }

    public boolean tienePermiso(String modulo, String accion) {
        if (permisos == null && !("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol))) return false;
        
        String p = normalizar(permisos);
        String m = normalizar(modulo);
        String a = "";
        switch (accion.toUpperCase()) {
            case "VER": return tienePermiso(modulo); // Caso especial: Ver depende de tener cualquier permiso
            case "CREAR": a = "C"; break;
            case "EDITAR": a = "E"; break;
            case "ELIMINAR": a = "D"; break;
            case "EXPORTAR": a = "X"; break;
            default: a = accion.toUpperCase().substring(0, 1);
        }

        // Prioridad 1: Si existe una definición específica para el módulo (ej: PRODUCTOS:VCE)
        int idx = p.indexOf(m + ":");
        if (idx != -1) {
            int endIdx = p.indexOf(",", idx);
            String sub = (endIdx == -1) ? p.substring(idx) : p.substring(idx, endIdx);
            String actionsPart = sub.substring(sub.indexOf(":") + 1);
            return actionsPart.contains(a);
        }
        
        // Prioridad 2: Si es Administrador y no hay restricción específica, tiene permiso
        if ("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) return true;
        
        // Prioridad 3: Formato antiguo o permiso general por nombre
        return p.contains(m);
    }
}
