/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.controlador;

import ec.edu.monster.dao.PersonaDAO;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.modelo.UserCache;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named(value = "loginController")
@SessionScoped
public class LoginController implements Serializable {
    
    private String username;
    private String password;
    private boolean rememberMe;
    private ExternalContext context = FacesContext.getCurrentInstance().getExternalContext(); 
    private UserCache usu = new UserCache();
    
    private PersonaDAO personaDAO;
    
    public LoginController() {
        personaDAO = new PersonaDAO();
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
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
    
    public void doLogin() throws NoSuchAlgorithmException, IOException {
        System.out.println("=== INICIANDO LOGIN ===");
        System.out.println("Usuario: " + username);
        
        if (username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrese un usuario"));
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ingrese una contraseña"));
            return;
        }
        
        // Buscar persona por username en MongoDB
        Persona persona = personaDAO.buscarPorUsername(username);
        
        if (persona == null) {
            System.out.println("❌ Usuario no encontrado: " + username);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario no encontrado"));
            return;
        }
        
        // Verificar estado de la persona
        if (!"ACTIVO".equals(persona.getEstado())) {
            System.out.println("⚠️ Usuario inactivo: " + username);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Usuario inactivo"));
            return;
        }
        
        // Verificar contraseña
        String passwordHash = generarHashSHA256(password);
        String storedHash = persona.getPassword_hash();
        
        System.out.println("🔑 Contraseña ingresada (hash): " + passwordHash.substring(0, 10) + "...");
        System.out.println("🔑 Contraseña almacenada (hash): " + (storedHash != null ? storedHash.substring(0, 10) + "..." : "null"));
        
        if (!passwordHash.equals(storedHash)) {
            System.out.println("❌ Contraseña incorrecta para usuario: " + username);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Contraseña incorrecta"));
            return;
        }
        
        // Login exitoso
        System.out.println("✅ LOGIN EXITOSO - Usuario: " + username);
        
        // Guardar persona en sesión
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("persona", persona);
        
        // Cargar datos en cache
        cargarDatosUsuarioCache(persona);
        
        // Limpiar campos del formulario
        username = null;
        password = null;
        
        // Redirección
        System.out.println("🔄 Redirigiendo a index1.xhtml");
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/Monster_University/faces/index1.xhtml");
    }
    
    private void cargarDatosUsuarioCache(Persona persona) {
        try {
            // Cargar información básica de la persona
            usu.setUsuario(persona.getUsername());
            usu.setNombre(persona.getNombreCompleto());
            usu.setEmail(persona.getEmail());
            usu.setCodigo(persona.getCodigo());
            
            // Si tiene rol, cargarlo
            if (persona.getRol() != null) {
                usu.setRol(persona.getRol().getNombre());
            } else {
                usu.setRol("Sin rol asignado");
            }
            
            System.out.println("✅ Datos cargados en cache:");
            System.out.println("   Usuario: " + usu.getUsuario());
            System.out.println("   Nombre: " + usu.getNombre());
            System.out.println("   Email: " + usu.getEmail());
            System.out.println("   Código: " + usu.getCodigo());
            System.out.println("   Rol: " + usu.getRol());
            
        } catch (Exception e) {
            System.out.println("❌ Error cargando datos de usuario en cache: " + e.getMessage());
        }
    }
    
    public String doLogout() throws IOException {
        System.out.println("=== CERRANDO SESIÓN ===");
        
        // Obtener usuario antes de limpiar
        Persona persona = getPersonaLogueada();
        if (persona != null) {
            System.out.println("👤 Cerrando sesión de: " + persona.getUsername());
        }
        
        // Limpiar sesión
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("persona");
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("usu");
        
        // Limpiar cache del usuario
        usu.clear();
        
        // Limpiar variables locales
        username = null;
        password = null;
        rememberMe = false;
        
        // Invalidar sesión
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        
        System.out.println("✅ Sesión cerrada exitosamente");
        
        // Redirigir al login
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/Monster_University/faces/login.xhtml");
        return null;
    }
    
    /**
     * Método para verificar si hay sesión activa
     */
    public boolean isLoggedIn() {
        return getPersonaLogueada() != null;
    }

    /**
     * Obtiene la persona logueada de la sesión
     */
    public Persona getPersonaLogueada() {
        return (Persona) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("persona");
    }
    
    /**
     * Obtiene el nombre del rol del usuario logueado
     */
    public String getNombreRolUsuario() {
        Persona persona = getPersonaLogueada();
        if (persona != null && persona.getRol() != null) {
            return persona.getRol().getNombre();
        }
        return "Sin rol asignado";
    }
    
    /**
     * Obtiene el nombre completo del usuario logueado
     */
    public String getNombreCompletoUsuario() {
        Persona persona = getPersonaLogueada();
        if (persona != null) {
            return persona.getNombreCompleto();
        }
        return "";
    }
    
    /**
     * Obtiene el email del usuario logueado
     */
    public String getEmailUsuario() {
        Persona persona = getPersonaLogueada();
        if (persona != null) {
            return persona.getEmail();
        }
        return "";
    }
    
    /**
     * Obtiene el código de la persona logueada
     */
    public String getCodigoUsuario() {
        Persona persona = getPersonaLogueada();
        if (persona != null) {
            return persona.getCodigo();
        }
        return "";
    }
    
    /**
     * Generar hash SHA-256 de una cadena
     */
    private String generarHashSHA256(String texto) {
        try {
            if (texto == null || texto.isEmpty()) {
                return "";
            }
            
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(texto.getBytes("UTF-8"));
            
            // Convertir bytes a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al generar hash SHA-256: " + e.getMessage());
            // Fallback simple
            return Integer.toHexString(texto.hashCode());
        }
    }
    
    /**
     * Verifica si el usuario actual tiene rol asignado
     */
    public boolean tieneRol() {
        Persona persona = getPersonaLogueada();
        return persona != null && persona.getRol() != null;
    }
}