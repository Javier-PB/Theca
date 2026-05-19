/**
 * Descripción: DTO para crear una nueva Etiqueta.
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 19 abr 2026
 * 
 */
package com.theca.backend.dto.etiqueta;

import com.theca.backend.enums.EstadoSincronizacion;

import jakarta.validation.constraints.NotBlank;

public class CreateEtiquetaDTO {

	@NotBlank(message = "El nombre de la etiqueta es obligatorio")
    private String nombre;
	private EstadoSincronizacion estadoSincronizacion;
	private String usuarioId;

    public CreateEtiquetaDTO() {}

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
