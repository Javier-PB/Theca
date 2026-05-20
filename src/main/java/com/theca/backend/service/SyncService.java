package com.theca.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theca.backend.dto.sync.CambioDTO;
import com.theca.backend.dto.sync.SyncPushRequestDTO;
import com.theca.backend.dto.sync.SyncPullResponseDTO;
import com.theca.backend.entity.Cola;
import com.theca.backend.enums.EntidadCola;
import com.theca.backend.enums.OperacionCola;
import com.theca.backend.repository.ColaRepository;
import com.theca.backend.s3.GestorObjetosS3;

@Service
public class SyncService {

    @Autowired
    private ColaRepository colaRepository;
    
    @Autowired
    private GestorObjetosS3 gestorObjetosS3;
    
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Método que recibe cambios del cliente y los guarda en la cola:
    public void pushChanges(SyncPushRequestDTO request) {
        for (CambioDTO cambio : request.getCambios()) {
            
            String archivoKey = procesarArchivoOffline(cambio);
            
            String datosJsonFinal = limpiarYActualizarJson(cambio.getDatosJson(), archivoKey);
            
            Cola registro = new Cola();
            registro.setEntidad(EntidadCola.valueOf(cambio.getEntidad()));
            registro.setIdEntidad(cambio.getIdEntidad());
            registro.setOperacion(OperacionCola.valueOf(cambio.getOperacion()));
            registro.setJsonDatosCambiados(datosJsonFinal);
            registro.setFechaModificacion(LocalDateTime.now());
            registro.setSincronizado(false);
            
            colaRepository.save(registro);
        }
    }
    
    // Método para procesar un archivo offline y subirlo a S3:
    private String procesarArchivoOffline(CambioDTO cambio) {
        if (!"RECURSO".equals(cambio.getEntidad()) || 
            !"CREATE".equals(cambio.getOperacion()) ||
            cambio.getArchivoBase64() == null ||
            cambio.getArchivoBase64().isEmpty()) {
            return null;
        }
        
        try {
            byte[] archivoBytes = Base64.getDecoder().decode(cambio.getArchivoBase64());
            
            String key = "offline-demo/" + System.currentTimeMillis() + "-" + 
                         (cambio.getArchivoNombre() != null ? cambio.getArchivoNombre() : "archivo.bin");
            
            String contentType = cambio.getArchivoContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }
            
            gestorObjetosS3.subirArchivoDesdeBytes(key, archivoBytes, contentType);
            
            System.out.println("✅ Archivo offline subido a S3: " + key);
            return key;
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar archivo offline: " + e.getMessage());
            return null;
        }
    }
    
    // Método para limpiar campos temporales del JSON y añadir archivoKey:
    private String limpiarYActualizarJson(String datosJson, String archivoKey) {
        if (datosJson == null || datosJson.isEmpty()) {
            return datosJson;
        }
        
        try {
            JsonNode node = objectMapper.readTree(datosJson);
            
            if (node.isObject()) {
                ObjectNode objectNode = (ObjectNode) node;
                
                objectNode.remove("archivoBase64");
                objectNode.remove("archivoNombre");
                objectNode.remove("archivoContentType");
                
                if (archivoKey != null && !archivoKey.isEmpty()) {
                    objectNode.put("archivoKey", archivoKey);
                }
                
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (Exception e) {
            System.err.println("Error al limpiar JSON: " + e.getMessage());
        }
        
        return datosJson;
    }

    // Método que devuelve los cambios pendientes al cliente:
    public SyncPullResponseDTO pullChanges() {
        List<Cola> pendientes = colaRepository.findBySincronizadoFalse();
        List<CambioDTO> cambios = new ArrayList<>();
        
        for (Cola cola : pendientes) {
            CambioDTO cambio = new CambioDTO();
            cambio.setEntidad(cola.getEntidad().name());
            cambio.setIdEntidad(cola.getIdEntidad());
            cambio.setOperacion(cola.getOperacion().name());
            cambio.setDatosJson(cola.getJsonDatosCambiados());
            cambios.add(cambio);
        }
        
        return new SyncPullResponseDTO(cambios, LocalDateTime.now().toString());
    }
    
    // Método que marca los cambios como sincronizados:
    public void markAsSynced(List<String> ids) {
        for (String id : ids) {
            colaRepository.findById(id).ifPresent(cola -> {
                cola.setSincronizado(true);
                colaRepository.save(cola);
            });
        }
    }
    
}