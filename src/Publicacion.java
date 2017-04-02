import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.SysexMessage;

@SuppressWarnings("all")
public class Publicacion {

	private String titulo;
	private String autor;
	private String cadena_contenido;
	private ArrayList<String> colaboradores;
	private String soporte;
	private String tipo_produccion;
	private String paginas;
	private String agno;
	
	private String ISSN;
	private String ISBN;
	
	//Se sabe que siempre se indica primero el tipo de producion y luego el tipo de soporte
	private final static String tipo_produccion_flag = "Tipo de producción:";
	
	private final static String tipo_produccion_articulo = "Artículo científico ";
	private final static String tipo_produccion_capitulo = "Capítulo de libro";
	private final static String tipo_produccion_capitulos = "Capítulos de libros";
	private final static String tipo_produccion_libro = "Libro o monografía científica";
	private final static String tipo_produccion_bibliografia = "Bibliografía";
	private final static String tipo_producicon_prologo = "Prólogo";
	private final static String tipo_produccion_informe = "Informe científico-técnico";
	private final static String tipo_produccion_resegna = "Reseña";
	
	private final static String[] tipos_produccion = {tipo_produccion_articulo,
													  tipo_produccion_capitulo,
													  tipo_produccion_capitulos,
													  tipo_produccion_libro,
													  tipo_produccion_bibliografia,
													  tipo_producicon_prologo,
													  tipo_produccion_informe,
													  tipo_produccion_resegna};
	
	private final static String tipo_soporte_flag = "Tipo de soporte:";
	
	private final static String soporte_libro = " Libro";
	private final static String soporte_revista = " Revista";
	
	private final static String[] soportes = {soporte_libro,soporte_revista};
	
	private final static String ISBN_index = "ISBN";
	private final static String ISSN_index = "ISSN";
	private final static String Paginas_index = "pp. ";
	
	
	private final static Pattern PAGES_PATTERN = Pattern.compile(" ([0-9][0-9][0-9]|[0-9][0-9]|[0-9]) +- +([0-9][0-9][0-9]|[0-9][0-9]|[0-9])");
	private final static Pattern YEAR_PATTERN = Pattern.compile("[1-2][0-9][0-9][0-9]");
	private final static Pattern ISSN_PATTERN = Pattern.compile("ISSN ....-....");
	private final static Pattern ISBN_PATTERN = Pattern.compile("ISBN .{17}");//Consta de 13 caracteres y 4 -
	//BUG CON LOS ISBN DE 10 digitos
	
	int idex_publicacion; //indice en el array.
	int numero_publicacion; //numero que aparece delante de cada publicacion

	private String BibTex_string;


	public Publicacion(String contenido, int i){
		colaboradores = new ArrayList<String>();
		cadena_contenido = contenido;
		idex_publicacion = i;
		numero_publicacion = i+1;

	}
	public void setAutorPrincipal(String p){
		autor = p;
	}
	public String removeMetadata(String data){
		return data.replaceAll("\n|\r"," ");
	}
	
	//Limpia los datos y rellena cada uno de los campos
	public void cleanData() {
		//Limpia saltos de pagina
		autor = removeMetadata(autor);
		cadena_contenido = removeMetadata(cadena_contenido);
		//Borra el numero de publicacion
		cadena_contenido = cadena_contenido.replaceFirst(Integer.toString(numero_publicacion)+" ","");
		
		//Saca y almacena cada uno de los autores separados por ; busca el primer . que separa el ultimo autor con el titulo
		int index = 0;
		while(true){
			index = cadena_contenido.indexOf(";");
			if(index==-1) break;
			colaboradores.add(removeMetadata(cadena_contenido.substring(0,index)));
			cadena_contenido = cadena_contenido.substring(index+1);
		}
		//Saca el año y el ISSN/ISBN en caso de que los haya
		Matcher j = YEAR_PATTERN.matcher(cadena_contenido);
		if(j.find()){
			agno = j.group();
		}
		j = ISSN_PATTERN.matcher(cadena_contenido);
		if(j.find()){
			ISSN = j.group().substring("ISSN".length()).trim();
		}
		j = ISBN_PATTERN.matcher(cadena_contenido);
		if(j.find()){
			ISBN = j.group().substring("ISBN".length()).trim();
		}
		//Comenzamos a extraer informacion de atras hacia delante
		//Tipo de soporte
		int soporte_index, produccion_index = 0;
		soporte_index = cadena_contenido.indexOf(tipo_soporte_flag);
		if(soporte_index!=-1){
			String sop = cadena_contenido.substring(soporte_index,cadena_contenido.length());
			for (int i = 0; i < soportes.length; i++) {
				if(sop.contains(soportes[i])){
					soporte = soportes[i];
					break;
				}
			}
			//Desheachamos todo lo que esta despues de soportes
			cadena_contenido = cadena_contenido.substring(0,soporte_index);
		}
		//Producion
		produccion_index = cadena_contenido.indexOf(tipo_produccion_flag);
		if(produccion_index!=-1){
			String pro = cadena_contenido.substring(produccion_index);
			for (int i = 0; i < tipos_produccion.length; i++) {
				if(pro.contains(tipos_produccion[i])){
					tipo_produccion = tipos_produccion[i];
				}
			}
			cadena_contenido = cadena_contenido.substring(0,produccion_index);
		}
		int pages_index = cadena_contenido.indexOf(Paginas_index);
		if(pages_index!=-1){
			Matcher m = PAGES_PATTERN.matcher(cadena_contenido.substring(pages_index));
			if(m.find()){
				paginas = m.group();
			}else{
				System.err.println("Error econtrado el numero de paginas");
			}
			cadena_contenido = cadena_contenido.substring(0,pages_index);
		}
		titulo = cadena_contenido;
		titulo = titulo.trim();
		if(titulo.charAt(titulo.length()-1)==','){
			titulo = titulo.substring(0,titulo.length()-1);
		}
	}
	public String generateBibTexRow(String field, String content){
		return "\t"+field+"={"+content+"},\n";
	}

	public String generateBibTexString(){
		StringBuilder builder = new StringBuilder();
		if(soporte==soporte_libro){
			builder.append("@book{CVN_ORCID,\n");
		}else{
			builder.append("@article{CVN_ORCID,\n");
		}
		builder.append(generateBibTexRow("title", titulo));
		builder.append(generateBibTexRow("author", getCadenaAutoresBibTex()));
		if(paginas!=null) builder.append(generateBibTexRow("pages", paginas));
		if(agno!=null)    builder.append(generateBibTexRow("year", agno));
		if(ISBN!=null)    builder.append(generateBibTexRow("isbn", ISBN));
		if(ISSN!=null)    builder.append(generateBibTexRow("issn", ISSN));
		
		builder.append("}");
		return builder.toString();
	}
	
	private String getCadenaAutoresBibTex() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < colaboradores.size(); i++) {
			sb.append(colaboradores.get(i));
				sb.append(" and ");
		}
		sb.append(autor);
		return sb.toString();
	}
	
	public String getCadenaContenido() {
		// TODO Auto-generated method stub
		return cadena_contenido;
	}
}
