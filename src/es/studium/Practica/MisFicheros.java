package es.studium.Practica;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MisFicheros extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField txtExtension;
    private JTextArea textAreaResultados;
    private JButton btnBuscar;

    public MisFicheros() {
        setTitle("Mis Ficheros");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        textAreaResultados = new JTextArea();
        textAreaResultados.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textAreaResultados);
        add(scrollPane, BorderLayout.CENTER);
        JPanel panelInferior = new JPanel(new FlowLayout());
        txtExtension = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        panelInferior.add(txtExtension);
        panelInferior.add(btnBuscar);
        add(panelInferior, BorderLayout.SOUTH);
        btnBuscar.addActionListener(e -> buscarFicheros());

        textAreaResultados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    try {
                        int pos = textAreaResultados.viewToModel(e.getPoint());
                        int lineaInicio = textAreaResultados.getLineStartOffset(textAreaResultados.getLineOfOffset(pos));
                        int lineaFin = textAreaResultados.getLineEndOffset(textAreaResultados.getLineOfOffset(pos));
                        String rutaFichero = textAreaResultados.getText(lineaInicio, lineaFin - lineaInicio).trim();

                        if (rutaFichero.toLowerCase().endsWith(".exe")) {
                            abrirProceso(rutaFichero);
                        } else {
                            JOptionPane.showMessageDialog(MisFicheros.this, 
                                "Solo se pueden ejecutar archivos con extensión .exe mediante doble clic.", 
                                "Aviso", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MisFicheros.this, 
                            "Error al procesar la selección: " + ex.getMessage(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private void buscarFicheros() {
        String extension = txtExtension.getText().trim();
        if (extension.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, introduce una extensión (ej. .exe, .dll, .txt).", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        textAreaResultados.setText("Buscando ficheros con extensión " + extension + " en todas las unidades...\n");
        final String extBusqueda = extension.toLowerCase();

        new Thread(() -> {
            File[] roots = File.listRoots();
            List<String> encontrados = new ArrayList<>();
            
            for (File root : roots) {
                buscarRecursivo(root, extBusqueda, encontrados);
            }

            SwingUtilities.invokeLater(() -> {
                textAreaResultados.setText("");
                if (encontrados.isEmpty()) {
                    textAreaResultados.append("No se encontraron ficheros con la extensión " + extBusqueda + ".\n");
                } else {
                    for (String fichero : encontrados) {
                        textAreaResultados.append(fichero + "\n");
                    }
                    textAreaResultados.append("\n--- Búsqueda finalizada. Total encontrados: " + encontrados.size() + " ---");
                }
            });
        }).start();
    }

    private void buscarRecursivo(File directorio, String extension, List<String> lista) {
        if (directorio == null || !directorio.isDirectory()) {
            return;
        }
        File[] ficheros = directorio.listFiles();
        if (ficheros != null) {
            for (File f : ficheros) {
                try {
                    if (f.isDirectory()) {
                        buscarRecursivo(f, extension, lista);
                    } else {
                        if (f.getName().toLowerCase().endsWith(extension)) {
                            lista.add(f.getAbsolutePath());
                        }
                    }
                } catch (SecurityException e) {
                }
            }
        }
    }

    private void abrirProceso(String rutaEjecutable) {
        try {
            ProcessBuilder pb = new ProcessBuilder(rutaEjecutable);
            pb.start();
            JOptionPane.showMessageDialog(this, "Proceso lanzado correctamente:\n" + rutaEjecutable, "Información de Proceso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al ejecutar el proceso: " + ex.getMessage(), "Error de E/S", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MisFicheros().setVisible(true);
        });
    }
}