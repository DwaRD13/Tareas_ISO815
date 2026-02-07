"""
Sistema de Recepción de Nómina - Banco APAP
Aplicación de escritorio para cargar y procesar archivos de nómina
"""

import flet as ft
from tkinter import Tk, filedialog

from database import DatabaseManager
from file_handler import FileHandler

db_manager = DatabaseManager()


def main(page: ft.Page):
    page.title = "Banco APAP - Sistema de Nómina"
    page.window.width = 1400
    page.window.height = 900
    page.theme_mode = ft.ThemeMode.LIGHT
    page.padding = 0
    page.bgcolor = "#f8f9fa"
    
    datos_en_memoria = []
    empresa_id_actual = None
    archivo_origen_actual = None

    def cargar_archivo(e):
        root = Tk()
        root.withdraw()
        root.wm_attributes('-topmost', 1)
        
        ruta = filedialog.askopenfilename(
            title="Seleccionar archivo de nómina",
            filetypes=[("Archivos JSON", "*.json"), ("Todos los archivos", "*.*")]
        )
        
        root.destroy()
        
        if ruta:
            input_ruta.value = ruta
            page.update()
            cargar_desde_ruta(ruta)
    
    def cargar_desde_ruta(ruta):
        """Carga los datos desde un archivo de nómina"""
        nonlocal empresa_id_actual, archivo_origen_actual
        
        if not ruta:
            mostrar_mensaje("Por favor selecciona un archivo", "orange")
            return
        
        try:
            datos_en_memoria.clear()
            tabla_datos.rows.clear()
            
            # Usar el FileHandler para leer el archivo
            datos, empresa_id, nombre_archivo = FileHandler.leer_archivo_nomina(ruta)
            
            # Guardar metadatos
            empresa_id_actual = empresa_id
            archivo_origen_actual = nombre_archivo
            
            for fila in datos:
                datos_en_memoria.append(fila)
                tabla_datos.rows.append(
                    ft.DataRow(
                        cells=[
                            ft.DataCell(ft.Text(fila[0], size=13)),
                            ft.DataCell(ft.Text(fila[1], size=13)),
                            ft.DataCell(ft.Text(fila[2], size=13)),
                            ft.DataCell(ft.Text(f"${fila[3]}", size=13, weight="bold", color="#2e7d32")),
                        ]
                    )
                )
            
            contador = len(datos)
            if contador > 0:
                badge_registros.value = str(contador)
                badge_registros.visible = True
                btn_guardar.disabled = False
                btn_guardar.opacity = 1.0
                mostrar_mensaje(f"{contador} registros cargados correctamente", "green")
            else:
                mostrar_mensaje("No se encontraron registros válidos", "orange")
            
            page.update()
            
        except FileNotFoundError:
            mostrar_mensaje("El archivo no existe", "red")
        except Exception as ex:
            mostrar_mensaje(f"Error al leer el archivo: {str(ex)}", "red")


    def guardar_en_nube(e):
        """Guarda los datos cargados en Supabase"""
        if not datos_en_memoria:
            mostrar_mensaje("No hay datos para guardar", "red")
            return

        btn_guardar.disabled = True
        btn_guardar.opacity = 0.5
        btn_cargar.disabled = True
        btn_cargar.opacity = 0.5
        progress_bar.visible = True
        progress_bar.value = 0
        
        mostrar_mensaje("Subiendo datos a Supabase...", "blue")
        page.update()

        registros_exitosos = 0
        registros_fallidos = 0
        total = len(datos_en_memoria)

        for i, fila in enumerate(datos_en_memoria):
            cedula, nombre, cuenta, monto_texto = fila

            # Validar el monto usando FileHandler
            monto_final, estado = FileHandler.validar_monto(monto_texto)
            
            if estado == "APLICADO":
                registros_exitosos += 1
            else:
                registros_fallidos += 1

            try:
                # Usar DatabaseManager para insertar con metadatos
                db_manager.insertar_pago(cedula, nombre, cuenta, monto_final, estado, 
                                       archivo_origen=archivo_origen_actual, empresa_id=empresa_id_actual)
            except Exception as ex:
                print(f"Error Supabase: {ex}")
                mostrar_mensaje("Error de conexión con Supabase", "red")
                btn_guardar.disabled = False
                btn_guardar.opacity = 1.0
                btn_cargar.disabled = False
                btn_cargar.opacity = 1.0
                progress_bar.visible = False
                page.update()
                return

            progress_bar.value = (i + 1) / total
            page.update()

        progress_bar.visible = False
        btn_limpiar.visible = True
        btn_cargar.disabled = False
        btn_cargar.opacity = 1.0
        
        if registros_fallidos == 0:
            mostrar_mensaje(f"Perfecto! {registros_exitosos} registros subidos exitosamente", "green")
        else:
            mostrar_mensaje(f"Completado: {registros_exitosos} OK, {registros_fallidos} con errores", "orange")
        
        page.update()


    def limpiar_todo(e):
        """Reinicia la aplicación limpiando todos los datos"""
        datos_en_memoria.clear()
        tabla_datos.rows.clear()
        input_ruta.value = ""
        progress_bar.value = 0
        progress_bar.visible = False
        badge_registros.visible = False
        badge_registros.value = "0"
        btn_guardar.disabled = True
        btn_guardar.opacity = 0.5
        btn_cargar.disabled = False
        btn_cargar.opacity = 1.0
        btn_limpiar.visible = False
        mostrar_mensaje("Aplicación reiniciada", "grey")
        page.update()


    def mostrar_mensaje(texto, color):
        """Muestra un mensaje de estado en la interfaz"""
        txt_estado.value = texto
        txt_estado.color = color
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
                ft.Text("ASOCIACIÓN POPULAR DE AHORROS Y PRÉSTAMOS", 
                       size=12, weight="w400", color="#666"),
                ft.Text("Sistema de Recepción de Nómina", 
                       size=24, weight="bold", color="#212121"),
            ], spacing=2),
        ], spacing=20, alignment=ft.MainAxisAlignment.START),
        bgcolor="white",
        padding=30,
        border=ft.Border(bottom=ft.BorderSide(2, "#e0e0e0")),
    )

    input_ruta = ft.TextField(
        label="Ruta del archivo",
        hint_text="El archivo se cargará automáticamente al seleccionarlo",
        width=600,
        height=60,
        text_size=14,
        border_color="#1976d2",
        focused_border_color="#1565c0",
        cursor_color="#1976d2",
        read_only=True,
    )

    badge_registros = ft.Container(
        content=ft.Text("0", color="white", size=12, weight="bold"),
        bgcolor="#2e7d32",
        padding=ft.Padding(left=10, right=10, top=5, bottom=5),
        border_radius=12,
        visible=False,
    )

    btn_cargar = ft.Container(
        content=ft.Text("Cargar Archivo", size=15, weight="bold", color="white"),
        bgcolor="#1976d2",
        padding=15,
        border_radius=8,
        ink=True,
        on_click=cargar_archivo,
    )

    card_carga = ft.Container(
        content=ft.Column([
            ft.Row([
                ft.Text("Cargar Datos", size=18, weight="bold", color="#212121"),
                badge_registros,
            ]),
                 ft.Text("Haz clic en el botón para seleccionar tu archivo JSON de nómina", 
                   size=13, color="#666"),
            ft.Divider(height=20, color="transparent"),
            input_ruta,
            ft.Container(height=10),
            btn_cargar,
        ], spacing=10),
        bgcolor="white",
        padding=30,
        border_radius=12,
        shadow=ft.BoxShadow(blur_radius=10, color="#00000010", offset=ft.Offset(0, 4)),
        margin=ft.Margin(left=0, right=0, top=20, bottom=20),
    )

    tabla_datos = ft.DataTable(
        columns=[
            ft.DataColumn(ft.Text("Cédula", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Nombre", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Cuenta", weight="bold", size=14)),
            ft.DataColumn(ft.Text("Monto", weight="bold", size=14)),
        ],
        rows=[],
        border=ft.border.all(1, "#e0e0e0"),
        heading_row_color="#f5f5f5",
        heading_row_height=50,
        data_row_min_height=45,
        horizontal_lines=ft.BorderSide(1, "#f0f0f0"),
    )

    contenedor_tabla = ft.Container(
        content=ft.Column([tabla_datos], scroll=ft.ScrollMode.AUTO),
        bgcolor="white",
        border=ft.border.all(1, "#e0e0e0"),
        border_radius=12,
        padding=20,
        height=350,
        shadow=ft.BoxShadow(blur_radius=10, color="#00000010", offset=ft.Offset(0, 4)),
    )

    btn_guardar = ft.Container(
        content=ft.Text("Subir a Supabase", size=15, weight="bold", color="white"),
        bgcolor="#2e7d32",
        padding=ft.Padding(left=30, right=30, top=15, bottom=15),
        border_radius=8,
        ink=True,
        on_click=guardar_en_nube,
        disabled=True,
        opacity=0.5,
    )

    btn_limpiar = ft.Container(
        content=ft.Text("Reiniciar", size=14, weight="bold", color="white"),
        bgcolor="#757575",
        padding=ft.Padding(left=25, right=25, top=15, bottom=15),
        border_radius=8,
        ink=True,
        on_click=limpiar_todo,
        visible=False,
    )
    
    progress_bar = ft.ProgressBar(
        width=600,
        height=8,
        color="#2e7d32",
        bgcolor="#e0e0e0",
        border_radius=4,
        visible=False,
    )

    txt_estado = ft.Text(
        "",
        size=15,
        weight="w500",
        text_align=ft.TextAlign.CENTER,
    )

    footer = ft.Container(
        content=ft.Column([
            progress_bar,
            ft.Container(height=15),
            txt_estado,
            ft.Container(height=20),
            ft.Row([
                btn_guardar,
                btn_limpiar,
            ], spacing=20, alignment=ft.MainAxisAlignment.CENTER),
        ], horizontal_alignment=ft.CrossAxisAlignment.CENTER),
        padding=30,
    )

    page.add(
        ft.Column([
            header,
            ft.Container(
                content=ft.Column([
                    card_carga,
                    contenedor_tabla,
                    footer,
                ], spacing=0),
                width=1200,
                padding=ft.Padding(left=40, right=40, top=0, bottom=0),
            )
        ], spacing=0, horizontal_alignment=ft.CrossAxisAlignment.CENTER)
    )


if __name__ == "__main__":
    ft.app(target=main)

