package ec.edu.monster.controlador;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import java.io.Serializable;

public class Conexion implements Serializable {
    
    private static final long serialVersionUID = 2L;
    
    private transient MongoClient mongo;
    private transient MongoDatabase dataB;
    private String nombreBaseDatos;
    private String error;
    
    // Constructor vacío
    public Conexion() {
        this.nombreBaseDatos = "monster_university_neatbeans";
        this.error = "";
    }
    
    // Constructor con parámetros
    public Conexion(MongoClient mongo, MongoDatabase dataB) {
        this.mongo = mongo;
        this.dataB = dataB;
        if (dataB != null) {
            this.nombreBaseDatos = dataB.getName();
        } else {
            this.nombreBaseDatos = "monster_university_neatbeans";
        }
        this.error = "";
    }
    
    // Getters
    public MongoClient getMongo() {
        return mongo;
    }

    public MongoDatabase getDataB() {
        return dataB;
    }
    
    public String getNombreBaseDatos() {
        return nombreBaseDatos;
    }
    
    public String getError() {
        return error;
    }
    
    // Método para crear conexión
    public Conexion crearConexion() {
        // Conexión a MongoDB local (puerto por defecto 27017)
        String connectionString = "mongodb://localhost:27017";
        System.out.println("Intentando conectar a: " + connectionString);
        
        try {
            MongoClientURI uri = new MongoClientURI(connectionString);
            System.out.println("URI creada: " + uri);
            
            mongo = new MongoClient(uri);
            System.out.println("MongoClient creado");
            
            // Verificar conexión
            mongo.listDatabaseNames().first();
            System.out.println("Ping a MongoDB exitoso");
            
            dataB = mongo.getDatabase("monster_university_neatbeans");
            this.nombreBaseDatos = "monster_university_neatbeans";
            this.error = "";
            
            System.out.println("Conexión exitosa a MongoDB local. Base de datos: " + dataB.getName());
            
        } catch (Exception e) {
            this.error = e.getMessage();
            System.err.println("Error al conectar a MongoDB local: " + e.getMessage());
            e.printStackTrace();
            
            // Intentar conexión alternativa sin URI
            try {
                System.out.println("Intentando conexión directa...");
                mongo = new MongoClient("localhost", 27017);
                dataB = mongo.getDatabase("monster_university_neatbeans");
                this.nombreBaseDatos = "monster_university_neatbeans";
                this.error = "";
                System.out.println("Conexión directa exitosa");
            } catch (Exception ex2) {
                System.err.println("Error en conexión directa: " + ex2.getMessage());
                return null;
            }
        }
        return this;
    }
    
    // Método para cerrar la conexión
    public void cerrarConexion() {
        try {
            if (mongo != null) {
                mongo.close();
                System.out.println("Conexión a MongoDB cerrada correctamente.");
                mongo = null;
                dataB = null;
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }
    
    // Método para verificar el estado de la conexión
    public boolean isConectado() {
        if (mongo == null || dataB == null) {
            System.out.println("Conexion.isConectado(): false (mongo o dataB es null)");
            return false;
        }
        
        try {
            // Intentar una operación simple para verificar la conexión
            mongo.listDatabaseNames().first();
            System.out.println("Conexion.isConectado(): true");
            return true;
        } catch (Exception e) {
            System.out.println("Conexion.isConectado(): false - " + e.getMessage());
            return false;
        }
    }
}