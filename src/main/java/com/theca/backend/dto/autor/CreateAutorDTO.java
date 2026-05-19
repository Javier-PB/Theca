/**
 * Descripción: DTO para crear un nuevo Autor.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */
package com.theca.backend.dto.autor;

import com.theca.backend.enums.EstadoSincronizacion;

public class CreateAutorDTO {

    private String nombre;
    private EstadoSincronizacion estadoSincronizacion;
    private String usuarioId;

    public CreateAutorDTO() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

	public EstadoSincronizacion getEstadoSincronizacion() {
		return estadoSincronizacion;
	}

	public void setEstadoSincronizacion(EstadoSincronizacion estadoSincronizacion) {
		this.estadoSincronizacion = estadoSincronizacion;
	}

	public String getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(String usuarioId) {
		this.usuarioId = usuarioId;
	}
    
}
