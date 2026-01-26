"""
Módulo para manejar la lectura de archivos de nómina
"""

import os


class FileHandler:
    """Maneja la lectura y validación de archivos de nómina"""
    
    @staticmethod
    def leer_archivo_nomina(ruta):
        """
        Lee un archivo de nómina y retorna los datos en formato lista
        
        Args:
            ruta: Ruta del archivo a leer
        
        Returns:
            Lista de listas con los datos [cedula, nombre, cuenta, monto]
        
        Raises:
            FileNotFoundError: Si el archivo no existe
            Exception: Si hay error al leer el archivo
        """
        if not ruta:
            raise ValueError("La ruta del archivo no puede estar vacía")
        
        if not os.path.exists(ruta):
            raise FileNotFoundError("El archivo no existe")
        
        datos = []
        
        with open(ruta, "r", encoding="utf-8") as f:
            for linea in f:
                partes = linea.strip().split(',')
                if len(partes) == 4:
                    datos.append(partes)
        
        return datos
    
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
