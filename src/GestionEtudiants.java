import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Comparator;

public class GestionEtudiants {

    private ArrayList<Etudiant> listeEtudiant;

    public GestionEtudiants() {
        listeEtudiant = new ArrayList<>();
    }

    public void ajouterEtudiant(Etudiant e) {
        listeEtudiant.add(e);
    }

    public ArrayList<Etudiant> getListeEtudiant() {
        return listeEtudiant;
    }
    public void trierParMoyenne() {
        listeEtudiant.sort(Comparator.comparingDouble(Etudiant::getMoyenne).reversed());
    }
    public Etudiant rechercherEtudiantParMatricule(String matricule) {
        for (Etudiant e : listeEtudiant) {
            if (e.getMatricule().equals(matricule)) {
                return e;
            }
        }
        return null;
    }

    public void supprimerUnEtudiantParMatricule(String matricule) {
        listeEtudiant.removeIf(e -> e.getMatricule().equals(matricule));
    }

    // ================= SAUVEGARDE =================
    public void sauvegarderEtudiants(String fichier) {

        try {
            File dir = new File("data");
            if (!dir.exists()) dir.mkdir(); // 🔥 crée dossier

            BufferedWriter bw = new BufferedWriter(new FileWriter(fichier));

            for (Etudiant e : listeEtudiant) {
                bw.write(e.getMatricule() + ";" +
                         e.getNom() + ";" +
                         e.getPrenom() + ";" +
                         e.getDate_Naissance());
                bw.newLine();
            }

            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ================= CHARGEMENT =================
    public void chargerEtudiants(String fichier) {

        listeEtudiant.clear(); // 🔥 évite doublons

        try {
            File f = new File(fichier);
            if (!f.exists()) return; // rien à charger

            BufferedReader br = new BufferedReader(new FileReader(fichier));

            String ligne;
            while ((ligne = br.readLine()) != null) {

                String[] parts = ligne.split(";");

                if (parts.length >= 4) {
                    Etudiant e = new Etudiant(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            new Note()
                    );
                    listeEtudiant.add(e);
                }
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
 // ================= SAVE NOTES =================
    public void sauvegarderNotes(String fichier) {

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fichier));

            for (Etudiant e : listeEtudiant) {

                if (e.getNote() != null) {

                    StringBuilder sb = new StringBuilder();
                    sb.append(e.getMatricule());

                    for (Map.Entry<String, Double> entry : e.getNote().getNotes().entrySet()) {
                        sb.append(";").append(entry.getKey())
                          .append(":").append(entry.getValue());
                    }

                    bw.write(sb.toString());
                    bw.newLine();
                }
            }

            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // ================= LOAD NOTES =================
    public void chargerNotes(String fichier) {

        try {
            File f = new File(fichier);
            if (!f.exists()) return;

            BufferedReader br = new BufferedReader(new FileReader(fichier));

            String ligne;
            while ((ligne = br.readLine()) != null) {

                String[] parts = ligne.split(";");

                if (parts.length < 2) continue; // sécurité

                Etudiant e = rechercherEtudiantParMatricule(parts[0]);

                if (e != null) {

                    Note note = new Note();

                    for (int i = 1; i < parts.length; i++) {

                        String[] nv = parts[i].split(":");

                        if (nv.length == 2) {
                            try {
                                note.ajouter(nv[0], Double.parseDouble(nv[1]));
                            } catch (NumberFormatException ex) {
                                System.out.println("Erreur note : " + parts[i]);
                            }
                        }
                    }

                    e.setNote(note);
                }
            }

            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}