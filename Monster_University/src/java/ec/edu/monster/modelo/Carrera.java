package ec.edu.monster.modelo;

import org.bson.types.ObjectId;

public class Carrera {
    private ObjectId id;
    private String codigo;
    private String nombre;
    private Integer creditosMaximos;
    private Integer creditosMinimos;
    
    // Constructor vacío
    public Carrera() {
    }
    
    // Constructor completo
    public Carrera(String codigo, String nombre, Integer creditosMaximos, Integer creditosMinimos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.creditosMaximos = creditosMaximos;
        this.creditosMinimos = creditosMinimos;
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
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public Integer getCreditosMaximos() {
        return creditosMaximos;
    }
    
    public void setCreditosMaximos(Integer creditosMaximos) {
        this.creditosMaximos = creditosMaximos;
    }
    
    public Integer getCreditosMinimos() {
        return creditosMinimos;
    }
    
    public void setCreditosMinimos(Integer creditosMinimos) {
        this.creditosMinimos = creditosMinimos;
    }
    
    @Override
    public String toString() {
        return "Carrera{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", creditosMaximos=" + creditosMaximos +
                ", creditosMinimos=" + creditosMinimos +
                '}';
    }
}