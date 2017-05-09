import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Congreso extends Publicacion {

	String contenido;
	int id;
	String titulo, nombre, fecha;

	Pattern year = Pattern.compile("[0-9][0-9]/[0-9][0-9]/[0-9][0-9][0-9][0-9]");
	Pattern title = Pattern.compile("Título: *\r");
	Pattern name = Pattern.compile("Nombre del congreso: *\r");


	public Congreso(String string, int i) {
		super(string,i);
		contenido = string; id = i;
	}

	public void cleanData() {
		try{
			Matcher m = year.matcher(contenido);
			if(m.find()){
				fecha = m.group();
			}
			int init_index, end_index ;
			init_index = contenido.indexOf("Título del trabajo:");
			end_index = contenido.indexOf("Nombre del congreso:");
			init_index = init_index + new String("Título del trabajo:").length();
			titulo = contenido.substring(init_index,end_index);

			init_index = contenido.indexOf("Nombre del congreso:");
			init_index = init_index + new String("Nombre del congreso:").length();

			end_index = contenido.indexOf("Ciudad de celebración:");		
			if(end_index==-1){
				end_index = contenido.indexOf("Fecha de celebración:");
			}
			if(end_index==-1){
				end_index = contenido.indexOf("Fecha de finalización:");
			}
			if(end_index==-1){
				end_index = contenido.indexOf("Entidad organizadora");
			}

			if(init_index!=-1 && end_index!= -1)
				nombre = contenido.substring(init_index,end_index);
			else
				nombre = "";

			titulo = titulo.trim().replaceAll("\r\n","");
			nombre = nombre.trim().replaceAll("\r\n","");
			fecha = fecha.trim().replaceAll("\r\n", "");
		}catch(Exception e){

		}



	}

	public String generateBibTexString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@conference{CVN_ORCID,\n");
		sb.append(generateBibTexRow("title", titulo));
		sb.append(generateBibTexRow("journal", nombre));
		sb.append(generateBibTexRow("year", fecha));
		sb.append("}\n");
		return sb.toString();
	}
}
