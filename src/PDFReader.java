import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFReader {

	public PDFReader() {
		System.out.println("----PDF reader starts----");
	}
	private final static String PUBLICATIONS_INIT_FLAG = "Publicaciones, documentos científicos y técnicos";
	private final static String PUBLICATIONS_END_FLAG_0  = "I+D+i y participación en comités científicos";
	private final static String PUBLICATIONS_END_FLAG_1  = "Trabajos presentados en congresos nacionales o internacionales";

	private final static Pattern PATTERN_STARTS_WITH_NUMBER = Pattern.compile("^[0-9] |^[0-9][0-9] |^[0-9][0-9][0-9] ");
	
	private Publicacion[] publicaciones;


	//pdf el nombre debera contener la extension -> XXXX.pdf
	public void processPDF(String pdfName){

		try {
			PrintWriter writer = new PrintWriter(pdfName+".bib", "UTF-8");
			
			PDDocument document = null;
			File f = new File("Resources/"+pdfName+".pdf");
			document = PDDocument.load(f);
			document.getClass();

			if (!document.isEncrypted()) {
				
				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);
				
				PDFTextStripper reader = new PDFTextStripper();
				reader.setStartPage(1);
				reader.setEndPage(1);
				String personalData = reader.getText(document);
				
				Persona p = new Persona(personalData);
				
				PDFTextStripper Tstripper = new PDFTextStripper();
				String string = Tstripper.getText(document);
				
				String REMOVE_CVCODE_AND_PAGES_REGEX = p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9][0-9][0-9]"+"|"+
							   						   p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9][0-9]"+"|"+
							   						   p.curriculum_code+"(\r\n|\n\r|(\r|\n))[0-9]";
				
				string = string.replaceAll(REMOVE_CVCODE_AND_PAGES_REGEX,"");
				
				StringBuilder st = new StringBuilder(string);
				
				st = getPublicaciones(st);
				String[] array_publicaciones = getPublicacionesArray(st);
				publicaciones = new Publicacion[array_publicaciones.length];
				
				for (int i = 0; i < array_publicaciones.length; i++) {
					publicaciones[i] = new Publicacion(array_publicaciones[i]);
					publicaciones[i].setAutorPrincipal(p.getNombre());
					publicaciones[i].cleanData();
					
					writer.print(publicaciones[i].generateBibTexString());
					if(GlobalConfig.DEBUG) System.out.print(array_publicaciones[i]+"\n---\n");
				}
				writer.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
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
				end_index = publicaciones.length();
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

	//Devuelve un String con el apartado de publicaciones
	private StringBuilder getPublicaciones(StringBuilder st) {

		int publications_init_index = st.indexOf(PUBLICATIONS_INIT_FLAG);
		if(publications_init_index==-1){
			System.err.println("No se ha encontrado el inicio de la seccion de obras");
			System.exit(-1);
		}
		
		StringBuilder publicaciones = new StringBuilder(st.substring(publications_init_index));
		int delete_init_index; //Acota el margen de borrado en caso de que no haya mas obras despues del documento.

		
		delete_init_index = publicaciones.indexOf(PUBLICATIONS_END_FLAG_1);
		if(delete_init_index==-1){
			delete_init_index = publicaciones.indexOf(PUBLICATIONS_END_FLAG_0);
		}
		if(delete_init_index==-1){
			System.out.println("---No se ha encontrado flag final---");
		}
		
		if(delete_init_index!=-1) //Se borra todo lo que haya despues del flag final
			publicaciones = new StringBuilder(publicaciones.substring(0,delete_init_index));

		return publicaciones;
	}
}
