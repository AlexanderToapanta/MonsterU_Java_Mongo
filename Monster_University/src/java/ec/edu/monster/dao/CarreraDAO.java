package ec.edu.monster.dao;

import ec.edu.monster.controlador.Conexion;
import ec.edu.monster.modelo.Carrera;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class CarreraDAO {
    
    private static final Logger LOGGER = Logger.getLogger(CarreraDAO.class.getName());
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    
    public CarreraDAO() {
        Conexion conexion = new Conexion().crearConexion();
        if (conexion != null && conexion.isConectado()) {
            database = conexion.getDataB();
            collection = database.getCollection("carreras");
            LOGGER.info("Colección 'carreras' obtenida correctamente");
        } else {
            LOGGER.severe("Error: No hay conexión a la base de datos");
        }
    }
    
    // ========== MÉTODOS CRUD ==========
    
    // Crear nueva carrera
    public boolean crearCarrera(Carrera carrera) {
        try {
            // Verificar si ya existe una carrera con el mismo código
            if (existeCarrera(carrera.getCodigo())) {
                LOGGER.warning("Error: Ya existe una carrera con el código: " + carrera.getCodigo());
                return false;
            }
            
            Document docCarrera = new Document()
                .append("codigo", carrera.getCodigo())
                .append("nombre", carrera.getNombre())
                .append("creditosMaximos", carrera.getCreditosMaximos())
                .append("creditosMinimos", carrera.getCreditosMinimos());
            
            collection.insertOne(docCarrera);
            LOGGER.info("Carrera creada: " + carrera.getCodigo() + " - " + carrera.getNombre());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear carrera", e);
            return false;
        }
    }
    
    // Buscar carrera por código
    public Carrera buscarPorCodigo(String codigo) {
        try {
            Document doc = collection.find(Filters.eq("codigo", codigo)).first();
            if (doc != null) {
                return convertirDocumentACarrera(doc);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar carrera por código: " + codigo, e);
        }
        return null;
    }
    
    // Buscar carrera por ID de MongoDB
    public Carrera buscarPorId(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Document doc = collection.find(Filters.eq("_id", objectId)).first();
            if (doc != null) {
                return convertirDocumentACarrera(doc);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar carrera por ID: " + id, e);
        }
        return null;
    }
    
    // Método find (alias de buscarPorCodigo para compatibilidad)
    public Carrera find(String codigo) {
        return buscarPorCodigo(codigo);
    }
    
    // Listar todas las carreras
    public List<Carrera> listarTodas() {
        List<Carrera> carreras = new ArrayList<>();
        try {
            // Ordenar por código ascendente
            Bson sort = Filters.eq("codigo", 1);
            for (Document doc : collection.find().sort(sort)) {
                carreras.add(convertirDocumentACarrera(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar carreras", e);
        }
        return carreras;
    }
    
    // Alias para findAll (compatibilidad)
    public List<Carrera> findAll() {
        return listarTodas();
    }
    
    // Listar carreras activas
    public List<Carrera> listarActivas() {
        List<Carrera> carreras = new ArrayList<>();
        try {
            Bson filtro = Filters.eq("estado", "ACTIVO");
            Bson sort = Filters.eq("nombre", 1);
            for (Document doc : collection.find(filtro).sort(sort)) {
                carreras.add(convertirDocumentACarrera(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al listar carreras activas", e);
        }
        return carreras;
    }
    
    // Actualizar carrera
    public boolean actualizarCarrera(Carrera carrera) {
        try {
            Bson filtro = Filters.eq("codigo", carrera.getCodigo());
            Bson update = Updates.combine(
                Updates.set("nombre", carrera.getNombre()),
                Updates.set("creditosMaximos", carrera.getCreditosMaximos()),
                Updates.set("creditosMinimos", carrera.getCreditosMinimos())
            );
            
            collection.updateOne(filtro, update);
            LOGGER.info("Carrera actualizada: " + carrera.getCodigo() + " - " + carrera.getNombre());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar carrera", e);
            return false;
        }
    }
    
    // Alias para edit (compatibilidad)
    public boolean edit(Carrera carrera) {
        return actualizarCarrera(carrera);
    }
    
    // Eliminar carrera por código
    public boolean eliminarCarrera(String codigo) {
        try {
            Bson filtro = Filters.eq("codigo", codigo);
            collection.deleteOne(filtro);
            LOGGER.info("Carrera eliminada: " + codigo);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar carrera", e);
            return false;
        }
    }
    
    // Alias para remove (compatibilidad)
    public boolean remove(Carrera carrera) {
        return eliminarCarrera(carrera.getCodigo());
    }
    
    // ========== MÉTODOS ESPECÍFICOS ==========
    
    // Cambiar estado de carrera
    public boolean cambiarEstado(String carreraCodigo, String nuevoEstado) {
        try {
            Bson filtro = Filters.eq("codigo", carreraCodigo);
            Bson update = Updates.set("estado", nuevoEstado);
            collection.updateOne(filtro, update);
            LOGGER.info("Estado cambiado a " + nuevoEstado + " para carrera: " + carreraCodigo);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cambiar estado", e);
            return false;
        }
    }
    
    // ========== MÉTODOS DE CONSULTA ==========
    
    // Verificar si existe carrera
    public boolean existeCarrera(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }
    
    // Generar código automático - MÉTODO CLAVE
    public String generarSiguienteCodigo() {
        try {
            // Buscar el código más alto actual
            Document sort = new Document("codigo", -1);
            Document projection = new Document("codigo", 1);
            
            Document ultimaCarrera = collection.find()
                .sort(sort)
                .projection(projection)
                .limit(1)
                .first();
            
            if (ultimaCarrera != null && ultimaCarrera.getString("codigo") != null) {
                String ultimoCodigo = ultimaCarrera.getString("codigo");
                
                // Extraer la parte numérica del código
                // Asumiendo formato: "CARR001", "CARR002", etc.
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
            
            // Si no hay carreras o el formato no es válido, empezar con CARR001
            return "CARR001";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar siguiente código", e);
            // Fallback: generar código basado en timestamp
            return "CARR" + System.currentTimeMillis() % 1000;
        }
    }
    
    // Generar código automático con prefijo específico
    public String generarSiguienteCodigo(String prefijo) {
        try {
            // Buscar el código más alto actual con el prefijo
            Bson regex = Filters.regex("codigo", "^" + prefijo + "\\d+$");
            Document sort = new Document("codigo", -1);
            Document projection = new Document("codigo", 1);
            
            Document ultimaCarrera = collection.find(regex)
                .sort(sort)
                .projection(projection)
                .limit(1)
                .first();
            
            if (ultimaCarrera != null && ultimaCarrera.getString("codigo") != null) {
                String ultimoCodigo = ultimaCarrera.getString("codigo");
                
                // Extraer la parte numérica
                String numeros = ultimoCodigo.substring(prefijo.length());
                if (!numeros.isEmpty()) {
                    int siguienteNumero = Integer.parseInt(numeros) + 1;
                    // Mantener la misma longitud de dígitos
                    int longitudNumeros = numeros.length();
                    String formato = "%0" + longitudNumeros + "d";
                    return prefijo + String.format(formato, siguienteNumero);
                }
            }
            
            // Si no hay carreras con ese prefijo, empezar con 001
            return prefijo + "001";
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar siguiente código con prefijo: " + prefijo, e);
            return prefijo + "001";
        }
    }
    
    // Contar total de carreras
    public long contarCarreras() {
        try {
            return collection.countDocuments();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al contar carreras", e);
            return 0;
        }
    }
    
    // Buscar carreras por nombre (búsqueda parcial)
    public List<Carrera> buscarPorNombre(String nombre) {
        List<Carrera> carreras = new ArrayList<>();
        try {
            // Búsqueda case-insensitive y parcial
            Bson filtro = Filters.regex("nombre", ".*" + nombre + ".*", "i");
            Bson sort = Filters.eq("nombre", 1);
            for (Document doc : collection.find(filtro).sort(sort)) {
                carreras.add(convertirDocumentACarrera(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar carreras por nombre: " + nombre, e);
        }
        return carreras;
    }
    
    // Buscar carreras por rango de créditos
    public List<Carrera> buscarPorRangoCreditos(int minCreditos, int maxCreditos) {
        List<Carrera> carreras = new ArrayList<>();
        try {
            Bson filtro = Filters.and(
                Filters.gte("creditosMinimos", minCreditos),
                Filters.lte("creditosMaximos", maxCreditos)
            );
            Bson sort = Filters.eq("nombre", 1);
            for (Document doc : collection.find(filtro).sort(sort)) {
                carreras.add(convertirDocumentACarrera(doc));
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar carreras por rango de créditos", e);
        }
        return carreras;
    }
    
    // ========== MÉTODOS PRIVADOS ==========
    
    // Convertir Document a Carrera
    private Carrera convertirDocumentACarrera(Document doc) {
        Carrera carrera = new Carrera();
        
        try {
            // Campos básicos
            carrera.setCodigo(doc.getString("codigo"));
            carrera.setNombre(doc.getString("nombre"));
            
            // Créditos
            Object creditosMax = doc.get("creditosMaximos");
            if (creditosMax != null) {
                if (creditosMax instanceof Integer) {
                    carrera.setCreditosMaximos((Integer) creditosMax);
                } else if (creditosMax instanceof Double) {
                    carrera.setCreditosMaximos(((Double) creditosMax).intValue());
                }
            }
            
            Object creditosMin = doc.get("creditosMinimos");
            if (creditosMin != null) {
                if (creditosMin instanceof Integer) {
                    carrera.setCreditosMinimos((Integer) creditosMin);
                } else if (creditosMin instanceof Double) {
                    carrera.setCreditosMinimos(((Double) creditosMin).intValue());
                }
            }
            
            
            
            // Estado (por defecto ACTIVO si no existe)
            String estado = doc.getString("estado");
          
            
            // ID de MongoDB
            ObjectId id = doc.getObjectId("_id");
            if (id != null) {
                
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al convertir Document a Carrera", e);
        }
        
        return carrera;
    }
    
    // Método para debug
    public void mostrarEstadisticas() {
        try {
            long total = collection.countDocuments();
            long activos = collection.countDocuments(Filters.eq("estado", "ACTIVO"));
            long inactivos = collection.countDocuments(Filters.eq("estado", "INACTIVO"));
            
            LOGGER.info("=== ESTADÍSTICAS DE CARRERAS ===");
            LOGGER.info("Total carreras: " + total);
            LOGGER.info("Carreras activas: " + activos);
            LOGGER.info("Carreras inactivas: " + inactivos);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener estadísticas", e);
        }
    }
}