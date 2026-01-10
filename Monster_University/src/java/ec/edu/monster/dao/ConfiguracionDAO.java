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

public class ConfiguracionDAO {
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    
    public ConfiguracionDAO() {
        Conexion conexion = new Conexion().crearConexion();
        if (conexion != null && conexion.isConectado()) {
            database = conexion.getDataB();
            collection = database.getCollection("configuraciones");
            System.out.println("Colección 'configuraciones' obtenida correctamente");
        } else {
            System.err.println("Error: No hay conexión a la base de datos");
        }
    }
    
    // Obtener todos los valores de un tipo específico (sexo, estado_civil, etc.)
    public List<Map<String, String>> obtenerValoresPorTipo(String tipo) {
        List<Map<String, String>> valores = new ArrayList<>();
        
        try {
            Document doc = collection.find(Filters.eq("tipo", tipo)).first();
            
            if (doc != null) {
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
        }
        
        return valores;
    }
    
    // Obtener todos los tipos de configuraciones disponibles
    public List<String> obtenerTipos() {
        List<String> tipos = new ArrayList<>();
        
        try {
            for (Document doc : collection.find()) {
                tipos.add(doc.getString("tipo"));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener tipos: " + e.getMessage());
        }
        
        return tipos;
    }
    
    // Obtener el nombre de un código específico
    public String obtenerNombrePorCodigo(String tipo, String codigo) {
        try {
            Document doc = collection.find(Filters.eq("tipo", tipo)).first();
            
            if (doc != null) {
                List<Document> listaValores = (List<Document>) doc.get("valores");
                
                for (Document valorDoc : listaValores) {
                    if (valorDoc.getString("codigo").equals(codigo)) {
                        return valorDoc.getString("nombre");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al obtener nombre: " + e.getMessage());
        }
        
        return codigo; 
    }
}