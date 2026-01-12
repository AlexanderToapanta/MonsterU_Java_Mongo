package ec.edu.monster.controlador;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.modelo.Rol;
import ec.edu.monster.modelo.UserCache;
import ec.edu.monster.modelo.TemplateRol;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.bson.Document;

@Named(value = "loginController")
@SessionScoped
public class LoginController implements Serializable {
    
    private final PasswordController passController;
    private Persona usuario;
    private ExternalContext context = FacesContext.getCurrentInstance().getExternalContext(); 
    private UserCache usu = new UserCache();
    
    private Conexion conexionMongo;
    private MongoCollection<Document> personasCollection;
    private MongoCollection<Document> rolesCollection;
    private MongoCollection<Document> templatesCollection;

    public LoginController() {
        usuario = new Persona();
        passController = new PasswordController();
        conexionMongo = new Conexion();
    }

    @PostConstruct
    public void init() {
        try {
            conexionMongo.crearConexion();
            if (conexionMongo.isConectado()) {
                MongoDatabase database = conexionMongo.getDataB();
                personasCollection = database.getCollection("personas");
                rolesCollection = database.getCollection("roles");
                templatesCollection = database.getCollection("templates_roles");
                System.out.println("Conexión a MongoDB establecida para login");
            } else {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "No se pudo conectar a MongoDB");
            }
            
            Persona usuarioLogueado = getUsuarioLogueado();
            if (usuarioLogueado != null) {
                cargarDatosUsuarioCache(usuarioLogueado);
            }
        } catch (Exception e) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "Error al inicializar MongoDB", e);
        }
    }

    public void doLogin() throws NoSuchAlgorithmException, IOException {
        String username = usuario.getUsername();
        String password = usuario.getPassword_hash();
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario y contraseña requeridos"));
            return;
        }
        
        String claveCifrada = passController.encriptarClave(password);
        
        if (!conexionMongo.isConectado()) {
            conexionMongo.crearConexion();
            if (conexionMongo.isConectado()) {
                MongoDatabase database = conexionMongo.getDataB();
                personasCollection = database.getCollection("personas");
                rolesCollection = database.getCollection("roles");
                templatesCollection = database.getCollection("templates_roles");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo conectar a la base de datos"));
                return;
            }
        }
        
        // Buscar usuario en personas
        Document personaDoc = personasCollection.find(
            Filters.and(
                Filters.eq("username", username),
                Filters.eq("password_hash", claveCifrada)
            )
        ).first();
        
        if (personaDoc != null) {
            Persona usuarioLogueado = documentToPersonaConRolCompleto(personaDoc);
            
            if (!"ACTIVO".equals(usuarioLogueado.getEstado())) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario inactivo"));
                return;
            }
            
            // Verificar y actualizar opciones del rol con el template
            actualizarOpcionesDesdeTemplate(usuarioLogueado.getRol());
            
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuario", usuarioLogueado);
            cargarDatosUsuarioCache(usuarioLogueado);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Login exitoso"));
            
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("/Monster_University/faces/index1.xhtml");
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Credenciales incorrectas"));
        }
    }
    
    /**
     * Busca el rol en la colección de roles y obtiene sus opciones completas
     */
    private Persona documentToPersonaConRolCompleto(Document doc) {
        Persona persona = new Persona();
        
        if (doc.getObjectId("_id") != null) {
            persona.setId(doc.getObjectId("_id"));
        }
        persona.setCodigo(doc.getString("codigo"));
        persona.setPeperTipo(doc.getString("peperTipo"));
        persona.setDocumento(doc.getString("documento"));
        persona.setNombres(doc.getString("nombres"));
        persona.setApellidos(doc.getString("apellidos"));
        persona.setEmail(doc.getString("email"));
        persona.setCelular(doc.getString("celular"));
        persona.setFecha_nacimiento(doc.getDate("fecha_nacimiento"));
        persona.setSexo(doc.getString("sexo"));
        persona.setEstado_civil(doc.getString("estado_civil"));
        persona.setUsername(doc.getString("username"));
        persona.setPassword_hash(doc.getString("password_hash"));
        persona.setFecha_ingreso(doc.getDate("fecha_ingreso"));
        persona.setImagen_perfil(doc.getString("imagen_perfil"));
        persona.setEstado(doc.getString("estado"));
        
        // Obtener el ID del rol desde el documento de persona
        String rolId = null;
        if (doc.containsKey("rol")) {
            if (doc.get("rol") instanceof Document) {
                Document rolDoc = (Document) doc.get("rol");
                if (rolDoc.containsKey("_id")) {
                    rolId = rolDoc.get("_id").toString();
                } else if (rolDoc.containsKey("codigo")) {
                    rolId = rolDoc.getString("codigo");
                }
            } else if (doc.get("rol") instanceof String) {
                rolId = doc.getString("rol");
            }
        }
        
        // Buscar el rol completo en la colección de roles
        if (rolId != null) {
            Rol rol = buscarRolPorId(rolId);
            if (rol != null) {
                persona.setRol(rol);
                System.out.println("Rol encontrado para " + persona.getUsername() + ": " + rol.getNombre());
                if (rol.getOpciones_permitidas() != null) {
                    System.out.println("Opciones del rol: " + rol.getOpciones_permitidas().size() + " opciones");
                }
            } else {
                System.out.println("No se encontró el rol con ID: " + rolId);
            }
        } else {
            System.out.println("No se encontró información del rol en el documento de persona");
        }
        
        return persona;
    }
    
    /**
     * Busca un rol por ID o código en la colección de roles
     */
    private Rol buscarRolPorId(String identificador) {
        try {
            Document rolDoc = null;
            
            // Intentar buscar por ObjectId primero
            try {
                org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(identificador);
                rolDoc = rolesCollection.find(Filters.eq("_id", objectId)).first();
            } catch (IllegalArgumentException e) {
                // Si no es un ObjectId válido, buscar por código
                rolDoc = rolesCollection.find(Filters.eq("codigo", identificador)).first();
            }
            
            if (rolDoc != null) {
                Rol rol = new Rol();
                
                
                if (rolDoc.containsKey("codigo")) {
                    rol.setCodigo(rolDoc.getString("codigo"));
                }
                if (rolDoc.containsKey("nombre")) {
                    rol.setNombre(rolDoc.getString("nombre"));
                }
                if (rolDoc.containsKey("descripcion")) {
                    rol.setDescripcion(rolDoc.getString("descripcion"));
                }
                if (rolDoc.containsKey("estado")) {
                    rol.setEstado(rolDoc.getString("estado"));
                }
                
                // Cargar opciones permitidas
                if (rolDoc.containsKey("opciones_permitidas")) {
                    List<String> opciones = rolDoc.getList("opciones_permitidas", String.class);
                    if (opciones != null) {
                        rol.setOpciones_permitidas(opciones);
                        System.out.println("Opciones cargadas desde colección roles: " + opciones);
                    } else {
                        // Alternativa si getList no funciona
                        Object opcionesObj = rolDoc.get("opciones_permitidas");
                        if (opcionesObj instanceof List) {
                            List<?> opcionesList = (List<?>) opcionesObj;
                            List<String> opcionesString = new ArrayList<>();
                            for (Object obj : opcionesList) {
                                if (obj != null) {
                                    opcionesString.add(obj.toString());
                                }
                            }
                            rol.setOpciones_permitidas(opcionesString);
                        }
                    }
                }
                
                return rol;
            }
        } catch (Exception e) {
            System.out.println("Error buscando rol: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Actualiza las opciones del rol comparando con el template
     */
    private void actualizarOpcionesDesdeTemplate(Rol rol) {
        if (rol == null || rol.getCodigo() == null) {
            return;
        }
        
        try {
            // Buscar template para este rol por código de rol
            Document templateDoc = templatesCollection.find(
                Filters.eq("codigo_rol", rol.getCodigo())
            ).first();
            
            if (templateDoc != null) {
                TemplateRol template = documentToTemplate(templateDoc);
                
                // Comparar opciones actuales con las del template
                List<String> opcionesActuales = rol.getOpciones_permitidas();
                List<String> opcionesTemplate = template.getOpciones_disponibles();
                
                if (opcionesActuales == null) {
                    opcionesActuales = new ArrayList<>();
                }
                
                if (opcionesTemplate != null && !opcionesTemplate.isEmpty()) {
                    // Verificar si hay opciones no permitidas por el template
                    List<String> opcionesNoPermitidas = new ArrayList<>();
                    for (String opcion : opcionesActuales) {
                        if (!opcionesTemplate.contains(opcion)) {
                            opcionesNoPermitidas.add(opcion);
                        }
                    }
                    
                    if (!opcionesNoPermitidas.isEmpty()) {
                        System.out.println("Advertencia: El rol " + rol.getNombre() + 
                                         " tiene opciones no permitidas por el template: " + 
                                         opcionesNoPermitidas);
                        
                        // Puedes optar por remover las opciones no permitidas
                        // opcionesActuales.removeAll(opcionesNoPermitidas);
                        // rol.setOpciones_permitidas(opcionesActuales);
                    }
                    
                    // También podrías agregar opciones que falten del template
                    List<String> opcionesFaltantes = new ArrayList<>();
                    for (String opcionTemplate : opcionesTemplate) {
                        if (!opcionesActuales.contains(opcionTemplate)) {
                            opcionesFaltantes.add(opcionTemplate);
                        }
                    }
                    
                    if (!opcionesFaltantes.isEmpty()) {
                        System.out.println("Información: Al rol " + rol.getNombre() + 
                                         " le faltan opciones del template: " + 
                                         opcionesFaltantes);
                    }
                }
                
                // Guardar el template asociado al rol para referencia futura
                rol.setTemplateAsociado(template);
            }
        } catch (Exception e) {
            System.out.println("Error comparando con template: " + e.getMessage());
        }
    }
    
    /**
     * Convierte un Document a TemplateRol
     */
    private TemplateRol documentToTemplate(Document doc) {
        TemplateRol template = new TemplateRol();
        
        if (doc.getObjectId("_id") != null) {
            template.setId(doc.getObjectId("_id"));
        }
        if (doc.containsKey("codigo_rol")) {
            template.setCodigoRol(doc.getString("codigo_rol"));
        }
        if (doc.containsKey("nombre_template")) {
            template.setNombreTemplate(doc.getString("nombre_template"));
        }
        if (doc.containsKey("descripcion")) {
            template.setDescripcion(doc.getString("descripcion"));
        }
        if (doc.containsKey("opciones_disponibles")) {
            List<String> opciones = doc.getList("opciones_disponibles", String.class);
            template.setOpciones_disponibles(opciones);
        }
        
        return template;
    }
    
    private void cargarDatosUsuarioCache(Persona usuario) {
        try {
            usu.setUsuario(usuario.getUsername());
            usu.setNombre(usuario.getNombres() + " " + usuario.getApellidos());
            usu.setEmail(usuario.getEmail());
            usu.setDocumento(usuario.getDocumento());
            usu.setTipoPersona(usuario.getPeperTipo());
            
            if (usuario.getRol() != null) {
                usu.setRol(usuario.getRol().getNombre());
                usu.setCodigoRol(usuario.getRol().getCodigo());
                usu.setOpciones(usuario.getRol().getOpciones_permitidas());
            }
            
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usu", usu);
            
            System.out.println("Usuario cargado en cache: " + usuario.getUsername());
            System.out.println("Rol: " + (usuario.getRol() != null ? usuario.getRol().getNombre() : "Sin rol"));
        } catch (Exception e) {
            System.out.println("Error cargando datos de usuario en cache: " + e.getMessage());
        }
    }
    
    // ============ FUNCIONES PARA VERIFICAR PERMISOS ============
    
    public boolean tieneOpcion(String codigoOpcion) {
        Persona usuario = getUsuarioLogueado();
        
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        
        return usuario.getRol().tieneOpcion(codigoOpcion);
    }
    
    public boolean tieneOpcionEnGrupo(String... codigosOpciones) {
        Persona usuario = getUsuarioLogueado();
        
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        
        List<String> opciones = usuario.getRol().getOpciones_permitidas();
        if (opciones == null || opciones.isEmpty()) {
            return false;
        }
        
        for (String codigo : codigosOpciones) {
            if (opciones.contains(codigo)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean tieneTodasOpciones(String... codigosOpciones) {
        Persona usuario = getUsuarioLogueado();
        
        if (usuario == null || usuario.getRol() == null) {
            return false;
        }
        
        List<String> opciones = usuario.getRol().getOpciones_permitidas();
        if (opciones == null) {
            return false;
        }
        
        for (String codigo : codigosOpciones) {
            if (!opciones.contains(codigo)) {
                return false;
            }
        }
        
        return true;
    }
    
    public List<String> obtenerOpcionesPorGrupo(String prefijoGrupo) {
        Persona usuario = getUsuarioLogueado();
        List<String> resultado = new ArrayList<>();
        
        if (usuario == null || usuario.getRol() == null) {
            return resultado;
        }
        
        List<String> opciones = usuario.getRol().getOpciones_permitidas();
        if (opciones == null) {
            return resultado;
        }
        
        for (String opcion : opciones) {
            if (opcion != null && opcion.startsWith(prefijoGrupo)) {
                resultado.add(opcion);
            }
        }
        
        return resultado;
    }
    
    public String getNombreRolUsuario() {
        Persona usuario = getUsuarioLogueado();
        return usuario != null && usuario.getRol() != null ? usuario.getRol().getNombre() : "";
    }
    
    public String getCodigoRolUsuario() {
        Persona usuario = getUsuarioLogueado();
        return usuario != null && usuario.getRol() != null ? usuario.getRol().getCodigo() : "";
    }
    
    public List<String> getOpcionesUsuario() {
        Persona usuario = getUsuarioLogueado();
        return usuario != null && usuario.getRol() != null ? 
               usuario.getRol().getOpciones_permitidas() : new ArrayList<>();
    }
    
    private boolean necesitaCambiarContrasena(Persona usuario) {
        return false;
    }

    public String doLogout() throws IOException {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usuario");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usu");
        
        if (conexionMongo != null && conexionMongo.isConectado()) {
            conexionMongo.cerrarConexion();
        }
        
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/Monster_University/faces/login.xhtml");
        return null;
    }
    
    public boolean isLoggedIn() {
        return getUsuarioLogueado() != null;
    }

    public Persona getUsuarioLogueado() {
        return (Persona) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuario");
    }
    
    public String getNombreUsuario() {
        Persona usuario = getUsuarioLogueado();
        if (usuario != null) {
            return usuario.getNombres() + " " + usuario.getApellidos();
        }
        return "";
    }
    
    public String getTipoPersona() {
        Persona usuario = getUsuarioLogueado();
        if (usuario != null) {
            return usuario.getPeperTipo();
        }
        return "";
    }
    
    public String obtenerDescripcionOpcion(String codigoOpcion) {
        Map<String, String> descripciones = new HashMap<>();
        descripciones.put("PER", "Acceso al módulo de Personal");
        descripciones.put("FIN", "Acceso al módulo de Finanzas");
        descripciones.put("ACA", "Acceso al módulo Académico");
        descripciones.put("SEG", "Acceso al módulo de Seguridad");
        descripciones.put("CRE", "Crear Personal");
        descripciones.put("AC1", "Administrar Carreras");
        descripciones.put("AC2", "Reporte de Carreras");
        descripciones.put("SE1", "Administrar Roles");
        descripciones.put("SE2", "Asignar Roles");
        descripciones.put("SE3", "Asignar Opciones a Roles");
        
        return descripciones.getOrDefault(codigoOpcion, "Opción no definida");
    }
    
    // Getters y Setters
    public Persona getUsuario() {
        return usuario;
    }

    public void setUsuario(Persona usuario) {
        this.usuario = usuario;
    }

    public ExternalContext getContext() {
        return context;
    }

    public void setContext(ExternalContext context) {
        this.context = context;
    }

    public UserCache getUsu() {
        return usu;
    }

    public void setUsu(UserCache usu) {
        this.usu = usu;
    }
}