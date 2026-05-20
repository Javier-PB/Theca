/**
 * Descripción: DTO para representar un cambio pendiente de sincronizar.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 1 may 2026
 */

package com.theca.backend.dto.sync;

public class CambioDTO {
    
    private String entidad;
    private String idEntidad;
    private String operacion;
    private String datosJson;
    private String archivoBase64;
    private String archivoNombre;
    private String archivoContentType;
    
    public String getEntidad() {
    	return entidad;
    }
    
    public void setEntidad(String entidad) {
    	this.entidad = entidad;
    }
    
    public String getIdEntidad() {
    	return idEntidad;
    }
   
    public void setIdEntidad(String idEntidad) {
    	this.idEntidad = idEntidad;
    }
    
    public String getOperacion() {
    	return operacion;
    }
    
    public void setOperacion(String operacion) {
    	this.operacion = operacion;
    }
    
    public String getDatosJson() {
    	return datosJson;
    }
    
    public void setDatosJson(String datosJson) {
    	this.datosJson = datosJson;
    }

	public String getArchivoBase64() {
		return archivoBase64;
	}

	public void setArchivoBase64(String archivoBase64) {
		this.archivoBase64 = archivoBase64;
	}

	public String getArchivoNombre() {
		return archivoNombre;
	}

	public void setArchivoNombre(String archivoNombre) {
		this.archivoNombre = archivoNombre;
	}

	public String getArchivoContentType() {
		return archivoContentType;
	}

	public void setArchivoContentType(String archivoContentType) {
		this.archivoContentType = archivoContentType;
	}
    
}
