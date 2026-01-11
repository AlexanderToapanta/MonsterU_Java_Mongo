package ec.edu.monster.dao;

import ec.edu.monster.controlador.Conexion;
import ec.edu.monster.modelo.Persona;
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

public class PersonaDAO {
    
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    
    public PersonaDAO() {
        Conexion conexion = new Conexion().crearConexion();
        if (conexion != null && conexion.isConectado()) {
            database = conexion.getDataB();
            collection = database.getCollection("personas");
            System.out.println("Colección 'personas' obtenida correctamente");
        } else {
            System.err.println("Error: No hay conexión a la base de datos");
        }
    }
    
    // Crear nueva persona
    public boolean crearPersona(Persona persona) {
        try {
            Document docPersona = new Document()
                .append("codigo", persona.getCodigo())
                .append("peperTipo", persona.getPeperTipo()) // Cambiado aquí
                .append("documento", persona.getDocumento())
                .append("nombres", persona.getNombres())
                .append("apellidos", persona.getApellidos())
                .append("email", persona.getEmail())
                .append("celular", persona.getCelular())
                .append("fecha_nacimiento", persona.getFecha_nacimiento())
                .append("sexo", persona.getSexo())
                .append("estado_civil", persona.getEstado_civil())
                .append("username", persona.getUsername())
                .append("password_hash", persona.getPassword_hash())
                .append("fecha_ingreso", persona.getFecha_ingreso())
                .append("imagen_perfil", persona.getImagen_perfil())
                .append("estado", persona.getEstado());
            
            // Si tiene rol, agregarlo como documento embebido
            if (persona.getRol() != null) {
                Rol rol = persona.getRol();
                Document docRol = new Document()
                    .append("codigo", rol.getCodigo())
                    .append("nombre", rol.getNombre())
                    .append("descripcion", rol.getDescripcion())
                    .append("estado", rol.getEstado());
                docPersona.append("rol", docRol);
            }
            
            collection.insertOne(docPersona);
            persona.setId(docPersona.getObjectId("_id"));
            System.out.println("Persona creada: " + persona.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al crear persona: " + e.getMessage());
            return false;
        }
    }
    
    // Buscar persona por ID
    public Persona buscarPorId(String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Document doc = collection.find(Filters.eq("_id", objectId)).first();
            
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por ID: " + e.getMessage());
        }
        return null;
    }
    
    public Persona buscarPorCelular(String Celular){
        try {
            Document doc = collection.find(Filters.eq("celular", Celular)).first();
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por código: " + e.getMessage());
        }
        return null;
    }
    
    public Persona buscarPorEmail(String email){
        try {
            Document doc = collection.find(Filters.eq("email", email)).first();
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por código: " + e.getMessage());
        }
        return null;
    }
    
    // Buscar persona por código
    public Persona buscarPorCodigo(String codigo) {
        try {
            Document doc = collection.find(Filters.eq("codigo", codigo)).first();
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por código: " + e.getMessage());
        }
        return null;
    }
    
    // Buscar persona por username
    public Persona buscarPorUsername(String username) {
        try {
            Document doc = collection.find(Filters.eq("username", username)).first();
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por username: " + e.getMessage());
        }
        return null;
    }
    
    // Buscar persona por documento
    public Persona buscarPorDocumento(String documento) {
        try {
            Document doc = collection.find(Filters.eq("documento", documento)).first();
            if (doc != null) {
                return convertirDocumentAPersona(doc);
            }
        } catch (Exception e) {
            System.err.println("Error al buscar persona por documento: " + e.getMessage());
        }
        return null;
    }
    
