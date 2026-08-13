
public class Personne {
// les attributs de personnes 
	
	String nom ; 
	String prenom;
	String date_Naissance ; 

	public Personne(String nom ,String prenom , String date_naissance) {
	
		this.nom = nom ; 
		this.prenom = prenom ;
		this.date_Naissance = date_naissance;
	}
	
	// getter and setters 
	
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	public String getDate_Naissance() {
		return date_Naissance;
	}

	public void setDate_Naissance(String date_Naissance) {
		this.date_Naissance = date_Naissance;
	}
}
