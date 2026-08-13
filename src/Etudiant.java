public class Etudiant {
    private String matricule;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private Note note;

    // Constructeur
    public Etudiant(String matricule, String nom, String prenom, String dateNaissance, Note note) {
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.note = note;
    }

    // Getters et Setters
    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

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
        return dateNaissance;
    }

    public void setDate_Naissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public double getMoyenne() {
        return note != null ? note.getMoyenne() : 0.0;
    }
    public void ajouterNote(String matiere, double valeur) {
        if (note == null) {
            note = new Note();
        }
        note.ajouter(matiere, valeur);
    }
}