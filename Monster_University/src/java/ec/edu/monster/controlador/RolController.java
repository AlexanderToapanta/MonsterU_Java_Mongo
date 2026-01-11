package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Rol;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.dao.RolDAO;
import ec.edu.monster.dao.PersonaDAO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@Named("rolController")
@SessionScoped
public class RolController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(RolController.class.getName());
    
    private RolDAO rolDAO;
    private PersonaDAO personaDAO;
    
    private List<Rol> items; // Renombrado para coincidir con el XHTML
    private Rol selected; // Renombrado para coincidir con el XHTML
    private String codigoRolSeleccionado;
    
    private List<Persona> personasSinRol;
    private List<Persona> personasConRol;
    private List<Persona> personasSeleccionadasSinRol;
    private List<Persona> personasSeleccionadasConRol;

    @PostConstruct
    public void init() {
        rolDAO = new RolDAO();
        personaDAO = new PersonaDAO();
        items = new ArrayList<>();
        personasSinRol = new ArrayList<>();
        personasConRol = new ArrayList<>();
        personasSeleccionadasSinRol = new ArrayList<>();
        personasSeleccionadasConRol = new ArrayList<>();
        cargarRoles();
    }

    public RolController() {
    }

    // ========== MÉTODOS REQUERIDOS POR EL XHTML ==========
    
    // Para value="#{rolController.items}" en el dataTable
    public List<Rol> getItems() {
        if (items == null || items.isEmpty()) {
            cargarRoles();
        }
        return items;
    }
    
    // Para selection="#{rolController.selected}" en el dataTable
    public Rol getSelected() {
        return selected;
    }
    
    public void setSelected(Rol selected) {
        this.selected = selected;
        // Cuando se selecciona un rol, también cargamos las personas
        if (selected != null) {
            cargarPersonasPorRol(selected.getCodigo());
        }
    }
    
    // Para rowKey="#{item.xerolId}" en el dataTable - necesitamos un getter para el ID
    public String getRowKey(Rol rol) {
        return rol.getCodigo();
    }
    
    // ========== MÉTODOS PARA LOS BOTONES DEL XHTML ==========
    
    // Para actionListener="#{rolController.prepareCreate}"
    public void prepareCreate() {
        selected = new Rol();
        try {
            // Generar código automático
            String nextCode = rolDAO.generarSiguienteCodigo();
            selected.setCodigo(nextCode);
            
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error generando código automático para rol", ex);
            // Si hay error, dejar código vacío para que el usuario lo ingrese
        }
    }
    
    // Para el botón Crear (llamado desde Create.xhtml)
    public void create() {
        if (selected != null) {
            try {
                if (rolDAO.crearRol(selected)) {
                    addSuccessMessage("Rol creado exitosamente: " + selected.getNombre());
                    items = null; // Forzar recarga en próximo getItems()
                    selected = null;
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al crear rol", e);
                addErrorMessage("Error al crear rol: " + e.getMessage());
            }
        }
    }
    
    // Para el botón Editar (llamado desde Edit.xhtml)
    public void update() {
        if (selected != null) {
            try {
                if (rolDAO.actualizarRol(selected)) {
                    addSuccessMessage("Rol actualizado exitosamente: " + selected.getNombre());
                    items = null; // Forzar recarga en próximo getItems()
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al actualizar rol", e);
                addErrorMessage("Error al actualizar rol: " + e.getMessage());
            }
        }
    }
    
    // Para actionListener="#{rolController.destroy}" en el botón Borrar
    public void destroy() {
        if (selected != null) {
            try {
                // Verificar si hay personas con este rol
                List<Persona> personasConEsteRol = personaDAO.buscarPersonasPorRol(selected.getCodigo());
                if (!personasConEsteRol.isEmpty()) {
                    addErrorMessage("No se puede eliminar el rol '" + selected.getNombre() + 
                                   "' porque tiene " + personasConEsteRol.size() + 
                                   " persona(s) asignada(s)");
                    return;
                }
                
                // Cambiar estado a INACTIVO
                if (rolDAO.cambiarEstado(selected.getCodigo(), "INACTIVO")) {
                    addSuccessMessage("Rol eliminado: " + selected.getNombre());
                    selected = null;
                    items = null; // Forzar recarga en próximo getItems()
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al eliminar rol", e);
                addErrorMessage("Error al eliminar rol: " + e.getMessage());
            }
        }
    }
    
    // ========== MÉTODOS QUE YA TENÍAS (CON AJUSTES MENORES) ==========
    
    private void cargarRoles() {
        try {
            items = rolDAO.listarTodos();
            LOGGER.info("Roles cargados: " + items.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar roles", e);
            items = new ArrayList<>();
        }
    }
    
    public String getCodigoRolSeleccionado() {
        return codigoRolSeleccionado;
    }
    
    public void setCodigoRolSeleccionado(String codigoRolSeleccionado) {
        this.codigoRolSeleccionado = codigoRolSeleccionado;
    }
    
    public void cargarPersonasPorRol(String codigoRol) {
        if (codigoRol != null && !codigoRol.isEmpty()) {
            try {
                // Obtener personas sin rol
                personasSinRol = personaDAO.buscarPersonasSinRol();
                
                // Obtener personas con este rol
                personasConRol = personaDAO.buscarPersonasPorRol(codigoRol);
                
                LOGGER.info("Personas sin rol: " + personasSinRol.size());
                LOGGER.info("Personas con rol seleccionado: " + personasConRol.size());
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al cargar personas por rol", e);
                personasSinRol = new ArrayList<>();
                personasConRol = new ArrayList<>();
            }
        } else {
            limpiarSeleccion();
        }
    }
    
    public void cargarPersonasPorRol() {
        if (selected != null && selected.getCodigo() != null) {
            cargarPersonasPorRol(selected.getCodigo());
        }
    }
    
    public List<Persona> getPersonasSinRol() {
        return personasSinRol;
    }
    
    public List<Persona> getPersonasConRol() {
        return personasConRol;
    }
    
    public List<Persona> getPersonasSeleccionadasSinRol() {
        return personasSeleccionadasSinRol;
    }
    
    public void setPersonasSeleccionadasSinRol(List<Persona> personasSeleccionadasSinRol) {
        this.personasSeleccionadasSinRol = personasSeleccionadasSinRol;
    }
    
    public List<Persona> getPersonasSeleccionadasConRol() {
        return personasSeleccionadasConRol;
    }
    
    public void setPersonasSeleccionadasConRol(List<Persona> personasSeleccionadasConRol) {
        this.personasSeleccionadasConRol = personasSeleccionadasConRol;
    }
    
    // Métodos para asignar/quitar personas (los mantienes igual)
    public void asignarPersonasSeleccionadas() {
        // Tu implementación actual
    }
    
    public void quitarPersonasSeleccionadas() {
        // Tu implementación actual
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    public void limpiarSeleccion() {
        selected = null;
        codigoRolSeleccionado = null;
        personasSinRol.clear();
        personasConRol.clear();
        personasSeleccionadasSinRol.clear();
        personasSeleccionadasConRol.clear();
        LOGGER.info("Selección limpiada");
    }
    
    public boolean isRolSeleccionado() {
        return selected != null;
    }
    
    public int getTotalPersonasSinRol() {
        return personasSinRol != null ? personasSinRol.size() : 0;
    }
    
    public int getTotalPersonasConRol() {
        return personasConRol != null ? personasConRol.size() : 0;
    }
    
    public String getEstadisticas() {
        if (selected == null) {
            return "Seleccione un rol para ver estadísticas";
        }
        return String.format("Personas sin rol: %d | Personas con rol '%s': %d", 
                getTotalPersonasSinRol(), 
                selected.getNombre(), 
                getTotalPersonasConRol());
    }
    
    // Métodos para mensajes (reemplazan a JsfUtil)
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", message));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
    
    // Método para buscar rol por código (útil para conversores)
    public Rol findRol(String codigo) {
        if (codigo == null || codigo.isEmpty()) {
            return null;
        }
        try {
            return rolDAO.buscarPorCodigo(codigo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar rol por código", e);
            return null;
        }
    }
}