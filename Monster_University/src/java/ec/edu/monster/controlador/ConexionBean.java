package ec.edu.monster.controlador;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class ConexionBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private transient Conexion conexion;
    private String mensaje;
    private boolean conectado;
    
    public ConexionBean() {
        mensaje = "";
        conectado = false;
        System.out.println("ConexionBean inicializado");
    }
    
    // Método para probar la conexión
    public void probarConexion() {
        System.out.println("Ejecutando probarConexion()");
        
        try {
            conexion = new Conexion().crearConexion();
            
            if (conexion != null && conexion.isConectado()) {
                mensaje = "¡Conexión exitosa a MongoDB local! " + 
                          "Base de datos: " + conexion.getNombreBaseDatos();
                conectado = true;
                System.out.println("Conexión exitosa: " + mensaje);
                
                // Añadir mensaje JSF
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Conectado a MongoDB"));
            } else {
                mensaje = "Error: No se pudo conectar a MongoDB local. " +
                          "Verifica que MongoDB esté ejecutándose.";
                conectado = false;
                System.out.println("Error de conexión: " + mensaje);
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo conectar"));
            }
        } catch (Exception e) {
            mensaje = "Excepción al conectar: " + e.getMessage();
            conectado = false;
            System.err.println("Excepción en probarConexion: " + e.getMessage());
            e.printStackTrace();
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_FATAL, "Error", e.getMessage()));
        }
    }
    
    // Método para cerrar la conexión
    public void cerrarConexion() {
        System.out.println("Ejecutando cerrarConexion()");
        
        if (conexion != null) {
            conexion.cerrarConexion();
            mensaje = "Conexión cerrada correctamente.";
            conectado = false;
            conexion = null;
            System.out.println("Conexión cerrada");
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Conexión cerrada"));
        } else {
            mensaje = "No hay conexión activa para cerrar.";
            System.out.println("No hay conexión activa");
        }
    }
    
    // Método para obtener información de la base de datos
    public String obtenerInfoBaseDatos() {
        if (conexion != null && conexion.getDataB() != null) {
            return "Base de datos: " + conexion.getDataB().getName();
        } else if (conexion != null) {
            return "Base de datos: " + conexion.getNombreBaseDatos();
        }
        return "No hay conexión activa";
    }
    
    // Getters y Setters
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isConectado() {
        return conectado;
    }

    public void setConectado(boolean conectado) {
        this.conectado = conectado;
    }
    
    public Conexion getConexion() {
        return conexion;
    }
    
    public void setConexion(Conexion conexion) {
        this.conexion = conexion;
    }
}