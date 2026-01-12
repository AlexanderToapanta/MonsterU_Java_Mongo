package ec.edu.monster.controlador;

import ec.edu.monster.dao.CarreraDAO;
import ec.edu.monster.modelo.Carrera;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named("carreraController")
@ViewScoped
public class CarreraController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CarreraController.class.getName());
    
    private CarreraDAO carreraDAO;
    private Carrera current;
    private Carrera selected;
    private List<Carrera> items;
    
    @PostConstruct
    public void init() {
        try {
            carreraDAO = new CarreraDAO();
            items = carreraDAO.findAll();
            current = new Carrera();
            
            LOGGER.info("CarreraController inicializado. Total carreras: " + items.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inicializando CarreraController", e);
            addErrorMessage("Error al inicializar el sistema");
            items = new ArrayList<>();
        }
    }
    
    // ========== MÉTODOS PARA EL XHTML ==========
    
    public void prepareCreate() {
        try {
            current = new Carrera();
            // Generar código automático usando el DAO
            String siguienteCodigo = carreraDAO.generarSiguienteCodigo();
            current.setCodigo(siguienteCodigo);
            
            LOGGER.info("Preparando crear nueva carrera. Código generado: " + siguienteCodigo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al preparar creación de carrera", e);
            current = new Carrera();
            current.setCodigo("CARR001"); // Fallback
            addErrorMessage("Error al generar código automático");
        }
    }
    
    public void prepareView() {
        if (selected != null) {
            current = selected;
            LOGGER.info("Preparando para ver carrera: " + current.getNombre());
        } else {
            addErrorMessage("No hay carrera seleccionada para ver");
        }
    }
    
    public void prepareEdit() {
        if (selected != null) {
            current = selected;
            LOGGER.info("Preparando para editar carrera: " + current.getNombre());
        } else {
            addErrorMessage("No hay carrera seleccionada para editar");
        }
    }
    
    public void create() {
        try {
            if (current == null) {
                addErrorMessage("No hay datos de carrera para crear");
                return;
            }
            
            if (current.getCodigo() == null || current.getCodigo().trim().isEmpty()) {
                addErrorMessage("El código de la carrera es requerido");
                return;
            }
            
            if (current.getNombre() == null || current.getNombre().trim().isEmpty()) {
                addErrorMessage("El nombre de la carrera es requerido");
                return;
            }
            
            // Validar que créditos máximos sean >= créditos mínimos
            if (current.getCreditosMaximos() != null && current.getCreditosMinimos() != null) {
                if (current.getCreditosMaximos() < current.getCreditosMinimos()) {
                    addErrorMessage("Los créditos máximos deben ser mayores o iguales a los créditos mínimos");
                    return;
                }
            }
            
            // Validar que créditos sean números positivos
            if (current.getCreditosMaximos() != null && current.getCreditosMaximos() <= 0) {
                addErrorMessage("Los créditos máximos deben ser mayores a 0");
                return;
            }
            
            if (current.getCreditosMinimos() != null && current.getCreditosMinimos() <= 0) {
                addErrorMessage("Los créditos mínimos deben ser mayores a 0");
                return;
            }
            
            // Validar que el código no exista (por si acaso)
            if (carreraDAO.existeCarrera(current.getCodigo())) {
                addErrorMessage("Ya existe una carrera con el código: " + current.getCodigo());
                // Regenerar código
                String nuevoCodigo = carreraDAO.generarSiguienteCodigo();
                current.setCodigo(nuevoCodigo);
                addErrorMessage("Se ha generado un nuevo código: " + nuevoCodigo + ". Intente guardar nuevamente.");
                return;
            }
            
            boolean creado = carreraDAO.crearCarrera(current);
            if (creado) {
                addSuccessMessage("Carrera creada exitosamente con código: " + current.getCodigo());
                items = carreraDAO.findAll(); // Actualizar lista
                current = new Carrera(); // Resetear para nuevo registro
            } else {
                addErrorMessage("No se pudo crear la carrera");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear carrera", e);
            addErrorMessage("Error al crear la carrera: " + e.getMessage());
        }
    }
    
    public void update() {
        try {
            if (current == null || current.getCodigo() == null) {
                addErrorMessage("No hay carrera seleccionada para actualizar");
                return;
            }
            
            if (current.getNombre() == null || current.getNombre().trim().isEmpty()) {
                addErrorMessage("El nombre de la carrera es requerido");
                return;
            }
            
            // Validar que créditos máximos sean >= créditos mínimos
            if (current.getCreditosMaximos() != null && current.getCreditosMinimos() != null) {
                if (current.getCreditosMaximos() < current.getCreditosMinimos()) {
                    addErrorMessage("Los créditos máximos deben ser mayores o iguales a los créditos mínimos");
                    return;
                }
            }
            
            // Validar que créditos sean números positivos
            if (current.getCreditosMaximos() != null && current.getCreditosMaximos() <= 0) {
                addErrorMessage("Los créditos máximos deben ser mayores a 0");
                return;
            }
            
            if (current.getCreditosMinimos() != null && current.getCreditosMinimos() <= 0) {
                addErrorMessage("Los créditos mínimos deben ser mayores a 0");
                return;
            }
            
            boolean actualizado = carreraDAO.actualizarCarrera(current);
            if (actualizado) {
                addSuccessMessage("Carrera actualizada exitosamente");
                items = carreraDAO.findAll(); // Actualizar lista
                current = new Carrera(); // Resetear current
                selected = null; // Limpiar selección
            } else {
                addErrorMessage("No se pudo actualizar la carrera");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar carrera", e);
            addErrorMessage("Error al actualizar la carrera: " + e.getMessage());
        }
    }
    
    public void destroy() {
        try {
            if (selected == null) {
                addErrorMessage("No hay carrera seleccionada para eliminar");
                return;
            }
            
            if (selected.getCodigo() != null) {
                boolean eliminada = carreraDAO.eliminarCarrera(selected.getCodigo());
                if (eliminada) {
                    addSuccessMessage("Carrera eliminada exitosamente");
                    items = carreraDAO.findAll();
                    selected = null; // Limpiar selección
                    current = new Carrera();
                } else {
                    addErrorMessage("No se pudo eliminar la carrera");
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar carrera", e);
            addErrorMessage("Error al eliminar la carrera: " + e.getMessage());
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    private void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    private void addWarningMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public Carrera getCurrent() {
        if (current == null) {
            current = new Carrera();
        }
        return current;
    }
    
    public void setCurrent(Carrera current) {
        this.current = current;
    }
    
    public Carrera getSelected() {
        return selected;
    }
    
    public void setSelected(Carrera selected) {
        this.selected = selected;
    }
    
    public List<Carrera> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
    
    public void setItems(List<Carrera> items) {
        this.items = items;
    }
    
    // ========== GETTERS PARA EL XHTML ==========
    
    // Para rowKey="#{carreraController.getRowKey(item)}"
    public String getRowKey(Carrera carrera) {
        return carrera != null ? carrera.getCodigo() : null;
    }
    
    // ========== GETTERS PARA LAS PROPIEDADES DEL CURRENT ==========
    
    public String getCurrentCodigo() {
        return current != null ? current.getCodigo() : "";
    }
    
    public void setCurrentCodigo(String codigo) {
        if (current != null) {
            current.setCodigo(codigo);
        }
    }
    
    public String getCurrentNombre() {
        return current != null ? current.getNombre() : "";
    }
    
    public void setCurrentNombre(String nombre) {
        if (current != null) {
            current.setNombre(nombre);
        }
    }
    
    public Integer getCurrentCreditosMaximos() {
        return current != null ? current.getCreditosMaximos() : null;
    }
    
    public void setCurrentCreditosMaximos(Integer creditosMaximos) {
        if (current != null) {
            current.setCreditosMaximos(creditosMaximos);
        }
    }
    
    public Integer getCurrentCreditosMinimos() {
        return current != null ? current.getCreditosMinimos() : null;
    }
    
    public void setCurrentCreditosMinimos(Integer creditosMinimos) {
        if (current != null) {
            current.setCreditosMinimos(creditosMinimos);
        }
    }
    
    // ========== MÉTODOS DE COMPATIBILIDAD ==========
    
    // Para mantener compatibilidad si algún XHTML usa estos nombres
    public String getMecarrId() {
        return current != null ? current.getCodigo() : null;
    }
    
    public String getMecarrNombre() {
        return current != null ? current.getNombre() : "";
    }
    
    public void setMecarrNombre(String nombre) {
        if (current != null) {
            current.setNombre(nombre);
        }
    }
    
    public Integer getMecarrMaxCred() {
        return current != null ? current.getCreditosMaximos() : null;
    }
    
    public void setMecarrMaxCred(Integer maxCred) {
        if (current != null) {
            current.setCreditosMaximos(maxCred);
        }
    }
    
    public Integer getMecarrMinCred() {
        return current != null ? current.getCreditosMinimos() : null;
    }
    
    public void setMecarrMinCred(Integer minCred) {
        if (current != null) {
            current.setCreditosMinimos(minCred);
        }
    }
}