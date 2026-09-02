package CX;

public class SesionUsuario {
    private static SesionUsuario instancia;

    private Long usuarioId;
    private String nombreUsuario;
    private String rol;
    private String permisos;
    private boolean licenciaActiva;
    private String licenciaVencimiento;

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

    public void iniciarSesion(long id, String nombre, String rol, String permisos,
                              boolean licenciaActiva, String licenciaVencimiento) {
        this.usuarioId = id;
        this.nombreUsuario = nombre;
        this.rol = rol;
        this.permisos = permisos;
        this.licenciaActiva = licenciaActiva;
        this.licenciaVencimiento = licenciaVencimiento;
    }

    public void cerrarSesion() {
        this.usuarioId = null;
        this.nombreUsuario = null;
        this.rol = null;
        this.permisos = null;
        this.licenciaActiva = false;
        this.licenciaVencimiento = null;
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

    public boolean isLicenciaActiva() {
        return licenciaActiva;
    }

    public void setLicenciaActiva(boolean licenciaActiva) {
        this.licenciaActiva = licenciaActiva;
    }

    public String getLicenciaVencimiento() {
        return licenciaVencimiento;
    }

    public void setLicenciaVencimiento(String licenciaVencimiento) {
        this.licenciaVencimiento = licenciaVencimiento;
    }

    /** ¿El usuario actual tiene su licencia vencida (activa y con fecha pasada)? */
    public boolean licenciaVencida() {
        if (!licenciaActiva) return false;
        if (licenciaVencimiento == null || licenciaVencimiento.trim().isEmpty()) return false;
        try {
            java.time.LocalDate v = java.time.LocalDate.parse(licenciaVencimiento.trim(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            return java.time.LocalDate.now().isAfter(v);
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return s.toUpperCase()
                .replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O").replace("Ú", "U")
                .replaceAll("[^A-Z0-9:,]", ""); // Eliminar caracteres extraños o acentos no mapeados
    }

    public boolean tienePermiso(String modulo) {
        // Admin siempre tiene acceso total al modulo, ignorando permisos detallados.
        if ("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) return true;

        String p = normalizar(permisos);
        String m = normalizar(modulo);

        // El acceso al modulo (verlo en el menu) depende SOLO del permiso de "Ver" (V).
        // Desactivar "Ver" oculta el modulo aunque tenga permisos de accion (C/E/D/X).
        int idx = p.indexOf(m + ":");
        if (idx != -1) {
            int endIdx = p.indexOf(",", idx);
            String sub = (endIdx == -1) ? p.substring(idx) : p.substring(idx, endIdx);
            String ac = sub.substring(sub.indexOf(":") + 1);
            return ac.contains("V");
        }

        return p.contains(m);
    }

    public boolean tienePermiso(String modulo, String accion) {
        // Admin siempre tiene todos los permisos, ignorando permisos detallados.
        if ("Admin".equalsIgnoreCase(rol) || "Administrador".equalsIgnoreCase(rol)) return true;

        if (permisos == null) return false;
        
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
        
        // Prioridad 2: Formato antiguo o permiso general por nombre
        return p.contains(m);
    }
}
