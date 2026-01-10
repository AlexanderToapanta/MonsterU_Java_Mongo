package ec.edu.monster.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ec.edu.monster.controlador.Conexion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ConfiguracionDAO {
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private final String COLECCION = "configuraciones";
    
    public ConfiguracionDAO() {
        Conexion conexion = new Conexion().crearConexion();
        if (conexion != null && conexion.isConectado()) {
            database = conexion.getDataB();
            collection = database.getCollection(COLECCION);
            System.out.println("Colección '" + COLECCION + "' obtenida correctamente");
        } else {
            System.err.println("Error: No hay conexión a la base de datos");
            throw new RuntimeException("No se pudo conectar a la base de datos");
        }
    }
    
    // Obtener todos los valores de un tipo específico (sexo, estado_civil, etc.)
    public List<Map<String, String>> obtenerValoresPorTipo(String tipo) {
        List<Map<String, String>> valores = new ArrayList<>();
        
        try {
            Document doc = collection.find(Filters.eq("tipo", tipo)).first();
            
            if (doc != null && doc.containsKey("valores")) {
                List<Document> listaValores = (List<Document>) doc.get("valores");
                
                for (Document valorDoc : listaValores) {
                    Map<String, String> valor = new HashMap<>();
                    valor.put("codigo", valorDoc.getString("codigo"));
                    valor.put("nombre", valorDoc.getString("nombre"));
                    valores.add(valor);
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener valores de tipo " + tipo + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return valores;
    }
    
    // Obtener todos los tipos de configuraciones disponibles
    public List<String> obtenerTipos() {
        List<String> tipos = new ArrayList<>();
        
        try {
            for (Document doc : collection.find()) {
                if (doc.containsKey("tipo")) {
                    tipos.add(doc.getString("tipo"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener tipos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return tipos;
    }
    
    // Obtener el nombre de un código específico
    public String obtenerNombrePorCodigo(String tipo, String codigo) {
        try {
            Document doc = collection.find(Filters.eq("tipo", tipo)).first();
            
            if (doc != null && doc.containsKey("valores")) {
                List<Document> listaValores = (List<Document>) doc.get("valores");
                
                for (Document valorDoc : listaValores) {
                    if (valorDoc.getString("codigo").equals(codigo)) {
                        return valorDoc.getString("nombre");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener nombre: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null; // Retornar null en lugar del código para indicar que no se encontró
    }
    
    // Obtener el código por nombre
    public String obtenerCodigoPorNombre(String tipo, String nombre) {
        try {
            Document doc = collection.find(Filters.eq("tipo", tipo)).first();
            
            if (doc != null && doc.containsKey("valores")) {
                List<Document> listaValores = (List<Document>) doc.get("valores");
                
                for (Document valorDoc : listaValores) {
                    if (valorDoc.getString("nombre").equalsIgnoreCase(nombre)) {
                        return valorDoc.getString("codigo");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener código: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Obtener documento completo por tipo
    public Document obtenerDocumentoPorTipo(String tipo) {
        try {
            return collection.find(Filters.eq("tipo", tipo)).first();
        } catch (Exception e) {
            System.err.println("Error al obtener documento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Verificar si un tipo de configuración existe
    public boolean existeTipo(String tipo) {
        try {
            long count = collection.countDocuments(Filters.eq("tipo", tipo));
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error al verificar tipo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Agregar nuevo valor a un tipo existente
    public boolean agregarValor(String tipo, String codigo, String nombre) {
        try {
            Document nuevoValor = new Document("codigo", codigo)
                    .append("nombre", nombre);
            
            Document update = new Document("$push", 
                new Document("valores", nuevoValor)
            );
            
            collection.updateOne(Filters.eq("tipo", tipo), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al agregar valor: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}