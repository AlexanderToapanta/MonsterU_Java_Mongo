package ec.edu.monster.controlador;

import ec.edu.monster.dao.ConfiguracionDAO;
import ec.edu.monster.dao.PersonaDAO;
import ec.edu.monster.dao.RolDAO;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.modelo.Rol;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("rolController")
@SessionScoped
public class RolController implements Serializable {

    private RolDAO rolDAO;
    private PersonaDAO personaDAO;
    private ConfiguracionDAO configuracionDAO;
    
    private List<Rol> roles;
    private Rol rolSeleccionado;
    
    // Para asignación de opciones
    private List<Map<String, String>> opcionesSistema;  // Cambiado a Map
    private List<String> opcionesSeleccionadas;
    
    // Para asignación de personas a roles
    private List<Persona> personasSinRol;
    private List<Persona> personasConRol;
    private List<Persona> personasSeleccionadasSinRol;
    private List<Persona> personasSeleccionadasConRol;

    public RolController() {
        rolDAO = new RolDAO();
        personaDAO = new PersonaDAO();
        configuracionDAO = new ConfiguracionDAO();
        
        personasSeleccionadasSinRol = new ArrayList<>();
        personasSeleccionadasConRol = new ArrayList<>();
        opcionesSeleccionadas = new ArrayList<>();
    }

    // ============ GETTERS Y SETTERS ============
    
    public List<Rol> getRoles() {
        if (roles == null) {
            cargarRoles();
        }
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public Rol getRolSeleccionado() {
        return rolSeleccionado;
    }

    public void setRolSeleccionado(Rol rolSeleccionado) {
        this.rolSeleccionado = rolSeleccionado;
    }

    public List<Map<String, String>> getOpcionesSistema() {
        if (opcionesSistema == null) {
            cargarOpcionesSistema();
        }
        return opcionesSistema;
    }

    public void setOpcionesSistema(List<Map<String, String>> opcionesSistema) {
        this.opcionesSistema = opcionesSistema;
    }

    public List<String> getOpcionesSeleccionadas() {
        return opcionesSeleccionadas;
    }

    public void setOpcionesSeleccionadas(List<String> opcionesSeleccionadas) {
        this.opcionesSeleccionadas = opcionesSeleccionadas;
    }

    public List<Persona> getPersonasSinRol() {
        return personasSinRol;
    }

    public void setPersonasSinRol(List<Persona> personasSinRol) {
        this.personasSinRol = personasSinRol;
    }

    public List<Persona> getPersonasConRol() {
        return personasConRol;
    }

    public void setPersonasConRol(List<Persona> personasConRol) {
        this.personasConRol = personasConRol;
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
    
    // ============ MÉTODOS PARA ROLES ============
    
    public void cargarRoles() {
        try {
            roles = rolDAO.listarTodos();
            System.out.println("Roles cargados: " + roles.size());
        } catch (Exception e) {
            System.err.println("Error al cargar roles: " + e.getMessage());
            roles = new ArrayList<>();
        }
    }
    
    public void nuevoRol() {
        rolSeleccionado = new Rol();
        rolSeleccionado.setEstado("ACTIVO");
        generarCodigoRol();
    }
    
    private void generarCodigoRol() {
        try {
            List<Rol> todosRoles = rolDAO.listarTodos();
            
            if (todosRoles.isEmpty()) {
                rolSeleccionado.setCodigo("ROL001");
                return;
            }
            
            int maxNumero = 0;
            for (Rol rol : todosRoles) {
                String codigo = rol.getCodigo();
                if (codigo != null && codigo.startsWith("ROL") && codigo.length() == 6) {
                    try {
                        String numeroStr = codigo.substring(3);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Código con formato incorrecto: " + codigo);
                    }
                }
            }
            
            for (int i = 1; i <= 999; i++) {
                String codigoCandidato = String.format("ROL%03d", i);
                boolean existe = false;
                
                for (Rol rol : todosRoles) {
                    if (codigoCandidato.equals(rol.getCodigo())) {
                        existe = true;
                        break;
                    }
                }
                
                if (!existe) {
                    rolSeleccionado.setCodigo(codigoCandidato);
                    System.out.println("Código disponible asignado: " + codigoCandidato);
                    return;
                }
            }
            
            rolSeleccionado.setCodigo(String.format("ROL%03d", maxNumero + 1));
            
        } catch (Exception e) {
            System.err.println("Error generando código de rol: " + e.getMessage());
            rolSeleccionado.setCodigo("ROL001");
        }
    }
    
    public void guardarRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("No hay rol seleccionado para guardar");
                return;
            }
            
            if (rolSeleccionado.getCodigo() == null || rolSeleccionado.getCodigo().trim().isEmpty()) {
                mostrarError("El código del rol es requerido");
                return;
            }
            
            if (rolSeleccionado.getNombre() == null || rolSeleccionado.getNombre().trim().isEmpty()) {
                mostrarError("El nombre del rol es requerido");
                return;
            }
            
            // Verificar si es nuevo rol o actualización
            if (rolSeleccionado.getCodigo() == null) {
                // Nuevo rol
                if (rolDAO.existeCodigo(rolSeleccionado.getCodigo())) {
                    mostrarError("Ya existe un rol con el código: " + rolSeleccionado.getCodigo());
                    return;
                }
                
                boolean creado = rolDAO.crearRol(rolSeleccionado);
                if (creado) {
                    mostrarExito("Rol creado exitosamente: " + rolSeleccionado.getNombre());
                    roles = null; // Forzar recarga
                    rolSeleccionado = null;
                } else {
                    mostrarError("Error al crear el rol");
                }
            } else {
                // Actualizar rol existente
                boolean actualizado = rolDAO.actualizarRol(rolSeleccionado);
                if (actualizado) {
                    mostrarExito("Rol actualizado exitosamente: " + rolSeleccionado.getNombre());
                    roles = null; // Forzar recarga
                } else {
                    mostrarError("Error al actualizar el rol");
                }
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al guardar rol", e);
            mostrarError("Error al guardar rol: " + e.getMessage());
        }
    }
    
