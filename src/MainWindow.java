import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
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
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	JButton btnGuardarBibtex;
	JButton btnGuardarComo;
	private JEditorPane editorPane;
	PDFReader pdfreader;

	int current_file;
	File[] files;

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
					/**/
					files = null;
					current_file = -1;
					editorPane.setText(pdfreader.COMPLETE_STRING);
				}

			}


		});
		mnArchivo.add(mntmAbrirFichero);

		JMenuItem mntmAbrirDirectorio = new JMenuItem("Abrir Carpeta");
		mntmAbrirDirectorio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					files = selectedFile.listFiles();

					current_file = 0;
					pdfreader = new PDFReader();
					pdfreader.processPDF(files[current_file]);
					pdfreader.generateCompleteString();
					saveButtons(true);
					editorPane.setText(pdfreader.COMPLETE_STRING);

				}

			}
		});
		mnArchivo.add(mntmAbrirDirectorio);

		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);

		JPanel left_panel = new JPanel();
		splitPane.setLeftComponent(left_panel);
		left_panel.setLayout(new BorderLayout(0, 0));

		JDesktopPane desktopPane = new JDesktopPane();
		left_panel.add(desktopPane, BorderLayout.NORTH);

		btnGuardarBibtex = new JButton("Guardar Bibtex");
		btnGuardarBibtex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				printInFile(null);
				nextFile();
			}
		});
		desktopPane.setLayout(new BorderLayout(0, 0));
		desktopPane.add(btnGuardarBibtex, BorderLayout.NORTH);
		btnGuardarBibtex.setEnabled(false);

		btnGuardarComo = new JButton("Guardar Bibtex como...");
		btnGuardarComo.setEnabled(false);
		btnGuardarComo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("BibTex","bib"));
				if (fileChooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
					printInFile(fileChooser.getSelectedFile());
				}
				nextFile();
			}
		});
		desktopPane.add(btnGuardarComo);
		
		JPanel panel = new JPanel();
		desktopPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel l = new JLabel("Añadir campo a un trabajo");
		panel.add(l);
		
		JButton Titulo = new JButton("Titulo");
		Titulo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				insertStringWhereCaret("title={ },\n");
			}
		});
		panel.add(Titulo);
		
		JButton Autor = new JButton("Autor/Autores");
		Autor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("author={ },\n");
			}
		});
		Autor.setToolTipText("Varios autores deben separarse mediante la palabra and");
		panel.add(Autor);
		
		JButton Fechas = new JButton("Fechas");
		Fechas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("year={ },\n");
			}
		});
		panel.add(Fechas);
		
		JButton Paginas = new JButton("Paginas");
		Paginas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("pages={ - },\n");
			}
		});
		panel.add(Paginas);
		
		JButton ISSN = new JButton("ISSN");
		ISSN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("issn={},\n");
			}
		});
		panel.add(ISSN);
		
		JButton ISBN = new JButton("ISBN");
		ISBN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("isbn={ },\n");
			}
		});
		panel.add(ISBN);
		
		JButton journal = new JButton("Journal");
		journal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("journal={ },\n");
			}
		});
		panel.add(journal);
		
		JLabel l2 = new JLabel("Añadir Nuevos trabajos");
		panel.add(l2);
		
		JButton libro = new JButton("Nuevo Libro");
		libro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("%Trabajo añadido a mano\n@book{CVN_ORCID,\n}");
			}
		});
		panel.add(libro);
		
		JButton articulo = new JButton("Nuevo articulo");
		articulo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("%Trabajo añadido a mano\n@article{CVN_ORCID,\n}");
			}
		});
		panel.add(articulo);
		
		JButton conferencia = new JButton("Nueva conferencia");
		conferencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertStringWhereCaret("%Trabajo añadido a mano\n@conference{CVN_ORCID,\n}");
			}
		});
		panel.add(conferencia);
		

		editorPane = new JEditorPane();
		JScrollPane scroll = new JScrollPane(editorPane);
		splitPane.setRightComponent(scroll);

	}

	protected void nextFile() {
		if(files!=null){
			if(current_file < files.length-1){
				current_file++;
				pdfreader.processPDF(files[current_file]);
				pdfreader.generateCompleteString();
				saveButtons(true);
				editorPane.setText(pdfreader.COMPLETE_STRING);
			}else{
				current_file = -1;
				files = null;
			}
		}


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
	
	private void insertStringWhereCaret(String str){
		
		  Document doc = editorPane.getDocument();
	      try {
			doc.insertString(editorPane.getCaretPosition(), str, null);
		} catch (BadLocationException e) {
			JOptionPane.showMessageDialog(contentPane, "No se puede introducir cadena ahi.", "ERROR", JOptionPane.ERROR_MESSAGE);
		}
	}


}
