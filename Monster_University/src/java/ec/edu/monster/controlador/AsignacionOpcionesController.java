package ec.edu.monster.controlador;

import ec.edu.monster.dao.ConfiguracionDAO;
import ec.edu.monster.dao.RolDAO;
import ec.edu.monster.modelo.Rol;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.PrimeFaces;

@Named(value = "asignacionOpcionesController")
@ViewScoped
public class AsignacionOpcionesController implements Serializable {

    private RolDAO rolDAO;
    private ConfiguracionDAO configuracionDAO;
    
    // Variables de estado
    private String rolSeleccionadoId; // CAMBIADO de rolSeleccionadoCodigo
    private Rol rolSeleccionado;
    private List<Rol> itemsAvailableSelectOne; // CAMBIADO de rolesDisponibles
    private List<Map<String, String>> todasOpcionesSistema;
    private List<Map<String, String>> opcionesAsignadas;
    private List<Map<String, String>> opcionesSinAsignar; // CAMBIADO de opcionesNoAsignadasTemp
    
    // Selecciones en tablas
    private List<Map<String, String>> opcionesSeleccionadasSinAsignar; // CAMBIADO de selectedOpcionesNoAsignadas
    private List<Map<String, String>> opcionesSeleccionadasAsignadas; // CAMBIADO de selectedOpcionesAsignadas
    
    // Para asignación rápida
    private String opcionSeleccionadaCodigo;
    
    public AsignacionOpcionesController() {
    }
    
    @PostConstruct
    public void init() {
        rolDAO = new RolDAO();
        configuracionDAO = new ConfiguracionDAO();
        
        cargarRolesDisponibles();
        cargarTodasOpcionesSistema();
        
        // Inicializar listas vacías
        opcionesAsignadas = new ArrayList<>();
        opcionesSinAsignar = new ArrayList<>();
        opcionesSeleccionadasSinAsignar = new ArrayList<>();
        opcionesSeleccionadasAsignadas = new ArrayList<>();
    }
    
    // ========== MÉTODOS DE CARGA ==========
    
    private void cargarRolesDisponibles() {
        try {
            itemsAvailableSelectOne = rolDAO.listarActivos(); // CAMBIADO
            System.out.println("Roles activos cargados: " + itemsAvailableSelectOne.size());
        } catch (Exception e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
            itemsAvailableSelectOne = new ArrayList<>(); // CAMBIADO
            
            // Datos de ejemplo para desarrollo
            Rol admin = new Rol();
            admin.setCodigo("ROL001");
            admin.setNombre("Administrador");
            admin.setDescripcion("Administrador del Sistema");
            admin.setOpciones_permitidas(new ArrayList<>());
            
            itemsAvailableSelectOne.add(admin); // CAMBIADO
        }
    }
    
    private void cargarTodasOpcionesSistema() {
        try {
            todasOpcionesSistema = configuracionDAO.obtenerValoresPorTipo("opciones_sistema");
            System.out.println("Opciones del sistema cargadas: " + todasOpcionesSistema.size());
        } catch (Exception e) {
            System.err.println("Error al cargar opciones del sistema: " + e.getMessage());
            todasOpcionesSistema = new ArrayList<>();
            
            // Datos de ejemplo para desarrollo
            Map<String, String> opcion1 = new HashMap<>();
            opcion1.put("codigo", "PER");
            opcion1.put("nombre", "Personal");
            opcion1.put("categoria", "Personal");
            todasOpcionesSistema.add(opcion1);
            
            Map<String, String> opcion2 = new HashMap<>();
            opcion2.put("codigo", "USU");
            opcion2.put("nombre", "Usuarios");
            opcion2.put("categoria", "Seguridad");
            todasOpcionesSistema.add(opcion2);
            
            Map<String, String> opcion3 = new HashMap<>();
            opcion3.put("codigo", "ROL");
            opcion3.put("nombre", "Roles");
            opcion3.put("categoria", "Seguridad");
            todasOpcionesSistema.add(opcion3);
        }
    }
    
    // ========== MÉTODOS PRINCIPALES ==========
    
