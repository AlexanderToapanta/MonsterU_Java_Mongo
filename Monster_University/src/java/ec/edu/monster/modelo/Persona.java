package ec.edu.monster.modelo;

import java.io.Serializable;
import java.util.Date;
import org.bson.types.ObjectId;

public class Persona implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private ObjectId id;
    private String codigo;
    private String peperTipo; // Cambiado de tipo_documento a peperTipo
    private String documento;
    private String nombres;
    private String apellidos;
    private String email;
    private String celular;
    private Date fecha_nacimiento;
    private String sexo; // "M" o "F"
    private String estado_civil; // "S", "C", "D", "V"
    private String username;
    private String password_hash;
    private Rol rol;
    private Date fecha_ingreso;
    private String imagen_perfil;
    private String estado; // "ACTIVO", "INACTIVO"
    
    // Constructores
    public Persona() {
        this.id = new ObjectId();
        this.estado = "ACTIVO";
        this.fecha_ingreso = new Date();
    }
    
    public Persona(String codigo, String nombres, String apellidos, String email) {
        this();
        this.codigo = codigo;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
    }
    
    // Getters y Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    // Cambiado de getTipo_documento a getPeperTipo
    public String getPeperTipo() {
        return peperTipo;
    }
    
    // Cambiado de setTipo_documento a setPeperTipo
    public void setPeperTipo(String peperTipo) {
        this.peperTipo = peperTipo;
    }
    
    public String getDocumento() {
        return documento;
    }
    
    public void setDocumento(String documento) {
        this.documento = documento;
    }
    
    public String getNombres() {
        return nombres;
    }
    
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCelular() {
        return celular;
    }
    
    public void setCelular(String celular) {
        this.celular = celular;
    }
    
    public Date getFecha_nacimiento() {
        return fecha_nacimiento;
    }
    
    public void setFecha_nacimiento(Date fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }
    
    public String getSexo() {
        return sexo;
    }
    
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    
    public String getEstado_civil() {
        return estado_civil;
    }
    
    public void setEstado_civil(String estado_civil) {
        this.estado_civil = estado_civil;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword_hash() {
        return password_hash;
    }
    
    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public Date getFecha_ingreso() {
        return fecha_ingreso;
    }
    
    public void setFecha_ingreso(Date fecha_ingreso) {
        this.fecha_ingreso = fecha_ingreso;
    }
    
    public String getImagen_perfil() {
        return imagen_perfil;
    }
    
    public void setImagen_perfil(String imagen_perfil) {
        this.imagen_perfil = imagen_perfil;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    // Métodos de ayuda
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
    
    @Override
    public String toString() {
        return "Persona{" + 
                "codigo=" + codigo + 
                ", peperTipo=" + peperTipo + 
                ", nombres=" + nombres + 
                ", apellidos=" + apellidos + 
                ", email=" + email + 
                ", username=" + username + 
                ", rol=" + (rol != null ? rol.getNombre() : "Sin rol") + 
                '}';
    }
}