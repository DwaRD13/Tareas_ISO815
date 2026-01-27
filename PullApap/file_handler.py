"""
Módulo para manejar la lectura de archivos de nómina
"""

import os


class FileHandler:
    """Maneja la lectura y validación de archivos de nómina"""
    
    @staticmethod
    def leer_archivo_nomina(ruta):
        """
        Lee un archivo de nómina en formato APFC y retorna los datos en formato lista
        
        Formato APFC:
        - Línea E (Encabezado): ENOM + empresa_id (20) + fecha_inicio (10) + fecha_fin (10)
        - Línea D (Detalle): D + cedula (11) + cuenta (20) + monto (con decimales) + nombre (opcional) + NO
        - Línea S (Sumatoria): S + cantidad (11) + total (con decimales)
        
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
            for linea in f:
                linea = linea.strip()
                
                if not linea:
                    continue
                
                # Procesar línea de Encabezado (E)
                if linea.startswith('E'):
                    if len(linea) >= 41:
                        empresa_id = linea[1:21].strip()
                
                # Procesar línea de Detalle (D)
                elif linea.startswith('D'):
                    try:
                        # D + cedula(11) + cuenta(20) + resto (monto + nombre + NO)
                        cedula = linea[1:12].strip()
                        cuenta = linea[12:32].strip()
                        resto = linea[32:]  # Todo lo que viene después de la cuenta
                        
                        # Extraer el monto (buscar el patrón numérico con punto decimal)
                        monto_str = ""
                        nombre = ""
                        
                        # El resto termina con "NO", quitarlo primero
                        if resto.endswith('NO'):
                            contenido = resto[:-2]  # Quitar "NO" del final
                        else:
                            contenido = resto
                        
                        # Buscar el monto (números con punto decimal opcional al inicio)
                        i = 0
                        while i < len(contenido) and (contenido[i].isdigit() or contenido[i] == '.'):
                            monto_str += contenido[i]
                            i += 1
                        
                        # Lo que queda es el nombre
                        nombre = contenido[i:].strip()
                        
                        # Si no hay nombre, usar un valor por defecto
                        if not nombre:
                            nombre = "SIN NOMBRE"
                        
                        # Formatear cédula (agregar guiones si no los tiene)
                        cedula = FileHandler.formatear_cedula(cedula)
                        
                        # Convertir monto a decimal
                        monto_decimal = float(monto_str) if monto_str else 0.0
                        
                        if cedula and cuenta and monto_decimal > 0:
                            datos.append([cedula, nombre, cuenta, str(monto_decimal)])
                    except Exception as e:
                        print(f"Error procesando línea D: {e}")
                        continue
                
                # Línea S (Sumatoria) - solo informativa
                elif linea.startswith('S'):
                    pass
        
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
