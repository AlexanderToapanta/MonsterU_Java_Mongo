package ec.edu.monster.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rol implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String codigo;
    private String nombre;
    private String descripcion;
    private List<String> opciones_permitidas;
    private String estado; // "ACTIVO", "INACTIVO"
    
    // Constructores
    public Rol() {
        this.estado = "ACTIVO";
        this.opciones_permitidas = new ArrayList<>();
    }
    
    public Rol(String codigo, String nombre) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
    }
    
    public Rol(String codigo, String nombre, List<String> opcionesPermitidas) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.opciones_permitidas = opcionesPermitidas;
    }
    
    // Métodos para manejar opciones
    public void agregarOpcion(String opcion) {
        if (opciones_permitidas == null) {
            opciones_permitidas = new ArrayList<>();
        }
        if (!opciones_permitidas.contains(opcion)) {
            opciones_permitidas.add(opcion);
        }
    }
    
    public void eliminarOpcion(String opcion) {
        if (opciones_permitidas != null) {
            opciones_permitidas.remove(opcion);
        }
    }
    
    public boolean tieneOpcion(String opcion) {
        return opciones_permitidas != null && opciones_permitidas.contains(opcion);
    }
    
    // Getters y Setters
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<String> getOpciones_permitidas() {
        return opciones_permitidas;
    }
    
    public void setOpciones_permitidas(List<String> opciones_permitidas) {
        this.opciones_permitidas = opciones_permitidas;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Rol{" + 
                "codigo=" + codigo + 
                ", nombre=" + nombre + 
                ", descripcion=" + descripcion + 
                ", opciones=" + (opciones_permitidas != null ? opciones_permitidas.size() : 0) + 
                '}';
    }
}