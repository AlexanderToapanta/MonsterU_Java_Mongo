/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.controlador;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.modelo.UserCache;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
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

    public LoginController() {
        usuario = new Persona();
        passController = new PasswordController();
        conexionMongo = new Conexion();
    }

    @PostConstruct
    public void init() {
        // Inicializar conexión a MongoDB
        try {
            conexionMongo.crearConexion();
            if (conexionMongo.isConectado()) {
                MongoDatabase database = conexionMongo.getDataB();
                personasCollection = database.getCollection("personas");
                System.out.println("Conexión a MongoDB establecida para login");
            } else {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "No se pudo conectar a MongoDB");
            }
            
            // Verificar si ya hay un usuario logueado
            Persona usuarioLogueado = getUsuarioLogueado();
            if (usuarioLogueado != null) {
                // Si ya hay sesión, cargar datos en cache
                cargarDatosUsuarioCache(usuarioLogueado);
            }
        } catch (Exception e) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, "Error al inicializar MongoDB", e);
        }
    }

    public void doLogin() throws NoSuchAlgorithmException, IOException {
        String username = usuario.getUsername();
        String password = usuario.getPassword_hash();
        
        // Validar campos
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario y contraseña requeridos"));
            return;
        }
        
        // Encriptar la contraseña ingresada
        String claveCifrada = passController.encriptarClave(password);
        
        // Verificar conexión a MongoDB
        if (!conexionMongo.isConectado()) {
            conexionMongo.crearConexion();
            if (conexionMongo.isConectado()) {
                MongoDatabase database = conexionMongo.getDataB();
                personasCollection = database.getCollection("personas");
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo conectar a la base de datos"));
                return;
            }
        }
        
        // Buscar usuario en MongoDB
        Document personaDoc = personasCollection.find(
            Filters.and(
                Filters.eq("username", username),
                Filters.eq("password_hash", claveCifrada)
            )
        ).first();
        
        if (personaDoc != null) {
            Persona usuarioLogueado = documentToPersona(personaDoc);
            
            // Verificar estado del usuario
            if (!"ACTIVO".equals(usuarioLogueado.getEstado())) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario inactivo"));
                return;
            }
            
            // Guardar usuario en sesión
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usuario", usuarioLogueado);
            
            // Cargar datos en cache
            cargarDatosUsuarioCache(usuarioLogueado);
            
            // Mensaje de éxito
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Login exitoso"));
            
            // Redirigir a crearpersonal.xhtml
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("/Monster_University/faces/index1.xhtml");
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Credenciales incorrectas"));
        }
    }
    
    private void cargarDatosUsuarioCache(Persona usuario) {
        try {
            // Cargar información básica del usuario desde MongoDB
            usu.setUsuario(usuario.getUsername());
            usu.setNombre(usuario.getNombres() + " " + usuario.getApellidos());
            usu.setEmail(usuario.getEmail());
            usu.setDocumento(usuario.getDocumento());
            usu.setTipoPersona(usuario.getPeperTipo());
            
            // Guardar cache en sesión
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("usu", usu);
            
            System.out.println("Usuario cargado en cache: " + usuario.getUsername());
            System.out.println("Nombre completo: " + usuario.getNombres() + " " + usuario.getApellidos());
            
        } catch (Exception e) {
            System.out.println("Error cargando datos de usuario en cache: " + e.getMessage());
        }
    }
    
    /**
     * Convierte un Document de MongoDB a un objeto PersonaMongo
     */
    private Persona documentToPersona(Document doc) {
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
        
        return persona;
    }
    
    private boolean necesitaCambiarContrasena(Persona usuario) {
        // Por ahora retornamos false, puedes implementar esta lógica después
        return false;
    }

    public String doLogout() throws IOException {
        // Limpiar sesión
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usuario");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usu");
        
        // Cerrar conexión MongoDB
        if (conexionMongo != null && conexionMongo.isConectado()) {
            conexionMongo.cerrarConexion();
        }
        
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        
        // Redirigir al login
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/Monster_University/faces/login.xhtml");
        return null;
    }
    
    /**
     * Método para verificar si hay sesión activa
     */
    public boolean isLoggedIn() {
        return getUsuarioLogueado() != null;
    }

    /**
     * Obtiene el usuario logueado de la sesión
     */
    public Persona getUsuarioLogueado() {
        return (Persona) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("usuario");
    }
    
    /**
     * Obtiene el nombre completo del usuario logueado
     */
    public String getNombreUsuario() {
        Persona usuario = getUsuarioLogueado();
        if (usuario != null) {
            return usuario.getNombres() + " " + usuario.getApellidos();
        }
        return "";
    }
    
    /**
     * Obtiene el tipo de persona del usuario logueado
     */
    public String getTipoPersona() {
        Persona usuario = getUsuarioLogueado();
        if (usuario != null) {
            return usuario.getPeperTipo();
        }
        return "";
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