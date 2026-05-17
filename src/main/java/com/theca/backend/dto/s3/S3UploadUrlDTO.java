/**
 * Descripción: DTO para la actualización de URLs de S3.
 * 
 * @author Javier Pérez Báez
 * @version 1.0
 * @date 17 may 2026
 * 
 */

package com.theca.backend.dto.s3;

public class S3UploadUrlDTO {
    private String uploadUrl;
    private String key;
    
    public S3UploadUrlDTO() {}
    
    public S3UploadUrlDTO(String uploadUrl, String key) {
        this.uploadUrl = uploadUrl;
        this.key = key;
    }
    
    public String getUploadUrl() {
    	return uploadUrl;
    }
    
    public void setUploadUrl(String uploadUrl) {
    	this.uploadUrl = uploadUrl;
    }
    
    public String getKey() {
    	return key;
    }
    
    public void setKey(String key) {
    	this.key = key;
    }
    
}
