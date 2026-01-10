package ec.edu.monster.modelo;

import java.util.List;

public class Configuracion {
    private String id;
    private String tipo;
    private List<OpcionConfiguracion> valores;
    
    // Constructor, getters y setters
    
    public Configuracion() {}
    
    public Configuracion(String tipo, List<OpcionConfiguracion> valores) {
        this.tipo = tipo;
        this.valores = valores;
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public List<OpcionConfiguracion> getValores() { return valores; }
    public void setValores(List<OpcionConfiguracion> valores) { this.valores = valores; }
}