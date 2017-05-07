import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.sound.midi.SysexMessage;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.ShutdownChannelGroupException;
import java.awt.event.ActionEvent;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	JButton btnGuardarBibtex;
	JButton btnGuardarComo;
	private JEditorPane editorPane;
	PDFReader pdfreader;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {

		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch (Exception e) {
			System.err.println("Error setting look and feel");
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);

		JMenu mnArchivo = new JMenu("Archivo");
		menuBar.add(mnArchivo);

		JMenuItem mntmAbrirFichero = new JMenuItem("Abrir fichero");
		mntmAbrirFichero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					pdfreader = new PDFReader();
					pdfreader.processPDF(selectedFile);
					pdfreader.generateCompleteString();
					saveButtons(true);

					editorPane.setText(pdfreader.COMPLETE_STRING);
				}

			}


		});
		mnArchivo.add(mntmAbrirFichero);

		JMenuItem mntmAbrirDirectorio = new JMenuItem("Abrir Carpeta");
		mnArchivo.add(mntmAbrirDirectorio);

		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);

		JPanel left_panel = new JPanel();
		splitPane.setLeftComponent(left_panel);
		left_panel.setLayout(new BorderLayout(0, 0));

		JDesktopPane desktopPane = new JDesktopPane();
		left_panel.add(desktopPane, BorderLayout.NORTH);
		desktopPane.setLayout(new BorderLayout(0, 0));

		btnGuardarBibtex = new JButton("Guardar Bibtex");
		btnGuardarBibtex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				printInFile(null);
			}
		});
		desktopPane.add(btnGuardarBibtex, BorderLayout.NORTH);
		btnGuardarBibtex.setEnabled(false);

		btnGuardarComo = new JButton("Guardar Bibtex como...");
		btnGuardarComo.setEnabled(false);
		btnGuardarComo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if (fileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
					printInFile(fileChooser.getSelectedFile());

				}
			}
		});
		desktopPane.add(btnGuardarComo, BorderLayout.SOUTH);

		editorPane = new JEditorPane();
		JScrollPane scroll = new JScrollPane(editorPane);
		splitPane.setRightComponent(scroll);

	}

	protected void printInFile(File selectedFile) {
		String filename;
		try{
			if(selectedFile==null){
				filename = pdfreader.p.getNombreFormato()+".bib";
				selectedFile = new File(filename);
			}
			
			BufferedWriter output = new BufferedWriter(new FileWriter(selectedFile));
			output.write(editorPane.getText());
			output.close();
			JOptionPane.showMessageDialog(contentPane, "Documento guardado", "INFO", JOptionPane.INFORMATION_MESSAGE);

		} catch (FileNotFoundException e2) {
			JOptionPane.showMessageDialog(contentPane, "File not found", "ERROR", JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedEncodingException e1) {
			JOptionPane.showMessageDialog(contentPane, "Unsupporded econding.", "ERROR", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(contentPane, "IOException", "ERROR", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void saveButtons(boolean b) {
		btnGuardarBibtex.setEnabled(b);
		btnGuardarComo.setEnabled(true);
	}

}
