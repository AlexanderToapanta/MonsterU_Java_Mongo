/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.edu.monster.modelo;

import java.io.Serializable;

public class UserCache implements Serializable {
    private String usuario;
    private String perfil;
    private String nombre;
    private String foto;
    private String direccion;
    private String telefono;
    private String email;
    private String codigo;
    private String rol;

    public UserCache() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
    
    /**
     * Método para limpiar todos los datos del usuario
     */
    public void clear() {
        this.usuario = null;
        this.perfil = null;
        this.nombre = null;
        this.foto = null;
        this.direccion = null;
        this.telefono = null;
        this.email = null;
        this.codigo = null;
        this.rol = null;
    }
    
    /**
     * Verifica si el usuario tiene datos en caché
     */
    public boolean isEmpty() {
        return usuario == null && nombre == null && email == null;
    }
    
    @Override
    public String toString() {
        return "UserCache{" +
                "usuario='" + usuario + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", codigo='" + codigo + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}