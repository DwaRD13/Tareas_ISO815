"""
Módulo para manejar la lectura de archivos de nómina
"""

import json
import os


class FileHandler:
    """Maneja la lectura y validación de archivos de nómina"""
    
    @staticmethod
    def leer_archivo_nomina(ruta):
        """
        Lee un archivo de nómina/crédito en formato JSON y retorna los datos en formato lista

        Nuevo formato JSON esperado (créditos estudiantiles):
        {
            "metadata": {
                "transacion_id": "...",
                "timestamp": "...",
                "sistema_origen": "FUNDAPEC"
            },
            "estudiante": {
                "matricula": "...",
                "estado": "...",
                "periodo_academico": "...",
                "descripcion_periodo": "..."
            },
            "detalle_pago": {
                "codigo_pago": "...",
                "monto_aprobado": 0.0,
                "moneda": "DOP",
                "fecha_aprobacion": "..."
            }
        }

        Formato anterior (nómina):
        {
            "encabezado": {
                "empresa_id": "...",
                "fecha_inicio": "dd/mm/aaaa",
                "fecha_fin": "dd/mm/aaaa"
            },
            "detalles": [
                {
                    "cedula": "...",
                    "cuenta": "...",
                    "monto": 0,
                    "nombre": "..."
                }
            ]
        }

        Args:
            ruta: Ruta del archivo a leer

        Returns:
            Tupla (datos_db, datos_tabla, empresa_id, nombre_archivo, columnas) donde:
            - datos_db: Lista de listas con [cedula, nombre, cuenta, monto]
            - datos_tabla: Lista de listas con columnas a mostrar en la tabla
            - empresa_id: ID de la empresa/sistema origen
            - nombre_archivo: Nombre del archivo procesado
            - columnas: Lista de nombres de columnas para la tabla

        Raises:
            FileNotFoundError: Si el archivo no existe
            Exception: Si hay error al leer el archivo
        """
        if not ruta:
            raise ValueError("La ruta del archivo no puede estar vacía")
        
        if not os.path.exists(ruta):
            raise FileNotFoundError("El archivo no existe")
        
        datos_db = []
        datos_tabla = []
        empresa_id = None
        nombre_archivo = os.path.basename(ruta)
        columnas = []

        with open(ruta, "r", encoding="utf-8") as f:
            contenido = json.load(f)

        # Detectar el formato del JSON
        if "metadata" in contenido and "estudiante" in contenido and "detalle_pago" in contenido:
            # Nuevo formato: créditos estudiantiles
            columnas = ["Matricula", "Estado", "Periodo", "Monto aprobado"]
            metadata = contenido.get("metadata", {})
            estudiante = contenido.get("estudiante", {})
            detalle_pago = contenido.get("detalle_pago", {})
            
            # Extraer empresa_id del sistema origen
            empresa_id = metadata.get("sistema_origen", "DESCONOCIDO")
            
            # Mapear campos del nuevo formato
            matricula = str(estudiante.get("matricula", "")).strip()
            estado = str(estudiante.get("estado", "")).strip()
            periodo = str(estudiante.get("descripcion_periodo", "")).strip()
            
            codigo_pago = str(detalle_pago.get("codigo_pago", "")).strip()
            monto_aprobado = detalle_pago.get("monto_aprobado", 0)
            fecha_aprobacion = str(detalle_pago.get("fecha_aprobacion", "")).strip()
            
            # Construir nombre descriptivo para persistencia
            nombre = f"Est. {matricula}"
            if periodo:
                nombre += f" - {periodo}"
            
            # Usar matrícula como identificador (sin formatear como cédula)
            cedula = matricula
            
            # Usar código de pago como cuenta
            cuenta = codigo_pago if codigo_pago else matricula
            
            monto_decimal = float(monto_aprobado) if monto_aprobado is not None else 0.0
            
            if cedula and cuenta and monto_decimal > 0:
                datos_db.append([cedula, nombre, cuenta, str(monto_decimal)])
                datos_tabla.append([matricula, estado, periodo, str(monto_decimal)])
        
        else:
            # Formato anterior: nómina tradicional
            columnas = ["Cedula", "Nombre", "Cuenta", "Monto"]
            encabezado = contenido.get("encabezado", {}) if isinstance(contenido, dict) else {}
            empresa_id = encabezado.get("empresa_id")

            detalles = contenido.get("detalles", []) if isinstance(contenido, dict) else []
            for item in detalles:
                try:
                    cedula = str(item.get("cedula", "")).strip()
                    cuenta = str(item.get("cuenta", "")).strip()
                    nombre = str(item.get("nombre", "")).strip()
                    monto_valor = item.get("monto", 0)

                    if not nombre:
                        nombre = "SIN NOMBRE"

                    cedula = FileHandler.formatear_cedula(cedula)

                    monto_decimal = float(monto_valor) if monto_valor is not None else 0.0

                    if cedula and cuenta and monto_decimal > 0:
                        datos_db.append([cedula, nombre, cuenta, str(monto_decimal)])
                        datos_tabla.append([cedula, nombre, cuenta, str(monto_decimal)])
                except Exception as e:
                    print(f"Error procesando detalle JSON: {e}")
                    continue

        return datos_db, datos_tabla, empresa_id, nombre_archivo, columnas
    
    @staticmethod
    def formatear_cedula(cedula):
        """
        Formatea una cédula sin guiones al formato XXX-XXXXXXX-X
        
        Args:
            cedula: Cédula sin formato (11 dígitos)
        
        Returns:
            Cédula formateada con guiones
        """
        # Remover caracteres no numéricos
        cedula = ''.join(c for c in cedula if c.isdigit())
        
        if len(cedula) == 11:
            return f"{cedula[0:3]}-{cedula[3:10]}-{cedula[10]}"
        
        return cedula
    
    @staticmethod
    def validar_monto(monto_texto):
        """
        Valida y convierte un monto de texto a float
        
        Args:
            monto_texto: Monto en formato texto
        
        Returns:
            Tupla (monto_float, estado)
            - monto_float: Monto convertido a float
            - estado: "APLICADO" si es válido, "ERROR_DATOS" si no lo es
        """
        try:
            monto_final = float(monto_texto)
            if monto_final < 0:
                raise ValueError("Monto negativo")
            return monto_final, "APLICADO"
        except:
            return 0.0, "ERROR_DATOS"
