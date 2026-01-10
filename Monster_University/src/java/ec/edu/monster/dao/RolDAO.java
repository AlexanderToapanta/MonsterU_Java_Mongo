package ec.edu.monster.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ec.edu.monster.controlador.Conexion;
import ec.edu.monster.modelo.Rol;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;

public class RolDAO {
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private final String COLECCION = "roles";
    
    public RolDAO() {
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
    
    // Método 1: Verificar si existe un rol con el código especificado
    public boolean existeCodigo(String codigo) {
        try {
            long count = collection.countDocuments(Filters.eq("codigo", codigo));
            return count > 0;
        } catch (Exception e) {
            System.err.println("Error al verificar código: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Método 2: Crear un nuevo rol (para nuevo rol)
    public boolean crearRol(Rol rol) {
        try {
            Document doc = new Document("codigo", rol.getCodigo())
                    .append("nombre", rol.getNombre())
                    .append("descripcion", rol.getDescripcion())
                    .append("estado", rol.getEstado())
                    .append("opciones_permitidas", rol.getOpciones_permitidas());
            
            collection.insertOne(doc);
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear rol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Método 3: Actualizar un rol existente
    public boolean actualizarRol(Rol rol) {
        try {
            // Primero buscamos el rol por su código
            Document rolExistente = collection.find(Filters.eq("codigo", rol.getCodigo())).first();
            
            if (rolExistente == null) {
                System.err.println("Rol no encontrado con código: " + rol.getCodigo());
                return false;
            }
            
            ObjectId objectId = rolExistente.getObjectId("_id");
            
            Document updateDoc = new Document()
                    .append("nombre", rol.getNombre())
                    .append("descripcion", rol.getDescripcion())
                    .append("estado", rol.getEstado())
                    .append("opciones_permitidas", rol.getOpciones_permitidas());
            
            Document update = new Document("$set", updateDoc);
            
            collection.updateOne(Filters.eq("_id", objectId), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar rol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Método 4: Eliminar un rol por código
    public boolean eliminarRol(String codigoRol) {
        try {
            // Primero buscamos el rol por su código
            Document rolExistente = collection.find(Filters.eq("codigo", codigoRol)).first();
            
            if (rolExistente == null) {
                System.err.println("Rol no encontrado con código: " + codigoRol);
                return false;
            }
            
            ObjectId objectId = rolExistente.getObjectId("_id");
            collection.deleteOne(Filters.eq("_id", objectId));
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar rol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Método 5: Listar todos los roles
    public List<Rol> listarTodos() {
        List<Rol> roles = new ArrayList<>();
        
        try {
            for (Document doc : collection.find()) {
                Rol rol = convertirDocumentoARol(doc);
                roles.add(rol);
            }
        } catch (Exception e) {
            System.err.println("Error al listar roles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return roles;
    }
    
    // Método 6: Buscar rol por código
    public Rol buscarPorCodigo(String codigo) {
        try {
            Document doc = collection.find(Filters.eq("codigo", codigo)).first();
            
            if (doc != null) {
                return convertirDocumentoARol(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar rol por código: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Método 7: Convertir Document de MongoDB a objeto Rol
    private Rol convertirDocumentoARol(Document doc) {
        Rol rol = new Rol();
        
        if (doc.containsKey("codigo")) {
            rol.setCodigo(doc.getString("codigo"));
        }
        
        if (doc.containsKey("nombre")) {
            rol.setNombre(doc.getString("nombre"));
        }
        
        if (doc.containsKey("descripcion")) {
            rol.setDescripcion(doc.getString("descripcion"));
        }
        
        if (doc.containsKey("estado")) {
            rol.setEstado(doc.getString("estado"));
        }
        
        if (doc.containsKey("opciones_permitidas")) {
            rol.setOpciones_permitidas((List<String>) doc.get("opciones_permitidas"));
        } else {
            rol.setOpciones_permitidas(new ArrayList<>());
        }
        
        return rol;
    }
    
    // Método 8: Buscar roles activos
    public List<Rol> buscarRolesActivos() {
        List<Rol> roles = new ArrayList<>();
        
        try {
            for (Document doc : collection.find(Filters.eq("estado", "ACTIVO"))) {
                Rol rol = convertirDocumentoARol(doc);
                roles.add(rol);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar roles activos: " + e.getMessage());
            e.printStackTrace();
        }
        
        return roles;
    }
    
    // Método 9: Contar total de roles
    public long contarTotalRoles() {
        try {
            return collection.countDocuments();
        } catch (Exception e) {
            System.err.println("Error al contar roles: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    // Método 10: Actualizar solo las opciones permitidas
    public boolean actualizarOpcionesPermitidas(String codigoRol, List<String> opcionesPermitidas) {
        try {
            Document updateDoc = new Document("opciones_permitidas", opcionesPermitidas);
            Document update = new Document("$set", updateDoc);
            
            collection.updateOne(Filters.eq("codigo", codigoRol), update);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar opciones permitidas: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}