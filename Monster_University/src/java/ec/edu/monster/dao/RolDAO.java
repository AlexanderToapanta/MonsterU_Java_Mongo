package ec.edu.monster.dao;

import ec.edu.monster.controlador.Conexion;
import ec.edu.monster.modelo.Rol;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class RolDAO {
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    
    public RolDAO() {
        Conexion conexion = new Conexion().crearConexion();
        if (conexion != null && conexion.isConectado()) {
            database = conexion.getDataB();
            collection = database.getCollection("roles");
            System.out.println("Colección 'roles' obtenida correctamente");
        } else {
            System.err.println("Error: No hay conexión a la base de datos");
        }
    }
    
    // Crear nuevo rol
    public boolean crearRol(Rol rol) {
        try {
            Document docRol = new Document()
                .append("codigo", rol.getCodigo())
                .append("nombre", rol.getNombre())
                .append("descripcion", rol.getDescripcion())
                .append("opciones_permitidas", rol.getOpciones_permitidas())
                .append("estado", rol.getEstado());
            
            collection.insertOne(docRol);
            System.out.println("Rol creado: " + rol.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear rol: " + e.getMessage());
            return false;
        }
    }
    
    // Buscar rol por código
    public Rol buscarPorCodigo(String codigo) {
        try {
            Document doc = collection.find(Filters.eq("codigo", codigo)).first();
            if (doc != null) {
                return convertirDocumentARol(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar rol por código: " + e.getMessage());
        }
        return null;
    }
    
    // Buscar rol por ID
    public Rol buscarPorId(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Document doc = collection.find(Filters.eq("_id", objectId)).first();
            if (doc != null) {
                return convertirDocumentARol(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar rol por ID: " + e.getMessage());
        }
        return null;
    }
    
    // Listar todos los roles
    public List<Rol> listarTodos() {
        List<Rol> roles = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                roles.add(convertirDocumentARol(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return roles;
    }
    
    // Listar roles activos
    public List<Rol> listarActivos() {
        List<Rol> roles = new ArrayList<>();
        try {
            Bson filtro = Filters.eq("estado", "ACTIVO");
            for (Document doc : collection.find(filtro)) {
                roles.add(convertirDocumentARol(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al listar roles activos: " + e.getMessage());
        }
        return roles;
    }
    
    // Actualizar rol
    public boolean actualizarRol(Rol rol) {
        try {
            Bson filtro = Filters.eq("codigo", rol.getCodigo());
            Bson update = Updates.combine(
                Updates.set("nombre", rol.getNombre()),
                Updates.set("descripcion", rol.getDescripcion()),
                Updates.set("opciones_permitidas", rol.getOpciones_permitidas()),
                Updates.set("estado", rol.getEstado())
            );
            
            collection.updateOne(filtro, update);
            System.out.println("Rol actualizado: " + rol.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar rol: " + e.getMessage());
            return false;
        }
    }
    
    // Agregar opción a rol
    public boolean agregarOpcion(String rolCodigo, String opcion) {
        try {
            Bson filtro = Filters.eq("codigo", rolCodigo);
            Bson update = Updates.addToSet("opciones_permitidas", opcion);
            collection.updateOne(filtro, update);
            System.out.println("Opción " + opcion + " agregada a rol: " + rolCodigo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al agregar opción: " + e.getMessage());
            return false;
        }
    }
    
    // Eliminar opción de rol
    public boolean eliminarOpcion(String rolCodigo, String opcion) {
        try {
            Bson filtro = Filters.eq("codigo", rolCodigo);
            Bson update = Updates.pull("opciones_permitidas", opcion);
            collection.updateOne(filtro, update);
            System.out.println("Opción " + opcion + " eliminada de rol: " + rolCodigo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar opción: " + e.getMessage());
            return false;
        }
    }
    
    // Cambiar estado de rol
    public boolean cambiarEstado(String rolCodigo, String nuevoEstado) {
        try {
            Bson filtro = Filters.eq("codigo", rolCodigo);
            Bson update = Updates.set("estado", nuevoEstado);
            collection.updateOne(filtro, update);
            System.out.println("Estado cambiado a " + nuevoEstado + " para rol: " + rolCodigo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }
    
    // Verificar si existe rol
    public boolean existeRol(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }
    
    // Obtener opciones permitidas por rol
    public List<String> obtenerOpcionesPorRol(String rolCodigo) {
        Rol rol = buscarPorCodigo(rolCodigo);
        return rol != null ? rol.getOpciones_permitidas() : new ArrayList<>();
    }
    
    // Verificar si rol tiene permiso para una opción
    public boolean tienePermiso(String rolCodigo, String opcion) {
        Rol rol = buscarPorCodigo(rolCodigo);
        return rol != null && rol.tieneOpcion(opcion);
    }
    
    // Convertir Document a Rol
    private Rol convertirDocumentARol(Document doc) {
        Rol rol = new Rol();
        
        rol.setCodigo(doc.getString("codigo"));
        rol.setNombre(doc.getString("nombre"));
        rol.setDescripcion(doc.getString("descripcion"));
        
        // Obtener lista de opciones
        List<String> opciones = (List<String>) doc.get("opciones_permitidas");
        if (opciones != null) {
            rol.setOpciones_permitidas(opciones);
        }
        
        rol.setEstado(doc.getString("estado"));
        
        return rol;
    }
}