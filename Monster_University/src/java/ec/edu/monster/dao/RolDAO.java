package ec.edu.monster.dao;

import ec.edu.monster.controlador.Conexion;
import ec.edu.monster.modelo.Rol;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import static com.sun.xml.ws.spi.db.BindingContextFactory.LOGGER;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
    
    // ========== MÉTODOS CRUD ==========
    
    // Crear nuevo rol
    public boolean crearRol(Rol rol) {
        try {
            // Verificar si ya existe un rol con el mismo código
            if (existeRol(rol.getCodigo())) {
                System.err.println("Error: Ya existe un rol con el código: " + rol.getCodigo());
                return false;
            }
            
            Document docRol = new Document()
                .append("codigo", rol.getCodigo())
                .append("nombre", rol.getNombre())
                .append("descripcion", rol.getDescripcion())
                .append("opciones_permitidas", rol.getOpciones_permitidas() != null ? rol.getOpciones_permitidas() : new ArrayList<>())
                .append("estado", rol.getEstado() != null ? rol.getEstado() : "ACTIVO");
            
            collection.insertOne(docRol);
            System.out.println("Rol creado: " + rol.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear rol: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return null;
    }
    
    // Buscar rol por ID de MongoDB
    public Rol buscarPorId(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Document doc = collection.find(Filters.eq("_id", objectId)).first();
            if (doc != null) {
                return convertirDocumentARol(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar rol por ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    // Método find (alias de buscarPorCodigo para compatibilidad)
    public Rol find(String codigo) {
        return buscarPorCodigo(codigo);
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
            e.printStackTrace();
        }
        return roles;
    }
    
    // Alias para findAll (compatibilidad)
    public List<Rol> findAll() {
        return listarTodos();
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
            e.printStackTrace();
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
                Updates.set("opciones_permitidas", rol.getOpciones_permitidas() != null ? rol.getOpciones_permitidas() : new ArrayList<>()),
                Updates.set("estado", rol.getEstado() != null ? rol.getEstado() : "ACTIVO")
            );
            
            collection.updateOne(filtro, update);
            System.out.println("Rol actualizado: " + rol.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar rol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Alias para edit (compatibilidad)
    public boolean edit(Rol rol) {
        return actualizarRol(rol);
    }
    
    // Eliminar rol por código
    public boolean eliminarRol(String codigo) {
        try {
            Bson filtro = Filters.eq("codigo", codigo);
            collection.deleteOne(filtro);
            System.out.println("Rol eliminado: " + codigo);
            return true;
        } catch (Exception e) {
            System.err.println("Error al eliminar rol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Alias para remove (compatibilidad)
    public boolean remove(Rol rol) {
        return eliminarRol(rol.getCodigo());
    }
    
    // ========== MÉTODOS ESPECÍFICOS ==========
    
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }
    
    // ========== MÉTODOS DE CONSULTA ==========
    
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
    
    // Generar código automático
    public String generarCodigoAuto() {
        try {
            List<Rol> todosRoles = listarTodos();
            int maxNum = 0;
            
            for (Rol r : todosRoles) {
                if (r.getCodigo() != null && r.getCodigo().matches("ROL-\\d+")) {
                    String numStr = r.getCodigo().substring(4);
                    try {
                        int num = Integer.parseInt(numStr);
                        if (num > maxNum) maxNum = num;
                    } catch (NumberFormatException e) {
                        // Ignorar códigos que no siguen el formato
                    }
                }
            }
            
            return String.format("ROL-%03d", maxNum + 1);
        } catch (Exception e) {
            return "ROL-001";
        }
    }
    
    // Contar total de roles
    public long contarRoles() {
        try {
            return collection.countDocuments();
        } catch (Exception e) {
            System.err.println("Error al contar roles: " + e.getMessage());
            return 0;
        }
    }
    
    // Buscar roles por nombre (búsqueda parcial)
    public List<Rol> buscarPorNombre(String nombre) {
        List<Rol> roles = new ArrayList<>();
        try {
            // Búsqueda case-insensitive y parcial
            Bson filtro = Filters.regex("nombre", ".*" + nombre + ".*", "i");
            for (Document doc : collection.find(filtro)) {
                roles.add(convertirDocumentARol(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar roles por nombre: " + e.getMessage());
            e.printStackTrace();
        }
        return roles;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    // Convertir Document a Rol
    private Rol convertirDocumentARol(Document doc) {
        Rol rol = new Rol();
        
        try {
            // Campos básicos
            rol.setCodigo(doc.getString("codigo"));
            rol.setNombre(doc.getString("nombre"));
            rol.setDescripcion(doc.getString("descripcion"));
            
            // Obtener lista de opciones
            List<String> opciones = (List<String>) doc.get("opciones_permitidas");
            if (opciones != null) {
                rol.setOpciones_permitidas(opciones);
            } else {
                rol.setOpciones_permitidas(new ArrayList<>());
            }
            
            // Estado (por defecto ACTIVO si no existe)
            String estado = doc.getString("estado");
            rol.setEstado(estado != null ? estado : "ACTIVO");
            
        } catch (Exception e) {
            System.err.println("Error al convertir Document a Rol: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rol;
    }
    
    // Método para debug
    public void mostrarEstadisticas() {
        try {
            long total = collection.countDocuments();
            long activos = collection.countDocuments(Filters.eq("estado", "ACTIVO"));
            long inactivos = collection.countDocuments(Filters.eq("estado", "INACTIVO"));
            
            System.out.println("=== ESTADÍSTICAS DE ROLES ===");
            System.out.println("Total roles: " + total);
            System.out.println("Roles activos: " + activos);
            System.out.println("Roles inactivos: " + inactivos);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
    }

  public String generarSiguienteCodigo() {
    try {
        MongoCollection<Document> rolesCollection = database.getCollection("roles");
        
        // Buscar el código más alto actual
        Document sort = new Document("codigo", -1);
        Document projection = new Document("codigo", 1);
        
        Document ultimoRol = rolesCollection.find()
            .sort(sort)
            .projection(projection)
            .limit(1)
            .first();
        
        if (ultimoRol != null && ultimoRol.getString("codigo") != null) {
            String ultimoCodigo = ultimoRol.getString("codigo");
            
            // Extraer la parte numérica del código
            // Asumiendo formato: "ROL001", "ROL002", etc.
            if (ultimoCodigo.matches("^[A-Za-z]+\\d+$")) {
                // Separar letras y números
                String letras = ultimoCodigo.replaceAll("\\d", "");
                String numeros = ultimoCodigo.replaceAll("[^0-9]", "");
                
                if (!numeros.isEmpty()) {
                    int siguienteNumero = Integer.parseInt(numeros) + 1;
                    // Determinar el número de ceros a mantener
                    int longitudNumeros = numeros.length();
                    String formato = "%0" + longitudNumeros + "d";
                    return letras + String.format(formato, siguienteNumero);
                }
            }
        }
        
        // Si no hay roles o el formato no es válido, empezar con ROL001
        return "ROL001";
        
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error al generar siguiente código", e);
        // Fallback: generar código basado en timestamp
        return "ROL" + System.currentTimeMillis() % 1000;
    }
}
}