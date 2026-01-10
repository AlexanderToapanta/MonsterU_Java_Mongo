package ec.edu.monster.modelo;

public class OpcionConfiguracion {
    private String codigo;
    private String nombre;
    
    // Constructor, getters y setters
    
    public OpcionConfiguracion() {}
    
    public OpcionConfiguracion(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}