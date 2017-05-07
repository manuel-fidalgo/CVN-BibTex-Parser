
public class Persona {

	public String nombre;
	public String curriculum_code;
	public String firtsPageContent;
	public String[] lines;
	
	//Toma el contenido de la primera pagina y almacena todos sus datos.
	public Persona(String fstPage){
		
		firtsPageContent = fstPage;
		lines = firtsPageContent.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if(GlobalConfig.DEBUG) System.out.print("Line "+i+" -> "+lines[i]);
		}
		nombre = lines[2];
		curriculum_code = lines[lines.length-1];
		curriculum_code = curriculum_code.replaceAll("\r","");
	}
	
	public String getNombre(){
		return nombre;
	}
	public String getNombreFormato(){
		String ret;
		ret = nombre.trim();
		ret = ret.replaceAll("\n","");
		ret = ret.replaceAll("\r", "");
		ret = ret.replaceAll(" ","");
		return ret;
	}
}
