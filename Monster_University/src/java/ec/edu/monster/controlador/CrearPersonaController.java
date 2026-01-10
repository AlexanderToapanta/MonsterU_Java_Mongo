package ec.edu.monster.controlador;

import ec.edu.monster.dao.ConfiguracionDAO;
import ec.edu.monster.dao.PersonaDAO;
import ec.edu.monster.dao.RolDAO;
import ec.edu.monster.modelo.Persona;
import ec.edu.monster.modelo.Rol;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import org.primefaces.model.file.UploadedFile;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

@Named(value = "crearPersonaController")
@SessionScoped
public class CrearPersonaController implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private static final Pattern PATRON_EMAIL = 
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_CELULAR = 
        Pattern.compile("^09[0-9]{8}$");
    
    // DAOs para MongoDB
    private PersonaDAO personaDAO;
    private RolDAO rolDAO;
    private ConfiguracionDAO configuracionDAO;
    private List<Map<String, String>> opcionesSexoBD;
    private List<Map<String, String>> opcionesEstadoCivilBD;
    private Persona nuevaPersona;
    private String codigoGenerado;
    private String sexoSeleccionado;  // "M" o "F"
    private String estadoCivilSeleccionado; // "S", "C", "D", "V"
    private boolean cedulaValida = false;
    private boolean emailValido = false;
    private boolean celularValido = false;
    private UploadedFile imagenSubida;
    private String nombreArchivoImagen;
    
    // Configuración de correo (actualiza con tus credenciales reales)
    private final String CORREO_REMITENTE = "alexandertoapantaj05@gmail.com";
    private final String CONTRASENIA_REMITENTE = "tu_api_key_de_sendgrid_aqui";
    private final String SERVIDOR_SMTP = "smtp.sendgrid.net";
    private final int PUERTO_SMTP = 587;

    public CrearPersonaController() {
        personaDAO = new PersonaDAO();
        rolDAO = new RolDAO();
        configuracionDAO = new ConfiguracionDAO();
        cargarOpcionesDesdeBD();
        initNuevaPersona();
    }

    // Inicializar nueva persona
    public void initNuevaPersona() {
        nuevaPersona = new Persona();
        sexoSeleccionado = null;
        estadoCivilSeleccionado = null;
        cedulaValida = false;
        emailValido = false;
        celularValido = false;
        imagenSubida = null;
        nombreArchivoImagen = null;
        generarNuevoCodigo();
        nuevaPersona.setFecha_ingreso(new Date());
        nuevaPersona.setEstado("ACTIVO");
        // Eliminada la línea: nuevaPersona.setTipo_documento("CEDULA");
        // Ahora solo manejamos peperTipo que viene del formulario
    }
    
    private void cargarOpcionesDesdeBD() {
        try {
            // Cargar sexos desde la base de datos
            opcionesSexoBD = configuracionDAO.obtenerValoresPorTipo("sexo");
            System.out.println("Opciones de sexo cargadas desde BD: " + opcionesSexoBD.size());
            
            // Cargar estados civiles desde la base de datos
            opcionesEstadoCivilBD = configuracionDAO.obtenerValoresPorTipo("estado_civil");
            System.out.println("Opciones de estado civil cargadas desde BD: " + opcionesEstadoCivilBD.size());
            
        } catch (Exception e) {
            System.err.println("Error al cargar opciones desde BD: " + e.getMessage());
            
            // Valores por defecto en caso de error
            opcionesSexoBD = new ArrayList<>();
            Map<String, String> sexoM = new HashMap<>();
            sexoM.put("codigo", "M");
            sexoM.put("nombre", "Masculino");
            opcionesSexoBD.add(sexoM);
            
            Map<String, String> sexoF = new HashMap<>();
            sexoF.put("codigo", "F");
            sexoF.put("nombre", "Femenino");
            opcionesSexoBD.add(sexoF);
            
            opcionesEstadoCivilBD = new ArrayList<>();
            Map<String, String> estCivilS = new HashMap<>();
            estCivilS.put("codigo", "S");
            estCivilS.put("nombre", "Soltero/a");
            opcionesEstadoCivilBD.add(estCivilS);
            
            Map<String, String> estCivilC = new HashMap<>();
            estCivilC.put("codigo", "C");
            estCivilC.put("nombre", "Casado/a");
            opcionesEstadoCivilBD.add(estCivilC);
        }
    }
    
    // Métodos para obtener las opciones desde BD (reemplazan los anteriores)
    public List<Map<String, String>> getOpcionesSexoDesdeBD() {
        if (opcionesSexoBD == null || opcionesSexoBD.isEmpty()) {
            cargarOpcionesDesdeBD();
        }
        return opcionesSexoBD;
    }
    
    public List<Map<String, String>> getOpcionesEstadoCivilDesdeBD() {
        if (opcionesEstadoCivilBD == null || opcionesEstadoCivilBD.isEmpty()) {
            cargarOpcionesDesdeBD();
        }
        return opcionesEstadoCivilBD;
    }
    
    // Métodos auxiliares para obtener el nombre de un código
    public String getNombreSexo(String codigo) {
        if (codigo == null) return "";
        return configuracionDAO.obtenerNombrePorCodigo("sexo", codigo);
    }
    
    public String getNombreEstadoCivil(String codigo) {
        if (codigo == null) return "";
        return configuracionDAO.obtenerNombrePorCodigo("estado_civil", codigo);
    }
    
    // ============ GETTERS Y SETTERS ============
    
    public UploadedFile getImagenSubida() {
        return imagenSubida;
    }

    public void setImagenSubida(UploadedFile imagenSubida) {
        this.imagenSubida = imagenSubida;
    }

    public String getSexoSeleccionado() {
        return sexoSeleccionado;
    }

    public void setSexoSeleccionado(String sexoSeleccionado) {
        this.sexoSeleccionado = sexoSeleccionado;
    }

    public String getEstadoCivilSeleccionado() {
        return estadoCivilSeleccionado;
    }

    public void setEstadoCivilSeleccionado(String estadoCivilSeleccionado) {
        this.estadoCivilSeleccionado = estadoCivilSeleccionado;
    }

    public String getCodigoGenerado() {
        if (codigoGenerado == null) {
            generarNuevoCodigo();
        }
        return codigoGenerado;
    }

    public String getNombreArchivoImagen() {
        return nombreArchivoImagen;
    }

    public void setNombreArchivoImagen(String nombreArchivoImagen) {
        this.nombreArchivoImagen = nombreArchivoImagen;
    }
    
    public Persona getNuevaPersona() {
        return nuevaPersona;
    }

    public void setNuevaPersona(Persona nuevaPersona) {
        this.nuevaPersona = nuevaPersona;
    }
    
    public void setCodigoGenerado(String codigoGenerado) {
        this.codigoGenerado = codigoGenerado;
    }
    
    public boolean isCedulaValida() {
        return cedulaValida;
    }
    
    public void setCedulaValida(boolean cedulaValida) {
        this.cedulaValida = cedulaValida;
    }
    
    public boolean isEmailValido() {
        return emailValido;
    }
    
    public void setEmailValido(boolean emailValido) {
        this.emailValido = emailValido;
    }
    
    public boolean isCelularValido() {
        return celularValido;
    }
    
    public void setCelularValido(boolean celularValido) {
        this.celularValido = celularValido;
    }
    
    // ============ MÉTODOS DE IMAGEN ============
    
    public void subirImagen() {
        System.out.println("=== SUBIR IMAGEN ===");
        
        if (imagenSubida == null) {
            System.out.println("❌ No hay imagen seleccionada");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                "No hay imagen seleccionada"));
            return;
        }
        
        System.out.println("Archivo: " + imagenSubida.getFileName());
        System.out.println("Tamaño: " + imagenSubida.getSize() + " bytes");
        
        try {
            // Generar nombre único
            String documento = nuevaPersona.getDocumento();
            if (documento == null || documento.trim().isEmpty()) {
                documento = "temp_" + System.currentTimeMillis();
            } else {
                documento = documento.replaceAll("[^0-9]", "");
                if (documento.isEmpty()) {
                    documento = "temp_" + System.currentTimeMillis();
                }
            }
            
            String nombreOriginal = imagenSubida.getFileName();
            String extension = "";
            if (nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }
            
            nombreArchivoImagen = documento + "_" + System.currentTimeMillis() + extension;
            
            // Definir ruta
            String rutaBase = "C:\\Users\\Usuario\\Documents\\Monster_U_Java_Mongo\\MonsterU_Java_Mongo\\Monster_University\\web\\img\\";
            String rutaCompleta = rutaBase + nombreArchivoImagen;
            
            System.out.println("Ruta completa: " + rutaCompleta);
            
            // Crear directorio si no existe
            File dir = new File(rutaBase);
            if (!dir.exists()) {
                boolean creado = dir.mkdirs();
                System.out.println("Directorio creado: " + creado);
            }
            
            // Guardar archivo
            try (InputStream in = imagenSubida.getInputStream();
                 FileOutputStream out = new FileOutputStream(rutaCompleta)) {
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            // Asignar a la persona
            nuevaPersona.setImagen_perfil(nombreArchivoImagen);
            System.out.println("✅ Imagen guardada: " + nombreArchivoImagen);
            
            // Verificar que el archivo se guardó
            File archivoGuardado = new File(rutaCompleta);
            if (archivoGuardado.exists()) {
                System.out.println("✅ Archivo físico existe: " + archivoGuardado.length() + " bytes");
            }
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                "Imagen subida correctamente"));
            
            imagenSubida = null;
            
        } catch (Exception e) {
            System.err.println("❌ Error al subir imagen: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Error al subir imagen: " + e.getMessage()));
        }
    }
    
    public void eliminarImagen() {
        try {
            if (nuevaPersona.getImagen_perfil() != null && !nuevaPersona.getImagen_perfil().isEmpty()) {
                String rutaBase = "C:\\Users\\Usuario\\Documents\\Monster_U_Java_Mongo\\MonsterU_Java_Mongo\\Monster_University\\web\\img\\";
                String rutaCompleta = rutaBase + nuevaPersona.getImagen_perfil();
                
                File archivo = new File(rutaCompleta);
                if (archivo.exists()) {
                    if (archivo.delete()) {
                        System.out.println("✅ Imagen eliminada físicamente: " + nuevaPersona.getImagen_perfil());
                    } else {
                        System.out.println("⚠️ No se pudo eliminar el archivo físico");
                    }
                }
            }
            
            nuevaPersona.setImagen_perfil(null);
            nombreArchivoImagen = null;
            imagenSubida = null;
            
            System.out.println("✅ Referencias de imagen limpiadas");
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                "Imagen eliminada"));
            
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar imagen: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Error al eliminar imagen: " + e.getMessage()));
        }
    }
    
    public String getUrlImagen() {
        if (nuevaPersona.getImagen_perfil() == null || nuevaPersona.getImagen_perfil().isEmpty()) {
            return null;
        }
        return "img/" + nuevaPersona.getImagen_perfil();
    }
    
    // ============ MÉTODOS DE NEGOCIO ============
    
    private void generarNuevoCodigo() {
        try {
            System.out.println("=== GENERANDO NUEVO CÓDIGO ===");
            
            List<Persona> todasPersonas = personaDAO.listarTodas();
            System.out.println("Total personas en BD: " + todasPersonas.size());
            
            if (todasPersonas.isEmpty()) {
                codigoGenerado = "PE001";
                nuevaPersona.setCodigo(codigoGenerado);
                System.out.println("✅ Código inicial: " + codigoGenerado);
                return;
            }
            
            int maxNumero = 0;
            for (Persona persona : todasPersonas) {
                String codigo = persona.getCodigo();
                if (codigo != null && codigo.startsWith("PE") && codigo.length() == 5) {
                    try {
                        String numeroStr = codigo.substring(2);
                        int numero = Integer.parseInt(numeroStr);
                        if (numero > maxNumero) {
                            maxNumero = numero;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Código con formato incorrecto: " + codigo);
                    }
                }
            }
            
            for (int i = 1; i <= 999; i++) {
                String codigoCandidato = String.format("PE%03d", i);
                
                boolean existe = false;
                for (Persona persona : todasPersonas) {
                    if (codigoCandidato.equals(persona.getCodigo())) {
                        existe = true;
                        break;
                    }
                }
                
                if (!existe) {
                    codigoGenerado = codigoCandidato;
                    nuevaPersona.setCodigo(codigoGenerado);
                    System.out.println("✅ Código disponible asignado: " + codigoGenerado);
                    return;
                }
            }
            
            codigoGenerado = String.format("PE%03d", maxNumero + 1);
            nuevaPersona.setCodigo(codigoGenerado);
            System.out.println("✅ Código asignado (secuencial): " + codigoGenerado);
            
        } catch (Exception e) {
            System.err.println("❌ Error generando código: " + e.getMessage());
            e.printStackTrace();
            codigoGenerado = "PE001";
            nuevaPersona.setCodigo(codigoGenerado);
        }
    }

    public void crearPersona() {
        try {
            System.out.println("=== INICIANDO PROCESO CREAR PERSONA ===");
            
            // Validar peperTipo (campo obligatorio según el XHTML)
            if (nuevaPersona.getPeperTipo() == null || nuevaPersona.getPeperTipo().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "Debe seleccionar un tipo de persona"));
                return;
            }
            
            // Verificar imagen seleccionada pero no subida
            if (imagenSubida != null && imagenSubida.getSize() > 0 && 
                (nuevaPersona.getImagen_perfil() == null || nuevaPersona.getImagen_perfil().isEmpty())) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                    "Tiene una imagen seleccionada. Por favor haga clic en 'Subir Imagen' para procesarla"));
                return;
            }
            
            if (!validarDatosCompletos()) {
                System.out.println("❌ Validaciones fallaron");
                return;
            }
            
            if (sexoSeleccionado == null || sexoSeleccionado.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "Debe seleccionar un sexo"));
                return;
            }
            
            // Asignar sexo
            nuevaPersona.setSexo(sexoSeleccionado);
            System.out.println("✅ Sexo asignado: " + sexoSeleccionado);
            
            // Asignar estado civil si se seleccionó
            if (estadoCivilSeleccionado != null && !estadoCivilSeleccionado.trim().isEmpty()) {
                nuevaPersona.setEstado_civil(estadoCivilSeleccionado);
                System.out.println("✅ Estado civil asignado: " + estadoCivilSeleccionado);
            }
            
            // Verificar si el código ya existe
            if (personaDAO.existeCodigo(codigoGenerado)) {
                System.out.println("⚠️ El código ya existe, generando uno nuevo...");
                generarNuevoCodigo();
                System.out.println("Nuevo código generado: " + codigoGenerado);
            }
            
            // Asignar valores adicionales
            nuevaPersona.setCodigo(codigoGenerado);
            nuevaPersona.setEstado("ACTIVO");
            
            // Generar username
            String username = generarUsername();
            nuevaPersona.setUsername(username);
            System.out.println("✅ Username generado: " + username);
            
            // Generar hash de contraseña
            String passwordHash = generarHashSHA256(nuevaPersona.getDocumento());
            nuevaPersona.setPassword_hash(passwordHash);
            System.out.println("✅ Password hash generado (SHA-256)");
            
            // Asignar rol según el tipo de persona
            asignarRolSegunTipoPersona();
            
            // Guardar persona en MongoDB
            System.out.println("💾 Guardando persona en MongoDB...");
            boolean guardado = personaDAO.crearPersona(nuevaPersona);
            
            if (guardado) {
                System.out.println("🎉 PERSONA CREADA EXITOSAMENTE EN MONGODB");
                System.out.println("Código: " + nuevaPersona.getCodigo());
                System.out.println("Tipo de Persona: " + nuevaPersona.getPeperTipo());
                System.out.println("Nombre: " + nuevaPersona.getNombres() + " " + nuevaPersona.getApellidos());
                System.out.println("Documento: " + nuevaPersona.getDocumento());
                System.out.println("Email: " + nuevaPersona.getEmail());
                
                // Enviar correo con credenciales
                System.out.println("📧 Enviando correo de credenciales...");
                enviarCorreoCredenciales(
                    nuevaPersona.getEmail(),
                    nuevaPersona.getUsername(),
                    nuevaPersona.getDocumento() // Contraseña inicial es la cédula
                );
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                    "Persona creada exitosamente. Código: " + nuevaPersona.getCodigo()));
                
                // Reiniciar formulario
                initNuevaPersona();
                System.out.println("🔄 Formulario reiniciado");
                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "No se pudo crear la persona en la base de datos"));
            }
            
        } catch (Exception e) {
            System.err.println("💥 ERROR GENERAL: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Error al crear persona: " + e.getMessage()));
        }
    }
    
    private void asignarRolSegunTipoPersona() {
        try {
            Rol rol = null;
            String peperTipo = nuevaPersona.getPeperTipo();
            
            // Asignar rol según el tipo de persona seleccionado
            if (peperTipo != null) {
                switch (peperTipo) {
                    case "Administrador del Sistema":
                        rol = rolDAO.buscarPorCodigo("ADMIN");
                        if (rol == null) {
                            rol = crearRolSiNoExiste("ADMIN", "Administrador del Sistema", 
                                "Administrador con todos los permisos", "ACTIVO");
                        }
                        break;
                    case "Docente":
                        rol = rolDAO.buscarPorCodigo("DOC");
                        if (rol == null) {
                            rol = crearRolSiNoExiste("DOC", "Docente", 
                                "Docente con permisos académicos", "ACTIVO");
                        }
                        break;
                    case "Administrador de matriculas":
                        rol = rolDAO.buscarPorCodigo("ADM_MAT");
                        if (rol == null) {
                            rol = crearRolSiNoExiste("ADM_MAT", "Administrador de Matrículas", 
                                "Administrador de procesos de matrícula", "ACTIVO");
                        }
                        break;
                    case "Secretaria Academica":
                        rol = rolDAO.buscarPorCodigo("SEC_ACAD");
                        if (rol == null) {
                            rol = crearRolSiNoExiste("SEC_ACAD", "Secretaría Académica", 
                                "Personal de secretaría académica", "ACTIVO");
                        }
                        break;
                    default:
                        // Rol por defecto
                        rol = rolDAO.buscarPorCodigo("USER");
                        if (rol == null) {
                            rol = crearRolSiNoExiste("USER", "Usuario Estándar", 
                                "Usuario con permisos básicos", "ACTIVO");
                        }
                        break;
                }
            }
            
            if (rol == null) {
                // Rol por defecto como último recurso
                rol = new Rol();
                rol.setCodigo("USER");
                rol.setNombre("Usuario Estándar");
                rol.setDescripcion("Usuario con permisos básicos");
                rol.setEstado("ACTIVO");
            }
            
            nuevaPersona.setRol(rol);
            System.out.println("✅ Rol asignado según peperTipo: " + rol.getNombre());
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al asignar rol por tipo de persona: " + e.getMessage());
            // Continuar sin rol si hay error
        }
    }
    
    private Rol crearRolSiNoExiste(String codigo, String nombre, String descripcion, String estado) {
        Rol rol = new Rol();
        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setEstado(estado);
        return rol;
    }
    
    private String generarUsername() {
        String nombre = nuevaPersona.getNombres();
        String apellido = nuevaPersona.getApellidos();
        
        if (nombre == null || apellido == null || 
            nombre.trim().isEmpty() || apellido.trim().isEmpty()) {
            return "user_" + nuevaPersona.getDocumento();
        }
        
        String primeraLetra = nombre.trim().substring(0, 1).toUpperCase();
        String apellidoSinEspacios = apellido.trim().replaceAll("\\s+", "");
        
        String username = primeraLetra + apellidoSinEspacios;
        username = username.replaceAll("[^a-zA-Z0-9]", ""); // Remover caracteres especiales
        
        // Verificar si ya existe y agregar número si es necesario
        if (personaDAO.existeUsername(username)) {
            for (int i = 1; i <= 100; i++) {
                String candidato = username + i;
                if (!personaDAO.existeUsername(candidato)) {
                    return candidato.toLowerCase();
                }
            }
            // Si todos están ocupados, usar UUID
            return username.toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 4);
        }
        
        return username.toLowerCase();
    }
    
    /**
     * Generar hash SHA-256 de una cadena
     */
    private String generarHashSHA256(String texto) {
        try {
            if (texto == null || texto.isEmpty()) {
                return "";
            }
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
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
     * Alternativa: Generar hash usando Base64
     */
    private String generarHashBase64(String texto) {
        try {
            if (texto == null || texto.isEmpty()) {
                return "";
            }
            
            // Agregar un salt simple
            String saltedText = texto + "SALT_MONSTER_UNIVERSITY";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(saltedText.getBytes("UTF-8"));
            
            // Convertir a Base64
            return Base64.getEncoder().encodeToString(hashBytes);
            
        } catch (Exception e) {
            System.err.println("⚠️ Error al generar hash Base64: " + e.getMessage());
            return texto; // Fallback a texto plano (NO recomendado para producción)
        }
    }
    
    // ============ VALIDACIONES ============
    
    public void validarCedula() {
        String cedula = nuevaPersona.getDocumento();
        if (cedula == null || cedula.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "La cédula es requerida"));
            cedulaValida = false;
            return;
        }
        
        cedula = cedula.trim();
        if (cedula.length() != 10) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "La cédula debe tener 10 dígitos"));
            cedulaValida = false;
            return;
        }
        
        if (!cedula.matches("[0-9]+")) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "La cédula debe contener solo números"));
            cedulaValida = false;
            return;
        }
        
        if (!validarCedulaEcuatoriana(cedula)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Cédula inválida - Verifique los dígitos"));
            cedulaValida = false;
            return;
        }
        
        if (personaDAO.existeDocumento(cedula)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Esta cédula ya está registrada en el sistema"));
            cedulaValida = false;
            return;
        }
        
        cedulaValida = true;
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
            "Cédula válida"));
    }
    
    public void validarEmail() {
        String email = nuevaPersona.getEmail();
        if (email == null || email.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "El email es requerido"));
            emailValido = false;
            return;
        }
        
        email = email.trim().toLowerCase();
        
        if (!PATRON_EMAIL.matcher(email).matches()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Formato de email inválido"));
            emailValido = false;
            return;
        }
        
        if (personaDAO.existeEmail(email)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Este email ya está registrado en el sistema"));
            emailValido = false;
            return;
        }
        
        emailValido = true;
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
            "Email válido"));
    }
    
    public void validarCelular() {
        String celular = nuevaPersona.getCelular();
        
        if (celular == null || celular.trim().isEmpty()) {
            celularValido = true;
            return;
        }
        
        celular = celular.trim();
        
        if (!PATRON_CELULAR.matcher(celular).matches()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Celular inválido. Use formato: 09XXXXXXXX"));
            celularValido = false;
            return;
        }
        
        if (personaDAO.existeCelular(celular)) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Este número de celular ya está registrado"));
            celularValido = false;
            return;
        }
        
        celularValido = true;
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
            "Celular válido"));
    }
    
    public void validarFechaNacimiento() {
        Date fechaNac = nuevaPersona.getFecha_nacimiento();
        if (fechaNac != null) {
            Date hoy = new Date();
            if (fechaNac.after(hoy)) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                    "La fecha de nacimiento no puede ser futura"));
            }
        }
    }
    
    private boolean validarCedulaEcuatoriana(String cedula) {
        try {
            int provincia = Integer.parseInt(cedula.substring(0, 2));
            if (provincia < 1 || provincia > 24) {
                return false;
            }
            
            int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
            if (tercerDigito < 0 || tercerDigito > 6) {
                return false;
            }
            
            int total = 0;
            int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
            int verificador = Integer.parseInt(cedula.substring(9, 10));
            
            for (int i = 0; i < 9; i++) {
                int valor = Integer.parseInt(cedula.substring(i, i + 1)) * coeficientes[i];
                if (valor > 9) {
                    valor -= 9;
                }
                total += valor;
            }
            
            int residuo = total % 10;
            int digitoVerificador = (residuo == 0) ? 0 : 10 - residuo;
            
            return digitoVerificador == verificador;
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean validarDatosCompletos() {
        validarCedula();
        validarEmail();
        validarCelular();
        validarFechaNacimiento();
        
        if (!cedulaValida) {
            return false;
        }
        
        if (!emailValido) {
            return false;
        }
        
        if (!celularValido && nuevaPersona.getCelular() != null 
            && !nuevaPersona.getCelular().trim().isEmpty()) {
            return false;
        }
        
        return validarDatosPersona();
    }
    
    private boolean validarDatosPersona() {
        if (nuevaPersona.getNombres() == null || 
            nuevaPersona.getNombres().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "El nombre es requerido"));
            return false;
        }
        
        if (nuevaPersona.getApellidos() == null || 
            nuevaPersona.getApellidos().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "El apellido es requerido"));
            return false;
        }
        
        if (nuevaPersona.getDocumento() == null || 
            nuevaPersona.getDocumento().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "La cédula es requerida"));
            return false;
        }
        
        if (nuevaPersona.getEmail() == null || 
            nuevaPersona.getEmail().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "El email es requerido"));
            return false;
        }
        
        // Validar peperTipo (nuevo campo obligatorio)
        if (nuevaPersona.getPeperTipo() == null || 
            nuevaPersona.getPeperTipo().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "El tipo de persona es requerido"));
            return false;
        }
        
        return true;
    }
    
    // ============ MÉTODOS PARA OPCIONES DE SELECT ============
    
    public List<String> getOpcionesSexo() {
        return java.util.Arrays.asList("M", "F");
    }
    
    public String getLabelSexo(String codigo) {
        if ("M".equals(codigo)) return "Masculino";
        if ("F".equals(codigo)) return "Femenino";
        return codigo;
    }
    
    public List<String> getOpcionesEstadoCivil() {
        return java.util.Arrays.asList("S", "C", "D", "V");
    }
    
    public String getLabelEstadoCivil(String codigo) {
        switch (codigo) {
            case "S": return "Soltero/a";
            case "C": return "Casado/a";
            case "D": return "Divorciado/a";
            case "V": return "Viudo/a";
            default: return codigo;
        }
    }
    
    // Método para obtener opciones de tipo de persona (peperTipo)
    public List<String> getOpcionesTipoPersona() {
        return java.util.Arrays.asList(
            "Administrador del Sistema",
            "Docente",
            "Administrador de matriculas",
            "Secretaria Academica"
        );
    }
    
    // ============ MÉTODOS DE CORREO ============
    
    private void enviarCorreoCredenciales(String destinatario, String nombreUsuario, String contrasenia) {
        try {
            System.out.println("=== ENVIANDO CORREO CON SENDGRID ===");
            System.out.println("📧 Destinatario: " + destinatario);
            System.out.println("👤 Usuario: " + nombreUsuario);
            
            // Validar destinatario
            if (destinatario == null || destinatario.trim().isEmpty()) {
                System.out.println("⚠️ No se envió correo: destinatario vacío");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", 
                    "No se pudo enviar correo: email del destinatario vacío"));
                return;
            }
            
            // Configuración SMTP para SendGrid
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SERVIDOR_SMTP);
            props.put("mail.smtp.port", String.valueOf(PUERTO_SMTP));
            props.put("mail.smtp.ssl.trust", SERVIDOR_SMTP);
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");
            
            // Crear sesión con autenticación
            Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        System.out.println("🔑 Autenticando con SendGrid...");
                        System.out.println("     Usuario: apikey");
                        System.out.println("     API Key: " + (CONTRASENIA_REMITENTE != null ? 
                            CONTRASENIA_REMITENTE.substring(0, Math.min(4, CONTRASENIA_REMITENTE.length())) + "..." : "null"));
                        
                        return new PasswordAuthentication("apikey", CONTRASENIA_REMITENTE);
                    }
                });
            
            // Habilitar depuración
            session.setDebug(true);
            
            // Crear mensaje de correo
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(CORREO_REMITENTE, "Monsters University"));
            message.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse(destinatario.trim()));
            message.setSubject("Credenciales de Acceso - Monsters University");
            message.setSentDate(new Date());
            
            // Contenido del correo
            String contenidoHTML = construirContenidoHTML(nombreUsuario, contrasenia);
            String textoPlano = construirContenidoTextoPlano(nombreUsuario, contrasenia);
            
            // Configurar contenido multipart
            MimeMultipart multipart = new MimeMultipart("alternative");
            
            // Parte de texto plano
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textoPlano, "utf-8");
            multipart.addBodyPart(textPart);
            
            // Parte HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(contenidoHTML, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);
            
            // Asignar contenido
            message.setContent(multipart);
            
            // Enviar el correo
            System.out.println("🚀 Enviando correo...");
            Transport.send(message);
            
            System.out.println("✅ CORREO ENVIADO EXITOSAMENTE");
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", 
                "Credenciales enviadas por correo a: " + destinatario));
                
        } catch (MessagingException e) {
            System.err.println("❌ ERROR SMTP: " + e.getMessage());
            e.printStackTrace();
            
            String mensajeError = "Error al enviar correo: ";
            if (e.getMessage().contains("535")) {
                mensajeError += "Credenciales SMTP incorrectas (Error 535). Verifica API Key.";
            } else if (e.getMessage().contains("550") || e.getMessage().contains("554")) {
                mensajeError += "Correo rechazado por el servidor.";
            } else if (e.getMessage().contains("Connection timed out")) {
                mensajeError += "Timeout de conexión.";
            } else {
                mensajeError += e.getMessage();
            }
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error correo", 
                mensajeError));
                
        } catch (Exception e) {
            System.err.println("❌ ERROR inesperado: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", 
                "Error inesperado al enviar correo: " + e.getMessage()));
        }
    }
    
    private String construirContenidoHTML(String nombreUsuario, String contrasenia) {
        return "<!DOCTYPE html>" +
               "<html lang='es'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>Credenciales Monsters University</title>" +
               "<style>" +
               "body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }" +
               ".container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }" +
               ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }" +
               ".header h1 { margin: 0; font-size: 28px; }" +
               ".content { padding: 30px; }" +
               ".credentials-box { background: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; border-radius: 0 5px 5px 0; }" +
               ".credential-item { margin: 15px 0; font-size: 16px; }" +
               ".label { font-weight: bold; color: #555; display: inline-block; width: 120px; }" +
               ".value { color: #222; font-family: 'Courier New', monospace; background: #e9ecef; padding: 5px 10px; border-radius: 3px; }" +
               ".instructions { background: #e7f3ff; border: 1px solid #b6d4fe; border-radius: 5px; padding: 20px; margin: 25px 0; }" +
               ".instructions h3 { color: #084298; margin-top: 0; }" +
               ".btn-container { text-align: center; margin: 30px 0; }" +
               ".btn-access { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 14px 32px; text-decoration: none; border-radius: 30px; font-weight: bold; display: inline-block; transition: transform 0.3s; }" +
               ".btn-access:hover { transform: translateY(-2px); box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4); }" +
               ".warning { background: #fff3cd; border: 1px solid #ffc107; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: center; font-weight: bold; }" +
               ".footer { background: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; border-top: 1px solid #dee2e6; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>Monsters University</h1>" +
               "<p>Sistema de Gestión de Matrículas</p>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>¡Bienvenido/a al Sistema!</h2>" +
               "<p>Se han creado tus credenciales para acceder al sistema de gestión:</p>" +
               "<div class='credentials-box'>" +
               "<div class='credential-item'><span class='label'>👤 Usuario:</span> <span class='value'>" + nombreUsuario + "</span></div>" +
               "<div class='credential-item'><span class='label'>🔑 Contraseña:</span> <span class='value'>" + contrasenia + "</span></div>" +
               "</div>" +
               "<div class='instructions'>" +
               "<h3>📋 Instrucciones de Acceso:</h3>" +
               "<ol>" +
               "<li>Accede al sistema a través del siguiente enlace</li>" +
               "<li>Ingresa las credenciales proporcionadas</li>" +
               "<li>Cambia tu contraseña en tu primer inicio de sesión</li>" +
               "</ol>" +
               "</div>" +
               "<div class='btn-container'>" +
               "<a href='http://localhost:8080/Monster_University' class='btn-access'>🔗 Acceder al Sistema</a>" +
               "</div>" +
               "<div class='warning'>" +
               "⚠️ IMPORTANTE: Esta es una contraseña temporal. Por seguridad, cámbiala inmediatamente después de tu primer acceso." +
               "</div>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>© 2023 Monsters University. Todos los derechos reservados.</p>" +
               "<p>Este es un correo automático, por favor no responder.</p>" +
               "<p><small>Si no solicitaste estas credenciales, por favor ignora este mensaje.</small></p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    private String construirContenidoTextoPlano(String nombreUsuario, String contrasenia) {
        return "MONSTERS UNIVERSITY\n" +
               "=====================\n\n" +
               "CREDENCIALES DE ACCESO AL SISTEMA\n\n" +
               "¡Bienvenido/a!\n\n" +
               "Se han creado tus credenciales para acceder al sistema de gestión:\n\n" +
               "USUARIO: " + nombreUsuario + "\n" +
               "CONTRASEÑA: " + contrasenia + "\n\n" +
               "INSTRUCCIONES DE ACCESO:\n" +
               "1. Accede al sistema: http://localhost:8080/Monster_University\n" +
               "2. Ingresa las credenciales proporcionadas\n" +
               "3. Cambia tu contraseña en tu primer inicio de sesión\n\n" +
               "IMPORTANTE: Esta es una contraseña temporal. Por seguridad, cámbiala inmediatamente después de tu primer acceso.\n\n" +
               "----------------------------------------\n" +
               "© 2023 Monsters University\n" +
               "Este es un correo automático, no responder.\n" +
               "Si no solicitaste estas credenciales, ignora este mensaje.";
    }
}

/**
 * Convertidor para Rol (si se necesita en selects)
 */
@FacesConverter(forClass = Rol.class)
class RolConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        RolDAO rolDAO = new RolDAO();
        return rolDAO.buscarPorCodigo(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Rol) {
            return ((Rol) value).getCodigo();
        }
        return "";
    }
}