    public void cargarOpcionesRol() { // CAMBIADO de cargarAsignacionesPorRol
        System.out.println("Cargando asignaciones para rol código: " + rolSeleccionadoId);
        
        if (rolSeleccionadoId == null || rolSeleccionadoId.trim().isEmpty()) {
            rolSeleccionado = null;
            opcionesAsignadas.clear();
            opcionesSinAsignar.clear();
            return;
        }
        
        try {
            // Buscar rol por código
            rolSeleccionado = rolDAO.buscarPorCodigo(rolSeleccionadoId); // CAMBIADO
            
            if (rolSeleccionado != null) {
                System.out.println("Rol encontrado: " + rolSeleccionado.getNombre() + 
                                 " (Código: " + rolSeleccionado.getCodigo() + ")" +
                                 " con " + rolSeleccionado.getOpciones_permitidas().size() + " opciones");
                
                // Cargar opciones asignadas al rol
                cargarOpcionesAsignadas();
                
                // Preparar opciones no asignadas
                prepararOpcionesNoAsignadas();
                
                // Limpiar selecciones
                opcionesSeleccionadasSinAsignar.clear(); // CAMBIADO
                opcionesSeleccionadasAsignadas.clear(); // CAMBIADO
                
                mostrarMensajeInfo("Rol '" + rolSeleccionado.getNombre() + "' cargado correctamente");
            } else {
                System.err.println("No se encontró el rol con código: " + rolSeleccionadoId); // CAMBIADO
                mostrarMensajeError("No se encontró el rol seleccionado");
                opcionesAsignadas.clear();
                opcionesSinAsignar.clear(); // CAMBIADO
            }
        } catch (Exception e) {
            System.err.println("Error al cargar asignaciones: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al cargar asignaciones: " + e.getMessage());
        }
    }
    
    private void cargarOpcionesAsignadas() {
        opcionesAsignadas.clear();
        
        if (rolSeleccionado != null && todasOpcionesSistema != null) {
            List<String> codigosAsignados = rolSeleccionado.getOpciones_permitidas();
            
            for (Map<String, String> opcion : todasOpcionesSistema) {
                if (codigosAsignados.contains(opcion.get("codigo"))) {
                    opcionesAsignadas.add(opcion);
                }
            }
        }
    }
    
    private void prepararOpcionesNoAsignadas() {
        opcionesSinAsignar.clear(); // CAMBIADO
        
        if (rolSeleccionado != null && todasOpcionesSistema != null) {
            List<String> codigosAsignados = rolSeleccionado.getOpciones_permitidas();
            
            for (Map<String, String> opcion : todasOpcionesSistema) {
                if (!codigosAsignados.contains(opcion.get("codigo"))) {
                    opcionesSinAsignar.add(opcion); // CAMBIADO
                }
            }
        }
    }
    
    public List<Map<String, String>> getOpcionesSinAsignar() { // CAMBIADO de getOpcionesNoAsignadas
        return opcionesSinAsignar;
    }
    
    // ========== MÉTODOS DE ASIGNACIÓN/RETIRO ==========
    
    public void asignarOpcionesSeleccionadas() {
        if (opcionesSeleccionadasSinAsignar == null || opcionesSeleccionadasSinAsignar.isEmpty()) { // CAMBIADO
            mostrarMensajeAdvertencia("Seleccione al menos una opción para asignar");
            return;
        }
        
        if (rolSeleccionado == null) {
            mostrarMensajeError("No hay rol seleccionado");
            return;
        }
        
        try {
            int asignadasCount = 0;
            for (Map<String, String> opcion : opcionesSeleccionadasSinAsignar) { // CAMBIADO
                String codigoOpcion = opcion.get("codigo");
                
                // Verificar si ya está asignada (por si acaso)
                if (!rolSeleccionado.getOpciones_permitidas().contains(codigoOpcion)) {
                    // Agregar al rol
                    boolean agregado = rolDAO.agregarOpcion(rolSeleccionado.getCodigo(), codigoOpcion);
                    
                    if (agregado) {
                        // Actualizar lista local
                        rolSeleccionado.agregarOpcion(codigoOpcion);
                        asignadasCount++;
                        System.out.println("Opción asignada: " + codigoOpcion + " a rol: " + rolSeleccionado.getCodigo());
                    }
                }
            }
            
            // Refrescar las listas
            cargarOpcionesAsignadas();
            prepararOpcionesNoAsignadas();
            
            // Limpiar selección
            opcionesSeleccionadasSinAsignar.clear(); // CAMBIADO
            
            if (asignadasCount > 0) {
                mostrarMensajeExito(asignadasCount + " opción(es) asignada(s) correctamente");
            } else {
                mostrarMensajeAdvertencia("No se asignaron nuevas opciones (posiblemente ya estaban asignadas)");
            }
            
        } catch (Exception e) {
            System.err.println("Error al asignar opciones: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al asignar opciones: " + e.getMessage());
        }
    }
    
