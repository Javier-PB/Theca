/**
 * Descripción: DTO para la descarga desde URLs de S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.dto.s3;

public class S3DownloadUrlDTO {
	
    private String downloadUrl;
    private String key;
    
    public S3DownloadUrlDTO() {}
    
    public S3DownloadUrlDTO(String downloadUrl, String key) {
        this.downloadUrl = downloadUrl;
        this.key = key;
    }
    
    public String getDownloadUrl() {
    	return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
    	this.downloadUrl = downloadUrl;
    }
    
    public String getKey() {
    	return key;
    }
    
    public void setKey(String key) {
    	this.key = key;
    }
    
}
