import flet as ft
import json
import tkinter as tk
from tkinter import filedialog
import os
from datetime import datetime

COLORES = {
    "primario": "#003366",
    "secundario": "#00509E",
    "fondo": "#f4f7f9",
    "exito": "#2E7D32",
    "error": "#C62828",
    "advertencia": "#F9A825",
    "texto_oscuro": "#212121",
    "texto_gris": "#757575",
    "blanco": "#FFFFFF",
    "borde": "#E0E0E0"
}

def main(page: ft.Page):
    page.title = "UNAPEC - Sistema de Pagos"
    page.bgcolor = COLORES["fondo"]
    page.padding = 20
    page.theme_mode = ft.ThemeMode.LIGHT
    page.window_min_width = 1000
    page.window_min_height = 700

    txt_estado = ft.Text("Seleccione un archivo JSON...", size=14, color=COLORES["texto_gris"])
    
    lbl_matricula = ft.Text("---", size=20, weight=ft.FontWeight.BOLD, color=COLORES["primario"])
    lbl_estado = ft.Text("---", size=14, weight=ft.FontWeight.W_500)
    lbl_periodo = ft.Text("---", size=14)
    icono_estado = ft.Icon(ft.Icons.INFO, color=COLORES["texto_gris"], size=18)

    lbl_monto = ft.Text("---", size=24, weight=ft.FontWeight.BOLD, color=COLORES["exito"])
    lbl_codigo_pago = ft.Text("---", size=14, weight=ft.FontWeight.BOLD, color=COLORES["texto_oscuro"])
    lbl_fecha_aprobacion = ft.Text("---", size=14, color=COLORES["texto_oscuro"])
    lbl_moneda = ft.Text("---", size=14, color=COLORES["texto_oscuro"])

    lbl_sistema = ft.Text("---", size=12)
    lbl_trans_id = ft.Text("---", size=12)
    lbl_fecha = ft.Text("---", size=12)
    lbl_archivo = ft.Text("---", size=12)
    lbl_ruta = ft.Text("---", size=10)

    contenedor_carga = ft.Column(
        [ft.ProgressRing(), ft.Text("Procesando...")], 
        visible=False, horizontal_alignment="center"
    )

    def actualizar_estado(msg, tipo="info"):
        colores = {"exito": COLORES["exito"], "error": COLORES["error"], "info": COLORES["texto_gris"]}
        txt_estado.value = msg
        txt_estado.color = colores.get(tipo, COLORES["texto_gris"])
        page.update()

    def procesar_json(ruta):
        try:
            print(f"Leyendo archivo: {ruta}") 
            with open(ruta, 'r', encoding='utf-8') as f:
                data = json.load(f)
                print("Datos leídos:", data) 

            meta = data.get("metadata", {})
            fecha_raw = meta.get("timestamp", "")
            
            est = data.get("estudiante", {})
            
            pago = data.get("detalle_pago", {})
            val_codigo = pago.get("codigo_pago", "No encontrado")
            val_monto = float(pago.get("monto_aprobado", 0))
            val_moneda = pago.get("moneda", "DOP")
            val_fecha_aprob = pago.get("fecha_aprobacion", "No disponible")

            print(f"Pago extraído: {val_codigo}, {val_monto}")

            lbl_matricula.value = est.get("matricula", "N/A")
            lbl_estado.value = est.get("estado", "N/A")
            lbl_periodo.value = est.get("descripcion_periodo", "N/A")
            
            estado_str = est.get("estado", "").lower()
            if "activo" in estado_str:
                icono_estado.icon = ft.Icons.CHECK_CIRCLE
                icono_estado.color = COLORES["exito"]
                lbl_estado.color = COLORES["exito"]
            else:
                icono_estado.icon = ft.Icons.ERROR
                icono_estado.color = COLORES["error"]
                lbl_estado.color = COLORES["error"]

            lbl_monto.value = f"${val_monto:,.2f}"
            lbl_codigo_pago.value = str(val_codigo)
            lbl_fecha_aprobacion.value = str(val_fecha_aprob)
            lbl_moneda.value = str(val_moneda)

            lbl_sistema.value = meta.get("sistema_origen", "N/A")
            lbl_trans_id.value = meta.get("transacion_id", "N/A")
            lbl_fecha.value = fecha_raw.replace("T", " ")[:19]
            lbl_archivo.value = os.path.basename(ruta)
            lbl_ruta.value = ruta

            contenedor_resultados.visible = True
            contenedor_carga.visible = False
            actualizar_estado("Datos cargados correctamente", "exito")
            page.update()

        except Exception as e:
            contenedor_carga.visible = False
            print("Error:", e)
            actualizar_estado(f"Error: {str(e)}", "error")
            page.update()

    def seleccionar_archivo(e):
        root = tk.Tk()
        root.withdraw()
        root.wm_attributes('-topmost', 1)
        ruta = filedialog.askopenfilename(filetypes=[("JSON", "*.json")])
        root.destroy()
        if ruta:
            contenedor_carga.visible = True
            page.update()
            import time
            time.sleep(0.1)
            procesar_json(ruta)

    def limpiar(e):
        contenedor_resultados.visible = False
        actualizar_estado("Listo")
        page.update()

    def crear_fila_dato(icono, etiqueta, variable_control):
        return ft.Row([
            ft.Icon(icono, size=16, color=COLORES["primario"]),
            ft.Text(etiqueta, size=14, color=COLORES["texto_gris"]),
            variable_control 
        ])

    card_estudiante = ft.Container(
        content=ft.Column([
            ft.Text("INFORMACIÓN DEL ESTUDIANTE", weight="bold"),
            ft.Divider(),
            ft.Row([lbl_matricula, ft.Container(expand=True), icono_estado]),
            crear_fila_dato(ft.Icons.SCHOOL, "Estado:", lbl_estado),
            crear_fila_dato(ft.Icons.CALENDAR_TODAY, "Periodo:", lbl_periodo),
        ], spacing=10),
        padding=20, bgcolor="white", border_radius=10,
        shadow=ft.BoxShadow(blur_radius=10, color=ft.Colors.BLACK12)
    )

    card_pago = ft.Container(
        content=ft.Column([
            ft.Text("DETALLES DEL PAGO", weight="bold"),
            ft.Divider(),
            ft.Text("Monto Aprobado", size=12, color=COLORES["texto_gris"]),
            lbl_monto,
            ft.Divider(),
            crear_fila_dato(ft.Icons.QR_CODE, "Código:", lbl_codigo_pago),
            crear_fila_dato(ft.Icons.CALENDAR_MONTH, "Fecha:", lbl_fecha_aprobacion),
            crear_fila_dato(ft.Icons.MONETIZATION_ON, "Moneda:", lbl_moneda),
        ], spacing=10),
        padding=20, bgcolor="white", border_radius=10,
        shadow=ft.BoxShadow(blur_radius=10, color=ft.Colors.BLACK12)
    )

    card_metadata = ft.Container(
        content=ft.Column([
            ft.Text("METADATA", weight="bold"),
            ft.Divider(),
            crear_fila_dato(ft.Icons.COMPUTER, "Sistema:", lbl_sistema),
            crear_fila_dato(ft.Icons.RECEIPT_LONG, "ID Trans:", lbl_trans_id),
            crear_fila_dato(ft.Icons.ACCESS_TIME, "Fecha:", lbl_fecha),
            ft.Divider(),
            lbl_archivo,
            lbl_ruta
        ], spacing=10),
        padding=20, bgcolor="white", border_radius=10,
        shadow=ft.BoxShadow(blur_radius=10, color=ft.Colors.BLACK12)
    )

    contenedor_resultados = ft.Column(
        controls=[
            card_estudiante,
            ft.Container(height=10),
            ft.Row([card_pago, card_metadata], alignment=ft.MainAxisAlignment.START, vertical_alignment=ft.CrossAxisAlignment.START),
        ],
        visible=False
    )

    page.add(
        ft.Container(
            content=ft.Column([
                ft.Text("UNAPEC - Procesamiento", size=24, weight="bold", color="white"),
                ft.Container(height=10),
                ft.Row([
                    ft.ElevatedButton("Cargar JSON", icon=ft.Icons.UPLOAD, on_click=seleccionar_archivo),
                    ft.OutlinedButton("Limpiar", icon=ft.Icons.CLEAR, on_click=limpiar, style=ft.ButtonStyle(color="white"))
                ]),
                txt_estado,
                contenedor_carga
            ]),
            bgcolor=COLORES["primario"], padding=20, border_radius=10
        ),
        ft.Container(height=20),
        contenedor_resultados
    )

if __name__ == "__main__":
    ft.app(target=main)