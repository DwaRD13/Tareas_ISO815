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
        Lee un archivo de nómina en formato JSON y retorna los datos en formato lista

        Formato JSON esperado:
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
            ],
            "sumatoria": {
                "cantidad": 0,
                "total": 0
            }
        }

        Args:
            ruta: Ruta del archivo a leer

        Returns:
            Tupla (datos, empresa_id, nombre_archivo) donde:
            - datos: Lista de listas con [cedula, nombre, cuenta, monto]
            - empresa_id: ID de la empresa del encabezado
            - nombre_archivo: Nombre del archivo procesado

        Raises:
            FileNotFoundError: Si el archivo no existe
            Exception: Si hay error al leer el archivo
        """
        if not ruta:
            raise ValueError("La ruta del archivo no puede estar vacía")
        
        if not os.path.exists(ruta):
            raise FileNotFoundError("El archivo no existe")
        
        datos = []
        empresa_id = None
        nombre_archivo = os.path.basename(ruta)

        with open(ruta, "r", encoding="utf-8") as f:
            contenido = json.load(f)

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
                    datos.append([cedula, nombre, cuenta, str(monto_decimal)])
            except Exception as e:
                print(f"Error procesando detalle JSON: {e}")
                continue

        return datos, empresa_id, nombre_archivo
    
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