    public void eliminarRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol para eliminar");
                return;
            }
            
            // Verificar si hay personas con este rol
            List<Persona> personasConEsteRol = personaDAO.buscarPorRol(rolSeleccionado.getCodigo());
            if (!personasConEsteRol.isEmpty()) {
                mostrarError("No se puede eliminar el rol. Hay " + personasConEsteRol.size() + 
                           " personas asignadas a este rol.");
                return;
            }
            
            boolean eliminado = rolDAO.eliminarRol(rolSeleccionado.getCodigo().toString());
            if (eliminado) {
                mostrarExito("Rol eliminado exitosamente: " + rolSeleccionado.getNombre());
                roles = null;
                rolSeleccionado = null;
            } else {
                mostrarError("Error al eliminar el rol");
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al eliminar rol", e);
            mostrarError("Error al eliminar rol: " + e.getMessage());
        }
    }
    
    public void cambiarEstadoRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol");
                return;
            }
            
            String nuevoEstado = "ACTIVO".equals(rolSeleccionado.getEstado()) ? "INACTIVO" : "ACTIVO";
            rolSeleccionado.setEstado(nuevoEstado);
            
            boolean actualizado = rolDAO.actualizarRol(rolSeleccionado);
            if (actualizado) {
                mostrarExito("Estado del rol cambiado a: " + nuevoEstado);
                roles = null; // Forzar recarga
            } else {
                mostrarError("Error al cambiar estado del rol");
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al cambiar estado", e);
            mostrarError("Error al cambiar estado: " + e.getMessage());
        }
    }
    
    // ============ MÉTODOS PARA OPCIONES DEL SISTEMA ============
    
    public void cargarOpcionesSistema() {
        try {
            // Obtener las opciones directamente desde MongoDB
            opcionesSistema = configuracionDAO.obtenerValoresPorTipo("opciones_sistema");
            
            if (opcionesSistema == null || opcionesSistema.isEmpty()) {
                System.out.println("No se encontraron opciones del sistema en la base de datos");
                opcionesSistema = new ArrayList<>();
                
                // Agregar opciones por defecto
                agregarOpcionesPorDefecto();
            } else {
                System.out.println("Opciones del sistema cargadas desde DB: " + opcionesSistema.size());
                
                // Verificar la estructura de los datos
                for (Map<String, String> opcion : opcionesSistema) {
                    System.out.println("Opción: " + opcion);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar opciones del sistema: " + e.getMessage());
            e.printStackTrace();
            opcionesSistema = new ArrayList<>();
            
            // Opciones por defecto en caso de error
            agregarOpcionesPorDefecto();
        }
    }
    
    private void agregarOpcionesPorDefecto() {
        // Crear opciones por defecto como Maps
        List<Map<String, String>> opciones = new ArrayList<>();
        
        opciones.add(crearOpcion("PER", "Personal"));
        opciones.add(crearOpcion("FIN", "Finanzas"));
        opciones.add(crearOpcion("ACA", "Académico"));
        opciones.add(crearOpcion("SEG", "Seguridad"));
        opciones.add(crearOpcion("REP", "Reportes"));
        opciones.add(crearOpcion("CONF", "Configuración"));
        
        this.opcionesSistema = opciones;
    }
    
    private Map<String, String> crearOpcion(String codigo, String nombre) {
        Map<String, String> opcion = new java.util.HashMap<>();
        opcion.put("codigo", codigo);
        opcion.put("nombre", nombre);
        return opcion;
    }
    
    // ============ MÉTODOS PARA ASIGNACIÓN DE OPCIONES A ROLES ============
    
    public void cargarOpcionesParaRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol primero");
                return;
            }
            
            // Cargar opciones del sistema
            cargarOpcionesSistema();
            
            // Cargar opciones ya asignadas al rol
            if (rolSeleccionado.getOpciones_permitidas()!= null) {
                opcionesSeleccionadas = new ArrayList<>(rolSeleccionado.getOpciones_permitidas());
            } else {
                opcionesSeleccionadas = new ArrayList<>();
            }
            
            System.out.println("Opciones cargadas para rol " + rolSeleccionado.getNombre() + 
                             ": " + opcionesSeleccionadas.size());
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al cargar opciones", e);
            mostrarError("Error al cargar opciones: " + e.getMessage());
        }
    }
    
    public void guardarOpcionesRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol primero");
                return;
            }
            
            if (opcionesSeleccionadas == null) {
                opcionesSeleccionadas = new ArrayList<>();
            }
            
            // Actualizar las opciones del rol
            rolSeleccionado.setOpciones_permitidas(opcionesSeleccionadas);
            
            // Guardar en la base de datos
            boolean actualizado = rolDAO.actualizarRol(rolSeleccionado);
            
            if (actualizado) {
                mostrarExito("Opciones asignadas exitosamente al rol: " + rolSeleccionado.getNombre());
            } else {
                mostrarError("Error al guardar las opciones del rol");
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al guardar opciones", e);
            mostrarError("Error al guardar opciones: " + e.getMessage());
        }
    }
    
    public boolean tieneOpcion(String codigoOpcion) {
        if (rolSeleccionado == null || rolSeleccionado.getOpciones_permitidas()== null) {
            return false;
        }
        return rolSeleccionado.getOpciones_permitidas().contains(codigoOpcion);
    }
    
    // ============ MÉTODOS PARA ASIGNACIÓN DE PERSONAS A ROLES ============
    
    public void cargarPersonasParaRol() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol primero");
                return;
            }
            
            // Cargar personas sin rol
            personasSinRol = personaDAO.buscarPersonasSinRol();
            
            // Cargar personas con este rol específico
            personasConRol = personaDAO.buscarPorRol(rolSeleccionado.getCodigo());
            
            // Limpiar selecciones
            personasSeleccionadasSinRol.clear();
            personasSeleccionadasConRol.clear();
            
            System.out.println("Personas cargadas para rol " + rolSeleccionado.getNombre() + ":");
            System.out.println("  Sin rol: " + (personasSinRol != null ? personasSinRol.size() : 0));
            System.out.println("  Con este rol: " + (personasConRol != null ? personasConRol.size() : 0));
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al cargar personas", e);
            mostrarError("Error al cargar personas: " + e.getMessage());
        }
    }
    
    public void asignarPersonasSeleccionadas() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol primero");
                return;
            }
            
            if (personasSeleccionadasSinRol == null || personasSeleccionadasSinRol.isEmpty()) {
                mostrarError("Seleccione personas para asignar");
                return;
            }
            
            int asignadas = 0;
            for (Persona persona : personasSeleccionadasSinRol) {
                try {
                    // Asignar rol a la persona
                    persona.setRol(rolSeleccionado);
                    boolean actualizada = personaDAO.actualizarPersona(persona);
                    
                    if (actualizada) {
                        asignadas++;
                        // Mover de la lista de sin rol a con rol
                        if (personasSinRol != null) personasSinRol.remove(persona);
                        if (personasConRol != null) personasConRol.add(persona);
                    }
                } catch (Exception e) {
                    System.err.println("Error al asignar rol a persona " + persona.getCodigo() + ": " + e.getMessage());
                }
            }
            
            if (asignadas > 0) {
                mostrarExito(asignadas + " persona(s) asignada(s) al rol: " + rolSeleccionado.getNombre());
                personasSeleccionadasSinRol.clear();
            } else {
                mostrarError("No se pudo asignar ninguna persona");
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al asignar personas", e);
            mostrarError("Error al asignar personas: " + e.getMessage());
        }
    }
    
    public void quitarPersonasSeleccionadas() {
        try {
            if (rolSeleccionado == null) {
                mostrarError("Seleccione un rol primero");
                return;
            }
            
            if (personasSeleccionadasConRol == null || personasSeleccionadasConRol.isEmpty()) {
                mostrarError("Seleccione personas para quitar");
                return;
            }
            
            int quitadas = 0;
            for (Persona persona : personasSeleccionadasConRol) {
                try {
                    // Quitar rol de la persona
                    persona.setRol(null);
                    boolean actualizada = personaDAO.actualizarPersona(persona);
                    
                    if (actualizada) {
                        quitadas++;
                        // Mover de la lista de con rol a sin rol
                        if (personasConRol != null) personasConRol.remove(persona);
                        if (personasSinRol != null) personasSinRol.add(persona);
                    }
                } catch (Exception e) {
                    System.err.println("Error al quitar rol de persona " + persona.getCodigo() + ": " + e.getMessage());
                }
            }
            
            if (quitadas > 0) {
                mostrarExito(quitadas + " persona(s) quitada(s) del rol: " + rolSeleccionado.getNombre());
                personasSeleccionadasConRol.clear();
            } else {
                mostrarError("No se pudo quitar ninguna persona");
            }
            
        } catch (Exception e) {
            Logger.getLogger(RolController.class.getName()).log(Level.SEVERE, "Error al quitar personas", e);
            mostrarError("Error al quitar personas: " + e.getMessage());
        }
    }
    
    public void guardarCambiosPersonas() {
        // Este método solo muestra un mensaje ya que los cambios se guardan automáticamente
        mostrarExito("Los cambios han sido guardados automáticamente");
    }
    
    // ============ MÉTODOS DE UTILIDAD PARA LA VISTA ============
    
    public boolean isRolSeleccionado() {
        return rolSeleccionado != null;
    }
    public void verDetalles(Rol rol) {
    this.rolSeleccionado = rol;
    System.out.println("Mostrando detalles del rol: " + rol.getCodigo());
}

