/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelo;

import java.io.Serializable;
import java.util.List;

public class UserCache implements Serializable {
    private String usuario;
    private String perfil;
    private String nombre;
    private String foto;
    private String direccion;
    private String telefono;
    private String Email;
    private String Documento;
    private String TipoPersona;
 private String rol;
    private String codigoRol;
    private List<String> opciones;
    public UserCache() {
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public String getDocumento() {
        return Documento;
    }

    public void setDocumento(String Documento) {
        this.Documento = Documento;
    }

    public String getTipoPersona() {
        return TipoPersona;
    }

    public void setTipoPersona(String TipoPersona) {
        this.TipoPersona = TipoPersona;
    }

   
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public List<String> getOpciones() {
        return opciones;
    }
    
    public void setOpciones(List<String> opciones) {
        this.opciones = opciones;
    }
    
    public boolean tieneOpcion(String codigoOpcion) {
        return opciones != null && opciones.contains(codigoOpcion);
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
    }
    
    public String getCodigoRol() {
        return codigoRol;
    }
    
    public void setCodigoRol(String codigoRol) {
        this.codigoRol = codigoRol;
    }
}
