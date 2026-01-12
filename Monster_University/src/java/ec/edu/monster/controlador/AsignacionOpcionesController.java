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
    private String rolSeleccionadoCodigo;
    private Rol rolSeleccionado;
    private List<Rol> rolesDisponibles;
    private List<Map<String, String>> todasOpcionesSistema;
    private List<Map<String, String>> opcionesAsignadas;
    private List<Map<String, String>> opcionesNoAsignadasTemp;
    
    // Selecciones en tablas
    private List<Map<String, String>> selectedOpcionesNoAsignadas;
    private List<Map<String, String>> selectedOpcionesAsignadas;
    
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
        opcionesNoAsignadasTemp = new ArrayList<>();
        selectedOpcionesNoAsignadas = new ArrayList<>();
        selectedOpcionesAsignadas = new ArrayList<>();
    }
    
    // ========== MÉTODOS DE CARGA ==========
    
    private void cargarRolesDisponibles() {
        try {
            rolesDisponibles = rolDAO.listarActivos();
            System.out.println("Roles activos cargados: " + rolesDisponibles.size());
        } catch (Exception e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
            rolesDisponibles = new ArrayList<>();
            
            // Datos de ejemplo para desarrollo
            Rol admin = new Rol();
            admin.setCodigo("ROL001");
            admin.setNombre("Administrador");
            admin.setDescripcion("Administrador del Sistema");
            admin.setOpciones_permitidas(new ArrayList<>());
            
            rolesDisponibles.add(admin);
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
    
    public void cargarAsignacionesPorRol() {
        System.out.println("Cargando asignaciones para rol código: " + rolSeleccionadoCodigo);
        
        if (rolSeleccionadoCodigo == null || rolSeleccionadoCodigo.trim().isEmpty()) {
            rolSeleccionado = null;
            opcionesAsignadas.clear();
            opcionesNoAsignadasTemp.clear();
            return;
        }
        
        try {
            // Buscar rol por código
            rolSeleccionado = rolDAO.buscarPorCodigo(rolSeleccionadoCodigo);
            
            if (rolSeleccionado != null) {
                System.out.println("Rol encontrado: " + rolSeleccionado.getNombre() + 
                                 " (Código: " + rolSeleccionado.getCodigo() + ")" +
                                 " con " + rolSeleccionado.getOpciones_permitidas().size() + " opciones");
                
                // Cargar opciones asignadas al rol
                cargarOpcionesAsignadas();
                
                // Preparar opciones no asignadas
                prepararOpcionesNoAsignadas();
                
                // Limpiar selecciones
                selectedOpcionesNoAsignadas.clear();
                selectedOpcionesAsignadas.clear();
                
                mostrarMensajeInfo("Rol '" + rolSeleccionado.getNombre() + "' cargado correctamente");
            } else {
                System.err.println("No se encontró el rol con código: " + rolSeleccionadoCodigo);
                mostrarMensajeError("No se encontró el rol seleccionado");
                opcionesAsignadas.clear();
                opcionesNoAsignadasTemp.clear();
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
        opcionesNoAsignadasTemp.clear();
        
        if (rolSeleccionado != null && todasOpcionesSistema != null) {
            List<String> codigosAsignados = rolSeleccionado.getOpciones_permitidas();
            
            for (Map<String, String> opcion : todasOpcionesSistema) {
                if (!codigosAsignados.contains(opcion.get("codigo"))) {
                    opcionesNoAsignadasTemp.add(opcion);
                }
            }
        }
    }
    
    public List<Map<String, String>> getOpcionesNoAsignadas() {
        return opcionesNoAsignadasTemp;
    }
    
    // ========== MÉTODOS DE ASIGNACIÓN/RETIRO ==========
    
    public void asignarOpcionesSeleccionadas() {
        if (selectedOpcionesNoAsignadas == null || selectedOpcionesNoAsignadas.isEmpty()) {
            mostrarMensajeAdvertencia("Seleccione al menos una opción para asignar");
            return;
        }
        
        if (rolSeleccionado == null) {
            mostrarMensajeError("No hay rol seleccionado");
            return;
        }
        
        try {
            int asignadasCount = 0;
            for (Map<String, String> opcion : selectedOpcionesNoAsignadas) {
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
            selectedOpcionesNoAsignadas.clear();
            
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
    
    public void retirarOpcionesSeleccionadas() {
        if (selectedOpcionesAsignadas == null || selectedOpcionesAsignadas.isEmpty()) {
            mostrarMensajeAdvertencia("Seleccione al menos una opción para retirar");
            return;
        }
        
        if (rolSeleccionado == null) {
            mostrarMensajeError("No hay rol seleccionado");
            return;
        }
        
        try {
            int retiradasCount = 0;
            for (Map<String, String> opcion : selectedOpcionesAsignadas) {
                String codigoOpcion = opcion.get("codigo");
                
                // Eliminar del rol
                boolean eliminado = rolDAO.eliminarOpcion(rolSeleccionado.getCodigo(), codigoOpcion);
                
                if (eliminado) {
                    // Actualizar lista local
                    rolSeleccionado.eliminarOpcion(codigoOpcion);
                    retiradasCount++;
                    System.out.println("Opción retirada: " + codigoOpcion + " del rol: " + rolSeleccionado.getCodigo());
                }
            }
            
            // Refrescar las listas
            cargarOpcionesAsignadas();
            prepararOpcionesNoAsignadas();
            
            // Limpiar selección
            selectedOpcionesAsignadas.clear();
            
            if (retiradasCount > 0) {
                mostrarMensajeExito(retiradasCount + " opción(es) retirada(s) correctamente");
            } else {
                mostrarMensajeAdvertencia("No se retiraron opciones (posiblemente ya no estaban asignadas)");
            }
            
        } catch (Exception e) {
            System.err.println("Error al retirar opciones: " + e.getMessage());
            e.printStackTrace();
            mostrarMensajeError("Error al retirar opciones: " + e.getMessage());
        }
    }
    
    public void asignarOpcionARol() {
        if (rolSeleccionadoCodigo == null || rolSeleccionadoCodigo.trim().isEmpty()) {
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
                rolSeleccionado = rolDAO.buscarPorCodigo(rolSeleccionadoCodigo);
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
    
    public void reiniciarFiltros() {
        rolSeleccionadoCodigo = null;
        rolSeleccionado = null;
        opcionesAsignadas.clear();
        opcionesNoAsignadasTemp.clear();
        selectedOpcionesNoAsignadas.clear();
        selectedOpcionesAsignadas.clear();
        opcionSeleccionadaCodigo = null;
        
        mostrarMensajeInfo("Selección limpiada");
    }
    
    // ========== MÉTODOS UTILITARIOS ==========
    
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
    
    public String getRolSeleccionadoCodigo() {
        return rolSeleccionadoCodigo;
    }
    
    public void setRolSeleccionadoCodigo(String rolSeleccionadoCodigo) {
        this.rolSeleccionadoCodigo = rolSeleccionadoCodigo;
    }
    
    public Rol getRolSeleccionado() {
        return rolSeleccionado;
    }
    
    public void setRolSeleccionado(Rol rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }
    
    public List<Rol> getRolesDisponibles() {
        return rolesDisponibles;
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
    
    public List<Map<String, String>> getSelectedOpcionesNoAsignadas() {
        return selectedOpcionesNoAsignadas;
    }
    
    public void setSelectedOpcionesNoAsignadas(List<Map<String, String>> selectedOpcionesNoAsignadas) {
        this.selectedOpcionesNoAsignadas = selectedOpcionesNoAsignadas;
    }
    
    public List<Map<String, String>> getSelectedOpcionesAsignadas() {
        return selectedOpcionesAsignadas;
    }
    
    public void setSelectedOpcionesAsignadas(List<Map<String, String>> selectedOpcionesAsignadas) {
        this.selectedOpcionesAsignadas = selectedOpcionesAsignadas;
    }
    
    public String getOpcionSeleccionadaCodigo() {
        return opcionSeleccionadaCodigo;
    }
    
    public void setOpcionSeleccionadaCodigo(String opcionSeleccionadaCodigo) {
        this.opcionSeleccionadaCodigo = opcionSeleccionadaCodigo;
    }
}