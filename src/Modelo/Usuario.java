package Modelo;

public class Usuario {
    private long id;
    private String nombre;
    private String email;
    private String password;
    private String rol;
    private String pregunta1;
    private String pregunta2;
    private String pregunta3;
    private String pregunta4;
    private String respuesta1;
    private String respuesta2;
    private String respuesta3;
    private String respuesta4;
    private String permisos;
    private boolean licenciaActiva;
    private String licenciaVencimiento;

    public Usuario() {}

    public Usuario(long id, String nombre, String email, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPermisos() { return permisos; }
    public void setPermisos(String permisos) { this.permisos = permisos; }

    public String getPregunta1() { return pregunta1; }
    public void setPregunta1(String pregunta1) { this.pregunta1 = pregunta1; }

    public String getPregunta2() { return pregunta2; }
    public void setPregunta2(String pregunta2) { this.pregunta2 = pregunta2; }

    public String getPregunta3() { return pregunta3; }
    public void setPregunta3(String pregunta3) { this.pregunta3 = pregunta3; }

    public String getPregunta4() { return pregunta4; }
    public void setPregunta4(String pregunta4) { this.pregunta4 = pregunta4; }

    public String getRespuesta1() { return respuesta1; }
    public void setRespuesta1(String respuesta1) { this.respuesta1 = respuesta1; }

    public String getRespuesta2() { return respuesta2; }
    public void setRespuesta2(String respuesta2) { this.respuesta2 = respuesta2; }

    public String getRespuesta3() { return respuesta3; }
    public void setRespuesta3(String respuesta3) { this.respuesta3 = respuesta3; }

    public String getRespuesta4() { return respuesta4; }
    public void setRespuesta4(String respuesta4) { this.respuesta4 = respuesta4; }

    public boolean isLicenciaActiva() { return licenciaActiva; }
    public void setLicenciaActiva(boolean licenciaActiva) { this.licenciaActiva = licenciaActiva; }

    public String getLicenciaVencimiento() { return licenciaVencimiento; }
    public void setLicenciaVencimiento(String licenciaVencimiento) { this.licenciaVencimiento = licenciaVencimiento; }
}
