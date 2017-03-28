import java.util.ArrayList;

public class Publicacion {
	
	private String titulo;
	private String autor;
	private String cadena_contenido;
	private ArrayList<String> colaboradores;
	
	private String BibTex_string;
	
	
	public Publicacion(String contenido){
		cadena_contenido = contenido;
	}
	public void setAutorPrincipal(String p){
		autor = p;
	}
	
	public String getPublicacionAsBibText(){
		return null;
	}

	public void cleanData() {
		autor = autor.replaceAll("\n|\r"," ");
		cadena_contenido = cadena_contenido.replaceAll("\n|\r"," ");
	}
	public String generateBibTexString(){
		String ret = String.format("@article{,\n"+
									"\tauthor={%s},\n"+
									"\ttitle={%s},\n"+
									"}\r"
			                        ,autor,cadena_contenido);
		return ret;
	}
}
