import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.swing.JOptionPane;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFReader {

	public PDFReader() {
		System.out.println("----PDF reader starts----");
	}
	private final static String PUBLICATIONS_INIT_FLAG = "Publicaciones, documentos científicos y técnicos";
	private final static String CONGRESOS_INIT_FLAG = "Trabajos presentados en congresos nacionales o internacionales";

	private final static String PUBLICATIONS_END_FLAG_0  = "I+D+i y participación en comités científicos";
	private final static String PUBLICATIONS_END_FLAG_1  = "Trabajos presentados en congresos nacionales o internacionales";

	public String COMPLETE_STRING;

	private Publicacion[] publicaciones;
	private Congreso[] congresos;

	public Persona p;

	//pdf el nombre debera contener la extension -> XXXX.pdf
	public void processPDF(File f){

		try {

			PDDocument document = null;
			document = PDDocument.load(f);
			document.getClass();

			if (!document.isEncrypted()) {

				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);

				PDFTextStripper reader = new PDFTextStripper();
				reader.setStartPage(1);
				reader.setEndPage(1);
				String personalData = reader.getText(document);

				p = new Persona(personalData);

				PDFTextStripper Tstripper = new PDFTextStripper();
				String string = Tstripper.getText(document);

				String REMOVE_CVCODE_AND_PAGES_REGEX = p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9][0-9][0-9]"+"|"+
						p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9][0-9]"+"|"+
						p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9]";

				string = string.replaceAll(REMOVE_CVCODE_AND_PAGES_REGEX,"");

				StringBuilder contenido = new StringBuilder(string);
				StringBuilder publis;
				StringBuilder congres;

				publis = getPublicaciones(contenido);
				congres = getCongresos(contenido);

				String[] array_publicaciones = getPublicacionesArray(publis);
				String[] array_congresos = getCongresosArray(congres);

				publicaciones = new Publicacion[array_publicaciones.length];
				congresos = new Congreso[array_congresos.length];

				for (int i = 0; i < array_publicaciones.length; i++) {
					publicaciones[i] = new Publicacion(array_publicaciones[i],i);
					publicaciones[i].setAutorPrincipal(p.getNombre());
					publicaciones[i].cleanData();

				}

				for (int i = 0; i < array_congresos.length; i++) {
					congresos[i] = new Congreso(array_congresos[i],i);
					congresos[i].cleanData();

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void generateCompleteString(){
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < p.lines.length; i++) {
			sb.append("% "+p.lines[i]);
		}
		sb.append("\n%"+PUBLICATIONS_INIT_FLAG+"\n");
		for (int i = 0; i < publicaciones.length; i++) {
			sb.append(publicaciones[i].generateBibTexString());
		}
		sb.append("\n%"+CONGRESOS_INIT_FLAG+"\n");
		for (int i = 0; i < congresos.length; i++) {
			sb.append(congresos[i].generateBibTexString());
		}

		COMPLETE_STRING = sb.toString();
	}


	private String[] getPublicacionesArray(StringBuilder publicaciones) {


		ArrayList<String> lst = new ArrayList<String>();
		int init_index=0, end_index=0, obra_index = 1;
		boolean flag = true;
		while(flag){


			init_index = publicaciones.indexOf("\n"+obra_index+" ");
			end_index = publicaciones.indexOf("\n"+(obra_index+1)+" ");

			if(end_index==-1){
				flag = false;
				end_index = init_index + 200;
			}
			try{
				lst.add(publicaciones.substring(init_index, end_index));
			}catch (Exception e) {
				System.err.printf("Error añadiendo obra %d, init_index-> %d, end_indlex->%d\n",obra_index,init_index,end_index);
			}
			try{
				publicaciones.delete(init_index, end_index);
			}catch(StringIndexOutOfBoundsException e){
				System.err.println("Error borrando la obra-> " + (obra_index));
			}
			obra_index++;

		}
		//Devuelve el string de las publicaciones;
		String[] ret = new String[lst.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]= lst.get(i);
		}	
		return ret;
	}

	private String[] getCongresosArray(StringBuilder congresos) {


		ArrayList<String> lst = new ArrayList<String>();
		int init_index=0, end_index=0, obra_index = 1;
		boolean flag = true;
		while(flag){


			init_index = congresos.indexOf("\n"+obra_index+" ");
			end_index = congresos.indexOf("\n"+(obra_index+1)+" ");

			if(end_index==-1 || end_index-init_index>1500){
				flag = false;
				end_index = init_index + 200;
			}
			try{
				lst.add(congresos.substring(init_index, end_index));
			}catch (Exception e) {
				System.err.printf("Error añadiendo obra %d, init_index-> %d, end_indlex->%d\n",obra_index,init_index,end_index);
			}
			try{
				congresos.delete(init_index, end_index);
			}catch(StringIndexOutOfBoundsException e){
				System.err.println("Error borrando la obra-> " + (obra_index));
			}
			obra_index++;

		}
		//Devuelve el string de las publicaciones;
		String[] ret = new String[lst.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i]= lst.get(i);
		}	
		return ret;
	}

	//Devuelve un String desde el flag de las publicaciones hasta el final
	private StringBuilder getPublicaciones(StringBuilder st) {

		int publications_init_index = st.indexOf(PUBLICATIONS_INIT_FLAG);
		if(publications_init_index==-1){
			JOptionPane.showMessageDialog(null, "No se ha encontrado la cadena que marca el inicio de las publicaciones", "ERROR", JOptionPane.ERROR_MESSAGE);
			while(true){
				String newflag = JOptionPane.showInputDialog("Introduzca la cadena de inicio");
				if(newflag!=null){
					publications_init_index = st.indexOf(newflag);
					if(publications_init_index!=-1) break;
					else
						JOptionPane.showMessageDialog(null, "Cadena no encontrada, intentelo otra vez o cancele", "ERROR", JOptionPane.ERROR_MESSAGE);
				}else{
					return new StringBuilder("");
				}
			}
		}

		StringBuilder publicaciones = new StringBuilder(st.substring(publications_init_index));

		return publicaciones;
	}
	//Devuelve un String desde el flag de las publicaciones hasta el final
	private StringBuilder getCongresos(StringBuilder st) {

		int publications_init_index = st.indexOf(CONGRESOS_INIT_FLAG);
		if(publications_init_index==-1){
			JOptionPane.showMessageDialog(null, "No se ha encontrado la cadena que marca el inicio de los congresos", "ERROR", JOptionPane.ERROR_MESSAGE);
			while(true){
				String newflag = JOptionPane.showInputDialog("Introduzca la cadena de inicio");
				if(newflag!=null){
					publications_init_index = st.indexOf(newflag);
					if(publications_init_index!=-1) break;
					else
						JOptionPane.showMessageDialog(null, "Cadena no encontrada, intentelo otra vez o cancele", "ERROR", JOptionPane.ERROR_MESSAGE);
				}else{
					return new StringBuilder("");
				}
			}
		}


		StringBuilder publicaciones = new StringBuilder(st.substring(publications_init_index));

		return publicaciones;
	}
}

class GlobalConfig {
	public static final boolean DEBUG = true;
	public static final boolean SHOW_ERROR_LOG = false;
}