    public void quitarOpcionesSeleccionadas() { // CAMBIADO de retirarOpcionesSeleccionadas
        if (opcionesSeleccionadasAsignadas == null || opcionesSeleccionadasAsignadas.isEmpty()) { // CAMBIADO
            mostrarMensajeAdvertencia("Seleccione al menos una opción para quitar");
            return;
        }
        
        if (rolSeleccionado == null) {
            mostrarMensajeError("No hay rol seleccionado");
            return;
        }
        
        try {
            int quitadasCount = 0;
            for (Map<String, String> opcion : opcionesSeleccionadasAsignadas) { // CAMBIADO
                String codigoOpcion = opcion.get("codigo");
                
                // Eliminar del rol
                boolean eliminado = rolDAO.eliminarOpcion(rolSeleccionado.getCodigo(), codigoOpcion);
                
                if (eliminado) {
                    // Actualizar lista local
                    rolSeleccionado.eliminarOpcion(codigoOpcion);
                    quitadasCount++;
                    System.out.println("Opción quitada: " + codigoOpcion + " del rol: " + rolSeleccionado.getCodigo());
                }
            }
            
            // Refrescar las listas
            cargarOpcionesAsignadas();
            prepararOpcionesNoAsignadas();
            
            // Limpiar selección
            opcionesSeleccionadasAsignadas.clear(); // CAMBIADO
            
            if (quitadasCount > 0) {
                mostrarMensajeExito(quitadasCount + " opción(es) quitada(s) correctamente");
            } else {
                mostrarMensajeAdvertencia("No se quitaron opciones (posiblemente ya no estaban asignadas)");
            }
            
        } catch (Exception e) {
            System.err.println("Error al quitar opciones: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al quitar opciones: " + e.getMessage());
        }
    }
    
    public void asignarOpcionARol() {
        if (rolSeleccionadoId == null || rolSeleccionadoId.trim().isEmpty()) { // CAMBIADO
            mostrarMensajeError("Seleccione un rol primero");
            return;
        }
        
        if (opcionSeleccionadaCodigo == null || opcionSeleccionadaCodigo.trim().isEmpty()) {
            mostrarMensajeError("Seleccione una opción");
            return;
        }
        
        try {
            // Asegurar que el rol esté cargado
            if (rolSeleccionado == null) {
                rolSeleccionado = rolDAO.buscarPorCodigo(rolSeleccionadoId); // CAMBIADO
            }
            
            if (rolSeleccionado != null) {
                // Verificar si ya está asignada
                if (rolSeleccionado.tieneOpcion(opcionSeleccionadaCodigo)) {
                    mostrarMensajeAdvertencia("La opción ya está asignada a este rol");
                    return;
                }
                
                // Asignar opción
                boolean asignado = rolDAO.agregarOpcion(rolSeleccionado.getCodigo(), opcionSeleccionadaCodigo);
                
                if (asignado) {
                    rolSeleccionado.agregarOpcion(opcionSeleccionadaCodigo);
                    
                    // Refrescar listas
                    cargarOpcionesAsignadas();
                    prepararOpcionesNoAsignadas();
                    
                    // Limpiar selección
                    opcionSeleccionadaCodigo = null;
                    
                    mostrarMensajeExito("Opción asignada correctamente al rol " + rolSeleccionado.getNombre());
                } else {
                    mostrarMensajeError("No se pudo asignar la opción");
                }
            } else {
                mostrarMensajeError("No se pudo encontrar el rol");
            }
            
        } catch (Exception e) {
            System.err.println("Error en asignación rápida: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al asignar opción: " + e.getMessage());
        }
    }
    
