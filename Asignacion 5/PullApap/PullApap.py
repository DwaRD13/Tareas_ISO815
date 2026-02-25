import flet as ft
from fastapi import FastAPI
from pydantic import BaseModel
from typing import List, Optional
import uvicorn
import threading

from database import DatabaseManager

db_manager = DatabaseManager()


app = FastAPI(title="API y UI de Nómina - Banco APAP")

class Empresa(BaseModel):
    id: int = 1 # Valor por defecto por si no viene
    RNC: int
    nombre: str
    formaPago: Optional[str] = None
    cuentaBancaria: Optional[str] = None

class DetalleNomina(BaseModel):
    cedula: str
    cuentaDestino: str
    sueldoNeto: float
    tipoCuenta: str
    sueldoBrutoPeriodo: Optional[float] = None
    descuentoTSS: Optional[float] = None

class NominaDTO(BaseModel):
    empresa: Empresa
    fechaPago: str
    totalPagado: float
    cantidadRegistros: int
    detalles: List[DetalleNomina]

actualizar_ui_callback = None

@app.post("/api/nomina/recibirDatos")
async def procesar_nomina(nomina: NominaDTO):
    print(f"Recibiendo nómina de la empresa: {nomina.empresa.nombre}")
    
    registros_exitosos = 0
    registros_fallidos = 0

    for detalle in nomina.detalles:
        estado_transaccion = "APLICADO"
        try:
            db_manager.insertar_pago(
                cedula=detalle.cedula,
                nombre="Empleado desde JavaFX", 
                cuenta=detalle.cuentaDestino,
                monto=detalle.sueldoNeto,
                estado=estado_transaccion,
                archivo_origen="API REST",
                empresa_id=nomina.empresa.RNC   
            )
            registros_exitosos += 1
        except Exception as e:
            print(f"Error BD: {str(e)}")
            registros_fallidos += 1

    if actualizar_ui_callback:
        actualizar_ui_callback(nomina.detalles, nomina.empresa.nombre)

    return {"status": "success", "mensaje": f"OK! {registros_exitosos} registros subidos"}


def arrancar_api_en_fondo():
    uvicorn.run(app, host="0.0.0.0", port=8080)



def main(page: ft.Page):
    global actualizar_ui_callback

    page.title = "Banco APAP - Dashboard en Tiempo Real"
    page.window.width = 1400
    page.window.height = 900
    page.theme_mode = ft.ThemeMode.LIGHT
    page.padding = 0
    page.bgcolor = "#f8f9fa"

    badge_registros = ft.Container(
        content=ft.Text("0", color="white", size=12, weight="bold"),
        bgcolor="#2e7d32", padding=ft.Padding(left=10, right=10, top=5, bottom=5),
        border_radius=12, visible=False,
    )

    txt_estado = ft.Text("Esperando conexión desde JavaFX...", size=15, weight="w500", color="grey")

    tabla_datos = ft.DataTable(
        columns=[
            ft.DataColumn(ft.Text("Cédula", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Nombre", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Cuenta", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Monto", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Estado", weight="bold", size=14)),
        ],
        rows=[], border=ft.Border.all(1, "#e0e0e0"), heading_row_color="#f5f5f5",
    )

    def actualizar_pantalla(detalles_recibidos: List[DetalleNomina], nombre_empresa: str):
        txt_estado.value = f"¡Nómina de {nombre_empresa} recibida y guardada en BD!"
        txt_estado.color = "green"
        
        for det in detalles_recibidos:
            tabla_datos.rows.append(
                ft.DataRow(cells=[
                    ft.DataCell(ft.Text(det.cedula, size=13)),
                    ft.DataCell(ft.Text("Empleado", size=13)),
                    ft.DataCell(ft.Text(det.cuentaDestino, size=13)),
                    ft.DataCell(ft.Text(f"${det.sueldoNeto:,.2f}", size=13, weight="bold", color="#2e7d32")),
                    ft.DataCell(ft.Icon(ft.Icons.CHECK_CIRCLE, color="green", size=20)),
                ])
            )
        
        badge_registros.value = str(len(tabla_datos.rows))
        badge_registros.visible = True
        btn_limpiar.visible = True
        
        page.update()

    actualizar_ui_callback = actualizar_pantalla

    def limpiar_todo(e):
        tabla_datos.rows.clear()
        badge_registros.visible = False
        btn_limpiar.visible = False
        txt_estado.value = "Esperando conexión desde JavaFX..."
        txt_estado.color = "grey"
        page.update()

    btn_limpiar = ft.Container(
        content=ft.Text("Limpiar Pantalla", size=14, weight="bold", color="white"),
        bgcolor="#757575", padding=15, border_radius=8, ink=True, on_click=limpiar_todo, visible=False
    )

    header = ft.Container(
        content=ft.Row([
            ft.Container(content=ft.Text("APAP", size=28, weight="bold", color="white"), bgcolor="#1976d2", padding=15, border_radius=10),
            ft.Column([
                ft.Text("ASOCIACIÓN POPULAR DE AHORROS Y PRÉSTAMOS", size=12, color="#666"),
                ft.Text("Dashboard de Recepción API", size=24, weight="bold", color="#212121"),
            ], spacing=2),
        ], spacing=20), bgcolor="white", padding=30, border=ft.Border(bottom=ft.BorderSide(2, "#e0e0e0"))
    )

    card_info = ft.Container(
        content=ft.Row([
            ft.Column([
                ft.Row([ft.Text("Registros Recibidos:", size=18, weight="bold"), badge_registros]),
                txt_estado
            ])
        ]), bgcolor="white", padding=30, border_radius=12, margin=ft.Margin(top=20, bottom=20, left=0, right=0)
    )

    contenedor_tabla = ft.Container(
        content=ft.Column([tabla_datos], scroll=ft.ScrollMode.AUTO),
        bgcolor="white", border=ft.Border.all(1, "#e0e0e0"), border_radius=12, padding=20, height=450
    )

    page.add(
        ft.Column([
            header,
            ft.Container(
                content=ft.Column([card_info, contenedor_tabla, ft.Container(content=btn_limpiar, alignment=ft.Alignment.CENTER, padding=20)]),
                width=1200, padding=40
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER)
    )


if __name__ == "__main__":
    hilo_api = threading.Thread(target=arrancar_api_en_fondo, daemon=True)
    hilo_api.start()
    
    ft.app(target=main)