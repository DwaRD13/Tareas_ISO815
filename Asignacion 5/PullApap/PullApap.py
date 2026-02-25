import flet as ft
from fastapi import FastAPI
from pydantic import BaseModel, validator
from typing import List, Optional
import uvicorn
import threading

from database import DatabaseManager

db_manager = DatabaseManager()

app = FastAPI(title="API y UI de Nómina - Banco APAP")

class Empresa(BaseModel):
    id: int
    RNC: int
    nombre: str
    formaPago: Optional[str] = None
    cuentaBancaria: Optional[str] = None

    @validator('RNC', pre=True)
    def convert_rnc_to_str(cls, v):
        return str(v) if v is not None else v

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
    print(f"Fecha de pago: {nomina.fechaPago}")
    print(f"Total a pagar: {nomina.totalPagado}")

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
                empresa_id=nomina.empresa.id   
            )
            registros_exitosos += 1
        except Exception as e:
            print(f"Error BD: {str(e)}")
            registros_fallidos += 1

    if actualizar_ui_callback:
        actualizar_ui_callback(nomina)  
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
        bgcolor="#2e7d32",
        padding=ft.Padding(left=10, right=10, top=5, bottom=5),
        border_radius=12,
        visible=False,
    )

    txt_estado = ft.Text("Esperando conexión desde JavaFX...", size=15, weight="w500", color="grey")

    resumen_empresa = ft.Text("", size=16, weight="bold")
    resumen_fecha = ft.Text("")
    resumen_total = ft.Text("")
    resumen_rnc = ft.Text("")
    resumen_cuenta_empresa = ft.Text("")
    resumen_forma_pago = ft.Text("")

    tarjeta_resumen = ft.Container(
        content=ft.Column([
            ft.Row([ft.Text("📋 Resumen de la nómina recibida", size=20, weight="bold")]),
            ft.Divider(height=10),
            ft.Row([
                ft.Column([resumen_empresa, resumen_rnc], spacing=5),
                ft.Column([resumen_fecha, resumen_forma_pago], spacing=5),
                ft.Column([resumen_total, resumen_cuenta_empresa], spacing=5),
            ], alignment=ft.MainAxisAlignment.SPACE_BETWEEN),
        ]),
        bgcolor="white",
        padding=20,
        border_radius=12,
        shadow=ft.BoxShadow(blur_radius=10, color="#00000010", offset=ft.Offset(0, 4)),
        visible=False,  
    )

    tabla_datos = ft.DataTable(
        columns=[
            ft.DataColumn(ft.Text("Cédula", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Tipo Cuenta", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Cuenta Destino", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Sueldo Bruto", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Descuento TSS", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Sueldo Neto", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Estado", weight="bold", size=14)),
        ],
        rows=[],
        border=ft.Border.all(1, "#e0e0e0"),
        heading_row_color="#f5f5f5",
        heading_row_height=50,
        data_row_min_height=45,
        horizontal_lines=ft.BorderSide(1, "#f0f0f0"),
    )

    contenedor_tabla = ft.Container(
        content=ft.Column([tabla_datos], scroll=ft.ScrollMode.AUTO),
        bgcolor="white",
        border=ft.Border.all(1, "#e0e0e0"),
        border_radius=12,
        padding=20,
        height=400,
    )

    btn_limpiar = ft.Container(
        content=ft.Text("Limpiar Pantalla", size=14, weight="bold", color="white"),
        bgcolor="#757575",
        padding=15,
        border_radius=8,
        ink=True,
        on_click=lambda e: limpiar_todo(),
        visible=False,
    )

    def actualizar_pantalla(nomina: NominaDTO):
        resumen_empresa.value = f"Empresa: {nomina.empresa.nombre}"
        resumen_rnc.value = f"RNC: {nomina.empresa.RNC}"
        resumen_fecha.value = f"Fecha pago: {nomina.fechaPago}"
        resumen_forma_pago.value = f"Forma pago: {nomina.empresa.formaPago or 'N/A'}"
        resumen_total.value = f"Total pagado: RD$ {nomina.totalPagado:,.2f}"
        resumen_cuenta_empresa.value = f"Cuenta empresa: {nomina.empresa.cuentaBancaria or 'N/A'}"
        tarjeta_resumen.visible = True

        tabla_datos.rows.clear()

        for det in nomina.detalles:
            bruto = f"RD$ {det.sueldoBrutoPeriodo:,.2f}" if det.sueldoBrutoPeriodo else "N/A"
            descuento = f"RD$ {det.descuentoTSS:,.2f}" if det.descuentoTSS else "N/A"
            neto = f"RD$ {det.sueldoNeto:,.2f}"

            tabla_datos.rows.append(
                ft.DataRow(
                    cells=[
                        ft.DataCell(ft.Text(det.cedula, size=13)),
                        ft.DataCell(ft.Text(det.tipoCuenta, size=13)),
                        ft.DataCell(ft.Text(det.cuentaDestino, size=13)),
                        ft.DataCell(ft.Text(bruto, size=13)),
                        ft.DataCell(ft.Text(descuento, size=13)),
                        ft.DataCell(ft.Text(neto, size=13, weight="bold", color="#2e7d32")),
                        ft.DataCell(ft.Icon(ft.Icons.CHECK_CIRCLE, color="green", size=20)),
                    ]
                )
            )

        # Actualizar badge y estado
        badge_registros.value = str(len(nomina.detalles))
        badge_registros.visible = True
        txt_estado.value = f"¡Nómina de {nomina.empresa.nombre} recibida y guardada en BD!"
        txt_estado.color = "green"
        btn_limpiar.visible = True

        page.update()

    actualizar_ui_callback = actualizar_pantalla

    def limpiar_todo():
        tabla_datos.rows.clear()
        badge_registros.visible = False
        btn_limpiar.visible = False
        tarjeta_resumen.visible = False
        txt_estado.value = "Esperando conexión desde JavaFX..."
        txt_estado.color = "grey"
        page.update()

    header = ft.Container(
        content=ft.Row([
            ft.Container(
                content=ft.Text("APAP", size=28, weight="bold", color="white"),
                bgcolor="#1976d2",
                padding=15,
                border_radius=10,
            ),
            ft.Column([
                ft.Text("ASOCIACIÓN POPULAR DE AHORROS Y PRÉSTAMOS", size=12, color="#666"),
                ft.Text("Nomina recibidas", size=24, weight="bold", color="#212121"),
            ], spacing=2),
        ], spacing=20),
        bgcolor="white",
        padding=30,
        border=ft.Border(bottom=ft.BorderSide(2, "#e0e0e0")),
    )

    card_info = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Text("Registros Recibidos:", size=18, weight="bold"),
                badge_registros,
            ]),
            txt_estado,
            tarjeta_resumen,
        ]),
        bgcolor="white",
        padding=30,
        border_radius=12,
        shadow=ft.BoxShadow(blur_radius=10, color="#00000010", offset=ft.Offset(0, 4)),
        margin=ft.Margin(top=20, bottom=20, left=0, right=0),
    )

    page.add(
        ft.Column([
            header,
            ft.Container(
                content=ft.Column([
                    card_info,
                    contenedor_tabla,
                    ft.Container(
                        content=btn_limpiar,
                        alignment=ft.Alignment.CENTER,
                        padding=20,
                    ),
                ]),
                width=1200,
                padding=40,
            )
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER)
    )

if __name__ == "__main__":
    hilo_api = threading.Thread(target=arrancar_api_en_fondo, daemon=True)
    hilo_api.start()

    ft.app(target=main)