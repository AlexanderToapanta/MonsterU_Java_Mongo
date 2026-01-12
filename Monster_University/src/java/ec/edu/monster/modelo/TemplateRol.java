package ec.edu.monster.modelo;

import org.bson.types.ObjectId;
import java.util.List;

public class TemplateRol {
    private ObjectId id;
    private String codigoRol;
    private String nombreTemplate;
    private String descripcion;
    private List<String> opciones_disponibles;
    
    // Getters y Setters
    public ObjectId getId() {
        return id;
    }
    
    public void setId(ObjectId id) {
        this.id = id;
    }
    
    public String getCodigoRol() {
        return codigoRol;
    }
    
    public void setCodigoRol(String codigoRol) {
        this.codigoRol = codigoRol;
    }
    
    public String getNombreTemplate() {
        return nombreTemplate;
    }
    
    public void setNombreTemplate(String nombreTemplate) {
        this.nombreTemplate = nombreTemplate;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<String> getOpciones_disponibles() {
        return opciones_disponibles;
    }
    
    public void setOpciones_disponibles(List<String> opciones_disponibles) {
        this.opciones_disponibles = opciones_disponibles;
    }
}