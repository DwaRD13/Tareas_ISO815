"""
Módulo para manejar la conexión y operaciones con Supabase
"""

from supabase import create_client, Client
from config import SUPABASE_URL, SUPABASE_KEY


class DatabaseManager:
    """Gestor de conexión y operaciones con Supabase"""
    
    def __init__(self):
        """Inicializa la conexión con Supabase"""
        try:
            self.supabase: Client = create_client(SUPABASE_URL, SUPABASE_KEY)
            print("Conexión con Supabase exitosa")
        except Exception as e:
            print(f"Error de configuración: {e}")
            raise
    
    def insertar_pago(self, cedula, nombre, cuenta_destino, monto, estado, archivo_origen=None, empresa_id=None):
        """
        Inserta un registro de pago en la base de datos
        
        Args:
            cedula: Cédula del empleado
            nombre: Nombre del empleado
            cuenta_destino: Número de cuenta bancaria
            monto: Monto del pago
            estado: Estado del pago (APLICADO, ERROR_DATOS, etc.)
            archivo_origen: Nombre del archivo de nómina (opcional)
            empresa_id: ID de la empresa del encabezado (opcional)
        
        Returns:
            Response de Supabase
        
        Raises:
            Exception: Si hay error en la inserción
        """
        def _truncate(value, max_len):
            if value is None:
                return None
            texto = str(value)
            return texto[:max_len]

        # Ajustar longitudes para columnas varchar(20)
        cuenta_destino = _truncate(cuenta_destino, 20)
        empresa_id = _truncate(empresa_id, 20)

        datos_json = {
            "cedula": cedula,
            "nombre": nombre,
            "cuenta_destino": cuenta_destino,
            "monto": monto,
            "estado": estado
        }
        
        if archivo_origen:
            datos_json["archivo_origen"] = archivo_origen
        
        if empresa_id:
            datos_json["empresa_id"] = empresa_id
        
        return self.supabase.table("pagos_recibidos").insert(datos_json).execute()
