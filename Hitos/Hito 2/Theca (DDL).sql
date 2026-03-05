CREATE DATABASE theca;

CREATE TABLE T_USUARIO (
    identificador SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    correo VARCHAR(255) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE T_CATEGORIA (
    identificador SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_sincronizacion VARCHAR(20),
    id_categoria_padre INT NULL,
    CONSTRAINT fk_categoria_padre FOREIGN KEY (id_categoria_padre) 
        REFERENCES T_CATEGORIA(identificador) ON DELETE SET NULL,
    CONSTRAINT chk_estado_categoria CHECK (
        estado_sincronizacion IN ('SINCRONIZADO', 'PENDIENTE', 'CONFLICTO')
    )
);

CREATE TABLE T_ETIQUETA (
    identificador SERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT null,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_sincronizacion VARCHAR(20),
    CONSTRAINT chk_estado_etiqueta CHECK (
        estado_sincronizacion IN ('SINCRONIZADO', 'PENDIENTE', 'CONFLICTO')
    )
);

CREATE TABLE T_RECURSO (
    identificador SERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    autor VARCHAR(255),
    tipo VARCHAR(50) unique NOT NULL,
    enlace VARCHAR(500),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado_sincronizacion VARCHAR(20),
    version INT,
    id_usuario INT NOT NULL,
    CONSTRAINT fk_recurso_usuario FOREIGN KEY (id_usuario) 
        REFERENCES T_USUARIO(identificador) ON DELETE cascade,
	CONSTRAINT chk_estado_recurso CHECK (
        estado_sincronizacion IN ('SINCRONIZADO', 'PENDIENTE', 'CONFLICTO')
    )
);

CREATE TABLE T_RECURSO_CATEGORIA (
    identificador SERIAL PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_categoria INT NOT NULL,
    CONSTRAINT fk_rc_recurso FOREIGN KEY (id_recurso) 
        REFERENCES T_RECURSO(identificador) ON DELETE CASCADE,
    CONSTRAINT fk_rc_categoria FOREIGN KEY (id_categoria) 
        REFERENCES T_CATEGORIA(identificador) ON DELETE CASCADE,
    CONSTRAINT uq_recurso_categoria UNIQUE (id_recurso, id_categoria)
);

CREATE TABLE T_RECURSO_ETIQUETA (
    identificador SERIAL PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_etiqueta INT NOT NULL,
    CONSTRAINT fk_re_etiqueta_recurso FOREIGN KEY (id_recurso) 
        REFERENCES T_RECURSO(identificador) ON DELETE CASCADE,
    CONSTRAINT fk_re_etiqueta_etiqueta FOREIGN KEY (id_etiqueta) 
        REFERENCES T_ETIQUETA(identificador) ON DELETE CASCADE,
    CONSTRAINT uq_recurso_etiqueta UNIQUE (id_recurso, id_etiqueta)
);

CREATE TABLE T_COLA (
    identificador SERIAL PRIMARY KEY,
    entidad VARCHAR(50) NOT NULL,
    id_entidad INT NOT NULL,
    operacion VARCHAR(10) NOT NULL,
    json_datos_cambiados JSONB NOT NULL,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sincronizado BOOLEAN DEFAULT FALSE,
    CONSTRAINT chk_entidad CHECK (entidad IN ('RECURSO', 'ETIQUETA', 'CATEGORIA')),
    CONSTRAINT chk_operacion CHECK (operacion IN ('CREATE', 'INSERT', 'UPDATE', 'DELETE'))
);