// Método para preparar edición
public void prepararEdicion(Rol rol) {
    this.rolSeleccionado = rol;
    System.out.println("Preparando edición del rol: " + rol.getCodigo());
}

// Métodos de estadísticas
public int contarRolesActivos() {
    if (roles == null) {
        cargarRoles();
    }
    int count = 0;
    for (Rol rol : roles) {
        if ("ACTIVO".equals(rol.getEstado())) {
            count++;
        }
    }
    return count;
}

public int contarRolesInactivos() {
    if (roles == null) {
        cargarRoles();
    }
    int count = 0;
    for (Rol rol : roles) {
        if ("INACTIVO".equals(rol.getEstado())) {
            count++;
        }
    }
    return count;
}
    public void limpiarSeleccion() {
        rolSeleccionado = null;
        opcionesSeleccionadas = new ArrayList<>();
        personasSinRol = null;
        personasConRol = null;
        personasSeleccionadasSinRol.clear();
        personasSeleccionadasConRol.clear();
        System.out.println("Selección limpiada");
    }
    
    public String getEstadisticasRol() {
        if (rolSeleccionado == null) {
            return "Seleccione un rol para ver estadísticas";
        }
        
        int totalSinRol = personasSinRol != null ? personasSinRol.size() : 0;
        int totalConRol = personasConRol != null ? personasConRol.size() : 0;
        int totalOpciones = opcionesSeleccionadas != null ? opcionesSeleccionadas.size() : 0;
        
        return String.format("Personas sin rol: %d | Personas con este rol: %d | Opciones asignadas: %d", 
                           totalSinRol, totalConRol, totalOpciones);
    }
    
    public int getTotalPersonasSinRol() {
        return personasSinRol != null ? personasSinRol.size() : 0;
    }
    
    public int getTotalPersonasConRol() {
        return personasConRol != null ? personasConRol.size() : 0;
    }
    
    public int getTotalOpcionesAsignadas() {
        return opcionesSeleccionadas != null ? opcionesSeleccionadas.size() : 0;
    }
    
    public boolean isHayPersonasSinRolSeleccionadas() {
        return personasSeleccionadasSinRol != null && !personasSeleccionadasSinRol.isEmpty();
    }
    
    public boolean isHayPersonasConRolSeleccionadas() {
        return personasSeleccionadasConRol != null && !personasSeleccionadasConRol.isEmpty();
    }
    
    // Métodos auxiliares para obtener nombre y código de opciones
    public String getNombreOpcion(String codigo) {
        for (Map<String, String> opcion : getOpcionesSistema()) {
            if (opcion.get("codigo").equals(codigo)) {
                return opcion.get("nombre");
            }
        }
        return codigo; // Si no se encuentra, devolver el código
    }
    
    public List<String> getCodigosOpciones() {
        List<String> codigos = new ArrayList<>();
        for (Map<String, String> opcion : getOpcionesSistema()) {
            codigos.add(opcion.get("codigo"));
        }
        return codigos;
    }
    
    private void mostrarExito(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", mensaje));
    }
    
    private void mostrarError(String mensaje) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", mensaje));
    }
}