    public void guardarCambios() {
        if (rolSeleccionado == null) {
            mostrarMensajeError("No hay rol seleccionado");
            return;
        }
        
        try {
            // Actualizar el rol en la base de datos
            boolean actualizado = rolDAO.actualizarRol(rolSeleccionado);
            
            if (actualizado) {
                mostrarMensajeExito("Cambios guardados correctamente para el rol: " + rolSeleccionado.getNombre());
            } else {
                mostrarMensajeError("No se pudieron guardar los cambios");
            }
            
        } catch (Exception e) {
            System.err.println("Error al guardar cambios: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al guardar cambios: " + e.getMessage());
        }
    }
    
    public void limpiarSeleccion() { // CAMBIADO de reiniciarFiltros
        rolSeleccionadoId = null; // CAMBIADO
        rolSeleccionado = null;
        opcionesAsignadas.clear();
        opcionesSinAsignar.clear(); // CAMBIADO
        opcionesSeleccionadasSinAsignar.clear(); // CAMBIADO
        opcionesSeleccionadasAsignadas.clear(); // CAMBIADO
        opcionSeleccionadaCodigo = null;
        
        mostrarMensajeInfo("Selección limpiada");
    }
    
    // ========== MÉTODOS UTILITARIOS ==========
    
    // MÉTODOS NUEVOS QUE NECESITAS AGREGAR
    public int getTotalOpcionesSinAsignar() {
        return opcionesSinAsignar != null ? opcionesSinAsignar.size() : 0;
    }
    
    public int getTotalOpcionesAsignadas() {
        return opcionesAsignadas != null ? opcionesAsignadas.size() : 0;
    }
    
    private void mostrarMensajeExito(String mensaje) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update(":growl");
    }
    
    private void mostrarMensajeError(String mensaje) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update(":growl");
    }
    
    private void mostrarMensajeAdvertencia(String mensaje) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update(":growl");
    }
    
    private void mostrarMensajeInfo(String mensaje) {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", mensaje);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update(":growl");
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public String getRolSeleccionadoId() { // CAMBIADO de getRolSeleccionadoCodigo
        return rolSeleccionadoId;
    }
    
    public void setRolSeleccionadoId(String rolSeleccionadoId) { // CAMBIADO de setRolSeleccionadoCodigo
        this.rolSeleccionadoId = rolSeleccionadoId;
    }
    
    public Rol getRolSeleccionado() {
        return rolSeleccionado;
    }
    
    public void setRolSeleccionado(Rol rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }
    
    public List<Rol> getItemsAvailableSelectOne() { // CAMBIADO de getRolesDisponibles
        return itemsAvailableSelectOne;
    }
    
    public List<Map<String, String>> getTodasOpcionesSistema() {
        return todasOpcionesSistema;
    }
    
    public List<Map<String, String>> getOpcionesAsignadas() {
        return opcionesAsignadas;
    }
    
    public void setOpcionesAsignadas(List<Map<String, String>> opcionesAsignadas) {
        this.opcionesAsignadas = opcionesAsignadas;
    }
    
    public List<Map<String, String>> getOpcionesSeleccionadasSinAsignar() { // CAMBIADO de getSelectedOpcionesNoAsignadas
        return opcionesSeleccionadasSinAsignar;
    }
    
    public void setOpcionesSeleccionadasSinAsignar(List<Map<String, String>> opcionesSeleccionadasSinAsignar) {
        this.opcionesSeleccionadasSinAsignar = opcionesSeleccionadasSinAsignar;
    }
    
    public List<Map<String, String>> getOpcionesSeleccionadasAsignadas() { // CAMBIADO de getSelectedOpcionesAsignadas
        return opcionesSeleccionadasAsignadas;
    }
    
    public void setOpcionesSeleccionadasAsignadas(List<Map<String, String>> opcionesSeleccionadasAsignadas) {
        this.opcionesSeleccionadasAsignadas = opcionesSeleccionadasAsignadas;
    }
    
    public String getOpcionSeleccionadaCodigo() {
        return opcionSeleccionadaCodigo;
    }
    
    public void setOpcionSeleccionadaCodigo(String opcionSeleccionadaCodigo) {
        this.opcionSeleccionadaCodigo = opcionSeleccionadaCodigo;
    }
}