package ec.edu.monster.controlador;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import ec.edu.monster.dao.CarreraDAO;
import ec.edu.monster.modelo.Carrera;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;

@Named("carreraController")
@ViewScoped
public class CarreraController implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(CarreraController.class.getName());
    
    private CarreraDAO carreraDAO;
    private Carrera current;
    private Carrera selected;
    private List<Carrera> items;
    
    // Variables para filtros
    private String filtroNombre;
    private Integer filtroMin;
    private Integer filtroMax;
    
    // Lista para mostrar resultados
    private List<Carrera> reportes;
    private List<Carrera> carrerasFiltradas;
    
    @PostConstruct
    public void init() {
        filtroNombre = "";
        filtroMin = null;
        filtroMax = null;
        
        try {
            carreraDAO = new CarreraDAO();
            items = carreraDAO.findAll();
            current = new Carrera();
            
            // Inicializar reportes con datos reales de la base de datos
            cargarTodasCarreras();
            
            LOGGER.info("CarreraController inicializado. Total carreras: " + items.size());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error inicializando CarreraController", e);
            addErrorMessage("Error al inicializar el sistema");
            items = new ArrayList<>();
            reportes = new ArrayList<>();
            carrerasFiltradas = new ArrayList<>();
        }
    }
    
    // ========== MÉTODOS PARA EL XHTML ==========
    
    public void prepareCreate() {
        try {
            current = new Carrera();
            // Generar código automático usando el DAO
            String siguienteCodigo = carreraDAO.generarSiguienteCodigo();
            current.setCodigo(siguienteCodigo);
            
            LOGGER.info("Preparando crear nueva carrera. Código generado: " + siguienteCodigo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al preparar creación de carrera", e);
            current = new Carrera();
            current.setCodigo("CARR001"); // Fallback
            addErrorMessage("Error al generar código automático");
        }
    }
    
    public void prepareView() {
        if (selected != null) {
            current = selected;
            LOGGER.info("Preparando para ver carrera: " + current.getNombre());
        } else {
            addErrorMessage("No hay carrera seleccionada para ver");
        }
    }
    
    public void prepareEdit() {
        if (selected != null) {
            current = selected;
            LOGGER.info("Preparando para editar carrera: " + current.getNombre());
        } else {
            addErrorMessage("No hay carrera seleccionada para editar");
        }
    }
    
    public void create() {
        try {
            if (current == null) {
                addErrorMessage("No hay datos de carrera para crear");
                return;
            }
            
            if (current.getCodigo() == null || current.getCodigo().trim().isEmpty()) {
                addErrorMessage("El código de la carrera es requerido");
                return;
            }
            
            if (current.getNombre() == null || current.getNombre().trim().isEmpty()) {
                addErrorMessage("El nombre de la carrera es requerido");
                return;
            }
            
            // Validar que créditos máximos sean >= créditos mínimos
            if (current.getCreditosMaximos() != null && current.getCreditosMinimos() != null) {
                if (current.getCreditosMaximos() < current.getCreditosMinimos()) {
                    addErrorMessage("Los créditos máximos deben ser mayores o iguales a los créditos mínimos");
                    return;
                }
            }
            
            // Validar que créditos sean números positivos
            if (current.getCreditosMaximos() != null && current.getCreditosMaximos() <= 0) {
                addErrorMessage("Los créditos máximos deben ser mayores a 0");
                return;
            }
            
            if (current.getCreditosMinimos() != null && current.getCreditosMinimos() <= 0) {
                addErrorMessage("Los créditos mínimos deben ser mayores a 0");
                return;
            }
            
            // Validar que el código no exista (por si acaso)
            if (carreraDAO.existeCarrera(current.getCodigo())) {
                addErrorMessage("Ya existe una carrera con el código: " + current.getCodigo());
                // Regenerar código
                String nuevoCodigo = carreraDAO.generarSiguienteCodigo();
                current.setCodigo(nuevoCodigo);
                addErrorMessage("Se ha generado un nuevo código: " + nuevoCodigo + ". Intente guardar nuevamente.");
                return;
            }
            
            boolean creado = carreraDAO.crearCarrera(current);
            if (creado) {
                addSuccessMessage("Carrera creada exitosamente con código: " + current.getCodigo());
                items = carreraDAO.findAll(); // Actualizar lista
                current = new Carrera(); // Resetear para nuevo registro
                // Actualizar reportes también
                cargarTodasCarreras();
            } else {
                addErrorMessage("No se pudo crear la carrera");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al crear carrera", e);
            addErrorMessage("Error al crear la carrera: " + e.getMessage());
        }
    }
    
    public void update() {
        try {
            if (current == null || current.getCodigo() == null) {
                addErrorMessage("No hay carrera seleccionada para actualizar");
                return;
            }
            
            if (current.getNombre() == null || current.getNombre().trim().isEmpty()) {
                addErrorMessage("El nombre de la carrera es requerido");
                return;
            }
            
            // Validar que créditos máximos sean >= créditos mínimos
            if (current.getCreditosMaximos() != null && current.getCreditosMinimos() != null) {
                if (current.getCreditosMaximos() < current.getCreditosMinimos()) {
                    addErrorMessage("Los créditos máximos deben ser mayores o iguales a los créditos mínimos");
                    return;
                }
            }
            
            // Validar que créditos sean números positivos
            if (current.getCreditosMaximos() != null && current.getCreditosMaximos() <= 0) {
                addErrorMessage("Los créditos máximos deben ser mayores a 0");
                return;
            }
            
            if (current.getCreditosMinimos() != null && current.getCreditosMinimos() <= 0) {
                addErrorMessage("Los créditos mínimos deben ser mayores a 0");
                return;
            }
            
            boolean actualizado = carreraDAO.actualizarCarrera(current);
            if (actualizado) {
                addSuccessMessage("Carrera actualizada exitosamente");
                items = carreraDAO.findAll(); // Actualizar lista
                current = new Carrera(); // Resetear current
                selected = null; // Limpiar selección
                // Actualizar reportes también
                cargarTodasCarreras();
            } else {
                addErrorMessage("No se pudo actualizar la carrera");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar carrera", e);
            addErrorMessage("Error al actualizar la carrera: " + e.getMessage());
        }
    }
    
    public void destroy() {
        try {
            if (selected == null) {
                addErrorMessage("No hay carrera seleccionada para eliminar");
                return;
            }
            
            if (selected.getCodigo() != null) {
                boolean eliminada = carreraDAO.eliminarCarrera(selected.getCodigo());
                if (eliminada) {
                    addSuccessMessage("Carrera eliminada exitosamente");
                    items = carreraDAO.findAll();
                    selected = null; // Limpiar selección
                    current = new Carrera();
                    // Actualizar reportes también
                    cargarTodasCarreras();
                } else {
                    addErrorMessage("No se pudo eliminar la carrera");
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar carrera", e);
            addErrorMessage("Error al eliminar la carrera: " + e.getMessage());
        }
    }
    
    // ========== MÉTODOS PARA EL REPORTE ==========
    
    // Método para cargar todas las carreras desde la base de datos
    private void cargarTodasCarreras() {
        try {
            // Cargar todas las carreras desde la base de datos usando el DAO
            reportes = carreraDAO.findAll();
            
            if (reportes == null) {
                reportes = new ArrayList<>();
            }
            
            // Inicialmente, mostrar todas
            carrerasFiltradas = new ArrayList<>(reportes);
            
            LOGGER.info("Carreras cargadas para reporte: " + reportes.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar carreras desde la base de datos", e);
            addErrorMessage("Error al cargar las carreras desde la base de datos");
            reportes = new ArrayList<>();
            carrerasFiltradas = new ArrayList<>();
        }
    }
    
    // Método para buscar carreras con filtros
    public void buscar() {
        try {
            // Si no hay reportes cargados, cargarlos primero
            if (reportes == null || reportes.isEmpty()) {
                cargarTodasCarreras();
            }
            
            // Aplicar filtros
            aplicarFiltros();
            
            String mensaje;
            if (carrerasFiltradas.isEmpty()) {
                mensaje = "No se encontraron carreras con los filtros aplicados";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin resultados", mensaje));
            } else {
                mensaje = "Se encontraron " + carrerasFiltradas.size() + " carreras";
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Búsqueda completada", mensaje));
            }
            
            LOGGER.info("Búsqueda realizada. Resultados: " + carrerasFiltradas.size());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en la búsqueda de carreras", e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error", "No se pudo realizar la búsqueda: " + e.getMessage()));
        }
    }
    
    private void aplicarFiltros() {
        if (reportes == null || reportes.isEmpty()) {
            carrerasFiltradas = new ArrayList<>();
            return;
        }
        
        carrerasFiltradas = reportes.stream()
            .filter(c -> {
                // Filtrar por nombre (si se especificó)
                if (filtroNombre != null && !filtroNombre.trim().isEmpty()) {
                    String nombreCarrera = c.getNombre();
                    if (nombreCarrera == null || 
                        !nombreCarrera.toLowerCase().contains(filtroNombre.toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtrar por créditos mínimos (si se especificó)
                if (filtroMin != null) {
                    Integer creditosMinCarrera = c.getCreditosMinimos();
                    if (creditosMinCarrera == null || creditosMinCarrera < filtroMin) {
                        return false;
                    }
                }
                
                // Filtrar por créditos máximos (si se especificó)
                if (filtroMax != null) {
                    Integer creditosMaxCarrera = c.getCreditosMaximos();
                    if (creditosMaxCarrera == null || creditosMaxCarrera > filtroMax) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    // Método para generar PDF
    public void generarPdf() {
        try {
            // Si no hay carreras filtradas, usar todas
            List<Carrera> datosParaPdf = carrerasFiltradas != null && !carrerasFiltradas.isEmpty() 
                ? carrerasFiltradas 
                : reportes;
            
            if (datosParaPdf == null || datosParaPdf.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                    "Advertencia", "No hay datos para generar el PDF"));
                return;
            }
            
            // Lógica para generar PDF
            generarPdfConDatos(datosParaPdf);
            
            LOGGER.info("PDF generado con " + datosParaPdf.size() + " carreras");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar PDF", e);
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Error", "No se pudo generar el PDF: " + e.getMessage()));
        }
    }
    
    private void generarPdfConDatos(List<Carrera> carreras) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        
        document.open();
        
        // Título
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Reporte de Carreras", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        
        document.add(new Paragraph(" ")); // Espacio
        
        // Fecha
        Font dateFont = new Font(Font.FontFamily.HELVETICA, 10);
        Paragraph date = new Paragraph("Fecha: " + new Date(), dateFont);
        document.add(date);
        
        // Información de filtros (si aplica)
        if ((filtroNombre != null && !filtroNombre.isEmpty()) || 
            filtroMin != null || filtroMax != null) {
            document.add(new Paragraph(" "));
            Paragraph filtros = new Paragraph("Filtros aplicados:", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD));
            document.add(filtros);
            
            StringBuilder filtrosStr = new StringBuilder();
            if (filtroNombre != null && !filtroNombre.isEmpty()) {
                filtrosStr.append("Nombre: ").append(filtroNombre).append(" | ");
            }
            if (filtroMin != null) {
                filtrosStr.append("Créditos Mínimos: ").append(filtroMin).append(" | ");
            }
            if (filtroMax != null) {
                filtrosStr.append("Créditos Máximos: ").append(filtroMax);
            }
            
            if (filtrosStr.length() > 0) {
                document.add(new Paragraph(filtrosStr.toString()));
            }
        }
        
        document.add(new Paragraph(" ")); // Espacio
        
        // Tabla de carreras
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        
        // Encabezados
        table.addCell(new PdfPCell(new Phrase("Código", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Nombre", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Créditos Mínimos", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        table.addCell(new PdfPCell(new Phrase("Créditos Máximos", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD))));
        
        // Datos
        for (Carrera carrera : carreras) {
            table.addCell(carrera.getCodigo() != null ? carrera.getCodigo() : "");
            table.addCell(carrera.getNombre() != null ? carrera.getNombre() : "");
            table.addCell(carrera.getCreditosMinimos() != null ? String.valueOf(carrera.getCreditosMinimos()) : "0");
            table.addCell(carrera.getCreditosMaximos() != null ? String.valueOf(carrera.getCreditosMaximos()) : "0");
        }
        
        document.add(table);
        
        // Total de registros
        document.add(new Paragraph(" "));
        Paragraph total = new Paragraph("Total de carreras: " + carreras.size(), 
            new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
        document.add(total);
        
        document.close();
        
        // Enviar PDF al navegador
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        
        externalContext.responseReset();
        externalContext.setResponseContentType("application/pdf");
        externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"reporte_carreras.pdf\"");
        
        ServletOutputStream out = (ServletOutputStream) externalContext.getResponseOutputStream();
        baos.writeTo(out);
        out.flush();
        
        facesContext.responseComplete();
    }
    
    // Método para limpiar filtros
    public void limpiarFiltros() {
        filtroNombre = "";
        filtroMin = null;
        filtroMax = null;
        
        if (reportes != null) {
            carrerasFiltradas = new ArrayList<>(reportes);
        } else {
            carrerasFiltradas = new ArrayList<>();
        }
        
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
            "Filtros limpiados", "Se muestran todas las carreras"));
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    private void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    private void addWarningMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", msg);
        FacesContext.getCurrentInstance().addMessage(null, facesMsg);
    }
    
    // ========== GETTERS Y SETTERS ==========
    
    public Carrera getCurrent() {
        if (current == null) {
            current = new Carrera();
        }
        return current;
    }
    
    public void setCurrent(Carrera current) {
        this.current = current;
    }
    
    public Carrera getSelected() {
        return selected;
    }
    
    public void setSelected(Carrera selected) {
        this.selected = selected;
    }
    
    public List<Carrera> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }
    
    public void setItems(List<Carrera> items) {
        this.items = items;
    }
    
    // Getters y Setters para filtros y reportes
    public String getFiltroNombre() {
        return filtroNombre;
    }
    
    public void setFiltroNombre(String filtroNombre) {
        this.filtroNombre = filtroNombre;
    }
    
    public Integer getFiltroMin() {
        return filtroMin;
    }
    
    public void setFiltroMin(Integer filtroMin) {
        this.filtroMin = filtroMin;
    }
    
    public Integer getFiltroMax() {
        return filtroMax;
    }
    
    public void setFiltroMax(Integer filtroMax) {
        this.filtroMax = filtroMax;
    }
    
    public List<Carrera> getReportes() {
        // Si no hay carreras filtradas, devolver todas
        if (carrerasFiltradas != null && !carrerasFiltradas.isEmpty()) {
            return carrerasFiltradas;
        }
        // Si hay reportes, devolverlos
        if (reportes != null) {
            return reportes;
        }
        // Si no hay nada, devolver lista vacía
        return new ArrayList<>();
    }
    
    public void setReportes(List<Carrera> reportes) {
        this.reportes = reportes;
    }
    
    public List<Carrera> getCarrerasFiltradas() {
        return carrerasFiltradas;
    }
    
    public void setCarrerasFiltradas(List<Carrera> carrerasFiltradas) {
        this.carrerasFiltradas = carrerasFiltradas;
    }
    
    // ========== GETTERS PARA EL XHTML ==========
    
    public String getRowKey(Carrera carrera) {
        return carrera != null ? carrera.getCodigo() : null;
    }
    
    // ========== GETTERS PARA LAS PROPIEDADES DEL CURRENT ==========
    
    public String getCurrentCodigo() {
        return current != null ? current.getCodigo() : "";
    }
    
    public void setCurrentCodigo(String codigo) {
        if (current != null) {
            current.setCodigo(codigo);
        }
    }
    
    public String getCurrentNombre() {
        return current != null ? current.getNombre() : "";
    }
    
    public void setCurrentNombre(String nombre) {
        if (current != null) {
            current.setNombre(nombre);
        }
    }
    
    public Integer getCurrentCreditosMaximos() {
        return current != null ? current.getCreditosMaximos() : null;
    }
    
    public void setCurrentCreditosMaximos(Integer creditosMaximos) {
        if (current != null) {
            current.setCreditosMaximos(creditosMaximos);
        }
    }
    
    public Integer getCurrentCreditosMinimos() {
        return current != null ? current.getCreditosMinimos() : null;
    }
    
    public void setCurrentCreditosMinimos(Integer creditosMinimos) {
        if (current != null) {
            current.setCreditosMinimos(creditosMinimos);
        }
    }
}