    // Buscar por tipo de persona
    public List<Persona> buscarPorPeperTipo(String peperTipo) {
        List<Persona> personas = new ArrayList<>();
        try {
            Bson filtro = Filters.eq("peperTipo", peperTipo);
            for (Document doc : collection.find(filtro)) {
                personas.add(convertirDocumentAPersona(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al buscar personas por peperTipo: " + e.getMessage());
        }
        return personas;
    }
    
    // Listar todas las personas
    public List<Persona> listarTodas() {
        List<Persona> personas = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                personas.add(convertirDocumentAPersona(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al listar personas: " + e.getMessage());
        }
        return personas;
    }
    
    // Listar personas activas
    public List<Persona> listarActivas() {
        List<Persona> personas = new ArrayList<>();
        try {
            Bson filtro = Filters.eq("estado", "ACTIVO");
            for (Document doc : collection.find(filtro)) {
                personas.add(convertirDocumentAPersona(doc));
            }
        } catch (Exception e) {
            System.err.println("Error al listar personas activas: " + e.getMessage());
        }
        return personas;
    }
    
    // Actualizar persona
    public boolean actualizarPersona(Persona persona) {
        try {
            Bson filtro = Filters.eq("_id", persona.getId());
            
            // Crear documento con los campos actualizados
            Document actualizaciones = new Document();
            actualizaciones.append("codigo", persona.getCodigo());
            actualizaciones.append("peperTipo", persona.getPeperTipo()); // Cambiado aquí
            actualizaciones.append("documento", persona.getDocumento());
            actualizaciones.append("nombres", persona.getNombres());
            actualizaciones.append("apellidos", persona.getApellidos());
            actualizaciones.append("email", persona.getEmail());
            actualizaciones.append("celular", persona.getCelular());
            actualizaciones.append("fecha_nacimiento", persona.getFecha_nacimiento());
            actualizaciones.append("sexo", persona.getSexo());
            actualizaciones.append("estado_civil", persona.getEstado_civil());
            actualizaciones.append("username", persona.getUsername());
            actualizaciones.append("password_hash", persona.getPassword_hash());
            actualizaciones.append("estado", persona.getEstado());
            
            // Actualizar rol si existe
            if (persona.getRol() != null) {
                Rol rol = persona.getRol();
                Document docRol = new Document()
                    .append("codigo", rol.getCodigo())
                    .append("nombre", rol.getNombre())
                    .append("descripcion", rol.getDescripcion())
                    .append("estado", rol.getEstado());
                actualizaciones.append("rol", docRol);
            }
            
            collection.updateOne(filtro, new Document("$set", actualizaciones));
            System.out.println("Persona actualizada: " + persona.getCodigo());
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar persona: " + e.getMessage());
            return false;
        }
    }
    
    // Asignar rol a persona
    public boolean asignarRol(String personaId, Rol rol) {
        try {
            ObjectId objectId = new ObjectId(personaId);
            Bson filtro = Filters.eq("_id", objectId);
            
            Document docRol = new Document()
                .append("codigo", rol.getCodigo())
                .append("nombre", rol.getNombre())
                .append("descripcion", rol.getDescripcion())
                .append("estado", rol.getEstado());
            
            Bson update = Updates.set("rol", docRol);
            collection.updateOne(filtro, update);
            System.out.println("Rol asignado a persona ID: " + personaId);
            return true;
        } catch (Exception e) {
            System.err.println("Error al asignar rol: " + e.getMessage());
            return false;
        }
    }
    
    // Cambiar estado de persona
    public boolean cambiarEstado(String personaId, String nuevoEstado) {
        try {
            ObjectId objectId = new ObjectId(personaId);
            Bson filtro = Filters.eq("_id", objectId);
            Bson update = Updates.set("estado", nuevoEstado);
            collection.updateOne(filtro, update);
            System.out.println("Estado cambiado a " + nuevoEstado + " para persona ID: " + personaId);
            return true;
        } catch (Exception e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }
    
    // Eliminar persona (cambiar estado a INACTIVO)
    public boolean eliminarPersona(String personaId) {
        return cambiarEstado(personaId, "INACTIVO");
    }
    
    // Convertir Document de MongoDB a objeto Persona
    private Persona convertirDocumentAPersona(Document doc) {
        Persona persona = new Persona();
        
        persona.setId(doc.getObjectId("_id"));
        persona.setCodigo(doc.getString("codigo"));
        persona.setPeperTipo(doc.getString("peperTipo")); // Cambiado aquí
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
        
        // Convertir rol si existe
        Document docRol = (Document) doc.get("rol");
        if (docRol != null) {
            Rol rol = new Rol();
            rol.setCodigo(docRol.getString("codigo"));
            rol.setNombre(docRol.getString("nombre"));
            rol.setDescripcion(docRol.getString("descripcion"));
            rol.setEstado(docRol.getString("estado"));
            persona.setRol(rol);
        }
        
        return persona;
    }
    
    // Método para verificar si existe un código
    public boolean existeCodigo(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }
    
    // Método para verificar si existe un username
    public boolean existeUsername(String username) {
        return buscarPorUsername(username) != null;
    }
    
    // Método para verificar si existe un documento
    public boolean existeDocumento(String documento) {
        return buscarPorDocumento(documento) != null;
    }

    public boolean existeCelular(String celular) {
        return buscarPorCelular(celular) != null;
    }

    public boolean existeEmail(String email) {
        return buscarPorEmail(email) != null;
    }
    
    // Método para verificar si existe un tipo de persona
    public boolean existePeperTipo(String peperTipo) {
        return !buscarPorPeperTipo(peperTipo).isEmpty();
    }
    public List<Persona> buscarPersonasSinRol() {
        List<Persona> personas = new ArrayList<>();
        try {
            // Buscar personas donde el campo rol sea null o no exista
            for (Document doc : collection.find(Filters.or(
                Filters.eq("rol", null),
                Filters.exists("rol", false)
            ))) {
                personas.add(convertirDocumentAPersona(doc));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return personas;
    }
    
    public List<Persona> buscarPersonasPorRol(String codigoRol) {
        List<Persona> personas = new ArrayList<>();
        try {
            // Buscar personas cuyo rol tenga el código especificado
            for (Document doc : collection.find(Filters.eq("rol.codigo", codigoRol))) {
                personas.add(convertirDocumentAPersona(doc));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return personas;
    }
}